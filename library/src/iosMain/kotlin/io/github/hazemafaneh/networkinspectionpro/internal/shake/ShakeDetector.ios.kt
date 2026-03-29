package io.github.hazemafaneh.networkinspectionpro.internal.shake

import io.github.hazemafaneh.networkinspectionpro.internal.utils.currentTimeMs
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import kotlin.math.roundToInt
import kotlin.math.sqrt

private fun Double.fmt() = (this * 1000).roundToInt() / 1000.0

@OptIn(ExperimentalForeignApi::class)
actual class ShakeDetector actual constructor(private val onShake: () -> Unit) {

    private val motionManager = CMMotionManager()
    private var lastShakeMs = 0L
    private val debounceMs = 500L
    private val shakeThreshold = 1.5

    actual fun start() {
        println("[ShakeDetector] start() called")
        println("[ShakeDetector] deviceMotionAvailable=${motionManager.deviceMotionAvailable}")
        println("[ShakeDetector] accelerometerAvailable=${motionManager.accelerometerAvailable}")

        if (!motionManager.deviceMotionAvailable) {
            println("[ShakeDetector] ERROR: deviceMotion not available, falling back to raw accelerometer")
            startAccelerometer()
            return
        }

        motionManager.deviceMotionUpdateInterval = 0.05
        motionManager.startDeviceMotionUpdatesToQueue(NSOperationQueue.mainQueue) { data, error ->
            if (error != null) {
                println("[ShakeDetector] deviceMotion error: $error")
                return@startDeviceMotionUpdatesToQueue
            }
            if (data == null) {
                println("[ShakeDetector] deviceMotion data is null")
                return@startDeviceMotionUpdatesToQueue
            }
            val (ax, ay, az) = data.userAcceleration.useContents { Triple(x, y, z) }
            val magnitude = sqrt(ax * ax + ay * ay + az * az)
            val nowMs = currentTimeMs()
            println("[ShakeDetector] userAcceleration magnitude=${magnitude.fmt()}")
            if (magnitude > shakeThreshold && nowMs - lastShakeMs > debounceMs) {
                lastShakeMs = nowMs
                println("[ShakeDetector] SHAKE detected! magnitude=${magnitude.fmt()}")
                onShake()
            }
        }
        println("[ShakeDetector] deviceMotion started")
    }

    private fun startAccelerometer() {
        if (!motionManager.accelerometerAvailable) {
            println("[ShakeDetector] ERROR: accelerometer not available either — shake detection disabled")
            return
        }
        motionManager.accelerometerUpdateInterval = 0.05
        motionManager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, error ->
            if (error != null) {
                println("[ShakeDetector] accelerometer error: $error")
                return@startAccelerometerUpdatesToQueue
            }
            if (data == null) return@startAccelerometerUpdatesToQueue
            val (ax, ay, az) = data.acceleration.useContents { Triple(x, y, z) }
            val magnitude = sqrt(ax * ax + ay * ay + az * az)
            val dynamicG = magnitude - 1.0
            val nowMs = currentTimeMs()
            println("[ShakeDetector] fallback acc magnitude=${magnitude.fmt()} dynamicG=${dynamicG.fmt()}")
            if (dynamicG > shakeThreshold && nowMs - lastShakeMs > debounceMs) {
                lastShakeMs = nowMs
                println("[ShakeDetector] SHAKE detected via fallback! dynamicG=${dynamicG.fmt()}")
                onShake()
            }
        }
        println("[ShakeDetector] fallback accelerometer started")
    }

    actual fun stop() {
        println("[ShakeDetector] stop() called")
        motionManager.stopDeviceMotionUpdates()
        motionManager.stopAccelerometerUpdates()
    }
}
