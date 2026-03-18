package io.github.hazemafaneh.networkinspectionpro

object NetworkInspectionPro {
    var isEnabled: Boolean = false
        private set

    fun enable() {
        isEnabled = true
    }

    fun disable() {
        isEnabled = false
    }
}
