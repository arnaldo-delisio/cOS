# cOS Architecture - Single Unified Application

## Overview

cOS is built as a **single unified Android application** that serves as both conversational launcher and intelligent assistant. This architectural approach eliminates complexity by providing one cohesive experience that intelligently handles all user interactions, reducing distraction while maximizing productivity.

**Design Philosophy**: One app, one experience, one codebase - all community efforts focused on perfecting the unified vision.

## Single App Architecture

cOS is architected as one unified Android application with integrated components working together seamlessly:

```
┌──────────────────────────────────────────────────────────┐
│                 Unified Launcher Interface                 │
│        Clean Chat Interface + Essential Shortcuts         │
└──────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────────────────────────────────────┐
│                   Unified Input Processing                 │
│           Voice (Vosk) + Text → Single Input Stream       │
└──────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────────────────────────────────────┐
│                 Local AI Understanding                     │
│         Gemma 3n + Conversation Context + Learning        │
└──────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────────────────────────────────────┐
│                  Intelligent Action Routing               │
│    Intent Classification + Risk Assessment + Preferences  │
└──────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────────────────────────────────────┐
│                Deep Android Integration                    │
│   Smart App Control + Content Filtering + System Access  │
└──────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────────────────────────────────────┐
│                   Minimal Action Feedback                  │
│    Built-in Tools + Smart Confirmations + Learning        │
└──────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Unified Conversational Interface

The primary and only user interface - a clean launcher with conversation at its center, eliminating mode complexity.

```kotlin
class UnifiedLauncherActivity : Activity() {
    private val conversationView = CleanChatInterface()
    private val essentialShortcuts = MinimalShortcuts()
    private val inputHandler = UnifiedInputHandler()
    private val aiEngine = LocalAIEngine()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up distraction-free launcher interface
        // Handle seamless voice and text input
        // Display conversation history with context
        // Manage customizable essential shortcuts
        // No mode switching - single consistent experience
    }
}
```

**Key Features:**
- Clean chat-style conversation display
- Voice and text input seamlessly integrated (no mode switching)
- Minimal, customizable essential shortcuts
- Standard Android notifications/quick settings
- Zero learning curve - natural language only
- Inspired by olauncher's distraction-free philosophy

### 2. Local AI Understanding Engine

On-device AI processing using Gemma 3n via MediaPipe LLM Inference API for complete privacy and intelligent conversation understanding.

```kotlin
class LocalAIEngine {
    private val llmInference = LlmInference.createFromOptions(
        context,
        LlmInference.LlmInferenceOptions.builder()
            .setModelPath("/data/local/tmp/llm/gemma-3n.bin")
            .setMaxTokens(1024)
            .setTopK(40)
            .setTemperature(0.8f)
            .setRandomSeed(101)
            .build()
    )
    private val contextManager = UnifiedContextManager()
    private val preferenceLearner = SilentLearningEngine()
    
    suspend fun processConversation(input: UserInput): IntelligentResponse {
        val context = contextManager.getConversationContext()
        val preferences = preferenceLearner.getUserPreferences()
        val prompt = buildUnifiedPrompt(input, context, preferences)
        
        val response = gemmaModel.generate(prompt)
        val action = extractIntelligentAction(response)
        
        // Learn from interaction silently
        preferenceLearner.observeInteraction(input, action)
        contextManager.updateContext(input, action)
        
        return IntelligentResponse(action, confidence = calculateConfidence(response))
    }
    
    private fun buildUnifiedPrompt(input: UserInput, context: Context, prefs: Preferences): String {
        return """
        User request: "${input.text}"
        Conversation context: ${context.summary}
        Learned preferences: ${prefs.summary}
        Available integrations: ${getDeepIntegrations()}
        
        Understand intent and determine best intelligent action.
        Focus on reducing distraction and maximizing productivity.
        """
    }
}
```

**Responsibilities:**
- Natural language understanding with context
- Intent extraction and intelligent action planning
- Silent learning and preference adaptation
- Confidence scoring and risk assessment
- Cross-conversation context management

### 3. Deep Android Integration Layer

Provides intelligent control over Android system and apps, going beyond simple app launching to actual app control.

```kotlin
class DeepAndroidIntegration {
    private val accessibilityService = cOSAccessibilityService()
    private val packageManager = context.packageManager
    private val contentResolver = context.contentResolver
    private val preferenceEngine = SilentPreferenceLearner()
    
    // Intelligent app control with context and preferences
    fun executeIntelligentAction(action: IntelligentAction) {
        when (action.type) {
            ActionType.SHOW_PHOTOS -> {
                val filters = action.contentFilters // person, date, location, etc.
                val preferredApp = preferenceEngine.getPreferredGalleryApp()
                launchGalleryWithIntelligentFilters(preferredApp, filters)
            }
            ActionType.NAVIGATE -> {
                val destination = action.destination
                val preferredMapsApp = preferenceEngine.getPreferredMapsApp()
                launchMapsWithContextualSearch(preferredMapsApp, destination)
            }
            ActionType.SEND_MESSAGE -> {
                val recipient = action.recipient
                val message = action.message
                val preferredApp = preferenceEngine.getMessagingAppForContact(recipient)
                executeSmartMessaging(preferredApp, recipient, message)
            }
            ActionType.CONTROL_SYSTEM -> {
                executeSystemControl(action.systemCommand)
            }
        }
    }
    
