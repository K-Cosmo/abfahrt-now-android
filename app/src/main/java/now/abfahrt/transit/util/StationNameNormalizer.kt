package now.abfahrt.transit.util

/**
 * Shared provider-name cleanup for stops, route-preview lookup and target filtering.
 *
 * Provider APIs use country- and city-specific variants such as:
 * - "Berlin, Grindelwaldweg"
 * - "Grindelwaldweg (Berlin)"
 * - "U Franz-Neumann-Platz (Am Schäfersee) (Berlin)"
 * - "Düsseldorf, Heinrich-Heine-Allee"
 * - "Paris, Châtelet - Les Halles"
 *
 * The raw API values remain unchanged in the data model. This utility only creates
 * derived display/lookup forms. Direction strings must use directionDisplayName()
 * or filterLookupName(); never apply stopDisplayName() to directions.
 */
object StationNameNormalizer {
    private val squareQualifierRegex = Regex("""\s*\[[^\]]*]\s*""")
    private val trailingSquareQualifierRegex = Regex("""\s*\[[^\]]*]\s*$""")
    private val roundQualifierRegex = Regex("""\s*\([^()]*\)\s*""")
    private val trailingRoundQualifierRegex = Regex("""\s*\([^()]*\)\s*$""")
    private val whitespaceRegex = Regex("""\s+""")
    private val nonWordRegex = Regex("""[^\p{L}\p{N}]+""")
    private val modalPrefixRegex = Regex("""\b(u|s|ubhf|sbhf)\b""")
    private val leadingProviderPlacePrefixRegex = Regex("""^([^,]{2,64}),\s+(.+)$""")

    private val prefixRejectTokens = setOf(
        "airport", "aeroport", "aéroport", "aeropuerto",
        "bahnhof", "hbf", "flughafen", "gare", "estacion", "estación", "estacio", "estació", "stazione", "station",
        "metro", "métro", "tram", "bus", "terminal", "port", "porto", "puerto"
    )

    fun stopDisplayName(name: String, city: String? = null): String {
        var result = stripTrailingProviderContext(name)
        result = stripLeadingProviderPlacePrefix(result, preferredLocality = city)
        if ((result.startsWith("U ") || result.startsWith("S+U ")) && result.contains("/")) {
            result = result.substringBefore("/").trim()
        }
        return result.ifBlank { name.trim() }
    }

    fun directionDisplayName(name: String): String =
        stripTrailingProviderContext(name).ifBlank { name.trim() }

    fun canonicalStopName(name: String, city: String? = null): String {
        var result = stripAllProviderQualifiers(name)
        result = stripLeadingProviderPlacePrefix(result, preferredLocality = city)
        if (result.startsWith("U ") && result.contains("/")) {
            result = result.substringBefore("/").trim()
        }
        return result.ifBlank { name.trim() }
    }

    fun routeLookupName(value: String): String =
        technicalLookupName(canonicalStopName(value, city = null), includeVehicleWords = false)

    fun filterLookupName(value: String): String =
        technicalLookupName(canonicalStopName(value, city = null), includeVehicleWords = true)

    private fun technicalLookupName(value: String, includeVehicleWords: Boolean): String {
        var result = value
            .lowercase()
            .replace("s+u", " ")
            .replace("s + u", " ")
            .replace("s/u", " ")
            .replace("u-bhf", " ")
            .replace("s-bhf", " ")
            .replace("u bahnhof", " ")
            .replace("s bahnhof", " ")
            .replace("bahnhof", " ")
            .replace("bhf", " ")

        if (includeVehicleWords) {
            result = result
                .replace("tram", " ")
                .replace("bus", " ")
        }

        return result
            .replace(nonWordRegex, " ")
            .replace(modalPrefixRegex, " ")
            .replace(whitespaceRegex, " ")
            .trim()
    }

    private fun stripTrailingProviderContext(name: String): String {
        var result = name.trim()
            .substringBefore(" [")
            .replace(trailingSquareQualifierRegex, " ")
            .trim()
        result = result.replace(trailingRoundQualifierRegex, " ").trim()
        return normalizeWhitespace(result)
    }

    private fun stripAllProviderQualifiers(name: String): String =
        normalizeWhitespace(
            name.trim()
                .replace(squareQualifierRegex, " ")
                .replace(roundQualifierRegex, " ")
        )

    private fun stripLeadingProviderPlacePrefix(name: String, preferredLocality: String? = null): String {
        val trimmed = normalizeWhitespace(name)
        val match = leadingProviderPlacePrefixRegex.matchEntire(trimmed) ?: return trimmed
        val prefix = match.groupValues[1].trim()
        val rest = match.groupValues[2].trim()
        if (prefix.isBlank() || rest.isBlank()) return trimmed

        val preferredMatches = preferredLocality
            ?.takeIf { it.isNotBlank() }
            ?.let { normalizeForPrefixComparison(it) == normalizeForPrefixComparison(prefix) }
            ?: false
        if (preferredMatches) return rest

        // Keep abbreviations/district-style prefixes such as "Franz. Buchholz, Guyotstr.".
        if (prefix.contains('.')) return trimmed

        val prefixLookup = normalizeForPrefixComparison(prefix)
        val words = prefixLookup.split(' ').filter { it.isNotBlank() }
        val hasRejectedToken = words.any { it in prefixRejectTokens }
        val looksLikeLocality = words.isNotEmpty() && words.size <= 4 && !hasRejectedToken

        return if (looksLikeLocality) rest else trimmed
    }

    private fun normalizeForPrefixComparison(value: String): String =
        value
            .lowercase()
            .replace(roundQualifierRegex, " ")
            .replace(nonWordRegex, " ")
            .replace(whitespaceRegex, " ")
            .trim()

    private fun normalizeWhitespace(value: String): String =
        value.replace(whitespaceRegex, " ").trim()
}
