package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
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

    /**
     * Fetches user's subscribed channels using authorized OAuth token.
     * Header: "Authorization: Bearer <oauth_token>"
     */
    @GET("subscriptions")
    suspend fun getMySubscriptions(
        @Header("Authorization") authHeader: String,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("mine") mine: Boolean = true,
        @Query("maxResults") maxResults: Int = 50,
        @Query("order") order: String = "alphabetical",
        @Query("pageToken") pageToken: String? = null
    ): Response<YouTubeSubscriptionListResponse>

    /**
     * Fetches playlist items (e.g. channel uploads playlist "UU...") using OAuth token or API key.
     */
    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Header("Authorization") authHeader: String? = null,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String? = null
    ): Response<YouTubePlaylistItemListResponse>

    /**
     * Fetches the user's activity feed (recent videos from subscribed channels).
     */
    @GET("activities")
    suspend fun getMyActivities(
        @Header("Authorization") authHeader: String,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("mine") mine: Boolean = true,
        @Query("maxResults") maxResults: Int = 50
    ): Response<YouTubeActivitiesResponse>
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

@JsonClass(generateAdapter = true)
data class YouTubeSubscriptionListResponse(
    @Json(name = "items") val items: List<YouTubeSubscriptionItemDto> = emptyList(),
    @Json(name = "nextPageToken") val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSubscriptionItemDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "snippet") val snippet: YouTubeSubscriptionSnippetDto?,
    @Json(name = "contentDetails") val contentDetails: YouTubeSubscriptionContentDetailsDto?
)

@JsonClass(generateAdapter = true)
data class YouTubeSubscriptionSnippetDto(
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "resourceId") val resourceId: YouTubeResourceIdDto?,
    @Json(name = "publishedAt") val publishedAt: String = "",
    @Json(name = "thumbnails") val thumbnails: YouTubeThumbnailsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeResourceIdDto(
    @Json(name = "kind") val kind: String = "",
    @Json(name = "channelId") val channelId: String = ""
)

@JsonClass(generateAdapter = true)
data class YouTubeSubscriptionContentDetailsDto(
    @Json(name = "totalItemCount") val totalItemCount: Long? = 0L,
    @Json(name = "newItemCount") val newItemCount: Long? = 0L,
    @Json(name = "activityType") val activityType: String? = ""
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemListResponse(
    @Json(name = "items") val items: List<YouTubePlaylistItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "snippet") val snippet: YouTubePlaylistItemSnippetDto?,
    @Json(name = "contentDetails") val contentDetails: YouTubePlaylistItemContentDetailsDto?
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemSnippetDto(
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "channelId") val channelId: String = "",
    @Json(name = "channelTitle") val channelTitle: String = "",
    @Json(name = "publishedAt") val publishedAt: String = "",
    @Json(name = "thumbnails") val thumbnails: YouTubeThumbnailsDto? = null,
    @Json(name = "resourceId") val resourceId: YouTubePlaylistItemResourceIdDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemResourceIdDto(
    @Json(name = "videoId") val videoId: String = ""
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemContentDetailsDto(
    @Json(name = "videoId") val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeActivitiesResponse(
    @Json(name = "items") val items: List<YouTubeActivityItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeActivityItemDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "snippet") val snippet: YouTubeVideoSnippetDto?,
    @Json(name = "contentDetails") val contentDetails: YouTubeActivityContentDetailsDto?
)

@JsonClass(generateAdapter = true)
data class YouTubeActivityContentDetailsDto(
    @Json(name = "upload") val upload: YouTubeActivityUploadDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeActivityUploadDto(
    @Json(name = "videoId") val videoId: String? = null
)
