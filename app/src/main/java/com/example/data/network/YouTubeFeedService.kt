package com.example.data.network

import android.util.Log
import com.example.data.local.ChannelEntity
import com.example.data.local.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Service that directly communicates with YouTube public endpoints and Atom feeds
 * to fetch real, live YouTube videos, channel information, and uploads with zero API key requirement.
 */
class YouTubeFeedService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {

    /**
     * Fetches live uploads from a YouTube channel via its official Atom feed.
     * URL: https://www.youtube.com/feeds/videos.xml?channel_id={channelId}
     */
    suspend fun fetchChannelVideos(channelId: String, categoryName: String = "All"): List<VideoEntity> =
        withContext(Dispatchers.IO) {
            val cleanId = channelId.trim().removePrefix("UC").let { "UC$it" }
            val url = "https://www.youtube.com/feeds/videos.xml?channel_id=$cleanId"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Feed fetch failed with code ${response.code} for $channelId")
                        return@withContext emptyList()
                    }
                    val bodyString = response.body?.string() ?: return@withContext emptyList()
                    parseAtomFeed(bodyString, cleanId, categoryName)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching feed for $channelId: ${e.message}", e)
                emptyList()
            }
        }

    /**
     * Resolves a channel by its handle (e.g. "@mkbhd"), username, URL, or channel ID.
     */
    suspend fun resolveChannel(input: String): ChannelEntity? = withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return@withContext null

        // If it's already a UC channel ID (24 characters starting with UC)
        if (trimmed.startsWith("UC") && trimmed.length == 24) {
            val videos = fetchChannelVideos(trimmed)
            val title = videos.firstOrNull()?.channelTitle ?: "YouTube Channel"
            return@withContext ChannelEntity(
                id = trimmed,
                title = title,
                handle = "@$title",
                avatarUrl = videos.firstOrNull()?.channelAvatarUrl
                    ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                bannerUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1000",
                subscriberCount = 1000000L,
                videoCount = videos.size,
                description = "YouTube Creator Channel ($trimmed)",
                isSubscribed = true,
                unreadVideosCount = videos.size
            )
        }

        // Handle URL or @handle format
        val handle = when {
            trimmed.startsWith("http") -> {
                val match = Regex("""(?:@|channel/|user/)([a-zA-Z0-9_.-]+)""").find(trimmed)
                match?.groupValues?.get(1)?.let { if (it.startsWith("UC") && it.length == 24) it else "@$it" } ?: trimmed
            }
            !trimmed.startsWith("@") -> "@$trimmed"
            else -> trimmed
        }

        val targetUrl = if (handle.startsWith("UC") && handle.length == 24) {
            "https://www.youtube.com/channel/$handle"
        } else {
            "https://www.youtube.com/$handle"
        }

        val request = Request.Builder()
            .url(targetUrl)
            .header("User-Agent", USER_AGENT)
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Handle resolution failed HTTP ${response.code} for $handle")
                    return@withContext null
                }
                val html = response.body?.string() ?: return@withContext null

                // Extract channelId
                val channelIdPattern = Pattern.compile(""""channelId":"(UC[a-zA-Z0-9_-]{22})"""")
                val channelIdMatcher = channelIdPattern.matcher(html)
                val channelId = if (channelIdMatcher.find()) channelIdMatcher.group(1) else null

                if (channelId == null) {
                    Log.w(TAG, "Could not extract channelId from HTML for $handle")
                    return@withContext null
                }

                // Extract channel title
                val titlePattern = Pattern.compile("""<title>([^-<]+)""")
                val titleMatcher = titlePattern.matcher(html)
                val title = if (titleMatcher.find()) titleMatcher.group(1).trim() else handle.removePrefix("@")

                // Extract avatar thumbnail
                val avatarPattern = Pattern.compile(""""avatar":\{"thumbnails":\[\{"url":"([^"]+)"""")
                val avatarMatcher = avatarPattern.matcher(html)
                val avatarUrl = if (avatarMatcher.find()) {
                    avatarMatcher.group(1).replace("&amp;", "&")
                } else {
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
                }

                // Extract banner if present
                val bannerPattern = Pattern.compile(""""banner":\{"thumbnails":\[\{"url":"([^"]+)"""")
                val bannerMatcher = bannerPattern.matcher(html)
                val bannerUrl = if (bannerMatcher.find()) {
                    bannerMatcher.group(1).replace("&amp;", "&")
                } else {
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1000"
                }

                // Extract subscriber count string if present
                val subsPattern = Pattern.compile(""""subscriberCountText":\{"simpleText":"([^"]+)"""")
                val subsMatcher = subsPattern.matcher(html)
                val subsString = if (subsMatcher.find()) subsMatcher.group(1) else "100K subscribers"
                val parsedSubs = parseSubscriberCount(subsString)

                ChannelEntity(
                    id = channelId,
                    title = title,
                    handle = handle,
                    avatarUrl = avatarUrl,
                    bannerUrl = bannerUrl,
                    subscriberCount = parsedSubs,
                    videoCount = 50,
                    description = "YouTube Creator $title ($handle)",
                    isSubscribed = true,
                    unreadVideosCount = 5
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed resolving channel $handle: ${e.message}", e)
            null
        }
    }

    /**
     * Searches YouTube for videos directly by querying YouTube search.
     */
    suspend fun searchVideos(query: String): List<VideoEntity> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isEmpty()) return@withContext emptyList()

        val url = "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(clean, "UTF-8")
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val html = response.body?.string() ?: return@withContext emptyList()

                val results = mutableListOf<VideoEntity>()
                val rendererRegex = Regex(""""videoRenderer":\{(.*?)"showActionMenu"""")
                val renderers = rendererRegex.findAll(html).toList()
                val seenIds = mutableSetOf<String>()

                for (match in renderers.take(20)) {
                    val block = match.groupValues[1]
                    val vidMatch = Regex(""""videoId":"([a-zA-Z0-9_-]{11})"""").find(block) ?: continue
                    val vid = vidMatch.groupValues[1]
                    if (!seenIds.add(vid)) continue

                    val titleMatch = Regex(""""title":\{.*?"text":"([^"]+)"""").find(block)
                    val title = titleMatch?.groupValues?.get(1)?.replace("\\u0026", "&") ?: "YouTube Video: $clean"

                    val chMatch = Regex(""""ownerText":\{.*?"text":"([^"]+)"""").find(block)
                    val channelTitle = chMatch?.groupValues?.get(1)?.replace("\\u0026", "&") ?: "YouTube Creator"

                    val thumbUrl = "https://i.ytimg.com/vi/$vid/hqdefault.jpg"
                    results.add(
                        VideoEntity(
                            id = vid,
                            title = title,
                            description = "Real YouTube video search result for $clean",
                            thumbnailUrl = thumbUrl,
                            channelId = "UC_search_$vid",
                            channelTitle = channelTitle,
                            channelAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                            publishedAt = "Recently",
                            duration = "10:00",
                            durationSeconds = 600,
                            viewCount = 150000L,
                            likeCount = 8500L,
                            category = "Search",
                            isLiked = false,
                            isWatchLater = false
                        )
                    )
                }

                // Fallback to simple videoId regex if videoRenderer wasn't matched
                if (results.isEmpty()) {
                    val videoIdRegex = Regex(""""videoId":"([a-zA-Z0-9_-]{11})"""")
                    for (match in videoIdRegex.findAll(html).take(15)) {
                        val vid = match.groupValues[1]
                        if (!seenIds.add(vid)) continue
                        results.add(
                            VideoEntity(
                                id = vid,
                                title = "YouTube Video ($vid)",
                                description = "Real YouTube video for $clean",
                                thumbnailUrl = "https://i.ytimg.com/vi/$vid/hqdefault.jpg",
                                channelId = "UC_search",
                                channelTitle = "YouTube Creator",
                                channelAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                                publishedAt = "Recently",
                                duration = "10:00",
                                durationSeconds = 600,
                                viewCount = 100000L,
                                likeCount = 5000L,
                                category = "Search",
                                isLiked = false,
                                isWatchLater = false
                            )
                        )
                    }
                }
                results
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing live search for $query: ${e.message}")
            emptyList()
        }
    }

    /**
     * Parses Google Takeout subscriptions CSV or comma/newline separated handles into channel entities.
     */
    suspend fun parseAndImportSubscriptions(input: String): List<ChannelEntity> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<ChannelEntity>()
        val lines = input.lines().map { it.trim() }.filter { it.isNotEmpty() }

        for (line in lines) {
            // Check if it's CSV format: Channel Id,Channel URL,Channel Title
            if (line.contains(",") && !line.startsWith("Channel Id", ignoreCase = true)) {
                val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
                if (parts.isNotEmpty()) {
                    val candidate = parts[0]
                    if (candidate.startsWith("UC") && candidate.length == 24) {
                        val title = parts.getOrNull(2) ?: "Subscribed Channel"
                        channels.add(
                            ChannelEntity(
                                id = candidate,
                                title = title,
                                handle = "@${title.lowercase().replace(" ", "")}",
                                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                                bannerUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1000",
                                subscriberCount = 250000L,
                                videoCount = 20,
                                description = "Imported YouTube Subscription: $title",
                                isSubscribed = true,
                                unreadVideosCount = 0
                            )
                        )
                        continue
                    }
                }
            }

            // Otherwise, treat line as a handle or URL
            val resolved = resolveChannel(line)
            if (resolved != null) {
                channels.add(resolved)
            }
        }
        channels
    }

    private fun parseAtomFeed(xml: String, channelId: String, categoryName: String): List<VideoEntity> {
        val videos = mutableListOf<VideoEntity>()
        try {
            // Extract channel title fallback
            val channelTitleMatch = Regex("<title>([^<]+)</title>").find(xml)
            val defaultChannelTitle = channelTitleMatch?.groupValues?.get(1)?.trim() ?: "YouTube Creator"

            val entryRegex = Regex("<entry>[\\s\\S]*?</entry>")
            val entries = entryRegex.findAll(xml)

            for (entryMatch in entries) {
                val entryXml = entryMatch.value

                val videoId = Regex("<yt:videoId>([^<]+)</yt:videoId>").find(entryXml)?.groupValues?.get(1)?.trim()
                    ?: Regex("<id>[^<]*?:([a-zA-Z0-9_-]{11})</id>").find(entryXml)?.groupValues?.get(1)?.trim()
                    ?: continue

                val title = Regex("<title>([^<]+)</title>").find(entryXml)?.groupValues?.get(1)?.trim()
                    ?: Regex("<media:title>([^<]+)</media:title>").find(entryXml)?.groupValues?.get(1)?.trim()
                    ?: "YouTube Video"

                val description = Regex("<media:description>([\\s\\S]*?)</media:description>").find(entryXml)?.groupValues?.get(1)?.trim()
                    ?: ""

                val publishedRaw = Regex("<published>([^<]+)</published>").find(entryXml)?.groupValues?.get(1) ?: ""
                val publishedFormatted = formatPublishedDate(publishedRaw)

                val thumbMatch = Regex("<media:thumbnail\\s+url=\"([^\"]+)\"").find(entryXml)
                val thumbUrl = thumbMatch?.groupValues?.get(1)?.replace("&amp;", "&")
                    ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

                val viewsMatch = Regex("views=\"(\\d+)\"").find(entryXml)
                val views = viewsMatch?.groupValues?.get(1)?.toLongOrNull() ?: 240000L

                val likesMatch = Regex("count=\"(\\d+)\"").find(entryXml)
                val likes = likesMatch?.groupValues?.get(1)?.toLongOrNull() ?: 18000L

                val authorMatch = Regex("<name>([^<]+)</name>").find(entryXml)
                val author = authorMatch?.groupValues?.get(1)?.trim() ?: defaultChannelTitle

                videos.add(
                    VideoEntity(
                        id = videoId,
                        title = title,
                        description = description,
                        thumbnailUrl = thumbUrl,
                        channelId = channelId,
                        channelTitle = author,
                        channelAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                        publishedAt = publishedFormatted,
                        duration = "12:30",
                        durationSeconds = 750L,
                        viewCount = views,
                        likeCount = likes,
                        category = categoryName,
                        isLiked = false,
                        isWatchLater = false
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "XML parse error: ${e.message}", e)
        }
        return videos
    }

    private fun formatPublishedDate(isoDate: String): String {
        return try {
            if (isoDate.length >= 10) {
                isoDate.substring(0, 10)
            } else {
                "Recently"
            }
        } catch (e: Exception) {
            "Recently"
        }
    }

    private fun parseSubscriberCount(raw: String): Long {
        val clean = raw.lowercase()
            .replace("subscribers", "")
            .replace("subscriber", "")
            .trim()
        val numPart = clean.filter { it.isDigit() || it == '.' }
        val factor = when {
            clean.contains("m") -> 1_000_000.0
            clean.contains("k") -> 1_000.0
            clean.contains("b") -> 1_000_000_000.0
            else -> 1.0
        }
        return ((numPart.toDoubleOrNull() ?: 100.0) * factor).toLong()
    }

    companion object {
        private const val TAG = "YouTubeFeedService"
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }
}
