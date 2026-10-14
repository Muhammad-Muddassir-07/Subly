package com.example.data.local

import com.example.data.models.Comment

object DefaultSublyData {

    val sampleChannels = listOf(
        ChannelEntity(
            id = "UCBJycsmduvYEL83R_U4JriQ",
            title = "Marques Brownlee",
            handle = "@mkbhd",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 18700000,
            videoCount = 1680,
            description = "Quality Tech Videos | YouTuber | Geek | Consumer Electronics enthusiast.",
            isSubscribed = true,
            unreadVideosCount = 2
        ),
        ChannelEntity(
            id = "UCsXVk37bltHxD1rDPwtNM8Q",
            title = "Kurzgesagt – In a Nutshell",
            handle = "@kurzgesagt",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 22400000,
            videoCount = 215,
            description = "Videos explaining things with optimistic nihilism. We are a small team who want to make science look beautiful.",
            isSubscribed = true,
            unreadVideosCount = 1
        ),
        ChannelEntity(
            id = "UCvjgXvBlbQiydffZU7m1_aw",
            title = "Coding Tech",
            handle = "@codingtech",
            avatarUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 890000,
            videoCount = 940,
            description = "Best conferences and masterclasses for software engineers worldwide.",
            isSubscribed = true,
            unreadVideosCount = 0
        ),
        ChannelEntity(
            id = "UC7_gcs09iThXybpVgjHZ_7g",
            title = "PBS Space Time",
            handle = "@pbsspacetime",
            avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 3100000,
            videoCount = 480,
            description = "Exploring the outer reaches of space, the craziness of astrophysics, and the mysteries of the universe.",
            isSubscribed = false,
            unreadVideosCount = 0
        ),
        ChannelEntity(
            id = "UC2C_jShtL725hvbm1arSV9w",
            title = "Lofi Girl",
            handle = "@lofigirl",
            avatarUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 14300000,
            videoCount = 320,
            description = "24/7 lofi hip hop radio - beats to relax/study to.",
            isSubscribed = true,
            unreadVideosCount = 3
        ),
        ChannelEntity(
            id = "UC0vBXGSyV14uvJ4hECDOl0Q",
            title = "IGN",
            handle = "@ign",
            avatarUrl = "https://images.unsplash.com/photo-1612287233261-09855365e12f?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 17900000,
            videoCount = 12500,
            description = "The latest game reviews, trailers, and walkthroughs for consoles and PC.",
            isSubscribed = false,
            unreadVideosCount = 0
        )
    )

    val sampleVideos = listOf(
        VideoEntity(
            id = "v1_mkbhd_phone",
            title = "The Next Generation Flagship Smartphone Review: What Went Right?",
            description = "A deep dive into the latest hardware changes, titanium chassis, new camera sensor architecture, and battery benchmark tests after 30 days of daily use.",
            thumbnailUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80",
            channelId = "UCBJycsmduvYEL83R_U4JriQ",
            channelTitle = "Marques Brownlee",
            channelAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
            publishedAt = "2 hours ago",
            duration = "16:42",
            durationSeconds = 1002,
            viewCount = 1845000,
            likeCount = 124000,
            category = "Tech",
            isLiked = true,
            isWatchLater = false
        ),
        VideoEntity(
            id = "v2_kurz_blackhole",
            title = "What Happens If Earth Was Swallowed by a Black Hole?",
            description = "Spaghettification, tidal forces, and the dilation of time. We visualize the exact physics of an event horizon encounter in stunning animation.",
            thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
            channelId = "UCsXVk37bltHxD1rDPwtNM8Q",
            channelTitle = "Kurzgesagt – In a Nutshell",
            channelAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
            publishedAt = "1 day ago",
            duration = "11:15",
            durationSeconds = 675,
            viewCount = 3890000,
            likeCount = 310000,
            category = "Education",
            isLiked = false,
            isWatchLater = true
        ),
        VideoEntity(
            id = "v3_coding_kotlin",
            title = "Modern Android Architecture: Jetpack Compose & Clean State",
            description = "Building highly scalable reactive mobile applications using Kotlin Coroutines, StateFlow, Room Local Cache, and clean MVVM patterns.",
            thumbnailUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=80",
            channelId = "UCvjgXvBlbQiydffZU7m1_aw",
            channelTitle = "Coding Tech",
            channelAvatarUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=150&auto=format&fit=crop&q=80",
            publishedAt = "3 days ago",
            duration = "44:18",
            durationSeconds = 2658,
            viewCount = 420000,
            likeCount = 28500,
            category = "Tech",
            isLiked = false,
            isWatchLater = false
        ),
        VideoEntity(
            id = "v4_lofi_beats",
            title = "Chill Lofi Beats for Deep Focus, Coding and Reading",
            description = "Smooth ambient beats and relaxing rain audio to help you concentrate, study, work, or unwind.",
            thumbnailUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=800&auto=format&fit=crop&q=80",
            channelId = "UC2C_jShtL725hvbm1arSV9w",
            channelTitle = "Lofi Girl",
            channelAvatarUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150&auto=format&fit=crop&q=80",
            publishedAt = "Streamed 5 hours ago",
            duration = "2:30:00",
            durationSeconds = 9000,
            viewCount = 8200000,
            likeCount = 590000,
            category = "Music",
            isLiked = true,
            isWatchLater = false
        ),
        VideoEntity(
            id = "v5_ign_gameplay",
            title = "Top 10 Most Anticipated Open World RPGs Coming in 2026",
            description = "From massive sci-fi galaxies to hyper-detailed medieval kingdoms, here are the biggest upcoming sandbox releases.",
            thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&auto=format&fit=crop&q=80",
            channelId = "UC0vBXGSyV14uvJ4hECDOl0Q",
            channelTitle = "IGN",
            channelAvatarUrl = "https://images.unsplash.com/photo-1612287233261-09855365e12f?w=150&auto=format&fit=crop&q=80",
            publishedAt = "6 days ago",
            duration = "18:55",
            durationSeconds = 1135,
            viewCount = 950000,
            likeCount = 67000,
            category = "Gaming",
            isLiked = false,
            isWatchLater = true
        ),
        VideoEntity(
            id = "v6_space_quantum",
            title = "Is Quantum Entanglement Faster Than the Speed of Light?",
            description = "Einstein called it 'spooky action at a distance'. Today we explore Bell's theorem and the quantum mechanics of non-locality.",
            thumbnailUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=800&auto=format&fit=crop&q=80",
            channelId = "UC7_gcs09iThXybpVgjHZ_7g",
            channelTitle = "PBS Space Time",
            channelAvatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop&q=80",
            publishedAt = "1 week ago",
            duration = "15:08",
            durationSeconds = 908,
            viewCount = 1120000,
            likeCount = 98000,
            category = "Education",
            isLiked = false,
            isWatchLater = false
        )
    )

