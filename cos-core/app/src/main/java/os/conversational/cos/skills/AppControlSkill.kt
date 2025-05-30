package os.conversational.cos.skills

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import os.conversational.cos.core.*

/**
 * App control skill for cOS
 * Handles launching and managing applications through conversation
 */
class AppControlSkill(private val context: Context) : ConversationalSkill() {
    
    override val requiredPermissions = listOf<String>()
    
    override fun canHandle(intent: Intent): Boolean {
        return intent == os.conversational.cos.core.Intent.APP_CONTROL
    }
    
    override suspend fun processConversation(
        input: String,
        context: ConversationContext
    ): ConversationResponse {
        val command = parseAppCommand(input.lowercase())
        
        return try {
            when (command.action) {
                AppAction.LAUNCH -> launchApp(command.appName)
                AppAction.LIST_APPS -> listInstalledApps()
                AppAction.CLOSE -> closeApp(command.appName)
                else -> ConversationResponse.error("I don't understand that app command yet.")
            }
        } catch (e: Exception) {
            ConversationResponse.error("App operation failed: ${e.message}")
        }
    }
    
    private fun parseAppCommand(input: String): AppCommand {
        return when {
            input.contains("open") || input.contains("launch") || input.contains("start") ->
                AppCommand(AppAction.LAUNCH, getAppNameFromInput(input))
            
            input.contains("list") || input.contains("show") ->
                AppCommand(AppAction.LIST_APPS)
            
            input.contains("close") || input.contains("kill") ->
                AppCommand(AppAction.CLOSE, getAppNameFromInput(input))
            
            else -> AppCommand(AppAction.UNKNOWN)
        }
    }
    
    private fun launchApp(appName: String): ConversationResponse {
        val packageManager = context.packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        val matchingApp = apps.find { app ->
            val label = packageManager.getApplicationLabel(app).toString()
            label.contains(appName, ignoreCase = true) || 
            app.packageName.contains(appName, ignoreCase = true)
        }
        
        return if (matchingApp != null) {
            val launchIntent = packageManager.getLaunchIntentForPackage(matchingApp.packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                val appLabel = packageManager.getApplicationLabel(matchingApp).toString()
                ConversationResponse.success("Launched $appLabel")
            } else {
                ConversationResponse.error("Cannot launch $appName - no launch intent found")
            }
        } else {
            ConversationResponse.error("App '$appName' not found")
        }
    }
    
    private fun listInstalledApps(): ConversationResponse {
        val packageManager = context.packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                packageManager.getLaunchIntentForPackage(app.packageName) != null
            }
            .take(10) // Limit to 10 apps
            .map { app ->
                packageManager.getApplicationLabel(app).toString()
            }
            .sorted()
        
        val appList = apps.joinToString("\n") { "📱 $it" }
        return ConversationResponse.success("Installed apps (showing first 10):\n$appList")
    }
    
    private fun closeApp(appName: String): ConversationResponse {
        // Note: This is limited on non-rooted devices
        // We can only suggest closing or provide instructions
        return ConversationResponse.success("To close $appName, please use the recent apps button and swipe it away, or go to Settings > Apps > $appName > Force Stop")
    }
    
    private fun getAppNameFromInput(input: String): String {
        val keywords = listOf("open", "launch", "start", "close", "kill")
        var appName = input
        
        keywords.forEach { keyword ->
            appName = appName.replace(keyword, "").trim()
        }
        
        // Remove common words
        appName = appName.replace("app", "").trim()
        
        return appName
    }
}

/**
 * App command data class
 */
data class AppCommand(
    val action: AppAction,
    val appName: String = ""
)

/**
 * Available app actions
 */
enum class AppAction {
    LAUNCH,
    LIST_APPS,
    CLOSE,
    UNKNOWN
}
