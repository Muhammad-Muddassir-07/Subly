package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.models.Channel
import com.example.data.models.Video
import com.example.ui.SublyViewModel
import com.example.ui.components.AddToPlaylistBottomSheet
import com.example.ui.components.ChannelAvatarStoryItem
import com.example.ui.components.VideoCard
import com.example.ui.components.formatSubscriberCount
import com.example.ui.theme.SublyRedPrimary

@Composable
fun SubscriptionsScreen(
    viewModel: SublyViewModel,
    onVideoSelected: (Video) -> Unit,
    onChannelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val subscribedChannels by viewModel.subscribedChannels.collectAsState()
    val allChannels by viewModel.allChannels.collectAsState()
    val selectedChannelId by viewModel.subscriptionFilterChannelId.collectAsState()
    val videos by viewModel.subscriptionsFeedVideos.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    var playlistVideoToSave by remember { mutableStateOf<Video?>(null) }
    var showManageChannelsModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subscriptions Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Subscriptions",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${subscribedChannels.size} channels followed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = { showManageChannelsModal = true },
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("manage_subscriptions_button")
            ) {
                Text("Manage", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Subscriptions Story Rail
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // "All" Pill
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.setSubscriptionChannelFilter(null) }
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedChannelId == null) SublyRedPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ALL",
                            color = if (selectedChannelId == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Feed",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedChannelId == null) SublyRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(subscribedChannels, key = { it.id }) { channel ->
                ChannelAvatarStoryItem(
                    channel = channel,
                    isSelected = channel.id == selectedChannelId,
                    onClick = { viewModel.setSubscriptionChannelFilter(channel.id) }
                )
            }
        }

        // Selected Channel Indicator Banner if active
        selectedChannelId?.let { chId ->
            val channel = subscribedChannels.find { it.id == chId }
            if (channel != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Showing videos from ${channel.title}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reset",
                            color = SublyRedPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.setSubscriptionChannelFilter(null) }
                        )
                    }
                }
            }
        }

        // Subscriptions Video Feed
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (videos.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subscriptions,
                            contentDescription = null,
                            tint = SublyRedPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No recent videos found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Subscribe to more creators to see their latest uploads here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showManageChannelsModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SublyRedPrimary)
                        ) {
                            Text("Discover Channels")
                        }
                    }
                }
            } else {
                items(videos, key = { it.id }) { video ->
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

    if (showManageChannelsModal) {
        ManageChannelsDialog(
            allChannels = allChannels,
            onToggleSubscribe = { viewModel.toggleSubscribe(it) },
            onChannelClick = {
                showManageChannelsModal = false
                onChannelSelected(it)
            },
            onDismiss = { showManageChannelsModal = false }
        )
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
fun ManageChannelsDialog(
    allChannels: List<Channel>,
    onToggleSubscribe: (Channel) -> Unit,
    onChannelClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Manage Subscriptions", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allChannels, key = { it.id }) { channel ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(channel.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = channel.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { onChannelClick(channel.id) }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = channel.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = formatSubscriberCount(channel.subscriberCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onToggleSubscribe(channel) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (channel.isSubscribed) MaterialTheme.colorScheme.surfaceVariant else SublyRedPrimary,
                                contentColor = if (channel.isSubscribed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (channel.isSubscribed) "Subscribed" else "Subscribe",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SublyRedPrimary)
            ) {
                Text("Done")
            }
        }
    )
}
