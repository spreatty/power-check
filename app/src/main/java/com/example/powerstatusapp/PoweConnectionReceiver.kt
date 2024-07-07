package com.example.powerstatusapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val powerStatus = if (intent.action == Intent.ACTION_POWER_CONNECTED) "charger" else "battery"
        Toast.makeText(context, "Power: $powerStatus", Toast.LENGTH_LONG).show()

        val urlKey = context.getString(R.string.url_key)
        val sharedPreferences = context.getSharedPreferences("PowerStatusApp", Context.MODE_PRIVATE)
        val urlString = sharedPreferences?.getString(urlKey, "")

        if (!urlString.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val responseCode = sendPowerStatusRequest(urlString, powerStatus)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Response Code: $responseCode", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sendPowerStatusRequest(urlString: String, powerStatus: String): Int {
        return try {
            val url = URL(urlString)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            urlConnection.doOutput = true

            val jsonParam = JSONObject()
            jsonParam.put("power", powerStatus)

            val os: OutputStream = urlConnection.outputStream
            os.write(jsonParam.toString().toByteArray(Charsets.UTF_8))
            os.flush()
            os.close()

            val responseCode = urlConnection.responseCode
            urlConnection.disconnect()
            responseCode
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
}
