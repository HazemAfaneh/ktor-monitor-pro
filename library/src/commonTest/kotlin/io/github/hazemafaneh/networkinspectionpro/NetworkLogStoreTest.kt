package io.github.hazemafaneh.networkinspectionpro

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkLogStoreTest {

    private fun entry(id: String, url: String = "https://example.com", responseCode: Int? = null) =
        NetworkLogEntry(
            id = id,
            method = "GET",
            url = url,
            timestampMs = 1000L,
            responseCode = responseCode
        )

    @BeforeTest
    fun setUp() {
        NetworkLogStore.clear()
    }

    @AfterTest
    fun tearDown() {
        NetworkLogStore.clear()
    }

    @Test
    fun `empty by default`() {
        assertTrue(NetworkLogStore.entries.value.isEmpty())
    }

    @Test
    fun `addOrUpdate adds new entry`() {
        NetworkLogStore.addOrUpdate(entry("1"))
        assertEquals(1, NetworkLogStore.entries.value.size)
        assertEquals("1", NetworkLogStore.entries.value.first().id)
    }

    @Test
    fun `addOrUpdate appends multiple distinct entries`() {
        NetworkLogStore.addOrUpdate(entry("1"))
        NetworkLogStore.addOrUpdate(entry("2"))
        NetworkLogStore.addOrUpdate(entry("3"))
        assertEquals(3, NetworkLogStore.entries.value.size)
    }

    @Test
    fun `addOrUpdate replaces existing entry with same id`() {
        NetworkLogStore.addOrUpdate(entry("1", url = "https://example.com/old"))
        NetworkLogStore.addOrUpdate(entry("1", url = "https://example.com/new", responseCode = 200))

        val entries = NetworkLogStore.entries.value
        assertEquals(1, entries.size)
        assertEquals("https://example.com/new", entries.first().url)
        assertEquals(200, entries.first().responseCode)
    }

    @Test
    fun `addOrUpdate preserves order when updating`() {
        NetworkLogStore.addOrUpdate(entry("1"))
        NetworkLogStore.addOrUpdate(entry("2"))
        NetworkLogStore.addOrUpdate(entry("1", responseCode = 200))

        val entries = NetworkLogStore.entries.value
        assertEquals(2, entries.size)
        assertEquals("1", entries[0].id)
        assertEquals("2", entries[1].id)
    }

    @Test
    fun `clear removes all entries`() {
        NetworkLogStore.addOrUpdate(entry("1"))
        NetworkLogStore.addOrUpdate(entry("2"))
        NetworkLogStore.clear()
        assertTrue(NetworkLogStore.entries.value.isEmpty())
    }
}
