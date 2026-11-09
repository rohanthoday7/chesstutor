package com.chesstutor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chesstutor.core.chess.Board
import com.chesstutor.core.chess.Move
import com.chesstutor.core.chess.Piece
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.chess.Square
import com.chesstutor.data.local.BoardTheme
import com.chesstutor.ui.theme.ChessCheckGlow
import com.chesstutor.ui.theme.ChessMoveHighlight

@Composable
fun ChessboardView(
    board: Board,
    isFlipped: Boolean = false,
    selectedSquare: Square? = null,
    legalMovesForSelected: List<Move> = emptyList(),
    lastMove: Move? = null,
    inCheckSquare: Square? = null,
    theme: BoardTheme = BoardTheme.GREEN,
    showCoordinates: Boolean = true,
    showLegalMoveDots: Boolean = true,
    onSquareClicked: (Square) -> Unit,
    modifier: Modifier = Modifier
) {
    val lightSquareColor = Color(theme.lightHex)
    val darkSquareColor = Color(theme.darkHex)

    val legalDestinations = legalMovesForSelected.map { it.to }.toSet()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, Color(0xFF262421), RoundedCornerShape(8.dp))
    ) {
        val squareSize = maxWidth / 8

        Column(modifier = Modifier.fillMaxSize()) {
            val rankRange = if (isFlipped) 0..7 else 7 downTo 0
            val fileRange = if (isFlipped) 7 downTo 0 else 0..7

            for (rank in rankRange) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (file in fileRange) {
                        val square = Square(file, rank)
                        val piece = board[square]
                        val isLight = square.isLightSquare
                        val baseColor = if (isLight) lightSquareColor else darkSquareColor

                        val isSelected = square == selectedSquare
                        val isLastMove = lastMove != null && (square == lastMove.from || square == lastMove.to)
                        val isCheck = square == inCheckSquare
                        val isLegalTarget = legalDestinations.contains(square)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(baseColor)
                                .clickable { onSquareClicked(square) },
                            contentAlignment = Alignment.Center
                        ) {
                            // Last Move Highlight
                            if (isLastMove) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(ChessMoveHighlight)
                                )
                            }

                            // Selected Square Highlight
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0x77F7EC59))
                                )
                            }

                            // King in Check Glow
                            if (isCheck) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(ChessCheckGlow)
                                )
                            }

                            // Piece Rendering — outline + fill layers for visibility on any square color
                            if (piece != null) {
                                val pieceFontSize = (squareSize.value * 0.72f).sp
                                val pieceColor = if (piece.color == PieceColor.WHITE) Color.White else Color(0xFF1A1A1A)
                                val outlineColor = if (piece.color == PieceColor.WHITE) Color(0xFF2D2D2D) else Color(0xFFD8D8D8)
                                val offDp = (squareSize.value * 0.045f).coerceAtLeast(1f).dp

                                Box(contentAlignment = Alignment.Center) {
                                    // 4-directional outline strokes for crisp contrast on any background
                                    Text(text = piece.unicodeChar, fontSize = pieceFontSize, color = outlineColor,
                                        modifier = Modifier.offset(x = -offDp, y = 0.dp))
                                    Text(text = piece.unicodeChar, fontSize = pieceFontSize, color = outlineColor,
                                        modifier = Modifier.offset(x = offDp, y = 0.dp))
                                    Text(text = piece.unicodeChar, fontSize = pieceFontSize, color = outlineColor,
                                        modifier = Modifier.offset(x = 0.dp, y = -offDp))
                                    Text(text = piece.unicodeChar, fontSize = pieceFontSize, color = outlineColor,
                                        modifier = Modifier.offset(x = 0.dp, y = offDp))
                                    // Piece fill rendered last (on top)
                                    Text(text = piece.unicodeChar, fontSize = pieceFontSize, color = pieceColor)
                                }
                            }

                            // Legal Move Hint (Dot or Capture Ring)
                            if (showLegalMoveDots && isLegalTarget) {
                                if (piece != null) {
                                    // Target square has enemy piece: capture ring overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.9f)
                                            .border(4.dp, Color(0x66000000), CircleShape)
                                    )
                                } else {
                                    // Target square is empty: soft translucent filled circle
                                    Box(
                                        modifier = Modifier
                                            .size(squareSize * 0.32f)
                                            .background(Color(0x40000000), CircleShape)
                                    )
                                }
                            }

                            // Coordinates
                            if (showCoordinates) {
                                // Rank on leftmost file
                                val showRank = if (isFlipped) file == 7 else file == 0
                                if (showRank) {
                                    Text(
                                        text = "${rank + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLight) darkSquareColor else lightSquareColor,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                    )
                                }

                                // File on bottommost rank
                                val showFile = if (isFlipped) rank == 7 else rank == 0
                                if (showFile) {
                                    Text(
                                        text = "${('a' + file)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLight) darkSquareColor else lightSquareColor,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

