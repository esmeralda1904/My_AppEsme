package com.example.my_appesmeralda

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.my_appesmeralda.R
import com.example.my_appesmeralda.ui.theme.My_AppEsmeraldaTheme
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

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
        try {
            activityContext?.let {
                Wearable.getDataClient(it).removeListener(this)
                Wearable.getMessageClient(it).removeListener(this)
                Wearable.getCapabilityClient(it).removeListener(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            activityContext?.let {
                Wearable.getDataClient(it).addListener(this)
                Wearable.getMessageClient(it).addListener(this)
                Wearable.getCapabilityClient(it)
                    .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        // Puedes implementar aquí el manejo de datos
    }

    override fun onMessageReceived(ME: MessageEvent) {
        Log.d("onMessageReceived", "ID del nodo: ${ME.sourceNodeId}")
        Log.d("onMessageReceived", "Payload: ${ME.path}")
        val message = String(ME.data, StandardCharsets.UTF_8)
        Log.d("onMessageReceived", "Mensaje: $message")
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        // Implementa aquí si necesitas manejar cambios de capacidad
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