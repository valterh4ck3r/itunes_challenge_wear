package com.valternegreiros.itunes_challenge_wear.ui.features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.valternegreiros.itunes_challenge_wear.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valternegreiros.itunes_challenge_wear.ui.theme.DarkBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainNavigationScreen(
    viewModel: MainNavigationViewModel,
    onNavigateToSong: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
) {
    val lastPlayedSong by viewModel.lastPlayedSong.collectAsState()
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 24.dp,
                start = 12.dp,
                end = 12.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = currentTime,
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }

            item {
                Text(
                    text = "Music",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Now Playing
            item {
                NavigationChip(
                    text = "Now playing",
                    iconResId = R.drawable.ic_menu_now_playing,
                    onClick = {
                        viewModel.getEncodedLastSong()?.let { onNavigateToSong(it) }
                    }
                )
            }

            // Albums
            item {
                NavigationChip(
                    text = "Albums",
                    iconResId = R.drawable.ic_menu_albums,
                    onClick = {
                        viewModel.getEncodedLastSong()?.let { onNavigateToAlbum(it) }
                    }
                )
            }

            // Songs (Home)
            item {
                NavigationChip(
                    text = "Songs",
                    iconResId = R.drawable.ic_menu_songs,
                    onClick = onNavigateToHome
                )
            }
        }
    }
}

@Composable
fun NavigationChip(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) Color(0xFF1C1C1C) else Color(0xFF1C1C1C).copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = if (enabled) Color.White else Color.Gray,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = text,
            color = if (enabled) Color.White else Color.Gray,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
