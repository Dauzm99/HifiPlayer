package com.example.hifiplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hifiplayer.data.Track
import com.example.hifiplayer.ui.viewmodels.MusicViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MusicViewModel,
    onTrackClick: (Track) -> Unit,
    onSetFolderClick: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val tracks by viewModel.allTracks.collectAsState()
    val artists by viewModel.allArtists.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tracks", "Artists", "Albums", "Playlists")

    var filterByArtist by remember { mutableStateOf<String?>(null) }
    var filterByAlbum by remember { mutableStateOf<String?>(null) }

    val displayedTracks = remember(tracks, filterByArtist, filterByAlbum) {
        tracks.filter { track ->
            (filterByArtist == null || track.artist == filterByArtist) &&
            (filterByAlbum == null || track.album == filterByAlbum)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        val title = when {
                            filterByArtist != null -> filterByArtist!!
                            filterByAlbum != null -> filterByAlbum!!
                            else -> "Echo Library"
                        }
                        Text(title, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        if (filterByArtist != null || filterByAlbum != null) {
                            IconButton(onClick = {
                                filterByArtist = null
                                filterByAlbum = null
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onSetFolderClick) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Set Music Folder")
                        }
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                        IconButton(onClick = onNavigateToAbout) {
                            Icon(Icons.Default.Info, contentDescription = "About")
                        }
                    }
                )
                if (filterByArtist == null && filterByAlbum == null) {
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (filterByArtist != null || filterByAlbum != null) {
                TrackList(displayedTracks, onTrackClick)
            } else {
                when (selectedTab) {
                    0 -> TrackList(tracks, onTrackClick)
                    1 -> CategoryList(artists) { filterByArtist = it }
                    2 -> CategoryList(albums) { filterByAlbum = it }
                    3 -> PlaylistList()
                }
            }
        }
    }
}

@Composable
fun TrackList(tracks: List<Track>, onTrackClick: (Track) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tracks) { track ->
            TrackItem(
                title = track.title,
                artist = track.artist,
                onClick = { onTrackClick(track) }
            )
        }
    }
}

@Composable
fun CategoryList(items: List<String>, onItemClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items) { item ->
            ListItem(
                headlineContent = { Text(item) },
                modifier = Modifier.clickable { onItemClick(item) }
            )
        }
    }
}

@Composable
fun PlaylistList() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No Playlists yet", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = { /* TODO: Create Playlist */ }, modifier = Modifier.padding(16.dp)) {
            Text("Create Playlist")
        }
    }
}

@Composable
fun TrackItem(title: String, artist: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder for Album Art
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {}

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
