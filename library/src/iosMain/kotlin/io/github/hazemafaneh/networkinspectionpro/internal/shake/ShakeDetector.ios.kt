package io.github.hazemafaneh.networkinspectionpro.internal.shake

import io.github.hazemafaneh.networkinspectionpro.internal.utils.currentTimeMs
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import kotlin.math.sqrt

@OptIn(ExperimentalForeignApi::class)
actual class ShakeDetector actual constructor(private val onShake: () -> Unit) {

    private val motionManager = CMMotionManager()
    private var lastShakeMs = 0L
    private val debounceMs = 500L
    private val shakeThreshold = 2.5

    actual fun start() {
        if (!motionManager.accelerometerAvailable) return
        motionManager.accelerometerUpdateInterval = 0.1
        motionManager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, _ ->
            data ?: return@startAccelerometerUpdatesToQueue
            val (ax, ay, az) = data.acceleration.useContents { Triple(x, y, z) }
            val magnitude = sqrt(ax * ax + ay * ay + az * az)
            val dynamicG = magnitude - 1.0
            val nowMs = currentTimeMs()
            if (dynamicG > shakeThreshold && nowMs - lastShakeMs > debounceMs) {
                lastShakeMs = nowMs
                onShake()
            }
        }
    }

    actual fun stop() {
        motionManager.stopAccelerometerUpdates()
    }
}
