package com.fachri.heretoslay.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Handles Authentication and local player session state.
 *
 * Resilient design:
 * 1. Tries Firebase Anonymous Auth first.
 * 2. If Firebase project has admin restrictions or disabled user sign-ups,
 *    gracefully falls back to a persistent device-unique UUID stored in SharedPreferences.
 * 3. Works seamlessly with Firestore test-mode rules so friends can play immediately.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private var cachedPlayerName: String = ""

    suspend fun getOrSignIn(): String {
        // 1. If Firebase Auth already has an active user, return it
        val currentUser = auth.currentUser
        if (currentUser != null) {
            return currentUser.uid
        }

        // 2. Try Firebase Anonymous Sign-In
        try {
            val result = auth.signInAnonymously().await()
            val uid = result.user?.uid
            if (uid != null) {
                return uid
            }
        } catch (_: Exception) {
            // Firebase Auth may be restricted by project settings.
            // Fall back seamlessly to persistent device UUID.
        }

        return getOrCreatePersistentUid()
    }

    fun getCurrentUid(): String {
        return auth.currentUser?.uid ?: getOrCreatePersistentUid()
    }

    fun setPlayerName(name: String) {
        cachedPlayerName = name.trim()
        try {
            val context = FirebaseApp.getInstance().applicationContext
            context.getSharedPreferences("hts_auth", Context.MODE_PRIVATE)
                .edit()
                .putString("player_name", cachedPlayerName)
                .apply()
        } catch (_: Exception) {}
    }

    fun getPlayerName(): String {
        if (cachedPlayerName.isNotBlank()) return cachedPlayerName
        return try {
            val context = FirebaseApp.getInstance().applicationContext
            context.getSharedPreferences("hts_auth", Context.MODE_PRIVATE)
                .getString("player_name", "") ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun getOrCreatePersistentUid(): String {
        return try {
            val context = FirebaseApp.getInstance().applicationContext
            val prefs = context.getSharedPreferences("hts_auth", Context.MODE_PRIVATE)
            var uid = prefs.getString("persistent_uid", null)
            if (uid == null) {
                uid = "player_" + UUID.randomUUID().toString().take(12)
                prefs.edit().putString("persistent_uid", uid).apply()
            }
            uid
        } catch (_: Exception) {
            inMemoryFallbackUid
        }
    }

    companion object {
        private val inMemoryFallbackUid: String by lazy {
            "player_" + UUID.randomUUID().toString().take(12)
        }
    }
}
