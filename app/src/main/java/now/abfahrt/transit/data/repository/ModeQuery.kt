package now.abfahrt.transit.data.repository

import now.abfahrt.transit.data.model.TransportMode

/**
 * Build the API `mode` query parameter from the selected transport modes.
 *
 * Rules:
 * - all modes selected: omit the parameter
 * - fewer/equal exclusions than inclusions: send exclusions, e.g. `-bus,-ferry`
 * - otherwise: send inclusions, e.g. `subway,tram`
 */
internal fun buildModeParamForRequest(selectedModes: Set<TransportMode>): String? {
    val all = TransportMode.entries.toSet()
    if (selectedModes == all) return null

    val normalized = selectedModes.intersect(all)
    if (normalized.isEmpty()) return ""

    val excluded = all - normalized
    return if (excluded.size <= normalized.size) {
        excluded.joinToString(",") { "-${it.apiValue}" }
    } else {
        normalized.joinToString(",") { it.apiValue }
    }
}
