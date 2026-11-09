package com.chesstutor

import com.chesstutor.core.chess.Board
import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.ChessRules
import com.chesstutor.core.chess.GameStatus
import com.chesstutor.core.chess.Move
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.chess.PieceType
import com.chesstutor.core.chess.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessRulesTest {

    @Test
    fun testInitialBoardSetup() {
        val state = ChessGameState.initial()
        assertEquals(PieceColor.WHITE, state.turn)
        assertEquals(20, ChessRules.getLegalMoves(state).size) // 16 pawn moves + 4 knight moves
        assertEquals(1, state.fullmoveNumber)
        assertEquals(0, state.halfmoveClock)
        assertFalse(ChessRules.isInCheck(state.board, PieceColor.WHITE))
        assertFalse(ChessRules.isInCheck(state.board, PieceColor.BLACK))
    }

    @Test
    fun testPawnDoubleStepAndEnPassant() {
        var state = ChessGameState.initial()

        // 1. e4
        val e2 = Square.fromNotation("e2")
        val e4 = Square.fromNotation("e4")
        state = ChessRules.makeMove(state, Move(e2, e4))
        assertEquals(Square.fromNotation("e3"), state.enPassantSquare)
        assertEquals(PieceColor.BLACK, state.turn)

        // 1... a6
        val a7 = Square.fromNotation("a7")
        val a6 = Square.fromNotation("a6")
        state = ChessRules.makeMove(state, Move(a7, a6))

        // 2. e5
        val e5 = Square.fromNotation("e5")
        state = ChessRules.makeMove(state, Move(e4, e5))

        // 2... d5 (Black double step)
        val d7 = Square.fromNotation("d7")
        val d5 = Square.fromNotation("d5")
        state = ChessRules.makeMove(state, Move(d7, d5))
        assertEquals(Square.fromNotation("d6"), state.enPassantSquare)

        // 3. exd6 (White En Passant Capture)
        val d6 = Square.fromNotation("d6")
        val legalMoves = ChessRules.getLegalMovesForSquare(state, e5)
        val epMove = legalMoves.firstOrNull { it.to == d6 && it.isEnPassant }
        assertNotNull("En passant move must be legal", epMove)

        state = ChessRules.makeMove(state, epMove!!)
        // Pawn on d5 must be captured and removed from board
        assertEquals(null, state.board[d5])
        assertEquals(PieceType.PAWN, state.board[d6]?.type)
        assertEquals(PieceColor.WHITE, state.board[d6]?.color)
    }

    @Test
    fun testScholarsMateCheckmate() {
        var state = ChessGameState.initial()

        // 1. e4 e5
        state = ChessRules.makeMove(state, Move(Square.fromNotation("e2"), Square.fromNotation("e4")))
        state = ChessRules.makeMove(state, Move(Square.fromNotation("e7"), Square.fromNotation("e5")))

        // 2. Qh5 Nc6
        state = ChessRules.makeMove(state, Move(Square.fromNotation("d1"), Square.fromNotation("h5")))
        state = ChessRules.makeMove(state, Move(Square.fromNotation("b8"), Square.fromNotation("c6")))

        // 3. Bc4 Nf6
        state = ChessRules.makeMove(state, Move(Square.fromNotation("f1"), Square.fromNotation("c4")))
        state = ChessRules.makeMove(state, Move(Square.fromNotation("g8"), Square.fromNotation("f6")))

        // 4. Qxf7# (Checkmate)
        val mateMove = Move(Square.fromNotation("h5"), Square.fromNotation("f7"))
        state = ChessRules.makeMove(state, mateMove)

        assertTrue(state.status is GameStatus.Checkmate)
        assertEquals(PieceColor.WHITE, (state.status as GameStatus.Checkmate).winner)
        assertEquals(0, ChessRules.getLegalMoves(state).size)
    }

    @Test
    fun testCastling() {
        // Position where White kingside castling path is clear
        val fen = "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        val state = ChessRules.fromFen(fen)

        val e1 = Square.fromNotation("e1")
        val g1 = Square.fromNotation("g1")
        val c1 = Square.fromNotation("c1")

        val legal = ChessRules.getLegalMovesForSquare(state, e1)
        val kingside = legal.firstOrNull { it.to == g1 && it.isCastling }
        val queenside = legal.firstOrNull { it.to == c1 && it.isCastling }

        assertNotNull("Kingside castling must be available", kingside)
        assertNotNull("Queenside castling must be available", queenside)

        // Execute Kingside castling
        val afterKingside = ChessRules.makeMove(state, kingside!!)
        assertEquals(PieceType.KING, afterKingside.board[g1]?.type)
        assertEquals(PieceType.ROOK, afterKingside.board[Square.fromNotation("f1")]?.type)
        assertEquals(null, afterKingside.board[Square.fromNotation("h1")])
    }

    @Test
    fun testCastlingPreventedThroughCheck() {
        // Black rook attacks f1; White cannot castle kingside through check
        val fen = "r3k2r/8/8/8/8/5r2/8/R3K2R w KQkq - 0 1"
        val state = ChessRules.fromFen(fen)

        val e1 = Square.fromNotation("e1")
        val g1 = Square.fromNotation("g1")
        val legal = ChessRules.getLegalMovesForSquare(state, e1)
        val kingside = legal.firstOrNull { it.to == g1 }

        assertEquals("Castling through attacked f1 square must be forbidden", null, kingside)
    }

    @Test
    fun testStalemate() {
        // Classic stalemate position: Black king on a8, White queen on c7, White king on c6
        val fen = "k7/2Q5/2K5/8/8/8/8/8 b - - 0 1"
        val state = ChessRules.fromFen(fen)

        assertEquals(0, ChessRules.getLegalMoves(state).size)
        assertFalse(ChessRules.isInCheck(state.board, PieceColor.BLACK))
        assertTrue(state.status is GameStatus.Stalemate)
    }

    @Test
    fun testInsufficientMaterial() {
        val kvk = Board()
        kvk[Square.fromNotation("e1")] = com.chesstutor.core.chess.Piece(PieceType.KING, PieceColor.WHITE)
        kvk[Square.fromNotation("e8")] = com.chesstutor.core.chess.Piece(PieceType.KING, PieceColor.BLACK)
        assertTrue(ChessRules.isInsufficientMaterial(kvk))

        val kvkb = kvk.copy()
        kvkb[Square.fromNotation("c1")] = com.chesstutor.core.chess.Piece(PieceType.BISHOP, PieceColor.WHITE)
        assertTrue(ChessRules.isInsufficientMaterial(kvkb))
    }
}

