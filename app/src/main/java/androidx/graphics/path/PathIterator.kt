/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * AbfahrtApp Build 130 adaptation:
 * This compatibility implementation intentionally supports API 34+ only (the app minSdk is 34)
 * and therefore delegates exclusively to the Android framework PathIterator implementation.
 */
@file:JvmName("PathUtilities")

package androidx.graphics.path

import android.graphics.Path

/**
 * API-compatible replacement for AndroidX graphics-path used by Compose on Android.
 *
 * AbfahrtApp's minSdk is 34, so the native pre-34 fallback from the published AndroidX artifact
 * is unnecessary. Conic-to-quadratic conversion is implemented in Kotlin in this compatibility
 * layer to avoid packaging libandroidx.graphics.path.so.
 */
@Suppress("NotCloseable")
public class PathIterator
constructor(
    public val path: Path,
    public val conicEvaluation: ConicEvaluation = ConicEvaluation.AsQuadratics,
    public val tolerance: Float = 0.25f,
) : Iterator<PathSegment> {

    private val implementation: PathIteratorImpl =
        PathIteratorApi34Impl(path, conicEvaluation, tolerance)

    public enum class ConicEvaluation {
        /** Conic segments are returned as conic segments. */
        AsConic,

        /** Conic segments are returned as quadratic approximations. */
        AsQuadratics,
    }

    @Suppress("MissingJvmstatic")
    public fun calculateSize(includeConvertedConics: Boolean = true): Int =
        implementation.calculateSize(includeConvertedConics)

    override fun hasNext(): Boolean = implementation.hasNext()

    public fun peek(): PathSegment.Type = implementation.peek()

    @JvmOverloads
    public fun next(points: FloatArray, offset: Int = 0): PathSegment.Type =
        implementation.next(points, offset)

    override fun next(): PathSegment = implementation.next()
}

public operator fun Path.iterator(): PathIterator = PathIterator(this)

public fun Path.iterator(
    conicEvaluation: PathIterator.ConicEvaluation,
    tolerance: Float = 0.25f,
): PathIterator = PathIterator(this, conicEvaluation, tolerance)
