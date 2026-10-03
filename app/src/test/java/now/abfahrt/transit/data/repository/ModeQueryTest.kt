package now.abfahrt.transit.data.repository

import now.abfahrt.transit.data.model.TransportMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModeQueryTest {

    @Test
    fun omitsModeParameterWhenAllModesAreSelected() {
        assertNull(buildModeParamForRequest(TransportMode.all))
    }

    @Test
    fun usesExclusionsWhenFewerModesAreDisabled() {
        val selected = TransportMode.all - setOf(TransportMode.BUS, TransportMode.FERRY)
        assertEquals("-bus,-ferry", buildModeParamForRequest(selected))
    }

    @Test
    fun usesInclusionsWhenFewerModesAreEnabled() {
        val selected = setOf(TransportMode.SUBWAY, TransportMode.TRAM)
        assertEquals("subway,tram", buildModeParamForRequest(selected))
    }
}
