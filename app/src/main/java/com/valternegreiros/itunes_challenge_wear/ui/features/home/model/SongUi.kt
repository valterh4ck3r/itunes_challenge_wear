package com.valternegreiros.itunes_challenge_wear.ui.features.home.model

import com.valternegreiros.itunes_challenge_wear.domain.model.Song

data class SongUi(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String? = null,
    val originalSong: Song
)
