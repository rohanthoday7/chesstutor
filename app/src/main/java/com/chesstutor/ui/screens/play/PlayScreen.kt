package com.chesstutor.ui.screens.play

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.data.local.BoardTheme
import com.chesstutor.ui.components.ChessboardView
import com.chesstutor.ui.components.EvaluationBar
import com.chesstutor.ui.components.PlayerHeaderView
import com.chesstutor.ui.components.PromotionDialog
import com.chesstutor.ui.theme.ChessCardDark
import com.chesstutor.ui.theme.ChessDarkBg
import com.chesstutor.ui.theme.ChessGreenPrimary
import com.chesstutor.ui.theme.ChessTextMuted
import com.chesstutor.ui.theme.ChessTextPrimary
import com.chesstutor.ui.theme.ChessTextSecondary
import com.chesstutor.ui.theme.ChessYellowAccent
import com.chesstutor.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var isOptionsMenuOpen by remember { mutableStateOf(false) }
    var isCoachDoubtSheetOpen by remember { mutableStateOf(false) }
    var doubtCustomInput by remember { mutableStateOf("") }

    val topPlayerName = if (state.playerColor == PieceColor.WHITE) state.opponentName else "You"
    val topPlayerRating = if (state.playerColor == PieceColor.WHITE) state.opponentRating else 1420
    val topPlayerAvatar = if (state.playerColor == PieceColor.WHITE) state.opponentAvatar else "♟️"
    val topPlayerTime = if (state.playerColor == PieceColor.WHITE) state.blackTimeMs else state.whiteTimeMs
    val isTopActive = if (state.playerColor == PieceColor.WHITE) state.gameState.turn == PieceColor.BLACK else state.gameState.turn == PieceColor.WHITE
    val topCaptured = if (state.playerColor == PieceColor.WHITE) state.capturedByBlack else state.capturedByWhite

    val bottomPlayerName = if (state.playerColor == PieceColor.WHITE) "You" else state.opponentName
    val bottomPlayerRating = if (state.playerColor == PieceColor.WHITE) 1420 else state.opponentRating
    val bottomPlayerAvatar = if (state.playerColor == PieceColor.WHITE) "♟️" else state.opponentAvatar
    val bottomPlayerTime = if (state.playerColor == PieceColor.WHITE) state.whiteTimeMs else state.blackTimeMs
    val isBottomActive = if (state.playerColor == PieceColor.WHITE) state.gameState.turn == PieceColor.WHITE else state.gameState.turn == PieceColor.BLACK
    val bottomCaptured = if (state.playerColor == PieceColor.WHITE) state.capturedByWhite else state.capturedByBlack

    val inCheckSquare = if (com.chesstutor.core.chess.ChessRules.isInCheck(state.gameState.board, state.gameState.turn)) {
        state.gameState.board.findKing(state.gameState.turn)
    } else null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChessDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Back & Opening Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◀ Back",
                    color = ChessTextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateBack() }
                )
                Text(
                    text = state.detectedOpening ?: "Live Game",
                    color = ChessTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (state.isBotThinking) "Thinking..." else "",
                    color = ChessYellowAccent,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Opponent Header
            PlayerHeaderView(
                playerName = topPlayerName,
                rating = topPlayerRating,
                avatarEmoji = topPlayerAvatar,
                timeRemainingMs = topPlayerTime,
                isActiveTurn = isTopActive,
                capturedPieces = topCaptured,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // AI Coach Banner (when Tutor Mode is active)
            if (state.isTutorModeActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF262421))
                        .border(1.dp, Color(0xFF383531), RoundedCornerShape(8.dp))
                        .clickable { isCoachDoubtSheetOpen = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = state.selectedTutor.avatarEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.selectedTutor.name,
                                color = ChessYellowAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ChessGreenPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(text = "TUTOR ON", color = ChessGreenPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = state.currentTutorFeedback?.coachMessage ?: state.selectedTutor.greeting,
                            color = ChessTextPrimary,
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Ask 🎓", color = ChessGreenPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Chessboard + Evaluation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EvaluationBar(
                    evaluationCentipawns = state.evaluationCentipawns,
                    isFlipped = state.isFlipped,
                    modifier = Modifier.fillMaxHeight(0.92f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                ChessboardView(
                    board = state.gameState.board,
                    isFlipped = state.isFlipped,
                    selectedSquare = state.selectedSquare,
                    legalMovesForSelected = state.legalMovesForSelected,
                    lastMove = state.gameState.moveHistory.lastOrNull()?.move,
                    inCheckSquare = inCheckSquare,
                    theme = BoardTheme.GREEN,
                    showCoordinates = true,
                    showLegalMoveDots = true,
                    onSquareClicked = { viewModel.onSquareClicked(it) },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Player Header
            PlayerHeaderView(
                playerName = bottomPlayerName,
                rating = bottomPlayerRating,
                avatarEmoji = bottomPlayerAvatar,
                timeRemainingMs = bottomPlayerTime,
                isActiveTurn = isBottomActive,
                capturedPieces = bottomCaptured,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ================= CHESS.COM AUTHENTIC BOTTOM CONTROLS =================

            // Move History Tape
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF262421))
                    .border(1.dp, Color(0xFF383531), RoundedCornerShape(6.dp))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.gameState.moveHistory.isEmpty()) {
                    Text(text = "Game started • White to move", color = ChessTextMuted, fontSize = 12.sp)
                } else {
                    state.gameState.moveHistory.forEachIndexed { i, record ->
                        if (i % 2 == 0) {
                            Text(
                                text = "${(i / 2) + 1}.",
                                color = Color(0xFF8B8987),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        val isLatestMove = i == state.gameState.moveHistory.size - 1
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isLatestMove) Color(0x3381B64C) else Color.Transparent)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = record.san,
                                color = if (isLatestMove) ChessGreenPrimary else Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isLatestMove) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // In-Game Tool Tray (Chess.com Signature Toolbar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF21201D))
                    .border(1.dp, Color(0xFF33302C), RoundedCornerShape(10.dp))
                    .navigationBarsPadding()
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game Options (☰)
                GameTrayIcon(icon = "☰", tooltip = "Options") {
                    isOptionsMenuOpen = true
                }

                // First Move (|◀)
                GameTrayIcon(icon = "⏮", tooltip = "First Move") {
                    // Navigate to start
                }

                // Previous Move (◀)
                GameTrayIcon(icon = "◀", tooltip = "Previous") {
                    // Navigate previous ply
                }

                // Next Move (▶)
                GameTrayIcon(icon = "▶", tooltip = "Next") {
                    // Navigate next ply
                }

                // Latest Move (▶|)
                GameTrayIcon(icon = "⏭", tooltip = "Live Move") {
                    // Navigate to live ply
                }

                // Coach Doubt / Advice (🎓)
                GameTrayIcon(icon = "🎓", tooltip = "Coach", isAccent = true) {
                    isCoachDoubtSheetOpen = true
                }

                // Coach Hint (💡)
                GameTrayIcon(icon = "💡", tooltip = "Hint", isAccent = false) {
                    viewModel.requestHint()
                }
            }
        }

        // Options Bottom Sheet
        if (isOptionsMenuOpen) {
            BasicAlertDialog(onDismissRequest = { isOptionsMenuOpen = false }) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF262421))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Game Menu",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        GameOptionItem(
                            icon = "🎓",
                            title = if (state.isTutorModeActive) "Disable Tutor Mode" else "Enable Tutor Mode"
                        ) {
                            viewModel.toggleTutorMode(!state.isTutorModeActive)
                            isOptionsMenuOpen = false
                        }

                        GameOptionItem(icon = "🔄", title = "Flip Board") {
                            viewModel.toggleFlipBoard()
                            isOptionsMenuOpen = false
                        }

                        GameOptionItem(icon = "🤝", title = "Offer Draw") {
                            isOptionsMenuOpen = false
                        }

                        GameOptionItem(icon = "🏳️", title = "Resign Game", isDestructive = true) {
                            viewModel.resign()
                            isOptionsMenuOpen = false
                        }

                        GameOptionItem(icon = "✕", title = "Close Menu") {
                            isOptionsMenuOpen = false
                        }
                    }
                }
            }
        }

        // Tutor Blunder Intervention Alert
        if (state.showTutorAlert && state.currentTutorFeedback != null) {
            val feedback = state.currentTutorFeedback!!
            BasicAlertDialog(onDismissRequest = { viewModel.dismissTutorAlert() }) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF262421))
                        .border(2.dp, Color(0xFFCA3431), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.selectedTutor.avatarEmoji, fontSize = 38.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${state.selectedTutor.name} Intervention!",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFCA3431))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = feedback.classification.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (feedback.patternIdentified != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pattern: ${feedback.patternIdentified}",
                                color = ChessYellowAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = feedback.coachMessage,
                            color = ChessTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        if (feedback.alternativeExplanation != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💡 Advice: ${feedback.alternativeExplanation}",
                                color = ChessTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.takeback() },
                                colors = ButtonDefaults.buttonColors(containerColor = ChessGreenPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Take Back ↺", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.dismissTutorAlert() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF383531)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Keep Move")
                            }
                        }
                    }
                }
            }
        }

        // Coach Doubt Resolution Dialog
        if (isCoachDoubtSheetOpen) {
            BasicAlertDialog(onDismissRequest = {
                isCoachDoubtSheetOpen = false
                viewModel.clearCoachDoubtResponse()
            }) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF262421))
                        .border(1.dp, Color(0xFF383531), RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = state.selectedTutor.avatarEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = state.selectedTutor.name,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = state.selectedTutor.title,
                                        color = ChessYellowAccent,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "✕",
                                color = ChessTextMuted,
                                fontSize = 16.sp,
                                modifier = Modifier.clickable {
                                    isCoachDoubtSheetOpen = false
                                    viewModel.clearCoachDoubtResponse()
                                }
                            )
                        }

                        // Switch Tutor Selector Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            com.chesstutor.core.tutor.TutorPersonality.ALL_TUTORS.forEach { t ->
                                val isChosen = t.id == state.selectedTutor.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isChosen) ChessGreenPrimary else Color(0xFF383531))
                                        .clickable { viewModel.selectTutor(t) }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${t.avatarEmoji} ${t.name.split(" ").last()}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ask Any Doubt:",
                            color = ChessTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Quick Doubt Chips
                        listOf(
                            "Why can't I castle?",
                            "What is en passant?",
                            "What's the best plan here?",
                            "How do pins and forks work?"
                        ).forEach { q ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF32302C))
                                    .clickable { viewModel.askCoachDoubt(q) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "❓ $q", color = Color(0xFFD4D2CF), fontSize = 11.sp)
                            }
                        }

                        // Coach Response Card
                        if (state.coachDoubtResponse != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E2619))
                                    .border(1.dp, ChessGreenPrimary, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "${state.selectedTutor.name} says:",
                                        color = ChessGreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = state.coachDoubtResponse!!,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pawn Promotion Modal Dialog
        if (state.pendingPromotionMove != null) {
            PromotionDialog(
                color = state.gameState.turn,
                onSelectPromotion = { viewModel.onPromotionSelected(it) },
                onDismiss = { viewModel.cancelPromotion() }
            )
        }

        // Game Over Modal Dialog
        if (state.gameState.status.isGameOver) {
            BasicAlertDialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChessCardDark)
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Game Over",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChessTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.gameState.status.description,
                            fontSize = 15.sp,
                            color = ChessYellowAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Estimated Accuracy: 88.4%",
                            fontSize = 13.sp,
                            color = ChessGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.startNewGame(state.gameMode, state.botPersonality) },
                                colors = ButtonDefaults.buttonColors(containerColor = ChessGreenPrimary)
                            ) {
                                Text(text = "Play Again", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onNavigateBack,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF383531))
                            ) {
                                Text(text = "Home")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameTrayIcon(
    icon: String,
    tooltip: String,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isAccent) Color(0x33F7EC59) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            color = if (isAccent) ChessYellowAccent else Color(0xFFC3C2C0),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GameOptionItem(
    icon: String,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF33302C))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = if (isDestructive) Color(0xFFE54B4B) else Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}
