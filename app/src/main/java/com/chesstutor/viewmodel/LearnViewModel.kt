package com.chesstutor.viewmodel

import androidx.lifecycle.ViewModel
import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.ChessRules
import com.chesstutor.core.chess.PgnUtils
import com.chesstutor.core.engine.OpeningBook
import com.chesstutor.core.engine.OpeningEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LearnUiState(
    val selectedOpening: OpeningEntry? = OpeningBook.OPENINGS[0],
    val currentPlyIndex: Int = 0,
    val gameState: ChessGameState = ChessGameState.initial(),
    val openingsList: List<OpeningEntry> = OpeningBook.OPENINGS
)

class LearnViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    fun selectOpening(opening: OpeningEntry) {
        _uiState.update {
            it.copy(
                selectedOpening = opening,
                currentPlyIndex = 0,
                gameState = ChessGameState.initial()
            )
        }
    }

    fun nextMove() {
        val current = _uiState.value
        val opening = current.selectedOpening ?: return
        if (current.currentPlyIndex < opening.movesSan.size) {
            val san = opening.movesSan[current.currentPlyIndex]
            val move = PgnUtils.parseSanMove(current.gameState, san)
            if (move != null) {
                val nextState = ChessRules.makeMove(current.gameState, move)
                _uiState.update {
                    it.copy(
                        currentPlyIndex = it.currentPlyIndex + 1,
                        gameState = nextState
                    )
                }
            }
        }
    }

    fun previousMove() {
        val current = _uiState.value
        val opening = current.selectedOpening ?: return
        if (current.currentPlyIndex > 0) {
            val targetPly = current.currentPlyIndex - 1
            var state = ChessGameState.initial()
            for (i in 0 until targetPly) {
                val san = opening.movesSan[i]
                val move = PgnUtils.parseSanMove(state, san)
                if (move != null) {
                    state = ChessRules.makeMove(state, move)
                }
            }
            _uiState.update {
                it.copy(
                    currentPlyIndex = targetPly,
                    gameState = state
                )
            }
        }
    }

    fun resetBoard() {
        _uiState.update {
            it.copy(
                currentPlyIndex = 0,
                gameState = ChessGameState.initial()
            )
        }
    }
}

