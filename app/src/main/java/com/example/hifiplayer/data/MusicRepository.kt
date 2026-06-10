package com.example.hifiplayer.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao
) {
    val allTracks: Flow<List<Track>> = trackDao.getAllTracks()

    private val _musicDirectory = MutableStateFlow<String?>(null)
    val musicDirectory = _musicDirectory.asStateFlow()

    fun setMusicDirectory(uriString: String) {
        _musicDirectory.value = uriString
        // Store in SharedPreferences or DataStore for persistence
        context.getSharedPreferences("hifi_prefs", Context.MODE_PRIVATE)
            .edit().putString("music_dir", uriString).apply()
    }

    init {
        _musicDirectory.value = context.getSharedPreferences("hifi_prefs", Context.MODE_PRIVATE)
            .getString("music_dir", null)
    }

    val allPlaylists: Flow<List<Playlist>> = trackDao.getAllPlaylists()

    suspend fun createPlaylist(name: String) = withContext(Dispatchers.IO) {
        trackDao.insertPlaylist(Playlist(name = name))
    }

    suspend fun addTrackToPlaylist(trackId: Long, playlistId: Long) = withContext(Dispatchers.IO) {
        trackDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, trackId))
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> {
        return trackDao.getTracksForPlaylist(playlistId)
    }

    suspend fun refreshLibrary() = withContext(Dispatchers.IO) {
        val currentDir = _musicDirectory.value
        if (currentDir == null) {
            scanMediaStore()
        } else {
            scanCustomDirectory(currentDir)
        }
    }

    private suspend fun scanMediaStore() {
        val tracks = mutableListOf<Track>()
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Scan for all audio files
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(dataColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val albumArtUri = ContentUris.withAppendedId(
                    android.net.Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                tracks.add(
                    Track(
                        id = id,
                        title = title,
                        artist = artist,
                        albumArtist = artist,
                        album = album,
                        duration = duration,
                        path = path,
                        uri = contentUri.toString(),
                        albumArtUri = albumArtUri.toString(),
                        format = "MP3/FLAC",
                        bitDepth = 16,
                        sampleRate = 44100,
                        year = null,
                        genre = null,
                        isrc = null
                    )
                )
            }
        }

        if (tracks.isNotEmpty()) {
            trackDao.deleteAll()
            trackDao.insertAll(tracks)
        }
    }

    private suspend fun scanCustomDirectory(uriString: String) {
        val tracks = mutableListOf<Track>()
        val rootUri = android.net.Uri.parse(uriString)
        val rootDoc = DocumentFile.fromTreeUri(context, rootUri) ?: return
        
        // Recursively scan rootDoc for audio files
        scanDocument(rootDoc, tracks)

        if (tracks.isNotEmpty()) {
            trackDao.deleteAll()
            trackDao.insertAll(tracks)
        }
    }

    private fun scanDocument(document: DocumentFile, tracks: MutableList<Track>) {
        if (document.isDirectory) {
            document.listFiles().forEach { scanDocument(it, tracks) }
        } else {
            val mime = document.type ?: ""
            if (mime.startsWith("audio/")) {
                // Extract metadata using MediaMetadataRetriever and add to tracks
                // For brevity, using a placeholder similar to scanMediaStore
                tracks.add(
                    Track(
                        id = document.uri.hashCode().toLong(),
                        title = document.name ?: "Unknown",
                        artist = "Unknown",
                        albumArtist = "Unknown",
                        album = "Local Folder",
                        duration = 0,
                        path = document.uri.toString(),
                        uri = document.uri.toString(),
                        albumArtUri = null,
                        format = document.type ?: "Unknown",
                        bitDepth = 16,
                        sampleRate = 44100,
                        year = null,
                        genre = null,
                        isrc = null
                    )
                )
            }
        }
    }
}
