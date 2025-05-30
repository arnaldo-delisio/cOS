package os.conversational.cos.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Manages AI model downloading, storage, and verification
 */
class ModelManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ModelManager"
        private const val MODEL_NAME = "gemma-3n.bin"
        private const val MODEL_URL = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task"
        private const val EXPECTED_SIZE = 1_200_000_000L // ~1.2GB expected size
        private const val CHUNK_SIZE = 8192
    }
    
    data class DownloadProgress(
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val status: DownloadStatus
    )
    
    enum class DownloadStatus {
        NOT_STARTED,
        CHECKING,
        DOWNLOADING,
        VERIFYING,
        COMPLETED,
        ERROR
    }
    
    /**
     * Get the internal storage path for the model
     */
    fun getModelPath(): String {
        return File(context.filesDir, "models/$MODEL_NAME").absolutePath
    }
    
    /**
     * Check if model exists and is valid
     */
    suspend fun isModelAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            val modelFile = File(getModelPath())
            modelFile.exists() && modelFile.length() > 0
        }
    }
    
    /**
     * Download model with progress updates
     */
    fun downloadModel(): Flow<DownloadProgress> = flow {
        emit(DownloadProgress(0f, 0, 0, DownloadStatus.CHECKING))
        
        try {
            // Create models directory
            val modelsDir = File(context.filesDir, "models")
            if (!modelsDir.exists()) {
                modelsDir.mkdirs()
            }
            
            val modelFile = File(modelsDir, MODEL_NAME)
            
            // Check if already downloaded
            if (modelFile.exists() && modelFile.length() > EXPECTED_SIZE * 0.9) {
                emit(DownloadProgress(1f, modelFile.length(), modelFile.length(), DownloadStatus.COMPLETED))
                return@flow
            }
            
            // Start download
            emit(DownloadProgress(0f, 0, EXPECTED_SIZE, DownloadStatus.DOWNLOADING))
            
            val url = URL(MODEL_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L
            
            connection.inputStream.use { input ->
                FileOutputStream(modelFile).use { output ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        
                        val progress = if (totalBytes > 0) {
                            downloadedBytes.toFloat() / totalBytes
                        } else {
                            downloadedBytes.toFloat() / EXPECTED_SIZE
                        }
                        
                        emit(DownloadProgress(
                            progress = progress,
                            downloadedBytes = downloadedBytes,
                            totalBytes = totalBytes,
                            status = DownloadStatus.DOWNLOADING
                        ))
                    }
                }
            }
            
            // Verify download
            emit(DownloadProgress(1f, downloadedBytes, totalBytes, DownloadStatus.VERIFYING))
            
            if (modelFile.length() < EXPECTED_SIZE * 0.9) {
                modelFile.delete()
                throw Exception("Downloaded file size is too small")
            }
            
            emit(DownloadProgress(1f, downloadedBytes, totalBytes, DownloadStatus.COMPLETED))
            
        } catch (e: Exception) {
            Log.e(TAG, "Model download failed", e)
            emit(DownloadProgress(0f, 0, 0, DownloadStatus.ERROR))
            throw e
        }
    }
    
    /**
     * Get model file size in MB
     */
    suspend fun getModelSizeMB(): Float {
        return withContext(Dispatchers.IO) {
            val modelFile = File(getModelPath())
            if (modelFile.exists()) {
                modelFile.length() / (1024f * 1024f)
            } else {
                0f
            }
        }
    }
    
    /**
     * Delete model to free up space
     */
    suspend fun deleteModel(): Boolean {
        return withContext(Dispatchers.IO) {
            val modelFile = File(getModelPath())
            if (modelFile.exists()) {
                modelFile.delete()
            } else {
                true
            }
        }
    }
}