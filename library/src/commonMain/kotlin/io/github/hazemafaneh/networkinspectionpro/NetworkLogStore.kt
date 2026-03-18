package io.github.hazemafaneh.networkinspectionpro

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object NetworkLogStore {
    private val _entries = MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    val entries: StateFlow<List<NetworkLogEntry>> = _entries.asStateFlow()

    internal fun addOrUpdate(entry: NetworkLogEntry) {
        _entries.update { current ->
            val index = current.indexOfFirst { it.id == entry.id }
            if (index == -1) {
                current + entry
            } else {
                current.toMutableList().also { it[index] = entry }
            }
        }
    }

    fun clear() {
        _entries.value = emptyList()
    }
}
