package com.example.data.local

import com.example.data.models.Comment

object DefaultSublyData {

    // Real YouTube channels used to pull live, fresh video feeds automatically via Atom RSS
    val initialChannels = listOf(
        ChannelEntity(
            id = "UCBJycsmduvYEL83R_U4JriQ",
            title = "Marques Brownlee",
            handle = "@mkbhd",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 18700000L,
            videoCount = 1680,
            description = "Quality Tech Videos | Consumer Electronics enthusiast.",
            isSubscribed = true,
            unreadVideosCount = 5
        ),
        ChannelEntity(
            id = "UCXuqSBlHAE6Xw-yeJA0Tunw",
            title = "Linus Tech Tips",
            handle = "@linustechtips",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 15800000L,
            videoCount = 6200,
            description = "Tech reviews, computer builds, and benchmarks.",
            isSubscribed = true,
            unreadVideosCount = 5
        ),
        ChannelEntity(
            id = "UCHnyfMqiRRG1u-2MsSQLbXA",
            title = "Veritasium",
            handle = "@veritasium",
            avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 16800000L,
            videoCount = 390,
            description = "An element of truth - videos about science and education.",
            isSubscribed = true,
            unreadVideosCount = 3
        ),
        ChannelEntity(
            id = "UCsXVk37bltHxD1rDPwtNM8Q",
            title = "Kurzgesagt – In a Nutshell",
            handle = "@kurzgesagt",
            avatarUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 22400000L,
            videoCount = 215,
            description = "Videos explaining science, humanity, and the universe.",
            isSubscribed = true,
            unreadVideosCount = 2
        ),
        ChannelEntity(
            id = "UCKy1dAqELo0zrOtPkf0eTMw",
            title = "IGN",
            handle = "@ign",
            avatarUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 18000000L,
            videoCount = 12000,
            description = "Video game reviews, trailers, news, and walkthroughs.",
            isSubscribed = true,
            unreadVideosCount = 6
        ),
        ChannelEntity(
            id = "UC2C_jShtL725hvbm1arSV9w",
            title = "Lofi Girl",
            handle = "@lofigirl",
            avatarUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 14300000L,
            videoCount = 320,
            description = "24/7 lofi hip hop radio - beats to relax/study to.",
            isSubscribed = true,
            unreadVideosCount = 1
        ),
        ChannelEntity(
            id = "UCX6OQ3DkcsbYNE6H8uQQuVA",
            title = "MrBeast",
            handle = "@mrbeast",
            avatarUrl = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1000&auto=format&fit=crop&q=80",
            subscriberCount = 340000000L,
            videoCount = 820,
            description = "I want to make the world a better place before I die.",
            isSubscribed = true,
            unreadVideosCount = 2
        )
    )

    val sampleChannels = initialChannels

    // All sample mock videos are removed; all videos are fetched LIVE from real YouTube feeds
    val sampleVideos = emptyList<VideoEntity>()

    val samplePlaylists = listOf(
        PlaylistEntity(
            id = "pl_favorites",
            title = "Favorites",
            description = "My favorite YouTube videos",
            videoCount = 0,
            thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&auto=format&fit=crop&q=80",
            isCustom = true,
            updatedAt = System.currentTimeMillis()
        )
    )

    suspend fun populateInitialData(database: SublyDatabase) {
        // Clean out legacy mock sample video records
        database.videoDao().deleteLegacySampleVideos()
        database.channelDao().deleteLegacySampleChannels()

        if (database.channelDao().getCount() == 0) {
            database.channelDao().insertChannels(initialChannels)
        }

        if (database.playlistDao().getCount() == 0) {
            samplePlaylists.forEach { pl ->
                database.playlistDao().insertPlaylist(pl)
            }
        }
    }
}
