package com.chesstutor.core.dna

import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.MoveRecord
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.chess.PieceType
import com.chesstutor.core.engine.OpeningBook
import kotlin.math.roundToInt

data class DnaRadarMetrics(
    val tactics: Int,         // 0..100
    val opening: Int,         // 0..100
    val endgame: Int,         // 0..100
    val aggressiveness: Int,  // 0..100
    val timeManagement: Int,  // 0..100
    val resilience: Int       // 0..100
) {
    val overallRating: Int
        get() = ((tactics + opening + endgame + aggressiveness + timeManagement + resilience) / 6.0).roundToInt()
}

data class ChessDnaProfile(
    val metrics: DnaRadarMetrics,
    val playerArchetype: String,     // e.g. "Aggressive Tactician", "Solid Positionalist", "Dynamic Flank Attacker"
    val archetypeDescription: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val trainingRecommendations: List<String>
)

object ChessDnaAnalyzer {

    fun analyzeGames(games: List<ChessGameState>, playerColor: PieceColor = PieceColor.WHITE): ChessDnaProfile {
        if (games.isEmpty()) {
            return defaultProfile()
        }

        var totalMoves = 0
        var tacticalHits = 0
        var checksGiven = 0
        var bookMovesCount = 0
        var endgameMoves = 0

        for (state in games) {
            val history = state.moveHistory
            val openingSans = history.map { it.san }
            val recognizedOpening = OpeningBook.identifyOpening(openingSans)

            if (recognizedOpening != null) {
                bookMovesCount += minOf(recognizedOpening.movesSan.size, history.size)
            }

            for (i in history.indices) {
                val record = history[i]
                val isPlayerTurn = if (playerColor == PieceColor.WHITE) i % 2 == 0 else i % 2 == 1
                if (!isPlayerTurn) continue

                totalMoves++
                if (record.capturedPiece != null) tacticalHits++
                if (record.san.contains("+") || record.san.contains("#")) checksGiven++

                val pieceCount = state.board.countPieces().values.sum()
                if (pieceCount <= 10) {
                    endgameMoves++
                }
            }
        }

        val totalSafe = maxOf(1, totalMoves)
        val tacticsScore = ((tacticalHits.toFloat() / totalSafe * 150f).coerceIn(40f, 95f)).roundToInt()
        val aggressiveScore = (((checksGiven + tacticalHits).toFloat() / totalSafe * 130f).coerceIn(35f, 96f)).roundToInt()
        val openingScore = ((bookMovesCount.toFloat() / maxOf(1, games.size * 6) * 100f).coerceIn(45f, 92f)).roundToInt()
        val endgameScore = ((endgameMoves.toFloat() / totalSafe * 120f).coerceIn(40f, 90f)).roundToInt()
        val timeScore = 78
        val resilienceScore = 75

        val metrics = DnaRadarMetrics(
            tactics = tacticsScore,
            opening = openingScore,
            endgame = endgameScore,
            aggressiveness = aggressiveScore,
            timeManagement = timeScore,
            resilience = resilienceScore
        )

        val archetype = determineArchetype(metrics)
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()
        val recs = mutableListOf<String>()

        if (metrics.tactics >= 70) strengths.add("Strong eye for tactical captures and checks")
        else weaknesses.add("Frequently misses tactical opportunities and defensive pins")

        if (metrics.aggressiveness >= 65) strengths.add("Relentless attacking pressure against enemy king")
        else weaknesses.add("Overly passive piece placement in open positions")

        if (metrics.opening >= 70) strengths.add("Solid opening knowledge; establishes center control early")
        else {
            weaknesses.add("Deviates early from established opening theory")
            recs.add("Study the Italian Game & Sicilian Defense in the Learn tab")
        }

        if (metrics.endgame < 65) {
            weaknesses.add("Endgame conversion rate drops when Queens are traded")
            recs.add("Practice King & Pawn endgame drills")
        } else {
            strengths.add("Patient and accurate endgame technique")
        }

        recs.add("Solve 5 daily puzzles each morning to sharpen calculation speed")

        return ChessDnaProfile(
            metrics = metrics,
            playerArchetype = archetype.first,
            archetypeDescription = archetype.second,
            strengths = strengths,
            weaknesses = weaknesses,
            trainingRecommendations = recs
        )
    }

    private fun determineArchetype(metrics: DnaRadarMetrics): Pair<String, String> {
        return when {
            metrics.aggressiveness >= 75 && metrics.tactics >= 70 -> Pair(
                "Aggressive Tactician",
                "You thrive in open, dynamic games with tactical fireworks. You enjoy sacrificing material for initiative."
            )
            metrics.opening >= 75 && metrics.endgame >= 70 -> Pair(
                "Classical Strategist",
                "You prize structure, safety, and smooth technical conversion. You excel at outplaying opponents in quiet endgames."
            )
            metrics.tactics >= 70 -> Pair(
                "Tactical Opportunist",
                "You pounce instantly on opponent blunders, turning small tactical lapses into decisive victories."
            )
            else -> Pair(
                "Balanced Explorer",
                "You have a well-rounded foundation across all phases of the game, ready to specialize as your experience grows."
            )
        }
    }

    fun defaultProfile(): ChessDnaProfile {
        val metrics = DnaRadarMetrics(
            tactics = 72,
            opening = 68,
            endgame = 64,
            aggressiveness = 75,
            timeManagement = 80,
            resilience = 70
        )
        return ChessDnaProfile(
            metrics = metrics,
            playerArchetype = "Dynamic Attacker",
            archetypeDescription = "You actively seek initiative, love open files, and attack the opponent's king with conviction.",
            strengths = listOf(
                "Sharp calculation in complex middlegames",
                "Rapid piece mobilization and active center control",
                "High speed and poise in time scrambles"
            ),
            weaknesses = listOf(
                "Occasional overextension when hunting the enemy king",
                "Rushing rook endgames instead of activating the king"
            ),
            trainingRecommendations = listOf(
                "Solve 3-minute Puzzle Rush daily to maintain tactical vision",
                "Practice King & Pawn endgame opposition principles in the Learn tab",
                "Explore the Ruy Lopez and Sicilian variations in Opening Explorer"
            )
        )
    }
}

