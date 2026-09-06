package com.fachri.heretoslay.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Handles Anonymous Firebase Authentication and local player session state.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private var cachedPlayerName: String = ""

    suspend fun getOrSignIn(): String {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            return currentUser.uid
        }
        val result = auth.signInAnonymously().await()
        val user = result.user ?: throw IllegalStateException("Failed to sign in anonymously with Firebase")
        return user.uid
    }

    fun getCurrentUid(): String? = auth.currentUser?.uid

    fun setPlayerName(name: String) {
        cachedPlayerName = name.trim()
    }

    fun getPlayerName(): String = cachedPlayerName
}
