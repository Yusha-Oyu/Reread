package com.reread.app.utils

import android.content.Context
import com.reread.app.data.User

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "reread_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_GENRE_PREFS = "genre_preferences"
        private const val KEY_HAS_SET_GENRES = "has_set_genres"
        private var roleListener: (() -> Unit)? = null

        fun setRoleChangeListener(listener: () -> Unit) {
            roleListener = listener
        }

        fun notifyRoleChanged() {
            roleListener?.invoke()
        }
    }

    fun save(user: User) {
        prefs.edit()
            .putInt(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_ROLE, user.role)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun setRole(newRole: String) {
        prefs.edit().putString(KEY_ROLE, newRole).apply()
        notifyRoleChanged()
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun saveGenrePreferences(genres: List<String>) {
        prefs.edit()
            .putString(KEY_GENRE_PREFS, genres.joinToString(","))
            .putBoolean(KEY_HAS_SET_GENRES, true)
            .apply()
    }

    fun getGenrePreferences(): List<String> {
        val saved = prefs.getString(KEY_GENRE_PREFS, "") ?: ""
        return if (saved.isBlank()) emptyList() else saved.split(",")
    }

    fun hasSetGenrePreferences(): Boolean = prefs.getBoolean(KEY_HAS_SET_GENRES, false)

    val userId: Int get() = prefs.getInt(KEY_USER_ID, -1)
    val username: String get() = prefs.getString(KEY_USERNAME, "") ?: ""
    val email: String get() = prefs.getString(KEY_EMAIL, "") ?: ""
    val role: String get() = prefs.getString(KEY_ROLE, "buyer") ?: "buyer"

    val isAdmin: Boolean get() = role == "admin"
    val isSeller: Boolean get() = role == "seller"
    val isBuyer: Boolean get() = role == "buyer"
}