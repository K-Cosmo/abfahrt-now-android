# Build 130 Specification — API-34+ graphics-path native elimination

## Goal
Eliminate the final hard 16-KB blocker proven by Build 129: the published `androidx.graphics:graphics-path:1.1.0` native library fails GNU_RELRO alignment on both gated 64-bit ABIs.

## Rationale
AbfahrtApp has `minSdk = 34`. Android 14/API 34 already provides framework `android.graphics.PathIterator`. The AndroidX native fallback exists for older Android versions and for conic conversion; AbfahrtApp does not need the pre-34 branch. Build 130 therefore excludes the external graphics-path module and provides an app-local API-compatible Kotlin implementation backed by framework PathIterator plus the AndroidX pure-Kotlin conic conversion algorithm.

## Requirements
- Keep MapLibre OpenGL 13.6.0 and DataStore 1.2.1 unchanged; both are already green on the Build-129 64-bit artifact audit.
- Exclude `androidx.graphics:graphics-path` globally so `libandroidx.graphics.path.so` cannot be packaged transitively.
- Preserve the public JVM/Kotlin API Compose consumes: `PathIterator`, `ConicEvaluation`, `PathSegment.Type`, `calculateSize`, `hasNext`, `peek`, `next`, and `Path.iterator`.
- No JNI, `System.loadLibrary`, local `.so`, CMake, or app-owned NDK build for this workaround.
- The compatibility source is valid only while `minSdk >= 34`.
- No change to transit, ORS, sorting, dedup, reachability, security, UI, or map-presentation logic.

## Acceptance
A real release APK built from Build 130 must show audit version 3 and:
- `OK ABSENT libandroidx.graphics.path.so`;
- `arm64-v8a/libdatastore_shared_counter.so` = `OK ELF`;
- `arm64-v8a/libmaplibre.so` = `OK ELF`;
- `x86_64/libdatastore_shared_counter.so` = `OK ELF`;
- `x86_64/libmaplibre.so` = `OK ELF`;
- zero 64-bit/unknown gating failures and all relevant ZIP offsets aligned.

After the artifact gate is green, Build 130 still requires the existing 16-KB runtime smoke before the overall 16-KB finding is closed.
