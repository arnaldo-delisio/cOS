# cOS - Conversational Operating System

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Android](https://img.shields.io/badge/Platform-Android%207.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)

> Transform any Android device into an adaptive, voice-first, privacy-respecting conversational experience.

## 🌟 Overview

cOS is an open-source Android application that reimagines how we interact with our smartphones through natural conversation. Unlike traditional voice assistants, cOS offers three adaptive interaction modes that users can switch between based on their needs:

- **🎯 Classic Mode** - Traditional Android interface enhanced with voice commands
- **⚡ Widget Mode** - Revolutionary contextual widgets that appear within conversations  
- **🔄 Hybrid Mode** - Intelligent switching between modes based on context

## ✨ Key Features

- **🎤 Voice-First Interface** - Natural language processing with on-device speech recognition (Vosk)
- **💬 Chat Interface** - Full text-based interaction for accessibility and privacy
- **🧩 Modular Skills System** - Extensible architecture for adding new conversational capabilities
- **🔒 Privacy by Design** - All processing happens on-device by default
- **♿ Accessibility** - Screen reader support, high contrast themes, keyboard navigation
- **🎨 Material Design 3** - Modern, clean interface following latest Android design guidelines

## 🚀 Getting Started

### Prerequisites

- Android Studio 2024.1+
- Android SDK 24+ (minimum), 34+ (recommended)
- JDK 11+
- Physical Android device (recommended for testing)

### Installation

```bash
# Clone the repository
git clone https://github.com/arnaldo-delisio/cos.git
cd cos/cos-core

# Build and install on connected device
./gradlew installDebug

# Or build APK
./gradlew assembleDebug
```

### First Run

1. Grant necessary permissions (microphone, storage)
2. Complete the onboarding tutorial
3. Try your first voice command: "What can you do?"
4. Switch between modes using the mode toggle button

## 🏗️ Architecture

cOS uses a modular architecture designed for extensibility:

```
┌─────────────────────┐
│   Input Layer       │ ← Voice, Text, Touch
├─────────────────────┤
│ Conversation Engine │ ← Natural language processing
├─────────────────────┤
│  Context Analyzer   │ ← Determines optimal UI response
├─────────────────────┤
│   Skill Router      │ ← Routes to appropriate skill
├─────────────────────┤
│  Presentation Layer │ ← Classic App / Widget / Hybrid
└─────────────────────┘
```

### Core Components

- **Conversation Engine** - Processes all inputs and maintains conversation context
- **Adaptive UI Manager** - Handles mode switching and UI adaptation
- **Skill System** - Modular capabilities (file management, app control, etc.)
- **Voice Engine** - Vosk integration for speech recognition, Android TTS for responses

## 🛠️ Development

### Project Structure

```
cos/
├── cos-core/           # Main Android application
├── cos-skills/         # Additional conversational skills
├── cos-launcher/       # Home screen replacement (future)
├── cos-components/     # Shared UI components
├── cos-docs/          # Documentation
└── cos-examples/      # Example implementations
```

### Creating a New Skill

```kotlin
class WeatherSkill : BaseSkill() {
    override fun processIntent(input: String, context: Context): SkillResponse {
        return SkillResponse(
            textResponse = "Currently 72°F and sunny",
            widgetView = WeatherWidget(temperature = 72, condition = "Sunny"),
            classicAction = LaunchWeatherApp(),
            suggestedMode = UIMode.WIDGET
        )
    }
}
```

### Building from Source

See [Development Guide](cos-docs/DEVELOPMENT_GUIDE.md) for detailed instructions.

## 🗺️ Roadmap

### Phase 1: Foundation (Current)
- ✅ Basic conversation engine
- ✅ Voice processing integration
- ✅ Initial skill system
- 🔄 Multi-mode UI system
- 🔄 Chat interface implementation

### Phase 2: Core Features
- 📋 Enhanced voice recognition
- 📋 Additional skills (Calendar, Contacts, System Control)
- 📋 Widget library expansion
- 📋 Context persistence

### Phase 3: Advanced Features
- 📋 Wake word detection
- 📋 Launcher replacement mode
- 📋 Plugin SDK for developers
- 📋 Multi-language support

## 🤝 Contributing

We welcome contributions! cOS is a community-driven project and we appreciate:

- 🐛 Bug reports and fixes
- 💡 Feature suggestions and implementations
- 📚 Documentation improvements
- 🌍 Translations
- 🧪 Testing on different devices

Please see our [Contributing Guide](cos-docs/CONTRIBUTING.md) for details.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](cos-docs/LICENSE) file for details.

## 🙏 Acknowledgments

Built on the shoulders of giants:
- [Vosk](https://alphacephei.com/vosk/) - On-device speech recognition
- [Material Design 3](https://m3.material.io/) - Design system
- [Kotlin](https://kotlinlang.org/) - Programming language
- Android Open Source Project

## 📞 Contact

- **GitHub Issues**: [Report bugs or request features](https://github.com/arnaldo-delisio/cos/issues)
- **Discussions**: [Join the conversation](https://github.com/arnaldo-delisio/cos/discussions)

---

**cOS - Making smartphones conversational, accessible, and private for everyone.** 🚀