package com.valternegreiros.itunes_challenge_wear.ui.features.album

import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valternegreiros.itunes_challenge_wear.data.connectivity.NetworkConnectivityObserver
import com.google.gson.Gson
import com.valternegreiros.itunes_challenge_wear.domain.model.ResponseState
import com.valternegreiros.itunes_challenge_wear.domain.model.Song
import com.valternegreiros.itunes_challenge_wear.domain.repository.HomeRepository
import com.valternegreiros.itunes_challenge_wear.ui.features.home.model.SongUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumUiData(
    val albumTitle: String = "",
    val artistName: String = "",
    val albumArtUrl: String? = null,
    val songs: List<SongUi> = emptyList()
)

typealias AlbumUiState = ResponseState<AlbumUiData>

@HiltViewModel
class AlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HomeRepository,
    private val connectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumUiState>(ResponseState.Loading)
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    val isConnected = connectivityObserver.isConnected

    private var lastQuery: String? = null
    private var lastInitialSong: Song? = null

    init {
        val songBase64: String? = savedStateHandle["songBase64"]
        songBase64?.let { base64 ->
            try {
                val json = String(Base64.decode(base64, Base64.URL_SAFE or Base64.NO_WRAP))
                val song = Gson().fromJson(json, Song::class.java)
                
                val albumQuery = song.collectionName ?: song.trackName
                lastQuery = albumQuery
                lastInitialSong = song
                loadAlbum(albumQuery, song)
            } catch (e: Exception) {
                _uiState.value = ResponseState.Error(message = "Failed to load album info")
            }
        }
    }

    fun refresh() {
        val query = lastQuery
        val song = lastInitialSong
        if (query != null && song != null) {
            loadAlbum(query, song)
        }
    }

    private fun loadAlbum(query: String, initialSong: Song) {
        _uiState.value = ResponseState.Loading
        viewModelScope.launch {
            val tracksFlow = if (initialSong.collectionId != null) {
                repository.getAlbumTracks(initialSong.collectionId)
            } else {
                repository.searchSongs(query, limit = 50, offset = 0)
            }

            tracksFlow.collect { state ->
                when (state) {
                    is ResponseState.Loading -> {
                        // Keep loading
                    }
                    is ResponseState.Success -> {
                        val songs = state.data.map { s ->
                            SongUi(
                                id = s.trackId.toString(),
                                title = s.trackName,
                                artist = s.artistName,
                                albumArtUrl = s.artworkUrl100,
                                originalSong = s
                            )
                        }
                        _uiState.value = ResponseState.Success(
                            AlbumUiData(
                                albumTitle = initialSong.collectionName ?: initialSong.trackName,
                                artistName = initialSong.artistName,
                                albumArtUrl = initialSong.artworkUrl100,
                                songs = songs
                            )
                        )
                    }
                    is ResponseState.Error -> {
                        _uiState.value = ResponseState.Error(
                            statusCode = state.statusCode,
                            message = state.message
                        )
                    }
                }
            }
        }
    }
}
