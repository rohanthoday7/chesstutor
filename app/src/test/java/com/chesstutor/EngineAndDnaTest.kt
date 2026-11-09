package com.chesstutor

import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.ChessRules
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.chess.Square
import com.chesstutor.core.dna.ChessDnaAnalyzer
import com.chesstutor.core.engine.BotPersonality
import com.chesstutor.core.engine.Evaluator
import com.chesstutor.core.engine.MinimaxEngine
import com.chesstutor.core.engine.OpeningBook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineAndDnaTest {

    @Test
    fun testInitialEvaluationSymmetry() {
        val initial = ChessGameState.initial()
        val score = Evaluator.evaluate(initial.board)
        assertEquals("Initial position must be roughly balanced (score near 0)", 0, score)
    }

    @Test
    fun testMinimaxFindsCheckmateInOne() {
        // White to move: Qh5xf7# is checkmate in 1
        val fen = "r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 0 4"
        val state = ChessRules.fromFen(fen)

        val engine = MinimaxEngine(BotPersonality.ALL_BOTS[4]) // Magnus AI
        val result = engine.calculateBestMove(state)

        assertNotNull(result.bestMove)
        assertEquals(Square.fromNotation("h5"), result.bestMove?.from)
        assertEquals(Square.fromNotation("f7"), result.bestMove?.to)
    }

    @Test
    fun testOpeningBookRecognition() {
        val moves = listOf("e4", "c5", "Nf3", "d6", "d4", "cxd4", "Nxd4", "Nf6", "Nc3", "a6")
        val opening = OpeningBook.identifyOpening(moves)

        assertNotNull("Must recognize Najdorf Sicilian", opening)
        assertEquals("B90", opening?.eco)
        assertTrue(opening?.name?.contains("Najdorf") == true)
    }

    @Test
    fun testChessDnaAnalysis() {
        val games = listOf(ChessGameState.initial())
        val profile = ChessDnaAnalyzer.analyzeGames(games, PieceColor.WHITE)

        assertNotNull(profile.playerArchetype)
        assertTrue(profile.metrics.tactics in 0..100)
        assertTrue(profile.metrics.opening in 0..100)
        assertTrue(profile.metrics.endgame in 0..100)
        assertTrue(profile.strengths.isNotEmpty())
        assertTrue(profile.trainingRecommendations.isNotEmpty())
    }
}

