package com.chronoplan.data.repository

import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInEmail(email: String, password: String): Result<Unit>
    suspend fun signUpEmail(email: String, password: String, displayName: String?): Result<Unit>
    suspend fun signInWithCredential(credential: AuthCredential): Result<Unit>
    suspend fun signOut(): Result<Unit>
    fun currentUserId(): String?
}
