package now.abfahrt.transit.util

/**
 * Separates the data catchment window from the visible user window.
 *
 * The overview still filters to the user-selected window, but the raw response is
 * intentionally fetched farther into the future so the detail sheet can show the
 * next three departures without opening another API request.
 */
object DepartureFetchPolicy {
    const val FOLLOW_UP_LOOKAHEAD_MINUTES: Int = 120

    fun requestToMinutesForDisplayWindow(displayToMinutes: Int?): Int? {
        val display = displayToMinutes ?: return FOLLOW_UP_LOOKAHEAD_MINUTES
        return maxOf(display, FOLLOW_UP_LOOKAHEAD_MINUTES)
    }
}
