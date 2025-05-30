# Assets Directory

## Vosk Speech Recognition Model

To use voice recognition in cOS, you need to download the Vosk model:

1. Download the small English model from:
   https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip

2. Extract the zip file

3. Rename the extracted folder to: `vosk-model-small-en-us-0.15`

4. Place the entire folder in this `assets` directory

The final structure should be:
```
assets/
└── vosk-model-small-en-us-0.15/
    ├── am/
    ├── graph/
    ├── ivector/
    └── conf/
```

## Alternative: Automatic Download

The app will attempt to download the model automatically on first run if it's not found in assets.

## Model Size

- Small model: ~40MB (faster, less accurate)
- Large model: ~1.8GB (slower, more accurate)

For development and testing, the small model is recommended.
