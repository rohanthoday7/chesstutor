package com.chesstutor.core.tutor

import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.ChessRules
import com.chesstutor.core.chess.Move
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.chess.PieceType
import com.chesstutor.core.chess.Square
import com.chesstutor.core.engine.Evaluator

enum class MoveClassification(val title: String, val badgeColorHex: Long) {
    BRILLIANT("Brilliant !!", 0xFF1BA3A3),
    GREAT("Great !", 0xFF5C8BB0),
    BEST("Best Move", 0xFF81B64C),
    GOOD("Good Move", 0xFFA5C369),
    INACCURACY("Inaccuracy ?!", 0xFFF0C15C),
    MISTAKE("Mistake ?", 0xFFE58F2A),
    BLUNDER("Blunder ??", 0xFFCA3431)
}

data class TutorFeedback(
    val classification: MoveClassification,
    val coachMessage: String,
    val patternIdentified: String? = null,
    val suggestedAlternative: Move? = null,
    val alternativeExplanation: String? = null
)

data class MoveLesson(
    val id: String,
    val title: String,
    val category: String, // "Basic Moves", "Special Rules", "Tactical Patterns"
    val description: String,
    val rulesExplanation: String,
    val demoFen: String
)

object ChessTutorAgent {

    // Move lessons library
    val ALL_LESSONS = listOf(
        MoveLesson(
            id = "lesson_castling",
            title = "Castling (Kingside & Queenside)",
            category = "Special Rules",
            description = "The only move where two pieces move at once. Protects your King and activates your Rook.",
            rulesExplanation = "Rules: 1. Neither King nor Rook has moved. 2. All squares between them are empty. 3. King is NOT in check. 4. King does not pass through or land on an attacked square.",
            demoFen = "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        ),
        MoveLesson(
            id = "lesson_en_passant",
            title = "En Passant (In Passing)",
            category = "Special Rules",
            description = "A special pawn capture that prevents enemy pawns from bypassing your pawn's attack.",
            rulesExplanation = "When an opponent pawn moves two squares forward and lands directly adjacent to your pawn, you can capture it diagonally on the square it skipped over. Must be done on the very next turn!",
            demoFen = "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3"
        ),
        MoveLesson(
            id = "lesson_promotion",
            title = "Pawn Promotion",
            category = "Special Rules",
            description = "When a humble pawn marches all the way to the 8th rank, it transforms into any major piece.",
            rulesExplanation = "You can promote to a Queen, Rook, Bishop, or Knight. While Queen is chosen 98% of the time, Knight underpromotion can deliver surprise checks without stalemate!",
            demoFen = "8/4P3/8/8/8/8/8/4K2k w - - 0 1"
        ),
        MoveLesson(
            id = "lesson_knight_fork",
            title = "The Royal Knight Fork",
            category = "Tactical Patterns",
            description = "Knights jump in an L-shape and can attack two valuable pieces at the same time.",
            rulesExplanation = "Because knights cannot be blocked and attack unlike any other piece, forking the King and Queen wins massive material instantly.",
            demoFen = "r1b1k2r/pppp1ppp/8/4q3/1bP5/2N5/PP1BPPPP/R2QKB1R w KQkq - 0 9"
        ),
        MoveLesson(
            id = "lesson_pin",
            title = "Absolute & Relative Pins",
            category = "Tactical Patterns",
            description = "A linear piece (Bishop, Rook, Queen) attacks a piece that cannot move without exposing something worse.",
            rulesExplanation = "An Absolute Pin freezes a piece because moving it exposes the King to illegal check. A Relative Pin freezes a piece because moving it loses the Queen or Rook.",
            demoFen = "r1bqk2r/pppp1ppp/2n2n2/1B2p3/1b2P3/3P1N2/PPP2PPP/RNBQK2R w KQkq - 0 5"
        ),
        MoveLesson(
            id = "lesson_smothered_mate",
            title = "Smothered Mate (Philidor's Legacy)",
            category = "Tactical Patterns",
            description = "A Knight checkmates an enemy King that is completely trapped by its own loyal defenders.",
            rulesExplanation = "The King cannot escape because all surrounding squares are occupied by friendly pieces. Only a Knight can jump and deliver mate here!",
            demoFen = "6k1/5Npp/8/8/8/8/5PPP/6K1 w - - 0 1"
        )
    )

    // Evaluate played move and provide real-time tutor feedback
    fun evaluateMove(
        stateBefore: ChessGameState,
        move: Move,
        tutor: TutorPersonality
    ): TutorFeedback {
        val stateAfter = ChessRules.makeMove(stateBefore, move)
        val evalBefore = Evaluator.evaluate(stateBefore.board)
        val evalAfter = Evaluator.evaluate(stateAfter.board)

        val isWhite = stateBefore.turn == PieceColor.WHITE
        val delta = if (isWhite) evalAfter - evalBefore else evalBefore - evalAfter

        // Check if moving piece is hung
        val targetSquare = move.to
        val movingPiece = stateBefore.board[move.from]
        val isTargetAttacked = ChessRules.isSquareAttacked(stateAfter.board, targetSquare, stateBefore.turn.opposite())
        val isTargetDefended = ChessRules.isSquareAttacked(stateAfter.board, targetSquare, stateBefore.turn)

        val isHanging = isTargetAttacked && !isTargetDefended && (movingPiece?.type != PieceType.PAWN)

        val classification = when {
            delta >= 180 -> MoveClassification.BRILLIANT
            delta >= 80 -> MoveClassification.GREAT
            delta >= -15 -> MoveClassification.BEST
            delta >= -60 -> MoveClassification.GOOD
            delta >= -150 -> MoveClassification.INACCURACY
            delta >= -300 -> MoveClassification.MISTAKE
            else -> MoveClassification.BLUNDER
        }

        val message = when (classification) {
            MoveClassification.BRILLIANT, MoveClassification.GREAT -> tutor.bestMovePraise
            MoveClassification.BEST, MoveClassification.GOOD -> when (tutor.style) {
                TutorStyle.CLASSICAL -> "Solid move. You are maintaining balance and coordinating your pieces well."
                TutorStyle.AGGRESSIVE -> "Good forward momentum! Keep looking for forcing checks and pins."
                TutorStyle.BEGINNER_FRIENDLY -> "Nice job! Your pieces are safe and you are developing nicely."
                TutorStyle.ANALYTICAL -> "Evaluation remains stable. Move matches standard positional guidelines."
            }
            MoveClassification.INACCURACY -> "Notice that this move concedes a bit of ground. There were more active squares available."
            MoveClassification.MISTAKE -> "Watch out! This move allows your opponent to gain a tempo or win a pawn."
            MoveClassification.BLUNDER -> {
                if (isHanging) {
                    "Hold on! Your ${movingPiece?.type?.name?.lowercase()} on ${targetSquare.notation} is unprotected and can be taken for free!"
                } else {
                    tutor.blunderWarningTemplate
                }
            }
        }

        val pattern = when {
            isHanging -> "Hanging Unprotected Piece"
            move.from.rank == stateBefore.turn.backRank && movingPiece?.type == PieceType.QUEEN && stateBefore.fullmoveNumber <= 3 -> "Early Queen Out"
            !stateBefore.castlingRights.whiteKingside && stateBefore.turn == PieceColor.WHITE -> "Delayed Castling"
            else -> null
        }

        return TutorFeedback(
            classification = classification,
            coachMessage = message,
            patternIdentified = pattern,
            suggestedAlternative = null,
            alternativeExplanation = if (classification == MoveClassification.BLUNDER) "Try developing minor pieces to the center first." else null
        )
    }

    // Interactive Doubt Q&A Engine
    fun answerDoubt(
        question: String,
        state: ChessGameState,
        tutor: TutorPersonality
    ): String {
        val q = question.lowercase().trim()

        return when {
            q.contains("castle") || q.contains("castling") -> {
                when (tutor.style) {
                    TutorStyle.CLASSICAL -> "To castle: your King and Rook must not have moved yet, the path between them must be empty, and your King cannot be in check or pass through check. Always prioritize castling before launching a wing attack!"
                    TutorStyle.BEGINNER_FRIENDLY -> "Castling is when your King moves 2 squares towards a Rook, and the Rook hops over to stand right next to him! Just make sure your King isn't currently under attack and the lane is clear."
                    TutorStyle.AGGRESSIVE -> "Castle quickly so your Rook can seize the open center files and start blasting open the opponent's lines!"
                    TutorStyle.ANALYTICAL -> "Castling moves the King to g1/c1 (or g8/c8). FIDE Article 3.8 forbids castling if the king's transit squares are threatened by opposing bitboards."
                }
            }

            q.contains("en passant") || q.contains("passant") -> {
                "En Passant is French for 'in passing'. If an enemy pawn double-steps past your pawn on rank 5 (or rank 4 for Black), you can capture it diagonally behind it on the very next turn. If you don't do it immediately, you lose the right!"
            }

            q.contains("pin") || q.contains("fork") || q.contains("skewer") -> {
                "Tactics are the heart of chess! A Fork is when one piece (often a Knight) attacks two targets simultaneously. A Pin freezes a piece because behind it sits a more valuable target like the King or Queen. A Skewer is the reverse: attacking a high-value piece in front of a lesser one!"
            }

            q.contains("plan") || q.contains("what should i do") || q.contains("best move") -> {
                val legalCount = ChessRules.getLegalMoves(state).size
                val inCheck = ChessRules.isInCheck(state.board, state.turn)
                if (inCheck) {
                    "Your King is in check right now! You must either move your King, capture the checking piece, or block the check."
                } else if (state.fullmoveNumber <= 8) {
                    "In the opening: 1. Stake a claim in the center with pawns. 2. Develop Knights before Bishops. 3. Castle your King to safety. Avoid moving the same piece twice."
                } else {
                    "In the middlegame: Look for open files for your rooks, identify your opponent's weakest pawn, and look for tactical forks or pins."
                }
            }

            q.contains("promote") || q.contains("promotion") -> {
                "When your pawn reaches the other end of the board (rank 8 for White, rank 1 for Black), it immediately transforms into a Queen, Rook, Bishop, or Knight. Always choose Queen unless a Knight check is needed to avoid stalemate!"
            }

            q.contains("trade") || q.contains("exchange") -> {
                "Before trading pieces, ask: Does this trade activate my opponent's pieces or mine? Pawns are worth 1, Knights & Bishops 3, Rooks 5, and Queens 9. Never trade a piece that has an active outpost for a passive piece."
            }

            else -> {
                "Great question! In this position with ${state.turn.name.lowercase().replaceFirstChar { it.uppercase() }} to move, always verify your piece safety first. Look for undefended pieces, ensure your king is secure, and coordinate your minor pieces towards the center."
            }
        }
    }
}

