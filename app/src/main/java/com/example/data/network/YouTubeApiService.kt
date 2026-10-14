package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("videos")
    suspend fun getVideos(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("id") id: String,
        @Query("key") apiKey: String
    ): Response<YouTubeVideoListResponse>

    @GET("videos")
    suspend fun getMostPopularVideos(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("chart") chart: String = "mostPopular",
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): Response<YouTubeVideoListResponse>

    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): Response<YouTubeSearchResponse>

    @GET("channels")
    suspend fun getChannelDetails(
        @Query("part") part: String = "snippet,statistics,brandingSettings",
        @Query("id") id: String,
        @Query("key") apiKey: String
    ): Response<YouTubeChannelResponse>
}

@JsonClass(generateAdapter = true)
data class YouTubeVideoListResponse(
    @Json(name = "items") val items: List<YouTubeVideoItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "snippet") val snippet: YouTubeVideoSnippetDto?,
    @Json(name = "statistics") val statistics: YouTubeVideoStatisticsDto?,
    @Json(name = "contentDetails") val contentDetails: YouTubeVideoContentDetailsDto?
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoSnippetDto(
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "channelId") val channelId: String = "",
    @Json(name = "channelTitle") val channelTitle: String = "",
    @Json(name = "publishedAt") val publishedAt: String = "",
    @Json(name = "thumbnails") val thumbnails: YouTubeThumbnailsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailsDto(
    @Json(name = "high") val high: YouTubeThumbnailDto?,
    @Json(name = "medium") val medium: YouTubeThumbnailDto?,
    @Json(name = "default") val defaultThumb: YouTubeThumbnailDto?
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailDto(
    @Json(name = "url") val url: String = ""
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoStatisticsDto(
    @Json(name = "viewCount") val viewCount: String? = "0",
    @Json(name = "likeCount") val likeCount: String? = "0"
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoContentDetailsDto(
    @Json(name = "duration") val duration: String = "PT5M"
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(
    @Json(name = "items") val items: List<YouTubeSearchItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchItemDto(
    @Json(name = "id") val id: YouTubeSearchIdDto?,
    @Json(name = "snippet") val snippet: YouTubeVideoSnippetDto?
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchIdDto(
    @Json(name = "videoId") val videoId: String?,
    @Json(name = "channelId") val channelId: String?
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelResponse(
    @Json(name = "items") val items: List<YouTubeChannelItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "snippet") val snippet: YouTubeChannelSnippetDto?,
    @Json(name = "statistics") val statistics: YouTubeChannelStatisticsDto?
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelSnippetDto(
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "customUrl") val customUrl: String? = "",
    @Json(name = "thumbnails") val thumbnails: YouTubeThumbnailsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelStatisticsDto(
    @Json(name = "subscriberCount") val subscriberCount: String? = "0",
    @Json(name = "videoCount") val videoCount: String? = "0"
)
