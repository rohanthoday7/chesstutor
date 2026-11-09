package com.chesstutor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chesstutor.ui.theme.ChessCardElevated
import com.chesstutor.ui.theme.ChessGreenPrimary
import com.chesstutor.ui.theme.ChessTextMuted

enum class NavSection(val label: String) {
    PLAY("Play"),
    PUZZLES("Puzzles"),
    LEARN("Learn"),
    SOCIAL("Social"),
    PROFILE("More")
}

// Custom Chess.com Vector Icons
object ChessNavIcons {
    // Chess Pawn Icon for Play
    val Pawn: ImageVector = ImageVector.Builder(
        name = "ChessPawn",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2f)
            curveTo(9.79f, 2f, 8f, 3.79f, 8f, 6f)
            curveTo(8f, 7.5f, 8.8f, 8.8f, 10f, 9.5f)
            verticalLineTo(11f)
            curveTo(7.8f, 11f, 6f, 12.8f, 6f, 15f)
            verticalLineTo(16f)
            horizontalLineTo(18f)
            verticalLineTo(15f)
            curveTo(18f, 12.8f, 16.2f, 11f, 14f, 11f)
            verticalLineTo(9.5f)
            curveTo(15.2f, 8.8f, 16f, 7.5f, 16f, 6f)
            curveTo(16f, 3.79f, 14.21f, 2f, 12f, 2f)
            close()
            moveTo(5f, 18f)
            verticalLineTo(20f)
            horizontalLineTo(19f)
            verticalLineTo(18f)
            horizontalLineTo(5f)
            close()
        }
    }.build()

    // Jigsaw Puzzle Icon
    val Puzzle: ImageVector = ImageVector.Builder(
        name = "ChessPuzzle",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(20.5f, 11f)
            horizontalLineTo(19f)
            verticalLineTo(7f)
            curveTo(19f, 5.9f, 18.1f, 5f, 17f, 5f)
            horizontalLineTo(13f)
            verticalLineTo(3.5f)
            curveTo(13f, 2.12f, 11.88f, 1f, 10.5f, 1f)
            curveTo(9.12f, 1f, 8f, 2.12f, 8f, 3.5f)
            verticalLineTo(5f)
            horizontalLineTo(4f)
            curveTo(2.9f, 5f, 2f, 5.9f, 2f, 7f)
            verticalLineTo(11f)
            horizontalLineTo(3.5f)
            curveTo(4.88f, 11f, 6f, 12.12f, 6f, 13.5f)
            curveTo(6f, 14.88f, 4.88f, 16f, 3.5f, 16f)
            horizontalLineTo(2f)
            verticalLineTo(20f)
            curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
            horizontalLineTo(8f)
            verticalLineTo(20.5f)
            curveTo(8f, 19.12f, 9.12f, 18f, 10.5f, 18f)
            curveTo(11.88f, 18f, 13f, 19.12f, 13f, 20.5f)
            verticalLineTo(22f)
            horizontalLineTo(17f)
            curveTo(18.1f, 22f, 19f, 21.1f, 19f, 20f)
            verticalLineTo(16f)
            horizontalLineTo(20.5f)
            curveTo(21.88f, 16f, 23f, 14.88f, 23f, 13.5f)
            curveTo(23f, 12.12f, 21.88f, 11f, 20.5f, 11f)
            close()
        }
    }.build()

    // Graduation Cap / Learn Icon
    val Learn: ImageVector = ImageVector.Builder(
        name = "ChessLearn",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 3f)
            lineTo(1f, 9f)
            lineTo(12f, 15f)
            lineTo(21f, 10.09f)
            verticalLineTo(17f)
            horizontalLineTo(23f)
            verticalLineTo(9f)
            lineTo(12f, 3f)
            close()
            moveTo(5f, 13.18f)
            verticalLineTo(17.18f)
            curveTo(5f, 19.39f, 8.13f, 21.18f, 12f, 21.18f)
            curveTo(15.87f, 21.18f, 19f, 19.39f, 19f, 17.18f)
            verticalLineTo(13.18f)
            lineTo(12f, 17f)
            lineTo(5f, 13.18f)
            close()
        }
    }.build()

    // Intersecting Users / Friends Icon
    val Social: ImageVector = ImageVector.Builder(
        name = "ChessSocial",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(16.5f, 13f)
            curveTo(18.43f, 13f, 20f, 11.43f, 20f, 9.5f)
            curveTo(20f, 7.57f, 18.43f, 6f, 16.5f, 6f)
            curveTo(14.57f, 6f, 13f, 7.57f, 13f, 9.5f)
            curveTo(13f, 11.43f, 14.57f, 13f, 16.5f, 13f)
            close()
            moveTo(9f, 11f)
            curveTo(11.21f, 11f, 13f, 9.21f, 13f, 7f)
            curveTo(13f, 4.79f, 11.21f, 3f, 9f, 3f)
            curveTo(6.79f, 3f, 5f, 4.79f, 5f, 7f)
            curveTo(5f, 9.21f, 6.79f, 11f, 9f, 11f)
            close()
            moveTo(9f, 13f)
            curveTo(6.33f, 13f, 1f, 14.34f, 1f, 17f)
            verticalLineTo(20f)
            horizontalLineTo(17f)
            verticalLineTo(17f)
            curveTo(17f, 14.34f, 11.67f, 13f, 9f, 13f)
            close()
            moveTo(16.5f, 14.5f)
            curveTo(16.03f, 14.5f, 15.54f, 14.55f, 15.04f, 14.65f)
            curveTo(16.24f, 15.48f, 17f, 16.64f, 17f, 18f)
            verticalLineTo(20f)
            horizontalLineTo(23f)
            verticalLineTo(18f)
            curveTo(23f, 15.7f, 19.3f, 14.5f, 16.5f, 14.5f)
            close()
        }
    }.build()

    // 3-Bar / More Menu Icon
    val More: ImageVector = ImageVector.Builder(
        name = "ChessMore",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(3f, 18f)
            horizontalLineTo(21f)
            verticalLineTo(16f)
            horizontalLineTo(3f)
            verticalLineTo(18f)
            close()
            moveTo(3f, 13f)
            horizontalLineTo(21f)
            verticalLineTo(11f)
            horizontalLineTo(3f)
            verticalLineTo(13f)
            close()
            moveTo(3f, 6f)
            verticalLineTo(8f)
            horizontalLineTo(21f)
            verticalLineTo(6f)
            horizontalLineTo(3f)
            close()
        }
    }.build()
}

@Composable
fun ChessBottomNavigationBar(
    currentSection: NavSection,
    onSectionSelected: (NavSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1C1A)) // Deep authentic Chess.com navigation bar background
            .navigationBarsPadding()
            .height(60.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavSection.entries.forEach { section ->
            val isSelected = section == currentSection
            val iconVector = when (section) {
                NavSection.PLAY -> ChessNavIcons.Pawn
                NavSection.PUZZLES -> ChessNavIcons.Puzzle
                NavSection.LEARN -> ChessNavIcons.Learn
                NavSection.SOCIAL -> ChessNavIcons.Social
                NavSection.PROFILE -> ChessNavIcons.More
            }

            val iconColor = if (isSelected) ChessGreenPrimary else Color(0xFF8B8987)
            val textColor = if (isSelected) ChessGreenPrimary else Color(0xFF8B8987)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSectionSelected(section) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon with subtle top dot when active (Chess.com signature)
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = section.label,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = section.label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}
