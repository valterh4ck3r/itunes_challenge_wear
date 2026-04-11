package com.valternegreiros.itunes_challenge_wear.data.repository

import android.content.Context
import com.valternegreiros.itunes_challenge_wear.data.local.dao.SongDao
import com.valternegreiros.itunes_challenge_wear.data.local.mapper.SongEntityMapper.toDomainList
import com.valternegreiros.itunes_challenge_wear.data.local.mapper.SongEntityMapper.toEntity
import com.valternegreiros.itunes_challenge_wear.data.local.mapper.SongEntityMapper.toEntityList
import com.valternegreiros.itunes_challenge_wear.data.remote.api.ITunesApiService
import com.valternegreiros.itunes_challenge_wear.data.remote.mapper.SongDtoMapper.toDomainList
import com.valternegreiros.itunes_challenge_wear.domain.model.ResponseState
import com.valternegreiros.itunes_challenge_wear.domain.model.Song
import com.valternegreiros.itunes_challenge_wear.domain.repository.HomeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val apiService: ITunesApiService,
    private val songDao: SongDao,
    @ApplicationContext private val context: Context,
    @com.valternegreiros.itunes_challenge_wear.di.IoDispatcher private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher
) : HomeRepository {

    /**
     * Offline-first search:
     * 1. Emit cached results immediately if available
     * 2. Fetch from network in background
     * 3. If network fails and we had no cache, emit failure
     */
    override fun searchSongs(
        term: String,
        limit: Int,
        offset: Int,
        forceRemote: Boolean
    ): Flow<ResponseState<List<Song>>> = flow {
        emit(ResponseState.Loading)

        // 1. Try to fetch from network
        try {

            val response = apiService.searchSongs(
                term = term,
                limit = limit,
                offset = offset
            )
            val songs = response.results.toDomainList()

            // 2. Cache successfully fetched songs
            songDao.insertAll(songs.toEntityList())

            emit(ResponseState.Success(songs))
        } catch (e: Exception) {
            // 3. On failure, try to emit from cache
            val cachedSongs = songDao.searchSongs(term, limit, offset).map { it.toDomainList() }
            
            // For search, we just take the first emission from local DB
            var emittedFromCache = false
            cachedSongs.collect { songs ->
                if (songs.isNotEmpty() && !emittedFromCache) {
                    emit(ResponseState.Success(songs))
                    emittedFromCache = true
                } else if (!emittedFromCache) {
                    val statusCode = (e as? HttpException)?.code()
                    emit(ResponseState.Error(statusCode = statusCode, message = e.message ?: "Network error"))
                    emittedFromCache = true
                }
            }
        }
    }.flowOn(ioDispatcher)

    override fun getAlbumTracks(collectionId: Long): Flow<ResponseState<List<Song>>> = flow {
        emit(ResponseState.Loading)
        try {
            val response = apiService.lookupByCollectionId(collectionId)
            // Filter out the album result, keep only songs
            val songs = response.results.toDomainList()
            
            // Cache album tracks
            songDao.insertAll(songs.toEntityList())
            
            emit(ResponseState.Success(songs))
        } catch (e: Exception) {
            // Fallback to cache by collectionId if possible (need a way to query by collectionId in DAO)
            // For now, emit the error as the DAO doesn't have a specific getTracksByCollectionId flow yet
            val statusCode = (e as? HttpException)?.code()
            emit(ResponseState.Error(statusCode = statusCode, message = e.message ?: "Network error"))
        }
    }.flowOn(ioDispatcher)

    override fun getRecentlyPlayedSongs(): Flow<List<Song>> {
        return songDao.getRecentlyPlayed().map { entities ->
            entities.toDomainList()
        }.flowOn(ioDispatcher)
    }

    override fun getAllCachedSongs(limit: Int): Flow<List<Song>> {
        return songDao.getAllCachedSongs(limit).map { entities ->
            entities.toDomainList()
        }.flowOn(ioDispatcher)
    }

    override suspend fun markSongAsPlayed(song: Song) {
        val now = System.currentTimeMillis()

        // 1. Fetch current stored data for this song from DB to keep local path
        val existingEntity = songDao.getSongById(song.trackId)

        // 2. Prepare the new entity with current timestamp and existing local path
        var updatedEntity = song.toEntity().copy(
            lastPlayedAt = now,
            previewUrlLocal = existingEntity?.previewUrlLocal ?: song.previewUrlLocal
        )

        // 3. Only download if we don't have it locally or the file was deleted
        val needsDownload = song.previewUrl != null && (
            updatedEntity.previewUrlLocal == null ||
            !File(updatedEntity.previewUrlLocal).exists()
        )

        if (needsDownload) {
            val localPath = downloadAndSavePreview(song.trackId, song.previewUrl)
            if (localPath != null) {
                updatedEntity = updatedEntity.copy(previewUrlLocal = localPath)
            }
        }

        songDao.insertAll(listOf(updatedEntity))
    }

    private suspend fun downloadAndSavePreview(trackId: Long, previewUrl: String): String? {
        return withContext(ioDispatcher) {
            try {
                val response = apiService.downloadFile(previewUrl)
                if (response.isSuccessful) {
                    val body = response.body() ?: return@withContext null
                    val file = File(context.filesDir, "previews/$trackId.m4a")
                    file.parentFile?.mkdirs()

                    body.byteStream().use { inputStream ->
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    file.absolutePath
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun clearRecentlyPlayed() {
        songDao.clearRecentlyPlayed()
    }
}
