package com.chesstutor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chesstutor.core.chess.Piece
import com.chesstutor.ui.theme.ChessCardDark
import com.chesstutor.ui.theme.ChessGreenPrimary
import com.chesstutor.ui.theme.ChessTextMuted
import com.chesstutor.ui.theme.ChessTextPrimary

@Composable
fun PlayerHeaderView(
    playerName: String,
    rating: Int,
    avatarEmoji: String,
    timeRemainingMs: Long,
    isActiveTurn: Boolean,
    capturedPieces: List<Piece>,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (timeRemainingMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val isLowTime = totalSeconds < 20

    val timeString = if (minutes > 0) {
        "%d:%02d".format(minutes, seconds)
    } else {
        val tenths = (timeRemainingMs % 1000) / 100
        "%02d.%d".format(seconds, tenths)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ChessCardDark)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Player Avatar + Name + Rating + Material
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF383531)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = avatarEmoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = playerName,
                    color = ChessTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($rating)",
                    color = ChessTextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Captured pieces preview
            Row {
                capturedPieces.takeLast(4).forEach { piece ->
                    Text(text = piece.unicodeChar, fontSize = 14.sp, color = Color(0xFFC0C0C0))
                }
            }
        }

        // Digital Clock
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isActiveTurn) Color(0xFF3B3835) else Color(0xFF1E1C1A))
                .then(
                    if (isActiveTurn) Modifier.border(1.5.dp, if (isLowTime) Color(0xFFE54B4B) else ChessGreenPrimary, RoundedCornerShape(6.dp))
                    else Modifier
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = timeString,
                color = if (isLowTime) Color(0xFFE54B4B) else if (isActiveTurn) Color.White else ChessTextMuted,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            )
        }
    }
}

