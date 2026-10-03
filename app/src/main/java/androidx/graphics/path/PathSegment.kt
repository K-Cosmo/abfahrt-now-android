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
 */
@file:JvmName("PathSegmentUtilities")

package androidx.graphics.path

import android.graphics.PointF

public class PathSegment
internal constructor(
    public val type: Type,
    @get:Suppress("ArrayReturn") public val points: Array<PointF>,
    public val weight: Float,
) {
    public enum class Type {
        Move,
        Line,
        Quadratic,
        Conic,
        Cubic,
        Close,
        Done,
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathSegment
        if (type != other.type) return false
        if (!points.contentEquals(other.points)) return false
        if (weight != other.weight) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + points.contentHashCode()
        result = 31 * result + weight.hashCode()
        return result
    }

    override fun toString(): String =
        "PathSegment(type=$type, points=${points.contentToString()}, weight=$weight)"
}

public val DoneSegment: PathSegment = PathSegment(PathSegment.Type.Done, emptyArray(), 0.0f)
public val CloseSegment: PathSegment = PathSegment(PathSegment.Type.Close, emptyArray(), 0.0f)
