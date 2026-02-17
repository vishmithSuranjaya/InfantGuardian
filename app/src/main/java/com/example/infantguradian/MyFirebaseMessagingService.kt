package com.example.infantguradian

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

// Removed duplicate data class declarations, relying on MonitoringModels.kt

object FcmDataStore {
    private val _fcmData = MutableStateFlow<MonitoringData>(MonitoringData())
    val fcmData = _fcmData.asStateFlow()

    fun update(data: MonitoringData) {
        _fcmData.value = data
    }
}

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val CHANNEL_ID = "fcm_alarm_channel"
    private val NOTIF_ID = 1001

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val dataJson = try { JSONObject(data as Map<*, *>).toString() } catch (_: Exception) { "{}" }
        val title = message.notification?.title ?: data["title"] ?: "FCM Message"
        val body = message.notification?.body ?: data["body"] ?: dataJson

        Log.d("FCM", "Received message title=$title body=$body data=$dataJson")

        if (data.isNotEmpty()) {
            val temp = data["temperature"]?.toDoubleOrNull()
            val prediction = data["prediction"] ?: "unknown"
            val confidence = data["confidence"]?.toDoubleOrNull() ?: 0.0
            val isAlarm = data["isAlarmActive"]?.toBoolean() ?: false

            val monitoringData = MonitoringData(
                sensors = Sensors(temperature = temp),
                babyCry = BabyCryInfo(
                    prediction = prediction,
                    confidence = confidence,
                    timestamp = data["timestamp"] ?: ""
                ),
                isAlarmActive = isAlarm
            )

            // Update global store
            FcmDataStore.update(monitoringData)

            // Broadcast a simple temperature string so UI can update directly
            val tempStr = temp?.toString() ?: "NAN"
            val broadcastIntent = Intent("com.example.infantguradian.TEMPERATURE_UPDATE").apply {
                putExtra("temperature", tempStr)
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
        }

        // Prepare intent to open AlarmActivity with payload
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("fcm_title", title)
            putExtra("fcm_body", body)
            putExtra("fcm_data_json", dataJson)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // If app in foreground, start activity directly; otherwise post notification
        if (isAppInForeground()) {
            try {
                startActivity(alarmIntent)
            } catch (e: Exception) {
                Log.w("FCM", "Failed to start AlarmActivity directly, posting notification", e)
                postNotification(title, body, alarmIntent)
            }
        } else {
            postNotification(title, body, alarmIntent)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")
    }

    private fun postNotification(title: String, body: String, targetIntent: Intent) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pending = PendingIntent.getActivity(this, 0, targetIntent, flags)

        // Create full-screen intent for lock screen
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("fcm_title", title)
            putExtra("fcm_body", body)
            putExtra("fcm_data_json", targetIntent.getStringExtra("fcm_data_json") ?: "{}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or
                     Intent.FLAG_ACTIVITY_NO_USER_ACTION)
        }
        val fullScreenPending = PendingIntent.getActivity(this, 1, fullScreenIntent, flags)

        val smallIcon = R.mipmap.ic_launcher

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setFullScreenIntent(fullScreenPending, true) // This shows alarm over lock screen
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, notif)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "FCM Alarm"
            val descriptionText = "Channel for FCM alarm tests"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun isAppInForeground(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = am.runningAppProcesses ?: return false
        val myProcess = processes.firstOrNull { it.processName == packageName } ?: return false
        return myProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }
}
