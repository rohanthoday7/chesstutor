package com.chesstutor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.ChessRules
import com.chesstutor.core.chess.Move
import com.chesstutor.core.chess.PgnUtils
import com.chesstutor.core.chess.PieceType
import com.chesstutor.core.chess.Square
import com.chesstutor.data.local.GameArchiveRepository
import com.chesstutor.data.local.OfflinePuzzle
import com.chesstutor.data.remote.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PuzzleMode {
    DAILY,
    RATED,
    PUZZLE_RUSH
}

data class PuzzleUiState(
    val mode: PuzzleMode = PuzzleMode.DAILY,
    val title: String = "Daily Puzzle",
    val rating: Int = 1200,
    val description: String = "Find the best move for White",
    val gameState: ChessGameState = ChessGameState.initial(),
    val selectedSquare: Square? = null,
    val legalMovesForSelected: List<Move> = emptyList(),
    val pendingPromotionMove: Pair<Square, Square>? = null,
    val solutionMoves: List<String> = emptyList(),
    val currentSolutionStep: Int = 0,
    val isSolved: Boolean = false,
    val isFailed: Boolean = false,
    val isLoading: Boolean = false,
    // Puzzle Rush specifics:
    val rushScore: Int = 0,
    val rushStrikes: Int = 0,
    val rushTimeRemainingSec: Int = 180,
    val isRushActive: Boolean = false,
    val rushHighScore: Int = 14
)

class PuzzleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    private var rushTimerJob: Job? = null
    private var currentOfflineIndex = 0

    init {
        loadDailyPuzzle()
    }

    fun loadDailyPuzzle() {
        _uiState.update { it.copy(isLoading = true, mode = PuzzleMode.DAILY) }
        viewModelScope.launch {
            try {
                val response = ApiClient.chessComApi.getDailyPuzzle()
                val parsedState = ChessRules.fromFen(response.fen)
                val moves = extractMovesFromPgn(response.pgn)

                _uiState.update {
                    it.copy(
                        title = response.title.ifBlank { "Daily Puzzle" },
                        rating = 1450,
                        description = "Find the winning continuation",
                        gameState = parsedState,
                        solutionMoves = moves,
                        currentSolutionStep = 0,
                        isSolved = false,
                        isFailed = false,
                        isLoading = false,
                        selectedSquare = null,
                        legalMovesForSelected = emptyList()
                    )
                }
            } catch (e: Exception) {
                // Graceful fallback to offline curated puzzles
                loadOfflinePuzzle(GameArchiveRepository.OFFLINE_PUZZLES[0])
            }
        }
    }

    fun startPuzzleRush() {
        rushTimerJob?.cancel()
        currentOfflineIndex = 0
        _uiState.update {
            it.copy(
                mode = PuzzleMode.PUZZLE_RUSH,
                rushScore = 0,
                rushStrikes = 0,
                rushTimeRemainingSec = 180,
                isRushActive = true
            )
        }

        loadNextRushPuzzle()

        // 3-minute countdown timer
        rushTimerJob = viewModelScope.launch {
            while (_uiState.value.rushTimeRemainingSec > 0 && _uiState.value.rushStrikes < 3) {
                delay(1000L)
                val remaining = _uiState.value.rushTimeRemainingSec - 1
                _uiState.update { it.copy(rushTimeRemainingSec = remaining) }
            }
            _uiState.update { it.copy(isRushActive = false) }
        }
    }

    private fun loadNextRushPuzzle() {
        val puzzles = GameArchiveRepository.OFFLINE_PUZZLES
        val puzzle = puzzles[currentOfflineIndex % puzzles.size]
        currentOfflineIndex++

        loadOfflinePuzzle(puzzle)
    }

    fun loadOfflinePuzzle(puzzle: OfflinePuzzle) {
        val state = ChessRules.fromFen(puzzle.fen)
        _uiState.update {
            it.copy(
                title = puzzle.title,
                rating = puzzle.rating,
                description = puzzle.description,
                gameState = state,
                solutionMoves = puzzle.solutionSans,
                currentSolutionStep = 0,
                isSolved = false,
                isFailed = false,
                isLoading = false,
                selectedSquare = null,
                legalMovesForSelected = emptyList()
            )
        }
    }

    fun onSquareClicked(square: Square) {
        val state = _uiState.value
        if (state.isSolved || state.isFailed || state.isLoading) return

        val selected = state.selectedSquare

        if (selected == null) {
            val piece = state.gameState.board[square]
            if (piece != null && piece.color == state.gameState.turn) {
                val legalMoves = ChessRules.getLegalMovesForSquare(state.gameState, square)
                _uiState.update {
                    it.copy(selectedSquare = square, legalMovesForSelected = legalMoves)
                }
            }
        } else {
            val matchingMove = state.legalMovesForSelected.firstOrNull { it.to == square }
            if (matchingMove != null) {
                val movingPiece = state.gameState.board[selected]
                val isPawnPromotion = movingPiece?.type == PieceType.PAWN &&
                        square.rank == state.gameState.turn.promotionRank

                if (isPawnPromotion && matchingMove.promotion == null) {
                    _uiState.update { it.copy(pendingPromotionMove = Pair(selected, square)) }
                } else {
                    attemptSolutionMove(matchingMove)
                }
            } else {
                val piece = state.gameState.board[square]
                if (piece != null && piece.color == state.gameState.turn) {
                    val legalMoves = ChessRules.getLegalMovesForSquare(state.gameState, square)
                    _uiState.update {
                        it.copy(selectedSquare = square, legalMovesForSelected = legalMoves)
                    }
                } else {
                    _uiState.update { it.copy(selectedSquare = null, legalMovesForSelected = emptyList()) }
                }
            }
        }
    }

    fun onPromotionSelected(promotionType: PieceType) {
        val pending = _uiState.value.pendingPromotionMove ?: return
        val move = Move(pending.first, pending.second, promotion = promotionType)
        _uiState.update { it.copy(pendingPromotionMove = null) }
        attemptSolutionMove(move)
    }

    private fun attemptSolutionMove(move: Move) {
        val current = _uiState.value
        val expectedSan = current.solutionMoves.getOrNull(current.currentSolutionStep)

        val nextGameState = ChessRules.makeMove(current.gameState, move)
        val moveSan = nextGameState.moveHistory.lastOrNull()?.san ?: ""

        // Check if move matches solution
        val isCorrect = expectedSan == null ||
                moveSan.replace("+", "").replace("#", "") == expectedSan.replace("+", "").replace("#", "")

        if (isCorrect) {
            val nextStep = current.currentSolutionStep + 1
            if (nextStep >= current.solutionMoves.size) {
                // Puzzle fully solved!
                _uiState.update {
                    it.copy(
                        gameState = nextGameState,
                        isSolved = true,
                        isFailed = false,
                        rushScore = it.rushScore + 1,
                        selectedSquare = null,
                        legalMovesForSelected = emptyList()
                    )
                }
                if (current.mode == PuzzleMode.PUZZLE_RUSH && current.isRushActive) {
                    viewModelScope.launch {
                        delay(600L)
                        loadNextRushPuzzle()
                    }
                }
            } else {
                // Play opponent's response from solution
                _uiState.update {
                    it.copy(
                        gameState = nextGameState,
                        currentSolutionStep = nextStep,
                        selectedSquare = null,
                        legalMovesForSelected = emptyList()
                    )
                }
                playOpponentResponse(nextStep)
            }
        } else {
            // Wrong move
            val newStrikes = current.rushStrikes + 1
            _uiState.update {
                it.copy(
                    isFailed = true,
                    rushStrikes = newStrikes,
                    selectedSquare = null,
                    legalMovesForSelected = emptyList()
                )
            }
            if (current.mode == PuzzleMode.PUZZLE_RUSH && newStrikes < 3) {
                viewModelScope.launch {
                    delay(800L)
                    loadNextRushPuzzle()
                }
            }
        }
    }

    private fun playOpponentResponse(opponentStepIndex: Int) {
        viewModelScope.launch {
            delay(500L)
            val current = _uiState.value
            val opponentSan = current.solutionMoves.getOrNull(opponentStepIndex) ?: return@launch
            val opponentMove = PgnUtils.parseSanMove(current.gameState, opponentSan)

            if (opponentMove != null) {
                val stateAfterOpponent = ChessRules.makeMove(current.gameState, opponentMove)
                _uiState.update {
                    it.copy(
                        gameState = stateAfterOpponent,
                        currentSolutionStep = opponentStepIndex + 1
                    )
                }
            }
        }
    }

    private fun extractMovesFromPgn(pgn: String): List<String> {
        val lines = pgn.lines()
        val moveLines = lines.filter { !it.startsWith("[") && it.isNotBlank() }
        val fullText = moveLines.joinToString(" ")
        val tokens = fullText.split("\\s+".toRegex())

        val moves = mutableListOf<String>()
        for (token in tokens) {
            if (token.contains(".") || token == "*" || token == "1-0" || token == "0-1" || token == "1/2-1/2") {
                continue
            }
            if (token.isNotBlank()) {
                moves.add(token)
            }
        }
        return moves
    }

    override fun onCleared() {
        super.onCleared()
        rushTimerJob?.cancel()
    }
}

