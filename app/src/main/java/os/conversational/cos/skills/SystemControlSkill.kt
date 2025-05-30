package os.conversational.cos.skills

import android.content.Context
import android.net.wifi.WifiManager
import android.bluetooth.BluetoothAdapter
import android.media.AudioManager
import android.provider.Settings
import android.content.Intent as AndroidIntent
import os.conversational.cos.core.*
import os.conversational.cos.widgets.SystemSettings
import os.conversational.cos.widgets.SystemStatus

/**
 * System control skill for managing device settings
 */
class SystemControlSkill(private val context: Context) : ConversationalSkill() {
    
    override val requiredPermissions = listOf(
        android.Manifest.permission.WRITE_SETTINGS,
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.ACCESS_WIFI_STATE,
        android.Manifest.permission.CHANGE_WIFI_STATE
    )
    
    override fun canHandle(intent: os.conversational.cos.core.Intent): Boolean {
        return intent == os.conversational.cos.core.Intent.SYSTEM_CONTROL
    }
    
    override suspend fun processConversation(
        input: String,
        context: ConversationContext
    ): ConversationResponse {
        val command = parseSystemCommand(input.lowercase())
        
        return try {
            when (command.action) {
                SystemAction.WIFI_TOGGLE -> toggleWifi(command.enabled)
                SystemAction.BLUETOOTH_TOGGLE -> toggleBluetooth(command.enabled)
                SystemAction.DND_TOGGLE -> toggleDoNotDisturb(command.enabled, command.duration)
                SystemAction.BRIGHTNESS_SET -> setBrightness(command.value)
                SystemAction.VOLUME_SET -> setVolume(command.value)
                SystemAction.AIRPLANE_MODE -> toggleAirplaneMode(command.enabled)
                SystemAction.SYSTEM_STATUS -> getSystemStatus()
                else -> ConversationResponse.error("I don't understand that system command.")
            }
        } catch (e: Exception) {
            ConversationResponse.error("System control failed: ${e.message}")
        }
    }
    
    private fun parseSystemCommand(input: String): SystemCommand {
        return when {
            input.contains("wifi") || input.contains("wi-fi") -> {
                val enabled = input.contains("on") || input.contains("enable") || input.contains("turn on")
                SystemCommand(SystemAction.WIFI_TOGGLE, enabled = enabled)
            }
            
            input.contains("bluetooth") -> {
                val enabled = input.contains("on") || input.contains("enable") || input.contains("turn on")
                SystemCommand(SystemAction.BLUETOOTH_TOGGLE, enabled = enabled)
            }
            
            input.contains("do not disturb") || input.contains("dnd") || input.contains("silent") -> {
                val enabled = !input.contains("off") && !input.contains("disable")
                val duration = extractDuration(input)
                SystemCommand(SystemAction.DND_TOGGLE, enabled = enabled, duration = duration)
            }
            
            input.contains("brightness") -> {
                val value = extractPercentage(input) ?: 0.5f
                SystemCommand(SystemAction.BRIGHTNESS_SET, value = value)
            }
            
            input.contains("volume") -> {
                val value = extractPercentage(input) ?: 0.5f
                SystemCommand(SystemAction.VOLUME_SET, value = value)
            }
            
            input.contains("airplane") || input.contains("flight mode") -> {
                val enabled = input.contains("on") || input.contains("enable")
                SystemCommand(SystemAction.AIRPLANE_MODE, enabled = enabled)
            }
            
            input.contains("status") || input.contains("settings") -> {
                SystemCommand(SystemAction.SYSTEM_STATUS)
            }
            
            else -> SystemCommand(SystemAction.UNKNOWN)
        }
    }
    
    private fun toggleWifi(enable: Boolean): ConversationResponse {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        return try {
            wifiManager.isWifiEnabled = enable
            val status = if (enable) "enabled" else "disabled"
            ConversationResponse.success(
                "WiFi $status",
                mapOf(
                    "systemStatus" to SystemStatus(wifiEnabled = enable),
                    "action" to "wifi_toggle"
                )
            )
        } catch (e: Exception) {
            ConversationResponse.error("Could not control WiFi. Please check permissions.")
        }
    }
    
