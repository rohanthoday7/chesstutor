package com.chesstutor.core.chess

class Board(private val squares: Array<Piece?> = Array(64) { null }) {

    operator fun get(square: Square): Piece? = squares[square.index]
    operator fun get(file: Int, rank: Int): Piece? = squares[rank * 8 + file]
    operator fun get(index: Int): Piece? = squares[index]

    operator fun set(square: Square, piece: Piece?) {
        squares[square.index] = piece
    }

    operator fun set(file: Int, rank: Int, piece: Piece?) {
        squares[rank * 8 + file] = piece
    }

    operator fun set(index: Int, piece: Piece?) {
        squares[index] = piece
    }

    fun findKing(color: PieceColor): Square? {
        for (i in 0 until 64) {
            val piece = squares[i]
            if (piece != null && piece.type == PieceType.KING && piece.color == color) {
                return Square.fromIndex(i)
            }
        }
        return null
    }

    fun copy(): Board {
        return Board(squares.copyOf())
    }

    fun clear() {
        for (i in 0 until 64) {
            squares[i] = null
        }
    }

    fun toFenPiecePlacement(): String {
        val sb = StringBuilder()
        for (rank in 7 downTo 0) {
            var emptyCount = 0
            for (file in 0..7) {
                val piece = get(file, rank)
                if (piece == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount)
                        emptyCount = 0
                    }
                    sb.append(piece.fenChar)
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount)
            }
            if (rank > 0) {
                sb.append('/')
            }
        }
        return sb.toString()
    }

    fun loadFenPiecePlacement(fenPlacement: String) {
        clear()
        val ranks = fenPlacement.split('/')
        require(ranks.size == 8) { "Invalid FEN rank count: ${ranks.size}" }

        for (rankIndex in 0..7) {
            val rank = 7 - rankIndex
            val rankStr = ranks[rankIndex]
            var file = 0
            for (char in rankStr) {
                if (char.isDigit()) {
                    file += char.digitToInt()
                } else {
                    val piece = Piece.fromFenChar(char)
                        ?: throw IllegalArgumentException("Unknown FEN piece character: $char")
                    set(file, rank, piece)
                    file++
                }
            }
            require(file == 8) { "Rank $rank in FEN does not total 8 files (got $file)" }
        }
    }

    fun countPieces(): Map<Piece, Int> {
        val counts = mutableMapOf<Piece, Int>()
        for (i in 0 until 64) {
            val piece = squares[i] ?: continue
            counts[piece] = (counts[piece] ?: 0) + 1
        }
        return counts
    }

    companion object {
        const val STARTING_FEN_PLACEMENT = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"

        fun initial(): Board {
            val b = Board()
            b.loadFenPiecePlacement(STARTING_FEN_PLACEMENT)
            return b
        }
    }
}

