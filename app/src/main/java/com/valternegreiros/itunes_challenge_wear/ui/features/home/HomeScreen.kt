package com.valternegreiros.itunes_challenge_wear.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import com.valternegreiros.itunes_challenge_wear.ui.features.home.components.PullToRefreshLayout
import com.valternegreiros.itunes_challenge_wear.ui.features.home.model.HomeUiState
import com.valternegreiros.itunes_challenge_wear.ui.theme.DarkBackground
import com.valternegreiros.itunes_challenge_wear.ui.theme.OnDarkTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSong: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    // Get current time to show at top like in the image
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                PullToRefreshLayout(
                    isRefreshing = true,
                    onRefresh = { viewModel.refreshWithRandomTerm() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .shimmer()
                        )
                    }
                }
            }

            is HomeUiState.Success -> {
                val data = state.data
                PullToRefreshLayout(
                    isRefreshing = data.isRefreshing,
                    onRefresh = { viewModel.refreshWithRandomTerm() }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            bottom = 48.dp,
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
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Discovering",
                                    color = OnDarkTextSecondary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = data.searchQuery,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (data.songs.isEmpty()) {
                            item {
                                Text(
                                    text = "No songs found",
                                    color = OnDarkTextSecondary,
                                    modifier = Modifier.padding(top = 32.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(data.songs) { songUi ->
                                HomeSongCard(
                                    title = songUi.title,
                                    artist = songUi.artist,
                                    albumArtUrl = songUi.albumArtUrl,
                                    onClick = {
                                        viewModel.onSongClicked(songUi.originalSong, onNavigateToSong)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Try again (Pull to Refresh)",
                        color = OnDarkTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { viewModel.refreshWithRandomTerm() }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSongCard(
    title: String,
    artist: String,
    albumArtUrl: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1C)) // Sleek dark surface color for the card
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with Coil
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF2B2B2B))
        ) {
            if (!albumArtUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = albumArtUrl,
                    contentDescription = "Artwork for $title",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp),
                    tint = OnDarkTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Text info (Title and Artist)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artist,
                color = OnDarkTextSecondary,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
