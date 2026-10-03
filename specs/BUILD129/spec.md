# Build 129 Specification — MapLibre 13.6.0 16-KB upstream evidence

## Goal
Perform one final isolated upstream MapLibre evidence test after Build 128 proved that OpenGL 12.3.1 still fails the 16-KB GNU_RELRO gate on both 64-bit ABIs.

## Requirements
- Use stable `org.maplibre.gl:android-sdk-opengl:13.6.0`.
- Keep the existing OpenGL renderer family; do not introduce Vulkan or multi-backend behavior.
- Keep `androidx.graphics:graphics-path:1.1.0` unchanged and track it as a separate blocker.
- No source-level UI, transit, ORS, dedup, reachability, security or map-presentation logic change.
- Release APK artifact audit is the acceptance evidence; dependency version/NDK version alone is not proof.

## Acceptance
- `lib/arm64-v8a/libmaplibre.so` = `OK ELF`.
- `lib/x86_64/libmaplibre.so` = `OK ELF`.
- no 64-bit MapLibre ZIP-alignment failure.
- Remaining hard failures, if any, may only be the separately tracked AndroidX `graphics-path` binary.

## Stop condition
If MapLibre 13.6.0 still fails either 64-bit ELF gate, stop version iteration. The next remediation must be source-/binary-level and explicitly scoped rather than another blind dependency bump.
