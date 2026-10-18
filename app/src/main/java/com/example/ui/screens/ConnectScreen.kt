package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ValidationResult
import com.example.ui.SublyViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SublyRedPrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun ConnectScreen(
    viewModel: SublyViewModel,
    onConnectedSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.activity.compose.BackHandler {
        // Disallow back-navigation past ConnectScreen unless authenticated or connected
    }

    val context = LocalContext.current
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()

    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var showApiKeyInput by remember { mutableStateOf(false) }
    var apiKeyText by remember { mutableStateOf("") }
    var apiKeyObscured by remember { mutableStateOf(true) }
    var apiKeyError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("connect_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Emblem
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = SublyRedPrimary,
                    shadowElevation = 12.dp,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Subly Play Icon",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome to Subly",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connect your Google or YouTube account to sync subscriptions and access real-time feeds.",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Feature Highlights
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ConnectFeatureRow(
                            icon = Icons.Default.Subscriptions,
                            title = "Subscribed Channels",
                            description = "Follow all your favorite creators without algorithmic noise."
                        )
                        HorizontalDivider(color = Color(0xFF334155))
                        ConnectFeatureRow(
                            icon = Icons.Default.CloudSync,
                            title = "Real-Time Cloud Sync",
                            description = "Keep your watch history, playlists, and watch later synced."
                        )
                        HorizontalDivider(color = Color(0xFF334155))
                        ConnectFeatureRow(
                            icon = Icons.Default.Lock,
                            title = "Secure & Private",
                            description = "OAuth token or API key stored encrypted on your device."
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary: Connect with Google / YouTube
                Button(
                    onClick = {
                        authErrorMessage = null
                        viewModel.signInWithGoogle(context) { success, error ->
                            if (success) {
                                authErrorMessage = null
                                onConnectedSuccess()
                            } else {
                                authErrorMessage = error
                            }
                        }
                    },
                    enabled = !isAuthenticating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SublyRedPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("connect_google_button")
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Signing in with Google…",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        color = SublyRedPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Connect with Google or YouTube",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // Error feedback card if sign in failed or was cancelled
                AnimatedVisibility(visible = authErrorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF451A1A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = authErrorMessage ?: "Sign in could not be completed.",
                            color = Color(0xFFFFB4AB),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Information Banner: No API key needed
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Automatic live YouTube feed sync is enabled. No API key required!",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Demo / Direct Access Button (Instant Entry with Live Sync)
                OutlinedButton(
                    onClick = {
                        viewModel.connectAccount(
                            email = "user@subly.app",
                            name = "Subly Member",
                            isDemo = false
                        )
                        viewModel.syncNow()
                        onConnectedSuccess()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("connect_demo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = SublyRedPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Subly Feed Directly",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                // Advanced / Optional Token or Key (Collapsed by default)
                var showAdvancedOptions by remember { mutableStateOf(false) }
                var oauthTokenText by remember { mutableStateOf("") }
                var isOAuthSyncing by remember { mutableStateOf(false) }
                var oauthError by remember { mutableStateOf<String?>(null) }

                Text(
                    text = if (showAdvancedOptions) "Hide Advanced Options" else "Advanced: Custom OAuth Token or API Key",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedOptions = !showAdvancedOptions }
                        .padding(vertical = 4.dp)
                )

                AnimatedVisibility(visible = showAdvancedOptions) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Optional: If you have a personal YouTube OAuth Bearer token or Data API key, you can enter it below:",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )

                        OutlinedTextField(
                            value = oauthTokenText,
                            onValueChange = {
                                oauthTokenText = it
                                oauthError = null
                            },
                            label = { Text("YouTube OAuth Access Token") },
                            placeholder = { Text("ya29.a0...") },
                            isError = oauthError != null,
                            supportingText = {
                                oauthError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (oauthTokenText.isBlank()) {
                                    oauthError = "Please enter an OAuth Bearer token."
                                    return@Button
                                }
                                isOAuthSyncing = true
                                oauthError = null
                                viewModel.syncSubscriptionsWithOAuth(oauthTokenText) { success, msg ->
                                    isOAuthSyncing = false
                                    if (success) {
                                        onConnectedSuccess()
                                    } else {
                                        oauthError = msg
                                    }
                                }
                            },
                            enabled = !isOAuthSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = SublyRedPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isOAuthSyncing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing…")
                            } else {
                                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync with Token")
                            }
                        }

                        HorizontalDivider(color = Color(0xFF334155))

                        OutlinedTextField(
                            value = apiKeyText,
                            onValueChange = {
                                apiKeyText = it
                                apiKeyError = null
                            },
                            label = { Text("YouTube API Key (Optional)") },
                            placeholder = { Text("AIzaSy…") },
                            visualTransformation = if (apiKeyObscured) PasswordVisualTransformation() else VisualTransformation.None,
                            trailingIcon = {
                                IconButton(onClick = { apiKeyObscured = !apiKeyObscured }) {
                                    Icon(
                                        imageVector = if (apiKeyObscured) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            isError = apiKeyError != null,
                            supportingText = {
                                apiKeyError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.saveApiKey(apiKeyText) { result ->
                                    if (result is ValidationResult.Invalid) {
                                        apiKeyError = result.errorMessage
                                    } else {
                                        viewModel.connectAccount(
                                            email = "custom.api@subly.local",
                                            name = "API Key Member",
                                            isDemo = false
                                        )
                                        viewModel.syncNow()
                                        onConnectedSuccess()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save API Key")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = SublyRedPrimary.copy(alpha = 0.18f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SublyRedPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 16.sp
            )
        }
    }
}
