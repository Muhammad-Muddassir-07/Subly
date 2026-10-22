package com.example.data.local

import com.example.data.models.Comment

object DefaultSublyData {

    // Initial curated real YouTube channels for instant subscriptions
    val initialChannels = listOf(
        ChannelEntity(
            id = "UCBJycsmduvYEL83R_U4JriQ",
            title = "Marques Brownlee",
            handle = "@mkbhd",
            avatarUrl = "https://yt3.googleusercontent.com/lkH37D712tiyphnu0Id0D5MwwQ7IRuwgQLVD05iMXlDyj-kOntRmgIOuDxngTalMyUrxiIRv=s176-c-k-c0x00ffffff-no-rj",
            bannerUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1000",
            subscriberCount = 19500000L,
            videoCount = 1600,
            description = "Quality Tech Videos | Geek | Consumer Electronics",
            isSubscribed = true,
            unreadVideosCount = 15
        ),
        ChannelEntity(
            id = "UCHnyfMqiRRG1u-2MsSQLbXA",
            title = "Veritasium",
            handle = "@veritasium",
            avatarUrl = "https://yt3.googleusercontent.com/ytc/AIdro_k6aK4g2U69B5G54R1U0x8c7j781n=s176-c-k-c0x00ffffff-no-rj",
            bannerUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1000",
            subscriberCount = 16500000L,
            videoCount = 420,
            description = "An element of truth - videos about science, education, and anything interesting.",
            isSubscribed = true,
            unreadVideosCount = 15
        ),
        ChannelEntity(
            id = "UCsBjURrPoezykLs9EqgamOA",
            title = "Fireship",
            handle = "@fireship",
            avatarUrl = "https://yt3.googleusercontent.com/ytc/AIdro_nF05o13r28-i99lYm1Z7K8W4j31=s176-c-k-c0x00ffffff-no-rj",
            bannerUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1000",
            subscriberCount = 3300000L,
            videoCount = 650,
            description = "High-intensity code tutorials and tech news to help you ship software faster.",
            isSubscribed = true,
            unreadVideosCount = 15
        ),
        ChannelEntity(
            id = "UCXuqSBlHAE6Xw-yeJA0Tunw",
            title = "Linus Tech Tips",
            handle = "@LinusTechTips",
            avatarUrl = "https://yt3.googleusercontent.com/ytc/AIdro_k5qWf0y=s176-c-k-c0x00ffffff-no-rj",
            bannerUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=1000",
            subscriberCount = 16000000L,
            videoCount = 6000,
            description = "Tech videos from entertaining to informative.",
            isSubscribed = true,
            unreadVideosCount = 15
        ),
        ChannelEntity(
            id = "UCsXVk37bltHxD1rDPwtNM8Q",
            title = "Kurzgesagt – In a Nutshell",
            handle = "@Kurzgesagt",
            avatarUrl = "https://yt3.googleusercontent.com/ytc/AIdro_l5n=s176-c-k-c0x00ffffff-no-rj",
            bannerUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1000",
            subscriberCount = 23000000L,
            videoCount = 220,
            description = "Videos explaining things with optimistic nihilism. Science made beautiful.",
            isSubscribed = true,
            unreadVideosCount = 15
        )
    )

    val sampleChannels = initialChannels
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
