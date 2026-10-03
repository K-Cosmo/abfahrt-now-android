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
 * AbfahrtApp Build 130 adaptation: pre-API-34 JNI implementation removed because minSdk=34.
 */
package androidx.graphics.path

import android.graphics.Path
import android.graphics.PathIterator as PlatformPathIterator
import android.graphics.PointF
import androidx.graphics.path.PathIterator.ConicEvaluation

internal abstract class PathIteratorImpl(
    val path: Path,
    val conicEvaluation: ConicEvaluation = ConicEvaluation.AsQuadratics,
    val tolerance: Float = 0.25f,
) {
    private val pointsData = FloatArray(8)

    abstract fun calculateSize(includeConvertedConics: Boolean): Int
    abstract fun hasNext(): Boolean
    abstract fun peek(): PathSegment.Type
    abstract fun next(points: FloatArray, offset: Int = 0): PathSegment.Type

    fun next(): PathSegment {
        val type = next(pointsData, 0)
        if (type == PathSegment.Type.Done) return DoneSegment
        if (type == PathSegment.Type.Close) return CloseSegment
        val weight = if (type == PathSegment.Type.Conic) pointsData[6] else 0.0f
        return PathSegment(type, floatsToPoints(pointsData, type), weight)
    }

    private fun floatsToPoints(pointsData: FloatArray, type: PathSegment.Type): Array<PointF> =
        when (type) {
            PathSegment.Type.Move ->
                arrayOf(PointF(pointsData[0], pointsData[1]))
            PathSegment.Type.Line ->
                arrayOf(
                    PointF(pointsData[0], pointsData[1]),
                    PointF(pointsData[2], pointsData[3]),
                )
            PathSegment.Type.Quadratic,
            PathSegment.Type.Conic ->
                arrayOf(
                    PointF(pointsData[0], pointsData[1]),
                    PointF(pointsData[2], pointsData[3]),
                    PointF(pointsData[4], pointsData[5]),
                )
            PathSegment.Type.Cubic ->
                arrayOf(
                    PointF(pointsData[0], pointsData[1]),
                    PointF(pointsData[2], pointsData[3]),
                    PointF(pointsData[4], pointsData[5]),
                    PointF(pointsData[6], pointsData[7]),
                )
            else -> emptyArray()
        }
}

/** API-34+ implementation backed by android.graphics.PathIterator. */
internal class PathIteratorApi34Impl(
    path: Path,
    conicEvaluation: ConicEvaluation = ConicEvaluation.AsQuadratics,
    tolerance: Float = 0.25f,
) : PathIteratorImpl(path, conicEvaluation, tolerance) {
    private val platformIterator = path.pathIterator
    private val conicConverter = ConicConverter()

    override fun calculateSize(includeConvertedConics: Boolean): Int {
        val convertConics =
            includeConvertedConics && conicEvaluation == ConicEvaluation.AsQuadratics
        var numVerbs = 0
        val tempIterator = path.pathIterator
        val tempFloats = FloatArray(8)
        while (tempIterator.hasNext()) {
            val type = tempIterator.next(tempFloats, 0)
            if (type == PlatformPathIterator.VERB_CONIC && convertConics) {
                with(conicConverter) {
                    convert(tempFloats, tempFloats[6], tolerance)
                    numVerbs += quadraticCount
                }
            } else {
                numVerbs++
            }
        }
        return numVerbs
    }

    override fun next(points: FloatArray, offset: Int): PathSegment.Type {
        if (conicConverter.currentQuadratic < conicConverter.quadraticCount) {
            conicConverter.nextQuadratic(points, offset)
            return PathSegment.Type.Quadratic
        }

        val typeValue = platformToAndroidXSegmentType(platformIterator.next(points, offset))
        if (
            typeValue == PathSegment.Type.Conic &&
                conicEvaluation == ConicEvaluation.AsQuadratics
        ) {
            with(conicConverter) {
                convert(points, points[6 + offset], tolerance, offset)
                if (quadraticCount > 0) {
                    nextQuadratic(points, offset)
                }
            }
            return PathSegment.Type.Quadratic
        }
        return typeValue
    }

    override fun hasNext(): Boolean = platformIterator.hasNext()

    override fun peek(): PathSegment.Type = platformToAndroidXSegmentType(platformIterator.peek())
}

private fun platformToAndroidXSegmentType(platformType: Int): PathSegment.Type =
    when (platformType) {
        PlatformPathIterator.VERB_CLOSE -> PathSegment.Type.Close
        PlatformPathIterator.VERB_CONIC -> PathSegment.Type.Conic
        PlatformPathIterator.VERB_CUBIC -> PathSegment.Type.Cubic
        PlatformPathIterator.VERB_DONE -> PathSegment.Type.Done
        PlatformPathIterator.VERB_LINE -> PathSegment.Type.Line
        PlatformPathIterator.VERB_MOVE -> PathSegment.Type.Move
        PlatformPathIterator.VERB_QUAD -> PathSegment.Type.Quadratic
        else -> throw IllegalArgumentException("Unknown path segment type $platformType")
    }
