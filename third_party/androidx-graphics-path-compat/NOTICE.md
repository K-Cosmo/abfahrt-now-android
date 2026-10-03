# AndroidX graphics-path compatibility source

AbfahrtApp Build 130 contains an API-34+ compatibility adaptation derived from AndroidX `graphics-path` source code from the Android Open Source Project / AndroidX project.

Upstream project: <https://github.com/androidx/androidx/tree/androidx-main/graphics/graphics-path>

The adapted source keeps the `androidx.graphics.path` public API needed by Compose, removes the pre-API-34 JNI path because AbfahrtApp's minSdk is 34, and uses the upstream pure-Kotlin conic conversion algorithm.

Copyright 2022 The Android Open Source Project.
Licensed under the Apache License, Version 2.0. See `LICENSE` in this directory.

This file is attribution/licensing information only and is not a normative AbfahrtApp product or architecture document. `/doc` remains the sole normative project source.
