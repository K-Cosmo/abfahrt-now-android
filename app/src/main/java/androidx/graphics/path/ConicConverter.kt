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
 * AbfahrtApp Build 130 adaptation: always use the AndroidX pure-Kotlin conversion path and remove
 * the JNI call. This avoids loading libandroidx.graphics.path.so on API 34+.
 */
package androidx.graphics.path

internal class ConicConverter {
    var quadraticCount: Int = 0
        private set

    var currentQuadratic: Int = 0
    private var quadraticData = FloatArray(32 * 2 * 2 + 2)

    fun nextQuadratic(points: FloatArray, offset: Int = 0): Boolean {
        if (currentQuadratic < quadraticCount) {
            val index = currentQuadratic * 2 * 2
            points[0 + offset] = quadraticData[index]
            points[1 + offset] = quadraticData[index + 1]
            points[2 + offset] = quadraticData[index + 2]
            points[3 + offset] = quadraticData[index + 3]
            points[4 + offset] = quadraticData[index + 4]
            points[5 + offset] = quadraticData[index + 5]
            currentQuadratic++
            return true
        }
        return false
    }

    fun convert(points: FloatArray, weight: Float, tolerance: Float, offset: Int = 0) {
        quadraticData = conicToQuadratics(points, offset, weight, tolerance)
        quadraticCount = (quadraticData.size - 2) / 4
        currentQuadratic = 0
    }
}
