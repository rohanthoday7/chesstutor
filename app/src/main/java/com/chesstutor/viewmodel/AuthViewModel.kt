package com.chesstutor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstutor.data.local.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    REGISTER
}

enum class SkillLevel(val title: String, val rating: Int, val description: String) {
    NEW("Beginner", 800, "I am new to chess"),
    INTERMEDIATE("Intermediate", 1200, "I know basic tactics and openings"),
    ADVANCED("Advanced", 1600, "I play competitive games")
}

data class AuthUiState(
    val authMode: AuthMode = AuthMode.REGISTER,
    val usernameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val selectedSkill: SkillLevel = SkillLevel.INTERMEDIATE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val session: UserSession = UserSession()
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setAuthMode(mode: AuthMode) {
        _uiState.update { it.copy(authMode = mode, errorMessage = null) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(usernameInput = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(emailInput = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(passwordInput = value, errorMessage = null) }
    }

    fun onSkillSelected(skill: SkillLevel) {
        _uiState.update { it.copy(selectedSkill = skill) }
    }

    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.usernameInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a username.") }
            return
        }
        if (state.authMode == AuthMode.REGISTER && state.emailInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter an email address.") }
            return
        }
        if (state.passwordInput.length < 4) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 4 characters.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(500L) // Simulate network auth exchange

            val session = UserSession(
                isLoggedIn = true,
                isGuest = false,
                username = state.usernameInput.trim(),
                email = state.emailInput.trim(),
                rating = if (state.authMode == AuthMode.REGISTER) state.selectedSkill.rating else 1200,
                avatarEmoji = "♟️"
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    session = session,
                    errorMessage = null
                )
            }
            onSuccess()
        }
    }

    fun playAsGuest(onSuccess: () -> Unit) {
        val guestSession = UserSession(
            isLoggedIn = true,
            isGuest = true,
            username = "Guest_${(1000..9999).random()}",
            email = "",
            rating = 1000,
            avatarEmoji = "👤"
        )
        _uiState.update { it.copy(session = guestSession) }
        onSuccess()
    }

    fun logout() {
        _uiState.update {
            it.copy(
                session = UserSession(isLoggedIn = false),
                usernameInput = "",
                emailInput = "",
                passwordInput = ""
            )
        }
    }
}

