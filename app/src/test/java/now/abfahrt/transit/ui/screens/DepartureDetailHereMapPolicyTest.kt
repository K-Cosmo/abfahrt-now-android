package now.abfahrt.transit.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DepartureDetailHereMapPolicyTest {

    @Test
    fun hereNeverLoadsOrsRoutePreview() {
        assertFalse(
            shouldLoadRoutePreview(
                isHere = true,
                orsApiKeyConfigured = true,
                destinationAvailable = true
            )
        )
    }

    @Test
    fun nonHereRequiresKeyAndDestination() {
        assertFalse(shouldLoadRoutePreview(isHere = false, orsApiKeyConfigured = false, destinationAvailable = true))
        assertFalse(shouldLoadRoutePreview(isHere = false, orsApiKeyConfigured = true, destinationAvailable = false))
        assertTrue(shouldLoadRoutePreview(isHere = false, orsApiKeyConfigured = true, destinationAvailable = true))
    }
}
