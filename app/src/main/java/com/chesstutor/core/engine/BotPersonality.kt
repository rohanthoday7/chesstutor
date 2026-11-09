package com.chesstutor.core.engine

data class BotPersonality(
    val id: String,
    val name: String,
    val title: String,
    val elo: Int,
    val searchDepth: Int,
    val blunderProbability: Float, // 0.0 to 1.0 chance of playing a suboptimal move
    val quote: String,
    val playstyleDescription: String,
    val avatarEmoji: String
) {
    companion object {
        val ALL_BOTS = listOf(
            BotPersonality(
                id = "jimmy",
                name = "Jimmy",
                title = "Beginner",
                elo = 600,
                searchDepth = 1,
                blunderProbability = 0.40f,
                quote = "I just learned how the knight moves! Let's have fun!",
                playstyleDescription = "Frequently makes early pawn moves and leaves pieces hanging.",
                avatarEmoji = "🐣"
            ),
            BotPersonality(
                id = "nelson",
                name = "Nelson",
                title = "Aggressive",
                elo = 1200,
                searchDepth = 3,
                blunderProbability = 0.15f,
                quote = "Watch out, my Queen is coming out on move two!",
                playstyleDescription = "Loves early Queen attacks (Wayward Queen attack, Scholar's mate).",
                avatarEmoji = "🦁"
            ),
            BotPersonality(
                id = "elena",
                name = "Elena",
                title = "Tactician",
                elo = 1600,
                searchDepth = 4,
                blunderProbability = 0.05f,
                quote = "Look for forks and pins; chess is 99% tactics.",
                playstyleDescription = "Plays solid opening principles, spots forks, pins, and skewers.",
                avatarEmoji = "🎯"
            ),
            BotPersonality(
                id = "master_alex",
                name = "Master Alex",
                title = "International Master",
                elo = 2200,
                searchDepth = 5,
                blunderProbability = 0.005f,
                quote = "Pawn structure and king safety dictate the outcome.",
                playstyleDescription = "Relentless positional pressure, deep tactical calculations.",
                avatarEmoji = "♟️"
            ),
            BotPersonality(
                id = "magnus_ai",
                name = "Magnus Bot",
                title = "Grandmaster",
                elo = 2800,
                searchDepth = 6,
                blunderProbability = 0.0f,
                quote = "Some people think that if their opponent plays a beautiful game, it's okay to lose. I try not to.",
                playstyleDescription = "Squeezes water from a stone in endgames. Precise, ruthless calculations.",
                avatarEmoji = "👑"
            )
        )

        fun getById(id: String): BotPersonality {
            return ALL_BOTS.firstOrNull { it.id == id } ?: ALL_BOTS[0]
        }
    }
}

