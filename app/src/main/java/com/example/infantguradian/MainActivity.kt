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


import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel

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

        // Fetch temperature from Azure IoT Central REST API on launch
        val azureApiUrl = "https://infantguardian.azureiotcentral.com/api/devices/kqofyk435t/telemetry/temperature?api-version=2022-07-31"
        val authToken = "SharedAccessSignature sr=929308f6-a3c9-4564-a4c9-134e79ca0a56&sig=o2CAN1Q8QiypHtQ8d%2BnliFNK%2BqQECG1Q8zSna%2FFaCVk%3D&skn=vishmith&se=1805023238855" // TODO: Replace with your actual token
        lifecycleScope.launch {
            viewModel.fetchTemperatureFromAzure(azureApiUrl, authToken)
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
        // Overlay permission intent for Android 6.0+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            startActivity(intent)
        }
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

            Spacer(modifier = Modifier.height(32.dp)) // Add vertical space between temperature and buttons

            Row(verticalAlignment = Alignment.CenterVertically) {
                FanControl(viewModel)

                Spacer(modifier = Modifier.width(16.dp))

                CotmobileControl(viewModel)
            }
        }
    }
}

@Composable
fun FanControl(viewModel: HomeViewModel) {
    var loading by remember { mutableStateOf(false) }
    var isOn by remember { mutableStateOf(false) }
    val apiUrl = "https://infantguardian.azureiotcentral.com/api/devices/kqofyk435t/commands/fanControl?api-version=2022-07-31" // TODO: Replace with your backend host
    val authToken = "SharedAccessSignature sr=929308f6-a3c9-4564-a4c9-134e79ca0a56&sig=o2CAN1Q8QiypHtQ8d%2BnliFNK%2BqQECG1Q8zSna%2FFaCVk%3D&skn=vishmith&se=1805023238855" // TODO: Use the same token as before if needed
    val coroutineScope = rememberCoroutineScope()
    // val fanResult by viewModel.fanResult.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = {
                loading = true
                coroutineScope.launch {
                    viewModel.turnFanOn(apiUrl, authToken)
                    isOn = !isOn
                    loading = false
                }
            },
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(containerColor = if (isOn) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
        ) {
            Text(
                text = when {
                    loading && !isOn -> "Turning On..."
                    loading && isOn -> "Turning Off..."
                    isOn -> "Turn Fan Off"
                    else -> "Turn Fan On"
                },
                color = if (isOn || loading) Color.White else Color.Black,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CotmobileControl(viewModel: HomeViewModel) {
    var loading by remember { mutableStateOf(false) }
    var isOn by remember { mutableStateOf(false) }
    val apiUrl = "https://infantguardian.azureiotcentral.com/api/devices/kqofyk435t/commands/CotMobile?api-version=2022-07-31" // TODO: Replace with your backend host
    val authToken = "SharedAccessSignature sr=929308f6-a3c9-4564-a4c9-134e79ca0a56&sig=o2CAN1Q8QiypHtQ8d%2BnliFNK%2BqQECG1Q8zSna%2FFaCVk%3D&skn=vishmith&se=1805023238855" // TODO: Use the same token as before if needed
    val coroutineScope = rememberCoroutineScope()
    // val cotResult by viewModel.cotMobileResult.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = {
                loading = true
                coroutineScope.launch {
                    viewModel.turnCotMobileOn(apiUrl, authToken)
                    isOn = !isOn
                    loading = false
                }
            },
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(containerColor = if (isOn) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
        ) {
            Text(
                text = when {
                    loading && !isOn -> "Turning On..."
                    loading && isOn -> "Turning Off..."
                    isOn -> "Turn Cotmobile Off"
                    else -> "Turn Cotmobile On"
                },
                color = if (isOn || loading) Color.White else Color.Black,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun AppContentPreview() {
    AppContent(viewModel = HomeViewModel())
}