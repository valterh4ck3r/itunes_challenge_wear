package com.valternegreiros.itunes_challenge_wear.ui.features.album

import androidx.lifecycle.SavedStateHandle
import com.valternegreiros.itunes_challenge_wear.MainDispatcherRule
import com.valternegreiros.itunes_challenge_wear.data.connectivity.NetworkConnectivityObserver
import com.valternegreiros.itunes_challenge_wear.domain.model.ResponseState
import com.valternegreiros.itunes_challenge_wear.domain.model.Song
import com.valternegreiros.itunes_challenge_wear.domain.repository.HomeRepository
import com.valternegreiros.itunes_challenge_wear.ui.core.util.Base64Utils
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: HomeRepository = mockk()
    private val connectivityObserver: NetworkConnectivityObserver = mockk()
    private val savedStateHandle: SavedStateHandle = mockk()
    private lateinit var viewModel: AlbumViewModel

    @Before
    fun setUp() {
        every { connectivityObserver.isConnected } returns flowOf(true)
    }

    @Test
    fun `viewModel should load album details when initialized with songBase64`() = runTest {
        // Given
        val songJson = """{"trackId":1, "trackName":"Song 1", "artistName":"Artist 1", "collectionName":"Album 1", "collectionId": 100}"""
        val songBase64 = Base64Utils.encode(songJson)
        every { savedStateHandle.get<String>("songBase64") } returns songBase64
        
        val tracks = listOf(
            Song(trackId = 1, trackName = "Song 1", artistName = "Artist 1", collectionName = "Album 1", artworkUrl100 = null, previewUrl = null, trackTimeMillis = null)
        )
        every { repository.getAlbumTracks(100L) } returns flowOf(ResponseState.Success(tracks))

        // When
        viewModel = AlbumViewModel(savedStateHandle, repository, connectivityObserver)

        // Then
        assertTrue(viewModel.uiState.value is ResponseState.Success)
        val successData = (viewModel.uiState.value as ResponseState.Success).data
        assertEquals("Album 1", successData.albumTitle)
        assertEquals(1, successData.songs.size)
        assertEquals("Song 1", successData.songs[0].title)
    }

    @Test
    fun `viewModel should emit error when base64 decoding fails`() = runTest {
        // Given
        val songBase64 = "invalid-base-64!@#"
        every { savedStateHandle.get<String>("songBase64") } returns songBase64

        // When
        viewModel = AlbumViewModel(savedStateHandle, repository, connectivityObserver)

        // Then
        assertTrue(viewModel.uiState.value is ResponseState.Error)
        assertEquals("Failed to load album info", (viewModel.uiState.value as ResponseState.Error).message)
    }
}
