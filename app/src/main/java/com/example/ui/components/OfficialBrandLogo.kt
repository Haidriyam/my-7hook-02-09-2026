package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R

/**
 * EXACT OFFICIAL 7HOOKS BRAND ASSET
 * MANDATE: ONE consistent official 7Hooks logo throughout the application.
 * Do NOT create another logo.
 * Do NOT generate an alternative logo.
 * Do NOT recolor the logo.
 * Do NOT use different logo variations between screens.
 */
object SevenHooksBrand {
    const val OFFICIAL_LOGO_URL =
        "https://7hooks.com/wp-content/uploads/2025/11/cropped-394608127_1379560635982079_6192428464456395363_n-removebg-preview-e1764314943627.png"

    val OFFICIAL_LOGO_RES = R.drawable.official_7hooks_logo
}

@Composable
fun Official7HooksLogo(
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .height(height)
            .aspectRatio(512f / 151f),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(SevenHooksBrand.OFFICIAL_LOGO_URL)
                .crossfade(true)
                .error(SevenHooksBrand.OFFICIAL_LOGO_RES)
                .placeholder(SevenHooksBrand.OFFICIAL_LOGO_RES)
                .fallback(SevenHooksBrand.OFFICIAL_LOGO_RES)
                .build(),
            contentDescription = "7Hooks Official Logo",
            contentScale = contentScale
        )
    }
}
