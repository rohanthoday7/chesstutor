package com.chesstutor.ui.screens.tutor

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.chesstutor.core.tutor.MoveLesson
import com.chesstutor.core.tutor.TutorPersonality
import com.chesstutor.data.local.BoardTheme
import com.chesstutor.ui.components.ChessboardView
import com.chesstutor.ui.theme.ChessCardDark
import com.chesstutor.ui.theme.ChessDarkBg
import com.chesstutor.ui.theme.ChessGreenPrimary
import com.chesstutor.ui.theme.ChessTextMuted
import com.chesstutor.ui.theme.ChessTextPrimary
import com.chesstutor.ui.theme.ChessTextSecondary
import com.chesstutor.ui.theme.ChessYellowAccent
import com.chesstutor.viewmodel.TutorViewModel

@Composable
fun TutorScreen(
    viewModel: TutorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Move Lessons, 1: Learned Patterns, 2: Ask Tutor
    var userQuestionText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChessDarkBg)
            .padding(16.dp)
    ) {
        // Header with Tutor Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Chessable AI Coach",
                    color = ChessTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Personalized coaching, move masterclasses & doubt answering",
                    color = ChessTextMuted,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (state.isTutorModeActive) "Active" else "Off",
                    color = if (state.isTutorModeActive) ChessGreenPrimary else ChessTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Switch(
                    checked = state.isTutorModeActive,
                    onCheckedChange = { viewModel.toggleTutorMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ChessGreenPrimary,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF383531)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 1: Choose Different Tutors (Horizontal Carousel)
        Text(
            text = "Select Your Coach",
            color = ChessTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.allTutors) { tutor ->
                val isSelected = tutor.id == state.selectedTutor.id
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF2E3D24) else ChessCardDark)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) ChessGreenPrimary else Color(0xFF3B3835),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.selectTutor(tutor) }
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tutor.avatarEmoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF383531))
                                    .padding(4.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = tutor.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tutor.title,
                                    color = ChessYellowAccent,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tutor.bio,
                            color = ChessTextMuted,
                            fontSize = 10.sp,
                            maxLines = 3
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "✓ Selected Coach",
                                color = ChessGreenPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Tabs (Move Lessons, Pattern Intelligence, Doubt Q&A)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF262421))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Move Lessons", "Your Patterns", "Ask Doubt").forEachIndexed { index, tabName ->
                val isTabSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isTabSelected) ChessGreenPrimary else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabName,
                        color = if (isTabSelected) Color.White else ChessTextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB CONTENT
        when (selectedTab) {
            0 -> {
                // MOVE LESSONS MASTERCLASS
                MoveLessonsTab(
                    lessons = state.lessons,
                    activeLesson = state.activeLesson,
                    activeLessonState = state.activeLessonState,
                    onSelectLesson = { viewModel.selectLesson(it) }
                )
            }
            1 -> {
                // LEARNED PATTERNS INTELLIGENCE
                LearnedPatternsTab(
                    patterns = state.learnedPatterns,
                    activeTutor = state.selectedTutor
                )
            }
            2 -> {
                // ASK TUTOR ANYTHING (DOUBTS)
                DoubtResolutionTab(
                    chat = state.doubtChat,
                    activeTutor = state.selectedTutor,
                    inputText = userQuestionText,
                    onInputChanged = { userQuestionText = it },
                    onSendQuestion = { q ->
                        viewModel.askDoubt(q, state.activeLessonState)
                        userQuestionText = ""
                    }
                )
            }
        }
    }
}

@Composable
fun MoveLessonsTab(
    lessons: List<MoveLesson>,
    activeLesson: MoveLesson?,
    activeLessonState: com.chesstutor.core.chess.ChessGameState,
    onSelectLesson: (MoveLesson) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (activeLesson != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ChessCardDark)
                        .border(1.dp, Color(0xFF383531), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeLesson.title,
                                color = ChessYellowAccent,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF383531))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = activeLesson.category,
                                    color = ChessGreenPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = activeLesson.description,
                            color = ChessTextPrimary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Mini Demo Board
                        ChessboardView(
                            board = activeLessonState.board,
                            theme = BoardTheme.GREEN,
                            showCoordinates = true,
                            showLegalMoveDots = false,
                            onSquareClicked = {},
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E1D1A))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💡 Rule: ${activeLesson.rulesExplanation}",
                                color = Color(0xFFD4D2CF),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "All Move Masterclasses",
                color = ChessTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(lessons) { lesson ->
            val isCurrent = lesson.id == activeLesson?.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCurrent) Color(0xFF2E3D24) else ChessCardDark)
                    .border(
                        width = 1.dp,
                        color = if (isCurrent) ChessGreenPrimary else Color(0xFF353330),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectLesson(lesson) }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lesson.title,
                            color = if (isCurrent) ChessGreenPrimary else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = lesson.description,
                            color = ChessTextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    Text(text = "▶", color = ChessTextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun LearnedPatternsTab(
    patterns: List<com.chesstutor.viewmodel.LearnedPattern>,
    activeTutor: TutorPersonality
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF21252B))
                    .border(1.dp, Color(0xFF3E4451), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = activeTutor.avatarEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${activeTutor.name}'s Pattern Diagnosis",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Analyzing your recent games to correct repetitive blunders in Tutor Mode.",
                            color = ChessTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        items(patterns) { pattern ->
            val badgeColor = when (pattern.severity) {
                "High" -> Color(0xFFCA3431)
                "Medium" -> Color(0xFFE58F2A)
                else -> Color(0xFF81B64C)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChessCardDark)
                    .border(1.dp, Color(0xFF383531), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pattern.patternName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${pattern.occurrenceCount}x Detected",
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Issue: ${pattern.diagnosis}",
                        color = ChessTextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💡 Coach Advice: ${pattern.remedy}",
                        color = ChessYellowAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun DoubtResolutionTab(
    chat: List<com.chesstutor.viewmodel.TutorChatMessage>,
    activeTutor: TutorPersonality,
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSendQuestion: (String) -> Unit
) {
    val quickQuestions = listOf(
        "Why can't I castle?",
        "What is en passant?",
        "What's the best plan here?",
        "How do pins and forks work?",
        "When should I promote a pawn?"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Question Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickQuestions) { q ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2D2A26))
                        .border(1.dp, Color(0xFF45413C), RoundedCornerShape(16.dp))
                        .clickable { onSendQuestion(q) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = q, color = ChessTextPrimary, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Message Log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chat) { msg ->
                val isUser = msg.isUser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isUser) ChessGreenPrimary else Color(0xFF262421))
                            .border(
                                1.dp,
                                if (isUser) Color.Transparent else Color(0xFF383531),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.sender,
                                color = if (isUser) Color.White.copy(alpha = 0.8f) else ChessYellowAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field + Send Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = { Text(text = "Ask ${activeTutor.name} any chess doubt...", fontSize = 12.sp, color = ChessTextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChessGreenPrimary,
                    unfocusedBorderColor = Color(0xFF383531),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendQuestion(inputText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChessGreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Ask", fontWeight = FontWeight.Bold)
            }
        }
    }
}

