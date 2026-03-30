package io.github.hazemafaneh.networkinspectionpro

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkInspectionProTest {

    @AfterTest
    fun tearDown() {
        NetworkInspectionPro.disable()
    }

    @Test
    fun `disabled by default`() {
        assertFalse(NetworkInspectionPro.isEnabled)
    }

    @Test
    fun `enable sets isEnabled to true`() {
        NetworkInspectionPro.enable()
        assertTrue(NetworkInspectionPro.isEnabled)
    }

    @Test
    fun `disable sets isEnabled to false`() {
        NetworkInspectionPro.enable()
        NetworkInspectionPro.disable()
        assertFalse(NetworkInspectionPro.isEnabled)
    }
}
