package com.valternegreiros.itunes_challenge_wear.ui.features.home.model
data class HomeUiData(
    val songs: List<SongUi> = emptyList(),
    val recentlyPlayed: List<SongUi> = emptyList(),
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val canLoadMore: Boolean = true
)
