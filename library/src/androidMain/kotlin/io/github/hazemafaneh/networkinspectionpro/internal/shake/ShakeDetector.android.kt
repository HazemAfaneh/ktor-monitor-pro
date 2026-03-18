package io.github.hazemafaneh.networkinspectionpro.internal.shake

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.github.hazemafaneh.networkinspectionpro.internal.utils.ApplicationContextHolder
import kotlin.math.sqrt

actual class ShakeDetector actual constructor(private val onShake: () -> Unit) {

    private val sensorManager: SensorManager by lazy {
        ApplicationContextHolder.applicationContext
            .getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
    }

    private var lastShakeMs = 0L
    private val debounceMs = 500L
    private val shakeThreshold = 2.7f

    private val listener = object : SensorEventListener {
        private var gravityX = 0f
        private var gravityY = 0f
        private var gravityZ = 0f

        override fun onSensorChanged(event: SensorEvent) {
            val alpha = 0.8f
            gravityX = alpha * gravityX + (1 - alpha) * event.values[0]
            gravityY = alpha * gravityY + (1 - alpha) * event.values[1]
            gravityZ = alpha * gravityZ + (1 - alpha) * event.values[2]

            val linearX = event.values[0] - gravityX
            val linearY = event.values[1] - gravityY
            val linearZ = event.values[2] - gravityZ

            val gForce = sqrt(linearX * linearX + linearY * linearY + linearZ * linearZ) /
                SensorManager.GRAVITY_EARTH

            val now = System.currentTimeMillis()
            if (gForce > shakeThreshold && now - lastShakeMs > debounceMs) {
                lastShakeMs = now
                onShake()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
    }

    actual fun start() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    actual fun stop() {
        sensorManager.unregisterListener(listener)
    }
}
