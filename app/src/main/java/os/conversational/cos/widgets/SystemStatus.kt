package os.conversational.cos.widgets

/**
 * Represents the current system status with multiple settings
 */
data class SystemStatus(
    val wifiEnabled: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val brightness: Float = 0.5f,
    val volumeLevel: Float = 0.5f,
    val doNotDisturbEnabled: Boolean = false,
    val dndUntil: String = "",
    val airplaneModeEnabled: Boolean = false
)