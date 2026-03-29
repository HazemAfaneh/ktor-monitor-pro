package io.github.hazemafaneh.networkinspectionpro.internal.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.serialization.json.Json
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
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

actual fun formatTimestamp(ms: Long): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
    val date = NSDate.dateWithTimeIntervalSince1970(ms / 1000.0)
    return formatter.stringFromDate(date)
}
