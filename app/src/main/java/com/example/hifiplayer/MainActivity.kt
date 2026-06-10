package com.example.hifiplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hifiplayer.ui.screens.AboutScreen
import com.example.hifiplayer.ui.screens.DownloaderScreen
import com.example.hifiplayer.ui.screens.MainScreen
import com.example.hifiplayer.ui.screens.NowPlayingScreen
import com.example.hifiplayer.ui.theme.HiFiPlayerTheme
import com.example.hifiplayer.ui.viewmodels.MusicViewModel
import com.example.hifiplayer.ui.viewmodels.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HiFiPlayerTheme {
                val navController = rememberNavController()
                val musicViewModel: MusicViewModel = hiltViewModel()
                val playerViewModel: PlayerViewModel = hiltViewModel()
                
                val directoryPicker = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocumentTree()
                ) { uri ->
                    uri?.let {
                        contentResolver.takePersistableUriPermission(
                            it,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        musicViewModel.setMusicDirectory(it.toString())
                    }
                }

                val currentTrack by playerViewModel.currentTrack.collectAsState()
                val isPlaying by playerViewModel.isPlaying.collectAsState()
                val currentPosition by playerViewModel.currentPosition.collectAsState()
                val duration by playerViewModel.duration.collectAsState()
                val sheetState = rememberBottomSheetScaffoldState()
                val scope = rememberCoroutineScope()

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination?.route

                            NavigationBarItem(
                                icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                                label = { Text("Library") },
                                selected = currentDestination == "main",
                                onClick = {
                                    navController.navigate("main") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Download, contentDescription = "Downloader") },
                                label = { Text("Downloader") },
                                selected = currentDestination == "downloader",
                                onClick = {
                                    navController.navigate("downloader") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                ) { outerPadding ->
                    BottomSheetScaffold(
                        scaffoldState = sheetState,
                        sheetContent = {
                            NowPlayingScreen(
                                track = currentTrack,
                                isPlaying = isPlaying,
                                position = currentPosition,
                                duration = duration,
                                onPlayPause = { playerViewModel.togglePlayPause() },
                                onSeek = { playerViewModel.seekTo(it) },
                                primaryColor = playerViewModel.primaryColor
                            )
                        },
                        sheetPeekHeight = if (currentTrack != null) 72.dp else 0.dp,
                        modifier = Modifier
                            .padding(outerPadding)
                            .clickable(
                                enabled = currentTrack != null,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                scope.launch {
                                    sheetState.bottomSheetState.expand()
                                }
                            }
                    ) { paddingValues ->
                        Surface(
                            modifier = Modifier.padding(paddingValues),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            NavHost(navController = navController, startDestination = "main") {
                                composable("main") {
                                    MainScreen(
                                        viewModel = musicViewModel,
                                        onTrackClick = { track ->
                                            playerViewModel.playTrack(track)
                                        },
                                        onSetFolderClick = { directoryPicker.launch(null) },
                                        onNavigateToAbout = { navController.navigate("about") }
                                    )
                                }
                                composable("downloader") {
                                    DownloaderScreen()
                                }
                                composable("about") {
                                    AboutScreen(
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
