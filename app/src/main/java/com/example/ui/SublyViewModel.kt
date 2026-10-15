package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DefaultSublyData
import com.example.data.local.SearchHistoryEntity
import com.example.data.models.Channel
import com.example.data.models.Comment
import com.example.data.models.Playlist
import com.example.data.models.UserPreferences
import com.example.data.models.Video
import com.example.data.models.VideoFilterCategory
import com.example.data.models.WatchHistoryItem
import com.example.data.network.ApiResult
import com.example.data.repository.SublyRepository
import com.example.data.repository.ValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerState(
    val currentVideo: Video? = null,
    val isPlaying: Boolean = false,
    val currentSeconds: Long = 0,
    val totalSeconds: Long = 300,
    val playbackSpeed: Float = 1.0f,
    val isControlsVisible: Boolean = true,
    val isLiked: Boolean = false,
    val isWatchLater: Boolean = false,
    val comments: List<Comment> = emptyList()
)

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Video> = emptyList(),
    val channelResults: List<Channel> = emptyList(),
    val selectedType: String = "All", // All, Videos, Channels
    val selectedDuration: String = "Any", // Any, Short (<4m), Long (>20m)
    val errorMessage: String? = null
)

class SublyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SublyRepository(application)
    private val authManager = com.example.data.auth.FirebaseAuthManager()
    private val firestoreRepo = com.example.data.repository.FirestoreSubscriptionRepository(application)

    val currentUser: com.google.firebase.auth.FirebaseUser?
        get() = authManager.currentUser

    val firebaseUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = authManager.authStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authManager.currentUser)

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    // User preferences & API key state
    val preferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    // UI Message events (e.g. Snackbars, Quota warnings)
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Category Filter in Home
    private val _selectedCategory = MutableStateFlow(VideoFilterCategory.ALL)
    val selectedCategory: StateFlow<VideoFilterCategory> = _selectedCategory.asStateFlow()

    // Home Feed Videos
    val feedVideos: StateFlow<List<Video>> = _selectedCategory.flatMapLatest { cat ->
        repository.getVideosFeed(cat.displayName)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Subscriptions
    val allChannels: StateFlow<List<Channel>> = repository.getAllChannels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscribedChannels: StateFlow<List<Channel>> = repository.getSubscribedChannels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _subscriptionFilterChannelId = MutableStateFlow<String?>(null)
    val subscriptionFilterChannelId: StateFlow<String?> = _subscriptionFilterChannelId.asStateFlow()

    // Subscriptions Feed (filtered by selected channel or all subscribed)
    val subscriptionsFeedVideos: StateFlow<List<Video>> = _subscriptionFilterChannelId.flatMapLatest { channelId ->
        if (channelId == null) {
            repository.getVideosFeed("All")
        } else {
            repository.getVideosByChannel(channelId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Library Data
    val likedVideos: StateFlow<List<Video>> = repository.getLikedVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchLaterVideos: StateFlow<List<Video>> = repository.getWatchLaterVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<Pair<Video, WatchHistoryItem>>> = repository.getWatchHistoryVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Player State
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    private var playbackTimerJob: Job? = null

    // Search State
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()
    val recentSearches: StateFlow<List<SearchHistoryEntity>> = repository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private var searchDebounceJob: Job? = null

    // Channel Detail Screen State
    private val _currentChannelId = MutableStateFlow<String?>(null)
    val currentChannelId: StateFlow<String?> = _currentChannelId.asStateFlow()
    val currentChannel: StateFlow<Channel?> = _currentChannelId.flatMapLatest { id ->
        if (id != null) repository.getChannelById(id) else MutableStateFlow(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val currentChannelVideos: StateFlow<List<Video>> = _currentChannelId.flatMapLatest { id ->
        if (id != null) repository.getVideosByChannel(id) else MutableStateFlow(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sync State
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Ensure default data is seeded if database is fresh
        viewModelScope.launch {
            repository.syncFeed()
        }
    }

    // Category actions
    fun setCategory(category: VideoFilterCategory) {
        _selectedCategory.value = category
    }

    // Subscriptions actions
    fun setSubscriptionChannelFilter(channelId: String?) {
        _subscriptionFilterChannelId.value = if (_subscriptionFilterChannelId.value == channelId) null else channelId
    }

    fun toggleSubscribe(channel: Channel) {
        viewModelScope.launch {
            repository.toggleSubscribe(channel.id, channel.isSubscribed)
            val newSubscribedState = !channel.isSubscribed
            val msg = if (newSubscribedState) "Subscribed to ${channel.title}" else "Unsubscribed from ${channel.title}"
            _userMessage.emit(msg)

            val uid = authManager.currentUser?.uid
            if (uid != null) {
                try {
                    if (newSubscribedState) {
                        firestoreRepo.saveSubscription(
                            userId = uid,
                            subscription = com.example.data.repository.UserSubscriptionRecord(
                                channelId = channel.id,
                                channelTitle = channel.title,
                                channelAvatarUrl = channel.avatarUrl,
                                subscribedAt = System.currentTimeMillis()
                            )
                        )
                    } else {
                        firestoreRepo.deleteSubscription(uid, channel.id)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("SublyViewModel", "Firestore sync error: ${e.message}")
                }
            }
        }
    }

    // Video Player actions
    fun openVideo(video: Video) {
        val comments = DefaultSublyData.sampleComments.filter { it.videoId == video.id }.ifEmpty {
            DefaultSublyData.sampleComments
        }

        viewModelScope.launch {
            // Load resume progress if available
            val history = repository.getHistoryProgress(video.id)
            val lastSec = history.flatMapLatest { MutableStateFlow(it?.progressSeconds ?: 0L) }
                .stateIn(viewModelScope).value

            _playerState.value = PlayerState(
                currentVideo = video,
                isPlaying = true,
                currentSeconds = lastSec,
                totalSeconds = video.durationSeconds,
                isLiked = video.isLiked,
                isWatchLater = video.isWatchLater,
                comments = comments
            )
            startPlaybackLoop()
        }
    }

    fun togglePlayPause() {
        val current = _playerState.value
        val newPlaying = !current.isPlaying
        _playerState.value = current.copy(isPlaying = newPlaying)
        if (newPlaying) {
            startPlaybackLoop()
        } else {
            playbackTimerJob?.cancel()
            saveCurrentPlaybackProgress()
        }
    }

    fun seekTo(seconds: Long) {
        val current = _playerState.value
        val clamped = seconds.coerceIn(0L, current.totalSeconds)
        _playerState.value = current.copy(currentSeconds = clamped)
        saveCurrentPlaybackProgress()
    }

    fun skipForward(seconds: Long = 10) {
        val current = _playerState.value
        seekTo(current.currentSeconds + seconds)
    }

    fun skipBackward(seconds: Long = 10) {
        val current = _playerState.value
        seekTo(current.currentSeconds - seconds)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    fun toggleControlsVisibility() {
        _playerState.value = _playerState.value.copy(isControlsVisible = !_playerState.value.isControlsVisible)
    }

    fun toggleLikeCurrentVideo() {
        val video = _playerState.value.currentVideo ?: return
        val newLiked = !_playerState.value.isLiked
        _playerState.value = _playerState.value.copy(isLiked = newLiked)
        viewModelScope.launch {
            repository.toggleLike(video.id, !newLiked)
            _userMessage.emit(if (newLiked) "Added to Liked Videos" else "Removed from Liked Videos")
        }
    }

    fun toggleWatchLater(video: Video) {
        viewModelScope.launch {
            repository.toggleWatchLater(video.id, video.isWatchLater)
            if (_playerState.value.currentVideo?.id == video.id) {
                _playerState.value = _playerState.value.copy(isWatchLater = !video.isWatchLater)
            }
            _userMessage.emit(if (!video.isWatchLater) "Saved to Watch Later" else "Removed from Watch Later")
        }
    }

    fun addComment(text: String): ValidationResult {
        val validation = repository.validateComment(text)
        if (validation is ValidationResult.Invalid) {
            return validation
        }
        val video = _playerState.value.currentVideo ?: return ValidationResult.Invalid("No video playing")
        val newComment = Comment(
            id = "c_${System.currentTimeMillis()}",
            videoId = video.id,
            authorName = "You",
            authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
            text = text.trim(),
            likes = 0,
            timeAgo = "Just now"
        )
        _playerState.value = _playerState.value.copy(
            comments = listOf(newComment) + _playerState.value.comments
        )
        return ValidationResult.Valid
    }

    private fun startPlaybackLoop() {
        playbackTimerJob?.cancel()
        playbackTimerJob = viewModelScope.launch {
            while (_playerState.value.isPlaying) {
                delay(1000)
                val current = _playerState.value
                if (current.currentSeconds < current.totalSeconds) {
                    val nextSec = current.currentSeconds + 1
                    _playerState.value = current.copy(currentSeconds = nextSec)
                    if (nextSec % 5 == 0L) {
                        saveCurrentPlaybackProgress()
                    }
                } else {
                    _playerState.value = current.copy(isPlaying = false)
                    saveCurrentPlaybackProgress()
                    break
                }
            }
        }
    }

    private fun saveCurrentPlaybackProgress() {
        val video = _playerState.value.currentVideo ?: return
        viewModelScope.launch {
            repository.savePlaybackProgress(video.id, _playerState.value.currentSeconds, _playerState.value.totalSeconds)
        }
    }

    // Search actions
    fun onSearchQueryChanged(newQuery: String) {
        _searchState.value = _searchState.value.copy(query = newQuery, errorMessage = null)
        searchDebounceJob?.cancel()
        if (newQuery.isBlank()) {
            _searchState.value = _searchState.value.copy(results = emptyList(), channelResults = emptyList())
            return
        }
        searchDebounceJob = viewModelScope.launch {
            delay(350) // debounce
            performSearch(newQuery)
        }
    }

    fun submitSearch(query: String) {
        val clean = repository.sanitizeSearchQuery(query)
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.recordSearch(clean)
            performSearch(clean)
        }
    }

    private suspend fun performSearch(query: String) {
        _searchState.value = _searchState.value.copy(isLoading = true, errorMessage = null)
        when (val result = repository.searchOnline(query)) {
            is ApiResult.Success -> {
                // Also search local channels
                val channels = repository.searchLocalChannels(query).flatMapLatest { MutableStateFlow(it) }
                    .stateIn(viewModelScope).value

                _searchState.value = _searchState.value.copy(
                    isLoading = false,
                    results = result.data,
                    channelResults = channels,
                    errorMessage = null
                )
            }
            is ApiResult.Error -> {
                _searchState.value = _searchState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun deleteRecentSearch(id: Long) {
        viewModelScope.launch {
            repository.deleteRecentSearch(id)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearRecentSearches()
        }
    }

    // Playlist actions
    fun createPlaylist(title: String, description: String = "", onResult: (ValidationResult) -> Unit) {
        viewModelScope.launch {
            val validation = repository.validatePlaylistTitle(title)
            if (validation is ValidationResult.Invalid) {
                onResult(validation)
                return@launch
            }
            repository.createPlaylist(title, description)
            _userMessage.emit("Playlist \"${title.trim()}\" created!")
            onResult(ValidationResult.Valid)
        }
    }

    fun addVideoToPlaylist(playlistId: String, videoId: String) {
        viewModelScope.launch {
            repository.addVideoToPlaylist(playlistId, videoId)
            _userMessage.emit("Added to playlist")
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            _userMessage.emit("Playlist deleted")
        }
    }

    // Channel details navigation
    fun selectChannel(channelId: String) {
        _currentChannelId.value = channelId
    }

    // Sync & Settings actions
    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            when (val result = repository.syncFeed()) {
                is ApiResult.Success -> {
                    _userMessage.emit("Synced feed successfully (${result.data} videos available)")
                }
                is ApiResult.Error -> {
                    _userMessage.emit(result.message)
                }
            }
            _isSyncing.value = false
        }
    }

    fun saveApiKey(key: String, onResult: (ValidationResult) -> Unit) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) {
            viewModelScope.launch {
                repository.updateApiKey("")
                _userMessage.emit("Switched to Offline Demo mode")
                onResult(ValidationResult.Valid)
            }
            return
        }

        val validation = repository.validateApiKey(trimmed)
        if (validation is ValidationResult.Invalid) {
            onResult(validation)
            return
        }

        viewModelScope.launch {
            repository.updateApiKey(trimmed)
            _userMessage.emit("API Key saved! Testing connection…")
            syncNow()
            onResult(ValidationResult.Valid)
        }
    }

    fun setDemoMode(isDemo: Boolean) {
        viewModelScope.launch {
            repository.setDemoMode(isDemo)
            _userMessage.emit(if (isDemo) "Demo Mode enabled (offline sample library)" else "Live API mode enabled")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _userMessage.emit("Watch history cleared")
        }
    }

    fun updateAutoplay(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAutoplay(enabled)
        }
    }

    fun connectAccount(
        email: String = "user@gmail.com",
        name: String = "YouTube Creator",
        photoUrl: String = "",
        isDemo: Boolean = false
    ) {
        viewModelScope.launch {
            repository.connectAccount(email, name, photoUrl, isDemo)
            _userMessage.emit("Successfully connected as $name")
        }
    }

    fun syncSubscriptionsWithFirestore(userId: String) {
        viewModelScope.launch {
            try {
                val currentSubscribed = subscribedChannels.value
                if (currentSubscribed.isNotEmpty()) {
                    val records = currentSubscribed.map { channel ->
                        com.example.data.repository.UserSubscriptionRecord(
                            channelId = channel.id,
                            channelTitle = channel.title,
                            channelAvatarUrl = channel.avatarUrl,
                            subscribedAt = System.currentTimeMillis(),
                            notificationsEnabled = true
                        )
                    }
                    firestoreRepo.batchSyncSubscriptions(userId, records)
                }
            } catch (e: Exception) {
                android.util.Log.w("SublyViewModel", "Firestore batch sync warning: ${e.message}")
            }
        }
    }

    fun signInWithGoogle(context: android.content.Context, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            val result = authManager.signInWithGoogle(context)
            _isAuthenticating.value = false

            result.onSuccess { user ->
                val email = user.email ?: "google.user@gmail.com"
                val name = user.displayName ?: "Google User"
                val photoUrl = user.photoUrl?.toString() ?: ""
                repository.connectAccount(email = email, name = name, photoUrl = photoUrl, isDemo = false)
                _userMessage.emit("Welcome, $name!")
                syncSubscriptionsWithFirestore(user.uid)
                onComplete(true, null)
            }.onFailure { exception ->
                val errorMsg = when (exception) {
                    is androidx.credentials.exceptions.GetCredentialCancellationException -> {
                        // User cancelled or dismissed picker
                        null
                    }
                    else -> exception.localizedMessage ?: "Sign-in failed. Please try again."
                }
                if (errorMsg != null) {
                    _userMessage.emit(errorMsg)
                }
                onComplete(false, errorMsg)
            }
        }
    }

    fun disconnectAccount(context: android.content.Context? = null) {
        viewModelScope.launch {
            authManager.signOut(context)
            repository.disconnectAccount()
            _userMessage.emit("Disconnected account")
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackTimerJob?.cancel()
        saveCurrentPlaybackProgress()
    }
}