    // Advanced content filtering with AI understanding
    fun getIntelligentContent(query: ContentQuery): List<SmartContent> {
        return when (query.type) {
            ContentType.PHOTOS -> getPhotosWithAIFiltering(query.naturalLanguageFilter)
            ContentType.CONTACTS -> getContactsWithIntelligentSearch(query.searchCriteria)
            ContentType.FILES -> getFilesWithContextualFiltering(query.fileRequest)
            ContentType.APPS -> getRelevantAppsForContext(query.context)
        }
    }
}
```

**Advanced Capabilities:**
- Intelligent app launching with learned preferences
- AI-powered content filtering (photos by natural language description)
- Smart messaging routing based on contact preferences
- System setting modifications through conversation
- Cross-app intelligent workflows
- Contextual app recommendations

### 4. Silent Preference Learning Engine

Silently learns user preferences to reduce cognitive load and improve experience without asking.

```kotlin
class SilentPreferenceLearningEngine {
    private val encryptedPreferences = EncryptedPreferences()
    private val behaviorAnalyzer = IntelligentBehaviorAnalyzer()
    private val contextAnalyzer = ConversationContextAnalyzer()
    
    fun observeInteractionSilently(input: UserInput, action: IntelligentAction, outcome: ActionOutcome) {
        // Learn messaging app preferences per contact without asking
        if (action is MessageAction) {
            updateMessagingPreferencesSilently(action.recipient, action.chosenApp, outcome.userSatisfaction)
        }
        
        // Learn conversation style preferences from user behavior
        updateConversationStyleSilently(input.style, outcome.userEngagement)
        
        // Learn confirmation preferences based on user patterns
        updateConfirmationPreferencesSilently(action.riskLevel, outcome.userAcceptance)
        
        // Learn app preferences for different contexts
        updateContextualAppPreferences(input.context, action.chosenApp, outcome.effectiveness)
        
        // Learn shortcut customization from usage patterns
        updateShortcutPreferencesFromUsage(input.triggerMethod, outcome.efficiency)
    }
    
    private fun updateMessagingPreferencesSilently(recipient: String, app: String, satisfaction: Float) {
        if (satisfaction > SATISFACTION_THRESHOLD) {
            val confidence = calculateConfidence(recipient, app)
            encryptedPreferences.setMessagingAppWithConfidence(recipient, app, confidence)
            logSilentLearning("Learned messaging preference: $recipient → $app (confidence: $confidence)")
        }
    }
    
    // Learn without interrupting user flow
    fun getLearnedPreference(context: ConversationContext): LearnedPreference? {
        return encryptedPreferences.getConfidentPreference(context)
    }
}
```

**Silent Learning Areas:**
- Messaging app preferences per contact (no asking)
- Conversation style adaptation (verbosity, formality)
- Confirmation requirement patterns
- Contextual app preferences
- Shortcut usage optimization
- Response timing preferences
- Integration depth preferences

### 5. Built-in Tools Suite

Essential tools to reduce dependency on external apps.

```kotlin
object BuiltInTools {
    val pdfViewer = PDFViewer()
    val calculator = SmartCalculator()
    val notesTaker = QuickNotes()
    val timerManager = TimerManager()
    val unitConverter = UnitConverter()
    
    fun handleToolRequest(toolIntent: ToolIntent): ToolResponse {
        return when (toolIntent.tool) {
            Tool.PDF_VIEWER -> pdfViewer.open(toolIntent.file)
            Tool.CALCULATOR -> calculator.calculate(toolIntent.expression)
            Tool.NOTES -> notesTaker.createNote(toolIntent.content)
            Tool.TIMER -> timerManager.setTimer(toolIntent.duration)
            Tool.CONVERTER -> unitConverter.convert(toolIntent.conversion)
        }
    }
}
```

## Unified Data Flow

### Simplified Conversation Flow
```
User speaks/types "Show photos of Sarah from vacation"
    ↓
Unified input processing (voice + text seamlessly)
    ↓
Local AI understands with full context + learned preferences
    ↓
Intelligent action determined: {type: SHOW_PHOTOS, filters: [person: "Sarah", context: "vacation"]}
    ↓
Deep Android integration executes smart gallery filtering
    ↓
User sees exactly what they wanted
    ↓
