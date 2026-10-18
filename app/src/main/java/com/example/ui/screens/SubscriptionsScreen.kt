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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
    val isSyncing by viewModel.isSyncing.collectAsState()

    var playlistVideoToSave by remember { mutableStateOf<Video?>(null) }
    var showManageChannelsModal by remember { mutableStateOf(false) }
    var showAddChannelModal by remember { mutableStateOf(false) }
    var showImportModal by remember { mutableStateOf(false) }
    var showOAuthTokenModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subscriptions Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
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

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showOAuthTokenModal = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Sync with YouTube OAuth Token",
                        tint = SublyRedPrimary
                    )
                }

                IconButton(
                    onClick = { viewModel.syncNow() },
                    modifier = Modifier.size(36.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = SublyRedPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync YouTube data",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showAddChannelModal = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SublyRedPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("add_channel_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showManageChannelsModal = true },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("manage_subscriptions_button")
                ) {
                    Text("Manage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
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
            if (isSyncing && videos.isEmpty()) {
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
                            text = "Syncing subscribed channels & real videos…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else if (videos.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SublyRedPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Subscriptions,
                                    contentDescription = null,
                                    tint = SublyRedPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (subscribedChannels.isEmpty()) "No Subscribed Channels" else "No Recent Uploads",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (subscribedChannels.isEmpty())
                                "Sync your real subscriptions with a YouTube OAuth Token, or add creators by handle (@mkbhd, @veritasium) to start watching."
                            else
                                "Your subscribed channels don't have new uploads right now. Pull down to sync or add more channels.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showOAuthTokenModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SublyRedPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OAuth Sync")
                            }
                            OutlinedButton(
                                onClick = { showAddChannelModal = true }
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Channel")
                            }
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

    // Add Channel Dialog
    if (showAddChannelModal) {
        AddChannelDialog(
            onDismiss = { showAddChannelModal = false },
            onAddChannel = { handleOrUrl, onDone ->
                viewModel.addYouTubeChannel(handleOrUrl) { success, msg ->
                    onDone(success, msg)
                    if (success) {
                        showAddChannelModal = false
                    }
                }
            },
            onOpenImport = {
                showAddChannelModal = false
                showImportModal = true
            }
        )
    }

    // Import Subscriptions Dialog
    if (showImportModal) {
        ImportSubscriptionsDialog(
            onDismiss = { showImportModal = false },
            onImport = { content, onDone ->
                viewModel.importSubscriptions(content) { success, msg ->
                    onDone(success, msg)
                    if (success) {
                        showImportModal = false
                    }
                }
            }
        )
    }

    // OAuth Token Sync Dialog
    if (showOAuthTokenModal) {
        OAuthTokenSyncDialog(
            onDismiss = { showOAuthTokenModal = false },
            onSync = { token, onDone ->
                viewModel.syncSubscriptionsWithOAuth(token) { success, msg ->
                    onDone(success, msg)
                    if (success) {
                        showOAuthTokenModal = false
                    }
                }
            }
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
    AlertDialog(
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

@Composable
fun AddChannelDialog(
    onDismiss: () -> Unit,
    onAddChannel: (String, (Boolean, String) -> Unit) -> Unit,
    onOpenImport: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickPicks = listOf("@mkbhd", "@veritasium", "@mrbeast", "@kurzgesagt", "@pewdiepie", "@tseries")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add YouTube Channel",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter any YouTube handle (e.g. @username) or channel link to fetch real uploads.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        errorMessage = null
                    },
                    label = { Text("Channel Handle or URL") },
                    placeholder = { Text("@mkbhd") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Quick picks to add instantly:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickPicks) { pick ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { input = pick }
                        ) {
                            Text(
                                text = pick,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = SublyRedPrimary
                            )
                        }
                    }
                }

                androidx.compose.material3.TextButton(
                    onClick = onOpenImport,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Import Multiple / Google Takeout CSV", fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        isLoading = true
                        onAddChannel(input.trim()) { success, msg ->
                            isLoading = false
                            if (!success) {
                                errorMessage = msg
                            }
                        }
                    }
                },
                enabled = !isLoading && input.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SublyRedPrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fetching…")
                } else {
                    Text("Add Channel")
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ImportSubscriptionsDialog(
    onDismiss: () -> Unit,
    onImport: (String, (Boolean, String) -> Unit) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Import Subscriptions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Paste channel handles (one per line) or paste your Google Takeout 'subscriptions.csv' contents directly here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = textInput,
                    onValueChange = {
                        textInput = it
                        importError = null
                    },
                    placeholder = { Text("@mkbhd\n@mrbeast\n@veritasium") },
                    minLines = 4,
                    maxLines = 8,
                    isError = importError != null,
                    supportingText = {
                        importError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        isImporting = true
                        onImport(textInput) { success, msg ->
                            isImporting = false
                            if (!success) importError = msg
                        }
                    }
                },
                enabled = !isImporting && textInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SublyRedPrimary)
            ) {
                if (isImporting) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importing…")
                } else {
                    Text("Import Channels")
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OAuthTokenSyncDialog(
    onDismiss: () -> Unit,
    onSync: (String, (Boolean, String) -> Unit) -> Unit
) {
    var tokenText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sync YouTube Subscriptions", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Provide your Google / YouTube OAuth Bearer token to fetch your real subscribed channels and video uploads via Retrofit service.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = tokenText,
                    onValueChange = {
                        tokenText = it
                        errorMessage = null
                    },
                    label = { Text("OAuth Bearer Token") },
                    placeholder = { Text("ya29.a0...") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tokenText.isBlank()) {
                        errorMessage = "Please enter an OAuth Bearer token"
                        return@Button
                    }
                    isSubmitting = true
                    errorMessage = null
                    onSync(tokenText) { success, msg ->
                        isSubmitting = false
                        if (success) {
                            onDismiss()
                        } else {
                            errorMessage = msg
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = SublyRedPrimary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Syncing…")
                } else {
                    Text("Sync Now")
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

