package com.chesstutor.core.engine

data class OpeningEntry(
    val eco: String,
    val name: String,
    val movesSan: List<String>,
    val description: String
)

object OpeningBook {

    val OPENINGS = listOf(
        OpeningEntry("C60", "Ruy Lopez", listOf("e4", "e5", "Nf3", "Nc6", "Bb5"), "One of the oldest and most classic chess openings."),
        OpeningEntry("B20", "Sicilian Defense", listOf("e4", "c5"), "The most popular and aggressive response to 1. e4."),
        OpeningEntry("B90", "Sicilian Defense: Najdorf Variation", listOf("e4", "c5", "Nf3", "d6", "d4", "cxd4", "Nxd4", "Nf6", "Nc3", "a6"), "Sharp and fighting variation favored by Kasparov and Fischer."),
        OpeningEntry("C00", "French Defense", listOf("e4", "e6"), "Solid counter-attacking opening challenging White's center with d5."),
        OpeningEntry("B10", "Caro-Kann Defense", listOf("e4", "c6"), "Extremely solid pawn structure leading to safe endgames."),
        OpeningEntry("C50", "Italian Game", listOf("e4", "e5", "Nf3", "Nc6", "Bc4"), "Focuses on rapid piece development and f7 pressure."),
        OpeningEntry("D06", "Queen's Gambit", listOf("d4", "d5", "c4"), "White offers a wing pawn to gain control of the center."),
        OpeningEntry("D30", "Queen's Gambit Declined", listOf("d4", "d5", "c4", "e6"), "Classical solid defense maintaining a foothold on d5."),
        OpeningEntry("D20", "Queen's Gambit Accepted", listOf("d4", "d5", "c4", "dxc4"), "Black accepts the gambit and aims to strike back at the center."),
        OpeningEntry("E60", "King's Indian Defense", listOf("d4", "Nf6", "c4", "g6"), "Hypermodern opening where Black builds a fortress and launches a kingside attack."),
        OpeningEntry("A10", "English Opening", listOf("c4"), "Flank opening fighting for d5 without committing central pawns early."),
        OpeningEntry("D02", "London System", listOf("d4", "d5", "Nf3", "Nf6", "Bf4"), "Versatile and solid opening scheme playable against almost any Black setup."),
        OpeningEntry("C42", "Petrov's Defense", listOf("e4", "e5", "Nf3", "Nf6"), "Symmetrical defense aiming for total equality and drawish security."),
        OpeningEntry("B01", "Scandinavian Defense", listOf("e4", "d5"), "Direct challenge to White's e4 pawn on move one."),
        OpeningEntry("C20", "King's Pawn Game", listOf("e4", "e5"), "Open game leading to rich tactical play.")
    )

    fun identifyOpening(moveHistorySans: List<String>): OpeningEntry? {
        if (moveHistorySans.isEmpty()) return null

        var bestMatch: OpeningEntry? = null
        var maxMatchedMoves = 0

        for (opening in OPENINGS) {
            val length = opening.movesSan.size
            if (moveHistorySans.size >= length) {
                val matches = opening.movesSan.indices.all { i ->
                    moveHistorySans[i].equals(opening.movesSan[i], ignoreCase = true)
                }
                if (matches && length > maxMatchedMoves) {
                    maxMatchedMoves = length
                    bestMatch = opening
                }
            } else {
                val matches = moveHistorySans.indices.all { i ->
                    moveHistorySans[i].equals(opening.movesSan[i], ignoreCase = true)
                }
                if (matches && moveHistorySans.size > maxMatchedMoves) {
                    maxMatchedMoves = moveHistorySans.size
                    bestMatch = opening
                }
            }
        }
        return bestMatch
    }

    fun getBookMove(moveHistorySans: List<String>): String? {
        for (opening in OPENINGS) {
            if (opening.movesSan.size > moveHistorySans.size) {
                val matches = moveHistorySans.indices.all { i ->
                    moveHistorySans[i].equals(opening.movesSan[i], ignoreCase = true)
                }
                if (matches) {
                    return opening.movesSan[moveHistorySans.size]
                }
            }
        }
        return null
    }
}

