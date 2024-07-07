package com.example.powerstatusapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.powerstatusapp.ui.theme.PowerStatusApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("PowerStatusApp", Context.MODE_PRIVATE)
        val urlKey = getString(R.string.url_key)

        val serviceIntent = Intent(this, PowerStateService::class.java)
        startService(serviceIntent)

        setContent {
            PowerStatusApp {
                val savedUrl = sharedPreferences.getString(urlKey, "") ?: ""
                var url by remember { mutableStateOf(savedUrl) }
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()

                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = {
                            url = it
                            sharedPreferences.edit().putString(urlKey, it).apply()
                        },
                        label = { Text("URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        coroutineScope.launch {
                            val statusCode = sendTestRequest(url)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Response Code: $statusCode", Toast.LENGTH_LONG).show()
                            }
                        }
                    }) {
                        Text("Test")
                    }
                }
            }
        }
    }

    private suspend fun sendTestRequest(urlString: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                urlConnection.doOutput = true

                val jsonParam = JSONObject()
                jsonParam.put("test", "status")

                val os: OutputStream = urlConnection.outputStream
                os.write(jsonParam.toString().toByteArray(Charsets.UTF_8))
                os.flush()
                os.close()

                val responseCode = urlConnection.responseCode
                println("Response Code : $responseCode")

                urlConnection.disconnect()
                responseCode
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }
        }
    }
}
