package com.valternegreiros.itunes_challenge_wear.ui.features.main

import com.valternegreiros.itunes_challenge_wear.ui.core.util.Base64Utils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.valternegreiros.itunes_challenge_wear.domain.model.Song
import com.valternegreiros.itunes_challenge_wear.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem

@HiltViewModel
class MainNavigationViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val player: ExoPlayer
) : ViewModel() {

    private val _lastPlayedSong = MutableStateFlow<Song?>(null)
    val lastPlayedSong: StateFlow<Song?> = _lastPlayedSong.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getRecentlyPlayedSongs(),
                repository.getAllCachedSongs(1)
            ) { recently, cached ->
                recently.firstOrNull() ?: cached.firstOrNull()
            }.collect { song ->
                _lastPlayedSong.value = song
            }
        }
    }

    fun getEncodedLastSong(): String? {
        // 1. Try to get the song directly from the player (Ground Truth for Now Playing)
        val songInPlayer = player.currentMediaItem?.localConfiguration?.tag as? Song
        
        // 2. Fallback to the last played song from DB
        val song = songInPlayer ?: _lastPlayedSong.value ?: return null
        
        val json = Gson().toJson(song)
        return Base64Utils.encode(json)
    }
}
