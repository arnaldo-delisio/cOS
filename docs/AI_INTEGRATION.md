# cOS AI Integration with MediaPipe Gemma 3n

## Overview

This document describes the MediaPipe LLM Inference integration that powers cOS's conversational AI capabilities using the Gemma 3n model.

## Implementation Details

### LocalAIEngine.kt Changes

The `LocalAIEngine` has been completely rewritten to use MediaPipe's LLM Inference API:

1. **Initialization**: Creates `LlmInference` instance with Gemma 3n model
2. **Inference**: Processes user input through structured prompts
3. **Response Parsing**: Extracts intent and action data from AI output
4. **Fallback**: Pattern matching for cases where AI parsing fails

### Key Features

- **Sub-300ms Response Time**: Gemma 3n is optimized for mobile inference
- **Structured Prompts**: Guide the AI to provide intent classification
- **Natural Responses**: AI generates conversational responses
- **Action Extraction**: Parses relevant data (person names, dates, etc.)

## Setup Instructions

### 1. Build and Install

```bash
# Build and install the app
./gradlew installDebug
```

### 2. Automatic Model Download

On first launch, cOS will:
- Detect that the AI model is missing
- Show a user-friendly download dialog
- Download the Gemma 3n model (≈1.2GB) from HuggingFace
- Display real-time progress
- Store the model in app's private storage
- Automatically initialize AI features when complete

**No manual deployment needed!** The app handles everything internally.

### 3. Model Management

The new `ModelManager` class handles all model operations:

```kotlin
// Check if model exists
val isAvailable = modelManager.isModelAvailable()

// Download with progress tracking
modelManager.downloadModel().collect { progress ->
    when (progress.status) {
        DOWNLOADING -> updateProgress(progress.progress)
        COMPLETED -> initializeAI()
        ERROR -> showRetryDialog()
    }
}

// Get model location (internal storage)
val modelPath = modelManager.getModelPath()
// Returns: /data/data/os.conversational.cos/files/models/gemma-3n.bin
```

### 4. Testing

Test the AI with various conversational inputs:
- "Show photos of Sarah from vacation"
- "Calculate 15% tip on $50"
- "Text mom I'm running late"
- "Find pizza places nearby"

## Architecture

```
User Input → MediaPipe LLM → Structured Response → Intent + Action Data
     ↓                              ↓                        ↓
Voice/Text              "INTENT: SHOW_FILTERED_PHOTOS"   ConversationEngine
                        "RESPONSE: Finding photos..."     executes action
                        "DATA: person:Sarah,..."
```

### Model Storage Architecture

```
App Launch → Check Model → Missing? → Show Download Dialog
     ↓           ↓           ↓              ↓
  Continue    Found at    Download    Progress UI
             app storage   1.2GB      Real-time %
                 ↓           ↓              ↓
            Initialize   Store in      Complete
               AI       app/files/    Initialize
```

## Performance Optimization

1. **Model**: Gemma 3n is 1.5x faster than Gemma 2B
2. **Quantization**: Int4 quantization reduces model size by 2.5-4x
3. **GPU Acceleration**: MediaPipe automatically uses GPU when available
4. **Caching**: Model loaded once and reused for all conversations

## Troubleshooting

### Model Not Found
```
Error: Model file not found. Please download the AI model through the app.
```
Solution: The app will automatically show a download dialog. Simply tap "Download" to get the model.

### Slow Inference
- Ensure testing on physical device (not emulator)
- Check if device has GPU support
- Monitor memory usage - needs 2GB+ RAM

### AI Not Understanding Intent
- Check logcat for prompt/response: `adb logcat | grep LocalAIEngine`
- Fallback pattern matching should still work
- Consider adjusting temperature/top_k parameters

## Future Enhancements

1. **Fine-tuning**: Use LoRA adapters for cOS-specific tasks
2. **Context Window**: Implement conversation history management
3. **Multi-turn**: Support complex multi-turn conversations
4. **Personalization**: Learn user preferences over time

## Resources

- [MediaPipe LLM Inference Guide](https://developers.google.com/mediapipe/solutions/genai/llm_inference)
- [Gemma 3n Documentation](https://ai.google.dev/gemma/docs/gemma-3n)
- [MediaPipe Android Samples](https://github.com/google-ai-edge/mediapipe-samples)