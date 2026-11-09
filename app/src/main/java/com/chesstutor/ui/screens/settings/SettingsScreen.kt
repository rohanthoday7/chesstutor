package com.chesstutor.ui.screens.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.chesstutor.ui.theme.ChessCardDark
import com.chesstutor.ui.theme.ChessDarkBg
import com.chesstutor.ui.theme.ChessGreenPrimary
import com.chesstutor.ui.theme.ChessTextMuted
import com.chesstutor.ui.theme.ChessTextPrimary
import com.chesstutor.ui.theme.ChessTextSecondary

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTheme by remember { mutableStateOf(BoardTheme.GREEN) }
    var soundEnabled by remember { mutableStateOf(true) }
    var autoPromoteQueen by remember { mutableStateOf(true) }
    var showLegalDots by remember { mutableStateOf(true) }
    var showCoordinates by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChessDarkBg)
            .padding(16.dp)
    ) {
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
                text = "Settings",
                color = ChessTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(1.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Board Themes
        Text(text = "Board Theme", color = ChessTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BoardTheme.entries.forEach { theme ->
                val isSelected = theme == selectedTheme
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ChessGreenPrimary else ChessCardDark)
                        .clickable { selectedTheme = theme }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = theme.displayName,
                        color = Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preference Toggles
        Text(text = "Gameplay Preferences", color = ChessTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChessCardDark)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingToggle(
                    title = "Sound Effects",
                    subtitle = "Play move, capture and check sounds",
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
                SettingToggle(
                    title = "Auto-Promote to Queen",
                    subtitle = "Automatically select Queen upon pawn reaching 8th rank",
                    checked = autoPromoteQueen,
                    onCheckedChange = { autoPromoteQueen = it }
                )
                SettingToggle(
                    title = "Show Legal Move Hints",
                    subtitle = "Display dots and capture rings for selected piece",
                    checked = showLegalDots,
                    onCheckedChange = { showLegalDots = it }
                )
                SettingToggle(
                    title = "Show Coordinates",
                    subtitle = "Show rank (1-8) and file (a-h) along the board",
                    checked = showCoordinates,
                    onCheckedChange = { showCoordinates = it }
                )
            }
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = ChessTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, color = ChessTextSecondary, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ChessGreenPrimary,
                uncheckedThumbColor = Color(0xFFC0C0C0),
                uncheckedTrackColor = Color(0xFF383531)
            )
        )
    }
}

