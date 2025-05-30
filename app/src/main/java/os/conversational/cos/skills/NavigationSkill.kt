package os.conversational.cos.skills

import android.content.Context
import android.content.Intent as AndroidIntent
import android.net.Uri
import os.conversational.cos.core.*

/**
 * Navigation skill for contextual maps and directions
 */
class NavigationSkill(private val context: Context) : ConversationalSkill() {
    
    override val requiredPermissions = listOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    override fun canHandle(intent: os.conversational.cos.core.Intent): Boolean {
        return intent == os.conversational.cos.core.Intent.NAVIGATION
    }
    
    override suspend fun processConversation(
        input: String,
        context: ConversationContext
    ): ConversationResponse {
        val command = parseNavigationCommand(input.lowercase())
        
        return try {
            when (command.action) {
                NavigationAction.GET_DIRECTIONS -> getDirections(command.destination)
                NavigationAction.FIND_NEARBY -> findNearby(command.searchQuery)
                NavigationAction.CURRENT_LOCATION -> getCurrentLocation()
                NavigationAction.TRAFFIC_INFO -> getTrafficInfo(command.destination)
                else -> ConversationResponse.error("I don't understand that navigation request.")
            }
        } catch (e: Exception) {
            ConversationResponse.error("Navigation failed: ${e.message}")
        }
    }
    
    private fun parseNavigationCommand(input: String): NavigationCommand {
        return when {
            input.contains("directions to") || input.contains("navigate to") || input.contains("go to") ->
                NavigationCommand(NavigationAction.GET_DIRECTIONS, extractDestination(input))
            
            input.contains("find") || input.contains("where is") || input.contains("nearest") ->
                NavigationCommand(NavigationAction.FIND_NEARBY, extractSearchQuery(input))
            
            input.contains("where am i") || input.contains("current location") ->
                NavigationCommand(NavigationAction.CURRENT_LOCATION)
            
            input.contains("traffic") || input.contains("how long") ->
                NavigationCommand(NavigationAction.TRAFFIC_INFO, extractDestination(input))
            
            else -> NavigationCommand(NavigationAction.UNKNOWN)
        }
    }
    
    private fun getDirections(destination: String): ConversationResponse {
        if (destination.isEmpty()) {
            return ConversationResponse.error("Please specify a destination")
        }
        
        // In production, integrate with Google Maps API or similar
        val mockDirections = listOf(
            "Head north on Main St toward 1st Ave",
            "Turn right onto Highway 101",
            "Take exit 15 for $destination",
            "Arrive at destination"
        )
        
        // Open Google Maps for actual navigation
        val mapsIntent = AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse("google.navigation:q=$destination"))
        mapsIntent.setPackage("com.google.android.apps.maps")
        
        if (mapsIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapsIntent)
        }
        
        return ConversationResponse.success(
            "I'll navigate you to $destination. The route is about 15 minutes.",
            mapOf(
                "destination" to destination,
                "distance" to "8.2 miles",
                "duration" to "15 minutes",
                "directions" to mockDirections
            )
        )
    }
    
    private fun findNearby(query: String): ConversationResponse {
        if (query.isEmpty()) {
            return ConversationResponse.error("What would you like to find nearby?")
        }
        
        // Mock nearby results
        val nearbyPlaces = when {
            query.contains("coffee") || query.contains("starbucks") -> 
                "Found 3 coffee shops nearby: Starbucks (0.2 mi), Local Café (0.4 mi), Coffee Bean (0.6 mi)"
            query.contains("gas") || query.contains("fuel") ->
                "Found 2 gas stations nearby: Shell (0.3 mi), Chevron (0.5 mi)"
            query.contains("restaurant") || query.contains("food") ->
                "Found 5 restaurants nearby: Italian Bistro (0.1 mi), Burger Palace (0.3 mi), Sushi Bar (0.4 mi)"
            else ->
                "Found several $query locations nearby"
        }
        
        return ConversationResponse.success(nearbyPlaces)
    }
    
    private fun getCurrentLocation(): ConversationResponse {
        // In production, get actual location
        return ConversationResponse.success("You're currently at Main Street, Downtown. Would you like directions somewhere?")
    }
    
    private fun getTrafficInfo(destination: String): ConversationResponse {
        if (destination.isEmpty()) {
            return ConversationResponse.success("Current traffic is light. Any specific destination?")
        }
        
        return ConversationResponse.success("Traffic to $destination is moderate. Estimated 18 minutes with current conditions.")
    }
    
    private fun extractDestination(input: String): String {
        val patterns = listOf(
            Regex("(?:directions to|navigate to|go to)\\s+(.+)"),
            Regex("(?:traffic to|how long to)\\s+(.+)")
        )
        
        patterns.forEach { pattern ->
            pattern.find(input)?.let { 
                return it.groupValues[1].trim()
            }
        }
        
        return ""
    }
    
    private fun extractSearchQuery(input: String): String {
        val patterns = listOf(
            Regex("(?:find|where is|nearest)\\s+(.+?)(?:\\s+near me|$)"),
            Regex("(?:find)\\s+(.+)")
        )
        
        patterns.forEach { pattern ->
            pattern.find(input)?.let { 
                return it.groupValues[1].trim()
            }
        }
        
        return ""
    }
}

data class NavigationCommand(
    val action: NavigationAction,
    val destination: String = "",
    val searchQuery: String = ""
)

enum class NavigationAction {
    GET_DIRECTIONS,
    FIND_NEARBY,
    CURRENT_LOCATION,
    TRAFFIC_INFO,
    UNKNOWN
}
