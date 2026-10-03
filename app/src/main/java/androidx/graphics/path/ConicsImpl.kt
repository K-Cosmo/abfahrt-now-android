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
 * AbfahrtApp Build 130 adaptation: Point storage is local to avoid introducing a new direct
 * androidx.collection dependency solely for this compatibility layer.
 */
package androidx.graphics.path

import java.util.ArrayList
import kotlin.math.abs
import kotlin.math.sqrt

private const val MAX_CONIC_TO_QUAD_COUNT = 5

internal fun conicToQuadratics(
    conicPoints: FloatArray,
    offset: Int,
    weight: Float,
    tolerance: Float,
): FloatArray {
    val conic = conicPoints.toConic(offset, weight)
    val targetQuadraticCount = conic.computeQuadraticCount(tolerance)
    val quadraticPoints = ArrayList<Float>()
    conic.splitIntoQuadratics(quadraticPoints, targetQuadraticCount)
    return quadraticPoints.toFloatArray()
}

private data class Point(val x: Float, val y: Float)

private fun Point.isFinite(): Boolean = x.isFinite() && y.isFinite()

private fun ArrayList<Float>.add(point: Point) {
    add(point.x)
    add(point.y)
}

private fun FloatArray.toConic(offset: Int, weight: Float): Conic =
    Conic(
        arrayOf(
            Point(this[0 + offset], this[1 + offset]),
            Point(this[2 + offset], this[3 + offset]),
            Point(this[4 + offset], this[5 + offset]),
        ),
        weight,
    )

private operator fun Point.plus(other: Point) = Point(x + other.x, y + other.y)
private operator fun Point.times(other: Point) = Point(x * other.x, y * other.y)
private fun between(a: Float, b: Float, c: Float): Boolean = (a - b) * (c - b) <= 0.0f

private fun subdivide(src: Conic, points: ArrayList<Float>, level: Int) {
    if (level == 0) {
        points.add(src.points[1])
        points.add(src.points[2])
        return
    }

    val split = src.split()
    val startY = src.points[0].y
    val endY = src.points[2].y
    if (between(startY, src.points[1].y, endY)) {
        val midY = split.a.points[2].y
        if (!between(startY, midY, endY)) {
            val closerY = if (abs(midY - startY) < abs(midY - endY)) startY else endY
            split.a.points[2] = Point(split.a.points[2].x, closerY)
            split.b.points[0] = Point(split.b.points[0].x, closerY)
        }
        if (!between(startY, split.a.points[1].y, split.a.points[2].y)) {
            split.a.points[1] = Point(split.a.points[1].x, startY)
        }
        if (!between(split.b.points[0].y, split.b.points[1].y, endY)) {
            split.b.points[1] = Point(split.b.points[1].x, endY)
        }
    }
    subdivide(split.a, points, level - 1)
    subdivide(split.b, points, level - 1)
}

private class Conic(val points: Array<Point>, val weight: Float) {
    fun computeQuadraticCount(tolerance: Float): Int {
        val a = weight - 1.0f
        val k = a / (4.0f * (2.0f + a))
        val x = k * (points[0].x - 2.0f * points[1].x + points[2].x)
        val y = k * (points[0].y - 2.0f * points[1].y + points[2].y)

        var error = sqrt(x * x + y * y)
        var count = 0
        while (count < MAX_CONIC_TO_QUAD_COUNT) {
            if (error <= tolerance) break
            error *= 0.25f
            count++
        }
        return count
    }

    class SplitResult(val a: Conic, val b: Conic)

    fun split(): SplitResult {
        val scale = Point(1.0f / (1.0f + weight), 1.0f / (1.0f + weight))
        val newWeight = sqrt(0.5f + weight * 0.5f)

        val p0 = points[0]
        val p1 = points[1]
        val p2 = points[2]
        val ww = Point(weight, weight)
        val weightedP1 = ww * p1
        var midpoint =
            (p0 + weightedP1 + weightedP1 + p2) * scale * Point(0.5f, 0.5f)

        if (!midpoint.isFinite()) {
            val weightDouble = weight.toDouble()
            val weightTimesTwo = weightDouble * 2.0
            val halfScale = 1.0 / (1.0 + weightDouble) * 0.5
            midpoint =
                Point(
                    ((p0.x.toDouble() + weightTimesTwo * p1.x.toDouble() + p2.x.toDouble()) *
                            halfScale)
                        .toFloat(),
                    ((p0.y.toDouble() + weightTimesTwo * p1.y.toDouble() + p2.y.toDouble()) *
                            halfScale)
                        .toFloat(),
                )
        }

        return SplitResult(
            Conic(arrayOf(p0, (p0 + weightedP1) * scale, midpoint), newWeight),
            Conic(arrayOf(midpoint, (weightedP1 + p2) * scale, p2), newWeight),
        )
    }

    private fun commonFinitePointCheck(dstPoints: ArrayList<Float>, count: Int): Int {
        val quadraticCount = 1 shl count
        if (dstPoints.any { !it.isFinite() }) {
            val pointCount = dstPoints.size / 2
            val p1 = points[1]
            for (i in 1 until pointCount) {
                val index = i * 2
                dstPoints[index] = p1.x
                dstPoints[index + 1] = p1.y
            }
        }
        return quadraticCount
    }

    fun splitIntoQuadratics(dstPoints: ArrayList<Float>, count: Int): Int {
        dstPoints.add(points[0])
        subdivide(this, dstPoints, count)
        return commonFinitePointCheck(dstPoints, count)
    }
}
