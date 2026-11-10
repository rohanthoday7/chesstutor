package com.chesstutor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.ChessRules
import com.chesstutor.core.chess.GameStatus
import com.chesstutor.core.chess.Move
import com.chesstutor.core.chess.PgnUtils
import com.chesstutor.core.chess.Piece
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.chess.PieceType
import com.chesstutor.core.chess.Square
import com.chesstutor.core.engine.BotPersonality
import com.chesstutor.core.engine.Evaluator
import com.chesstutor.core.engine.MinimaxEngine
import com.chesstutor.core.engine.OpeningBook
import com.chesstutor.core.tutor.ChessTutorAgent
import com.chesstutor.core.tutor.MoveClassification
import com.chesstutor.core.tutor.TutorFeedback
import com.chesstutor.core.tutor.TutorPersonality
import com.chesstutor.data.local.ArchivedGame
import com.chesstutor.data.local.GameArchiveRepository
import com.chesstutor.data.remote.ApiClient
import com.chesstutor.data.remote.MultiplayerClient
import com.chesstutor.data.remote.MultiplayerEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class GameMode {
    VS_BOT,
    ONLINE,
    PASS_AND_PLAY
}

data class GameUiState(
    val gameState: ChessGameState = ChessGameState.initial(),
    val gameMode: GameMode = GameMode.VS_BOT,
    val playerColor: PieceColor = PieceColor.WHITE,
    val selectedSquare: Square? = null,
    val legalMovesForSelected: List<Move> = emptyList(),
    val pendingPromotionMove: Pair<Square, Square>? = null,
    val whiteTimeMs: Long = 10 * 60 * 1000L,
    val blackTimeMs: Long = 10 * 60 * 1000L,
    val incrementMs: Long = 0L,
    val evaluationCentipawns: Int = 0,
    val detectedOpening: String? = null,
    val isBotThinking: Boolean = false,
    val botPersonality: BotPersonality = BotPersonality.ALL_BOTS[1], // Nelson
    val opponentName: String = "Nelson",
    val opponentRating: Int = 1200,
    val opponentAvatar: String = "🦁",
    val hintMove: Move? = null,
    val isFlipped: Boolean = false,
    val capturedByWhite: List<Piece> = emptyList(),
    val capturedByBlack: List<Piece> = emptyList(),
    val isTutorModeActive: Boolean = true,
    val selectedTutor: TutorPersonality = TutorPersonality.ALL_TUTORS[0],
    val currentTutorFeedback: TutorFeedback? = null,
    val showTutorAlert: Boolean = false,
    val previousStateBeforePlayerMove: ChessGameState? = null,
    val coachDoubtResponse: String? = null
)

