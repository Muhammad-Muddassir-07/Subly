package com.example.data.repository

import android.content.Context
import com.example.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Data model representing a synced subscription item stored in Firestore.
 * Path: users/{userId}/subscriptions/{channelId}
 */
data class UserSubscriptionRecord(
    val channelId: String = "",
    val channelTitle: String = "",
    val channelAvatarUrl: String = "",
    val subscribedAt: Long = System.currentTimeMillis(),
    val notificationsEnabled: Boolean = true,
    val unreadCount: Int = 0,
    val lastSyncedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "channelId" to channelId,
        "channelTitle" to channelTitle,
        "channelAvatarUrl" to channelAvatarUrl,
        "subscribedAt" to subscribedAt,
        "notificationsEnabled" to notificationsEnabled,
        "unreadCount" to unreadCount,
        "lastSyncedAt" to lastSyncedAt
    )
}

sealed class SubscriptionSyncState {
    data object Idle : SubscriptionSyncState()
    data object Syncing : SubscriptionSyncState()
    data class Success(val message: String, val syncedCount: Int) : SubscriptionSyncState()
    data class Error(val message: String) : SubscriptionSyncState()
}

/**
 * Repository pattern handling remote user subscription data syncing with Firebase Firestore.
 */
class FirestoreSubscriptionRepository(private val db: FirebaseFirestore) {

    // Secondary constructor resolving custom database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun subscriptionsCollection(userId: String) =
        db.collection("users").document(userId).collection("subscriptions")

    /**
     * Real-time Flow listening to user's remote subscriptions via snapshotListener.
     */
    fun getSubscriptionsFlow(userId: String): Flow<List<UserSubscriptionRecord>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = subscriptionsCollection(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val records = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(UserSubscriptionRecord::class.java)
                    } ?: emptyList()
                    trySend(records)
                }
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Save or update a subscription in the user's remote Firestore collection.
     */
    suspend fun saveSubscription(userId: String, subscription: UserSubscriptionRecord): Result<Unit> {
        return try {
            require(userId.isNotBlank()) { "User ID cannot be blank for subscription sync." }
            require(subscription.channelId.isNotBlank()) { "Channel ID cannot be blank." }

            subscriptionsCollection(userId)
                .document(subscription.channelId)
                .set(subscription.toMap(), SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a subscription from the user's remote Firestore collection.
     */
    suspend fun deleteSubscription(userId: String, channelId: String): Result<Unit> {
        return try {
            require(userId.isNotBlank()) { "User ID cannot be blank." }
            require(channelId.isNotBlank()) { "Channel ID cannot be blank." }

            subscriptionsCollection(userId)
                .document(channelId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Batch sync all local user subscriptions to Firestore in a single atomic transaction/batch.
     */
    suspend fun batchSyncSubscriptions(
        userId: String,
        subscriptions: List<UserSubscriptionRecord>
    ): Result<Int> {
        return try {
            require(userId.isNotBlank()) { "User ID cannot be blank for batch sync." }
            if (subscriptions.isEmpty()) return Result.success(0)

            val batch = db.batch()
            val collection = subscriptionsCollection(userId)

            subscriptions.forEach { record ->
                val docRef = collection.document(record.channelId)
                batch.set(docRef, record.toMap(), SetOptions.merge())
            }

            batch.commit().await()
            Result.success(subscriptions.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update notification preference for a specific channel.
     */
    suspend fun updateNotificationPreference(
        userId: String,
        channelId: String,
        enabled: Boolean
    ): Result<Unit> {
        return try {
            require(userId.isNotBlank()) { "User ID cannot be blank." }
            require(channelId.isNotBlank()) { "Channel ID cannot be blank." }

            subscriptionsCollection(userId)
                .document(channelId)
                .update("notificationsEnabled", enabled)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
