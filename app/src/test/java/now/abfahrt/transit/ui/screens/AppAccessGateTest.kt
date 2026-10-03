package now.abfahrt.transit.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppAccessGateTest {
    @Test
    fun completedOnboardingWithKeyAllowsAccess() {
        assertTrue(hasRequiredAbfahrtAccess(onboardingCompleted = true, apiKey = "secret"))
    }

    @Test
    fun completedOnboardingWithoutKeyRequiresOnboardingAgain() {
        assertFalse(hasRequiredAbfahrtAccess(onboardingCompleted = true, apiKey = "   "))
    }

    @Test
    fun keyAloneDoesNotBypassIncompleteOnboarding() {
        assertFalse(hasRequiredAbfahrtAccess(onboardingCompleted = false, apiKey = "secret"))
    }
}
