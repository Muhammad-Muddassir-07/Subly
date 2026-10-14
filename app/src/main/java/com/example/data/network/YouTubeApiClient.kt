package com.example.data.network

import com.example.data.local.ChannelEntity
import com.example.data.local.VideoEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val message: String,
        val statusCode: Int? = null,
        val isQuotaExceeded: Boolean = false,
        val isNetworkError: Boolean = false
    ) : ApiResult<Nothing>()
}

class YouTubeApiClient(private val baseUrl: String = "https://www.googleapis.com/youtube/v3/") {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val service: YouTubeApiService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(YouTubeApiService::class.java)

    suspend fun fetchPopularVideos(apiKey: String): ApiResult<List<VideoEntity>> {
        if (apiKey.isBlank()) {
            return ApiResult.Error("API Key is missing. Using local cached feed.", isNetworkError = false)
        }
        return try {
            val response = service.getMostPopularVideos(apiKey = apiKey.trim())
            if (response.isSuccessful) {
                val items = response.body()?.items ?: emptyList()
                val entities = items.map { it.toVideoEntity() }
                ApiResult.Success(entities)
            } else {
                handleErrorResponse(response.code(), response.errorBody()?.string())
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: Unable to reach YouTube servers. Showing cached videos.", isNetworkError = true)
        } catch (e: Exception) {
            ApiResult.Error("Failed to fetch videos: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun searchVideos(query: String, apiKey: String): ApiResult<List<VideoEntity>> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return ApiResult.Success(emptyList())
        }
        if (apiKey.isBlank()) {
            return ApiResult.Error("API key not configured for live search. Searching local library.", isNetworkError = false)
        }

        return try {
            val response = service.searchVideos(query = cleanQuery, apiKey = apiKey.trim())
            if (response.isSuccessful) {
                val items = response.body()?.items ?: emptyList()
                val entities = items.mapNotNull { searchItem ->
                    val videoId = searchItem.id?.videoId ?: return@mapNotNull null
                    val snippet = searchItem.snippet ?: return@mapNotNull null
                    val thumbUrl = snippet.thumbnails?.high?.url
                        ?: snippet.thumbnails?.medium?.url
                        ?: snippet.thumbnails?.defaultThumb?.url
                        ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800"

                    VideoEntity(
                        id = videoId,
                        title = snippet.title,
                        description = snippet.description,
                        thumbnailUrl = thumbUrl,
                        channelId = snippet.channelId,
                        channelTitle = snippet.channelTitle,
                        channelAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                        publishedAt = snippet.publishedAt,
                        duration = "10:00",
                        durationSeconds = 600,
                        viewCount = 0,
                        likeCount = 0,
                        category = "Search",
                        isLiked = false,
                        isWatchLater = false
                    )
                }
                ApiResult.Success(entities)
            } else {
                handleErrorResponse(response.code(), response.errorBody()?.string())
            }
        } catch (e: IOException) {
            ApiResult.Error("Network connection offline. Live search unavailable.", isNetworkError = true)
        } catch (e: Exception) {
            ApiResult.Error("Search error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchChannel(channelId: String, apiKey: String): ApiResult<ChannelEntity> {
        if (apiKey.isBlank()) {
            return ApiResult.Error("API key required to fetch channel online.", isNetworkError = false)
        }
        return try {
            val response = service.getChannelDetails(id = channelId, apiKey = apiKey.trim())
            if (response.isSuccessful) {
                val item = response.body()?.items?.firstOrNull()
                if (item != null) {
                    val snippet = item.snippet
                    val stats = item.statistics
                    val thumb = snippet?.thumbnails?.high?.url
                        ?: snippet?.thumbnails?.medium?.url
                        ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"

                    val entity = ChannelEntity(
                        id = item.id,
                        title = snippet?.title ?: "Channel",
                        handle = snippet?.customUrl ?: "@${snippet?.title?.lowercase()?.replace(" ", "")}",
                        avatarUrl = thumb,
                        bannerUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1000",
                        subscriberCount = stats?.subscriberCount?.toLongOrNull() ?: 0L,
                        videoCount = stats?.videoCount?.toIntOrNull() ?: 0,
                        description = snippet?.description ?: "",
                        isSubscribed = false,
                        unreadVideosCount = 0
                    )
                    ApiResult.Success(entity)
                } else {
                    ApiResult.Error("Channel not found", statusCode = 404)
                }
            } else {
                handleErrorResponse(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            ApiResult.Error("Unable to load channel: ${e.localizedMessage}")
        }
    }

    private fun handleErrorResponse(code: Int, errorBody: String?): ApiResult.Error {
        val isQuota = code == 403 && (errorBody?.contains("quotaExceeded", ignoreCase = true) == true ||
                errorBody?.contains("rateLimitExceeded", ignoreCase = true) == true)

        val message = when (code) {
            400 -> "Bad request. Please verify the parameter format."
            401 -> "Invalid API Key or unauthorized request. Please verify your credentials."
            403 -> if (isQuota) "YouTube Data API daily quota limit reached. Using offline library." else "Access forbidden. Check your API key restrictions."
            404 -> "Resource not found on YouTube servers."
            429 -> "Too many requests. Please pause before trying again."
            in 500..599 -> "YouTube server error (HTTP $code). Please try again shortly."
            else -> "YouTube API error (HTTP $code): ${errorBody?.take(100) ?: "Unknown"}"
        }
        return ApiResult.Error(
            message = message,
            statusCode = code,
            isQuotaExceeded = isQuota
        )
    }

    private fun YouTubeVideoItemDto.toVideoEntity(): VideoEntity {
        val snippet = this.snippet
        val stats = this.statistics
        val content = this.contentDetails

        val thumbUrl = snippet?.thumbnails?.high?.url
            ?: snippet?.thumbnails?.medium?.url
            ?: snippet?.thumbnails?.defaultThumb?.url
            ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800"

        val durationFormatted = parseIsoDuration(content?.duration ?: "PT5M")
        val durationSecs = parseDurationToSeconds(content?.duration ?: "PT5M")

        return VideoEntity(
            id = this.id,
            title = snippet?.title ?: "Untitled Video",
            description = snippet?.description ?: "",
            thumbnailUrl = thumbUrl,
            channelId = snippet?.channelId ?: "",
            channelTitle = snippet?.channelTitle ?: "Creator",
            channelAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            publishedAt = snippet?.publishedAt ?: "Recently",
            duration = durationFormatted,
            durationSeconds = durationSecs,
            viewCount = stats?.viewCount?.toLongOrNull() ?: 0L,
            likeCount = stats?.likeCount?.toLongOrNull() ?: 0L,
            category = "All",
            isLiked = false,
            isWatchLater = false
        )
    }

    companion object {
        fun parseIsoDuration(isoDuration: String): String {
            val pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val matcher = pattern.matcher(isoDuration)
            if (matcher.matches()) {
                val hours = matcher.group(1)?.toIntOrNull() ?: 0
                val minutes = matcher.group(2)?.toIntOrNull() ?: 0
                val seconds = matcher.group(3)?.toIntOrNull() ?: 0

                return if (hours > 0) {
                    String.format("%d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%d:%02d", minutes, seconds)
                }
            }
            return "5:00"
        }

        fun parseDurationToSeconds(isoDuration: String): Long {
            val pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val matcher = pattern.matcher(isoDuration)
            if (matcher.matches()) {
                val hours = (matcher.group(1)?.toLongOrNull() ?: 0L) * 3600
                val minutes = (matcher.group(2)?.toLongOrNull() ?: 0L) * 60
                val seconds = matcher.group(3)?.toLongOrNull() ?: 0L
                return hours + minutes + seconds
            }
            return 300L
        }
    }
}
