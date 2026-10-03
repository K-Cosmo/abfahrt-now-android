package now.abfahrt.transit.util

import now.abfahrt.transit.data.model.Departure
import now.abfahrt.transit.data.model.displayDistanceMeters
import now.abfahrt.transit.data.model.isHereOverride

/**
 * Single source of truth for displayed departure ordering and effective distance.
 *
 * Product order:
 * 1. "Here" departures first (station distance <= HERE_DISTANCE_THRESHOLD_METERS)
 * 2. effective distance (walk/bike distance if available, otherwise station distance)
 * 3. line
 * 4. departure time
 */
object DepartureDisplayOrdering {
    fun comparator(): Comparator<Departure> = compareBy<Departure>(
        { if (it.isHereOverride()) 0 else 1 },
        { effectiveDistanceForSort(it) },
        { it.line.trim().lowercase() },
        { it.timestamp },
        { it.direction.trim().lowercase() },
        { it.stop.trim().lowercase() },
        { it.platform.orEmpty().trim().lowercase() },
        { it.mode.orEmpty().trim().lowercase() }
    )

    fun effectiveDistanceForSort(departure: Departure): Int =
        effectiveDistanceOrNull(departure) ?: Int.MAX_VALUE

    fun effectiveDistanceOrNull(departure: Departure): Int? =
        departure.displayDistanceMeters()

    fun normalizedDistance(distance: Int?): Int? =
        distance?.takeIf { it > 0 }
}
