package now.abfahrt.transit.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateReleasePolicyTest {

    @Test
    fun `Build 150 release is newer than Build 149 app`() {
        val update = UpdateReleasePolicy.newerThanCurrent(
            tagName = "v1.1.0-b150",
            currentVersionCode = 1490
        )

        requireNotNull(update)
        assertEquals("1.1.0", update.versionName)
        assertEquals(150, update.buildNumber)
        assertEquals(
            "https://github.com/K-Cosmo/abfahrtsradar-android/releases/tag/v1.1.0-b150",
            update.releaseUrl
        )
    }

    @Test
    fun `same or older build is not offered`() {
        assertNull(UpdateReleasePolicy.newerThanCurrent("v1.1.0-b150", 1500))
        assertNull(UpdateReleasePolicy.newerThanCurrent("v1.1.0-b149", 1500))
    }

    @Test
    fun `build number is authoritative across version names`() {
        val update = UpdateReleasePolicy.newerThanCurrent("v1.2.0-b151", 1500)
        requireNotNull(update)
        assertEquals(151, update.buildNumber)
    }

    @Test
    fun `malformed or unsafe tags are ignored`() {
        val invalid = listOf(
            "1.1.0-b151",
            "v1.1-b151",
            "v1.1.0-build151",
            "v1.1.0-b0",
            "v1.1.0-b151/../../x",
            "v1.1.0-babc"
        )

        assertTrue(invalid.all { UpdateReleasePolicy.newerThanCurrent(it, 1500) == null })
    }
}
