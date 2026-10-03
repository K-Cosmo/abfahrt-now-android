# Build 128 Specification — MapLibre 16-KB remediation

## Goal
Remove the Build-127 arm64 MapLibre RELRO blocker without changing product behavior or introducing Vulkan.

## Requirements
- Use `org.maplibre.gl:android-sdk-opengl:12.3.1`.
- Keep existing MapLibre API usage unchanged unless compilation proves a minimal compatibility adjustment is necessary.
- No UI/transit/ORS behavior change.
- Artifact audit must prove MapLibre green on arm64-v8a and x86_64.
- `graphics-path` remains out of scope.
