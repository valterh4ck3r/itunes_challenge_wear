package com.valternegreiros.itunes_challenge_wear.data.remote.mapper

import com.valternegreiros.itunes_challenge_wear.data.remote.dto.TrackDto
import com.valternegreiros.itunes_challenge_wear.domain.model.Song

object SongDtoMapper {

    fun TrackDto.toDomain(): Song? {
        val id = trackId ?: return null
        return Song(
            trackId = id,
            collectionId = collectionId,
            trackName = trackName ?: "Unknown",
            artistName = artistName ?: "Unknown",
            collectionName = collectionName,
            artworkUrl100 = artworkUrl100,
            previewUrl = previewUrl,
            trackTimeMillis = trackTimeMillis
        )
    }

    fun List<TrackDto>.toDomainList(): List<Song> {
        return mapNotNull { it.toDomain() }
    }
}
