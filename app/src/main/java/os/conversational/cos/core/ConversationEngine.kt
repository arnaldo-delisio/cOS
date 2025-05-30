package os.conversational.cos.core

import android.content.Context
import os.conversational.cos.ai.*

/**
 * Core conversation engine for cOS
 * Handles voice input, AI-powered intent classification, and skill routing
 */
class ConversationEngine(private val context: Context) {
    
    private val skills = mutableMapOf<String, ConversationalSkill>()
    private var conversationContext = os.conversational.cos.ai.ConversationContext()
    private lateinit var aiEngine: LocalAIEngine
    
    /**
     * Check if AI model is available
     */
    suspend fun isModelAvailable(): Boolean {
        if (!::aiEngine.isInitialized) {
            aiEngine = LocalAIEngine(context)
        }
        return aiEngine.isModelAvailable()
    }
    
    /**
     * Get the model manager for download operations
     */
    fun getModelManager(): os.conversational.cos.ai.ModelManager {
        if (!::aiEngine.isInitialized) {
            aiEngine = LocalAIEngine(context)
        }
        return aiEngine.getModelManager()
    }
    
    /**
     * Initialize the conversation engine with AI
     */
    suspend fun initialize(): Boolean {
        if (!::aiEngine.isInitialized) {
            aiEngine = LocalAIEngine(context)
        }
        return aiEngine.initialize()
    }
    
    /**
     * Process user input using AI and return appropriate response
     */
    suspend fun processInput(input: String): ConversationResponse {
        if (!::aiEngine.isInitialized) {
            return ConversationResponse.error("AI engine not initialized")
        }
        
        // Use AI to understand the conversation
        val aiResponse = aiEngine.processConversation(
            ConversationInput(input, conversationContext)
        )
        
        return when (aiResponse) {
            is AIResponse.Success -> {
                // Route to appropriate skill based on AI understanding
                val skill = findSkillForIntent(mapToLegacyIntent(aiResponse.intent))
                
                if (skill != null) {
                    // Convert AI context to legacy context for skills
                    val legacyContext = os.conversational.cos.core.ConversationContext()
                    skill.processConversation(input, legacyContext)
                } else {
                    // Handle built-in AI responses
                    ConversationResponse.success(
                        message = aiResponse.response,
                        data = aiResponse.actionData
                    )
                }
            }
            is AIResponse.Error -> {
                ConversationResponse.error(aiResponse.message)
            }
        }
    }
    
    /**
     * Map new AI intents to legacy skill intents
     */
    private fun mapToLegacyIntent(aiIntent: ConversationIntent): Intent {
        return when (aiIntent) {
            ConversationIntent.LIST_FILES,
            ConversationIntent.ORGANIZE_FILES,
            ConversationIntent.DELETE_FILES -> Intent.FILE_MANAGEMENT
            
            ConversationIntent.LAUNCH_APP,
            ConversationIntent.LIST_APPS -> Intent.APP_CONTROL
            
            ConversationIntent.ADJUST_SETTINGS,
            ConversationIntent.TOGGLE_FEATURE -> Intent.SYSTEM_CONTROL
            
            ConversationIntent.MAKE_CALL,
            ConversationIntent.SEND_MESSAGE -> Intent.COMMUNICATION
            
            ConversationIntent.GET_DIRECTIONS,
            ConversationIntent.NAVIGATE -> Intent.NAVIGATION
            
            else -> Intent.UNKNOWN
        }
    }
    
    /**
     * Register a new conversational skill
     */
    fun registerSkill(name: String, skill: ConversationalSkill) {
        skills[name] = skill
    }
    
    private fun classifyIntent(input: String): Intent {
        // Simple pattern matching for now
        // TODO: Replace with LLM-based intent classification
        return when {
            input.contains("file", ignoreCase = true) -> Intent.FILE_MANAGEMENT
            input.contains("app", ignoreCase = true) -> Intent.APP_CONTROL  
            input.contains("setting", ignoreCase = true) -> Intent.SYSTEM_CONTROL
            input.contains("call", ignoreCase = true) || 
                input.contains("contact", ignoreCase = true) || 
                input.contains("message", ignoreCase = true) -> Intent.COMMUNICATION
            input.contains("navigate", ignoreCase = true) || 
                input.contains("direction", ignoreCase = true) || 
                input.contains("map", ignoreCase = true) -> Intent.NAVIGATION
            else -> Intent.UNKNOWN
        }
    }
    
    private fun findSkillForIntent(intent: Intent): ConversationalSkill? {
        return skills.values.find { it.canHandle(intent) }
    }
}

/**
 * Base class for all conversational skills
 */
abstract class ConversationalSkill {
    abstract suspend fun processConversation(
        input: String,
        context: ConversationContext
    ): ConversationResponse
    
    abstract fun canHandle(intent: Intent): Boolean
    abstract val requiredPermissions: List<String>
}

/**
 * Represents user intent classification
 */
enum class Intent {
    FILE_MANAGEMENT,
    APP_CONTROL,
    SYSTEM_CONTROL,
    PHONE_CONTROL,
    COMMUNICATION,
    NAVIGATION,
    UNKNOWN
}

/**
 * Conversation context for maintaining state
 */
data class ConversationContext(
    val history: MutableList<ConversationTurn> = mutableListOf(),
    val deviceState: DeviceState = DeviceState()
)

/**
 * Single conversation turn
 */
data class ConversationTurn(
    val input: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Current device state information
 */
data class DeviceState(
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val wifiConnected: Boolean = false,
    val bluetoothEnabled: Boolean = false
)

/**
 * Response from conversation processing
 */
sealed class ConversationResponse {
    data class Success(val message: String, val data: Any? = null) : ConversationResponse()
    data class Error(val message: String) : ConversationResponse()
    
    companion object {
        fun success(message: String, data: Any? = null) = Success(message, data)
        fun error(message: String) = Error(message)
    }
}
