package com.chesstutor.core.chess

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
    val isEnPassant: Boolean = false,
    val isCastling: Boolean = false
) {
    val uci: String
        get() = "${from.notation}${to.notation}${promotion?.symbol?.lowercaseChar() ?: ""}"

    companion object {
        fun fromUci(uci: String): Move {
            require(uci.length in 4..5) { "Invalid UCI move string: $uci" }
            val from = Square.fromNotation(uci.substring(0, 2))
            val to = Square.fromNotation(uci.substring(2, 4))
            val promo = if (uci.length == 5) PieceType.fromSymbol(uci[4]) else null
            return Move(from, to, promo)
        }
    }
}