Silent learning observes satisfaction and updates preferences
```

### Key Differences from Previous Multi-Mode System
- **No mode switching** - single conversation flow
- **AI handles complexity** - user doesn't choose interface paradigm  
- **Silent learning** - preferences learned without asking
- **Minimal feedback** - actions happen with minimal interruption
- **Context preservation** - conversation flows naturally

## Key Design Patterns

### 1. **Unified Facade Pattern**
DeepAndroidIntegration provides single, intelligent interface to all Android capabilities.

### 2. **Silent Observer Pattern**  
For non-intrusive preference learning and context updates.

### 3. **Intelligent Command Pattern**
For smart action execution with learned confirmation preferences.

### 4. **Strategy Pattern**
For different intelligent action types with contextual execution.

### 5. **Builder Pattern**
For constructing complex AI prompts with context, preferences, and capabilities.

## Security & Privacy Architecture

### On-Device Processing
```kotlin
class PrivacyManager {
    // All AI processing happens locally via MediaPipe
    private val localLLM = MediaPipeLLM(modelPath = "/data/local/tmp/llm/")
    
    // No network requests for core functionality
    fun processUserInput(input: String): Response {
        // Everything happens on-device
        return localLLM.process(input)
    }
    
    // Optional cloud features require explicit consent
    fun enableCloudFeature(feature: CloudFeature) {
        if (hasUserConsent(feature)) {
            cloudFeatures.enable(feature)
        }
    }
}
```

### Data Storage
- **Encrypted preferences** for learned behavior
- **Conversation history** with configurable retention
- **No telemetry** without explicit user consent
- **Secure model storage** for AI weights

## Performance Considerations

### Memory Management
```kotlin
class PerformanceManager {
    // Lazy loading of AI model with MediaPipe/LiteRT optimization
    private val aiModel by lazy { 
        MediaPipeLLMLoader.loadGemma3nModel() 
    }
    
    // Efficient conversation history
    private val conversationBuffer = CircularBuffer<Message>(maxSize = 100)
    
    // Background cleanup
    fun performMemoryCleanup() {
        // Clean old conversation history
        // Compress unused model weights
        // Clear temporary processing data
    }
}
```

### Response Times
- **Target**: <300ms for intent processing (1.5x faster with Gemma 3n)
- **Target**: <500ms for action execution
- **Strategy**: LiteRT optimization, int4 quantization, GPU delegate support

### Battery Optimization
- **Selective wake**: Only process when launcher is active
- **Efficient model**: Gemma 3n with <2GB memory footprint
- **Advanced quantization**: Int4 quantization for 2.5-4x size reduction
- **Smart background**: Minimal processing when not in use

## Extension Points

### 1. Custom Tools Integration
```kotlin
interface CustomTool {
    fun canHandle(intent: ToolIntent): Boolean
    fun execute(intent: ToolIntent): ToolResponse
    val metadata: ToolMetadata
}

class ToolRegistry {
    fun registerTool(tool: CustomTool)
    fun findToolForIntent(intent: ToolIntent): CustomTool?
}
```

### 2. AI Model Swapping
Support for different local models based on device capabilities:
- Gemma 3n (default - mobile-optimized)
- Gemma 3 1B (lower resource devices)
- Gemma 3 4B (high-end devices with >4GB RAM)

### 3. Platform Extensions
- **Tasker Integration** - Export learned automations
- **IFTTT Support** - Cross-platform automation
- **Smart Home** - Control IoT devices

## Testing Strategy

### Unit Tests
- AI intent extraction accuracy
- Android integration reliability
- Preference learning algorithms
- Security and privacy compliance

### Integration Tests
- End-to-end conversation flows
- Cross-app integration scenarios
- Performance under load
- Battery usage measurement

### User Experience Tests
- Conversation naturalness
- Response accuracy
- Learning effectiveness
- Accessibility compliance

## Scalability Considerations

The architecture is designed to scale from basic conversation to advanced AI assistant:

1. **Model Upgrades**: Easy swapping of AI models
2. **Feature Expansion**: Plugin-like tool system
3. **Platform Growth**: Extensible Android integration
4. **User Growth**: Efficient local processing

## Architectural Decision: Single App vs Modular

**Why Single Unified App:**
- **Simplicity**: Matches our vision of reducing complexity and distraction
- **Community Focus**: All contributors work on the same cohesive experience
- **Faster Development**: Single codebase with unified testing and deployment
- **User Experience**: No fragmentation, consistent interface and behavior
- **Privacy**: Easier to audit and secure with everything in one app

**Future Considerations:**
- Post v1.0, selective modularity may be introduced for themes and specialized tools
- Core experience will always remain unified
- Any future extensions will enhance, not fragment, the main experience

This architecture prioritizes simplicity, privacy, and user experience while enabling focused community development around a shared vision.

## Development Accelerators

### MediaPipe LLM Inference Integration
cOS leverages Google's MediaPipe LLM Inference API for optimal Gemma 3n integration:
- **Pre-built GPU acceleration** for mobile devices
- **Optimized model loading and caching**
- **Built-in quantization support** (INT4, FP16)
- **LoRA fine-tuning capabilities** for customization

### Reference Implementations
Learning from successful open-source launcher projects:
- **Lawnchair** - Modern launcher architecture patterns
- **Kvaesitso** - Search-focused interface design
- **Neo Launcher** - Customization and theming approaches

These accelerators reduce development time while maintaining cOS's unique conversational-first approach.