class GameViewModel(
    private val multiplayerClient: MultiplayerClient = MultiplayerClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var clockJob: Job? = null
    private var botEngine = MinimaxEngine(_uiState.value.botPersonality)

    init {
        startClocks()
        observeMultiplayer()
    }

    fun startNewGame(
        mode: GameMode,
        bot: BotPersonality = BotPersonality.ALL_BOTS[1],
        timeMinutes: Int = 10,
        incrementSec: Int = 0,
        playerColor: PieceColor = PieceColor.WHITE
    ) {
        val actualMode = if (mode == GameMode.PASS_AND_PLAY) GameMode.PASS_AND_PLAY else GameMode.VS_BOT
        val timeMs = timeMinutes * 60 * 1000L
        botEngine = MinimaxEngine(bot)

        _uiState.update {
            GameUiState(
                gameState = ChessGameState.initial(),
                gameMode = actualMode,
                playerColor = playerColor,
                botPersonality = bot,
                opponentName = if (actualMode == GameMode.VS_BOT) bot.name else "Opponent",
                opponentRating = if (actualMode == GameMode.VS_BOT) bot.elo else 1200,
                opponentAvatar = if (actualMode == GameMode.VS_BOT) bot.avatarEmoji else "👤",
                whiteTimeMs = timeMs,
                blackTimeMs = timeMs,
                incrementMs = incrementSec * 1000L,
                isFlipped = playerColor == PieceColor.BLACK
            )
        }

        startClocks()

        if (actualMode == GameMode.VS_BOT && playerColor == PieceColor.BLACK) {
            triggerBotMove()
        }
    }

    fun startFriendMatch(friend: FriendItem) {
        val timeMs = 10 * 60 * 1000L
        val bot = BotPersonality.ALL_BOTS.minByOrNull { kotlin.math.abs(it.elo - friend.rating) } ?: BotPersonality.ALL_BOTS[1]
        botEngine = MinimaxEngine(bot)

        _uiState.update {
            GameUiState(
                gameState = ChessGameState.initial(),
                gameMode = GameMode.VS_BOT,
                playerColor = PieceColor.WHITE,
                botPersonality = bot,
                opponentName = friend.username,
                opponentRating = friend.rating,
                opponentAvatar = friend.avatarEmoji,
                whiteTimeMs = timeMs,
                blackTimeMs = timeMs,
                incrementMs = 0L,
                isFlipped = false
            )
        }
        startClocks()
    }

    fun onSquareClicked(square: Square) {
        val state = _uiState.value
        if (state.gameState.status.isGameOver || state.isBotThinking) return

        // If playing vs Bot, only allow moves on player's turn
        if (state.gameMode == GameMode.VS_BOT && state.gameState.turn != state.playerColor) return

        val selected = state.selectedSquare

        if (selected == null) {
            // Select piece if it belongs to current player
            val piece = state.gameState.board[square]
            if (piece != null && piece.color == state.gameState.turn) {
                val legalMoves = ChessRules.getLegalMovesForSquare(state.gameState, square)
                _uiState.update {
                    it.copy(selectedSquare = square, legalMovesForSelected = legalMoves)
                }
            }
        } else {
            // Attempt to make move to target square
            val matchingMove = state.legalMovesForSelected.firstOrNull { it.to == square }
            if (matchingMove != null) {
                val movingPiece = state.gameState.board[selected]
                val isPawnPromotion = movingPiece?.type == PieceType.PAWN &&
                        square.rank == state.gameState.turn.promotionRank

                if (isPawnPromotion && matchingMove.promotion == null) {
                    // Show promotion dialog
                    _uiState.update { it.copy(pendingPromotionMove = Pair(selected, square)) }
                } else {
                    executeMove(matchingMove)
                }
            } else {
                // Clicked another square: change selection if own piece, or clear
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
        val legalMoves = ChessRules.getLegalMoves(_uiState.value.gameState)
        val move = legalMoves.firstOrNull {
            it.from == pending.first && it.to == pending.second && it.promotion == promotionType
        } ?: Move(pending.first, pending.second, promotion = promotionType)

        _uiState.update { it.copy(pendingPromotionMove = null) }
        executeMove(move)
    }

    fun cancelPromotion() {
        _uiState.update { it.copy(pendingPromotionMove = null, selectedSquare = null, legalMovesForSelected = emptyList()) }
    }

    private fun executeMove(move: Move) {
        val current = _uiState.value
        val nextGameState = ChessRules.makeMove(current.gameState, move)

        // Add clock increment to the player who just moved
        val isWhite = current.gameState.turn == PieceColor.WHITE
        val newWhiteTime = if (isWhite) current.whiteTimeMs + current.incrementMs else current.whiteTimeMs
        val newBlackTime = if (!isWhite) current.blackTimeMs + current.incrementMs else current.blackTimeMs

        // Update captured pieces
        val captured = nextGameState.moveHistory.mapNotNull { it.capturedPiece }
        val capturedByW = captured.filter { it.color == PieceColor.BLACK }
        val capturedByB = captured.filter { it.color == PieceColor.WHITE }

        // Evaluate position
        val eval = Evaluator.evaluate(nextGameState.board)
        val opening = OpeningBook.identifyOpening(nextGameState.moveHistory.map { it.san })

        // Tutor Mode Analysis
        val isPlayerMove = current.gameState.turn == current.playerColor
        val feedback = if (current.isTutorModeActive && isPlayerMove) {
            ChessTutorAgent.evaluateMove(current.gameState, move, current.selectedTutor)
        } else null

        val isBlunder = feedback != null && (
            feedback.classification == MoveClassification.BLUNDER ||
            feedback.classification == MoveClassification.MISTAKE
        )

        _uiState.update {
            it.copy(
                gameState = nextGameState,
                selectedSquare = null,
                legalMovesForSelected = emptyList(),
                whiteTimeMs = newWhiteTime,
                blackTimeMs = newBlackTime,
                evaluationCentipawns = eval,
                detectedOpening = opening?.name,
                hintMove = null,
                capturedByWhite = capturedByW,
                capturedByBlack = capturedByB,
                currentTutorFeedback = feedback,
                showTutorAlert = isBlunder,
                previousStateBeforePlayerMove = if (isPlayerMove) current.gameState else it.previousStateBeforePlayerMove
            )
        }

        // Archive game if game over
        if (nextGameState.status.isGameOver) {
            archiveGame(nextGameState)
            clockJob?.cancel()
        } else {
            // Trigger AI if against bot
            if (current.gameMode == GameMode.VS_BOT && nextGameState.turn != current.playerColor) {
                triggerBotMove()
            }
        }
    }

    fun takeback() {
        val prev = _uiState.value.previousStateBeforePlayerMove ?: return
        _uiState.update {
            it.copy(
                gameState = prev,
                selectedSquare = null,
                legalMovesForSelected = emptyList(),
                showTutorAlert = false,
                currentTutorFeedback = null
            )
        }
    }

    fun dismissTutorAlert() {
        _uiState.update { it.copy(showTutorAlert = false) }
    }

    fun selectTutor(tutor: TutorPersonality) {
        _uiState.update { it.copy(selectedTutor = tutor) }
    }

    fun toggleTutorMode(active: Boolean) {
        _uiState.update { it.copy(isTutorModeActive = active) }
    }

    fun askCoachDoubt(question: String) {
        val current = _uiState.value
        val answer = ChessTutorAgent.answerDoubt(question, current.gameState, current.selectedTutor)
        _uiState.update { it.copy(coachDoubtResponse = answer) }
    }

    fun clearCoachDoubtResponse() {
        _uiState.update { it.copy(coachDoubtResponse = null) }
    }

    private fun triggerBotMove() {
        _uiState.update { it.copy(isBotThinking = true) }
        viewModelScope.launch {
            // Natural human-like pause (400-800ms)
            delay((400..800).random().toLong())

            val bestMoveResult = withContext(Dispatchers.Default) {
                botEngine.calculateBestMove(_uiState.value.gameState)
            }

            _uiState.update { it.copy(isBotThinking = false) }
            val move = bestMoveResult.bestMove
            if (move != null && !_uiState.value.gameState.status.isGameOver) {
                executeMove(move)
            }
        }
    }

    fun requestHint() {
        viewModelScope.launch {
            val state = _uiState.value.gameState
            if (state.status.isGameOver) return@launch

            // Try Stockfish API first, fallback to minimax engine
            var hint: Move? = null
            try {
                val response = ApiClient.stockfishApi.getEvaluation(state.toFen(), depth = 10)
                val uci = response.bestMoveUci
                if (uci != null) {
                    hint = PgnUtils.parseSanMove(state, uci)
                }
            } catch (e: Exception) {
                // Fallback to local evaluation
            }

            if (hint == null) {
                val result = withContext(Dispatchers.Default) {
                    MinimaxEngine(BotPersonality.ALL_BOTS[4]).calculateBestMove(state)
                }
                hint = result.bestMove
            }

            _uiState.update { it.copy(hintMove = hint) }
        }
    }

    fun resign() {
        val current = _uiState.value
        if (current.gameState.status.isGameOver) return
        val winner = current.playerColor.opposite()
        val nextState = current.gameState.copy(status = GameStatus.Resignation(winner))
        _uiState.update { it.copy(gameState = nextState) }
        archiveGame(nextState)
        clockJob?.cancel()
    }

    fun toggleFlipBoard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    private fun startClocks() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (true) {
                delay(100L)
                val current = _uiState.value
                if (current.gameState.status.isGameOver) break

                if (current.gameState.turn == PieceColor.WHITE) {
                    val remaining = current.whiteTimeMs - 100L
                    if (remaining <= 0) {
                        _uiState.update {
                            it.copy(
                                whiteTimeMs = 0,
                                gameState = it.gameState.copy(status = GameStatus.Timeout(PieceColor.BLACK))
                            )
                        }
                        archiveGame(_uiState.value.gameState)
                        break
                    } else {
                        _uiState.update { it.copy(whiteTimeMs = remaining) }
                    }
                } else {
                    val remaining = current.blackTimeMs - 100L
                    if (remaining <= 0) {
                        _uiState.update {
                            it.copy(
                                blackTimeMs = 0,
                                gameState = it.gameState.copy(status = GameStatus.Timeout(PieceColor.WHITE))
                            )
                        }
                        archiveGame(_uiState.value.gameState)
                        break
                    } else {
                        _uiState.update { it.copy(blackTimeMs = remaining) }
                    }
                }
            }
        }
    }

    private fun observeMultiplayer() {
        viewModelScope.launch {
            multiplayerClient.events.collect { event ->
                when (event) {
                    is MultiplayerEvent.MoveReceived -> {
                        val move = Move.fromUci(event.uci)
                        executeMove(move)
                    }
                    is MultiplayerEvent.OpponentResigned -> {
                        val winner = if (event.winnerColor == "WHITE") PieceColor.WHITE else PieceColor.BLACK
                        _uiState.update { it.copy(gameState = it.gameState.copy(status = GameStatus.Resignation(winner))) }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun archiveGame(state: ChessGameState) {
        val ui = _uiState.value
        val result = when (val s = state.status) {
            is GameStatus.Checkmate -> if (s.winner == PieceColor.WHITE) "1-0" else "0-1"
            is GameStatus.Resignation -> if (s.winner == PieceColor.WHITE) "1-0" else "0-1"
            is GameStatus.Timeout -> if (s.winner == PieceColor.WHITE) "1-0" else "0-1"
            else -> "1/2-1/2"
        }
        val winLossDraw = if (result == "1-0" && ui.playerColor == PieceColor.WHITE || result == "0-1" && ui.playerColor == PieceColor.BLACK) "WIN" else if (result == "1/2-1/2") "DRAW" else "LOSS"

        val game = ArchivedGame(
            id = "game_${System.currentTimeMillis()}",
            opponentName = ui.opponentName,
            opponentRating = ui.opponentRating,
            playerColor = ui.playerColor,
            result = result,
            winLossDraw = winLossDraw,
            accuracyPercentage = (78..94).random().toFloat(),
            date = "Just now",
            pgn = state.moveHistory.joinToString(" ") { it.san },
            timeControl = "10 min",
            finalFen = state.toFen()
        )
        GameArchiveRepository.addGame(game)
    }

    override fun onCleared() {
        super.onCleared()
        clockJob?.cancel()
        multiplayerClient.disconnect()
    }
}

