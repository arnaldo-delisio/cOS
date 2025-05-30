# cOS (Conversational Operating System) - Project Context Prompt

## Project Overview
You are working on **cOS**, a revolutionary Android launcher that replaces traditional app-hopping with natural conversation. The core philosophy is "Reduce phone distraction while maximizing productivity through natural conversation."

**Key Concept**: cOS is a single unified Android application that serves as both a launcher and intelligent assistant, with all AI processing happening on-device for complete privacy.

## Current Status (May 2025)
- **Development Stage**: Pre-alpha (v0.1.0 target: June 2025)
- **Architecture**: Single Android app (will become launcher in v0.2.0)
- **AI Integration**: ✅ Gemma 3n via MediaPipe LLM Inference API implemented
- **Model Management**: ✅ Automatic in-app model download with progress UI
- **UI**: ✅ Modern chat interface (WhatsApp-style) with voice/text input
- **Voice**: ✅ Vosk speech recognition + Android TTS
- **Skills**: ✅ All 6 skills implemented and registered

## Technical Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **AI Model**: Gemma 3n (on-device via MediaPipe)
- **Voice Recognition**: Vosk (offline)
- **Min Android Version**: 7.0+ (API 24+)
- **Architecture Pattern**: Single unified app with skill-based modules

## Project Structure
```
cos/
├── app/src/main/java/os/conversational/cos/
│   ├── MainActivity.kt          # Main entry point with model download flow
│   ├── ai/
│   │   ├── LocalAIEngine.kt    # MediaPipe LLM integration
│   │   └── ModelManager.kt     # Handles model download/storage
│   ├── core/
│   │   └── ConversationEngine.kt # Intent routing & orchestration
│   ├── skills/                  # All skills implemented & registered
│   │   ├── AppControlSkill.kt   ✅ Registered
│   │   ├── CalculatorSkill.kt   ✅ Registered
│   │   ├── ContactsSkill.kt     ✅ Registered
│   │   ├── FileManagementSkill.kt ✅ Registered
│   │   ├── NavigationSkill.kt   ✅ Registered
│   │   └── SystemControlSkill.kt ✅ Registered
│   ├── ui/
│   │   ├── ChatInterface.kt     # Modern chat UI
│   │   ├── ModelDownloadDialog.kt # Model download UI
│   │   └── theme/              # Material 3 theming
│   ├── voice/
│   │   └── VoiceEngine.kt       # Vosk integration
│   └── widgets/                 # Data classes for skills
│       ├── Contact.kt
│       └── SystemSettings.kt
├── docs/                        # Comprehensive documentation
└── scripts/                     # Deployment tools (deprecated)
```

## Recent Fixes (Completed)
1. ✅ **Widget Classes**: Created `Contact` and `SystemSettings` in widgets package
2. ✅ **Skill Registration**: All 6 skills now registered in MainActivity
3. ✅ **Model Deployment**: Automatic in-app download, no manual deployment needed
4. ✅ **Intent Mapping**: Added COMMUNICATION and NAVIGATION intents

## Current Considerations
1. **Permissions**: Modern Android limits some system control capabilities
2. **Model Size**: ~1.2GB download on first launch (one-time)
3. **Physical Device**: AI features require real device, not emulator

## Key Design Principles
1. **Single Experience**: No modes or complexity - AI handles everything
2. **Conversation-First**: Natural language replaces app grids
3. **Complete Privacy**: All processing on-device
4. **Silent Learning**: Learns preferences without interrupting
5. **Minimal Feedback**: Actions happen with minimal UI interruption

## Performance Targets
- AI response: <200-300ms
- Voice accuracy: >95%
- Battery impact: <3-5%
- Memory: <150MB active, 2GB+ for model
- App size: <50MB (excluding model)

## Development Priorities
1. Test all skills on physical devices
2. Improve settings shortcuts and user guidance
3. Add communication features (SMS, calling)
4. Optimize AI response time
5. Add cross-app intelligent workflows
6. Prepare for launcher mode transformation (v0.2.0)

## Working Guidelines
- **Philosophy**: This is ONE app, not a collection of features
- **Focus**: Natural conversation over traditional UI
- **Privacy**: Everything stays on-device
- **Simplicity**: Reduce complexity for users
- **Performance**: Sub-300ms AI responses are critical

## Next Major Milestones
1. **v0.1.0** (June 2025): Complete conversational app
2. **v0.2.0** (August 2025): Launcher transformation
3. **v0.3.0** (October 2025): Deep Android integration
4. **v1.0.0** (December 2025): Public release

## Quick Start Commands
```bash
# Build and run (model downloads automatically on first launch)
./gradlew app:installDebug

# Run tests
./gradlew test

# Monitor logs
adb logcat | grep -E "LocalAIEngine|ModelManager"
```

## When Starting Work
1. Build and run the app
2. Test model download flow on first launch
3. Verify all 6 skills are working
4. Test voice recognition permissions
5. Focus on single unified experience

## Key Technical Features
- **Automatic Model Management**: ModelManager handles download/storage
- **Progress UI**: Real-time download progress with retry capability
- **Internal Storage**: Model stored in app's private directory
- **All Skills Active**: Calculator, Files, Apps, Contacts, Navigation, System (settings shortcuts)
- **Realistic Capabilities**: Focus on launcher features, settings shortcuts, volume control

Remember: cOS is about reimagining smartphone interaction through natural conversation, not just adding AI to existing paradigms.