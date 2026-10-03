package now.abfahrt.transit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import now.abfahrt.transit.data.model.TransportMode

/**
 * Four equal-width chips using the user's configured quick-filter slots.
 * Emoji stacked above label — same visual language as the settings chips.
 */
@Composable
fun TransportModeChips(
    selectedModes: Set<TransportMode>,
    quickSlots:    List<TransportMode>,
    onToggle:      (TransportMode) -> Unit,
    modifier:      Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        quickSlots.forEach { mode ->
            val selected = mode in selectedModes
            FilterChip(
                selected = selected,
                onClick  = { onToggle(mode) },
                modifier = Modifier.weight(1f),
                label = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = mode.emoji, fontSize = 16.sp, textAlign = TextAlign.Center)
                        Text(
                            text       = stringResource(mode.labelRes),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign  = TextAlign.Center,
                            maxLines   = 1,
                            softWrap   = false
                        )
                    }
                },
                enabled = !selected || selectedModes.size > 1
            )
        }
    }
}
