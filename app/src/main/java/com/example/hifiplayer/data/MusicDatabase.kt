package com.example.hifiplayer.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Track::class, Playlist::class, PlaylistTrackCrossRef::class],
    version = 2,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
