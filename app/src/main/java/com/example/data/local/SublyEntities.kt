package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val title: String,
    val handle: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val subscriberCount: Long,
    val videoCount: Int,
    val description: String,
    val isSubscribed: Boolean,
    val unreadVideosCount: Int
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelId: String,
    val channelTitle: String,
    val channelAvatarUrl: String,
    val publishedAt: String,
    val duration: String,
    val durationSeconds: Long,
    val viewCount: Long,
    val likeCount: Long,
    val category: String,
    val isLiked: Boolean,
    val isWatchLater: Boolean
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val videoCount: Int,
    val thumbnailUrl: String,
    val isCustom: Boolean,
    val updatedAt: Long
)

@Entity(tableName = "playlist_items", primaryKeys = ["playlistId", "videoId"])
data class PlaylistItemEntity(
    val playlistId: String,
    val videoId: String,
    val addedAt: Long
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val progressSeconds: Long,
    val totalSeconds: Long,
    val lastWatchedAt: Long
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long
)