    val samplePlaylists = listOf(
        PlaylistEntity(
            id = "pl_dev_mastery",
            title = "Developer Architecture & Rust",
            description = "High-performance systems programming, clean architecture, and UI tooling.",
            videoCount = 4,
            thumbnailUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400&auto=format&fit=crop&q=80",
            isCustom = true,
            updatedAt = System.currentTimeMillis() - 86400000
        ),
        PlaylistEntity(
            id = "pl_cosmos",
            title = "Astrophysics & Space Mysteries",
            description = "Cosmology, general relativity, quantum mechanics, and deep space exploration.",
            videoCount = 6,
            thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400&auto=format&fit=crop&q=80",
            isCustom = true,
            updatedAt = System.currentTimeMillis() - 172800000
        ),
        PlaylistEntity(
            id = "pl_focus_flow",
            title = "Focus & Late Night Coding Flow",
            description = "Ambient and lofi soundscapes engineered for deep work sessions.",
            videoCount = 12,
            thumbnailUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400&auto=format&fit=crop&q=80",
            isCustom = true,
            updatedAt = System.currentTimeMillis() - 43200000
        )
    )

    val sampleComments = listOf(
        Comment(
            id = "c1",
            videoId = "v1_mkbhd_phone",
            authorName = "Alex Rivera",
            authorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80",
            text = "The matte finish on the back glass looks so clean in the camera b-roll! Great breakdown as always.",
            likes = 342,
            timeAgo = "1 hour ago"
        ),
        Comment(
            id = "c2",
            videoId = "v1_mkbhd_phone",
            authorName = "Sarah Chen",
            authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&auto=format&fit=crop&q=80",
            text = "Battery endurance comparison was the real game changer here. Glad you included the charging curve.",
            likes = 128,
            timeAgo = "45 mins ago"
        ),
        Comment(
            id = "c3",
            videoId = "v2_kurz_blackhole",
            authorName = "David Keller",
            authorAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=100&auto=format&fit=crop&q=80",
            text = "The sound design during the event horizon crossing gave me actual chills. Kurzgesagt never misses.",
            likes = 890,
            timeAgo = "12 hours ago"
        )
    )

    suspend fun populateInitialData(database: SublyDatabase) {
        if (database.channelDao().getCount() == 0) {
            database.channelDao().insertChannels(sampleChannels)
        }
        if (database.videoDao().getCount() == 0) {
            database.videoDao().insertVideos(sampleVideos)
            // Pre-seed some history and playlists
            database.historyDao().upsertHistory(
                WatchHistoryEntity(
                    videoId = "v1_mkbhd_phone",
                    progressSeconds = 420,
                    totalSeconds = 1002,
                    lastWatchedAt = System.currentTimeMillis() - 3600000
                )
            )
            database.historyDao().upsertHistory(
                WatchHistoryEntity(
                    videoId = "v4_lofi_beats",
                    progressSeconds = 2400,
                    totalSeconds = 9000,
                    lastWatchedAt = System.currentTimeMillis() - 14400000
                )
            )
            samplePlaylists.forEach { pl ->
                database.playlistDao().insertPlaylist(pl)
            }
            database.playlistDao().insertPlaylistItem(
                PlaylistItemEntity("pl_dev_mastery", "v3_coding_kotlin", System.currentTimeMillis())
            )
            database.playlistDao().insertPlaylistItem(
                PlaylistItemEntity("pl_cosmos", "v2_kurz_blackhole", System.currentTimeMillis())
            )
            database.playlistDao().insertPlaylistItem(
                PlaylistItemEntity("pl_cosmos", "v6_space_quantum", System.currentTimeMillis())
            )
            database.playlistDao().insertPlaylistItem(
                PlaylistItemEntity("pl_focus_flow", "v4_lofi_beats", System.currentTimeMillis())
            )
        }
    }
}
