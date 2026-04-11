package com.valternegreiros.itunes_challenge_wear.ui.features.home

import com.valternegreiros.itunes_challenge_wear.MainDispatcherRule
import com.valternegreiros.itunes_challenge_wear.domain.model.ResponseState
import com.valternegreiros.itunes_challenge_wear.domain.model.Song
import com.valternegreiros.itunes_challenge_wear.domain.repository.HomeRepository
import com.valternegreiros.itunes_challenge_wear.ui.features.home.model.HomeUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: HomeRepository = mockk()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        every { repository.getRecentlyPlayedSongs() } returns flowOf(emptyList())
        every { repository.searchSongs(any(), any(), any(), any()) } returns flowOf(ResponseState.Loading)
        
        viewModel = HomeViewModel(repository)
    }

    @Test
    fun `searchSongs should update uiState to Success when repository returns results`() = runTest {
        // Given
        val query = "Queen"
        val songs = listOf(
            Song(trackId = 1L, trackName = "Bohemian Rhapsody", artistName = "Queen", collectionName = "A Night at the Opera", artworkUrl100 = "url", previewUrl = "preview", trackTimeMillis = 350000L)
        )
        every { repository.searchSongs(query, 20, 0) } returns flowOf(ResponseState.Success(songs))

        // When
        viewModel.searchSongs(query)

        // Then
        assertTrue(viewModel.uiState.value is HomeUiState.Success)
        val successState = viewModel.uiState.value as HomeUiState.Success
        assertEquals(1, successState.data.songs.size)
        assertEquals("Bohemian Rhapsody", successState.data.songs[0].title)
        assertEquals(query, successState.data.searchQuery)
    }

    @Test
    fun `searchSongs should update uiState to Error when repository returns error`() = runTest {
        // Given
        val query = "Queen"
        val errorMessage = "Network Error"
        every { repository.searchSongs(query, 20, 0) } returns flowOf(ResponseState.Error(message = errorMessage))

        // When
        viewModel.searchSongs(query)

        // Then
        assertTrue(viewModel.uiState.value is HomeUiState.Error)
        val errorState = viewModel.uiState.value as HomeUiState.Error
        assertEquals(errorMessage, errorState.message)
    }

    @Test
    fun `encodeSongToBase64 should return a valid base64 string`() {
        // Given
        val song = Song(trackId = 1L, trackName = "Bohemian Rhapsody", artistName = "Queen", collectionName = "A Night at the Opera", artworkUrl100 = "url", previewUrl = "preview", trackTimeMillis = 350000L)

        // When
        val result = viewModel.encodeSongToBase64(song)

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }
}
