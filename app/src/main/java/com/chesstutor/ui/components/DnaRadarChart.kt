package com.chesstutor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.chesstutor.core.dna.DnaRadarMetrics
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DnaRadarChart(
    metrics: DnaRadarMetrics,
    modifier: Modifier = Modifier
) {
    val labels = listOf("Tactics", "Opening", "Endgame", "Attack", "Time", "Resilience")
    val values = listOf(
        metrics.tactics,
        metrics.opening,
        metrics.endgame,
        metrics.aggressiveness,
        metrics.timeManagement,
        metrics.resilience
    )

    Box(modifier = modifier.size(240.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) * 0.72f
            val sides = 6
            val angleStep = (2 * Math.PI / sides).toFloat()

            // Draw concentric web rings (25%, 50%, 75%, 100%)
            val ringLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
            for (level in ringLevels) {
                val ringRadius = radius * level
                val ringPath = Path()
                for (i in 0 until sides) {
                    val angle = i * angleStep - (Math.PI / 2).toFloat()
                    val x = center.x + ringRadius * cos(angle)
                    val y = center.y + ringRadius * sin(angle)
                    if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
                }
                ringPath.close()
                drawPath(ringPath, Color(0xFF45423E), style = Stroke(width = 1.dp.toPx()))
            }

            // Draw radial axis spokes
            for (i in 0 until sides) {
                val angle = i * angleStep - (Math.PI / 2).toFloat()
                val x = center.x + radius * cos(angle)
                val y = center.y + radius * sin(angle)
                drawLine(Color(0xFF45423E), center, Offset(x, y), strokeWidth = 1.dp.toPx())
            }

            // Draw filled data polygon
            val dataPath = Path()
            val points = mutableListOf<Offset>()
            for (i in 0 until sides) {
                val normalized = (values[i] / 100f).coerceIn(0.1f, 1.0f)
                val currentRadius = radius * normalized
                val angle = i * angleStep - (Math.PI / 2).toFloat()
                val x = center.x + currentRadius * cos(angle)
                val y = center.y + currentRadius * sin(angle)
                val pt = Offset(x, y)
                points.add(pt)
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            // Translucent Green Fill
            drawPath(dataPath, Color(0x6681B64C), style = Fill)
            // Crisp Green Outline
            drawPath(dataPath, Color(0xFF81B64C), style = Stroke(width = 2.5.dp.toPx()))

            // Draw glowing points on vertices
            for (pt in points) {
                drawCircle(Color.White, radius = 4.dp.toPx(), center = pt)
                drawCircle(Color(0xFF81B64C), radius = 2.5.dp.toPx(), center = pt)
            }

            // Draw labels around chart
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            for (i in 0 until sides) {
                val angle = i * angleStep - (Math.PI / 2).toFloat()
                val labelDistance = radius + 22.dp.toPx()
                val x = center.x + labelDistance * cos(angle)
                val y = center.y + labelDistance * sin(angle) + 8f

                drawContext.canvas.nativeCanvas.drawText(
                    "${labels[i]} ${values[i]}",
                    x,
                    y,
                    textPaint
                )
            }
        }
    }
}

