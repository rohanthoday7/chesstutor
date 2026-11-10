package com.chesstutor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.ChessRules
import com.chesstutor.core.chess.Move
import com.chesstutor.core.chess.PieceType
import com.chesstutor.core.chess.Square
import com.chesstutor.core.tutor.ChessTutorAgent
import com.chesstutor.core.tutor.MoveClassification
import com.chesstutor.core.tutor.MoveLesson
import com.chesstutor.core.tutor.TutorFeedback
import com.chesstutor.core.tutor.TutorPersonality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TutorChatMessage(
    val id: String = "msg_${System.currentTimeMillis()}_${(100..999).random()}",
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class LearnedPattern(
    val patternName: String,
    val occurrenceCount: Int,
    val severity: String, // "High", "Medium", "Tip"
    val diagnosis: String,
    val remedy: String
)

data class TutorUiState(
    val selectedTutor: TutorPersonality = TutorPersonality.ALL_TUTORS[0], // GM Mikhail
    val allTutors: List<TutorPersonality> = TutorPersonality.ALL_TUTORS,
    val isTutorModeActive: Boolean = true,
    val lessons: List<MoveLesson> = ChessTutorAgent.ALL_LESSONS,
    val activeLesson: MoveLesson? = ChessTutorAgent.ALL_LESSONS[0],
    val activeLessonState: ChessGameState = ChessGameState.fromFen(ChessTutorAgent.ALL_LESSONS[0].demoFen),
    val lastFeedback: TutorFeedback? = null,
    val pendingBlunderFeedback: TutorFeedback? = null,
    val showBlunderIntervention: Boolean = false,
    val learnedPatterns: List<LearnedPattern> = emptyList(),
    val lessonSelectedSquare: Square? = null,
    val lessonLegalMoves: List<Move> = emptyList(),
    val lessonMessage: String? = "Tap a piece and make the correct move to practice!",
    val isLessonSuccess: Boolean = false,
    val doubtChat: List<TutorChatMessage> = listOf(
        TutorChatMessage(
            sender = "GM Mikhail",
            text = "Welcome to Tutor Mode! I am here to guide your moves, warn you before blunders, and answer any chess doubts you have.",
            isUser = false
        )
    )
)

class TutorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TutorUiState())
    val uiState: StateFlow<TutorUiState> = _uiState.asStateFlow()

    fun selectTutor(tutor: TutorPersonality) {
        _uiState.update { current ->
            val updatedChat = current.doubtChat + TutorChatMessage(
                sender = tutor.name,
                text = tutor.greeting,
                isUser = false
            )
            current.copy(
                selectedTutor = tutor,
                doubtChat = updatedChat
            )
        }
    }

    fun toggleTutorMode(enabled: Boolean) {
        _uiState.update { it.copy(isTutorModeActive = enabled) }
    }

    fun selectLesson(lesson: MoveLesson) {
        _uiState.update {
            it.copy(
                activeLesson = lesson,
                activeLessonState = ChessGameState.fromFen(lesson.demoFen),
                lessonSelectedSquare = null,
                lessonLegalMoves = emptyList(),
                lessonMessage = "Tap a piece and make the correct move to practice!",
                isLessonSuccess = false
            )
        }
    }

    fun resetLesson() {
        val lesson = _uiState.value.activeLesson ?: return
        _uiState.update {
            it.copy(
                activeLessonState = ChessGameState.fromFen(lesson.demoFen),
                lessonSelectedSquare = null,
                lessonLegalMoves = emptyList(),
                lessonMessage = "Try making the move yourself!",
                isLessonSuccess = false
            )
        }
    }

    fun demonstrateLessonMove() {
        val state = _uiState.value
        val lesson = state.activeLesson ?: return
        val startState = ChessGameState.fromFen(lesson.demoFen)
        val move = getDemoMoveForLesson(lesson.id)
        if (move != null) {
            val nextState = ChessRules.makeMove(startState, move)
            _uiState.update {
                it.copy(
                    activeLessonState = nextState,
                    lessonSelectedSquare = null,
                    lessonLegalMoves = emptyList(),
                    lessonMessage = "🤖 Coach demonstrated the move! Tap 'Reset' to practice it yourself.",
                    isLessonSuccess = true
                )
            }
        }
    }

    fun onLessonSquareClicked(square: Square) {
        val state = _uiState.value
        val lesson = state.activeLesson ?: return

        // If already successful, tapping a piece auto-resets so the user can practice again instantly
        if (state.isLessonSuccess) {
            resetLesson()
            val newState = _uiState.value
            val piece = newState.activeLessonState.board[square]
            if (piece != null && piece.color == newState.activeLessonState.turn) {
                val legal = ChessRules.getLegalMovesForSquare(newState.activeLessonState, square)
                _uiState.update { it.copy(lessonSelectedSquare = square, lessonLegalMoves = legal, lessonMessage = "Try your move!") }
            }
            return
        }

        val selected = state.lessonSelectedSquare
        if (selected == null) {
            val piece = state.activeLessonState.board[square]
            if (piece != null && piece.color == state.activeLessonState.turn) {
                val legal = ChessRules.getLegalMovesForSquare(state.activeLessonState, square)
                _uiState.update { it.copy(lessonSelectedSquare = square, lessonLegalMoves = legal) }
            }
        } else {
            val match = state.lessonLegalMoves.firstOrNull { it.to == square }
            if (match != null) {
                val demo = getDemoMoveForLesson(lesson.id)
                val isCorrect = demo == null || (match.from == demo.from && match.to == demo.to)

                val nextState = ChessRules.makeMove(state.activeLessonState, match)
                if (isCorrect) {
                    _uiState.update {
                        it.copy(
                            activeLessonState = nextState,
                            lessonSelectedSquare = null,
                            lessonLegalMoves = emptyList(),
                            lessonMessage = "🎉 Correct! You successfully mastered ${lesson.title}!",
                            isLessonSuccess = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            activeLessonState = nextState,
                            lessonSelectedSquare = null,
                            lessonLegalMoves = emptyList(),
                            lessonMessage = "💡 Good try! That's a legal move. Tap 'Reset & Practice' to try the master lesson move.",
                            isLessonSuccess = false
                        )
                    }
                }
            } else {
                val piece = state.activeLessonState.board[square]
                if (piece != null && piece.color == state.activeLessonState.turn) {
                    val legal = ChessRules.getLegalMovesForSquare(state.activeLessonState, square)
                    _uiState.update { it.copy(lessonSelectedSquare = square, lessonLegalMoves = legal) }
                } else {
                    _uiState.update { it.copy(lessonSelectedSquare = null, lessonLegalMoves = emptyList()) }
                }
            }
        }
    }

    private fun getDemoMoveForLesson(lessonId: String): Move? {
        return when (lessonId) {
            "lesson_castling" -> Move(Square(4, 0), Square(6, 0), isCastling = true)
            "lesson_en_passant" -> Move(Square(4, 4), Square(3, 5))
            "lesson_promotion" -> Move(Square(4, 6), Square(4, 7), promotion = PieceType.QUEEN)
            "lesson_knight_fork" -> Move(Square(2, 2), Square(3, 4))
            "lesson_pin" -> Move(Square(1, 4), Square(2, 5))
            "lesson_smothered_mate" -> Move(Square(5, 7), Square(4, 5))
            else -> null
        }
    }

    // Called when a move is made by the user in Tutor Mode
    fun onMovePlayed(stateBefore: ChessGameState, move: Move): TutorFeedback? {
        if (!_uiState.value.isTutorModeActive) return null

        val tutor = _uiState.value.selectedTutor
        val feedback = ChessTutorAgent.evaluateMove(stateBefore, move, tutor)

        // Track pattern if blunder or mistake
        if (feedback.patternIdentified != null) {
            recordLearnedPattern(feedback.patternIdentified)
        }

        val isBlunder = feedback.classification == MoveClassification.BLUNDER ||
                feedback.classification == MoveClassification.MISTAKE

        _uiState.update {
            it.copy(
                lastFeedback = feedback,
                pendingBlunderFeedback = if (isBlunder) feedback else null,
                showBlunderIntervention = isBlunder
            )
        }

        return feedback
    }

    fun dismissBlunderIntervention() {
        _uiState.update { it.copy(showBlunderIntervention = false, pendingBlunderFeedback = null) }
    }

    fun askDoubt(question: String, currentState: ChessGameState) {
        if (question.isBlank()) return

        val currentTutor = _uiState.value.selectedTutor
        val userMsg = TutorChatMessage(sender = "You", text = question.trim(), isUser = true)

        _uiState.update { it.copy(doubtChat = it.doubtChat + userMsg) }

        viewModelScope.launch {
            val answer = ChessTutorAgent.answerDoubt(question, currentState, currentTutor)
            val tutorMsg = TutorChatMessage(
                sender = currentTutor.name,
                text = answer,
                isUser = false
            )
            _uiState.update { it.copy(doubtChat = it.doubtChat + tutorMsg) }
        }
    }

    private fun recordLearnedPattern(patternName: String) {
        _uiState.update { state ->
            val existing = state.learnedPatterns.firstOrNull { it.patternName.equals(patternName, ignoreCase = true) }
            val updatedList = if (existing != null) {
                state.learnedPatterns.map {
                    if (it.patternName.equals(patternName, ignoreCase = true)) {
                        it.copy(occurrenceCount = it.occurrenceCount + 1)
                    } else it
                }
            } else {
                state.learnedPatterns + LearnedPattern(
                    patternName = patternName,
                    occurrenceCount = 1,
                    severity = "Medium",
                    diagnosis = "Repeated tactical vulnerability observed in live games.",
                    remedy = "Review the corresponding Move Lesson and practice double-checking piece defenses."
                )
            }
            state.copy(learnedPatterns = updatedList)
        }
    }
}

