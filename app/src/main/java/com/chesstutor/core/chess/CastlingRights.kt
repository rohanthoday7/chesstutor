package com.chesstutor.core.chess

data class CastlingRights(
    val whiteKingside: Boolean = true,
    val whiteQueenside: Boolean = true,
    val blackKingside: Boolean = true,
    val blackQueenside: Boolean = true
) {
    fun toFenString(): String {
        val sb = StringBuilder()
        if (whiteKingside) sb.append('K')
        if (whiteQueenside) sb.append('Q')
        if (blackKingside) sb.append('k')
        if (blackQueenside) sb.append('q')
        return if (sb.isEmpty()) "-" else sb.toString()
    }

    companion object {
        fun fromFenString(str: String): CastlingRights {
            return CastlingRights(
                whiteKingside = str.contains('K'),
                whiteQueenside = str.contains('Q'),
                blackKingside = str.contains('k'),
                blackQueenside = str.contains('q')
            )
        }
    }
}

