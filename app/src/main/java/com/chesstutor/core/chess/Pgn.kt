package com.chesstutor.core.chess

data class PgnMetadata(
    val event: String = "Casual Game",
    val site: String = "ChessTutor App",
    val date: String = "2026.11.05",
    val round: String = "1",
    val white: String = "Player 1",
    val black: String = "Player 2",
    val result: String = "*",
    val initialFen: String? = null
)

object PgnUtils {

    fun exportToPgn(state: ChessGameState, metadata: PgnMetadata): String {
        val sb = StringBuilder()
        sb.append("[Event \"${metadata.event}\"]\n")
        sb.append("[Site \"${metadata.site}\"]\n")
        sb.append("[Date \"${metadata.date}\"]\n")
        sb.append("[Round \"${metadata.round}\"]\n")
        sb.append("[White \"${metadata.white}\"]\n")
        sb.append("[Black \"${metadata.black}\"]\n")
        sb.append("[Result \"${metadata.result}\"]\n")
        if (metadata.initialFen != null && metadata.initialFen != ChessGameState.STARTING_FEN) {
            sb.append("[SetUp \"1\"]\n")
            sb.append("[FEN \"${metadata.initialFen}\"]\n")
        }
        sb.append("\n")

        var moveNum = 1
        for (i in state.moveHistory.indices) {
            if (i % 2 == 0) {
                sb.append("$moveNum. ")
            }
            sb.append("${state.moveHistory[i].san} ")
            if (i % 2 == 1) {
                moveNum++
            }
        }
        sb.append(metadata.result)
        return sb.toString().trim()
    }

    fun parseSanMove(state: ChessGameState, rawSan: String): Move? {
        val cleanSan = rawSan.replace("+", "").replace("#", "").replace("!", "").replace("?", "").trim()
        val legalMoves = ChessRules.getLegalMoves(state)

        for (m in legalMoves) {
            val movingPiece = state.board[m.from] ?: continue
            val capturedPiece = state.board[m.to]
            val testBoard = state.board.copy()
            testBoard[m.from] = null
            testBoard[m.to] = if (m.promotion != null) Piece(m.promotion, movingPiece.color) else movingPiece

            val testSan = ChessRules.moveToSan(state, m, movingPiece, capturedPiece, testBoard, legalMoves)
            val cleanTestSan = testSan.replace("+", "").replace("#", "").trim()

            if (cleanSan == cleanTestSan || cleanSan == m.uci) {
                return m
            }
        }
        return null
    }
}

