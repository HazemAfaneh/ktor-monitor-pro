package io.github.hazemafaneh.networkinspectionpro.internal.shake

expect class ShakeDetector(onShake: () -> Unit) {
    fun start()
    fun stop()
}
