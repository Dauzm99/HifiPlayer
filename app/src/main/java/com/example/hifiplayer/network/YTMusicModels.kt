package com.example.hifiplayer.network

import kotlinx.serialization.Serializable

@Serializable
data class InnerTubeContext(
    val client: ClientInfo
)

@Serializable
data class ClientInfo(
    val clientName: String = "ANDROID_MUSIC",
    val clientVersion: String = "6.45.54"
)

@Serializable
data class SearchRequest(
    val context: InnerTubeContext,
    val query: String
)

@Serializable
data class PlayerRequest(
    val context: InnerTubeContext,
    val videoId: String
)

// Simplified response models for mapping
@Serializable
data class SearchResponse(
    val contents: SearchContents? = null
)

@Serializable
data class SearchContents(
    val tabbedSearchResultsRenderer: TabbedResults? = null
)

@Serializable
data class TabbedResults(
    val tabs: List<SearchTab>? = null
)

@Serializable
data class SearchTab(
    val content: TabContent? = null
)

@Serializable
data class TabContent(
    val sectionListRenderer: SectionList? = null
)

@Serializable
data class SectionList(
    val contents: List<SectionContent>? = null
)

@Serializable
data class SectionContent(
    val musicShelfRenderer: MusicShelf? = null
)

@Serializable
data class MusicShelf(
    val contents: List<ShelfItem>? = null
)

@Serializable
data class ShelfItem(
    val musicResponsiveListItemRenderer: ListItemRenderer? = null
)

@Serializable
data class ListItemRenderer(
    val videoId: String? = null,
    val title: TextWrapper? = null,
    val flexColumns: List<FlexColumn>? = null,
    val thumbnails: ThumbnailList? = null
)

@Serializable
data class TextWrapper(
    val runs: List<TextRun>? = null
)

@Serializable
data class TextRun(
    val text: String
)

@Serializable
data class FlexColumn(
    val musicResponsiveListItemFlexColumnRenderer: FlexColumnRenderer? = null
)

@Serializable
data class FlexColumnRenderer(
    val text: TextWrapper? = null
)

@Serializable
data class ThumbnailList(
    val thumbnails: List<Thumbnail>? = null
)

@Serializable
data class Thumbnail(
    val url: String,
    val width: Int,
    val height: Int
)

@Serializable
data class PlayerResponse(
    val streamingData: StreamingData? = null
)

@Serializable
data class StreamingData(
    val formats: List<Format>? = null,
    val adaptiveFormats: List<Format>? = null
)

@Serializable
data class Format(
    val itag: Int,
    val mimeType: String,
    val bitrate: Int,
    val url: String? = null,
    val signatureCipher: String? = null,
    val contentLength: Long? = null,
    val audioQuality: String? = null,
    val approxDurationMs: String? = null
)
