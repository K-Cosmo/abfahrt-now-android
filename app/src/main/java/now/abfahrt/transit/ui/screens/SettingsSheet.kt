package now.abfahrt.transit.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import now.abfahrt.transit.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import now.abfahrt.transit.util.AppVersionInfo
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import now.abfahrt.transit.data.model.AppPreferences
import now.abfahrt.transit.data.model.TransportMode
import now.abfahrt.transit.data.model.OrsTravelMode

private enum class KeySheetType { ABFAHRT, ORS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    prefs:             AppPreferences,
    onDismiss:         () -> Unit,
    onRadiusChange:    (Int) -> Unit,
    onWindowChange:    (startMin: Int, endMin: Int) -> Unit,
    onRefreshChange:      (Int) -> Unit,
    onMaxPerDirChange:    (Int) -> Unit,
    onLanguageChange:     (now.abfahrt.transit.data.model.AppLanguage) -> Unit,
    onModeToggle:      (TransportMode) -> Unit,
    onQuickSlotsChange:(List<TransportMode>) -> Unit,
    onApiKeyChange:    (String) -> Unit,
    onOrsApiKeyChange: (String) -> Unit,
    onOrsTravelModeChange: (OrsTravelMode) -> Unit,
    onHideUnreachableChange: (Boolean) -> Unit,
    initialKeySheet: String? = null
) {
    val context      = LocalContext.current
    val keyboard     = LocalSoftwareKeyboardController.current
    var newApiKey      by remember { mutableStateOf("") }
    var keyVisible     by remember { mutableStateOf(false) }
    var keyError       by remember { mutableStateOf(false) }
    var newOrsApiKey   by remember { mutableStateOf("") }
    var orsKeyVisible  by remember { mutableStateOf(false) }
    var activeKeySheet by remember(initialKeySheet) {
        mutableStateOf(
            when (initialKeySheet) {
                "ors" -> KeySheetType.ORS
                "abfahrt" -> KeySheetType.ABFAHRT
                else -> null
            }
        )
    }

    var radius   by remember(prefs.radius)               { mutableFloatStateOf(prefs.radius.toFloat()) }
    var interval by remember(prefs.refreshIntervalMinutes){ mutableFloatStateOf(prefs.refreshIntervalMinutes.toFloat()) }

    // RangeSlider state: 0–120 min in 5-min steps
    var windowRange by remember(prefs.windowStartMinutes, prefs.windowEndMinutes) {
        mutableStateOf(prefs.windowStartMinutes.toFloat()..prefs.windowEndMinutes.toFloat())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)

            HorizontalDivider()

            // ── Radius ────────────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.section_radius)) {
                Text(
                    stringResource(R.string.radius_value, radius.roundToInt()),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                SliderRow(
                    value        = radius,
                    range        = 100f..2000f,
                    steps        = 18,
                    startLabel   = stringResource(R.string.radius_min),
                    endLabel     = stringResource(R.string.radius_max),
                    onValueChange         = { radius = ((it / 100f).roundToInt() * 100).toFloat() },
                    onValueChangeFinished = { onRadiusChange(radius.roundToInt()) }
                )
            }

            HorizontalDivider()

            // ── Time window — RangeSlider ─────────────────────────────────────
            SettingsSection(stringResource(R.string.section_window)) {
                val startMin = windowRange.start.toInt()
                val endMin   = windowRange.endInclusive.toInt()
                val orsAvailable = prefs.orsApiKey.isNotBlank()
                val hideUnreachableEnabled = prefs.hideUnreachableDepartures && orsAvailable
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            startMin == 0 -> stringResource(R.string.window_value_now, endMin)
                            else          -> stringResource(R.string.window_value, startMin, endMin)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                // Info card explaining API limitation
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = stringResource(R.string.settings_info_title),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.window_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.hide_unreachable_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (orsAvailable) stringResource(R.string.hide_unreachable_hint_enabled) else stringResource(R.string.hide_unreachable_hint_disabled),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = hideUnreachableEnabled,
                            onCheckedChange = { enabled ->
                                if (orsAvailable) {
                                    if (enabled && windowRange.start > 0f) {
                                        windowRange = 0f..windowRange.endInclusive
                                    }
                                    onHideUnreachableChange(enabled)
                                }
                            },
                            enabled = orsAvailable
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (hideUnreachableEnabled) stringResource(R.string.window_slider_hint_reachable_only) else stringResource(R.string.window_slider_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (hideUnreachableEnabled) {
                    Slider(
                        value = endMin.toFloat(),
                        onValueChange = { value ->
                            val e = ((value / 5).toInt() * 5).coerceAtLeast(5)
                            windowRange = 0f..e.toFloat()
                        },
                        onValueChangeFinished = {
                            onWindowChange(0, windowRange.endInclusive.toInt())
                        },
                        valueRange = 5f..60f,
                        steps = 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    RangeSlider(
                        value                 = windowRange,
                        onValueChange         = { range ->
                            val s = (range.start / 5).toInt() * 5
                            val e = ((range.endInclusive / 5).toInt() * 5).coerceAtLeast(s + 5)
                            windowRange = s.toFloat()..e.toFloat()
                        },
                        onValueChangeFinished = {
                            onWindowChange(
                                windowRange.start.toInt(),
                                windowRange.endInclusive.toInt()
                            )
                        },
                        valueRange = 0f..60f,
                        steps      = 0,
                        modifier   = Modifier.fillMaxWidth()
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.window_now_label), style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.outline)
                    Text(stringResource(R.string.window_max), style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.outline)
                }
            }

            HorizontalDivider()

            // ── Auto-refresh ──────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.section_refresh)) {
                Text(
                    text = if (interval.toInt() == 0) stringResource(R.string.disabled)
                           else if (interval.toInt() == 1) stringResource(R.string.refresh_minutes, interval.toInt())
                           else stringResource(R.string.refresh_minutes_plural, interval.toInt()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value                 = interval,
                    onValueChange         = { interval = it },
                    onValueChangeFinished = { onRefreshChange(interval.toInt()) },
                    valueRange            = 0f..5f,
                    steps                 = 4,
                    modifier              = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.refresh_off_label), style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.outline)
                    Text(stringResource(R.string.refresh_max), style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.outline)
                }
            }

            HorizontalDivider()

            // ── Departures per direction ──────────────────────────────────────
            SettingsSection(stringResource(R.string.section_per_direction)) {
                Text(
                    stringResource(R.string.per_direction_question),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf(1, 2, 3).forEachIndexed { idx, n ->
                        SegmentedButton(
                            selected  = prefs.maxPerDirection == n,
                            onClick   = { onMaxPerDirChange(n) },
                            shape     = SegmentedButtonDefaults.itemShape(idx, 3),
                            label     = { Text("$n") }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                if (prefs.maxPerDirection > 1) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💡", fontSize = 14.sp)
                            Text(
                                text = stringResource(R.string.per_direction_hint, prefs.maxPerDirection),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                Text(
                    stringResource(R.string.per_direction_api_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            HorizontalDivider()

            // ── Quick-filter slots ─────────────────────────────────────────────
            SettingsSection(stringResource(R.string.section_quickfilter)) {
                Text(
                    stringResource(R.string.quickfilter_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                QuickSlotPicker(
                    currentSlots   = prefs.quickFilterSlots,
                    onSlotsChanged = onQuickSlotsChange
                )
            }

            HorizontalDivider()

            // ── Transport modes ───────────────────────────────────────────────
            SettingsSection(stringResource(R.string.section_modes)) {
                Text(
                    stringResource(R.string.modes_question),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                // 4-column grid, same emoji-above-label design as main screen
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(6.dp)
                ) {
                    items(TransportMode.entries.size) { idx ->
                        val mode     = TransportMode.entries[idx]
                        val selected = mode in prefs.selectedModes
                        FilterChip(
                            selected = selected,
                            onClick  = { onModeToggle(mode) },
                            label = {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(mode.emoji, fontSize = 16.sp, textAlign = TextAlign.Center)
                                    Text(stringResource(mode.labelRes), fontSize = 11.sp,
                                         fontWeight = FontWeight.Medium,
                                         textAlign = TextAlign.Center,
                                         maxLines = 1, softWrap = false)
                                }
                            },
                            enabled = !selected || prefs.selectedModes.size > 1
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.modes_min_one_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)

                // ── API feedback ──────────────────────────────────────────────
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        val subject = Uri.encode(context.getString(R.string.feedback_subject))
                        val body    = Uri.encode(context.getString(R.string.api_feedback_greeting))
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:hi@abfahrt.now?subject=$subject&body=$body")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Mail, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.feedback_button_short))
                }
                Text(
                    stringResource(R.string.feedback_email_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            HorizontalDivider()

            // ── Language ──────────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.section_language)) {
                LanguagePicker(
                    current  = prefs.language,
                    onChange = onLanguageChange
                )
            }

            HorizontalDivider()

            // ── API Keys ─────────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.product_abfahrt_now)) {
                KeyStatusRow(
                    description = stringResource(R.string.apikey_save_hint),
                    isConfigured = prefs.apiKey.isNotBlank(),
                    onManage = {
                        newApiKey = prefs.apiKey
                        keyVisible = false
                        keyError = false
                        activeKeySheet = KeySheetType.ABFAHRT
                    }
                )
            }

            HorizontalDivider()

            SettingsSection(stringResource(R.string.product_openrouteservice)) {
                KeyStatusRow(
                    description = stringResource(R.string.ors_apikey_save_hint),
                    isConfigured = prefs.orsApiKey.isNotBlank(),
                    onManage = {
                        newOrsApiKey = prefs.orsApiKey
                        orsKeyVisible = false
                        activeKeySheet = KeySheetType.ORS
                    }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.ors_mode_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                OrsTravelModeToggle(
                    selected = prefs.orsTravelMode,
                    enabled = prefs.orsApiKey.isNotBlank(),
                    onSelect = onOrsTravelModeChange
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (prefs.orsApiKey.isNotBlank()) {
                        stringResource(R.string.ors_mode_hint_enabled)
                    } else {
                        stringResource(R.string.ors_mode_hint_disabled)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(8.dp))

            // ── Footer / Legal ────────────────────────────────────────────────
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            AppFooter()
            Spacer(Modifier.height(8.dp))
        }
    }

    when (activeKeySheet) {
        KeySheetType.ABFAHRT -> {
            ApiKeyEditorSheet(
                title = stringResource(R.string.section_abfahrt_apikey),
                hint = stringResource(R.string.apikey_new_label_short),
                value = newApiKey,
                valueVisible = keyVisible,
                onValueChange = { newApiKey = it.trim(); keyError = false },
                onToggleVisibility = { keyVisible = !keyVisible },
                errorText = if (keyError) stringResource(R.string.apikey_error_short) else null,
                supportingText = stringResource(R.string.apikey_save_hint),
                hasExistingKey = prefs.apiKey.isNotBlank(),
                requestButtonLabel = stringResource(R.string.api_key_request_button),
                requestButtonSubject = stringResource(R.string.api_key_request_subject),
                requestButtonBody = stringResource(R.string.api_key_request_body),
                requestButtonEmail = stringResource(R.string.api_support_email),
                allowDelete = false,
                onDismiss = { activeKeySheet = null; keyError = false },
                onDelete = {},
                onSave = {
                    keyboard?.hide()
                    if (newApiKey.isBlank()) keyError = true
                    else {
                        onApiKeyChange(newApiKey)
                        activeKeySheet = null
                        keyError = false
                    }
                }
            )
        }
        KeySheetType.ORS -> {
            ApiKeyEditorSheet(
                title = stringResource(R.string.section_ors_apikey),
                hint = stringResource(R.string.ors_apikey_new_label_short),
                value = newOrsApiKey,
                valueVisible = orsKeyVisible,
                onValueChange = { newOrsApiKey = it.trim() },
                onToggleVisibility = { orsKeyVisible = !orsKeyVisible },
                errorText = null,
                supportingText = stringResource(R.string.ors_apikey_sheet_hint),
                hasExistingKey = prefs.orsApiKey.isNotBlank(),
                linkPrimaryLabel = stringResource(R.string.ors_plans_label),
                linkPrimaryUrl = stringResource(R.string.ors_plans_url),
                linkSecondaryLabel = stringResource(R.string.ors_signup_label),
                linkSecondaryUrl = stringResource(R.string.ors_signup_url),
                onDismiss = { activeKeySheet = null },
                onDelete = {
                    onOrsApiKeyChange("")
                    newOrsApiKey = ""
                    activeKeySheet = null
                },
                onSave = {
                    keyboard?.hide()
                    onOrsApiKeyChange(newOrsApiKey)
                    activeKeySheet = null
                }
            )
        }
        null -> Unit
    }

}

// ── Reusable composables ──────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title:   String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}


@Composable
private fun KeyStatusRow(
    description: String,
    isConfigured: Boolean,
    onManage: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(
                            R.string.key_status_line,
                            if (isConfigured) stringResource(R.string.key_status_configured) else stringResource(R.string.key_status_not_configured)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Key,
                    contentDescription = null,
                    tint = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = onManage,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(if (isConfigured) Icons.Default.Edit else Icons.Default.Key, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isConfigured) stringResource(R.string.key_action_change_or_delete) else stringResource(R.string.key_action_add))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyEditorSheet(
    title: String,
    hint: String,
    value: String,
    valueVisible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    errorText: String?,
    supportingText: String,
    hasExistingKey: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    linkPrimaryLabel: String? = null,
    linkPrimaryUrl: String? = null,
    linkSecondaryLabel: String? = null,
    linkSecondaryUrl: String? = null,
    requestButtonLabel: String? = null,
    requestButtonSubject: String? = null,
    requestButtonBody: String? = null,
    requestButtonEmail: String? = null,
    allowDelete: Boolean = true,
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(supportingText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(hint) },
                leadingIcon = { Icon(Icons.Default.Key, null) },
                trailingIcon = {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(if (valueVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (valueVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                isError = errorText != null,
                supportingText = errorText?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSave() }),
                shape = RoundedCornerShape(10.dp)
            )
            if (!hasExistingKey && requestButtonLabel != null && requestButtonSubject != null && requestButtonBody != null && requestButtonEmail != null) {
                OutlinedButton(
                    onClick = {
                        val subject = Uri.encode(requestButtonSubject)
                        val body = Uri.encode(requestButtonBody)
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${requestButtonEmail}?subject=${subject}&body=${body}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Mail, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(requestButtonLabel)
                }
            }
            if ((linkPrimaryLabel != null && linkPrimaryUrl != null) || (linkSecondaryLabel != null && linkSecondaryUrl != null)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (linkPrimaryLabel != null && linkPrimaryUrl != null) {
                        TextButton(
                            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(linkPrimaryUrl))) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(linkPrimaryLabel)
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                    if (linkSecondaryLabel != null && linkSecondaryUrl != null) {
                        TextButton(
                            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(linkSecondaryUrl))) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(linkSecondaryLabel)
                        }
                    }
                }
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compactActionLayout = hasExistingKey && allowDelete && maxWidth < 360.dp
                if (compactActionLayout) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(stringResource(R.string.cancel), maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = onDelete,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0x22D32F2F),
                                    contentColor = Color(0xFFD32F2F)
                                ),
                                border = BorderStroke(1.dp, Color(0x66D32F2F))
                            ) {
                                Text(stringResource(R.string.delete), maxLines = 1)
                            }
                        }
                        Button(
                            onClick = onSave,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(stringResource(R.string.save), maxLines = 1)
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(stringResource(R.string.cancel), maxLines = 1)
                        }
                        if (hasExistingKey && allowDelete) {
                            OutlinedButton(
                                onClick = onDelete,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0x22D32F2F),
                                    contentColor = Color(0xFFD32F2F)
                                ),
                                border = BorderStroke(1.dp, Color(0x66D32F2F))
                            ) {
                                Text(stringResource(R.string.delete), maxLines = 1)
                            }
                        }
                        Button(
                            onClick = onSave,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(stringResource(R.string.save), maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SliderRow(
    value:       Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    range:       ClosedFloatingPointRange<Float>,
    steps:       Int,
    startLabel:  String,
    endLabel:    String
) {
    Column {
        Slider(
            value                 = value,
            onValueChange         = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange            = range,
            steps                 = steps,
            modifier              = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(startLabel, style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.outline)
            Text(endLabel,   style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun AppFooter() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val openUrl: (String) -> Unit = { url ->
        runCatching {
            context.startActivity(
                android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            )
        }
    }
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🚌", fontSize = 28.sp)
        Text(
            text      = stringResource(R.string.app_name),
            style     = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color     = MaterialTheme.colorScheme.primary
        )
        Text(
            text  = stringResource(R.string.app_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text  = stringResource(R.string.powered_by),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { openUrl("https://www.linkedin.com/company/riles-tech/") },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Text(
                text       = stringResource(R.string.built_by),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LegalLink(stringResource(R.string.impressum))   { openUrl("https://www.abfahrt.now/terms") }
            LegalDivider()
            LegalLink(stringResource(R.string.datenschutz)) { openUrl("https://www.abfahrt.now/privacy") }
            LegalDivider()
            LegalLink(stringResource(R.string.agb))         { openUrl("https://www.abfahrt.now/terms") }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text      = stringResource(R.string.disclaimer),
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.outline,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = context.getString(R.string.version_label, AppVersionInfo.displayVersion),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun LegalLink(label: String, onClick: () -> Unit) {
    TextButton(
        onClick        = onClick,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall,
             color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun LegalDivider() {
    Text(" · ", style = MaterialTheme.typography.labelSmall,
         color = MaterialTheme.colorScheme.outline)
}

// ── Quick slot picker ─────────────────────────────────────────────────────────

@Composable
private fun QuickSlotPicker(
    currentSlots:   List<TransportMode>,
    onSlotsChanged: (List<TransportMode>) -> Unit
) {
    var pending by remember(currentSlots) { mutableStateOf(currentSlots) }
    val allModes = TransportMode.entries
    val isValid  = pending.size == 4

    Text(stringResource(R.string.quickfilter_current_order),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        pending.forEachIndexed { index, mode ->
            Surface(
                color    = if (isValid) MaterialTheme.colorScheme.primaryContainer
                           else MaterialTheme.colorScheme.errorContainer,
                shape    = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(mode.emoji, fontSize = 16.sp)
                    Text(stringResource(mode.labelRes), fontSize = 10.sp,
                        color = if (isValid) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
                }
            }
        }
        // Empty placeholder slots
        repeat((4 - pending.size).coerceAtLeast(0)) {
            Surface(
                color    = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                shape    = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Box(Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                    Text("?", fontSize = 20.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
    if (!isValid) {
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.quickfilter_need_four, pending.size),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error)
    }

    Spacer(Modifier.height(10.dp))
    Text(stringResource(R.string.quickfilter_tap_hint),
        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(4.dp))

    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement   = Arrangement.spacedBy(6.dp)
    ) {
        items(allModes.size) { idx ->
            val mode    = allModes[idx]
            val inSlots = mode in pending
            FilterChip(
                selected = inSlots,
                onClick  = {
                    val updated = if (inSlots) {
                        pending - mode
                    } else {
                        if (pending.size >= 4) pending.drop(1) + mode else pending + mode
                    }
                    pending = updated
                    if (updated.size == 4) onSlotsChanged(updated)
                },
                label = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(mode.emoji, fontSize = 16.sp, textAlign = TextAlign.Center)
                        Text(
                            text = stringResource(mode.labelRes),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            )
        }
    }
    Spacer(Modifier.height(4.dp))
    Text(stringResource(R.string.quickfilter_order_note),
        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
}

// ── Language picker ───────────────────────────────────────────────────────────

private data class LangOption(
    val lang:       now.abfahrt.transit.data.model.AppLanguage,
    val flag:       String,
    val nativeName: String,
    val searchTerms: String
)

@Composable
private fun allLanguages() = listOf(
    LangOption(now.abfahrt.transit.data.model.AppLanguage.SYSTEM, "📱", stringResource(R.string.lang_picker_system),      stringResource(R.string.lang_system_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.DE,     "🇩🇪", stringResource(R.string.lang_picker_german),     stringResource(R.string.lang_de_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.EN,     "🇬🇧", stringResource(R.string.lang_picker_english_uk), stringResource(R.string.lang_en_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.NL,     "🇳🇱", stringResource(R.string.lang_picker_dutch),      stringResource(R.string.lang_nl_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.DA,     "🇩🇰", stringResource(R.string.lang_picker_danish),     stringResource(R.string.lang_da_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.NB,     "🇳🇴", stringResource(R.string.lang_picker_norwegian),  stringResource(R.string.lang_nb_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.SV,     "🇸🇪", stringResource(R.string.lang_picker_swedish),    stringResource(R.string.lang_sv_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.FI,     "🇫🇮", stringResource(R.string.lang_picker_finnish),    stringResource(R.string.lang_fi_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.IT,     "🇮🇹", stringResource(R.string.lang_picker_italian),    stringResource(R.string.lang_it_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.ES,     "🇪🇸", stringResource(R.string.lang_picker_spanish),    stringResource(R.string.lang_es_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.PT,     "🇵🇹", stringResource(R.string.lang_picker_portuguese), stringResource(R.string.lang_pt_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.FR,     "🇫🇷", stringResource(R.string.lang_picker_french),     stringResource(R.string.lang_fr_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.PL,     "🇵🇱", stringResource(R.string.lang_picker_polish),     stringResource(R.string.lang_pl_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.CS,     "🇨🇿", stringResource(R.string.lang_picker_czech),      stringResource(R.string.lang_cs_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.HU,     "🇭🇺", stringResource(R.string.lang_picker_hungarian),  stringResource(R.string.lang_hu_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.RO,     "🇷🇴", stringResource(R.string.lang_picker_romanian),   stringResource(R.string.lang_ro_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.SK,     "🇸🇰", stringResource(R.string.lang_picker_slovak),     stringResource(R.string.lang_sk_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.HR,     "🇭🇷", stringResource(R.string.lang_picker_croatian),   stringResource(R.string.lang_hr_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.SL,     "🇸🇮", stringResource(R.string.lang_picker_slovenian),  stringResource(R.string.lang_sl_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.ET,     "🇪🇪", stringResource(R.string.lang_picker_estonian),   stringResource(R.string.lang_et_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.LV,     "🇱🇻", stringResource(R.string.lang_picker_latvian),   stringResource(R.string.lang_lv_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.LT,     "🇱🇹", stringResource(R.string.lang_picker_lithuanian), stringResource(R.string.lang_lt_search_terms)),
    LangOption(now.abfahrt.transit.data.model.AppLanguage.TLH,    "🖖",  stringResource(R.string.lang_picker_klingon),    stringResource(R.string.lang_tlh_search_terms))
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePicker(
    current:  now.abfahrt.transit.data.model.AppLanguage,
    onChange: (now.abfahrt.transit.data.model.AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(expanded) {
        if (expanded) {
            // Wait until the popup is attached before requesting focus.
            delay(120)
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    val options = allLanguages()
    val filtered = remember(query, options) {
        if (query.isBlank()) options
        else {
            val q = query.lowercase()
            options.filter { opt ->
                opt.nativeName.lowercase().contains(q) || opt.searchTerms.lowercase().contains(q)
            }
        }
    }
    val selected = options.firstOrNull { it.lang == current } ?: options.first()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = "${selected.flag}  ${selected.nativeName}",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                query = ""
                keyboard?.hide()
            }
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        stringResource(R.string.language_search_placeholder),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.SecondaryEditable)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            if (filtered.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.language_no_results),
                            color = MaterialTheme.colorScheme.outline
                        )
                    },
                    onClick = {}
                )
            } else {
                filtered.forEach { opt ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(opt.flag, fontSize = 20.sp)
                                Text(opt.nativeName, style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        onClick = {
                            onChange(opt.lang)
                            expanded = false
                            query = ""
                            keyboard?.hide()
                        },
                        trailingIcon = if (opt.lang == current) ({
                            Icon(
                                androidx.compose.material.icons.Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }) else null
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(4.dp))
    Text(
        stringResource(R.string.language_update_note),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline
    )
}


@Composable
private fun OrsTravelModeToggle(
    selected: OrsTravelMode,
    enabled: Boolean,
    onSelect: (OrsTravelMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OrsTravelMode.entries.forEach { mode ->
            val selectedMode = selected == mode
            FilterChip(
                selected = selectedMode,
                onClick = { if (enabled && !selectedMode) onSelect(mode) },
                enabled = enabled,
                label = { Text(stringResource(mode.settingsLabelRes)) },
                modifier = Modifier.weight(1f),
                leadingIcon = if (selectedMode) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}
