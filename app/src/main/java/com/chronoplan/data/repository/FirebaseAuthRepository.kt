package com.chronoplan.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ActionCodeSettings
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val user = auth.currentUser

            // 🔹 Konfigurasi verifikasi email
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://chronoplan-last-project.web.app/") // link domain web kamu
                .setHandleCodeInApp(true) // ✅ wajib true biar buka di aplikasi
                .setAndroidPackageName(
                    "com.chronoplan",  // sesuai dengan AndroidManifest
                    true,              // aplikasi harus terinstal
                    "21"               // min versi app
                )
                .build()

            user?.sendEmailVerification(actionCodeSettings)?.await()

            Result.success("Verifikasi email dikirim ke ${user?.email}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val user = auth.currentUser
            if (user != null && user.isEmailVerified) {
                Result.success(user)
            } else {
                auth.signOut()
                Result.failure(Exception("Email belum diverifikasi. Silakan cek email Anda."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reloadUser(): Boolean {
        val user = auth.currentUser
        return if (user != null) {
            user.reload().await()
            user.isEmailVerified
        } else {
            false
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}
