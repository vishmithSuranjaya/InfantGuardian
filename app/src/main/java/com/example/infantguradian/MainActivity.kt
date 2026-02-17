package com.example.infantguradian

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request overlay permission for Android 10+ to show alarm over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.d("MainActivity", "Requesting overlay permission")
                requestOverlayPermission()
            } else {
                Log.d("MainActivity", "Overlay permission already granted")
            }
        }

        // Subscribe this device to the topic so it receives topic messages
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM token retrieved: $token")
                FirebaseMessaging.getInstance().subscribeToTopic("infantguardian")
                    .addOnCompleteListener { subTask ->
                        if (subTask.isSuccessful) {
                            Log.d("FCM", "Subscribed to topic: infantguardian")
                        } else {
                            Log.w("FCM", "Failed to subscribe to topic", subTask.exception)
                        }
                    }
            } else {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
            }
        }

        // Observe the global FCM data store and update the ViewModel's temperature whenever new data arrives
        lifecycleScope.launch {
            FcmDataStore.fcmData.collect { monitoringData ->
                // monitoringData is non-null (MutableStateFlow initialized with MonitoringData()), but sensors.temperature may be null
                val temp = monitoringData.sensors.temperature
                viewModel.updateTemperature(temp)
            }
        }

        setContent {
            AppContent(viewModel)
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 5469
    }
}

@Composable
fun AppContent(viewModel: HomeViewModel) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            // Remembered state to track whether we show loading or home
            var showLoading by remember { mutableStateOf(true) }

            // When this composable enters composition, wait 4 seconds then hide loading
            LaunchedEffect(Unit) {
                delay(4000L)
                showLoading = false
            }

            if (showLoading) {
                LoadingScreen()
            } else {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    // White background and centered circular image with temperature below and controls
//    val brandColor = Color(0xFF064785)
    val brandColor = Color.White

    // Observe the temperature StateFlow from the ViewModel
    val tempValue by viewModel.temperature.collectAsState()

    // Prepare display text: format with one decimal if available, otherwise show "NAN"
    val tempText = tempValue?.let { String.format(Locale.US, "%.1f°C", it) } ?: "NAN"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA2D7F2)           ), // page background set to white
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Circular container with white background behind the image
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFA2D7F2), shape = CircleShape) // circle background white
                    .border(width = 3.dp, color = Color.White, shape = CircleShape) // subtle light border
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baby_main_page),
                    contentDescription = "baby",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            // Temperature display (from FCM / ViewModel)
            Text(
                text = tempText,
                fontSize = 42.sp,
                fontWeight = FontWeight.SemiBold,
                color = brandColor,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Controls row
            Row(verticalAlignment = Alignment.CenterVertically) {
                FanControl()

                Spacer(modifier = Modifier.width(16.dp))

                CotmobileControl()
            }
        }
    }
}

@Composable
fun FanControl() {
    // local state for fan on/off
    var isOn by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { isOn = !isOn },
            colors = ButtonDefaults.buttonColors(containerColor = if (isOn) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
        ) {
            Text(text = if (isOn) "Turn Fan Off" else "Turn Fan On",
                color = if (isOn) Color.White else Color.Black,
                fontSize = 20.sp) // increased font size
        }

        Spacer(modifier = Modifier.height(8.dp))

    }
}

@Composable
fun CotmobileControl() {
    // local state for cotmobile on/off
    var isOn by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { isOn = !isOn },
            colors = ButtonDefaults.buttonColors(containerColor = if (isOn) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
        ) {
            Text(text = if (isOn) "Turn Cotmobile Off" else "Turn Cotmobile On",
                color = if (isOn) Color.White else Color.Black,
                fontSize = 20.sp) // increased font size
        }

        Spacer(modifier = Modifier.height(8.dp))


    }
}

@Preview(showBackground = true)
@Composable
fun AppContentPreview() {
    AppContent(viewModel = HomeViewModel())
}