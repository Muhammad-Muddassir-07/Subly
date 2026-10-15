package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(
    private val auth: FirebaseAuth = Firebase.auth
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Observes Firebase Auth state reactively as a Flow.
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Interactive Google Sign-In using Jetpack Credential Manager and Firebase Auth.
     * Uses context.findActivity() to ensure the Credential Manager bottom sheet displays properly.
     */
    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        val activity = context.findActivity() ?: context
        val credentialManager = CredentialManager.create(activity)
        val serverClientId = context.getString(R.string.default_web_client_id)

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(IllegalStateException("Firebase user was null after authentication."))
                }
            } else {
                Result.failure(IllegalStateException("Unexpected credential type: ${credential::class.java.name}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In was cancelled by user.", e)
            Result.failure(e)
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No Google accounts found on device.", e)
            Result.failure(IllegalStateException("No Google accounts found on device. Please add a Google account or continue in Demo mode.", e))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager failure: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase authentication error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(context: Context? = null) {
        auth.signOut()
        if (context != null) {
            try {
                val activity = context.findActivity() ?: context
                val credentialManager = CredentialManager.create(activity)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear credential state", e)
            }
        }
    }

    private fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    companion object {
        private const val TAG = "FirebaseAuthManager"
    }
}
