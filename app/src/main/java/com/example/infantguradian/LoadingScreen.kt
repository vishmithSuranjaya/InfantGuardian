package com.example.infantguradian

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer

/**
 * LoadingScreen composable (Jetpack Compose) rewritten from the React Native loading screen.
 * - imageRes: optional drawable resource id (pass R.drawable.onboarding_baby when you add it),
 *   otherwise the default app icon is used so the project compiles safely.
 */
@Composable
fun LoadingScreen() {
    // Soft pastel background and brand color for text
    val backgroundColor = Color(0xFFEAF6FF) // very light blue
    val brandColor = Color(0xFF064785) // deep blue used for brand text

    // subtle breathing animation
    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            // Rounded image with breathing animation
            val painter = painterResource(id = R.drawable.onboarding_baby)
            Box(modifier = Modifier
                .size(220.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(CircleShape)) {
                Image(
                    painter = painter,
                    contentDescription = "onboarding baby",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            // App name below image
            Text(
                text = "infantguardian",
                fontSize = 28.sp,
                color = brandColor,
                modifier = Modifier.padding(top = 18.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    LoadingScreen()
}
