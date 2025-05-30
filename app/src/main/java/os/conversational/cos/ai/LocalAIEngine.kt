package os.conversational.cos.ai

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Local AI engine for cOS using MediaPipe LLM Inference API
 * Handles on-device conversation understanding with Gemma 3n
 */
class LocalAIEngine(private val context: Context) {
    
    companion object {
        private const val TAG = "LocalAIEngine"
        private const val MAX_TOKENS = 1024
        private const val TOP_K = 40
        private const val TEMPERATURE = 0.8f
        private const val RANDOM_SEED = 101
    }
    
    private var llmInference: LlmInference? = null
    private var isInitialized = false
    private val modelManager = ModelManager(context)
    
    /**
     * Check if model is available for initialization
     */
    suspend fun isModelAvailable(): Boolean = modelManager.isModelAvailable()
    
    /**
     * Get the model manager for download operations
     */
    fun getModelManager(): ModelManager = modelManager
    
    /**
     * Initialize the AI engine with Gemma 3n model via MediaPipe
     */
    suspend fun initialize(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Check if model is available
                if (!modelManager.isModelAvailable()) {
                    Log.e(TAG, "Model file not found. Please download the AI model through the app.")
                    return@withContext false
                }
                
                val modelPath = modelManager.getModelPath()
                Log.d(TAG, "Initializing with model at: $modelPath")
                
                // Configure MediaPipe LLM Inference
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelPath)
                    .setMaxTokens(MAX_TOKENS)
                    .setTopK(TOP_K)
                    .setTemperature(TEMPERATURE)
                    .setRandomSeed(RANDOM_SEED)
                    .build()
                
