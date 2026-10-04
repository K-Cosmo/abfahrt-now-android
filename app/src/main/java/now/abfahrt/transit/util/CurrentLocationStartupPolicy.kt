package now.abfahrt.transit.util

internal enum class StartupLocationCorrectionDecision {
    IGNORE_STALE_TARGET,
    KEEP_PROVISIONAL,
    REANCHOR
}

/**
 * Pure policy for Build 154's Current-Location startup fast path.
 *
 * The 200 m boundary is the existing location-context contract used by the app.
 * This policy deliberately introduces no separate age or accuracy threshold for
 * FusedLocationProviderClient.lastLocation.
 */
internal object CurrentLocationStartupPolicy {
    const val MOVEMENT_THRESHOLD_METERS = 200f

    fun shouldUseProvisionalOrigin(hasLastLocation: Boolean): Boolean = hasLastLocation

    fun correctionDecision(
        targetStillCurrent: Boolean,
        distanceMeters: Float
    ): StartupLocationCorrectionDecision = when {
        !targetStillCurrent -> StartupLocationCorrectionDecision.IGNORE_STALE_TARGET
        distanceMeters >= MOVEMENT_THRESHOLD_METERS -> StartupLocationCorrectionDecision.REANCHOR
        else -> StartupLocationCorrectionDecision.KEEP_PROVISIONAL
    }
}
