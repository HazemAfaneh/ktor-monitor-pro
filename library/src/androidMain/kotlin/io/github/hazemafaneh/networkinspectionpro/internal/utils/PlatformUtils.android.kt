package io.github.hazemafaneh.networkinspectionpro.internal.utils

import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val prettyJson = Json { prettyPrint = true }

actual fun currentTimeMs(): Long = System.currentTimeMillis()

actual fun formatJson(raw: String): String = try {
    val element = Json.parseToJsonElement(raw)
    prettyJson.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), element)
} catch (_: Exception) {
    raw
}

actual fun formatTimestamp(ms: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(ms))