                // Create LLM Inference instance
                llmInference = LlmInference.createFromOptions(context, options)
                isInitialized = true
                Log.i(TAG, "MediaPipe LLM Inference initialized successfully with Gemma 3n")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MediaPipe LLM", e)
                e.printStackTrace()
                false
            }
        }
    }
    
    /**
     * Process conversation input and return intelligent response
     */
    suspend fun processConversation(input: ConversationInput): AIResponse {
        if (!isInitialized || llmInference == null) {
            return AIResponse.error("AI engine not initialized")
        }
        
        return withContext(Dispatchers.Default) {
            try {
                // Build conversational prompt for Gemma 3n
                val prompt = buildConversationalPrompt(input)
                
                // Generate response using MediaPipe LLM Inference
                val startTime = System.currentTimeMillis()
                val response = llmInference!!.generateResponse(prompt)
                val inferenceTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "Inference completed in ${inferenceTime}ms")
                
                // Parse AI response to extract intent and action data
                val parsedResponse = parseAIResponse(response, input.text)
                
                AIResponse.success(
                    intent = parsedResponse.intent,
                    confidence = parsedResponse.confidence,
                    response = parsedResponse.naturalResponse,
                    actionData = parsedResponse.actionData
                )
            } catch (e: Exception) {
                Log.e(TAG, "AI processing failed", e)
                AIResponse.error("AI processing failed: ${e.message}")
            }
        }
    }
    
    /**
     * Build a structured prompt for Gemma 3n that encourages intent extraction
     */
    private fun buildConversationalPrompt(input: ConversationInput): String {
        val contextInfo = if (input.context.previousIntent != null) {
            "Previous intent: ${input.context.previousIntent}"
        } else {
            "This is the start of a new conversation"
        }
        
        return """You are cOS, a conversational Android assistant. Analyze the user's request and provide:
1. The intent (one of: LIST_FILES, ORGANIZE_FILES, DELETE_FILES, LAUNCH_APP, LIST_APPS, SHOW_FILTERED_PHOTOS, SEND_MESSAGE, SEARCH_LOCATION, ADJUST_SETTINGS, TOGGLE_FEATURE, MAKE_CALL, GET_DIRECTIONS, NAVIGATE, CALCULATE, or UNKNOWN)
2. A natural, helpful response
3. Any relevant data extracted from the request

User request: "${input.text}"
$contextInfo

Respond in this format:
INTENT: [intent_name]
RESPONSE: [natural response to user]
DATA: [any extracted data as key:value pairs]"""
    }
    
    /**
     * Parse the AI response to extract structured data
     */
    private fun parseAIResponse(aiOutput: String, originalInput: String): ParsedAIResponse {
        val lines = aiOutput.lines()
        var intent = ConversationIntent.UNKNOWN
        var response = "I'll help you with that."
        val actionData = mutableMapOf<String, Any>()
        
        for (line in lines) {
            when {
                line.startsWith("INTENT:") -> {
                    val intentStr = line.substringAfter("INTENT:").trim()
                    intent = try {
                        ConversationIntent.valueOf(intentStr)
                    } catch (e: Exception) {
                        ConversationIntent.UNKNOWN
                    }
                }
                line.startsWith("RESPONSE:") -> {
                    response = line.substringAfter("RESPONSE:").trim()
                }
                line.startsWith("DATA:") -> {
                    val dataStr = line.substringAfter("DATA:").trim()
                    // Parse key:value pairs
                    dataStr.split(",").forEach { pair ->
                        val parts = pair.trim().split(":")
                        if (parts.size == 2) {
                            actionData[parts[0].trim()] = parts[1].trim()
                        }
                    }
                }
            }
        }
        
        // If AI didn't provide structured response, fall back to pattern matching
        if (intent == ConversationIntent.UNKNOWN) {
            intent = classifyIntentWithPatterns(originalInput)
            actionData.putAll(extractActionDataFromPatterns(intent, originalInput))
        }
        
        return ParsedAIResponse(
            intent = intent,
            confidence = if (intent != ConversationIntent.UNKNOWN) 0.8f else 0.3f,
            naturalResponse = response,
            actionData = actionData
        )
    }
    
    /**
     * Data class for parsed AI response
     */
    private data class ParsedAIResponse(
        val intent: ConversationIntent,
        val confidence: Float,
        val naturalResponse: String,
        val actionData: Map<String, Any>
    )
    
    /**
     * Fallback pattern matching when AI doesn't provide clear intent
     */
    private fun classifyIntentWithPatterns(input: String): ConversationIntent {
        val normalizedInput = input.lowercase().trim()
        
        return when {
            // File operations
            normalizedInput.contains(Regex("(list|show|find).*(files?|documents?|downloads?)")) -> 
                ConversationIntent.LIST_FILES
            normalizedInput.contains(Regex("organize|sort.*(files?|photos?)")) -> 
                ConversationIntent.ORGANIZE_FILES
            normalizedInput.contains(Regex("delete.*(old|files?)")) -> 
                ConversationIntent.DELETE_FILES
            
            // App control  
            normalizedInput.contains(Regex("(open|launch|start).*(app|application)")) -> 
                ConversationIntent.LAUNCH_APP
            normalizedInput.contains(Regex("show.*(apps?|applications?)")) -> 
                ConversationIntent.LIST_APPS
            
            // Smart queries
            normalizedInput.contains(Regex("show.*(photos?|pictures?).*(of|from)")) -> 
                ConversationIntent.SHOW_FILTERED_PHOTOS
            normalizedInput.contains(Regex("(text|message|send).*(to|mom|dad)")) -> 
                ConversationIntent.SEND_MESSAGE
            normalizedInput.contains(Regex("find.*(nearby|pizza|restaurant)")) -> 
                ConversationIntent.SEARCH_LOCATION
            
            // System control
            normalizedInput.contains(Regex("(turn|set|toggle).*(wifi|bluetooth|brightness|volume|dnd|disturb)")) -> 
                ConversationIntent.TOGGLE_FEATURE
            normalizedInput.contains(Regex("(adjust|change|modify).*(setting|brightness|volume)")) -> 
                ConversationIntent.ADJUST_SETTINGS
            
            // Communication
            normalizedInput.contains(Regex("(call|phone|dial)")) -> 
                ConversationIntent.MAKE_CALL
                
            // Navigation
            normalizedInput.contains(Regex("(directions|navigate|route).*(to|from)")) -> 
                ConversationIntent.GET_DIRECTIONS
            normalizedInput.contains(Regex("(take me|navigate|go).*(to|home)")) -> 
                ConversationIntent.NAVIGATE
            
            // Calculator
            normalizedInput.contains(Regex("calculate|\\d+\\s*[+\\-*/]\\s*\\d+")) -> 
                ConversationIntent.CALCULATE
            
            else -> ConversationIntent.UNKNOWN
        }
    }
    
    /**
     * Extract action data using patterns when AI parsing fails
     */
    private fun extractActionDataFromPatterns(intent: ConversationIntent, input: String): Map<String, Any> {
        return when (intent) {
            ConversationIntent.SHOW_FILTERED_PHOTOS -> {
                mapOf(
                    "person" to extractPersonName(input),
                    "timeframe" to extractTimeframe(input)
                )
            }
            ConversationIntent.SEND_MESSAGE -> {
                mapOf(
                    "recipient" to extractRecipient(input),
                    "message" to extractMessage(input)
                )
            }
            ConversationIntent.CALCULATE -> {
                mapOf(
                    "expression" to extractMathExpression(input)
                )
            }
            else -> emptyMap()
        }
    }
    
    // Helper functions for data extraction
    private fun extractPersonName(input: String): String {
        val personRegex = Regex("(?:of|from)\\s+(\\w+)")
        return personRegex.find(input.lowercase())?.groupValues?.get(1) ?: ""
    }
    
    private fun extractTimeframe(input: String): String {
        val timeRegex = Regex("(yesterday|today|last\\s+week|vacation|trip)")
        return timeRegex.find(input.lowercase())?.value ?: ""
    }
    
    private fun extractRecipient(input: String): String {
        val recipientRegex = Regex("(?:to|text)\\s+(\\w+)")
        return recipientRegex.find(input.lowercase())?.groupValues?.get(1) ?: ""
    }
    
    private fun extractMessage(input: String): String {
        // Extract message content after recipient
        val messageRegex = Regex("(?:that|saying)?\\s*[\"']?([^\"']+)[\"']?$")
        return messageRegex.find(input)?.groupValues?.get(1)?.trim() ?: input
    }
    
    private fun extractMathExpression(input: String): String {
        val mathRegex = Regex("(\\d+(?:\\.\\d+)?\\s*[+\\-*/]\\s*\\d+(?:\\.\\d+)?)")
        return mathRegex.find(input)?.value ?: input
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        try {
            llmInference?.close()
            llmInference = null
            isInitialized = false
            Log.i(TAG, "MediaPipe LLM Inference cleaned up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}

/**
 * Input data for conversation processing
 */
data class ConversationInput(
    val text: String,
    val context: ConversationContext = ConversationContext(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Conversation context for maintaining state
 */
data class ConversationContext(
    val previousIntent: ConversationIntent? = null,
    val userPreferences: Map<String, Any> = emptyMap(),
    val deviceState: Map<String, Any> = emptyMap()
)

/**
 * AI response with intent and action data
 */
sealed class AIResponse {
    data class Success(
        val intent: ConversationIntent,
        val confidence: Float,
        val response: String,
        val actionData: Map<String, Any> = emptyMap()
    ) : AIResponse()
    
    data class Error(val message: String) : AIResponse()
    
    companion object {
        fun success(
            intent: ConversationIntent,
            confidence: Float,
            response: String,
            actionData: Map<String, Any> = emptyMap()
        ) = Success(intent, confidence, response, actionData)
        
        fun error(message: String) = Error(message)
    }
}

/**
 * Conversation intents that the AI can understand
 */
enum class ConversationIntent {
    // File operations
    LIST_FILES,
    ORGANIZE_FILES,
    DELETE_FILES,
    
    // App control
    LAUNCH_APP,
    LIST_APPS,
    
    // Smart features (require AI understanding)
    SHOW_FILTERED_PHOTOS,
    SEND_MESSAGE,
    SEARCH_LOCATION,
    
    // System control
    ADJUST_SETTINGS,
    TOGGLE_FEATURE,
    
    // Communication
    MAKE_CALL,
    
    // Navigation
    GET_DIRECTIONS,
    NAVIGATE,
    
    // Built-in tools
    CALCULATE,
    
    // System
    UNKNOWN
}