package com.valternegreiros.itunes_challenge_wear.ui.features.song

import androidx.lifecycle.SavedStateHandle
import androidx.media3.exoplayer.ExoPlayer
import com.valternegreiros.itunes_challenge_wear.MainDispatcherRule
import com.valternegreiros.itunes_challenge_wear.data.connectivity.NetworkConnectivityObserver
import com.valternegreiros.itunes_challenge_wear.domain.model.Song
import com.valternegreiros.itunes_challenge_wear.domain.repository.HomeRepository
import com.valternegreiros.itunes_challenge_wear.ui.core.util.Base64Utils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: HomeRepository = mockk(relaxed = true)
    private val connectivityObserver: NetworkConnectivityObserver = mockk()
    private val player: ExoPlayer = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk()
    private lateinit var viewModel: SongViewModel

    @Before
    fun setUp() {
        every { connectivityObserver.isConnected } returns MutableStateFlow(true)
    }

    @Test
    fun `should prepare song when initialized with valid Base64`() = runTest {
        // Given
        val song = Song(trackId = 1, trackName = "Song 1", artistName = "Artist 1", collectionName = "Album 1", artworkUrl100 = null, previewUrl = "http://preview.m4a", trackTimeMillis = null)
        val songJson = com.google.gson.Gson().toJson(song)
        val base64 = Base64Utils.encode(songJson)
        every { savedStateHandle.get<String>("songBase64") } returns base64

        // When
        viewModel = SongViewModel(savedStateHandle, repository, connectivityObserver, player)

        // Then
        assertEquals(song, viewModel.uiState.value.song)
        verify { player.prepare() }
        verify { player.play() }
    }

    @Test
    fun `togglePlayPause should call player pause if playing`() = runTest {
        // Given
        every { player.isPlaying } returns true
        every { savedStateHandle.get<String>(any()) } returns null
        viewModel = SongViewModel(savedStateHandle, repository, connectivityObserver, player)

        // When
        viewModel.togglePlayPause()

        // Then
        verify { player.pause() }
    }

    @Test
    fun `togglePlayPause should call player play if not playing`() = runTest {
        // Given
        every { player.isPlaying } returns false
        every { savedStateHandle.get<String>(any()) } returns null
        viewModel = SongViewModel(savedStateHandle, repository, connectivityObserver, player)

        // When
        viewModel.togglePlayPause()

        // Then
        verify { player.play() }
    }
}
