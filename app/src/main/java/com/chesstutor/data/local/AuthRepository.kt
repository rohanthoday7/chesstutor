package com.chesstutor.data.local

import android.content.Context
import android.content.SharedPreferences

data class UserSession(
    val isLoggedIn: Boolean = false,
    val isGuest: Boolean = true,
    val username: String = "GrandmasterUser",
    val email: String = "user@chesstutor.app",
    val rating: Int = 1200,
    val avatarEmoji: String = "♟️"
)

class AuthRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("chess_auth_prefs", Context.MODE_PRIVATE)

    fun getSession(): UserSession {
        return UserSession(
            isLoggedIn = prefs.getBoolean("is_logged_in", false),
            isGuest = prefs.getBoolean("is_guest", true),
            username = prefs.getString("auth_username", "GrandmasterUser") ?: "GrandmasterUser",
            email = prefs.getString("auth_email", "") ?: "",
            rating = prefs.getInt("auth_rating", 1200),
            avatarEmoji = prefs.getString("auth_avatar", "♟️") ?: "♟️"
        )
    }

    fun saveSession(session: UserSession) {
        prefs.edit().apply {
            putBoolean("is_logged_in", session.isLoggedIn)
            putBoolean("is_guest", session.isGuest)
            putString("auth_username", session.username)
            putString("auth_email", session.email)
            putInt("auth_rating", session.rating)
            putString("auth_avatar", session.avatarEmoji)
            apply()
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}

