package com.example.data.models

data class Video(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelId: String,
    val channelTitle: String,
    val channelAvatarUrl: String,
    val publishedAt: String,
    val duration: String,
    val durationSeconds: Long = 300,
    val viewCount: Long = 0,
    val likeCount: Long = 0,
    val category: String = "All",
    val isLiked: Boolean = false,
    val isWatchLater: Boolean = false,
    val tags: List<String> = emptyList()
)

data class Channel(
    val id: String,
    val title: String,
    val handle: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val subscriberCount: Long,
    val videoCount: Int,
    val description: String,
    val isSubscribed: Boolean = false,
    val unreadVideosCount: Int = 0
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val videoCount: Int,
    val thumbnailUrl: String,
    val isCustom: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

data class WatchHistoryItem(
    val videoId: String,
    val progressSeconds: Long,
    val totalSeconds: Long,
    val lastWatchedAt: Long
)

data class Comment(
    val id: String,
    val videoId: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val text: String,
    val likes: Int,
    val timeAgo: String
)

data class UserPreferences(
    val apiKey: String = "",
    val isDemoMode: Boolean = true,
    val autoPlayNext: Boolean = true,
    val videoQuality: String = "1080p",
    val backgroundPlay: Boolean = true,
    val lastSyncTime: Long = 0,
    val isConnected: Boolean = false,
    val userEmail: String = "",
    val userName: String = ""
)

enum class VideoFilterCategory(val displayName: String) {
    ALL("All"),
    TECH("Tech"),
    GAMING("Gaming"),
    MUSIC("Music"),
    EDUCATION("Education"),
    NEWS("News"),
    PODCASTS("Podcasts")
}
