package com.chesstutor.data.local

import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.PieceColor

data class ArchivedGame(
    val id: String,
    val opponentName: String,
    val opponentRating: Int,
    val playerColor: PieceColor,
    val result: String, // "1-0", "0-1", "1/2-1/2"
    val winLossDraw: String, // "WIN", "LOSS", "DRAW"
    val accuracyPercentage: Float,
    val date: String,
    val pgn: String,
    val timeControl: String,
    val finalFen: String
)

data class OfflinePuzzle(
    val id: String,
    val title: String,
    val rating: Int,
    val fen: String,
    val solutionSans: List<String>,
    val theme: String,
    val description: String
)

object GameArchiveRepository {

    private val games = mutableListOf<ArchivedGame>()

    init {
        // Pre-populate with initial master reference games for immediate game review and analytics
        games.add(
            ArchivedGame(
                id = "game_01",
                opponentName = "Nelson (Bot)",
                opponentRating = 1200,
                playerColor = PieceColor.WHITE,
                result = "1-0",
                winLossDraw = "WIN",
                accuracyPercentage = 86.4f,
                date = "Today",
                pgn = "1. e4 e5 2. Qh5 Nc6 3. Bc4 Nf6 4. Qxf7# 1-0",
                timeControl = "10 min",
                finalFen = "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4"
            )
        )
        games.add(
            ArchivedGame(
                id = "game_02",
                opponentName = "Elena (Bot)",
                opponentRating = 1600,
                playerColor = PieceColor.BLACK,
                result = "0-1",
                winLossDraw = "WIN",
                accuracyPercentage = 91.2f,
                date = "Yesterday",
                pgn = "1. e4 c5 2. Nf3 d6 3. d4 cxd4 4. Nxd4 Nf6 5. Nc3 a6 6. Be3 e5 0-1",
                timeControl = "5+3",
                finalFen = "rnbqkb1r/1p3ppp/p2p1n2/4p3/4P3/2N1BN2/PPP2PPP/R2QKB1R w KQkq e6 0 7"
            )
        )
    }

    fun getAllGames(): List<ArchivedGame> = games.toList()

    fun addGame(game: ArchivedGame) {
        games.add(0, game)
    }

    // Curated offline puzzles for offline tactics and Puzzle Rush mode
    val OFFLINE_PUZZLES = listOf(
        OfflinePuzzle(
            id = "puz_01",
            title = "Anastasia's Mate",
            rating = 1100,
            fen = "5rk1/1p3ppp/8/8/8/8/1R3PPP/4R1K1 w - - 0 1",
            solutionSans = listOf("Rxb7"),
            theme = "Back Rank Mate",
            description = "Exploit the weak back rank!"
        ),
        OfflinePuzzle(
            id = "puz_02",
            title = "Royal Fork",
            rating = 1250,
            fen = "r1b1k2r/pppp1ppp/8/4q3/1bP5/2N5/PP1BPPPP/R2QKB1R w KQkq - 0 9",
            solutionSans = listOf("Nd5", "Bxd2+", "Qxd2"),
            theme = "Knight Fork",
            description = "Fork the Queen and Bishop!"
        ),
        OfflinePuzzle(
            id = "puz_03",
            title = "Greek Gift Sacrifice",
            rating = 1400,
            fen = "r1bq1rk1/pp1nbppp/2p1pn2/3p4/2PP4/2N1PN2/PPB2PPP/R1BQK2R w KQ - 0 8",
            solutionSans = listOf("Bxh7+", "Kxh7", "Ng5+"),
            theme = "Sacrifice",
            description = "Classic classical kingside breakthrough!"
        ),
        OfflinePuzzle(
            id = "puz_04",
            title = "Smothered Mate",
            rating = 1600,
            fen = "6k1/5ppp/8/8/8/5N2/5PPP/4Q1K1 w - - 0 1",
            solutionSans = listOf("Qe8#"),
            theme = "Back Rank",
            description = "Deliver checkmate in one move!"
        ),
        OfflinePuzzle(
            id = "puz_05",
            title = "Windmill Tactic",
            rating = 1750,
            fen = "r2q1rk1/5ppp/b7/8/8/8/5PPP/R1Q2RK1 w - - 0 1",
            solutionSans = listOf("Rxa6"),
            theme = "Discovered Attack",
            description = "Capture the unprotected bishop!"
        )
    )
}

