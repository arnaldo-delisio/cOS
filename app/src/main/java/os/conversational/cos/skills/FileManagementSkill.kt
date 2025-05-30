package os.conversational.cos.skills

import android.Manifest
import android.content.Context
import android.os.Environment
import os.conversational.cos.core.*
import java.io.File
import java.util.*

/**
 * File management skill for cOS
 * Handles file operations through natural conversation
 */
class FileManagementSkill(private val context: Context) : ConversationalSkill() {
    
    override val requiredPermissions = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    override fun canHandle(intent: os.conversational.cos.core.Intent): Boolean {
        return intent == Intent.FILE_MANAGEMENT
    }
    
    override suspend fun processConversation(
        input: String,
        context: ConversationContext
    ): ConversationResponse {
        val command = parseFileCommand(input.lowercase())
        
        return try {
            when (command.action) {
                FileAction.LIST_FILES -> listFiles(command.location)
                FileAction.ORGANIZE -> organizeFiles(command.location)
                FileAction.DELETE_OLD -> deleteOldFiles(command.location, command.days)
                FileAction.SEARCH -> searchFiles(command.query, command.location)
                FileAction.CREATE_FOLDER -> createFolder(command.location, command.folderName)
                else -> ConversationResponse.error("I don't understand that file command yet.")
            }
        } catch (e: Exception) {
            ConversationResponse.error("File operation failed: ${e.message}")
        }
    }
    
    private fun parseFileCommand(input: String): FileCommand {
        return when {
            input.contains("list") || input.contains("show") -> 
                FileCommand(FileAction.LIST_FILES, getLocationFromInput(input))
            
            input.contains("organize") || input.contains("sort") ->
                FileCommand(FileAction.ORGANIZE, getLocationFromInput(input))
            
            input.contains("delete old") || input.contains("cleanup") ->
                FileCommand(FileAction.DELETE_OLD, getLocationFromInput(input), getDaysFromInput(input))
            
            input.contains("search") || input.contains("find") ->
                FileCommand(FileAction.SEARCH, getLocationFromInput(input), query = getSearchQuery(input))
            
            input.contains("create folder") || input.contains("make folder") ->
                FileCommand(FileAction.CREATE_FOLDER, getLocationFromInput(input), 
                          folderName = getFolderName(input))
            
            else -> FileCommand(FileAction.UNKNOWN)
        }
    }
    
    private fun listFiles(location: String): ConversationResponse {
        val directory = File(getDirectoryPath(location))
        if (!directory.exists() || !directory.isDirectory) {
            return ConversationResponse.error("Directory not found: $location")
        }
        
        val files = directory.listFiles()?.take(10) // Limit to first 10 files
        val fileList = files?.joinToString("\n") { file ->
            if (file.isDirectory) "📁 ${file.name}" else "📄 ${file.name}"
        } ?: "No files found"
        
        return ConversationResponse.success("Files in $location:\n$fileList")
    }
    
    private fun organizeFiles(location: String): ConversationResponse {
        val directory = File(getDirectoryPath(location))
        if (!directory.exists()) {
            return ConversationResponse.error("Directory not found: $location")
        }
        
        val files = directory.listFiles() ?: return ConversationResponse.error("Cannot access directory")
        var movedCount = 0
        
        // Create folders for different file types
        val folders = mapOf(
            "Images" to listOf("jpg", "jpeg", "png", "gif", "bmp"),
            "Documents" to listOf("pdf", "doc", "docx", "txt", "rtf"),
            "Videos" to listOf("mp4", "avi", "mkv", "mov", "wmv"),
            "Audio" to listOf("mp3", "wav", "flac", "aac", "ogg"),
            "Archives" to listOf("zip", "rar", "7z", "tar", "gz")
        )
        
        folders.forEach { (folderName, extensions) ->
            val folder = File(directory, folderName)
            if (!folder.exists()) folder.mkdirs()
            
            files.filter { file ->
                extensions.any { ext -> file.name.lowercase().endsWith(".$ext") }
            }.forEach { file ->
                val newFile = File(folder, file.name)
                if (file.renameTo(newFile)) movedCount++
            }
        }
        
        return ConversationResponse.success("Organized $movedCount files into folders in $location")
    }
    
    private fun deleteOldFiles(location: String, days: Int): ConversationResponse {
        val directory = File(getDirectoryPath(location))
        if (!directory.exists()) {
            return ConversationResponse.error("Directory not found: $location")
        }
        
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val files = directory.listFiles() ?: return ConversationResponse.error("Cannot access directory")
        
        val deletedFiles = files.filter { file ->
            file.isFile && file.lastModified() < cutoffTime
        }.count { file ->
            file.delete()
        }
        
        return ConversationResponse.success("Deleted $deletedFiles old files from $location")
    }
    
    private fun searchFiles(query: String, location: String): ConversationResponse {
        val directory = File(getDirectoryPath(location))
        if (!directory.exists()) {
            return ConversationResponse.error("Directory not found: $location")
        }
        
        val results = directory.walkTopDown().filter { file ->
            file.name.contains(query, ignoreCase = true)
        }.take(10).toList()
        
        val resultList = results.joinToString("\n") { file ->
            val type = if (file.isDirectory) "📁" else "📄"
            "$type ${file.name} (${file.parentFile?.name})"
        }
        
        return if (results.isEmpty()) {
            ConversationResponse.success("No files found matching '$query' in $location")
        } else {
            ConversationResponse.success("Found ${results.size} files matching '$query':\n$resultList")
        }
    }
    
    private fun createFolder(location: String, folderName: String): ConversationResponse {
        val directory = File(getDirectoryPath(location))
        val newFolder = File(directory, folderName)
        
        return if (newFolder.mkdirs()) {
            ConversationResponse.success("Created folder '$folderName' in $location")
        } else {
            ConversationResponse.error("Failed to create folder '$folderName'")
        }
    }
    
    private fun getLocationFromInput(input: String): String {
        return when {
            input.contains("downloads") -> "downloads"
            input.contains("pictures") || input.contains("photos") -> "pictures"  
            input.contains("documents") -> "documents"
            input.contains("music") -> "music"
            input.contains("videos") -> "videos"
            else -> "downloads" // default
        }
    }
    
    private fun getDirectoryPath(location: String): String {
        return when (location.lowercase()) {
            "downloads" -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            "pictures" -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
            "documents" -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
            "music" -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath
            "videos" -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath
            else -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        }
    }
    
    private fun getDaysFromInput(input: String): Int {
        val regex = """(\d+)\s*days?""".toRegex()
        val match = regex.find(input)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 30
    }
    
    private fun getSearchQuery(input: String): String {
        val parts = input.split("search", "find")
        return if (parts.size > 1) {
            parts[1].split("in")[0].trim().removeSurrounding("\"", "'")
        } else ""
    }
    
    private fun getFolderName(input: String): String {
        val parts = input.split("folder")
        return if (parts.size > 1) {
            parts[1].trim().removeSurrounding("\"", "'")
        } else "New Folder"
    }
}

/**
 * File command data class
 */
data class FileCommand(
    val action: FileAction,
    val location: String = "downloads",
    val days: Int = 30,
    val query: String = "",
    val folderName: String = ""
)

/**
 * Available file actions
 */
enum class FileAction {
    LIST_FILES,
    ORGANIZE,
    DELETE_OLD,
    SEARCH,
    CREATE_FOLDER,
    UNKNOWN
}
