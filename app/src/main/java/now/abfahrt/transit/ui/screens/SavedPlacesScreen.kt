package now.abfahrt.transit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import now.abfahrt.transit.R
import now.abfahrt.transit.data.model.SavedPlace
import now.abfahrt.transit.data.model.SavedPlaceType
import now.abfahrt.transit.data.model.SearchResult
import now.abfahrt.transit.ui.theme.TransitBlue
import now.abfahrt.transit.ui.viewmodel.SavedPlacesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SavedPlacesScreen(
    viewModel: SavedPlacesViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.saved_places_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.saved_places_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                SavedPlaceCard(
                    type = SavedPlaceType.HOME,
                    place = state.home,
                    editing = state.editing == SavedPlaceType.HOME,
                    query = state.search.query,
                    searching = state.search.isSearching,
                    results = if (state.editing == SavedPlaceType.HOME) state.search.results else emptyList(),
                    errorMessage = if (state.editing == SavedPlaceType.HOME) state.search.errorMessage else null,
                    onEdit = { viewModel.edit(SavedPlaceType.HOME) },
                    onQueryChange = viewModel::updateQuery,
                    onSelect = viewModel::select,
                    onCancel = viewModel::cancelEditing,
                    onRemove = { viewModel.remove(SavedPlaceType.HOME) }
                )
            }

            item {
                SavedPlaceCard(
                    type = SavedPlaceType.WORK,
                    place = state.work,
                    editing = state.editing == SavedPlaceType.WORK,
                    query = state.search.query,
                    searching = state.search.isSearching,
                    results = if (state.editing == SavedPlaceType.WORK) state.search.results else emptyList(),
                    errorMessage = if (state.editing == SavedPlaceType.WORK) state.search.errorMessage else null,
                    onEdit = { viewModel.edit(SavedPlaceType.WORK) },
                    onQueryChange = viewModel::updateQuery,
                    onSelect = viewModel::select,
                    onCancel = viewModel::cancelEditing,
                    onRemove = { viewModel.remove(SavedPlaceType.WORK) }
                )
            }
        }
    }
}

@Composable
private fun SavedPlaceCard(
    type: SavedPlaceType,
    place: SavedPlace?,
    editing: Boolean,
    query: String,
    searching: Boolean,
    results: List<SearchResult>,
    errorMessage: String?,
    onEdit: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSelect: (SearchResult) -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit
) {
    val title = stringResource(if (type == SavedPlaceType.HOME) R.string.saved_places_home else R.string.saved_places_work)
    val icon = if (type == SavedPlaceType.HOME) Icons.Default.Home else Icons.Default.Work

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (place == null) {
                        Text(
                            stringResource(R.string.saved_places_not_set),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(place.title, fontWeight = FontWeight.SemiBold)
                        if (place.subtitle.isNotBlank()) {
                            Text(
                                place.subtitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                if (!editing) {
                    TextButton(onClick = onEdit) {
                        Text(stringResource(if (place == null) R.string.saved_places_set else R.string.saved_places_change))
                    }
                }
            }

            if (editing) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.saved_places_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TransitBlue,
                        focusedLabelColor = TransitBlue,
                        cursorColor = TransitBlue
                    )
                )

                when {
                    query.trim().length < 3 -> Text(
                        text = stringResource(R.string.route_search_prompt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    searching -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(stringResource(R.string.search_loading))
                    }
                    errorMessage != null -> Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    results.isEmpty() -> Text(
                        stringResource(R.string.search_no_results),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> Column {
                        results.forEachIndexed { index, result ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(result) }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(result.title, fontWeight = FontWeight.SemiBold)
                                if (result.subtitle.isNotBlank()) {
                                    Text(
                                        result.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (index < results.lastIndex) HorizontalDivider()
                        }
                    }
                }
            } else if (place != null) {
                TextButton(onClick = onRemove) {
                    Text(stringResource(R.string.saved_places_remove), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
