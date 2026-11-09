package com.chesstutor.ui.screens.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.engine.BotPersonality
import com.chesstutor.ui.components.NavSection
import com.chesstutor.ui.theme.ChessCardDark
import com.chesstutor.ui.theme.ChessDarkBg
import com.chesstutor.ui.theme.ChessGreenPrimary
import com.chesstutor.ui.theme.ChessTextMuted
import com.chesstutor.ui.theme.ChessTextPrimary
import com.chesstutor.ui.theme.ChessTextSecondary
import com.chesstutor.ui.theme.ChessYellowAccent
import com.chesstutor.viewmodel.GameMode
import kotlinx.coroutines.delay

data class TimeControlOption(
    val name: String,
    val minutes: Int,
    val incrementSec: Int,
    val category: String, // "Bullet", "Blitz", "Rapid", "Classical"
    val emoji: String,
    val isPopular: Boolean = false
)

val PRESET_TIME_CONTROLS = listOf(
    TimeControlOption("1 min", 1, 0, "Bullet", "🚅"),
    TimeControlOption("1 | 1", 1, 1, "Bullet", "🚅"),
    TimeControlOption("2 | 1", 2, 1, "Bullet", "🚅"),
    TimeControlOption("3 min", 3, 0, "Blitz", "⚡", isPopular = true),
    TimeControlOption("3 | 2", 3, 2, "Blitz", "⚡"),
    TimeControlOption("5 min", 5, 0, "Blitz", "🔥", isPopular = true),
    TimeControlOption("10 min", 10, 0, "Rapid", "⏱️", isPopular = true),
    TimeControlOption("15 | 10", 15, 10, "Rapid", "🕒"),
    TimeControlOption("30 min", 30, 0, "Classical", "📅")
)

