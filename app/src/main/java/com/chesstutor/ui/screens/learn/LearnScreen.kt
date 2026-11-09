package com.chesstutor.ui.screens.learn

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chesstutor.data.local.BoardTheme
import com.chesstutor.ui.components.ChessboardView
import com.chesstutor.ui.screens.tutor.TutorScreen
import com.chesstutor.ui.theme.ChessCardDark
import com.chesstutor.ui.theme.ChessDarkBg
import com.chesstutor.ui.theme.ChessGreenPrimary
import com.chesstutor.ui.theme.ChessTextMuted
import com.chesstutor.ui.theme.ChessTextPrimary
import com.chesstutor.ui.theme.ChessTextSecondary
import com.chesstutor.ui.theme.ChessYellowAccent
import com.chesstutor.viewmodel.LearnViewModel
import com.chesstutor.viewmodel.TutorViewModel

@Composable
fun LearnScreen(
    viewModel: LearnViewModel,
    tutorViewModel: TutorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val opening = state.selectedOpening
    var selectedTopTab by remember { mutableStateOf(1) } // 0: Openings, 1: AI Tutor

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChessDarkBg)
            .padding(16.dp)
    ) {
        // Top Switcher: Openings vs AI Tutor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF262421))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedTopTab == 1) ChessGreenPrimary else Color.Transparent)
                    .clickable { selectedTopTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎓 AI Tutor Agent",
                    color = if (selectedTopTab == 1) Color.White else ChessTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedTopTab == 0) ChessGreenPrimary else Color.Transparent)
                    .clickable { selectedTopTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📖 Openings Theory",
                    color = if (selectedTopTab == 0) Color.White else ChessTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTopTab == 1) {
            TutorScreen(viewModel = tutorViewModel)
        } else {
            // Openings Explorer Content
            Text(
                text = "Opening Explorer & Theory",
                color = ChessTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Study master moves, ECO theory and typical tactical ideas",
                color = ChessTextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Opening Info Header
            if (opening != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ChessCardDark)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${opening.eco} : ${opening.name}",
                                color = ChessYellowAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Move ${state.currentPlyIndex}/${opening.movesSan.size}",
                                color = ChessTextMuted,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = opening.description,
                            color = ChessTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chessboard
            ChessboardView(
                board = state.gameState.board,
                lastMove = state.gameState.moveHistory.lastOrNull()?.move,
                theme = BoardTheme.GREEN,
                showCoordinates = true,
                showLegalMoveDots = false,
                onSquareClicked = {},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Playback Controls: Previous, Reset, Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.previousMove() },
                    enabled = state.currentPlyIndex > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF383531)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "◀ Prev")
                }
                Button(
                    onClick = { viewModel.resetBoard() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF383531)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Reset")
                }
                Button(
                    onClick = { viewModel.nextMove() },
                    enabled = opening != null && state.currentPlyIndex < opening.movesSan.size,
                    colors = ButtonDefaults.buttonColors(containerColor = ChessGreenPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Next ▶")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Popular Openings Library",
                color = ChessTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Openings list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.openingsList) { op ->
                    val isSelected = op.eco == opening?.eco
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF3B3835) else ChessCardDark)
                            .clickable { viewModel.selectOpening(op) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${op.eco} - ${op.name}",
                                    color = if (isSelected) ChessGreenPrimary else ChessTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = op.movesSan.joinToString(" "),
                                    color = ChessTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(text = "▶", color = ChessTextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

