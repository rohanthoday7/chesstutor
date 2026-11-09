package com.chesstutor.data.local

import android.content.Context
import android.content.SharedPreferences

enum class BoardTheme(val displayName: String, val lightHex: Long, val darkHex: Long) {
    GREEN("Classic Green", 0xFFEBECD0, 0xFF739552),
    WOOD("Wood Finish", 0xFFE3C9A8, 0xFFB37B47),
    DARK("Dark Charcoal", 0xFFB8B8B8, 0xFF585858),
    GLASS("Ocean Blue", 0xFFD8E2E6, 0xFF527D9A)
}

data class UserSettings(
    val boardTheme: BoardTheme = BoardTheme.GREEN,
    val soundEnabled: Boolean = true,
    val autoPromoteQueen: Boolean = true,
    val showLegalMoveDots: Boolean = true,
    val showEvaluationBar: Boolean = true,
    val showCoordinates: Boolean = true,
    val username: String = "GrandmasterUser",
    val avatarEmoji: String = "♟️",
    val rapidRating: Int = 1200,
    val blitzRating: Int = 1150,
    val bulletRating: Int = 1100,
    val puzzleRating: Int = 1250,
    val puzzleRushHighScore: Int = 14
)

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("chess_tutor_prefs", Context.MODE_PRIVATE)

    fun getSettings(): UserSettings {
        val themeName = prefs.getString("board_theme", BoardTheme.GREEN.name) ?: BoardTheme.GREEN.name
        val theme = runCatching { BoardTheme.valueOf(themeName) }.getOrDefault(BoardTheme.GREEN)

        return UserSettings(
            boardTheme = theme,
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            autoPromoteQueen = prefs.getBoolean("auto_promote_queen", true),
            showLegalMoveDots = prefs.getBoolean("show_legal_dots", true),
            showEvaluationBar = prefs.getBoolean("show_eval_bar", true),
            showCoordinates = prefs.getBoolean("show_coordinates", true),
            username = prefs.getString("username", "GrandmasterUser") ?: "GrandmasterUser",
            avatarEmoji = prefs.getString("avatar_emoji", "♟️") ?: "♟️",
            rapidRating = prefs.getInt("rapid_rating", 1200),
            blitzRating = prefs.getInt("blitz_rating", 1150),
            bulletRating = prefs.getInt("bullet_rating", 1100),
            puzzleRating = prefs.getInt("puzzle_rating", 1250),
            puzzleRushHighScore = prefs.getInt("puzzle_rush_high", 14)
        )
    }

    fun saveSettings(settings: UserSettings) {
        prefs.edit().apply {
            putString("board_theme", settings.boardTheme.name)
            putBoolean("sound_enabled", settings.soundEnabled)
            putBoolean("auto_promote_queen", settings.autoPromoteQueen)
            putBoolean("show_legal_dots", settings.showLegalMoveDots)
            putBoolean("show_eval_bar", settings.showEvaluationBar)
            putBoolean("show_coordinates", settings.showCoordinates)
            putString("username", settings.username)
            putString("avatar_emoji", settings.avatarEmoji)
            putInt("rapid_rating", settings.rapidRating)
            putInt("blitz_rating", settings.blitzRating)
            putInt("bullet_rating", settings.bulletRating)
            putInt("puzzle_rating", settings.puzzleRating)
            putInt("puzzle_rush_high", settings.puzzleRushHighScore)
            apply()
        }
    }

    fun updatePuzzleRating(newRating: Int) {
        prefs.edit().putInt("puzzle_rating", newRating).apply()
    }

    fun updatePuzzleRushHighScore(score: Int) {
        val current = prefs.getInt("puzzle_rush_high", 0)
        if (score > current) {
            prefs.edit().putInt("puzzle_rush_high", score).apply()
        }
    }
}

