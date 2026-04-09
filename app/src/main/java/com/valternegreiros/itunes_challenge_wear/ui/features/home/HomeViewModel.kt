package com.valternegreiros.itunes_challenge_wear.ui.features.home

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.valternegreiros.itunes_challenge_wear.domain.model.ResponseState
import com.valternegreiros.itunes_challenge_wear.domain.model.Song
import com.valternegreiros.itunes_challenge_wear.domain.repository.HomeRepository
import com.valternegreiros.itunes_challenge_wear.ui.features.home.model.HomeUiData
import com.valternegreiros.itunes_challenge_wear.ui.features.home.model.HomeUiState
import com.valternegreiros.itunes_challenge_wear.ui.features.home.model.SongUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentData = HomeUiData()

    private val searchTerms = listOf(
        "Ed Sheeran", "Queen", "Taylor Swift", "Beatles", "Coldplay", 
        "Rihanna", "Drake", "Imagine Dragons", "The Weeknd", "Dua Lipa",
        "Michael Jackson", "Eminem", "Bruno Mars", "Adele", "Justin Bieber",
        "Maroon 5", "Pink Floyd", "Linkin Park", "Arctic Monkeys", "Radiohead"
    )

    init {
        // Start with a random term from our list
        refreshWithRandomTerm()
        observeRecentlyPlayed()
    }

    fun refreshWithRandomTerm() {
        val randomTerm = searchTerms.random()
        searchSongs(randomTerm)
    }

    private fun observeRecentlyPlayed() {
        viewModelScope.launch {
            repository.getRecentlyPlayedSongs().collect { songs ->
                currentData = currentData.copy(recentlyPlayed = songs.map { it.toUiModel() })
                if (_uiState.value is HomeUiState.Success) {
                    _uiState.update { HomeUiState.Success(currentData) }
                }
            }
        }
    }

    fun searchSongs(query: String) {
        if (query.isBlank()) {
            currentData = currentData.copy(songs = emptyList(), searchQuery = "")
            _uiState.value = HomeUiState.Success(currentData)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.searchSongs(query, limit = 20, offset = 0).collect { result ->
                when (result) {
                    is ResponseState.Loading -> {
                        _uiState.value = HomeUiState.Loading
                    }
                    is ResponseState.Success -> {
                        currentData = currentData.copy(
                            songs = result.data.map { it.toUiModel() },
                            searchQuery = query,
                            isRefreshing = false,
                            canLoadMore = result.data.size >= 20
                        )
                        _uiState.value = HomeUiState.Success(currentData)
                    }
                    is ResponseState.Error -> {
                        _uiState.value = HomeUiState.Error(result.message ?: "Error searching for songs")
                    }
                }
            }
        }
    }

    private fun Song.toUiModel(): SongUi {
        return SongUi(
            id = trackId.toString(),
            title = trackName,
            artist = artistName,
            albumArtUrl = artworkUrl100,
            originalSong = this
        )
    }

    fun encodeSongToBase64(song: Song): String {
        val json = Gson().toJson(song)
        return Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
    }
    
    fun onSongClicked(song: Song, onNavigate: (String) -> Unit) {
        viewModelScope.launch {
            repository.markSongAsPlayed(song)
            onNavigate(encodeSongToBase64(song))
        }
    }
}
