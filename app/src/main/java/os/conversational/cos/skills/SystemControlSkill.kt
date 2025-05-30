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
 * Focuses on achievable features: volume control, settings shortcuts, and system status
 * Note: Many system toggles require user interaction due to Android security restrictions
 */
class SystemControlSkill(private val context: Context) : ConversationalSkill() {
    
    override val requiredPermissions = listOf(
        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
        android.Manifest.permission.ACCESS_WIFI_STATE,
        android.Manifest.permission.BLUETOOTH
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
                SystemAction.WIFI_SETTINGS -> openWifiSettings()
                SystemAction.BLUETOOTH_SETTINGS -> openBluetoothSettings()
                SystemAction.DND_SETTINGS -> openDoNotDisturbSettings()
                SystemAction.BRIGHTNESS_SETTINGS -> openBrightnessSettings()
                SystemAction.VOLUME_SET -> setVolume(command.value)
                SystemAction.AIRPLANE_SETTINGS -> openAirplaneModeSettings()
                SystemAction.SYSTEM_STATUS -> getSystemStatus()
                else -> ConversationResponse.error("I don't understand that system command. Try 'open WiFi settings' or 'set volume to 50%'")
            }
        } catch (e: Exception) {
            ConversationResponse.error("System control failed: ${e.message}")
        }
    }
    
    private fun parseSystemCommand(input: String): SystemCommand {
        return when {
            input.contains("wifi") || input.contains("wi-fi") -> {
                SystemCommand(SystemAction.WIFI_SETTINGS)
            }
            
            input.contains("bluetooth") -> {
                SystemCommand(SystemAction.BLUETOOTH_SETTINGS)
            }
            
            input.contains("do not disturb") || input.contains("dnd") || input.contains("silent") -> {
                SystemCommand(SystemAction.DND_SETTINGS)
            }
            
            input.contains("brightness") -> {
                SystemCommand(SystemAction.BRIGHTNESS_SETTINGS)
            }
            
            input.contains("volume") -> {
                val value = extractPercentage(input) ?: 0.5f
                SystemCommand(SystemAction.VOLUME_SET, value = value)
            }
            
            input.contains("airplane") || input.contains("flight mode") -> {
                SystemCommand(SystemAction.AIRPLANE_SETTINGS)
            }
            
            input.contains("status") || input.contains("settings") -> {
                SystemCommand(SystemAction.SYSTEM_STATUS)
            }
            
            else -> SystemCommand(SystemAction.UNKNOWN)
        }
    }
    
    private fun openWifiSettings(): ConversationResponse {
        return try {
            val intent = AndroidIntent(Settings.ACTION_WIFI_SETTINGS)
            intent.flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            ConversationResponse.success(
                "Opening WiFi settings for you",
                mapOf("action" to "wifi_settings")
            )
        } catch (e: Exception) {
            ConversationResponse.error("Could not open WiFi settings")
        }
    }
    
    private fun openBluetoothSettings(): ConversationResponse {
        return try {
            val intent = AndroidIntent(Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            ConversationResponse.success(
                "Opening Bluetooth settings for you",
                mapOf("action" to "bluetooth_settings")
            )
        } catch (e: Exception) {
            ConversationResponse.error("Could not open Bluetooth settings")
        }
    }
    
    private fun openDoNotDisturbSettings(): ConversationResponse {
        return try {
            val intent = AndroidIntent(Settings.ACTION_SOUND_SETTINGS)
            intent.flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            ConversationResponse.success(
                "Opening sound settings where you can adjust Do Not Disturb",
                mapOf("action" to "dnd_settings")
            )
        } catch (e: Exception) {
            ConversationResponse.error("Could not open sound settings")
        }
    }
    
    private fun openBrightnessSettings(): ConversationResponse {
        return try {
            val intent = AndroidIntent(Settings.ACTION_DISPLAY_SETTINGS)
            intent.flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            ConversationResponse.success(
                "Opening display settings where you can adjust brightness",
                mapOf("action" to "brightness_settings")
            )
        } catch (e: Exception) {
            ConversationResponse.error("Could not open display settings")
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
    
    private fun openAirplaneModeSettings(): ConversationResponse {
        return try {
            val intent = AndroidIntent(Settings.ACTION_WIRELESS_SETTINGS)
            intent.flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            ConversationResponse.success(
                "Opening network settings where you can toggle airplane mode",
                mapOf("action" to "airplane_settings")
            )
        } catch (e: Exception) {
            ConversationResponse.error("Could not open network settings")
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
    WIFI_SETTINGS,
    BLUETOOTH_SETTINGS,
    DND_SETTINGS,
    BRIGHTNESS_SETTINGS,
    VOLUME_SET,
    AIRPLANE_SETTINGS,
    SYSTEM_STATUS,
    UNKNOWN
}
