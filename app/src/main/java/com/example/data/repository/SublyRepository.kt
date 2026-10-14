package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.ChannelEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.SublyDatabase
import com.example.data.local.VideoEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.models.Channel
import com.example.data.models.Comment
import com.example.data.models.Playlist
import com.example.data.models.UserPreferences
import com.example.data.models.Video
import com.example.data.models.WatchHistoryItem
import com.example.data.network.ApiResult
import com.example.data.network.YouTubeApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "subly_prefs")

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()
}

class SublyRepository(private val context: Context) {

    private val db = SublyDatabase.getInstance(context)
    private val videoDao = db.videoDao()
    private val channelDao = db.channelDao()
    private val playlistDao = db.playlistDao()
    private val historyDao = db.historyDao()
    private val searchHistoryDao = db.searchHistoryDao()
    private val apiClient = YouTubeApiClient()

    // DataStore keys
    private val KEY_API_KEY = stringPreferencesKey("youtube_api_key")
    private val KEY_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
    private val KEY_AUTOPLAY = booleanPreferencesKey("auto_play")
    private val KEY_QUALITY = stringPreferencesKey("video_quality")
    private val KEY_IS_CONNECTED = booleanPreferencesKey("is_connected")
    private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    private val KEY_USER_NAME = stringPreferencesKey("user_name")

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            apiKey = prefs[KEY_API_KEY] ?: "",
            isDemoMode = prefs[KEY_DEMO_MODE] ?: true,
            autoPlayNext = prefs[KEY_AUTOPLAY] ?: true,
            videoQuality = prefs[KEY_QUALITY] ?: "1080p",
            backgroundPlay = true,
            isConnected = prefs[KEY_IS_CONNECTED] ?: false,
            userEmail = prefs[KEY_USER_EMAIL] ?: "",
            userName = prefs[KEY_USER_NAME] ?: ""
        )
    }

    suspend fun connectAccount(email: String, name: String, isDemo: Boolean = false) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_CONNECTED] = true
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_NAME] = name
            prefs[KEY_DEMO_MODE] = isDemo
        }
    }

    suspend fun disconnectAccount() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_CONNECTED] = false
            prefs[KEY_USER_EMAIL] = ""
            prefs[KEY_USER_NAME] = ""
        }
    }

    suspend fun updateApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        context.dataStore.edit { prefs ->
            prefs[KEY_API_KEY] = trimmed
            prefs[KEY_DEMO_MODE] = trimmed.isEmpty()
        }
    }

    suspend fun setDemoMode(isDemo: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEMO_MODE] = isDemo
        }
    }

    suspend fun updateAutoplay(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTOPLAY] = enabled
        }
    }

    suspend fun updateQuality(quality: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_QUALITY] = quality
        }
    }

    // Input Validation Edge Cases
    fun validateApiKey(key: String): ValidationResult {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("API Key cannot be empty.")
        }
        if (trimmed.length < 20) {
            return ValidationResult.Invalid("API Key is too short. Google API keys are typically ~39 characters.")
        }
        if (trimmed.contains(" ")) {
            return ValidationResult.Invalid("API Key cannot contain spaces.")
        }
        val allowedPattern = Regex("^[a-zA-Z0-9_-]+$")
        if (!allowedPattern.matches(trimmed)) {
            return ValidationResult.Invalid("API Key contains invalid characters.")
        }
        return ValidationResult.Valid
    }

    suspend fun validatePlaylistTitle(title: String): ValidationResult {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Playlist title cannot be empty.")
        }
        if (trimmed.length > 50) {
            return ValidationResult.Invalid("Playlist title cannot exceed 50 characters.")
        }
        val existing = playlistDao.getAllPlaylists().first()
        if (existing.any { it.title.equals(trimmed, ignoreCase = true) }) {
            return ValidationResult.Invalid("A playlist with this title already exists.")
        }
        return ValidationResult.Valid
    }

    fun sanitizeSearchQuery(query: String): String {
        return query.trim().replace(Regex("\\s+"), " ")
    }

    fun validateComment(text: String): ValidationResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Comment cannot be empty.")
        }
        if (trimmed.length > 500) {
            return ValidationResult.Invalid("Comment is too long (${trimmed.length}/500 chars).")
        }
        return ValidationResult.Valid
    }

    // Video Queries
    fun getVideosFeed(category: String = "All"): Flow<List<Video>> {
        val source = if (category == "All") videoDao.getAllVideos() else videoDao.getVideosByCategory(category)
        return source.map { list -> list.map { it.toDomainModel() } }
    }

    fun getVideoById(id: String): Flow<Video?> {
        return videoDao.getVideoById(id).map { it?.toDomainModel() }
    }

    fun getLikedVideos(): Flow<List<Video>> {
        return videoDao.getLikedVideos().map { list -> list.map { it.toDomainModel() } }
    }

    fun getWatchLaterVideos(): Flow<List<Video>> {
        return videoDao.getWatchLaterVideos().map { list -> list.map { it.toDomainModel() } }
    }

    fun getVideosByChannel(channelId: String): Flow<List<Video>> {
        return videoDao.getVideosByChannel(channelId).map { list -> list.map { it.toDomainModel() } }
    }

    suspend fun toggleLike(videoId: String, currentLiked: Boolean) {
        videoDao.setLiked(videoId, !currentLiked)
    }

    suspend fun toggleWatchLater(videoId: String, currentWatchLater: Boolean) {
        videoDao.setWatchLater(videoId, !currentWatchLater)
    }

    // Channel Queries
    fun getAllChannels(): Flow<List<Channel>> {
        return channelDao.getAllChannels().map { list -> list.map { it.toDomainModel() } }
    }

    fun getSubscribedChannels(): Flow<List<Channel>> {
        return channelDao.getSubscribedChannels().map { list -> list.map { it.toDomainModel() } }
    }

    fun getChannelById(id: String): Flow<Channel?> {
        return channelDao.getChannelById(id).map { it?.toDomainModel() }
    }

    suspend fun toggleSubscribe(channelId: String, currentSubscribed: Boolean) {
        channelDao.setSubscribed(channelId, !currentSubscribed)
    }

    // Playlist Queries
    fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { list -> list.map { it.toDomainModel() } }
    }

    suspend fun createPlaylist(title: String, description: String = ""): Boolean {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return false
        val newPlaylist = PlaylistEntity(
            id = "pl_${System.currentTimeMillis()}",
            title = trimmedTitle,
            description = description.trim(),
            videoCount = 0,
            thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
            isCustom = true,
            updatedAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(newPlaylist)
        return true
    }

    suspend fun deletePlaylist(playlistId: String) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addVideoToPlaylist(playlistId: String, videoId: String) {
        playlistDao.insertPlaylistItem(PlaylistItemEntity(playlistId, videoId, System.currentTimeMillis()))
        playlistDao.updatePlaylistCount(playlistId)
    }

    suspend fun removeVideoFromPlaylist(playlistId: String, videoId: String) {
        playlistDao.removePlaylistItem(playlistId, videoId)
        playlistDao.updatePlaylistCount(playlistId)
    }

    fun getPlaylistVideos(playlistId: String): Flow<List<Video>> {
        return combine(
            playlistDao.getPlaylistItems(playlistId),
            videoDao.getAllVideos()
        ) { items, allVideos ->
            val itemMap = items.associateBy { it.videoId }
            allVideos.filter { itemMap.containsKey(it.id) }
                .sortedByDescending { itemMap[it.id]?.addedAt ?: 0 }
                .map { it.toDomainModel() }
        }
    }

    // Watch History
    fun getWatchHistory(): Flow<List<WatchHistoryItem>> {
        return historyDao.getAllHistory().map { list ->
            list.map {
                WatchHistoryItem(
                    videoId = it.videoId,
                    progressSeconds = it.progressSeconds,
                    totalSeconds = it.totalSeconds,
                    lastWatchedAt = it.lastWatchedAt
                )
            }
        }
    }

    fun getWatchHistoryVideos(): Flow<List<Pair<Video, WatchHistoryItem>>> {
        return combine(
            historyDao.getAllHistory(),
            videoDao.getAllVideos()
        ) { historyList, allVideos ->
            val videoMap = allVideos.associateBy { it.id }
            historyList.mapNotNull { item ->
                val video = videoMap[item.videoId]?.toDomainModel() ?: return@mapNotNull null
                val domainHistory = WatchHistoryItem(item.videoId, item.progressSeconds, item.totalSeconds, item.lastWatchedAt)
                Pair(video, domainHistory)
            }
        }
    }

    fun getHistoryProgress(videoId: String): Flow<WatchHistoryItem?> {
        return historyDao.getHistoryForVideo(videoId).map { entity ->
            entity?.let {
                WatchHistoryItem(it.videoId, it.progressSeconds, it.totalSeconds, it.lastWatchedAt)
            }
        }
    }

    suspend fun savePlaybackProgress(videoId: String, currentSec: Long, totalSec: Long) {
        historyDao.upsertHistory(
            WatchHistoryEntity(
                videoId = videoId,
                progressSeconds = currentSec,
                totalSeconds = totalSec,
                lastWatchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    // Search & Live Search
    fun searchLocalVideos(query: String): Flow<List<Video>> {
        val clean = sanitizeSearchQuery(query)
        return videoDao.searchVideos(clean).map { list -> list.map { it.toDomainModel() } }
    }

    fun searchLocalChannels(query: String): Flow<List<Channel>> {
        val clean = sanitizeSearchQuery(query)
        return channelDao.searchChannels(clean).map { list -> list.map { it.toDomainModel() } }
    }

    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> {
        return searchHistoryDao.getRecentSearches()
    }

    suspend fun recordSearch(query: String) {
        val clean = sanitizeSearchQuery(query)
        if (clean.isNotEmpty()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = clean, timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun deleteRecentSearch(id: Long) {
        searchHistoryDao.deleteSearch(id)
    }

    suspend fun clearRecentSearches() {
        searchHistoryDao.clearSearchHistory()
    }

    // API Sync
    suspend fun syncFeed(): ApiResult<Int> {
        val prefs = userPreferencesFlow.first()
        if (prefs.isDemoMode || prefs.apiKey.isBlank()) {
            // Re-seed demo database if needed
            val count = videoDao.getCount()
            return ApiResult.Success(count)
        }

        return when (val result = apiClient.fetchPopularVideos(prefs.apiKey)) {
            is ApiResult.Success -> {
                if (result.data.isNotEmpty()) {
                    videoDao.insertVideos(result.data)
                }
                ApiResult.Success(result.data.size)
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun searchOnline(query: String): ApiResult<List<Video>> {
        val prefs = userPreferencesFlow.first()
        val clean = sanitizeSearchQuery(query)
        if (clean.isEmpty()) return ApiResult.Success(emptyList())

        if (prefs.isDemoMode || prefs.apiKey.isBlank()) {
            val localMatches = videoDao.searchVideos(clean).first()
            return ApiResult.Success(localMatches.map { it.toDomainModel() })
        }

        return when (val result = apiClient.searchVideos(clean, prefs.apiKey)) {
            is ApiResult.Success -> {
                if (result.data.isNotEmpty()) {
                    videoDao.insertVideos(result.data)
                }
                ApiResult.Success(result.data.map { it.toDomainModel() })
            }
            is ApiResult.Error -> result
        }
    }

    private fun VideoEntity.toDomainModel(): Video {
        return Video(
            id = id,
            title = title,
            description = description,
            thumbnailUrl = thumbnailUrl,
            channelId = channelId,
            channelTitle = channelTitle,
            channelAvatarUrl = channelAvatarUrl,
            publishedAt = publishedAt,
            duration = duration,
            durationSeconds = durationSeconds,
            viewCount = viewCount,
            likeCount = likeCount,
            category = category,
            isLiked = isLiked,
            isWatchLater = isWatchLater
        )
    }

    private fun ChannelEntity.toDomainModel(): Channel {
        return Channel(
            id = id,
            title = title,
            handle = handle,
            avatarUrl = avatarUrl,
            bannerUrl = bannerUrl,
            subscriberCount = subscriberCount,
            videoCount = videoCount,
            description = description,
            isSubscribed = isSubscribed,
            unreadVideosCount = unreadVideosCount
        )
    }

    private fun PlaylistEntity.toDomainModel(): Playlist {
        return Playlist(
            id = id,
            title = title,
            description = description,
            videoCount = videoCount,
            thumbnailUrl = thumbnailUrl,
            isCustom = isCustom,
            updatedAt = updatedAt
        )
    }
}
