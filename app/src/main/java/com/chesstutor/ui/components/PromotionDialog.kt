package com.chesstutor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chesstutor.core.chess.Piece
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.chess.PieceType
import com.chesstutor.ui.theme.ChessCardDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionDialog(
    color: PieceColor,
    onSelectPromotion: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    val choices = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(ChessCardDark)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                choices.forEach { pieceType ->
                    val piece = Piece(pieceType, color)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF383531))
                            .clickable { onSelectPromotion(pieceType) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = piece.unicodeChar,
                            fontSize = 38.sp,
                            color = if (color == PieceColor.WHITE) Color.White else Color(0xFFC0C0C0)
                        )
                    }
                }
            }
        }
    }
}

