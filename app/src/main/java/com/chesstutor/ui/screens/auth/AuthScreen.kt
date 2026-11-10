package com.chesstutor.ui.screens.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chesstutor.ui.theme.ChessCardDark
import com.chesstutor.ui.theme.ChessDarkBg
import com.chesstutor.ui.theme.ChessGreenPrimary
import com.chesstutor.ui.theme.ChessTextMuted
import com.chesstutor.ui.theme.ChessTextPrimary
import com.chesstutor.ui.theme.ChessTextSecondary
import com.chesstutor.ui.theme.ChessYellowAccent
import com.chesstutor.viewmodel.AuthMode
import com.chesstutor.viewmodel.AuthViewModel
import com.chesstutor.viewmodel.SkillLevel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChessDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo & Title
        Text(text = "♟️", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Chessable",
            color = ChessTextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (state.authMode == AuthMode.REGISTER) "Create Your Free Chess Account" else "Welcome Back to Chessable",
            color = ChessGreenPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mode Switcher: Create Account vs Log In
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
                    .background(if (state.authMode == AuthMode.REGISTER) ChessGreenPrimary else Color.Transparent)
                    .clickable { viewModel.setAuthMode(AuthMode.REGISTER) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create Account",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (state.authMode == AuthMode.LOGIN) ChessGreenPrimary else Color.Transparent)
                    .clickable { viewModel.setAuthMode(AuthMode.LOGIN) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log In",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card containing input fields
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChessCardDark)
                .border(1.dp, Color(0xFF383531), RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Username field
                OutlinedTextField(
                    value = state.usernameInput,
                    onValueChange = { viewModel.onUsernameChanged(it) },
                    label = { Text("Username") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChessGreenPrimary,
                        unfocusedBorderColor = Color(0xFF45423E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ChessGreenPrimary,
                        unfocusedLabelColor = ChessTextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Email field (Only on registration)
                if (state.authMode == AuthMode.REGISTER) {
                    OutlinedTextField(
                        value = state.emailInput,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChessGreenPrimary,
                            unfocusedBorderColor = Color(0xFF45423E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = ChessGreenPrimary,
                            unfocusedLabelColor = ChessTextMuted
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Skill Level Selection
                    Text(
                        text = "Skill Level",
                        color = ChessTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SkillLevel.entries.forEach { skill ->
                            val isSelected = skill == state.selectedSkill
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFF383531) else Color(0xFF1E1C1A))
                                    .border(1.dp, if (isSelected) ChessGreenPrimary else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.onSkillSelected(skill) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = skill.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = skill.description, color = ChessTextMuted, fontSize = 10.sp)
                                }
                                Text(text = "${skill.rating} Elo", color = ChessYellowAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Password field
                OutlinedTextField(
                    value = state.passwordInput,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChessGreenPrimary,
                        unfocusedBorderColor = Color(0xFF45423E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ChessGreenPrimary,
                        unfocusedLabelColor = ChessTextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error Message if any
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage!!,
                        color = Color(0xFFE54B4B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Submit Button
                Button(
                    onClick = { viewModel.submit(onAuthSuccess) },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = ChessGreenPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = if (state.authMode == AuthMode.LOGIN) "Log In" else "Sign Up",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Guest Mode
        Row(
            modifier = Modifier
                .clickable { viewModel.playAsGuest(onAuthSuccess) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Play as Guest (Skip for now) ▶", color = ChessTextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

