# cOS - Conversational Operating System

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Android](https://img.shields.io/badge/Platform-Android%207.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![AI](https://img.shields.io/badge/AI-Local%20LLM-orange.svg)](https://github.com/google/gemma.cpp)

> Transform your Android device into a distraction-free, productivity-focused conversational interface.

## 🌟 Overview

cOS is a revolutionary Android launcher that replaces app-hopping with natural conversation. Instead of hunting through apps and menus, simply talk or type to your phone like you would to a smart assistant - but completely private and on-device.

**Core Philosophy:** *Reduce phone distraction while maximizing productivity through natural conversation.*

### ✨ Key Features

- **🎤 Conversation-First** - Natural language interface with zero learning curve
- **🧠 AI-Powered Understanding** - Local LLM (Gemma 3n) processes requests intelligently  
- **📱 Deep Android Integration** - Actually controls apps, not just launches them
- **🎯 Distraction Reduction** - Clean launcher inspired by olauncher's minimalism
- **🔒 Complete Privacy** - All AI processing happens on-device
- **🛠️ Built-in Essentials** - PDF viewer, calculator, notes to reduce app dependency
- **🧭 Intelligent Actions** - Filtered gallery, smart messaging, contextual app control
- **📚 Silent Learning** - AI learns preferences without asking, reducing cognitive load

## 🚀 How It Works

### Simple User Journey
```
1. Open cOS (clean launcher interface)
2. Speak or type naturally: "Show photos of Sarah from vacation"
3. AI understands and acts intelligently
4. Action happens with minimal interruption
5. Continue conversation seamlessly
```

### Example Interactions
```
You: "Show photos of Sarah from vacation"
→ Opens Gallery filtered to Sarah's photos from your last trip

You: "Text mom I'm running late"  
→ Sends message via your preferred app for that contact

You: "Find pizza places nearby"
→ Opens Maps with pizza restaurant results and reviews
```

### The Interface
```
┌─────────────────────────────┐
│  9:41 AM    📶 🔋 95%       │  ← Standard status bar
├─────────────────────────────┤
│                             │
│         cOS                 │  ← Minimal branding
│                             │
│  How can I help you today?  │  ← AI ready to assist
│                             │
│  [Recent conversations...]   │  ← Context preservation
│                             │
├─────────────────────────────┤
│  📷  📞  [📱] [⚙️]          │  ← Essential shortcuts
├─────────────────────────────┤
│ [🎤] [Type a message...]    │  ← Always available
└─────────────────────────────┘
```

## 🏗️ Architecture

cOS is built as a **single Android application** that serves as both launcher and intelligent assistant. This approach prioritizes simplicity, community focus, and user experience consistency.

```
Input (Voice/Text) → AI Understanding → Deep Android Action → Minimal Feedback
```

### Core Principles

1. **Single App Experience** - Everything in one cohesive experience, no fragmentation
2. **Conversation-First** - Everything happens through natural language
3. **Distraction Reduction** - Inspired by olauncher's minimalism  
4. **Deep Integration** - Don't just launch apps, actually control them
5. **Silent Learning** - AI learns user preferences without asking
6. **Built-in Essentials** - Include basic tools to reduce app dependency
7. **Community-Focused** - All contributors work on the same shared vision

### Core Components

- **Conversational Interface** - Single chat + essential shortcuts
- **Local AI Engine** - Gemma 3n via MediaPipe LLM Inference API for on-device processing
- **Deep Android Integration** - Intelligent app control and content filtering
- **Preference Learning Engine** - Silent learning of user habits and preferences
- **Built-in Tool Suite** - Essential utilities to reduce app dependency
- **Launcher Replacement** - Complete home screen replacement functionality

## 🛠️ Getting Started

### Prerequisites

- Android 7.0+ (API 24+)
- 2GB+ RAM (for local AI with Gemma 3n)
- Android Studio 2024.1+ (for development)

### Installation

```bash
# Clone the repository
git clone https://github.com/arnaldo-delisio/cOS.git
cd cOS

# Build and install the app
./gradlew installDebug
```

### First Run

1. Set cOS as your default launcher
2. Grant necessary permissions (microphone, accessibility, storage)
3. Start with simple requests: "Open camera" or "What's the weather?"
4. The AI learns your preferences automatically

## 🎯 What Makes cOS Different

### vs Traditional Launchers
- **Conversation-first** instead of app-grid focused
- **Zero learning curve** - natural language, no special commands
- **Invisible interface** - the best interface is no interface

### vs Voice Assistants  
- **Complete privacy** - all processing happens on-device
- **Deep integration** - actually controls apps, not just launches them
- **Launcher replacement** - seamless experience, not an overlay

### vs Multi-Mode Systems
- **Single experience** - no mode complexity or user confusion
- **AI handles complexity** - smart actions without user decisions
- **Consistent behavior** - same interface for all interactions

## 🗺️ Roadmap

### v0.1.0 - Foundation (Current)
- ✅ Minimal launcher interface
- ✅ Basic conversation engine
- ✅ Local AI (Gemma 3n) integration via MediaPipe
- 🔄 Essential conversational controls

### v0.2.0 - Deep Integration
- 📋 Intelligent photo/gallery filtering
- 📋 Smart messaging routing by contact
- 📋 Built-in tool suite (PDF, calculator, notes)
- 📋 Silent preference learning

### v0.3.0 - Advanced Intelligence
- 📋 Cross-app intelligent workflows
- 📋 Proactive AI suggestions
- 📋 Seamless voice activation
- 📋 Power user gesture shortcuts

### v1.0.0 - Public Release
- 📋 Complete conversational skill library
- 📋 Performance optimization for all devices
- 📋 Comprehensive privacy and security testing
- 📋 Play Store submission

## 🤝 Contributing

We welcome contributions to make phones more conversational and less distracting! cOS is built as a **single application** where all community efforts focus on improving the core experience.

**Community-Focused Development:**
- 🎯 **Shared Vision** - All contributors work on the same cohesive experience
- 🧠 **AI Integration** - Help implement and optimize local AI conversation understanding
- 📱 **Deep Android Control** - Build intelligent app integration and content filtering
- 🎨 **Conversational Interface** - Improve the distraction-free launcher experience
- 🛠️ **Built-in Tools** - Add essential functionality to reduce app dependency

**Ways to Contribute:**
- 🧪 Test the conversation experience on different devices
- 💡 Suggest new intelligent conversation patterns and AI improvements
- 🐛 Report bugs or unexpected AI behavior
- 📝 Improve documentation and user guides
- 🔧 Submit code improvements for the app

See our [Contributing Guide](docs/CONTRIBUTING.md) for details.

## 📚 Documentation

- **[Architecture](docs/ARCHITECTURE.md)** - Technical design and components
- **[Development Guide](docs/DEVELOPMENT_GUIDE.md)** - Setup and development workflow  
- **[Roadmap](docs/ROADMAP.md)** - Feature timeline and planning

## 📄 License

Licensed under the Apache License 2.0 - see [LICENSE](docs/LICENSE) for details.

## 🙏 Acknowledgments

- [Gemma 3n](https://ai.google.dev/gemma/docs/gemma-3n) - Mobile-optimized local AI model for on-device processing
- [MediaPipe](https://github.com/google-ai-edge/mediapipe) - LLM Inference API for optimized on-device AI
- [Vosk](https://alphacephei.com/vosk/) - Privacy-focused speech recognition
- [olauncher](https://github.com/tanujnotes/Olauncher) - Inspiration for minimal design
- [Lawnchair](https://github.com/LawnchairLauncher/lawnchair) - Modern launcher architecture reference
- Android Open Source Project

---

**cOS - Your phone, reimagined through conversation.** 🚀

*Less distraction. More productivity. Complete privacy.*