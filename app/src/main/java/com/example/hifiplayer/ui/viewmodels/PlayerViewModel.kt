package com.example.hifiplayer.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.hifiplayer.data.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val player: ExoPlayer
) : ViewModel() {

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    val primaryColor = mutableStateOf(Color(0xFFD0BCFF)) // Purple80 default

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    _duration.value = player.duration
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // In a real app, map back from mediaId to Track
            }
        })

        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition
                }
                delay(1000)
            }
        }
    }

    fun playTrack(track: Track) {
        _currentTrack.value = track
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(Uri.parse(track.albumArtUri))
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(track.uri)
            .setMediaId(track.id.toString())
            .setMediaMetadata(mediaMetadata)
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    override fun onCleared() {
        super.onCleared()
        // Player release is handled in PlaybackService
    }
}
