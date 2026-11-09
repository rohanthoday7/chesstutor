package com.chesstutor.ui.screens.puzzles

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.chesstutor.data.local.BoardTheme
import com.chesstutor.ui.components.ChessboardView
import com.chesstutor.ui.components.PromotionDialog
import com.chesstutor.ui.theme.ChessCardDark
import com.chesstutor.ui.theme.ChessDarkBg
import com.chesstutor.ui.theme.ChessGreenPrimary
import com.chesstutor.ui.theme.ChessTextMuted
import com.chesstutor.ui.theme.ChessTextPrimary
import com.chesstutor.ui.theme.ChessYellowAccent
import com.chesstutor.viewmodel.PuzzleMode
import com.chesstutor.viewmodel.PuzzleViewModel

@Composable
fun PuzzlesScreen(
    viewModel: PuzzleViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChessDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector: Daily vs Puzzle Rush
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ChessCardDark)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (state.mode == PuzzleMode.DAILY) ChessGreenPrimary else Color.Transparent)
                    .clickable { viewModel.loadDailyPuzzle() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Daily Puzzle",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (state.mode == PuzzleMode.PUZZLE_RUSH) Color(0xFFE58F2A) else Color.Transparent)
                    .clickable { viewModel.startPuzzleRush() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔥 Puzzle Rush",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Puzzle Header / Status Banner
        if (state.mode == PuzzleMode.PUZZLE_RUSH) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChessCardDark)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score: ${state.rushScore}",
                    color = ChessYellowAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Time: ${state.rushTimeRemainingSec / 60}:%02d".format(state.rushTimeRemainingSec % 60),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Strikes: ${"❌".repeat(state.rushStrikes)}${"⚪".repeat(3 - state.rushStrikes)}",
                    fontSize = 14.sp
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.title,
                        color = ChessTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rating: ${state.rating} • ${state.description}",
                        color = ChessTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Chessboard
        ChessboardView(
            board = state.gameState.board,
            selectedSquare = state.selectedSquare,
            legalMovesForSelected = state.legalMovesForSelected,
            lastMove = state.gameState.moveHistory.lastOrNull()?.move,
            theme = BoardTheme.GREEN,
            showCoordinates = true,
            showLegalMoveDots = true,
            onSquareClicked = { viewModel.onSquareClicked(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Result Banners
        if (state.isSolved) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChessGreenPrimary)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎉 Excellent! Puzzle Solved!",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (state.isFailed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFCA3431))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "❌ That's not the best move. Try again!",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset or Reload Button
        Button(
            onClick = {
                if (state.mode == PuzzleMode.PUZZLE_RUSH) viewModel.startPuzzleRush()
                else viewModel.loadDailyPuzzle()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF383531)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (state.mode == PuzzleMode.PUZZLE_RUSH) "Restart Puzzle Rush" else "New Puzzle")
        }

        // Promotion Dialog
        if (state.pendingPromotionMove != null) {
            PromotionDialog(
                color = state.gameState.turn,
                onSelectPromotion = { viewModel.onPromotionSelected(it) },
                onDismiss = {}
            )
        }
    }
}

