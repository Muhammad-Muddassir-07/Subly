package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT v.* FROM videos v INNER JOIN channels c ON v.channelId = c.id WHERE c.isSubscribed = 1 ORDER BY v.publishedAt DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT v.* FROM videos v INNER JOIN channels c ON v.channelId = c.id WHERE c.isSubscribed = 1 AND v.category = :category ORDER BY v.publishedAt DESC")
    fun getVideosByCategory(category: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    fun getVideoById(id: String): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoByIdSync(id: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE isLiked = 1 ORDER BY publishedAt DESC")
    fun getLikedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isWatchLater = 1 ORDER BY publishedAt DESC")
    fun getWatchLaterVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE channelId = :channelId ORDER BY publishedAt DESC")
    fun getVideosByChannel(channelId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR channelTitle LIKE '%' || :query || '%'")
    fun searchVideos(query: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Query("UPDATE videos SET isLiked = :isLiked WHERE id = :id")
    suspend fun setLiked(id: String, isLiked: Boolean)

    @Query("UPDATE videos SET isWatchLater = :isWatchLater WHERE id = :id")
    suspend fun setWatchLater(id: String, isWatchLater: Boolean)

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun getCount(): Int

    @Query("DELETE FROM videos WHERE id LIKE 'mock_%' OR id LIKE 'v_fake_%' OR id LIKE 'sample_%'")
    suspend fun deleteLegacySampleVideos()
}

@Dao
interface ChannelDao {
    @Query("DELETE FROM channels WHERE id LIKE 'mock_%' OR id LIKE 'sample_%'")
    suspend fun deleteLegacySampleChannels()

    @Query("DELETE FROM channels WHERE id LIKE 'UC_fake_%'")
    suspend fun deleteFakeUserChannels()

    @Query("DELETE FROM channels WHERE id = :id")
    suspend fun deleteChannel(id: String)

    @Query("SELECT * FROM channels ORDER BY title ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isSubscribed = 1 ORDER BY title ASC")
    fun getSubscribedChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id")
    fun getChannelById(id: String): Flow<ChannelEntity?>

    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelByIdSync(id: String): ChannelEntity?

    @Query("SELECT * FROM channels WHERE title LIKE '%' || :query || '%' OR handle LIKE '%' || :query || '%'")
    fun searchChannels(query: String): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("UPDATE channels SET isSubscribed = :isSubscribed WHERE id = :channelId")
    suspend fun setSubscribed(channelId: String, isSubscribed: Boolean)

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun getCount(): Int
}

@Dao
interface PlaylistDao {
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getCount(): Int

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    fun getPlaylistItems(playlistId: String): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removePlaylistItem(playlistId: String, videoId: String)

    @Query("UPDATE playlists SET videoCount = (SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId) WHERE id = :playlistId")
    suspend fun updatePlaylistCount(playlistId: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId")
    fun getHistoryForVideo(videoId: String): Flow<WatchHistoryEntity?>

    @Upsert
    suspend fun upsertHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteFromHistory(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 15")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearch(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}
