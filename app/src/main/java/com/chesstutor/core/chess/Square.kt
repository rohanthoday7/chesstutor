package com.chesstutor.core.chess

data class Square(
    val file: Int, // 0..7 (a..h)
    val rank: Int  // 0..7 (1..8)
) {
    init {
        require(file in 0..7 && rank in 0..7) { "Square out of bounds: file=$file, rank=$rank" }
    }

    val index: Int get() = rank * 8 + file

    val notation: String
        get() = "${('a' + file)}${rank + 1}"

    val isLightSquare: Boolean
        get() = (file + rank) % 2 != 0

    companion object {
        fun fromIndex(index: Int): Square {
            require(index in 0..63) { "Index out of range: $index" }
            return Square(file = index % 8, rank = index / 8)
        }

        fun fromNotation(notation: String): Square {
            require(notation.length == 2) { "Invalid square notation: $notation" }
            val file = notation[0].lowercaseChar() - 'a'
            val rank = notation[1].digitToInt() - 1
            return Square(file, rank)
        }

        fun isValid(file: Int, rank: Int): Boolean = file in 0..7 && rank in 0..7
    }
}

