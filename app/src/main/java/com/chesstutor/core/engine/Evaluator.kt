package com.chesstutor.core.engine

import com.chesstutor.core.chess.Board
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.chess.PieceType
import com.chesstutor.core.chess.Square

object Evaluator {

    // Piece-Square Tables (White perspective; mirrored for Black)
    private val PAWN_TABLE = intArrayOf(
        0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    )

    private val KNIGHT_TABLE = intArrayOf(
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    )

    private val BISHOP_TABLE = intArrayOf(
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    )

    private val ROOK_TABLE = intArrayOf(
          0,  0,  0,  0,  0,  0,  0,  0,
          5, 10, 10, 10, 10, 10, 10,  5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
          0,  0,  0,  5,  5,  0,  0,  0
    )

    private val QUEEN_TABLE = intArrayOf(
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    )

    private val KING_MIDGAME_TABLE = intArrayOf(
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-20,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
         20, 30, 10,  0,  0, 10, 30, 20
    )

    fun evaluate(board: Board): Int {
        var score = 0
        var whiteBishops = 0
        var blackBishops = 0

        for (rank in 0..7) {
            for (file in 0..7) {
                val square = Square(file, rank)
                val piece = board[square] ?: continue
                val isWhite = piece.color == PieceColor.WHITE
                val multiplier = if (isWhite) 1 else -1

                // Base Material Value
                score += piece.type.baseValue * multiplier

                // Positional Table Score
                val tableIndex = if (isWhite) {
                    (7 - rank) * 8 + file
                } else {
                    rank * 8 + file
                }

                val positionalBonus = when (piece.type) {
                    PieceType.PAWN -> PAWN_TABLE[tableIndex]
                    PieceType.KNIGHT -> KNIGHT_TABLE[tableIndex]
                    PieceType.BISHOP -> {
                        if (isWhite) whiteBishops++ else blackBishops++
                        BISHOP_TABLE[tableIndex]
                    }
                    PieceType.ROOK -> ROOK_TABLE[tableIndex]
                    PieceType.QUEEN -> QUEEN_TABLE[tableIndex]
                    PieceType.KING -> KING_MIDGAME_TABLE[tableIndex]
                }

                score += positionalBonus * multiplier
            }
        }

        // Bishop pair bonus
        if (whiteBishops >= 2) score += 35
        if (blackBishops >= 2) score -= 35

        return score
    }
}

