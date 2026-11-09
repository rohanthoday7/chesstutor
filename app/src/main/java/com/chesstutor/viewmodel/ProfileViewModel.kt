package com.chesstutor.viewmodel

import androidx.lifecycle.ViewModel
import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.dna.ChessDnaAnalyzer
import com.chesstutor.core.dna.ChessDnaProfile
import com.chesstutor.data.local.ArchivedGame
import com.chesstutor.data.local.GameArchiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val username: String = "GrandmasterUser",
    val title: String = "CM",
    val avatarEmoji: String = "♟️",
    val rapidRating: Int = 1420,
    val blitzRating: Int = 1380,
    val bulletRating: Int = 1290,
    val puzzleRating: Int = 1540,
    val totalGamesPlayed: Int = 48,
    val winCount: Int = 29,
    val lossCount: Int = 14,
    val drawCount: Int = 5,
    val dnaProfile: ChessDnaProfile = ChessDnaAnalyzer.defaultProfile(),
    val archivedGames: List<ArchivedGame> = emptyList(),
    val selectedGameForReview: ArchivedGame? = null
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        val games = GameArchiveRepository.getAllGames()
        val wins = games.count { it.winLossDraw == "WIN" }
        val losses = games.count { it.winLossDraw == "LOSS" }
        val draws = games.count { it.winLossDraw == "DRAW" }

        // Compute real Chess DNA across games
        val gameStates = games.map { ChessGameState.initial() }
        val dna = ChessDnaAnalyzer.analyzeGames(gameStates)

        _uiState.update {
            it.copy(
                archivedGames = games,
                totalGamesPlayed = maxOf(games.size, 1),
                winCount = wins,
                lossCount = losses,
                drawCount = draws,
                dnaProfile = dna,
                selectedGameForReview = games.firstOrNull()
            )
        }
    }

    fun selectGameForReview(game: ArchivedGame) {
        _uiState.update { it.copy(selectedGameForReview = game) }
    }
}

