package io.github.hazemafaneh.networkinspectionpro

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class NetworkLogEntryTest {

    private val entry = NetworkLogEntry(
        id = "1",
        method = "GET",
        url = "https://example.com/api/users",
        requestHeaders = mapOf("Authorization" to "Bearer token"),
        requestBody = null,
        responseCode = 200,
        responseHeaders = mapOf("Content-Type" to "application/json"),
        responseBody = """{"name":"Hazem"}""",
        durationMs = 123L,
        timestampMs = 1000L,
        isError = false
    )

    @Test
    fun `default values are applied correctly`() {
        val minimal = NetworkLogEntry(id = "2", method = "POST", url = "https://example.com", timestampMs = 0L)
        assertEquals(emptyMap(), minimal.requestHeaders)
        assertEquals(emptyMap(), minimal.responseHeaders)
        assertNull(minimal.requestBody)
        assertNull(minimal.responseCode)
        assertNull(minimal.responseBody)
        assertNull(minimal.durationMs)
        assertNull(minimal.errorMessage)
        assertFalse(minimal.isError)
    }

    @Test
    fun `serialization round-trip preserves all fields`() {
        val json = Json.encodeToString(entry)
        val decoded = Json.decodeFromString<NetworkLogEntry>(json)
        assertEquals(entry, decoded)
    }

    @Test
    fun `serialization round-trip with null optional fields`() {
        val minimal = NetworkLogEntry(id = "3", method = "DELETE", url = "https://example.com/1", timestampMs = 500L)
        val json = Json.encodeToString(minimal)
        val decoded = Json.decodeFromString<NetworkLogEntry>(json)
        assertEquals(minimal, decoded)
    }

    @Test
    fun `isError flag is preserved`() {
        val errorEntry = entry.copy(isError = true, errorMessage = "Timeout")
        val json = Json.encodeToString(errorEntry)
        val decoded = Json.decodeFromString<NetworkLogEntry>(json)
        assertEquals(true, decoded.isError)
        assertEquals("Timeout", decoded.errorMessage)
    }
}
