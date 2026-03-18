package io.github.hazemafaneh.networkinspectionpro.internal.utils

import kotlinx.serialization.json.Json

private val prettyJson = Json { prettyPrint = true }

actual fun currentTimeMs(): Long = System.currentTimeMillis()

actual fun formatJson(raw: String): String = try {
    val element = Json.parseToJsonElement(raw)
    prettyJson.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), element)
} catch (_: Exception) {
    raw
}