@Composable
fun HomeScreen(
    onStartGame: (GameMode, BotPersonality, Int, Int, PieceColor) -> Unit,
    onNavigateSection: (NavSection) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTimeControl by remember { mutableStateOf(PRESET_TIME_CONTROLS[6]) } // 10 min Rapid
    var isRated by remember { mutableStateOf(true) }

    // Dialog States
    var isTimeControlSheetOpen by remember { mutableStateOf(false) }
    var isSearchingOpponent by remember { mutableStateOf(false) }
    var searchTimer by remember { mutableIntStateOf(0) }
    var matchedOpponent by remember { mutableStateOf<Pair<String, Int>?>(null) }

    var isBotSelectorOpen by remember { mutableStateOf(false) }
    var selectedBot by remember { mutableStateOf(BotPersonality.ALL_BOTS[1]) } // Nelson
    var selectedColor by remember { mutableStateOf(PieceColor.WHITE) }

    var isPlayFriendOpen by remember { mutableStateOf(false) }

    // Simulated Matchmaking Radar Loop
    LaunchedEffect(isSearchingOpponent) {
        if (isSearchingOpponent) {
            searchTimer = 0
            matchedOpponent = null
            for (i in 1..2) {
                delay(1000L)
                searchTimer = i
            }
            // Pair with realistic online opponent
            val mockChallengers = listOf(
                Pair("Viktor_K 🇺🇸", 1435),
                Pair("Elena_R 🇩🇪", 1410),
                Pair("Alex_Tactics 🇬🇧", 1450),
                Pair("GrandPawn 🇨🇦", 1390),
                Pair("ChessKnight 🇫🇷", 1425)
            )
            val matched = mockChallengers.random()
            matchedOpponent = matched
            delay(1200L) // Show "Match Found!" before entering
            isSearchingOpponent = false
            onStartGame(
                GameMode.ONLINE,
                BotPersonality.ALL_BOTS[1],
                selectedTimeControl.minutes,
                selectedTimeControl.incrementSec,
                PieceColor.WHITE
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChessDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Header (Chess.com layout)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "♟️", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Chessable",
                        color = ChessTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Play a Game",
                        color = ChessGreenPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF262421))
                    .border(1.dp, Color(0xFF383531), RoundedCornerShape(20.dp))
                    .clickable { onNavigateSection(NavSection.PROFILE) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "Rating: 1420 ⚡", color = ChessYellowAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ================= HERO: CHESS.COM "NEW GAME" PLAY CARD =================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ChessCardDark)
                .border(1.dp, Color(0xFF3B3835), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                // Time Control Selector & Rated Toggle Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Control Dropdown Chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1C1A))
                            .border(1.dp, Color(0xFF45423E), RoundedCornerShape(8.dp))
                            .clickable { isTimeControlSheetOpen = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedTimeControl.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${selectedTimeControl.name} • ${selectedTimeControl.category}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "▾", color = ChessTextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Rated vs Unrated Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRated) ChessGreenPrimary.copy(alpha = 0.15f) else Color(0xFF1E1C1A))
                            .border(1.dp, if (isRated) ChessGreenPrimary.copy(alpha = 0.5f) else Color(0xFF383531), RoundedCornerShape(8.dp))
                            .clickable { isRated = !isRated }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (isRated) "⚡ Rated" else "Casual", color = if (isRated) ChessGreenPrimary else ChessTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The Big Iconic 3D Green "PLAY" Button (Chess.com Style)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF537B2F)) // 3D bottom border shadow
                        .padding(bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ChessGreenPrimary)
                            .clickable { isSearchingOpponent = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "⚔️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "PLAY (${selectedTimeControl.name})",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Popular Time Control Pills
                Text(
                    text = "POPULAR TIME CONTROLS",
                    color = ChessTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        PRESET_TIME_CONTROLS[0], // 1 min Bullet
                        PRESET_TIME_CONTROLS[3], // 3 min Blitz
                        PRESET_TIME_CONTROLS[5], // 5 min Blitz
                        PRESET_TIME_CONTROLS[6]  // 10 min Rapid
                    ).forEach { tc ->
                        val isSelected = selectedTimeControl == tc
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF383531) else Color(0xFF1E1C1A))
                                .border(1.dp, if (isSelected) ChessGreenPrimary else Color(0xFF33302C), RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedTimeControl = tc
                                    isSearchingOpponent = true
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = tc.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tc.name,
                                    color = if (isSelected) ChessGreenPrimary else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ================= SECTION: OTHER WAYS TO PLAY =================
        Text(
            text = "MORE WAYS TO PLAY",
            color = ChessTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Play vs Computer / Bots (Chess.com Bot library)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChessCardDark)
                .border(1.dp, Color(0xFF383531), RoundedCornerShape(12.dp))
                .clickable { isBotSelectorOpen = true }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF383531)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🤖", fontSize = 26.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Play Computer",
                            color = ChessTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Challenge Jimmy (600), Nelson, Elena or Magnus",
                            color = ChessTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Text(text = "▶", color = ChessGreenPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Play a Friend / Pass & Play
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChessCardDark)
                .border(1.dp, Color(0xFF383531), RoundedCornerShape(12.dp))
                .clickable { isPlayFriendOpen = true }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF383531)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👥", fontSize = 26.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Play a Friend",
                            color = ChessTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Challenge online friends or Pass & Play locally",
                            color = ChessTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Text(text = "▶", color = ChessGreenPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ================= DAILY PUZZLE & CHESS DNA =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Daily Puzzle
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChessCardDark)
                    .border(1.dp, Color(0xFF383531), RoundedCornerShape(12.dp))
                    .clickable { onNavigateSection(NavSection.PUZZLES) }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🧩", fontSize = 24.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ChessGreenPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "DAILY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Daily Puzzle",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Solve tactical puzzle",
                        color = ChessTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Chess DNA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChessCardDark)
                    .border(1.dp, Color(0xFF383531), RoundedCornerShape(12.dp))
                    .clickable { onNavigateSection(NavSection.PROFILE) }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🧬", fontSize = 24.sp)
                        Text(text = "1420 ⚡", color = ChessYellowAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chess DNA",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Attacker (78% Sharp)",
                        color = ChessTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // =========================================================================
    // MODALS & SHEETS (CHESS.COM STYLE)
    // =========================================================================

    // 1. MATCHMAKING RADAR DIALOG ("FINDING OPPONENT...")
    if (isSearchingOpponent) {
        Dialog(onDismissRequest = { isSearchingOpponent = false }) {
            val infiniteTransition = rememberInfiniteTransition(label = "radar")
            val radarScale by infiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF262421))
                    .border(1.dp, Color(0xFF45423E), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (matchedOpponent == null) {
                        // Pulsing Radar Animation
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(radarScale)
                                .clip(CircleShape)
                                .background(ChessGreenPrimary.copy(alpha = 0.18f))
                                .border(2.dp, ChessGreenPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚔️", fontSize = 36.sp)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Finding Opponent...",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedTimeControl.name} • ${selectedTimeControl.category} (${if (isRated) "Rated" else "Casual"})",
                            color = ChessGreenPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Rating Range: 1370 - 1470  (0:0${searchTimer})",
                            color = ChessTextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { isSearchingOpponent = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF383531)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "✕ Cancel Search", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        // Match Found Flash!
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(ChessGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✓", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Match Found!",
                            color = ChessGreenPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "vs ${matchedOpponent?.first} (${matchedOpponent?.second})",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You play as White • Game starting...",
                            color = ChessTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // 2. TIME CONTROL SELECTOR MODAL
    if (isTimeControlSheetOpen) {
        Dialog(onDismissRequest = { isTimeControlSheetOpen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF262421))
                    .border(1.dp, Color(0xFF45423E), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Choose Time Control",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "✕",
                            color = ChessTextMuted,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { isTimeControlSheetOpen = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PRESET_TIME_CONTROLS.forEach { tc ->
                            val isSelected = selectedTimeControl == tc
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF383531) else Color(0xFF1E1C1A))
                                    .border(1.dp, if (isSelected) ChessGreenPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedTimeControl = tc
                                        isTimeControlSheetOpen = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = tc.emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = tc.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = tc.category, color = ChessTextMuted, fontSize = 11.sp)
                                    }
                                }
                                if (tc.isPopular) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ChessGreenPrimary.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "POPULAR", color = ChessGreenPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. PLAY BOTS / COMPUTER MODAL
    if (isBotSelectorOpen) {
        Dialog(onDismissRequest = { isBotSelectorOpen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF262421))
                    .border(1.dp, Color(0xFF45423E), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Play vs Computer",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "✕",
                            color = ChessTextMuted,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { isBotSelectorOpen = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Color Chooser
                    Text(text = "Choose Your Side", color = ChessTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("♔ White", PieceColor.WHITE, Color.White),
                            Triple("⯪ Random", PieceColor.WHITE, ChessYellowAccent),
                            Triple("♚ Black", PieceColor.BLACK, Color(0xFFB0B0B0))
                        ).forEach { (label, col, txtCol) ->
                            val isSelected = selectedColor == col
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF383531) else Color(0xFF1E1C1A))
                                    .border(1.dp, if (isSelected) ChessGreenPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedColor = col }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = label, color = txtCol, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bot List
                    Text(text = "Select Bot Personality", color = ChessTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BotPersonality.ALL_BOTS.forEach { bot ->
                            val isSelected = selectedBot.id == bot.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF383531) else Color(0xFF1E1C1A))
                                    .border(1.dp, if (isSelected) ChessGreenPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedBot = bot }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = bot.avatarEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = "${bot.name} (${bot.elo})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = bot.title, color = ChessTextMuted, fontSize = 10.sp)
                                    }
                                }
                                if (isSelected) {
                                    Text(text = "SELECTED", color = ChessGreenPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start Game vs Bot Button
                    Button(
                        onClick = {
                            isBotSelectorOpen = false
                            onStartGame(
                                GameMode.VS_BOT,
                                selectedBot,
                                selectedTimeControl.minutes,
                                selectedTimeControl.incrementSec,
                                selectedColor
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChessGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(text = "Play vs ${selectedBot.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // 4. PLAY A FRIEND / PASS & PLAY MODAL
    if (isPlayFriendOpen) {
        Dialog(onDismissRequest = { isPlayFriendOpen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF262421))
                    .border(1.dp, Color(0xFF45423E), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Play a Friend",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "✕",
                            color = ChessTextMuted,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { isPlayFriendOpen = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "ONLINE FRIENDS", color = ChessTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple("Alex_GM", 2150, "🦁"),
                            Triple("Chess_Queen", 1840, "👑"),
                            Triple("Magnus_Fan", 1620, "⚡")
                        ).forEach { (friendName, elo, emoji) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1C1A))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = emoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = friendName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "$elo Elo • Online 🟢", color = ChessGreenPrimary, fontSize = 10.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        isPlayFriendOpen = false
                                        onStartGame(
                                            GameMode.ONLINE,
                                            BotPersonality.ALL_BOTS[1],
                                            selectedTimeControl.minutes,
                                            selectedTimeControl.incrementSec,
                                            PieceColor.WHITE
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ChessGreenPrimary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(text = "Challenge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pass & Play (Over the Board) Button
                    Button(
                        onClick = {
                            isPlayFriendOpen = false
                            onStartGame(
                                GameMode.PASS_AND_PLAY,
                                BotPersonality.ALL_BOTS[0],
                                15,
                                0,
                                PieceColor.WHITE
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF383531)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(text = "🤝 Pass & Play (Same Device)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
