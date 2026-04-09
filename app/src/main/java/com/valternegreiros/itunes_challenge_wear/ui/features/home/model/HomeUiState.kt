package com.valternegreiros.itunes_challenge_wear.ui.features.home.model

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: HomeUiData) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

data class HomeUiData(
    val songs: List<SongUi> = emptyList(),
    val recentlyPlayed: List<SongUi> = emptyList(),
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val canLoadMore: Boolean = true
)
