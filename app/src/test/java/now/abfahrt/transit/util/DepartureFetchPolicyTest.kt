package now.abfahrt.transit.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DepartureFetchPolicyTest {
    @Test
    fun fetchWindowIsWiderThanVisibleThirtyMinuteWindow() {
        assertEquals(120, DepartureFetchPolicy.requestToMinutesForDisplayWindow(30))
    }

    @Test
    fun keepsLargerUserWindowWhenUserAsksForMoreThanLookahead() {
        assertEquals(180, DepartureFetchPolicy.requestToMinutesForDisplayWindow(180))
    }
}
