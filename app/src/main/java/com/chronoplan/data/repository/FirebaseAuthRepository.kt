package com.chronoplan.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(private val auth: FirebaseAuth) {

    suspend fun signInEmail(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun signUpEmail(email: String, password: String): Result<Unit> = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun signInWithCredential(credential: AuthCredential): Result<Unit> = try {
        auth.signInWithCredential(credential).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    fun signOut() {
        auth.signOut()
    }

    fun currentUserId(): String? = auth.currentUser?.uid
}
