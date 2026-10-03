package now.abfahrt.transit.util

import now.abfahrt.transit.data.model.Departure
import now.abfahrt.transit.data.model.DepartureResponse
import now.abfahrt.transit.data.model.Station

/**
 * Pure merge logic for refresh stability.
 *
 * Normal refreshes can receive transiently incomplete API/add-on responses.
 * This merger keeps the previous, still time-valid departures as a grace pool
 * when the caller explicitly requests `preservePrevious=true`, while still
 * letting incoming realtime/time/status data update matching entries.
 */
object DepartureStableMerger {
    const val MIN_MATCH_WINDOW_MINUTES: Int = 1
    const val DEFAULT_STABLE_MATCH_WINDOW_MINUTES: Int = 3
    const val MAX_MATCH_WINDOW_MINUTES: Int = 5

    private data class PreferredDistanceData(
        val stationDistance: Int,
        val walkDistance: Int?,
        val walkDurationSeconds: Int?,
        val usesApproximateDistance: Boolean
    )

    fun mergeResponse(
        previous: DepartureResponse?,
        incoming: DepartureResponse,
        matchWindowMinutes: Int,
        preservePrevious: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): DepartureResponse {
        if (previous == null) {
            return incoming.copy(
                departures = incoming.departures.sortedWith(DepartureDisplayOrdering.comparator()),
                stations = incoming.stations?.sortedBy { it.distance }
            )
        }

        val mergedStations = mergeStations(previous.stations.orEmpty(), incoming.stations.orEmpty())
        val mergedDepartures = mergeDepartures(
            previous = previous.departures,
            incoming = incoming.departures,
            matchWindowMinutes = matchWindowMinutes,
            preservePrevious = preservePrevious,
            nowMillis = nowMillis
        )

        return incoming.copy(
            departures = mergedDepartures,
            stations = mergedStations,
            effectiveRadius = maxOf(previous.effectiveRadius ?: 0, incoming.effectiveRadius ?: 0)
                .takeIf { it > 0 }
        )
    }

    fun mergeDepartures(
        previous: List<Departure>,
        incoming: List<Departure>,
        matchWindowMinutes: Int,
        preservePrevious: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): List<Departure> {
        if (previous.isEmpty()) return incoming.sortedWith(DepartureDisplayOrdering.comparator())
        if (incoming.isEmpty()) return if (preservePrevious) {
            previous.filter { it.timestamp >= nowMillis - 30_000L }
                .sortedWith(DepartureDisplayOrdering.comparator())
        } else {
            emptyList()
        }

        val matchWindowMs = matchWindowMinutes
            .coerceIn(MIN_MATCH_WINDOW_MINUTES, MAX_MATCH_WINDOW_MINUTES) * 60_000L
        val oldPool = previous
            .filter { it.timestamp >= nowMillis - 60_000L }
            .toMutableList()

        val usedOld = BooleanArray(oldPool.size)
        val merged = mutableListOf<Departure>()

        for (newDep in incoming) {
            val newKey = baseKey(newDep)
            var bestIndex = -1
            var bestDelta = Long.MAX_VALUE
            for ((idx, oldDep) in oldPool.withIndex()) {
                if (usedOld[idx]) continue
                if (baseKey(oldDep) != newKey) continue
                val delta = kotlin.math.abs(oldDep.timestamp - newDep.timestamp)
                if (delta <= matchWindowMs && delta < bestDelta) {
                    bestDelta = delta
                    bestIndex = idx
                }
            }

            if (bestIndex >= 0) {
                usedOld[bestIndex] = true
                val oldDep = oldPool[bestIndex]
                val preferredDistanceData = preferredDistanceData(oldDep, newDep)
                merged += oldDep.copy(
                    time = newDep.time,
                    timestamp = newDep.timestamp,
                    delay = newDep.delay,
                    cancelled = newDep.cancelled,
                    occupancy = newDep.occupancy,
                    platform = newDep.platform ?: oldDep.platform,
                    stationDistance = preferredDistanceData.stationDistance,
                    walkDistance = preferredDistanceData.walkDistance,
                    walkDurationSeconds = preferredDistanceData.walkDurationSeconds,
                    usesApproximateDistance = preferredDistanceData.usesApproximateDistance,
                    providerStopId = newDep.providerStopId ?: oldDep.providerStopId
                )
            } else {
                merged += newDep
            }
        }

        if (preservePrevious) {
            oldPool.forEachIndexed { idx, oldDep ->
                if (!usedOld[idx] && oldDep.timestamp >= nowMillis - 30_000L) {
                    merged += oldDep
                }
            }
        }

        return merged
            .distinctBy {
                listOf(
                    it.providerStopId ?: it.stop,
                    DepartureServiceIdentity.lineDirectionKey(it),
                    it.platform.orEmpty(),
                    it.mode.orEmpty(),
                    it.timestamp / 60_000L
                ).joinToString("|")
            }
            .sortedWith(DepartureDisplayOrdering.comparator())
    }

    private fun mergeStations(previous: List<Station>, incoming: List<Station>): List<Station> =
        (previous + incoming)
            .distinctBy { it.id.ifBlank { it.name } }
            .sortedBy { it.distance }

    private fun baseKey(dep: Departure): String =
        listOf(
            dep.providerStopId ?: dep.stop,
            DepartureServiceIdentity.lineDirectionKey(dep),
            dep.platform.orEmpty(),
            dep.mode.orEmpty()
        ).joinToString("|")

    private fun normalizedDistance(distance: Int?): Int? =
        DepartureDisplayOrdering.normalizedDistance(distance)

    private fun preferredDistanceData(oldDep: Departure, newDep: Departure): PreferredDistanceData {
        val oldWalk = normalizedDistance(oldDep.walkDistance)
        val newWalk = normalizedDistance(newDep.walkDistance)
        val oldStation = normalizedDistance(oldDep.stationDistance)
        val newStation = normalizedDistance(newDep.stationDistance)

        val chosenWalkSource = when {
            oldWalk == null && newWalk == null -> null
            oldWalk != null && newWalk == null -> "old"
            oldWalk == null && newWalk != null -> "incoming"
            oldWalk != null && newWalk != null && !oldDep.usesApproximateDistance && newDep.usesApproximateDistance -> "old"
            oldWalk != null && newWalk != null && oldDep.usesApproximateDistance && !newDep.usesApproximateDistance -> "incoming"
            oldWalk != null && newWalk != null && oldWalk <= newWalk -> "old"
            else -> "incoming"
        }

        val stationDistance = when {
            oldStation == null && newStation == null -> 0
            oldStation != null && newStation == null -> oldStation
            oldStation == null && newStation != null -> newStation
            else -> minOf(oldStation!!, newStation!!)
        }

        return when (chosenWalkSource) {
            "old" -> PreferredDistanceData(
                stationDistance = stationDistance,
                walkDistance = oldWalk,
                walkDurationSeconds = oldDep.walkDurationSeconds,
                usesApproximateDistance = oldDep.usesApproximateDistance
            )
            "incoming" -> PreferredDistanceData(
                stationDistance = stationDistance,
                walkDistance = newWalk,
                walkDurationSeconds = newDep.walkDurationSeconds,
                usesApproximateDistance = newDep.usesApproximateDistance
            )
            else -> PreferredDistanceData(
                stationDistance = stationDistance,
                walkDistance = null,
                walkDurationSeconds = null,
                usesApproximateDistance = false
            )
        }
    }
}
