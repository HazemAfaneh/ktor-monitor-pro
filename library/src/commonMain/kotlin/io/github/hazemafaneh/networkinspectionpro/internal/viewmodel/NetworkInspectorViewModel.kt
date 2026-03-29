package io.github.hazemafaneh.networkinspectionpro.internal.viewmodel

import io.github.hazemafaneh.networkinspectionpro.NetworkLogEntry
import io.github.hazemafaneh.networkinspectionpro.NetworkLogStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class NetworkInspectorViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedEntryId = MutableStateFlow<String?>(null)
    val selectedEntryId: StateFlow<String?> = _selectedEntryId.asStateFlow()

    val filteredEntries: StateFlow<List<NetworkLogEntry>> = combine(
        NetworkLogStore.entries,
        _searchQuery
    ) { entries, query ->
        if (query.isBlank()) entries
        else entries.filter {
            it.url.contains(query, ignoreCase = true) ||
            it.method.contains(query, ignoreCase = true) ||
            it.responseCode?.toString()?.contains(query) == true
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onEntrySelected(entry: NetworkLogEntry?) {
        _selectedEntryId.value = entry?.id
    }

    fun onDestroy() {
        scope.cancel()
    }
}
