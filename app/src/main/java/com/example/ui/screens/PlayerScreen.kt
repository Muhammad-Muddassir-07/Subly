package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.models.Comment
import com.example.data.models.Video
import com.example.data.repository.ValidationResult
import com.example.ui.SublyViewModel
import com.example.ui.components.AddToPlaylistBottomSheet
import com.example.ui.components.VideoCard
import com.example.ui.components.VideoPlayerBox
import com.example.ui.components.formatSubscriberCount
import com.example.ui.components.formatViewCount
import com.example.ui.theme.SublyRedPrimary

@Composable
fun PlayerScreen(
    viewModel: SublyViewModel,
    onBackClick: () -> Unit,
    onChannelClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val allVideos by viewModel.feedVideos.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val channels by viewModel.allChannels.collectAsState()

    val video = playerState.currentVideo
    if (video == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No video selected", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    val channel = channels.find { it.id == video.channelId }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var commentInput by remember { mutableStateOf("") }
    var commentError by remember { mutableStateOf<String?>(null) }
    var playlistVideoToSave by remember { mutableStateOf<Video?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Player Box
        VideoPlayerBox(
            video = video,
            playerState = playerState,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onSeek = { viewModel.seekTo(it) },
            onSkipForward = { viewModel.skipForward(10) },
            onSkipBackward = { viewModel.skipBackward(10) },
            onSpeedChange = { viewModel.setPlaybackSpeed(it) },
            onToggleControls = { viewModel.toggleControlsVisibility() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
        ) {
            // Video Title & Stats
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            text = video.category,
                            color = SublyRedPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${formatViewCount(video.viewCount)} • ${video.publishedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        PlayerActionButton(
                            icon = if (playerState.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            label = if (playerState.isLiked) "Liked" else "Like",
                            isSelected = playerState.isLiked,
                            onClick = { viewModel.toggleLikeCurrentVideo() },
                            testTag = "player_like_button"
                        )
                        PlayerActionButton(
                            icon = if (playerState.isWatchLater) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            label = if (playerState.isWatchLater) "Saved" else "Watch Later",
                            isSelected = playerState.isWatchLater,
                            onClick = { viewModel.toggleWatchLater(video) },
                            testTag = "player_watch_later_button"
                        )
                        PlayerActionButton(
                            icon = Icons.Default.PlaylistAdd,
                            label = "Save",
                            isSelected = false,
                            onClick = { playlistVideoToSave = video },
                            testTag = "player_playlist_button"
                        )
                        PlayerActionButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            isSelected = false,
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, video.title)
                                    putExtra(Intent.EXTRA_TEXT, "${video.title}\nhttps://youtube.com/watch?v=${video.id}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                            },
                            testTag = "player_share_button"
                        )
                        PlayerActionButton(
                            icon = Icons.Default.PlayArrow,
                            label = "YouTube",
                            isSelected = false,
                            onClick = {
                                val ytIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com/watch?v=${video.id}"))
                                context.startActivity(ytIntent)
                            },
                            testTag = "player_youtube_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Channel Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChannelClick(video.channelId) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(video.channelAvatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = video.channelTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.channelTitle,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                            Text(
                                text = formatSubscriberCount(channel?.subscriberCount ?: 1200000L),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                channel?.let { viewModel.toggleSubscribe(it) }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (channel?.isSubscribed == true) MaterialTheme.colorScheme.surfaceVariant else SublyRedPrimary,
                                contentColor = if (channel?.isSubscribed == true) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("player_subscribe_button")
                        ) {
                            Text(
                                text = if (channel?.isSubscribed == true) "Subscribed" else "Subscribe",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expandable Description Box
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDescriptionExpanded = !isDescriptionExpanded },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Description",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = video.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isDescriptionExpanded) "Show less" else "…more",
                                color = SublyRedPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tabs for Up Next and Comments
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = SublyRedPrimary,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = SublyRedPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Up Next", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Comments (${playerState.comments.size})",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                }
            }

            if (selectedTab == 0) {
                // Related videos
                val upNextVideos = allVideos.filter { it.id != video.id }
                items(upNextVideos, key = { it.id }) { nextVideo ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        VideoCard(
                            video = nextVideo,
                            onVideoClick = { viewModel.openVideo(nextVideo) },
                            onChannelClick = onChannelClick,
                            onWatchLaterToggle = { viewModel.toggleWatchLater(it) },
                            onAddToPlaylistClick = { playlistVideoToSave = it }
                        )
                    }
                }
            } else {
                // Comments Tab
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Input Field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentInput,
                                onValueChange = {
                                    commentInput = it
                                    commentError = null
                                },
                                placeholder = { Text("Add a note or comment…") },
                                isError = commentError != null,
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("comment_input_field")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val result = viewModel.addComment(commentInput)
                                    if (result is ValidationResult.Valid) {
                                        commentInput = ""
                                        commentError = null
                                    } else if (result is ValidationResult.Invalid) {
                                        commentError = result.errorMessage
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SublyRedPrimary)
                                    .testTag("submit_comment_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Post comment",
                                    tint = Color.White
                                )
                            }
                        }

                        commentError?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }

                items(playerState.comments, key = { it.id }) { comment ->
                    CommentItemView(comment = comment)
                }
            }
        }
    }

    playlistVideoToSave?.let { v ->
        AddToPlaylistBottomSheet(
            video = v,
            playlists = playlists,
            onAddToPlaylist = { pId, vId -> viewModel.addVideoToPlaylist(pId, vId) },
            onCreatePlaylist = { title, desc, callback -> viewModel.createPlaylist(title, desc, callback) },
            onDismiss = { playlistVideoToSave = null }
        )
    }
}

@Composable
fun PlayerActionButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = if (isSelected) SublyRedPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) SublyRedPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) SublyRedPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CommentItemView(comment: Comment) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(comment.authorAvatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = comment.authorName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = comment.timeAgo,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comment.text,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ThumbUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                if (comment.likes > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${comment.likes}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
