package com.valternegreiros.itunes_challenge_wear.ui.features.main

import android.util.Base64
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

@HiltViewModel
class MainNavigationViewModel @Inject constructor(
    private val repository: HomeRepository
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
        val song = _lastPlayedSong.value ?: return null
        val json = Gson().toJson(song)
        return Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
    }
}
