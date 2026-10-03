package now.abfahrt.transit.util

import kotlin.math.abs
import now.abfahrt.transit.data.model.Departure

/**
 * Builds the compact list of follow-up departures shown in the detail sheet.
 *
 * The overview intentionally limits the list to one departure per line/direction.
 * The already-loaded raw/merged response is fetched with a wider data lookahead than
 * the visible overview window, so the detail sheet can show later departures without
 * another API call.
 */
object DepartureFollowUpTimes {
    const val DEFAULT_LIMIT: Int = 3
    private const val SAME_MINUTE_TOLERANCE_MS: Long = 30_000L
    private const val SAME_STOP_DISTANCE_TOLERANCE_METERS: Int = 80

    fun forSelectedDeparture(
        selected: Departure,
        allDepartures: List<Departure>,
        limit: Int = DEFAULT_LIMIT
    ): List<Departure> {
        if (limit <= 0) return emptyList()

        val candidates = (allDepartures + selected)
            .asSequence()
            .filter { isSameLocalFollowUpCandidate(selected, it) }
            .filter { it.timestamp >= selected.timestamp - SAME_MINUTE_TOLERANCE_MS }
            .filter { !it.cancelled || it.timestamp == selected.timestamp }
            .sortedBy { it.timestamp }
            .distinctBy { it.timestamp / 60_000L }
            .toList()

        return candidates
            .take(limit)
            .ifEmpty { listOf(selected) }
    }

    /**
     * Strict service-pattern match retained for tests and diagnostics.
     *
     * It requires the normalized stop identity and, if both sides declare a platform,
     * the same platform. The detail-sheet follow-up list uses the more tolerant
     * isSameLocalFollowUpCandidate() so provider platform/stop variants do not hide
     * local follow-up times that are already available in the response.
     */
    fun isSameServicePattern(selected: Departure, candidate: Departure): Boolean {
        if (!isSameLineDirectionAndMode(selected, candidate)) return false
        if (!normalStop(selected.stop).equals(normalStop(candidate.stop), ignoreCase = true)) return false

        val selectedPlatform = selected.platform.normalPlatform()
        val candidatePlatform = candidate.platform.normalPlatform()
        if (selectedPlatform.isNotBlank() && candidatePlatform.isNotBlank() && selectedPlatform != candidatePlatform) {
            return false
        }

        return true
    }

    fun isSameLocalFollowUpCandidate(selected: Departure, candidate: Departure): Boolean {
        if (!isSameLineDirectionAndMode(selected, candidate)) return false
        if (normalStop(selected.stop).equals(normalStop(candidate.stop), ignoreCase = true)) return true

        val selectedDistance = DepartureDisplayOrdering.effectiveDistanceOrNull(selected)
        val candidateDistance = DepartureDisplayOrdering.effectiveDistanceOrNull(candidate)
        if (selectedDistance == null || candidateDistance == null) return false

        return abs(selectedDistance - candidateDistance) <= SAME_STOP_DISTANCE_TOLERANCE_METERS
    }

    fun candidateStats(selected: Departure, allDepartures: List<Departure>): CandidateStats {
        val candidates = allDepartures + selected
        val sameLineDirection = candidates.count { sameLineAndDirection(selected, it) }
        val strictSameService = candidates.count { isSameServicePattern(selected, it) }
        val localFollowUps = candidates.count { isSameLocalFollowUpCandidate(selected, it) }
        val output = forSelectedDeparture(selected, allDepartures)
        return CandidateStats(
            sameLineDirection = sameLineDirection,
            strictSameService = strictSameService,
            localFollowUps = localFollowUps,
            output = output.size,
            outputTimes = output.joinToString("|") { formatCompactValue(it) }
        )
    }

    data class CandidateStats(
        val sameLineDirection: Int,
        val strictSameService: Int,
        val localFollowUps: Int,
        val output: Int,
        val outputTimes: String
    )

    fun formatCompactValue(departure: Departure): String =
        departure.time.trim().ifBlank { "–" }

    private fun isSameLineDirectionAndMode(selected: Departure, candidate: Departure): Boolean {
        if (!sameLineAndDirection(selected, candidate)) return false

        val selectedMode = selected.mode.normalMode()
        val candidateMode = candidate.mode.normalMode()
        if (selectedMode.isNotBlank() && candidateMode.isNotBlank() && selectedMode != candidateMode) {
            return false
        }

        return true
    }

    private fun sameLineAndDirection(selected: Departure, candidate: Departure): Boolean =
        DepartureServiceIdentity.lineDirectionKey(selected) ==
            DepartureServiceIdentity.lineDirectionKey(candidate)

    private fun normalStop(value: String): String =
        StationNameNormalizer.routeLookupName(value)

    private fun String?.normalPlatform(): String =
        this.orEmpty()
            .substringBefore(" (")
            .trim()
            .lowercase()

    private fun String?.normalMode(): String =
        this.orEmpty().trim().lowercase()
}
