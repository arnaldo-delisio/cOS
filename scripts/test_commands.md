# cOS Test Commands

## Basic Commands (Work Without AI)
1. "List files in downloads"
2. "Open camera app"
3. "Show installed apps"
4. "Calculate 50 divided by 5"

## AI-Powered Commands (Require Model)
1. "Show photos of Sarah from vacation"
2. "Text mom I'm running late"
3. "Find pizza places nearby"
4. "Calculate 15% tip on $50"
5. "Organize my downloads folder"
6. "Show me photos from yesterday"

## Edge Cases to Test
1. "What's the weather?" (Should fail gracefully)
2. "Play music" (Not implemented yet)
3. Random gibberish (Should return UNKNOWN intent)
4. Very long sentences (Test prompt limits)

## Performance Testing
- Measure time from speech end to response start
- Check memory usage during AI inference
- Monitor battery drain during extended use