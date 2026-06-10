package com.example.hifiplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val albumArtist: String?,
    val album: String,
    val duration: Long,
    val path: String,
    val uri: String,
    val albumArtUri: String?,
    val format: String = "FLAC",
    val bitDepth: Int = 24,
    val sampleRate: Int = 96000,
    val year: String? = null,
    val genre: String? = null,
    val isrc: String? = null
)
