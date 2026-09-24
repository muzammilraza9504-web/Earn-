package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserAuthProfile(
    val id: String,
    val displayName: String,
    val email: String,
    val photoUrl: String,
    val authType: String = "GOOGLE"
)

object GoogleAuthHelper {
    private const val TAG = "GoogleAuthHelper"

    // Default Web Client ID placeholder for test/dev environment
    // In production, configure your OAuth 2.0 Web client ID
    private const val DEFAULT_SERVER_CLIENT_ID = "394025609994-dev-app.apps.googleusercontent.com"

    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String = DEFAULT_SERVER_CLIENT_ID
    ): Result<UserAuthProfile> = withContext(Dispatchers.IO) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val profile = UserAuthProfile(
                    id = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.id.substringBefore("@"),
                    email = googleIdTokenCredential.id,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: "",
                    authType = "GOOGLE"
                )
                Result.success(profile)
            } else {
                Result.failure(Exception("Unknown credential type received: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google sign in cancelled by user", e)
            Result.failure(e)
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google credentials found on device", e)
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign in error", e)
            Result.failure(e)
        }
    }
}
