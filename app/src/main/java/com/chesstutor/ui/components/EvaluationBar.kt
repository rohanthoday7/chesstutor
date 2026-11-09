package com.chesstutor.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

@Composable
fun EvaluationBar(
    evaluationCentipawns: Int,
    isFlipped: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Clamped evaluation: range from -1000 to +1000 (+10.0 to -10.0 pawns)
    val clamped = evaluationCentipawns.coerceIn(-1000, 1000)
    // Percentage for White: 0.5 is equal (0 centipawns), 1.0 is +10, 0.0 is -10
    val targetWhitePercentage = (clamped + 1000f) / 2000f
    val animatedWhiteHeight by animateFloatAsState(targetValue = targetWhitePercentage, label = "eval_anim")

    val whitePercentage = if (isFlipped) 1f - animatedWhiteHeight else animatedWhiteHeight
    val evalText = if (clamped.absoluteValue >= 100000) {
        "M"
    } else {
        val pawns = evaluationCentipawns / 100.0
        if (pawns > 0) "+%.1f".format(pawns) else "%.1f".format(pawns)
    }

    Box(
        modifier = modifier
            .width(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF262421))
    ) {
        Column(modifier = Modifier.matchParentSize()) {
            // Black portion (top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(1f - whitePercentage)
                    .background(Color(0xFF3B3835))
            )
            // White portion (bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(1f)
                    .background(Color(0xFFE8E8E8))
            )
        }

        // Eval text overlay
        Text(
            text = evalText,
            color = if (whitePercentage > 0.5f) Color(0xFF262421) else Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(if (whitePercentage > 0.5f) Alignment.BottomCenter else Alignment.TopCenter)
                .padding(vertical = 4.dp)
        )
    }
}

