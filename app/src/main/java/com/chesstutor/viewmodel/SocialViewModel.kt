package com.chesstutor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesstutor.data.remote.ApiClient
import com.chesstutor.data.remote.LeaderboardPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SocialTab {
    LEADERBOARD,
    FRIENDS
}

enum class LeaderboardCategory {
    RAPID,
    BLITZ,
    BULLET,
    TACTICS
}

data class FriendItem(
    val username: String,
    val rating: Int,
    val isOnline: Boolean,
    val avatarEmoji: String
)

data class SocialUiState(
    val activeTab: SocialTab = SocialTab.LEADERBOARD,
    val selectedCategory: LeaderboardCategory = LeaderboardCategory.RAPID,
    val players: List<LeaderboardPlayer> = emptyList(),
    val friends: List<FriendItem> = emptyList(),
    val isAddFriendDialogOpen: Boolean = false,
    val friendInputUsername: String = "",
    val feedbackMessage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SocialViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    private var cachedLeaderboards: com.chesstutor.data.remote.ChessComLeaderboardsResponse? = null

    init {
        loadFriends()
        loadLeaderboard(LeaderboardCategory.RAPID)
    }

    fun setActiveTab(tab: SocialTab) {
        _uiState.update { it.copy(activeTab = tab, feedbackMessage = null) }
    }

    fun selectCategory(category: LeaderboardCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        val cached = cachedLeaderboards
        if (cached != null) {
            val list = when (category) {
                LeaderboardCategory.RAPID -> cached.liveRapid
                LeaderboardCategory.BLITZ -> cached.liveBlitz
                LeaderboardCategory.BULLET -> cached.liveBullet
                LeaderboardCategory.TACTICS -> cached.tactics
            }
            _uiState.update { it.copy(players = list) }
        } else {
            loadLeaderboard(category)
        }
    }

    fun loadLeaderboard(category: LeaderboardCategory) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = ApiClient.chessComApi.getLeaderboards()
                cachedLeaderboards = response

                val list = when (category) {
                    LeaderboardCategory.RAPID -> response.liveRapid
                    LeaderboardCategory.BLITZ -> response.liveBlitz
                    LeaderboardCategory.BULLET -> response.liveBullet
                    LeaderboardCategory.TACTICS -> response.tactics
                }

                _uiState.update {
                    it.copy(
                        selectedCategory = category,
                        players = list,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                val fallbackList = listOf(
                    LeaderboardPlayer(1, "MagnusCarlsen", 2882, 1, "GM"),
                    LeaderboardPlayer(2, "Hikaru", 2875, 2, "GM"),
                    LeaderboardPlayer(3, "DanielNaroditsky", 2795, 3, "GM"),
                    LeaderboardPlayer(4, "Firouzja2003", 2805, 4, "GM"),
                    LeaderboardPlayer(5, "FabianoCaruana", 2800, 5, "GM")
                )
                _uiState.update {
                    it.copy(
                        selectedCategory = category,
                        players = fallbackList,
                        isLoading = false,
                        errorMessage = "Showing cached master leaderboard"
                    )
                }
            }
        }
    }

    private fun loadFriends() {
        val initialFriends = listOf(
            FriendItem("Alex_GM", 2150, true, "🦁"),
            FriendItem("ChessMaster42", 1420, true, "😎"),
            FriendItem("TacticalWizard", 1680, false, "🧙‍♂️"),
            FriendItem("OpeningTheoryGuy", 1350, true, "📚")
        )
        _uiState.update { it.copy(friends = initialFriends) }
    }

    fun openAddFriendDialog() {
        _uiState.update { it.copy(isAddFriendDialogOpen = true, friendInputUsername = "", feedbackMessage = null) }
    }

    fun closeAddFriendDialog() {
        _uiState.update { it.copy(isAddFriendDialogOpen = false, friendInputUsername = "") }
    }

    fun onFriendInputChanged(value: String) {
        _uiState.update { it.copy(friendInputUsername = value, feedbackMessage = null) }
    }

    fun addFriend() {
        val target = _uiState.value.friendInputUsername.trim()
        if (target.isBlank()) {
            _uiState.update { it.copy(feedbackMessage = "Please enter a valid username.") }
            return
        }

        val alreadyFriends = _uiState.value.friends.any { it.username.equals(target, ignoreCase = true) }
        if (alreadyFriends) {
            _uiState.update { it.copy(feedbackMessage = "'$target' is already in your friends list.") }
            return
        }

        val newFriend = FriendItem(
            username = target,
            rating = (1200..1800).random(),
            isOnline = true,
            avatarEmoji = listOf("👑", "⚡", "🎯", "♟️", "🦁").random()
        )

        _uiState.update {
            it.copy(
                friends = listOf(newFriend) + it.friends,
                isAddFriendDialogOpen = false,
                friendInputUsername = "",
                feedbackMessage = "Added '$target' to your friends!"
            )
        }
    }
}
