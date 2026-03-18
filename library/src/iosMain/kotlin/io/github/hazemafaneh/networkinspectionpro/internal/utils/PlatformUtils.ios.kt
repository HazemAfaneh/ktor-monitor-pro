package io.github.hazemafaneh.networkinspectionpro.internal.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.serialization.json.Json
import platform.posix.gettimeofday
import platform.posix.timeval

private val prettyJson = Json { prettyPrint = true }

@OptIn(ExperimentalForeignApi::class)
actual fun currentTimeMs(): Long = memScoped {
    val tv = alloc<timeval>()
    gettimeofday(tv.ptr, null)
    tv.tv_sec * 1000L + tv.tv_usec / 1000L
}

actual fun formatJson(raw: String): String = try {
    val element = Json.parseToJsonElement(raw)
    prettyJson.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), element)
} catch (_: Exception) {
    raw
}
