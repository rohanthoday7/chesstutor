package com.chesstutor.core.chess

data class MoveRecord(
    val move: Move,
    val san: String,
    val capturedPiece: Piece?,
    val fenAfter: String
)

data class ChessGameState(
    val board: Board = Board.initial(),
    val turn: PieceColor = PieceColor.WHITE,
    val castlingRights: CastlingRights = CastlingRights(),
    val enPassantSquare: Square? = null,
    val halfmoveClock: Int = 0,
    val fullmoveNumber: Int = 1,
    val status: GameStatus = GameStatus.Active,
    val moveHistory: List<MoveRecord> = emptyList(),
    val positionOccurrences: Map<String, Int> = mapOf(
        board.toFenPiecePlacement() to 1
    )
) {
    fun toFen(): String {
        val placement = board.toFenPiecePlacement()
        val activeColor = if (turn == PieceColor.WHITE) "w" else "b"
        val castling = castlingRights.toFenString()
        val ep = enPassantSquare?.notation ?: "-"
        return "$placement $activeColor $castling $ep $halfmoveClock $fullmoveNumber"
    }

    val repetitionKey: String
        get() = "${board.toFenPiecePlacement()} ${if (turn == PieceColor.WHITE) "w" else "b"} ${castlingRights.toFenString()} ${enPassantSquare?.notation ?: "-"}"

    fun copyDeep(): ChessGameState {
        return copy(
            board = board.copy(),
            castlingRights = castlingRights.copy(),
            moveHistory = moveHistory.toList(),
            positionOccurrences = positionOccurrences.toMap()
        )
    }

    companion object {
        const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        fun initial(): ChessGameState = ChessRules.fromFen(STARTING_FEN)
    }
}

