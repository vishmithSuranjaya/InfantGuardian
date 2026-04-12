package com.example.infantguradian

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AzureIoTApi {
    suspend fun fetchTemperature(apiUrl: String, authToken: String): Double? = withContext(Dispatchers.IO) {
        try {
            val url = URL(apiUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("Authorization", authToken)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connect()

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                // The Node.js backend expects { value: <number>, timestamp: <string> }
                return@withContext json.optDouble("value", Double.NaN).takeIf { !it.isNaN() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
