package com.example.my_appesmeralda

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.my_appesmeralda.ui.theme.My_AppEsmeraldaTheme

class MainActivity : AppCompatActivity(),
    CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private lateinit var conectar: Button
    private var activityContext: Context? = null
    private var deviceConnected: Boolean = false
    private val PAYLOAD_PATH = "/APP_OPEN"
    private lateinit var nodeID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityContext = this
        conectar = findViewById(R.id.button)

        conectar.setOnClickListener {
            if (!deviceConnected) {
                val tempAct: Activity = activityContext as MainActivity
                getNodes(tempAct)
            }
        }
    }

    private fun getNodes(context: Context) {
        launch(Dispatchers.Default) {
            val nodeList = Wearable.getNodeClient(context).connectedNodes
            try {
                val nodes = Tasks.await(nodeList)
                for (node in nodes) {
                    Log.d("NODO", "ID del nodo: ${node.id}")
                    nodeID = node.id
                    deviceConnected = true
                }
            } catch (exception: Exception) {
                Log.d("ERROR en el nodo", exception.toString())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activityContext?.let {
            Wearable.getDataClient(it).removeListener(this)
            Wearable.getMessageClient(it).removeListener(this)
            Wearable.getCapabilityClient(it).removeListener(this)
        }
    }

    override fun onResume() {
        super.onResume()
        activityContext?.let {
            Wearable.getDataClient(it).addListener(this)
            Wearable.getMessageClient(it).addListener(this)
            Wearable.getCapabilityClient(it)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {}

    override fun onMessageReceived(ME: MessageEvent) {
        if (ME.path == "/sensor_data") { // 👈 Ruta del mensaje que manda el reloj
            val message = String(ME.data, StandardCharsets.UTF_8)
            Log.d("onMessageReceived", "Datos recibidos: $message")

            runOnUiThread {
                Toast.makeText(this, "Datos del reloj: $message", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d("onMessageReceived", "Ruta no reconocida: ${ME.path}")
        }
    }

    override fun onCapabilityChanged(info: CapabilityInfo) {
        Log.d("CAPABILITY", "Cambiado: ${info.name}")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    My_AppEsmeraldaTheme {
        Greeting("Android")
    }
}