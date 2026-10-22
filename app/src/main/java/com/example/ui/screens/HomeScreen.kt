package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.models.Video
import com.example.data.models.VideoFilterCategory
import com.example.ui.SublyViewModel
import com.example.ui.components.AddToPlaylistBottomSheet
import com.example.ui.components.VideoCard
import com.example.ui.theme.SublyRedPrimary

@Composable
fun HomeScreen(
    viewModel: SublyViewModel,
    onVideoSelected: (Video) -> Unit,
    onChannelSelected: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val feedVideos by viewModel.feedVideos.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    var playlistVideoToSave by remember { mutableStateOf<Video?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header Bar
        HomeTopBar(
            isSyncing = isSyncing,
            onSearchClick = onNavigateToSearch,
            onSyncClick = { viewModel.syncNow() },
            onProfileClick = onNavigateToProfile
        )

        // Categories Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(VideoFilterCategory.entries.toTypedArray()) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategory(category) },
                    label = {
                        Text(
                            text = category.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SublyRedPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
                )
            }
        }

        // Feed List
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Featured Video (if category is ALL and we have videos)
            if (selectedCategory == VideoFilterCategory.ALL && feedVideos.isNotEmpty()) {
                item {
                    HeroFeaturedCard(
                        video = feedVideos.first(),
                        onWatchClick = { onVideoSelected(feedVideos.first()) }
                    )
                }
            }

            // Regular Feed Items
            val displayVideos = if (selectedCategory == VideoFilterCategory.ALL && feedVideos.size > 1) {
                feedVideos.drop(1)
            } else {
                feedVideos
            }

            if (isSyncing && feedVideos.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = SublyRedPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching real YouTube feeds…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else if (displayVideos.isEmpty()) {
                item {
                    EmptyFeedPlaceholder(
                        category = selectedCategory.displayName,
                        onAddChannel = { handleOrUrl, callback ->
                            viewModel.addYouTubeChannel(handleOrUrl, callback)
                        },
                        onRefresh = { viewModel.syncNow() }
                    )
                }
            } else {
                items(displayVideos, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onVideoClick = onVideoSelected,
                        onChannelClick = onChannelSelected,
                        onWatchLaterToggle = { viewModel.toggleWatchLater(it) },
                        onAddToPlaylistClick = { playlistVideoToSave = it }
                    )
                }
            }
        }
    }

    playlistVideoToSave?.let { video ->
        AddToPlaylistBottomSheet(
            video = video,
            playlists = playlists,
            onAddToPlaylist = { pId, vId -> viewModel.addVideoToPlaylist(pId, vId) },
            onCreatePlaylist = { title, desc, callback -> viewModel.createPlaylist(title, desc, callback) },
            onDismiss = { playlistVideoToSave = null }
        )
    }
}

@Composable
fun HomeTopBar(
    isSyncing: Boolean,
    onSearchClick: () -> Unit,
    onSyncClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subly Logo Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = SublyRedPrimary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Subly",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Smart YouTube Stream",
                    style = MaterialTheme.typography.labelSmall,
                    color = SublyRedPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onSyncClick,
                modifier = Modifier.testTag("sync_feed_button")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SublyRedPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync feed",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag("home_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onProfileClick() }
                    .testTag("profile_avatar_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "S",
                        fontWeight = FontWeight.Bold,
                        color = SublyRedPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun HeroFeaturedCard(
    video: Video,
    onWatchClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWatchClick() }
            .testTag("hero_featured_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9.5f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(video.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient protection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            // Featured Tag
            Surface(
                color = SublyRedPrimary,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = "FEATURED NOW",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            // Bottom Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${video.channelTitle} • ${video.duration}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onWatchClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SublyRedPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Watch Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyFeedPlaceholder(
    category: String,
    onAddChannel: (String, (Boolean, String) -> Unit) -> Unit,
    onRefresh: () -> Unit
) {
    var channelInput by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = SublyRedPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Welcome to Your Subly Feed",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Add your favorite YouTube channels below to load their real, live videos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add YouTube Channel",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = channelInput,
                    onValueChange = {
                        channelInput = it
                        statusMessage = null
                    },
                    placeholder = { Text("Enter channel name or @handle", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                statusMessage?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = if (isError) MaterialTheme.colorScheme.error else SublyRedPrimary
                    )
                }

                Button(
                    onClick = {
                        if (channelInput.isNotBlank()) {
                            isAdding = true
                            statusMessage = null
                            onAddChannel(channelInput.trim()) { success, msg ->
                                isAdding = false
                                statusMessage = msg
                                isError = !success
                                if (success) {
                                    channelInput = ""
                                }
                            }
                        }
                    },
                    enabled = !isAdding && channelInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SublyRedPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAdding) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fetching live videos…", fontSize = 13.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Channel & Load Videos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onRefresh,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Feed", fontSize = 13.sp)
            }
        }
    }
}
