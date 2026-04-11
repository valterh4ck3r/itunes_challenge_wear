package com.valternegreiros.itunes_challenge_wear.ui.features.main

import com.valternegreiros.itunes_challenge_wear.MainDispatcherRule
import com.valternegreiros.itunes_challenge_wear.domain.model.Song
import com.valternegreiros.itunes_challenge_wear.domain.repository.HomeRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainNavigationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: HomeRepository = mockk()
    private val player: androidx.media3.exoplayer.ExoPlayer = mockk(relaxed = true)
    private lateinit var viewModel: MainNavigationViewModel

    @Before
    fun setUp() {
        every { player.currentMediaItem } returns null
    }

    @Test
    fun `lastPlayedSong should be updated with recently played song`() = runTest {
        // Given
        val song = Song(trackId = 1, trackName = "Song 1", artistName = "Artist 1", collectionName = "Album 1", artworkUrl100 = null, previewUrl = null, trackTimeMillis = null)
        every { repository.getRecentlyPlayedSongs() } returns flowOf(listOf(song))
        every { repository.getAllCachedSongs(1) } returns flowOf(emptyList())

        // When
        viewModel = MainNavigationViewModel(repository, player)

        // Then
        assertEquals(song, viewModel.lastPlayedSong.value)
    }

    @Test
    fun `getEncodedLastSong should return base64 string`() = runTest {
        // Given
        val song = Song(trackId = 1, trackName = "Song 1", artistName = "Artist 1", collectionName = "Album 1", artworkUrl100 = null, previewUrl = null, trackTimeMillis = null)
        every { repository.getRecentlyPlayedSongs() } returns flowOf(listOf(song))
        every { repository.getAllCachedSongs(1) } returns flowOf(emptyList())

        viewModel = MainNavigationViewModel(repository, player)

        // When
        val result = viewModel.getEncodedLastSong()

        // Then
        assertNotNull(result)
    }
}
