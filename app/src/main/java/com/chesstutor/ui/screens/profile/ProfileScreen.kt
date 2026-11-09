package com.chesstutor.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chesstutor.ui.components.DnaRadarChart
import com.chesstutor.ui.theme.ChessCardDark
import com.chesstutor.ui.theme.ChessDarkBg
import com.chesstutor.ui.theme.ChessGreenPrimary
import com.chesstutor.ui.theme.ChessTextMuted
import com.chesstutor.ui.theme.ChessTextPrimary
import com.chesstutor.ui.theme.ChessTextSecondary
import com.chesstutor.ui.theme.ChessYellowAccent
import com.chesstutor.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val dna = state.dnaProfile

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChessDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // User Profile Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChessCardDark)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF383531)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.avatarEmoji, fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ChessGreenPrimary)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(text = state.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.username,
                        color = ChessTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.totalGamesPlayed} Games • ${state.winCount}W / ${state.lossCount}L / ${state.drawCount}D",
                    color = ChessTextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ratings Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RatingCard(label = "Rapid", rating = state.rapidRating, emoji = "⏱️", modifier = Modifier.weight(1f))
            RatingCard(label = "Blitz", rating = state.blitzRating, emoji = "⚡", modifier = Modifier.weight(1f))
            RatingCard(label = "Bullet", rating = state.bulletRating, emoji = "🚅", modifier = Modifier.weight(1f))
            RatingCard(label = "Puzzles", rating = state.puzzleRating, emoji = "🧩", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CHESS DNA SECTION
        Text(
            text = "🧬 Chess DNA Player Intelligence",
            color = ChessTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Algorithmic analysis of your playing style, strengths and weaknesses",
            color = ChessTextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChessCardDark)
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Archetype Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF383531))
                        .border(1.dp, ChessGreenPrimary, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Archetype: ${dna.playerArchetype}",
                        color = ChessGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Radar Chart
                DnaRadarChart(metrics = dna.metrics)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = dna.archetypeDescription,
                    color = ChessTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Strengths & Weaknesses
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChessCardDark)
                .padding(16.dp)
        ) {
            Column {
                Text(text = "✅ Key Strengths", color = ChessGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                dna.strengths.forEach { s ->
                    Text(text = "• $s", color = ChessTextSecondary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "⚠️ Areas for Improvement", color = ChessYellowAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                dna.weaknesses.forEach { w ->
                    Text(text = "• $w", color = ChessTextSecondary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "🎯 Recommended Training", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                dna.trainingRecommendations.forEach { r ->
                    Text(text = "• $r", color = ChessTextSecondary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Games Archive
        Text(
            text = "Game Archive & Review",
            color = ChessTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        state.archivedGames.forEach { game ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChessCardDark)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val badgeColor = if (game.winLossDraw == "WIN") ChessGreenPrimary else if (game.winLossDraw == "DRAW") Color(0xFFC0C0C0) else Color(0xFFCA3431)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = game.winLossDraw, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "vs ${game.opponentName} (${game.opponentRating})", color = ChessTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${game.date} • ${game.timeControl}", color = ChessTextMuted, fontSize = 11.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Accuracy: ${game.accuracyPercentage}%", color = ChessGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = game.result, color = ChessTextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RatingCard(label: String, rating: Int, emoji: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ChessCardDark)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "$rating", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = label, color = ChessTextMuted, fontSize = 10.sp)
        }
    }
}

