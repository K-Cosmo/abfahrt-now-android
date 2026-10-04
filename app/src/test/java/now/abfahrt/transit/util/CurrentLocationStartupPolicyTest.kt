package now.abfahrt.transit.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrentLocationStartupPolicyTest {

    @Test
    fun `last location is eligible only as provisional origin when present`() {
        assertTrue(CurrentLocationStartupPolicy.shouldUseProvisionalOrigin(hasLastLocation = true))
        assertFalse(CurrentLocationStartupPolicy.shouldUseProvisionalOrigin(hasLastLocation = false))
    }

    @Test
    fun `fresh correction below 200m keeps provisional core context`() {
        assertEquals(
            StartupLocationCorrectionDecision.KEEP_PROVISIONAL,
            CurrentLocationStartupPolicy.correctionDecision(
                targetStillCurrent = true,
                distanceMeters = 199.9f
            )
        )
    }

    @Test
    fun `fresh correction at 200m reanchors`() {
        assertEquals(
            StartupLocationCorrectionDecision.REANCHOR,
            CurrentLocationStartupPolicy.correctionDecision(
                targetStillCurrent = true,
                distanceMeters = CurrentLocationStartupPolicy.MOVEMENT_THRESHOLD_METERS
            )
        )
    }

    @Test
    fun `fresh correction beyond 200m reanchors`() {
        assertEquals(
            StartupLocationCorrectionDecision.REANCHOR,
            CurrentLocationStartupPolicy.correctionDecision(
                targetStillCurrent = true,
                distanceMeters = 450f
            )
        )
    }

    @Test
    fun `fresh correction is ignored after target generation changed`() {
        assertEquals(
            StartupLocationCorrectionDecision.IGNORE_STALE_TARGET,
            CurrentLocationStartupPolicy.correctionDecision(
                targetStillCurrent = false,
                distanceMeters = 500f
            )
        )
    }
}
