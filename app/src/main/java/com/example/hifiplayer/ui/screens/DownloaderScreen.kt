package com.example.hifiplayer.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hifiplayer.ui.viewmodels.InstallerViewModel
import com.example.hifiplayer.downloader.DownloadWorker
import com.example.hifiplayer.network.YTTrack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderScreen(
    viewModel: InstallerViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Echo Downloader", fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search YouTube Music") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.search(searchQuery)
                        }
                    }) {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Results", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(results) { track ->
                    ListItem(
                        headlineContent = { Text(track.title) },
                        supportingContent = { Text(track.artist) },
                        trailingContent = {
                            IconButton(onClick = {
                                startDownload(context, track)
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Download")
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun startDownload(context: Context, track: YTTrack) {
    val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
        .setInputData(
            Data.Builder()
                .putString("VIDEO_ID", track.videoId)
                .putString("TITLE", track.title)
                .putString("ARTIST", track.artist)
                .putString("THUMBNAIL", track.thumbnail)
                .build()
        )
        .build()
    WorkManager.getInstance(context).enqueue(workRequest)
}
