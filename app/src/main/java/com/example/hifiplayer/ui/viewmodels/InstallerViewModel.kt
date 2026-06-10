package com.example.hifiplayer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hifiplayer.network.YTMusicService
import com.example.hifiplayer.network.YTTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstallerViewModel @Inject constructor(
    private val ytMusicService: YTMusicService
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<YTTrack>>(emptyList())
    val searchResults: StateFlow<List<YTTrack>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    fun search(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = ytMusicService.search(query)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearching.value = false
            }
        }
    }
}
