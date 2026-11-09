package com.chesstutor.core.chess

enum class PieceType(
    val symbol: Char,
    val baseValue: Int
) {
    PAWN('P', 100),
    KNIGHT('N', 320),
    BISHOP('B', 330),
    ROOK('R', 500),
    QUEEN('Q', 900),
    KING('K', 20000);

    companion object {
        fun fromSymbol(char: Char): PieceType? {
            return entries.firstOrNull { it.symbol == char.uppercaseChar() }
        }
    }
}

