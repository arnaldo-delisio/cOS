package os.conversational.cos.skills

import android.content.Context
import os.conversational.cos.core.*
import kotlin.math.*

/**
 * Calculator skill for cOS
 * Handles mathematical calculations through natural conversation
 */
class CalculatorSkill(private val context: Context) : ConversationalSkill() {
    
    override val requiredPermissions = listOf<String>()
    
    override fun canHandle(intent: os.conversational.cos.core.Intent): Boolean {
        return intent == os.conversational.cos.core.Intent.UNKNOWN // We'll handle this through AI routing
    }
    
    override suspend fun processConversation(
        input: String,
        context: ConversationContext
    ): ConversationResponse {
        return try {
            val result = evaluateExpression(input)
            ConversationResponse.success("The answer is $result")
        } catch (e: Exception) {
            ConversationResponse.error("I couldn't calculate that: ${e.message}")
        }
    }
    
    /**
     * Evaluate mathematical expressions
     */
    private fun evaluateExpression(input: String): Double {
        // Extract mathematical expression from natural language
        val expression = extractMathExpression(input)
        
        return when {
            expression.contains("+") -> {
                val parts = expression.split("+")
                parts.sumOf { it.trim().toDouble() }
            }
            expression.contains("-") -> {
                val parts = expression.split("-")
                parts[0].trim().toDouble() - parts[1].trim().toDouble()
            }
            expression.contains("*") || expression.contains("×") -> {
                val parts = expression.split(Regex("[*×]"))
                parts[0].trim().toDouble() * parts[1].trim().toDouble()
            }
            expression.contains("/") || expression.contains("÷") -> {
                val parts = expression.split(Regex("[/÷]"))
                val divisor = parts[1].trim().toDouble()
                if (divisor == 0.0) throw ArithmeticException("Division by zero")
                parts[0].trim().toDouble() / divisor
            }
            expression.contains("^") || input.contains("power") -> {
                val parts = expression.split("^")
                parts[0].trim().toDouble().pow(parts[1].trim().toDouble())
            }
            expression.contains("sqrt") || input.contains("square root") -> {
                val number = extractNumber(expression)
                sqrt(number)
            }
            expression.contains("%") || input.contains("percent") -> {
                handlePercentageCalculation(input)
            }
            else -> {
                // Try to parse as a single number
                expression.toDoubleOrNull() ?: throw IllegalArgumentException("Not a valid expression")
            }
        }
    }
    
    /**
     * Extract mathematical expression from natural language
     */
    private fun extractMathExpression(input: String): String {
        // Remove common words and extract the mathematical part
        var expression = input.lowercase()
            .replace("calculate", "")
            .replace("what is", "")
            .replace("what's", "")
            .replace("plus", "+")
            .replace("minus", "-")
            .replace("times", "*")
            .replace("multiplied by", "*")
            .replace("divided by", "/")
            .replace("to the power of", "^")
            .trim()
        
        // Extract the actual mathematical expression
        val mathRegex = Regex("([\\d.]+\\s*[+\\-*/^%]\\s*[\\d.]+|sqrt\\s*\\(?[\\d.]+\\)?|[\\d.]+)")
        val match = mathRegex.find(expression)
        
        return match?.value?.trim() ?: expression
    }
    
    /**
     * Extract a number from text
     */
    private fun extractNumber(text: String): Double {
        val numberRegex = Regex("([\\d.]+)")
        val match = numberRegex.find(text)
        return match?.value?.toDoubleOrNull() ?: throw IllegalArgumentException("No number found")
    }
    
    /**
     * Handle percentage calculations
     */
    private fun handlePercentageCalculation(input: String): Double {
        return when {
            input.contains("tip") -> {
                // "15% tip on $50"
                val percentMatch = Regex("(\\d+)%").find(input)
                val amountMatch = Regex("\\$(\\d+(?:\\.\\d+)?)").find(input)
                
                if (percentMatch != null && amountMatch != null) {
                    val percent = percentMatch.groupValues[1].toDouble()
                    val amount = amountMatch.groupValues[1].toDouble()
                    (amount * percent) / 100
                } else {
                    throw IllegalArgumentException("Couldn't parse tip calculation")
                }
            }
            input.contains("of") -> {
                // "20% of 100"
                val percentMatch = Regex("(\\d+)%").find(input)
                val numberMatch = Regex("of\\s+(\\d+(?:\\.\\d+)?)").find(input)
                
                if (percentMatch != null && numberMatch != null) {
                    val percent = percentMatch.groupValues[1].toDouble()
                    val number = numberMatch.groupValues[1].toDouble()
                    (number * percent) / 100
                } else {
                    throw IllegalArgumentException("Couldn't parse percentage calculation")
                }
            }
            else -> {
                throw IllegalArgumentException("Unknown percentage calculation")
            }
        }
    }
    
    /**
     * Format result for display
     */
    private fun formatResult(result: Double): String {
        return if (result == result.toInt().toDouble()) {
            result.toInt().toString()
        } else {
            String.format("%.2f", result)
        }
    }
}