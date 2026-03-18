package io.github.hazemafaneh.networkinspectionpro.internal.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hazemafaneh.networkinspectionpro.NetworkLogEntry
import io.github.hazemafaneh.networkinspectionpro.NetworkLogStore
import io.github.hazemafaneh.networkinspectionpro.internal.ui.components.MethodBadge
import io.github.hazemafaneh.networkinspectionpro.internal.ui.components.StatusBadge
import io.github.hazemafaneh.networkinspectionpro.internal.viewmodel.NetworkInspectorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkInspectorListScreen(
    viewModel: NetworkInspectorViewModel,
    onEntryClick: (NetworkLogEntry) -> Unit
) {
    val entries by viewModel.filteredEntries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Inspector") },
                actions = {
                    TextButton(onClick = { NetworkLogStore.clear() }) {
                        Text("Clear")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                placeholder = { Text("Search URL, method, status…") },
                singleLine = true
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(entries, key = { it.id }) { entry ->
                    NetworkLogEntryRow(entry = entry, onClick = { onEntryClick(entry) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun NetworkLogEntryRow(entry: NetworkLogEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MethodBadge(method = entry.method)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.url,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            if (entry.durationMs != null) {
                Text(
                    text = "${entry.durationMs}ms",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        StatusBadge(code = entry.responseCode)
    }
}
