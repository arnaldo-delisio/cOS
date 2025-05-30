#!/bin/bash
# Script to download and deploy Gemma 3n model for cOS

echo "=== cOS Gemma 3n Model Deployment Script ==="
echo

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "Error: No Android device connected. Please connect a device and try again."
    exit 1
fi

# Model URL - Update this with the actual Gemma 3n model URL
MODEL_URL="https://huggingface.co/google/gemma-3n-2B-it-litert/resolve/main/gemma-3n-2B-it.bin"
MODEL_FILE="gemma-3n.bin"

echo "1. Downloading Gemma 3n model..."
echo "   This may take a while depending on your connection speed."

# Download model if not already present
if [ ! -f "$MODEL_FILE" ]; then
    wget -c "$MODEL_URL" -O "$MODEL_FILE"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to download model. Please check the URL and your internet connection."
        exit 1
    fi
else
    echo "   Model file already exists locally."
fi

echo
echo "2. Creating model directory on device..."
adb shell mkdir -p /data/local/tmp/llm/

echo
echo "3. Pushing model to device..."
echo "   This may take several minutes..."
adb push "$MODEL_FILE" /data/local/tmp/llm/
if [ $? -ne 0 ]; then
    echo "Error: Failed to push model to device."
    exit 1
fi

echo
echo "4. Verifying model deployment..."
MODEL_SIZE=$(adb shell ls -l /data/local/tmp/llm/gemma-3n.bin | awk '{print $5}')
if [ -z "$MODEL_SIZE" ] || [ "$MODEL_SIZE" -eq 0 ]; then
    echo "Error: Model file not found or empty on device."
    exit 1
fi

echo "   Model successfully deployed (size: $MODEL_SIZE bytes)"
echo
echo "=== Deployment Complete ==="
echo
echo "Next steps:"
echo "1. Build and install the cOS app: ./gradlew installDebug"
echo "2. Launch cOS and test the AI conversation"
echo "3. Check logcat for MediaPipe initialization: adb logcat | grep LocalAIEngine"
echo
echo "Note: Make sure you're testing on a physical device with GPU support."
echo "      Emulators may not fully support MediaPipe GPU acceleration."