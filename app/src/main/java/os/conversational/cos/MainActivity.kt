package os.conversational.cos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import os.conversational.cos.core.ConversationEngine
import os.conversational.cos.ai.ModelManager
import os.conversational.cos.skills.AppControlSkill
import os.conversational.cos.skills.FileManagementSkill
import os.conversational.cos.skills.CalculatorSkill
import os.conversational.cos.skills.ContactsSkill
import os.conversational.cos.skills.SystemControlSkill
import os.conversational.cos.skills.NavigationSkill
import os.conversational.cos.ui.theme.COSTheme
import os.conversational.cos.ui.ChatInterface
import os.conversational.cos.ui.ChatMessage
import os.conversational.cos.ui.ModelDownloadDialog
import os.conversational.cos.voice.VoiceEngine

class MainActivity : ComponentActivity() {
    
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var conversationEngine: ConversationEngine
    private var onVoiceMessage: ((String) -> Unit)? = null
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
            initializeVoiceEngine()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupEngines()
        requestPermissions()
        
        setContent {
            COSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    COSMainScreen()
                }
            }
        }
    }
    
    private fun setupEngines() {
        voiceEngine = VoiceEngine(this)
        conversationEngine = ConversationEngine(this)
        
        // Register skills
        conversationEngine.registerSkill("file", FileManagementSkill(this))
        conversationEngine.registerSkill("app", AppControlSkill(this))
        conversationEngine.registerSkill("calculator", CalculatorSkill(this))
        conversationEngine.registerSkill("contacts", ContactsSkill(this))
        conversationEngine.registerSkill("system", SystemControlSkill(this))
        conversationEngine.registerSkill("navigation", NavigationSkill(this))
    }
    
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        if (permissions.any { 
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED 
        }) {
            requestPermissionLauncher.launch(permissions)
        } else {
            initializeVoiceEngine()
        }
    }
    
    private fun initializeVoiceEngine() {
        lifecycleScope.launch {
            // Initialize both voice and conversation engines
            val voiceInitialized = voiceEngine.initialize()
            val conversationInitialized = conversationEngine.initialize()
            
            if (voiceInitialized && conversationInitialized) {
                setupVoiceListener()
            } else {
                // Handle initialization failure
                voiceEngine.speak("Failed to initialize AI conversation system")
            }
        }
    }
    
    private fun setupVoiceListener() {
        voiceEngine.addListener(object : VoiceEngine.VoiceListener {
            override fun onSpeechResult(text: String) {
                // Send voice result to chat interface
                onVoiceMessage?.invoke(text)
            }
            
            override fun onError(error: String) {
                voiceEngine.speak("Voice error: $error")
            }
            
            override fun onListeningStateChanged(isListening: Boolean) {
                // UI will react to this through state
            }
        })
    }
    
    @Composable
    fun COSMainScreen() {
        var isListening by remember { mutableStateOf(false) }
        var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
        var showModelDownload by remember { mutableStateOf(false) }
        var downloadProgress by remember { mutableStateOf<ModelManager.DownloadProgress?>(null) }
        
        // Check if model is available and show download if needed
        LaunchedEffect(Unit) {
            val modelAvailable = conversationEngine.isModelAvailable()
            if (!modelAvailable) {
                showModelDownload = true
                messages = listOf(
                    ChatMessage(
                        text = "Welcome to cOS! To enable AI features, please download the AI model first.",
                        isUser = false
                    )
                )
            } else {
                messages = listOf(
                    ChatMessage(
                        text = "Hi! I'm cOS, your conversational assistant. Try saying 'Calculate 25 plus 30' or 'Turn on WiFi'.",
                        isUser = false
                    )
                )
            }
        }
        
        // Set up voice message handler
        LaunchedEffect(Unit) {
            onVoiceMessage = { voiceText ->
                // Add user voice message and process it
                processMessage(voiceText) { userMsg, aiMsg ->
                    messages = messages + userMsg + aiMsg
                }
                // Stop listening after receiving voice input
                isListening = false
            }
        }
        
        // Show model download dialog if needed
        if (showModelDownload) {
            ModelDownloadDialog(
                downloadProgress = downloadProgress,
                onDownloadClick = {
                    lifecycleScope.launch {
                        conversationEngine.getModelManager().downloadModel().collectLatest { progress ->
                            downloadProgress = progress
                            if (progress.status == ModelManager.DownloadStatus.COMPLETED) {
                                // Reinitialize conversation engine with model
                                val initialized = conversationEngine.initialize()
                                if (initialized) {
                                    messages = listOf(
                                        ChatMessage(
                                            text = "Great! AI features are now enabled. How can I help you today?",
                                            isUser = false
                                        )
                                    )
                                }
                                showModelDownload = false
                            }
                        }
                    }
                },
                onDismiss = {
                    showModelDownload = false
                }
            )
        }
        
        ChatInterface(
            messages = messages,
            isListening = isListening,
            onSendMessage = { message ->
                processMessage(message) { userMsg, aiMsg ->
                    messages = messages + userMsg + aiMsg
                }
            },
            onVoiceClick = {
                if (::voiceEngine.isInitialized) {
                    if (isListening) {
                        voiceEngine.stopListening()
                        isListening = false
                    } else {
                        voiceEngine.startListening()
                        isListening = true
                    }
                }
            }
        )
    }
    
    private fun processMessage(
        message: String,
        onResult: (ChatMessage, ChatMessage) -> Unit
    ) {
        val userMessage = ChatMessage(text = message, isUser = true)
        
        lifecycleScope.launch {
            val response = conversationEngine.processInput(message)
            val responseText = when (response) {
                is os.conversational.cos.core.ConversationResponse.Success -> response.message
                is os.conversational.cos.core.ConversationResponse.Error -> "Sorry, ${response.message}"
            }
            
            val aiMessage = ChatMessage(text = responseText, isUser = false)
            onResult(userMessage, aiMessage)
            
            // Speak the response
            if (::voiceEngine.isInitialized) {
                voiceEngine.speak(responseText)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::voiceEngine.isInitialized) {
            voiceEngine.cleanup()
        }
    }
}
