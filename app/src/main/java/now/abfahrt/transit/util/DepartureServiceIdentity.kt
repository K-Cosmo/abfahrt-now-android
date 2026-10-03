package now.abfahrt.transit.util

import now.abfahrt.transit.data.model.Departure

/**
 * Canonical identity for a public transport service direction.
 *
 * Provider context suffixes such as "(Berlin)" are display metadata, not a
 * separate travel direction. The overview, follow-up times and stable merge
 * must therefore use one shared line+direction identity.
 */
object DepartureServiceIdentity {
    private val whitespaceRegex = Regex("""\s+""")

    fun lineDirectionKey(departure: Departure): String =
        lineDirectionKey(departure.line, departure.direction)

    fun lineDirectionKey(line: String, direction: String): String =
        "${normalizeLine(line)}|${normalizeDirection(direction)}"

    fun normalizeDirection(direction: String): String =
        StationNameNormalizer.directionDisplayName(direction)
            .lowercase()
            .replace(whitespaceRegex, " ")
            .trim()

    private fun normalizeLine(line: String): String =
        line.trim().lowercase()
}
