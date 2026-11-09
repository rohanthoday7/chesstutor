package com.chesstutor.core.chess

enum class PieceColor {
    WHITE,
    BLACK;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE

    /** Direction of pawn advancement: White moves upwards (+1 rank), Black moves downwards (-1 rank) */
    val pawnDirection: Int get() = if (this == WHITE) 1 else -1

    /** Starting rank for pawns (0-indexed: White rank 1 is 1, Black rank 7 is 6) */
    val pawnStartRank: Int get() = if (this == WHITE) 1 else 6

    /** Promotion rank for pawns (White rank 8 is 7, Black rank 1 is 0) */
    val promotionRank: Int get() = if (this == WHITE) 7 else 0

    val backRank: Int get() = if (this == WHITE) 0 else 7
}

