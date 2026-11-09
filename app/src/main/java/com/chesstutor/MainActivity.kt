package com.chesstutor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.chesstutor.ui.components.ChessBottomNavigationBar
import com.chesstutor.ui.components.NavSection
import com.chesstutor.ui.screens.auth.AuthScreen
import com.chesstutor.ui.screens.home.HomeScreen
import com.chesstutor.ui.screens.learn.LearnScreen
import com.chesstutor.ui.screens.play.PlayScreen
import com.chesstutor.ui.screens.profile.ProfileScreen
import com.chesstutor.ui.screens.puzzles.PuzzlesScreen
import com.chesstutor.ui.screens.social.SocialScreen
import com.chesstutor.ui.theme.ChessDarkBg
import com.chesstutor.ui.theme.ChessTutorTheme
import com.chesstutor.viewmodel.AuthViewModel
import com.chesstutor.viewmodel.GameViewModel
import com.chesstutor.viewmodel.LearnViewModel
import com.chesstutor.viewmodel.ProfileViewModel
import com.chesstutor.viewmodel.PuzzleViewModel
import com.chesstutor.viewmodel.SocialViewModel
import com.chesstutor.viewmodel.TutorViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()
    private val puzzleViewModel: PuzzleViewModel by viewModels()
    private val learnViewModel: LearnViewModel by viewModels()
    private val tutorViewModel: TutorViewModel by viewModels()
    private val socialViewModel: SocialViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessTutorTheme {
                val authState by authViewModel.uiState.collectAsState()

                if (!authState.session.isLoggedIn) {
                    AuthScreen(
                        viewModel = authViewModel,
                        onAuthSuccess = {}
                    )
                } else {
                    MainContent(
                        authViewModel = authViewModel,
                        gameViewModel = gameViewModel,
                        puzzleViewModel = puzzleViewModel,
                        learnViewModel = learnViewModel,
                        tutorViewModel = tutorViewModel,
                        socialViewModel = socialViewModel,
                        profileViewModel = profileViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MainContent(
    authViewModel: AuthViewModel,
    gameViewModel: GameViewModel,
    puzzleViewModel: PuzzleViewModel,
    learnViewModel: LearnViewModel,
    tutorViewModel: TutorViewModel,
    socialViewModel: SocialViewModel,
    profileViewModel: ProfileViewModel
) {
    var currentSection by remember { mutableStateOf(NavSection.PLAY) }
    var isInActiveGame by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (!isInActiveGame) {
                ChessBottomNavigationBar(
                    currentSection = currentSection,
                    onSectionSelected = { currentSection = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChessDarkBg)
                .padding(innerPadding)
        ) {
            if (isInActiveGame) {
                PlayScreen(
                    viewModel = gameViewModel,
                    onNavigateBack = { isInActiveGame = false }
                )
            } else {
                when (currentSection) {
                    NavSection.PLAY -> HomeScreen(
                        onStartGame = { mode, bot, time, inc, color ->
                            gameViewModel.startNewGame(mode, bot, time, inc, color)
                            isInActiveGame = true
                        },
                        onNavigateSection = { currentSection = it }
                    )
                    NavSection.PUZZLES -> PuzzlesScreen(viewModel = puzzleViewModel)
                    NavSection.LEARN -> LearnScreen(viewModel = learnViewModel, tutorViewModel = tutorViewModel)
                    NavSection.SOCIAL -> SocialScreen(viewModel = socialViewModel)
                    NavSection.PROFILE -> ProfileScreen(viewModel = profileViewModel)
                }
            }
        }
    }
}
