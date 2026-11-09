package com.chesstutor.core.tutor

enum class TutorStyle {
    CLASSICAL,
    AGGRESSIVE,
    BEGINNER_FRIENDLY,
    ANALYTICAL
}

data class TutorPersonality(
    val id: String,
    val name: String,
    val title: String,
    val avatarEmoji: String,
    val style: TutorStyle,
    val bio: String,
    val greeting: String,
    val blunderWarningTemplate: String,
    val bestMovePraise: String
) {
    companion object {
        val ALL_TUTORS = listOf(
            TutorPersonality(
                id = "mikhail",
                name = "GM Mikhail",
                title = "Classical Grandmaster",
                avatarEmoji = "👴",
                style = TutorStyle.CLASSICAL,
                bio = "Emphasizes solid pawn structures, king safety, center control, and Capablanca's classical fundamentals.",
                greeting = "Greetings! Chess is an art of patience. Control the center, tuck your king away, and the game will reward you.",
                blunderWarningTemplate = "Careful! A rushed attack weakens your pawn foundation. Consider piece coordination before striking.",
                bestMovePraise = "Magnificent! A true classical positional move that bolsters your central command."
            ),
            TutorPersonality(
                id = "viktor",
                name = "Coach Viktor",
                title = "Tactical Firebrand",
                avatarEmoji = "🔥",
                style = TutorStyle.AGGRESSIVE,
                bio = "Specializes in aggressive sacrifices, forks, pins, skewers, and dynamic counter-attacks inspired by Tal and Kasparov.",
                greeting = "Ready to create fireworks? Never back down! Look for every check, capture, and tactical threat!",
                blunderWarningTemplate = "Hold your horses! You're stepping right into a pin or fork. Look at their tactical replies!",
                bestMovePraise = "Boom! That's a venomous tactical blow! Your opponent is on the ropes."
            ),
            TutorPersonality(
                id = "lily",
                name = "Coach Lily",
                title = "Friendly Mentor",
                avatarEmoji = "👩‍🏫",
                style = TutorStyle.BEGINNER_FRIENDLY,
                bio = "Gentle, step-by-step teacher who explains basic and special moves (Castling, En Passant, Promotion) without complex jargon.",
                greeting = "Hello! Don't worry if you make mistakes—that's how all chess champions learn! Let's explore the board together.",
                blunderWarningTemplate = "Oops! That piece might be in danger of being captured for free. Let's find a safer square together!",
                bestMovePraise = "Awesome move! You kept your pieces protected and advanced your plan!"
            ),
            TutorPersonality(
                id = "sofia",
                name = "Prof. Sofia",
                title = "Chess Scientist",
                avatarEmoji = "🧠",
                style = TutorStyle.ANALYTICAL,
                bio = "Calculates exact centipawn evaluations, piece mobility, opening probabilities, and endgame tablebases.",
                greeting = "Welcome. The game tree contains 10^120 possibilities. Let's analyze your patterns and optimize your win rate mathematically.",
                blunderWarningTemplate = "Suboptimal evaluation detected (drops by -2.4 pawns). Opponent has a forcing sequence here.",
                bestMovePraise = "Mathematically optimal. Top engine choice with an expected win probability increase of +14%."
            )
        )

        fun getById(id: String): TutorPersonality {
            return ALL_TUTORS.firstOrNull { it.id == id } ?: ALL_TUTORS[0]
        }
    }
}

