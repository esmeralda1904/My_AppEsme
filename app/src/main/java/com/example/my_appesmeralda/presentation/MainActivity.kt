/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */
package com.example.my_appesmeralda.presentation

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.my_appesmeralda.R
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRate: Float = 0f
    private var accelX: Float = 0f
    private var accelY: Float = 0f
    private var accelZ: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val boton: Button = findViewById(R.id.button)
        boton.setOnClickListener {
            Toast.makeText(this, "Leyendo sensores...", Toast.LENGTH_SHORT).show()
            startSensors()
        }
    }

    private fun startSensors() {
        val heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (heartSensor != null)
            sensorManager.registerListener(this, heartSensor, SensorManager.SENSOR_DELAY_NORMAL)
        if (accelSensor != null)
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Detiene la lectura después de 3 segundos
        window.decorView.postDelayed({
            sensorManager.unregisterListener(this)
            sendDataToPhone()
        }, 3000)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_HEART_RATE -> heartRate = event.values[0]
            Sensor.TYPE_ACCELEROMETER -> {
                accelX = event.values[0]
                accelY = event.values[1]
                accelZ = event.values[2]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun sendDataToPhone() {
        val data = JSONObject()
        data.put("heartRate", heartRate)
        data.put("accelX", accelX)
        data.put("accelY", accelY)
        data.put("accelZ", accelZ)

        val nodeClient = Wearable.getNodeClient(this)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this)
                    .sendMessage(node.id, "/sensor_data", data.toString().toByteArray())
            }
        }

        Toast.makeText(this, "Datos enviados al celular", Toast.LENGTH_SHORT).show()
    }
}