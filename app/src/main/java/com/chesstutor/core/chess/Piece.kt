package com.chesstutor.core.chess

data class Piece(
    val type: PieceType,
    val color: PieceColor
) {
    val fenChar: Char
        get() = if (color == PieceColor.WHITE) type.symbol.uppercaseChar() else type.symbol.lowercaseChar()

    val unicodeChar: String
        get() = when (color) {
            PieceColor.WHITE -> when (type) {
                PieceType.KING -> "♔"
                PieceType.QUEEN -> "♕"
                PieceType.ROOK -> "♖"
                PieceType.BISHOP -> "♗"
                PieceType.KNIGHT -> "♘"
                PieceType.PAWN -> "♙"
            }
            PieceColor.BLACK -> when (type) {
                PieceType.KING -> "♚"
                PieceType.QUEEN -> "♛"
                PieceType.ROOK -> "♜"
                PieceType.BISHOP -> "♝"
                PieceType.KNIGHT -> "♞"
                PieceType.PAWN -> "♟"
            }
        }

    companion object {
        fun fromFenChar(char: Char): Piece? {
            val type = PieceType.fromSymbol(char) ?: return null
            val color = if (char.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
            return Piece(type, color)
        }
    }
}

