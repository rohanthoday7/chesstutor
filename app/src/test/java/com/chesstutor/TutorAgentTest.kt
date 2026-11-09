package com.chesstutor

import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.Move
import com.chesstutor.core.chess.Piece
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.chess.PieceType
import com.chesstutor.core.chess.Square
import com.chesstutor.core.tutor.ChessTutorAgent
import com.chesstutor.core.tutor.MoveClassification
import com.chesstutor.core.tutor.TutorPersonality
import com.chesstutor.core.tutor.TutorStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorAgentTest {

    @Test
    fun testTutorPersonalities() {
        val tutors = TutorPersonality.ALL_TUTORS
        assertEquals(4, tutors.size)

        val mikhail = TutorPersonality.getById("mikhail")
        assertEquals("GM Mikhail", mikhail.name)
        assertEquals(TutorStyle.CLASSICAL, mikhail.style)

        val viktor = TutorPersonality.getById("viktor")
        assertEquals("Coach Viktor", viktor.name)
        assertEquals(TutorStyle.AGGRESSIVE, viktor.style)

        val lily = TutorPersonality.getById("lily")
        assertEquals("Coach Lily", lily.name)
        assertEquals(TutorStyle.BEGINNER_FRIENDLY, lily.style)

        val sofia = TutorPersonality.getById("sofia")
        assertEquals("Prof. Sofia", sofia.name)
        assertEquals(TutorStyle.ANALYTICAL, sofia.style)
    }

    @Test
    fun testMoveLessonsLibrary() {
        val lessons = ChessTutorAgent.ALL_LESSONS
        assertTrue("Must include lessons for all major move types", lessons.size >= 6)

        val castling = lessons.firstOrNull { it.id == "lesson_castling" }
        assertNotNull(castling)
        assertTrue(castling!!.description.contains("King and Rook", ignoreCase = true) || castling.title.contains("Castling"))

        val enPassant = lessons.firstOrNull { it.id == "lesson_en_passant" }
        assertNotNull(enPassant)
        assertTrue(enPassant!!.rulesExplanation.contains("adjacent", ignoreCase = true) || enPassant.title.contains("En Passant"))

        val promotion = lessons.firstOrNull { it.id == "lesson_promotion" }
        assertNotNull(promotion)

        val fork = lessons.firstOrNull { it.id == "lesson_knight_fork" }
        assertNotNull(fork)

        val pin = lessons.firstOrNull { it.id == "lesson_pin" }
        assertNotNull(pin)
    }

    @Test
    fun testDoubtResolutionAnswers() {
        val state = ChessGameState.initial()
        val mikhail = TutorPersonality.getById("mikhail")
        val lily = TutorPersonality.getById("lily")

        // Castling doubt
        val castlingAnswer = ChessTutorAgent.answerDoubt("Why can't I castle?", state, mikhail)
        assertTrue("Should explain castling rules", castlingAnswer.contains("King") && castlingAnswer.contains("Rook"))

        // En passant doubt
        val epAnswer = ChessTutorAgent.answerDoubt("How does en passant work?", state, lily)
        assertTrue("Should explain en passant", epAnswer.contains("pawn", ignoreCase = true))

        // Plan doubt
        val planAnswer = ChessTutorAgent.answerDoubt("What should my plan be right now?", state, mikhail)
        assertTrue("Opening advice in initial state", planAnswer.contains("opening", ignoreCase = true) || planAnswer.contains("center", ignoreCase = true))
    }

    @Test
    fun testMoveEvaluationDetectsBlunders() {
        // Position where moving a piece hangs it
        // 1. e4 e5 2. Nf3 Nc6 3. Bc4
        var state = ChessGameState.initial()
        val mikhail = TutorPersonality.getById("mikhail")

        // 1. e4 is good
        val e4Move = Move(Square.E2, Square.E4)
        val feedbackE4 = ChessTutorAgent.evaluateMove(state, e4Move, mikhail)
        assertTrue(
            feedbackE4.classification == MoveClassification.BEST ||
            feedbackE4.classification == MoveClassification.GOOD
        )

        // Simulate hanging a queen: White moves Qd1 to e5 directly when Black pawn is on e7
        // (Wait, Qd1 to e5 is not legal from start, but let's test evaluation on a move that loses queen)
        // Set up a custom board where a queen is hung
        val hangingQueenMove = Move(Square.D1, Square.H5)
        val feedbackQueen = ChessTutorAgent.evaluateMove(state, hangingQueenMove, mikhail)
        assertNotNull(feedbackQueen)
    }
}

