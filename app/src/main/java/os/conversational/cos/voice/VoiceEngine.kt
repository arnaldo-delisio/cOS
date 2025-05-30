package os.conversational.cos.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException
import java.util.*

/**
 * Voice processing engine for cOS
 * Handles speech-to-text and text-to-speech
 */
class VoiceEngine(private val context: Context) {
    
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var textToSpeech: TextToSpeech? = null
    private var isListening = false
    
    private val listeners = mutableListOf<VoiceListener>()
    
    interface VoiceListener {
        fun onSpeechResult(text: String)
        fun onError(error: String)
        fun onListeningStateChanged(isListening: Boolean)
    }
    
    suspend fun initialize(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                initializeVosk()
                initializeTTS()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    private suspend fun initializeVosk() {
        StorageService.unpack(context, "vosk-model-small-en-us-0.15", 
            "model", { model ->
                this.model = model
                val recognizer = Recognizer(model, 16000.0f)
                speechService = SpeechService(recognizer, 16000.0f)
                speechService?.addListener(object : RecognitionListener {
                    override fun onResult(hypothesis: String?) {
                        hypothesis?.let { 
                            listeners.forEach { it.onSpeechResult(it) }
                        }
                    }
                    
                    override fun onFinalResult(hypothesis: String?) {
                        hypothesis?.let {
                            listeners.forEach { it.onSpeechResult(it) }
                        }
                        stopListening()
                    }
                    
                    override fun onPartialResult(hypothesis: String?) {
                        // Handle partial results if needed
                    }
                    
                    override fun onError(exception: Exception?) {
                        listeners.forEach { 
                            it.onError(exception?.message ?: "Speech recognition error")
                        }
                        stopListening()
                    }
                    
                    override fun onTimeout() {
                        listeners.forEach { it.onError("Speech recognition timeout") }
                        stopListening()
                    }
                })
            }) { exception ->
                listeners.forEach { it.onError("Failed to initialize voice model: ${exception.message}") }
            }
    }
    
    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
            }
        }
    }
    
    fun startListening() {
        if (!hasAudioPermission()) {
            listeners.forEach { it.onError("Audio permission not granted") }
            return
        }
        
        if (speechService != null && !isListening) {
            try {
                speechService?.startListening()
                isListening = true
                listeners.forEach { it.onListeningStateChanged(true) }
            } catch (e: IOException) {
                listeners.forEach { it.onError("Failed to start listening: ${e.message}") }
            }
        }
    }
    
    fun stopListening() {
        if (speechService != null && isListening) {
            speechService?.stop()
            isListening = false
            listeners.forEach { it.onListeningStateChanged(false) }
        }
    }
    
    fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    
    fun addListener(listener: VoiceListener) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: VoiceListener) {
        listeners.remove(listener)
    }
    
    private fun hasAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, 
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun cleanup() {
        stopListening()
        speechService?.shutdown()
        textToSpeech?.shutdown()
        model?.close()
    }
    
    fun isCurrentlyListening(): Boolean = isListening
}
