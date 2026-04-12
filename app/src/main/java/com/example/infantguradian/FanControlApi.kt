package com.example.infantguradian

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FanControlApi {
    suspend fun turnFanOn(apiUrl: String, authToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(apiUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 50000
            conn.readTimeout = 50000
            conn.setRequestProperty("Authorization", authToken)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            // Build the command request JSON with correct timestamps
            val eventCreationTime = "2026-03-14T11:15:22.485Z"
            val eventTimestamp = "2026-03-14T11:15:27.538Z"
            val payload = JSONObject().apply {
                put("_eventcreationtime", eventCreationTime)
                put("fanControl", JSONObject().apply {
                    put("connectTimeoutInSeconds", 30)
                    put("methodName", "fanControl")
                    put("responseTimeoutInSeconds", 30)
                })
                put("_eventtype", "Command request")
                put("_timestamp", eventTimestamp)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.connect()

            val responseCode = conn.responseCode
            val response = try {
                conn.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                conn.errorStream?.bufferedReader()?.use { it.readText() } ?: e.message
            }

            if (responseCode in 200..299 && response != null) {
                val json = try { JSONObject(response) } catch (e: Exception) { null }
                val fanResult = json?.optJSONObject("fanControl")?.optString("result")
                val cotResult = json?.optJSONObject("CotMobile")?.optString("result")
                return@withContext fanResult ?: cotResult ?: response
            } else {
                return@withContext "Error: $response"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Exception: ${e.message}"
        }
    }
}
