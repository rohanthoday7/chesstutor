package com.chesstutor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.Move
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
    val learnedPatterns: List<LearnedPattern> = listOf(
        LearnedPattern(
            patternName = "Hanging Minor Pieces",
            occurrenceCount = 3,
            severity = "High",
            diagnosis = "Moving pieces to squares where they are targeted without adequate friendly defenders.",
            remedy = "Before releasing any piece, scan all enemy diagonals, ranks, and knight jumps to confirm it is not loose."
        ),
        LearnedPattern(
            patternName = "Delayed Castling",
            occurrenceCount = 2,
            severity = "Medium",
            diagnosis = "Leaving the King on e1/e8 beyond move 10 while opening center files.",
            remedy = "Aim to castle kingside within your first 7 to 8 moves to avoid tactical king walks."
        ),
        LearnedPattern(
            patternName = "Early Queen Attacks",
            occurrenceCount = 1,
            severity = "Tip",
            diagnosis = "Developing the Queen on moves 2-3 to chase checkmates, which allows the opponent to develop minor pieces with tempo.",
            remedy = "Develop Knights and Bishops first; bring out the Queen only after pieces are coordinated."
        )
    ),
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
                activeLessonState = ChessGameState.fromFen(lesson.demoFen)
            )
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

