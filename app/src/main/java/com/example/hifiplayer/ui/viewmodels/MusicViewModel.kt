package com.example.hifiplayer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hifiplayer.data.MusicRepository
import com.example.hifiplayer.data.Track
import com.example.hifiplayer.data.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    val allTracks: StateFlow<List<Track>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArtists: StateFlow<List<String>> = repository.allTracks
        .map { tracks -> tracks.map { it.artist }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlbums: StateFlow<List<String>> = repository.allTracks
        .map { tracks -> tracks.map { it.album }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val musicDirectory = repository.musicDirectory

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshLibrary()
        }
    }

    fun setMusicDirectory(uriString: String) {
        repository.setMusicDirectory(uriString)
        refresh()
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun addTrackToPlaylist(trackId: Long, playlistId: Long) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(trackId, playlistId)
        }
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> {
        return repository.getTracksForPlaylist(playlistId)
    }
}
