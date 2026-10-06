package now.abfahrt.transit.util

data class AvailableUpdate(
    val tagName: String,
    val versionName: String,
    val buildNumber: Int
) {
    val displayVersion: String
        get() = "$versionName (Build $buildNumber)"

    val releaseUrl: String
        get() = "https://github.com/K-Cosmo/abfahrtsradar-android/releases/tag/$tagName"
}

/**
 * GitHub release tags are part of the update contract: v<semver>-b<human build>.
 * Android versionCode remains buildNumber * 10 (for example Build 150 = 1500).
 */
object UpdateReleasePolicy {
    private val releaseTag = Regex("^v(\\d+\\.\\d+\\.\\d+)-b(\\d+)$")

    fun parse(tagName: String): AvailableUpdate? {
        val match = releaseTag.matchEntire(tagName.trim()) ?: return null
        val buildNumber = match.groupValues[2].toIntOrNull()?.takeIf { it > 0 } ?: return null
        return AvailableUpdate(
            tagName = match.value,
            versionName = match.groupValues[1],
            buildNumber = buildNumber
        )
    }

    fun newerThanCurrent(tagName: String, currentVersionCode: Int): AvailableUpdate? {
        val candidate = parse(tagName) ?: return null
        val candidateVersionCode = candidate.buildNumber.toLong() * 10L
        return candidate.takeIf { candidateVersionCode > currentVersionCode.toLong() }
    }
}
