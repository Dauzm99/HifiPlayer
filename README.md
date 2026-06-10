# HiFiPlayer - High-Fidelity Android Music Player

A modern, high-performance Android music player designed for audiophiles, featuring seamless YouTube Music integration, offline playback, and a dynamic Material 3 interface.

## 🚀 Key Features
- **High-Quality Streaming:** Search and stream high-bitrate audio directly from YouTube Music.
- **Background Playback:** Robust media service using Media3 (ExoPlayer) with full notification and lock-screen controls.
- **Advanced Downloader:** Multi-stage background downloading with WorkManager, including automatic ID3 tagging.
- **Dynamic Theming:** UI colors automatically adapt to the current album art using the Palette API.
- **Hybrid Library:** Seamlessly manages music from the Android MediaStore and custom user-selected folders (SAF).
- **Playlist Management:** Full CRUD support for local playlists stored via Room.

## 🛠 Technical Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3, Palette API
- **Media:** Media3 (ExoPlayer), MediaSession
- **Networking:** Ktor, Kotlinx Serialization
- **Architecture:** MVVM, Clean Architecture, Repository Pattern
- **DI:** Hilt (Dagger)
- **Database:** Room Persistence Library
- **Background Tasks:** WorkManager, HiltWorker
- **Storage:** Scoped Storage, Storage Access Framework (SAF)

---


