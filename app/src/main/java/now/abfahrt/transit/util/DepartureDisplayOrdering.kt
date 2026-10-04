package now.abfahrt.transit.util

import now.abfahrt.transit.data.model.Departure
import now.abfahrt.transit.data.model.DepartureSortProfile
import now.abfahrt.transit.data.model.displayDistanceMeters

/**
 * Single source of truth for displayed departure ordering and effective distance.
 *
 * Profiles:
 * - NEARBY: effective distance -> departure time -> direction -> line
 * - SOONEST: departure time -> effective distance -> direction -> line
 * - LINE_GROUPED: effective distance -> line -> departure time -> direction
 *
 * HERE departures already have effective distance 0 via displayDistanceMeters(). There is no
 * hidden global HERE priority ahead of SOONEST, because that would violate a time-first profile.
 */
object DepartureDisplayOrdering {
    fun comparator(profile: DepartureSortProfile = DepartureSortProfile.NEARBY): Comparator<Departure> =
        when (profile) {
            DepartureSortProfile.NEARBY -> compareBy<Departure>(
                { effectiveDistanceForSort(it) },
                { it.timestamp },
                { normalizedDirection(it) },
                { normalizedLine(it) },
                { normalizedStop(it) },
                { normalizedPlatform(it) },
                { normalizedMode(it) }
            )

            DepartureSortProfile.SOONEST -> compareBy<Departure>(
                { it.timestamp },
                { effectiveDistanceForSort(it) },
                { normalizedDirection(it) },
                { normalizedLine(it) },
                { normalizedStop(it) },
                { normalizedPlatform(it) },
                { normalizedMode(it) }
            )

            DepartureSortProfile.LINE_GROUPED -> compareBy<Departure>(
                { effectiveDistanceForSort(it) },
                { normalizedLine(it) },
                { it.timestamp },
                { normalizedDirection(it) },
                { normalizedStop(it) },
                { normalizedPlatform(it) },
                { normalizedMode(it) }
            )
        }

    fun effectiveDistanceForSort(departure: Departure): Int =
        effectiveDistanceOrNull(departure) ?: Int.MAX_VALUE

    fun effectiveDistanceOrNull(departure: Departure): Int? =
        departure.displayDistanceMeters()

    fun normalizedDistance(distance: Int?): Int? =
        distance?.takeIf { it > 0 }

    private fun normalizedDirection(departure: Departure): String =
        departure.direction.trim().lowercase()

    private fun normalizedLine(departure: Departure): String =
        departure.line.trim().lowercase()

    private fun normalizedStop(departure: Departure): String =
        departure.stop.trim().lowercase()

    private fun normalizedPlatform(departure: Departure): String =
        departure.platform.orEmpty().trim().lowercase()

    private fun normalizedMode(departure: Departure): String =
        departure.mode.orEmpty().trim().lowercase()
}
