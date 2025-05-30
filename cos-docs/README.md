# cOS - Conversational Operating System

> **Transform any Android device into a voice-first, privacy-respecting, fully conversational smartphone experience.**

## 🌟 Vision

cOS replaces traditional touch interfaces with natural conversation, making smartphones truly accessible to everyone. Built on proven open-source components, it offers a privacy-first alternative to Big Tech assistants.

## 🚀 **Two Revolutionary Approaches**

### 🎯 **Main Branch - Traditional Conversational OS**
The foundational conversational operating system with:
- Voice-to-text processing (Vosk)
- Conversational skills system
- File management through voice
- App control via conversation
- Traditional Android app integration

**Perfect for:** Users who want conversational control while keeping familiar Android interface

### ⌚ **Smartwatch Branch - Contextual Widget Interface** 
Revolutionary smartwatch-like phone experience with:
- Apps surface contextually within conversation
- Minimal, clean interface (no app grid)
- Predictive widget system
- Background intelligent app orchestration
- Conversation-centric design paradigm

**Perfect for:** Users who want a completely reimagined mobile interface

## 🏗️ Architecture (Main Branch)

### Core Components
```
┌─────────────────────┐
│   Voice Interface   │ ← User speaks/types
├─────────────────────┤
│ Conversation Engine │ ← LLM + Intent Classification  
├─────────────────────┤
│   Skill Router      │ ← Route to appropriate skill
├─────────────────────┤
│      Skills         │ ← File, System, App, Device
├─────────────────────┤
│  Component APIs     │ ← Material Files, Easer, etc.
├─────────────────────┤
│  Android System     │ ← Native Android APIs
└─────────────────────┘
```

### Built On
- **Dicio**: Voice processing foundation  
- **Material Files**: File management engine
- **Easer**: System automation framework
- **Headwind MDM**: Device control APIs

## 🎯 Current Status (Main Branch)

**✅ Working Now:**
- Voice-to-text with Vosk
- Text-to-speech responses
- File management through conversation
- App launching via voice
- System permissions integration

**Voice Commands:**
```
"List files in downloads"
"Organize my pictures" 
"Open calculator"
"Launch Spotify"
```

## 🚀 Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd cos-core

# For traditional conversational OS
git checkout master

# For smartwatch-like interface
git checkout smartwatch-interface

# Build and run
./gradlew assembleDebug
```

## 🌟 Branch Comparison

| Feature | Main Branch | Smartwatch Branch |
|---------|-------------|-------------------|
| **Interface** | Traditional Android + Voice | Minimal conversation stream |
| **App Integration** | Launch existing apps | Contextual widgets |
| **Complexity** | Familiar for Android users | Revolutionary new paradigm |
| **Development Stage** | Stable foundation | Experimental innovation |
| **Use Case** | Gradual transition | Complete interface reimagining |

## 🤝 Contributing

Choose your branch based on your interest:
- **Main Branch**: Enhance traditional conversational features
- **Smartwatch Branch**: Push the boundaries of mobile interface design

## 📄 License

Apache License 2.0

## 🌐 Community

- **GitHub**: [conversational-os](https://github.com/conversational-os)
- **Discord**: [Join our community](https://discord.gg/conversational-os)
- **Twitter**: [@conversational_os](https://twitter.com/conversational_os)

---

**cOS - Two paths to conversational mobile computing** 🚀
