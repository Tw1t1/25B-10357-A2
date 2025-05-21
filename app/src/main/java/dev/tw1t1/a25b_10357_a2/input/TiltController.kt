package dev.tw1t1.a25b_10357_a2.input

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class TiltController(private val context: Context) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var isRegistered = false

    // Sensitivity threshold - adjust these values based on how sensitive you want the controls to be
    private val TILT_THRESHOLD = 2.0f
    private val TILT_FORWARD_BACKWARD_THRESHOLD = 2.0f  // May need adjustment for comfortable play

    // Callback interfaces
    var onTiltLeft: (() -> Unit)? = null
    var onTiltRight: (() -> Unit)? = null
    var onTiltForward: (() -> Unit)? = null  // Added callback for tilting forward
    var onTiltBackward: (() -> Unit)? = null // Added callback for tilting backward

    // Debounce handling - to prevent multiple triggers when holding the device tilted
    private var lastTiltAction = TiltAction.NONE
    private var lastTiltTimestamp = 0L
    private val DEBOUNCE_TIME = 500L // milliseconds

    enum class TiltAction {
        NONE, LEFT, RIGHT, FORWARD, BACKWARD
    }

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun startListening() {
        if (!isRegistered && accelerometer != null) {
            sensorManager?.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
            isRegistered = true
        }
    }

    fun stopListening() {
        if (isRegistered) {
            sensorManager?.unregisterListener(this)
            isRegistered = false
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0] // Left/Right tilt
            val y = event.values[1] // Forward/Backward tilt

            // Check for tilt actions with debouncing
            val currentTime = System.currentTimeMillis()

            // Handle left/right tilt
            when {
                x < -TILT_THRESHOLD && abs(x) > abs(y) -> {
                    // Device is tilted right
                    if (lastTiltAction != TiltAction.RIGHT ||
                        currentTime - lastTiltTimestamp > DEBOUNCE_TIME) {
                        onTiltRight?.invoke()
                        lastTiltAction = TiltAction.RIGHT
                        lastTiltTimestamp = currentTime
                    }
                }
                x > TILT_THRESHOLD && abs(x) > abs(y) -> {
                    // Device is tilted left
                    if (lastTiltAction != TiltAction.LEFT ||
                        currentTime - lastTiltTimestamp > DEBOUNCE_TIME) {
                        onTiltLeft?.invoke()
                        lastTiltAction = TiltAction.LEFT
                        lastTiltTimestamp = currentTime
                    }
                }
                y < -TILT_FORWARD_BACKWARD_THRESHOLD && abs(y) > abs(x) -> {
                    // Device is tilted forward
                    if (lastTiltAction != TiltAction.FORWARD ||
                        currentTime - lastTiltTimestamp > DEBOUNCE_TIME) {
                        onTiltForward?.invoke()
                        lastTiltAction = TiltAction.FORWARD
                        lastTiltTimestamp = currentTime
                    }
                }
                y > TILT_FORWARD_BACKWARD_THRESHOLD && abs(y) > abs(x) -> {
                    // Device is tilted backward
                    if (lastTiltAction != TiltAction.BACKWARD ||
                        currentTime - lastTiltTimestamp > DEBOUNCE_TIME) {
                        onTiltBackward?.invoke()
                        lastTiltAction = TiltAction.BACKWARD
                        lastTiltTimestamp = currentTime
                    }
                }
                abs(x) < 1.0f && abs(y) < 1.0f -> {
                    // Device is relatively flat, reset tilt state
                    lastTiltAction = TiltAction.NONE
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
}