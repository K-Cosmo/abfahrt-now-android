package now.abfahrt.transit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import now.abfahrt.transit.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import now.abfahrt.transit.data.model.Departure
import now.abfahrt.transit.data.model.TransportMode
import now.abfahrt.transit.data.model.displayDistanceMeters
import now.abfahrt.transit.data.model.isHereOverride
import now.abfahrt.transit.ui.theme.CancelledGrey
import now.abfahrt.transit.ui.theme.DelayRed
import now.abfahrt.transit.ui.theme.OnTimeGreen
import now.abfahrt.transit.util.StationNameNormalizer

private data class LineBadgeStyle(val background: Color, val text: Color)


@Composable
fun DepartureCard(
    departure: Departure,
    modifier: Modifier = Modifier,
    trailingColumnWidth: androidx.compose.ui.unit.Dp? = null,
    onClick: () -> Unit = {}
) {
    val isCancelled = departure.cancelled
    val hasDelay    = departure.delay > 0
    val isEarly     = departure.delay < 0
    val cardAlpha   = if (isCancelled) 0.55f else 1f

    val mode = TransportMode.fromApiValue(departure.mode)
        ?: inferModeFromLine(departure.line)
    val isHere = departure.isHereOverride()
    val displayedDistance = departure.displayDistanceMeters()

    val badgeColor = lineBadgeColor(departure.line, mode)

    val timeChangeSignature = "${departure.time}|${departure.delay}|${departure.cancelled}|${departure.timestamp}"
    var previousTimeChangeSignature by remember { mutableStateOf<String?>(null) }
    val highlightBaseColor = when {
        isCancelled -> CancelledGrey
        hasDelay -> DelayRed
        isEarly -> OnTimeGreen
        else -> MaterialTheme.colorScheme.primary
    }
    val highlightMaxAlpha = when {
        isCancelled -> 0.22f
        hasDelay -> 0.38f
        isEarly -> 0.38f
        else -> 0.24f
    }
    val highlightProgress = remember { Animatable(0f) }

    LaunchedEffect(timeChangeSignature) {
        val previous = previousTimeChangeSignature
        previousTimeChangeSignature = timeChangeSignature
        if (previous != null && previous != timeChangeSignature) {
            highlightProgress.stop()
            highlightProgress.snapTo(1f)
            kotlinx.coroutines.delay(300)
            highlightProgress.animateTo(0f, animationSpec = tween(durationMillis = 1000))
        } else if (previous == null) {
            highlightProgress.snapTo(0f)
        }
    }

    val animatedTimeBackground = highlightBaseColor.copy(alpha = highlightMaxAlpha * highlightProgress.value)

    ElevatedCard(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth().alpha(cardAlpha),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Mode emoji + line badge ───────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)   // fixed column width → no shifting
            ) {
                Text(text = mode?.emoji ?: "🚍", fontSize = 18.sp)
                Spacer(Modifier.height(3.dp))
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .widthIn(min = 40.dp)  // uniform minimum badge width
                        .wrapContentWidth()
                ) {
                    Text(
                        text      = departure.line,
                        modifier  = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style     = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color     = badgeTextColor(departure.line, mode, badgeColor),
                        maxLines  = 1,
                        fontSize  = 11.sp
                    )
                }
            }

            Spacer(Modifier.width(5.dp))

            // ── Direction + stop ──────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDirectionNameForDisplay(departure.direction),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isCancelled) TextDecoration.LineThrough else null
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = formatStopNameForDisplay(departure.stop),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    if (displayedDistance != null && displayedDistance < Int.MAX_VALUE) {
                        Spacer(Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isHere || (departure.walkDistance != null && !departure.usesApproximateDistance)) {
                                    Icons.Filled.NearMe
                                } else {
                                    Icons.Filled.Flight
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = formatDistance(displayedDistance, isHere),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(2.dp))

            // ── Time + delay ──────────────────────────────────────────────────
            Surface(
                color = animatedTimeBackground,
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = (trailingColumnWidth?.let { Modifier.width(it) } ?: Modifier.widthIn(min = 92.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    when {
                        isCancelled -> Text(
                            stringResource(R.string.cancelled),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CancelledGrey,
                            maxLines = 1
                        )
                        else -> Text(
                            departure.time,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                hasDelay -> DelayRed
                                isEarly  -> OnTimeGreen
                                else     -> MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        when {
                            isCancelled -> {}
                            hasDelay    -> DelayBadge(stringResource(R.string.delay_plus, departure.delay), DelayRed)
                            isEarly     -> DelayBadge(stringResource(R.string.delay_minus, departure.delay), OnTimeGreen)
                            else        -> Text(
                                stringResource(R.string.on_time),
                                style = MaterialTheme.typography.labelSmall,
                                color = OnTimeGreen,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Delay badge ───────────────────────────────────────────────────────────────

@Composable
private fun DelayBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun formatDistance(metres: Int, isHere: Boolean): String = when {
    isHere -> stringResource(R.string.distance_here)
    metres < 1000 -> stringResource(R.string.distance_m, metres)
    else          -> stringResource(R.string.distance_km, metres / 1000.0)
}

fun badgeTextColor(line: String, mode: TransportMode?, bg: Color): Color =
    lineBadgeStyle(line, mode)?.text ?: run {
        val luminance = 0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue
        if (luminance > 0.55f) Color(0xFF1A1A1A) else Color.White
    }

private fun lineBadgeStyle(line: String, mode: TransportMode?): LineBadgeStyle? {
    val l = line.trim().uppercase()

    val vbbExplicit = mapOf(
        "FEX" to LineBadgeStyle(Color(0xFF79122F), Color(255, 255, 255)),
        "RB10" to LineBadgeStyle(Color(0xFF66AA22), Color(255, 255, 255)),
        "RB12" to LineBadgeStyle(Color(0xFFA5027D), Color(255, 255, 255)),
        "RB14" to LineBadgeStyle(Color(0xFFA5027D), Color(255, 255, 255)),
        "RB20" to LineBadgeStyle(Color(0xFF007734), Color(255, 255, 255)),
        "RB21" to LineBadgeStyle(Color(0xFF501689), Color(255, 255, 255)),
        "RB22" to LineBadgeStyle(Color(0xFF009BD5), Color(255, 255, 255)),
        "RB23" to LineBadgeStyle(Color(0xFFEB7405), Color(255, 255, 255)),
        "RB24" to LineBadgeStyle(Color(0xFFDA6BA2), Color(255, 255, 255)),
        "RB25" to LineBadgeStyle(Color(0xFF007CB0), Color(255, 255, 255)),
        "RB26" to LineBadgeStyle(Color(0xFF009686), Color(255, 255, 255)),
        "RB27" to LineBadgeStyle(Color(0xFFE2001A), Color(255, 255, 255)),
        "RB31" to LineBadgeStyle(Color(0xFF66AA22), Color(255, 255, 255)),
        "RB32" to LineBadgeStyle(Color(0xFF697C8A), Color(255, 255, 255)),
        "RB33" to LineBadgeStyle(Color(0xFFA5027D), Color(255, 255, 255)),
        "RB34" to LineBadgeStyle(Color(0xFF0066AD), Color(255, 255, 255)),
        "RB35" to LineBadgeStyle(Color(0xFF816DA6), Color(255, 255, 255)),
        "RB36" to LineBadgeStyle(Color(0xFFAD5937), Color(255, 255, 255)),
        "RB37" to LineBadgeStyle(Color(0xFFAD5937), Color(255, 255, 255)),
        "RB43" to LineBadgeStyle(Color(0xFF009BD5), Color(255, 255, 255)),
        "RB45" to LineBadgeStyle(Color(0xFFFFD502), Color(0, 0, 0)),
        "RB46" to LineBadgeStyle(Color(0xFFDA6BA2), Color(255, 255, 255)),
        "RB49" to LineBadgeStyle(Color(0xFF992746), Color(255, 255, 255)),
        "RB51" to LineBadgeStyle(Color(0xFFDA6BA2), Color(255, 255, 255)),
        "RB54" to LineBadgeStyle(Color(0xFF816DA6), Color(255, 255, 255)),
        "RB55" to LineBadgeStyle(Color(0xFFEB7405), Color(255, 255, 255)),
        "RB60" to LineBadgeStyle(Color(0xFF66AA22), Color(255, 255, 255)),
        "RB61" to LineBadgeStyle(Color(0xFF992746), Color(255, 255, 255)),
        "RB62" to LineBadgeStyle(Color(0xFFDA6BA2), Color(255, 255, 255)),
        "RB63" to LineBadgeStyle(Color(0xFFFFD502), Color(0, 0, 0)),
        "RB65" to LineBadgeStyle(Color(0xFF0066AD), Color(255, 255, 255)),
        "RB66" to LineBadgeStyle(Color(0xFF007734), Color(255, 255, 255)),
        "RB73" to LineBadgeStyle(Color(0xFF009686), Color(255, 255, 255)),
        "RB74" to LineBadgeStyle(Color(0xFF0066AD), Color(255, 255, 255)),
        "RB91" to LineBadgeStyle(Color(0xFFEB7405), Color(255, 255, 255)),
        "RB92" to LineBadgeStyle(Color(0xFFEB7405), Color(255, 255, 255)),
        "RB93" to LineBadgeStyle(Color(0xFFEB7405), Color(255, 255, 255)),
        "RE1" to LineBadgeStyle(Color(0xFFE2001A), Color(255, 255, 255)),
        "RE10" to LineBadgeStyle(Color(0xFF5E5E5D), Color(255, 255, 255)),
        "RE14" to LineBadgeStyle(Color(0xFFA98956), Color(255, 255, 255)),
        "RE15" to LineBadgeStyle(Color(0xFFFFD502), Color(0, 0, 0)),
        "RE18" to LineBadgeStyle(Color(0xFFEB7405), Color(255, 255, 255)),
        "RE2" to LineBadgeStyle(Color(0xFFFFD502), Color(0, 0, 0)),
        "RE3" to LineBadgeStyle(Color(0xFFEB7405), Color(255, 255, 255)),
        "RE4" to LineBadgeStyle(Color(0xFF992746), Color(255, 255, 255)),
        "RE5" to LineBadgeStyle(Color(0xFF0066AD), Color(255, 255, 255)),
        "RE6" to LineBadgeStyle(Color(0xFFDA6BA2), Color(255, 255, 255)),
        "RE66" to LineBadgeStyle(Color(0xFF007734), Color(255, 255, 255)),
        "RE7" to LineBadgeStyle(Color(0xFF007734), Color(255, 255, 255)),
        "RE8" to LineBadgeStyle(Color(0xFF501689), Color(255, 255, 255)),
        "S1" to LineBadgeStyle(Color(0xFFDA6BA2), Color(255, 255, 255)),
        "S2" to LineBadgeStyle(Color(0xFF007734), Color(255, 255, 255)),
        "S25" to LineBadgeStyle(Color(0xFF007734), Color(255, 255, 255)),
        "S26" to LineBadgeStyle(Color(0xFF007734), Color(255, 255, 255)),
        "S3" to LineBadgeStyle(Color(0xFF0066AD), Color(255, 255, 255)),
        "S41" to LineBadgeStyle(Color(0xFFAD5937), Color(255, 255, 255)),
        "S42" to LineBadgeStyle(Color(0xFFCB6418), Color(255, 255, 255)),
        "S45" to LineBadgeStyle(Color(0xFFCD9C53), Color(255, 255, 255)),
        "S46" to LineBadgeStyle(Color(0xFFCD9C53), Color(255, 255, 255)),
        "S47" to LineBadgeStyle(Color(0xFFCD9C53), Color(255, 255, 255)),
        "S5" to LineBadgeStyle(Color(0xFFEB7405), Color(255, 255, 255)),
        "S7" to LineBadgeStyle(Color(0xFF816DA6), Color(255, 255, 255)),
        "S75" to LineBadgeStyle(Color(0xFF816DA6), Color(255, 255, 255)),
        "S8" to LineBadgeStyle(Color(0xFF66AA22), Color(255, 255, 255)),
        "S85" to LineBadgeStyle(Color(0xFF66AA22), Color(255, 255, 255)),
        "S9" to LineBadgeStyle(Color(0xFF992746), Color(255, 255, 255)),
        "U1" to LineBadgeStyle(Color(0xFF7DAD4C), Color(255, 255, 255)),
        "U2" to LineBadgeStyle(Color(0xFFDA421E), Color(255, 255, 255)),
        "U3" to LineBadgeStyle(Color(0xFF16683D), Color(255, 255, 255)),
        "U4" to LineBadgeStyle(Color(0xFFF0D722), Color(0, 0, 0)),
        "U5" to LineBadgeStyle(Color(0xFF7E5330), Color(255, 255, 255)),
        "U6" to LineBadgeStyle(Color(0xFF8C6DAB), Color(255, 255, 255)),
        "U7" to LineBadgeStyle(Color(0xFF009BD5), Color(255, 255, 255)),
        "U8" to LineBadgeStyle(Color(0xFF224F86), Color(255, 255, 255)),
        "U9" to LineBadgeStyle(Color(0xFFF3791D), Color(255, 255, 255)),
        "X4" to LineBadgeStyle(Color(0xFF006E6B), Color(255, 255, 255)),
        "X8" to LineBadgeStyle(Color(0xFF73AEF4), Color(0, 0, 0)),
        "X9" to LineBadgeStyle(Color(0xFF6E368C), Color(255, 255, 255)),
    )
    vbbExplicit[l]?.let { return it }

    val vbbModeFallback = when (mode) {
        TransportMode.TRAM     -> LineBadgeStyle(Color(0xFFE2001A), Color.White)
        TransportMode.BUS      -> LineBadgeStyle(Color(0xFFA5027D), Color.White)
        TransportMode.FERRY    -> LineBadgeStyle(Color(0xFF009BD5), Color.White)
        else -> null
    }
    if (vbbModeFallback != null) return vbbModeFallback

    // Do not apply city-specific colors without a trusted region/city discriminator.
    // Otherwise generic lines such as “1”–“14” in other European metros would incorrectly
    // receive Paris/RATP colors. Future source of truth: API-provided GTFS route_color/route_text_color.

    return when (mode) {
        TransportMode.SUBWAY   -> LineBadgeStyle(Color(0xFF1565C0), Color.White)
        TransportMode.SUBURBAN -> LineBadgeStyle(Color(0xFF388E3C), Color.White)
        TransportMode.TRAM     -> LineBadgeStyle(Color(0xFFB11F25), Color.White)
        TransportMode.BUS      -> LineBadgeStyle(Color(0xFF6A1B9A), Color.White)
        TransportMode.REGIONAL -> LineBadgeStyle(Color(0xFF00695C), Color.White)
        TransportMode.EXPRESS  -> LineBadgeStyle(Color(0xFF4A148C), Color.White)
        TransportMode.FERRY    -> LineBadgeStyle(Color(0xFF0277BD), Color.White)
        null                   -> LineBadgeStyle(Color(0xFF546E7A), Color.White)
    }
}

fun lineBadgeColor(line: String, mode: TransportMode?): Color =
    lineBadgeStyle(line, mode)?.background ?: Color(0xFF546E7A)

/**
 * Heuristic mode inference when the API does not provide a mode field.
 */


fun formatStopNameForDisplay(name: String, city: String? = null): String =
    StationNameNormalizer.stopDisplayName(name, city)

fun formatDirectionNameForDisplay(name: String): String =
    StationNameNormalizer.directionDisplayName(name)


fun inferModeFromLine(line: String?): TransportMode? {
    if (line.isNullOrBlank()) return null
    val l = line.trim().uppercase()
    val berlinTramLines = setOf(
        "M1", "M2", "M4", "M5", "M6", "M8", "M10", "M13", "M17",
        "12", "16", "18", "21", "27", "37", "50", "60", "61", "62", "63", "67", "68"
    )

    return when {
        Regex("^S\\d+$").containsMatchIn(l)                         -> TransportMode.SUBURBAN
        Regex("^U\\d+$").containsMatchIn(l)                         -> TransportMode.SUBWAY
        l in berlinTramLines                                        -> TransportMode.TRAM
        l == "FEX"                                                   -> TransportMode.REGIONAL
        Regex("^(RE|RB|IRE|IC|EC|CNL)\\d*$").containsMatchIn(l)    -> TransportMode.REGIONAL
        Regex("^(ICE|TGV|AVE|THALYS|EUROSTAR)").containsMatchIn(l) -> TransportMode.EXPRESS
        l.startsWith("F") && l.drop(1).all { it.isDigit() }         -> TransportMode.FERRY
        else                                                          -> TransportMode.BUS
    }
}
