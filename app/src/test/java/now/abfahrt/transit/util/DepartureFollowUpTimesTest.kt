package now.abfahrt.transit.util

import now.abfahrt.transit.data.model.Departure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DepartureFollowUpTimesTest {
    private val baseNow = 1_800_000_000_000L

    @Test
    fun returnsSelectedAndNextTwoTimesForSameStopLineAndDirection() {
        val selected = dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Louise-Schroeder-Platz (Berlin)", minutes = 5)
        val all = listOf(
            dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Louise-Schroeder-Platz (Berlin)", minutes = 25),
            dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Osram-Höfe (Berlin)", minutes = 15),
            dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Louise-Schroeder-Platz (Berlin)", minutes = 15),
            dep(line = "50", direction = "Franz. Buchholz, Guyotstr.", stop = "Louise-Schroeder-Platz (Berlin)", minutes = 12),
            selected
        )

        val result = DepartureFollowUpTimes.forSelectedDeparture(selected, all)

        assertEquals(listOf("5 min", "15 min", "25 min"), result.map { it.time })
    }

    @Test
    fun matchesGenericStopNameVariantsButKeepsDifferentStopsSeparate() {
        val selected = dep(line = "U8", direction = "Wittenau", stop = "U Franz-Neumann-Platz (Am Schäfersee) (Berlin)", minutes = 4)
        val sameStopVariant = dep(line = "U8", direction = "Wittenau", stop = "Berlin, U Franz-Neumann-Platz", minutes = 14)
        val otherStop = dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str. (Berlin)", minutes = 14)

        assertTrue(DepartureFollowUpTimes.isSameServicePattern(selected, sameStopVariant))
        assertFalse(DepartureFollowUpTimes.isSameServicePattern(selected, otherStop))
    }

    @Test
    fun respectsPlatformWhenBothDeparturesDeclareOne() {
        val selected = dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str.", minutes = 3, platform = "2")
        val samePlatform = dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str. (Berlin)", minutes = 8, platform = "2 (U8)")
        val otherPlatform = dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str. (Berlin)", minutes = 8, platform = "1")

        assertTrue(DepartureFollowUpTimes.isSameServicePattern(selected, samePlatform))
        assertFalse(DepartureFollowUpTimes.isSameServicePattern(selected, otherPlatform))
    }

    @Test
    fun limitsToThreeCompactValues() {
        val selected = dep(line = "128", direction = "U Osloer Str.", stop = "Brienzer Str.", minutes = 5)
        val all = listOf(
            selected,
            dep(line = "128", direction = "U Osloer Str.", stop = "Brienzer Str.", minutes = 15),
            dep(line = "128", direction = "U Osloer Str.", stop = "Brienzer Str.", minutes = 25),
            dep(line = "128", direction = "U Osloer Str.", stop = "Brienzer Str.", minutes = 35)
        )

        val result = DepartureFollowUpTimes.forSelectedDeparture(selected, all)

        assertEquals(listOf("5 min", "15 min", "25 min"), result.map { DepartureFollowUpTimes.formatCompactValue(it) })
    }

    @Test
    fun followUpsUseLocalStopClusterWhenProviderPlatformOrStopVariantDiffers() {
        val selected = dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Berlin, Louise-Schroeder-Platz", minutes = 5, platform = "1", walkDistance = 590)
        val all = listOf(
            selected,
            dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Louise-Schroeder-Platz (Berlin)", minutes = 15, platform = "1 (Bus)", walkDistance = 635),
            dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Louise-Schroeder-Platz (Berlin)", minutes = 25, platform = "2", walkDistance = 644),
            dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Osram-Höfe (Berlin)", minutes = 35, platform = "1", walkDistance = 802)
        )

        val result = DepartureFollowUpTimes.forSelectedDeparture(selected, all)

        assertEquals(listOf("5 min", "15 min", "25 min"), result.map { it.time })
    }

    @Test
    fun followUpsKeepClearlyFartherStopsSeparateEvenWithSameLineAndDirection() {
        val selected = dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Brienzer Str.", minutes = 5, walkDistance = 450)
        val all = listOf(
            selected,
            dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Osram-Höfe (Berlin)", minutes = 15, walkDistance = 800),
            dep(line = "50", direction = "Wedding, Virchow-Klinikum", stop = "Bristolstr. (Berlin)", minutes = 25, walkDistance = 760)
        )

        val result = DepartureFollowUpTimes.forSelectedDeparture(selected, all)

        assertEquals(listOf("5 min"), result.map { it.time })
    }

    @Test
    fun providerCitySuffixStillCountsAsSameFollowUpDirection() {
        val selected = dep(
            line = "128",
            direction = "U Osloer Str.",
            stop = "Brienzer Str. (Berlin)",
            minutes = 5
        )
        val candidate = dep(
            line = "128",
            direction = "U Osloer Str. (Berlin)",
            stop = "Brienzer Str. (Berlin)",
            minutes = 15
        )

        assertTrue(DepartureFollowUpTimes.isSameServicePattern(selected, candidate))
    }

    private fun dep(
        line: String,
        direction: String,
        stop: String,
        minutes: Int,
        platform: String? = null,
        walkDistance: Int = 650
    ): Departure = Departure(
        line = line,
        direction = direction,
        time = "$minutes min",
        timestamp = baseNow + minutes * 60_000L,
        stop = stop,
        platform = platform,
        mode = if (line.startsWith("U")) "subway" else "bus",
        stationDistance = 500,
        walkDistance = walkDistance,
        walkDurationSeconds = 420,
        usesApproximateDistance = false
    )
    @Test
    fun followUpsCanComeFromLoadedDataOutsideVisibleDisplayWindow() {
        val selected = dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str.", minutes = 5)
        val all = listOf(
            selected,
            dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str.", minutes = 15),
            dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str.", minutes = 45)
        )

        val result = DepartureFollowUpTimes.forSelectedDeparture(selected, all)

        assertEquals(listOf("5 min", "15 min", "45 min"), result.map { it.time })
    }

}