    private fun toggleBluetooth(enable: Boolean): ConversationResponse {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        
        if (bluetoothAdapter == null) {
            return ConversationResponse.error("Bluetooth not supported on this device")
        }
        
        return try {
            if (enable && !bluetoothAdapter.isEnabled) {
                // Note: In modern Android, you need to request user permission
                val enableIntent = AndroidIntent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivity(enableIntent)
                ConversationResponse.success("Requesting to enable Bluetooth")
            } else if (!enable && bluetoothAdapter.isEnabled) {
                bluetoothAdapter.disable()
                ConversationResponse.success(
                    "Bluetooth disabled",
                    mapOf(
                        "systemStatus" to SystemStatus(bluetoothEnabled = false),
                        "action" to "bluetooth_toggle"
                    )
                )
            } else {
                val status = if (bluetoothAdapter.isEnabled) "already enabled" else "already disabled"
                ConversationResponse.success("Bluetooth is $status")
            }
        } catch (e: SecurityException) {
            ConversationResponse.error("Bluetooth permission required")
        }
    }
    
    private fun toggleDoNotDisturb(enable: Boolean, duration: String): ConversationResponse {
        // Note: Modern Android requires WRITE_SECURE_SETTINGS permission for DND
        return try {
            val intent = AndroidIntent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
            
            val message = if (enable) {
                val durationText = if (duration.isNotEmpty()) " for $duration" else ""
                "Opening Do Not Disturb settings to enable$durationText"
            } else {
                "Opening Do Not Disturb settings to disable"
            }
            
            ConversationResponse.success(
                message,
                mapOf(
                    "systemStatus" to SystemStatus(doNotDisturbEnabled = enable, dndUntil = duration),
                    "action" to "dnd_toggle"
                )
            )
        } catch (e: Exception) {
            ConversationResponse.error("Could not access Do Not Disturb settings")
        }
    }
    
    private fun setBrightness(value: Float): ConversationResponse {
        return try {
            // Note: Requires WRITE_SETTINGS permission
            val brightnessValue = (value * 255).toInt()
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )
            
            val percentage = (value * 100).toInt()
            ConversationResponse.success(
                "Brightness set to $percentage%",
                mapOf(
                    "systemStatus" to SystemStatus(brightness = value),
                    "action" to "brightness_set"
                )
            )
        } catch (e: SecurityException) {
            ConversationResponse.error("System settings permission required to change brightness")
        }
    }
    
    private fun setVolume(value: Float): ConversationResponse {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        return try {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volumeLevel = (value * maxVolume).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, 0)
            
            val percentage = (value * 100).toInt()
            ConversationResponse.success(
                "Volume set to $percentage%",
                mapOf(
                    "systemStatus" to SystemStatus(volumeLevel = value),
                    "action" to "volume_set"
                )
            )
        } catch (e: Exception) {
            ConversationResponse.error("Could not control volume")
        }
    }
    
    private fun toggleAirplaneMode(enable: Boolean): ConversationResponse {
        // Note: Requires system-level permissions in modern Android
        return try {
            val intent = AndroidIntent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            context.startActivity(intent)
            
            val action = if (enable) "enable" else "disable"
            ConversationResponse.success("Opening airplane mode settings to $action")
        } catch (e: Exception) {
            ConversationResponse.error("Could not access airplane mode settings")
        }
    }
    
    private fun getSystemStatus(): ConversationResponse {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        val wifiEnabled = wifiManager.isWifiEnabled
        val bluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
        val volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / 
                         audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        
        val status = SystemStatus(
            wifiEnabled = wifiEnabled,
            bluetoothEnabled = bluetoothEnabled,
            volumeLevel = volumeLevel
        )
        
        return ConversationResponse.success(
            "Here are your current system settings",
            mapOf(
                "systemStatus" to status,
                "action" to "system_status"
            )
        )
    }
    
    private fun extractDuration(input: String): String {
        val patterns = listOf(
            Regex("for (\\d+) hours?"),
            Regex("for (\\d+) minutes?"),
            Regex("until (\\d{1,2}:\\d{2})"),
            Regex("until (\\d{1,2} ?[ap]m)")
        )
        
        patterns.forEach { pattern ->
            pattern.find(input)?.let { 
                return it.groupValues[1]
            }
        }
        
        return ""
    }
    
    private fun extractPercentage(input: String): Float? {
        val patterns = listOf(
            Regex("(\\d+)%"),
            Regex("to (\\d+)"),
            Regex("set to (\\d+)")
        )
        
        patterns.forEach { pattern ->
            pattern.find(input)?.let { 
                val value = it.groupValues[1].toIntOrNull()
                return value?.let { it / 100f }
            }
        }
        
        return null
    }
}

data class SystemCommand(
    val action: SystemAction,
    val enabled: Boolean = false,
    val value: Float = 0f,
    val duration: String = ""
)

enum class SystemAction {
    WIFI_TOGGLE,
    BLUETOOTH_TOGGLE,
    DND_TOGGLE,
    BRIGHTNESS_SET,
    VOLUME_SET,
    AIRPLANE_MODE,
    SYSTEM_STATUS,
    UNKNOWN
}
