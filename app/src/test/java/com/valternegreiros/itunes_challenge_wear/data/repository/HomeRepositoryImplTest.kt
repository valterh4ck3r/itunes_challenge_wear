package com.valternegreiros.itunes_challenge_wear.data.repository

import android.content.Context
import com.valternegreiros.itunes_challenge_wear.data.local.dao.SongDao
import com.valternegreiros.itunes_challenge_wear.data.remote.api.ITunesApiService
import com.valternegreiros.itunes_challenge_wear.data.remote.dto.ITunesSearchResponse
import com.valternegreiros.itunes_challenge_wear.data.remote.dto.TrackDto
import com.valternegreiros.itunes_challenge_wear.domain.model.ResponseState
import com.valternegreiros.itunes_challenge_wear.domain.model.Song
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeRepositoryImplTest {

    private lateinit var repository: HomeRepositoryImpl
    private val apiService: ITunesApiService = mockk()
    private val songDao: SongDao = mockk()
    private val context: Context = mockk()
    private val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        repository = HomeRepositoryImpl(apiService, songDao, context, testDispatcher)
    }

    @Test
    fun `searchSongs should emit loading and then success when network call is successful`() = runTest {
        // Given
        val term = "Ed Sheeran"
        val trackDto = TrackDto(
            trackId = 1L,
            trackName = "Shape of You",
            artistName = "Ed Sheeran",
            collectionName = "Divide",
            artworkUrl100 = "url",
            previewUrl = "preview",
            trackTimeMillis = 200000L,
            collectionId = 10L
        )
        val response = ITunesSearchResponse(resultCount = 1, results = listOf(trackDto))
        
        coEvery { apiService.searchSongs(term, limit = 20, offset = 0) } returns response
        coEvery { songDao.insertAll(any()) } returns Unit

        // When
        val results = mutableListOf<ResponseState<List<Song>>>()
        repository.searchSongs(term, limit = 20, offset = 0, forceRemote = false).collect {
            results.add(it)
        }

        // Then
        assertEquals(2, results.size)
        assertTrue(results[0] is ResponseState.Loading)
        assertTrue(results[1] is ResponseState.Success)
        val successState = results[1] as ResponseState.Success
        assertEquals(1, successState.data.size)
        assertEquals("Shape of You", successState.data[0].trackName)
        
        coVerify { songDao.insertAll(any()) }
    }

    @Test
    fun `searchSongs should emit success from cache when network call fails`() = runTest {
        // Given
        val term = "Ed Sheeran"
        coEvery { apiService.searchSongs(term, limit = 20, offset = 0) } throws Exception("Network error")
        
        val cachedSongs = listOf(
            com.valternegreiros.itunes_challenge_wear.data.local.entity.SongEntity(
                trackId = 1L,
                trackName = "Shape of You",
                artistName = "Ed Sheeran",
                collectionName = "Divide",
                artworkUrl100 = "url",
                previewUrl = "preview",
                trackTimeMillis = 200000L,
                collectionId = 10L,
                cachedAt = System.currentTimeMillis()
            )
        )
        coEvery { songDao.searchSongs(term, limit = 20, offset = 0) } returns flowOf(cachedSongs)

        // When
        val results = mutableListOf<ResponseState<List<Song>>>()
        repository.searchSongs(term, limit = 20, offset = 0, forceRemote = false).collect {
            results.add(it)
        }

        // Then
        assertEquals(2, results.size)
        assertTrue(results[0] is ResponseState.Loading)
        assertTrue(results[1] is ResponseState.Success)
        val successState = results[1] as ResponseState.Success
        assertEquals(1, successState.data.size)
        assertEquals("Shape of You", successState.data[0].trackName)
    }

    @Test
    fun `getRecentlyPlayedSongs should return songs from dao`() = runTest {
        // Given
        val cachedSongs = listOf(
            com.valternegreiros.itunes_challenge_wear.data.local.entity.SongEntity(
                trackId = 1L,
                trackName = "Shape of You",
                artistName = "Ed Sheeran",
                collectionName = "Divide",
                artworkUrl100 = "url",
                previewUrl = "preview",
                trackTimeMillis = 200000L,
                collectionId = 10L,
                cachedAt = System.currentTimeMillis()
            )
        )
        coEvery { songDao.getRecentlyPlayed() } returns flowOf(cachedSongs)

        // When
        val results = mutableListOf<List<Song>>()
        repository.getRecentlyPlayedSongs().collect {
            results.add(it)
        }

        // Then
        assertEquals(1, results.size)
        assertEquals(1, results[0].size)
        assertEquals("Shape of You", results[0][0].trackName)
    }
}
