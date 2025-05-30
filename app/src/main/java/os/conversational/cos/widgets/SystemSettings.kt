package os.conversational.cos.widgets

data class SystemSettings(
    val settingName: String,
    val value: Any,
    val type: SettingType
) {
    enum class SettingType {
        BRIGHTNESS,
        VOLUME,
        WIFI,
        BLUETOOTH,
        AIRPLANE_MODE,
        DO_NOT_DISTURB,
        LOCATION,
        NFC,
        ROTATION,
        OTHER
    }
}