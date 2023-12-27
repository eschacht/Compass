package eschacht.eng.compass

// CompassSensorManager.kt
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class CompassSensorManager(context: Context, private val onAzimuthChanged: (Float) -> Unit) {
    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val rotationListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientationVals = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationVals)
                    val azimuth = Math.toDegrees(orientationVals[0].toDouble()).toFloat()
                    var normalizedAzimuth = Math.toDegrees(orientationVals[0].toDouble()).toFloat()
                    normalizedAzimuth = (normalizedAzimuth + 360) % 360
                    onAzimuthChanged(normalizedAzimuth)
                    Log.d("CompassSensorManager", "Azimuth updated: $azimuth")
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d("CompassSensorManager", "Sensor accuracy changed: $accuracy")
            // Handle sensor accuracy changes if needed
        }
    }

    fun start() {
        rotationSensor?.also { sensor ->
            sensorManager.registerListener(rotationListener, sensor, SensorManager.SENSOR_DELAY_GAME)
            Log.d("CompassSensorManager", "Sensor listener registered")
        }
    }

    fun stop() {
        sensorManager.unregisterListener(rotationListener)
        Log.d("CompassSensorManager", "Sensor listener unregistered")
    }
}
