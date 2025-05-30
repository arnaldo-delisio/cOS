# cOS Scripts

This directory contains utility scripts for cOS development and deployment.

## Available Scripts

### deploy_model.sh
Deploys the Gemma 3n AI model to an Android device for testing.

**Usage:**
```bash
./scripts/deploy_model.sh
```

This script will:
1. Download the Gemma 3n model from HuggingFace (if not already downloaded)
2. Create the necessary directories on your Android device
3. Push the model to `/data/local/tmp/llm/` on the device
4. Verify the deployment was successful

**Requirements:**
- Android device connected via ADB
- Active internet connection for model download
- ~2GB free space on device

## Future Scripts

Additional scripts will be added here for:
- Development environment setup
- Testing automation
- Release builds
- Performance profiling