package os.conversational.cos.widgets

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val photoUri: String? = null
)