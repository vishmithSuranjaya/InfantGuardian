package com.example.infantguradian

import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

class AlarmActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake up screen and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Keep screen on while alarm is showing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val title = intent.getStringExtra("fcm_title") ?: "No Title"
        val body = intent.getStringExtra("fcm_body") ?: "No Body"
        val dataJson = intent.getStringExtra("fcm_data_json") ?: "{}"

        // Initialize and start alarm sound
        startAlarmSound()

        setContent {
            MaterialTheme {
                // Handle back button press
                BackHandler {
                    Log.d("AlarmActivity", "Back button pressed!")
                    stopAlarmSound()
                    navigateToMainActivity()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFCCCC) // Light red background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Close button at top-right corner
                        IconButton(
                            onClick = {
                                Log.d("AlarmActivity", "Close button clicked!")
                                Toast.makeText(this@AlarmActivity, "Closing alarm...", Toast.LENGTH_SHORT).show()
                                stopAlarmSound()
                                navigateToMainActivity()
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "✕",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B0000) // Dark red
                            )
                        }

                        val scroll = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scroll)
                                .padding(16.dp)
                                .padding(top = 40.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Cry face image
                            Image(
                                painter = painterResource(id = R.drawable.cry_face),
                                contentDescription = "Crying Baby",
                                modifier = Modifier
                                    .size(150.dp)
                                    .padding(16.dp)
                            )

                            Text(
                                text = "⚠️ ALARM ⚠️",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B0000), // Dark red
                                modifier = Modifier.padding(top = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Title:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B0000)
                                    )
                                    Text(
                                        text = title,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Message:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B0000)
                                    )
                                    Text(
                                        text = body,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Data:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B0000)
                                    )
                                    val pretty = try {
                                        JSONObject(dataJson).toString(2)
                                    } catch (_: Exception) {
                                        dataJson
                                    }
                                    Text(
                                        text = pretty,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startAlarmSound() {
        try {
            // Use custom alarm sound from raw folder
            mediaPlayer = MediaPlayer().apply {
                // Load the alarm_sound.mp3 from res/raw folder
                val afd = resources.openRawResourceFd(R.raw.alarm_sound)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                isLooping = true // Loop the alarm sound continuously
                prepare()
                start()
            }
            Log.d("AlarmActivity", "Custom alarm sound started: alarm_sound.mp3")
        } catch (e: Exception) {
            Log.e("AlarmActivity", "Failed to start custom alarm sound, trying system default", e)
            // Fallback to system default alarm sound if custom sound fails
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, alarmUri)
                    isLooping = true
                    prepare()
                    start()
                }
                Log.d("AlarmActivity", "Fallback to system alarm sound")
            } catch (fallbackException: Exception) {
                Log.e("AlarmActivity", "Failed to start any alarm sound", fallbackException)
            }
        }
    }

    private fun stopAlarmSound() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            Log.d("AlarmActivity", "Alarm sound stopped")
        } catch (e: Exception) {
            Log.e("AlarmActivity", "Error stopping alarm sound", e)
        }
    }

    private fun navigateToMainActivity() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                // Clear all activities on top of MainActivity and bring it to front
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
            Log.d("AlarmActivity", "Navigating to MainActivity")
        } catch (e: Exception) {
            Log.e("AlarmActivity", "Error navigating to MainActivity, just finishing", e)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop sound when activity goes to background
        stopAlarmSound()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure sound is stopped and resources are released
        stopAlarmSound()
    }
}
