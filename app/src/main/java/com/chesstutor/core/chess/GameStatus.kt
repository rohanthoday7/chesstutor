package com.chesstutor.core.chess

sealed interface GameStatus {
    object Active : GameStatus
    data class Checkmate(val winner: PieceColor) : GameStatus
    object Stalemate : GameStatus
    object DrawInsufficientMaterial : GameStatus
    object DrawFiftyMoves : GameStatus
    object DrawThreefoldRepetition : GameStatus
    data class Resignation(val winner: PieceColor) : GameStatus
    data class Timeout(val winner: PieceColor) : GameStatus

    val isGameOver: Boolean
        get() = this !is Active

    val description: String
        get() = when (this) {
            is Active -> "Game in progress"
            is Checkmate -> "Checkmate! ${winner.name.lowercase().replaceFirstChar { it.uppercase() }} wins!"
            is Stalemate -> "Draw by stalemate"
            is DrawInsufficientMaterial -> "Draw by insufficient material"
            is DrawFiftyMoves -> "Draw by 50-move rule"
            is DrawThreefoldRepetition -> "Draw by threefold repetition"
            is Resignation -> "${winner.name.lowercase().replaceFirstChar { it.uppercase() }} wins by resignation"
            is Timeout -> "${winner.name.lowercase().replaceFirstChar { it.uppercase() }} wins on time"
        }
}

