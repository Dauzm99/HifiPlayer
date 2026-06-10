package com.example.hifiplayer.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YTMusicService @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            url("https://music.youtube.com/youtubei/v1/")
            header("Content-Type", "application/json")
            header("User-Agent", "Mozilla/5.0 (Android 14; Mobile; rv:128.0) Gecko/128.0 Firefox/128.0")
        }
    }

    // This key is a public InnerTube key used by web/mobile clients
    private val apiKey = "AIzaSyC1-8M882882882882882882882882882" // Placeholder, in real app extract from page

    suspend fun search(query: String): List<YTTrack> {
        val requestBody = SearchRequest(
            context = InnerTubeContext(ClientInfo()),
            query = query
        )

        val response: SearchResponse = client.post("search") {
            parameter("key", apiKey)
            setBody(requestBody)
        }.body()

        return mapSearchResponse(response)
    }

    suspend fun getStreamUrl(videoId: String): String? {
        val requestBody = PlayerRequest(
            context = InnerTubeContext(ClientInfo()),
            videoId = videoId
        )

        val response: PlayerResponse = client.post("player") {
            parameter("key", apiKey)
            setBody(requestBody)
        }.body()

        // Prefer highest bitrate audio-only adaptive format
        return response.streamingData?.adaptiveFormats
            ?.filter { it.mimeType.contains("audio") }
            ?.maxByOrNull { it.bitrate }
            ?.url
    }

    private fun mapSearchResponse(response: SearchResponse): List<YTTrack> {
        val tracks = mutableListOf<YTTrack>()
        val shelf = response.contents?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()
            ?.content?.sectionListRenderer?.contents?.mapNotNull { it.musicShelfRenderer }?.firstOrNull()

        shelf?.contents?.forEach { item ->
            val renderer = item.musicResponsiveListItemRenderer ?: return@forEach
            val videoId = renderer.videoId ?: return@forEach
            val title = renderer.title?.runs?.firstOrNull()?.text ?: "Unknown"
            val artist = renderer.flexColumns?.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text ?: "Unknown"
            val thumbnail = renderer.thumbnails?.thumbnails?.maxByOrNull { it.width * it.height }?.url

            tracks.add(YTTrack(videoId, title, artist, thumbnail))
        }
        return tracks
    }
}

data class YTTrack(
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnail: String?
)
