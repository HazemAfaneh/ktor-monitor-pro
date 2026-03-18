package io.github.hazemafaneh.networkinspectionpro

import kotlinx.serialization.Serializable

@Serializable
data class NetworkLogEntry(
    val id: String,
    val method: String,
    val url: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseCode: Int? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val durationMs: Long? = null,
    val timestampMs: Long,
    val isError: Boolean = false,
    val errorMessage: String? = null
)
