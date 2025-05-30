package os.conversational.cos.skills

import android.content.Context
import android.content.Intent as AndroidIntent
import android.net.Uri
import android.provider.ContactsContract
import android.database.Cursor
import android.Manifest
import os.conversational.cos.core.*
import os.conversational.cos.widgets.Contact

/**
 * Contacts and communication skill
 */
class ContactsSkill(private val context: Context) : ConversationalSkill() {
    
    override val requiredPermissions = listOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.SEND_SMS
    )
    
    override fun canHandle(intent: os.conversational.cos.core.Intent): Boolean {
        return intent == os.conversational.cos.core.Intent.COMMUNICATION
    }
    
    override suspend fun processConversation(
        input: String,
        context: ConversationContext
    ): ConversationResponse {
        val command = parseContactCommand(input.lowercase())
        
        return try {
            when (command.action) {
                ContactAction.CALL -> makeCall(command.contactName, command.phoneNumber)
                ContactAction.TEXT -> sendText(command.contactName, command.phoneNumber, command.message)
                ContactAction.EMAIL -> sendEmail(command.contactName, command.email, command.message)
                ContactAction.FIND_CONTACT -> findContact(command.contactName)
                ContactAction.LIST_RECENT -> listRecentContacts()
                else -> ConversationResponse.error("I don't understand that contact request.")
            }
        } catch (e: Exception) {
            ConversationResponse.error("Contact operation failed: ${e.message}")
        }
    }
    
    private fun parseContactCommand(input: String): ContactCommand {
        return when {
            input.contains("call") ->
                ContactCommand(ContactAction.CALL, extractContactName(input))
            
            input.contains("text") || input.contains("message") ->
                ContactCommand(
                    ContactAction.TEXT, 
                    extractContactName(input),
                    message = extractMessage(input)
                )
            
            input.contains("email") ->
                ContactCommand(
                    ContactAction.EMAIL,
                    extractContactName(input),
                    message = extractMessage(input)
                )
            
            input.contains("find") || input.contains("search") ->
                ContactCommand(ContactAction.FIND_CONTACT, extractContactName(input))
            
            input.contains("recent") || input.contains("last") ->
                ContactCommand(ContactAction.LIST_RECENT)
            
            else -> ContactCommand(ContactAction.UNKNOWN)
        }
    }
    
    private fun makeCall(contactName: String, phoneNumber: String): ConversationResponse {
        val contact = if (contactName.isNotEmpty()) {
            findContactByName(contactName)
        } else if (phoneNumber.isNotEmpty()) {
            Contact("unknown", "", phoneNumber)
        } else {
            return ConversationResponse.error("Please specify who to call")
        }
        
        if (contact == null) {
            return ConversationResponse.error("Contact '$contactName' not found")
        }
        
        if (contact.phoneNumber.isEmpty()) {
            return ConversationResponse.error("No phone number found for ${contact.name}")
        }
        
        // Make the call
        val callIntent = AndroidIntent(AndroidIntent.ACTION_CALL).apply {
            data = Uri.parse("tel:${contact.phoneNumber}")
        }
        
        try {
            context.startActivity(callIntent)
            val displayName = contact.name.ifEmpty() { contact.phoneNumber }
            return ConversationResponse.success(
                "Calling $displayName",
                mapOf(
                    "contact" to contact,
                    "action" to "call"
                )
            )
        } catch (e: SecurityException) {
            return ConversationResponse.error("Phone permission required to make calls")
        }
    }
    
    private fun sendText(contactName: String, phoneNumber: String, message: String): ConversationResponse {
        val contact = if (contactName.isNotEmpty()) {
            findContactByName(contactName)
        } else if (phoneNumber.isNotEmpty()) {
            Contact("unknown", "", phoneNumber)
        } else {
            return ConversationResponse.error("Please specify who to text")
        }
        
        if (contact == null) {
            return ConversationResponse.error("Contact '$contactName' not found")
        }
        
        // Open messaging app with pre-filled message
        val smsIntent = AndroidIntent(AndroidIntent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${contact.phoneNumber}")
            if (message.isNotEmpty()) {
                putExtra("sms_body", message)
            }
        }
        
        try {
            context.startActivity(smsIntent)
            val displayName = contact.name.ifEmpty() { contact.phoneNumber }
            val responseMessage = if (message.isNotEmpty()) {
                "Texting $displayName: \"$message\""
            } else {
                "Opening text message to $displayName"
            }
            
            return ConversationResponse.success(
                responseMessage,
                mapOf(
                    "contact" to contact,
                    "action" to "text",
                    "message" to message
                )
            )
        } catch (e: Exception) {
            return ConversationResponse.error("Could not open messaging app")
        }
    }
    
    private fun sendEmail(contactName: String, email: String, message: String): ConversationResponse {
        val contact = if (contactName.isNotEmpty()) {
            findContactByName(contactName)
        } else {
            return ConversationResponse.error("Please specify who to email")
        }
        
        if (contact == null) {
            return ConversationResponse.error("Contact '$contactName' not found")
        }
        
        val emailAddress = contact.email.ifEmpty() { email }
        if (emailAddress.isEmpty()) {
            return ConversationResponse.error("No email address found for ${contact.name}")
        }
        
        val emailIntent = AndroidIntent(AndroidIntent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
            if (message.isNotEmpty()) {
                putExtra(AndroidIntent.EXTRA_TEXT, message)
            }
        }
        
        try {
            context.startActivity(emailIntent)
            return ConversationResponse.success(
                "Opening email to ${contact.name}",
                mapOf(
                    "contact" to contact,
                    "action" to "email",
                    "message" to message
                )
            )
        } catch (e: Exception) {
            return ConversationResponse.error("Could not open email app")
        }
    }
    
    private fun findContact(contactName: String): ConversationResponse {
        if (contactName.isEmpty()) {
            return ConversationResponse.error("Please specify a contact name")
        }
        
        val contact = findContactByName(contactName)
        return if (contact != null) {
            ConversationResponse.success(
                "Found ${contact.name}",
                mapOf("contact" to contact)
            )
        } else {
            ConversationResponse.error("Contact '$contactName' not found")
        }
    }
    
    private fun listRecentContacts(): ConversationResponse {
        // Mock recent contacts - in production, get from call log or contacts
        val recentContacts = listOf(
            Contact("mom-id", "Mom", "+1234567890", "mom@email.com"),
            Contact("john-id", "John", "+1987654321", "john@email.com"),
            Contact("sarah-id", "Sarah", "+1122334455", "sarah@email.com")
        )
        
        return ConversationResponse.success(
            "Here are your recent contacts",
            mapOf("recentContacts" to recentContacts)
        )
    }
    
    private fun findContactByName(name: String): Contact? {
        if (name.isEmpty()) return null
        
        // Mock contact lookup - in production, query contacts database
        val mockContacts = mapOf(
            "mom" to Contact("mom-id", "Mom", "+1234567890", "mom@email.com"),
            "john" to Contact("john-id", "John", "+1987654321", "john@email.com"),
            "sarah" to Contact("sarah-id", "Sarah", "+1122334455", "sarah@email.com"),
            "dad" to Contact("dad-id", "Dad", "+1555666777", "dad@email.com"),
            "mike" to Contact("mike-id", "Mike", "+1999888777", "mike@email.com")
        )
        
        return mockContacts[name.lowercase()]
    }
    
    private fun extractContactName(input: String): String {
        val patterns = listOf(
            Regex("(?:call|text|message|email)\\s+(.+?)(?:\\s+about|\\s+that|$)"),
            Regex("(?:find|search)\\s+(.+?)(?:\\s+in contacts|$)")
        )
        
        patterns.forEach { pattern ->
            pattern.find(input)?.let { 
                return it.groupValues[1].trim()
            }
        }
        
        return ""
    }
    
    private fun extractMessage(input: String): String {
        val patterns = listOf(
            Regex("(?:text|message|email).+?(?:about|that|saying)\\s+(.+)"),
            Regex("(?:tell|ask).+?(?:about|that)\\s+(.+)")
        )
        
        patterns.forEach { pattern ->
            pattern.find(input)?.let { 
                return it.groupValues[1].trim()
            }
        }
        
        return ""
    }
}

data class ContactCommand(
    val action: ContactAction,
    val contactName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val message: String = ""
)

enum class ContactAction {
    CALL,
    TEXT,
    EMAIL,
    FIND_CONTACT,
    LIST_RECENT,
    UNKNOWN
}
