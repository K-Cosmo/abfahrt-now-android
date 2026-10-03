package now.abfahrt.transit.ui.screens

/**
 * Product access invariant from Build 136 onward: abfahrt.now is authenticated.
 * Onboarding is complete only if the user completed it and a non-blank API key
 * is currently available from the encrypted preference store.
 */
internal fun hasRequiredAbfahrtAccess(onboardingCompleted: Boolean, apiKey: String): Boolean =
    onboardingCompleted && apiKey.isNotBlank()
