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
import os.conversational.cos.core.ConversationEngine
import os.conversational.cos.skills.AppControlSkill
import os.conversational.cos.skills.FileManagementSkill
import os.conversational.cos.skills.CalculatorSkill
import os.conversational.cos.ui.theme.COSTheme
import os.conversational.cos.voice.VoiceEngine

class MainActivity : ComponentActivity() {
    
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var conversationEngine: ConversationEngine
    
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
                lifecycleScope.launch {
                    val response = conversationEngine.processInput(text)
                    when (response) {
                        is os.conversational.cos.core.ConversationResponse.Success -> {
                            voiceEngine.speak(response.message)
                        }
                        is os.conversational.cos.core.ConversationResponse.Error -> {
                            voiceEngine.speak("Sorry, ${response.message}")
                        }
                    }
                }
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
        var lastCommand by remember { mutableStateOf("") }
        var lastResponse by remember { mutableStateOf("") }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "cOS",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Conversational Operating System",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { 
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
            ) {
                Text(if (isListening) "🎙️ Stop Listening" else "🎤 Start Listening")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (lastCommand.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Last Command:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = lastCommand,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            if (lastResponse.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Response:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = lastResponse,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Try saying:\n• \"Show photos of Sarah from vacation\"\n• \"Calculate 15% tip on \$50\"\n• \"List files in downloads\"\n• \"Text mom I'm running late\"",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "🤖 AI-Powered Conversation (Beta)",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::voiceEngine.isInitialized) {
            voiceEngine.cleanup()
        }
    }
}
