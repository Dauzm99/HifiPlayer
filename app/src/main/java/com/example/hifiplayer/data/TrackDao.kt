package com.example.hifiplayer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT DISTINCT artist FROM tracks ORDER BY artist ASC")
    fun getAllArtists(): Flow<List<String>>

    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY album ASC, title ASC")
    fun getTracksByArtist(artist: String): Flow<List<Track>>

    @Query("SELECT DISTINCT album FROM tracks ORDER BY album ASC")
    fun getAllAlbums(): Flow<List<String>>

    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY title ASC")
    fun getTracksByAlbum(album: String): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<Track>)

    @Query("DELETE FROM tracks")
    suspend fun deleteAll()

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Long): Track?

    // Playlist Queries
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Transaction
    @Query("""
        SELECT t.* FROM tracks t 
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId 
        WHERE pt.playlistId = :playlistId 
        ORDER BY t.title ASC
    """)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>>
}
