package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChannelEntity::class,
        VideoEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        WatchHistoryEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SublyDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun channelDao(): ChannelDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: SublyDatabase? = null

        fun getInstance(context: Context): SublyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SublyDatabase::class.java,
                    "subly_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                // Ensure initial seed data is populated on background thread
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        DefaultSublyData.populateInitialData(instance)
                    } catch (e: Exception) {
                        android.util.Log.e("SublyDatabase", "Error populating initial data: ${e.message}", e)
                    }
                }

                instance
            }
        }
    }
}
