package com.chronoplan.ui.akun

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.data.model.AchievementDto
import com.chronoplan.data.model.UserStatsDto
import com.chronoplan.domain.usecase.AchievementUseCase
import com.chronoplan.domain.usecase.ChronoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AkunUiState(
    val username: String = "Guest",
    val email: String = "",
    val level: String = "Newbie",
    val avatarUrl: String? = null,
    val isCalendarSynced: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val showEditNameDialog: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val achievements: List<AchievementDto> = emptyList(),
    val stats: UserStatsDto = UserStatsDto()
)

class AkunViewModel(
    private val useCase: ChronoUseCase,
    private val achievementUseCase: AchievementUseCase = AchievementUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AkunUiState())
    val uiState: StateFlow<AkunUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadStats()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = useCase.getProfile()
            if (res.isSuccess) {
                val p = res.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    username = p.displayName,
                    email = p.email,
                    level = p.level,
                    avatarUrl = p.avatarUrl,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            useCase.observeAgendas().collect { agendas ->
                val completed = agendas.count { it.status == "done" }
                val total = agendas.size

                useCase.observeNotes().collect { notes ->
                    val stats = UserStatsDto(
                        totalAgendas = total,
                        completedAgendas = completed,
                        totalNotes = notes.size,
                        currentStreak = 0
                    )

                    val achievements = achievementUseCase.checkAchievements(stats)

                    _uiState.value = _uiState.value.copy(
                        stats = stats,
                        achievements = achievements
                    )
                }
            }
        }
    }

    // ✅ FIXED: Avatar upload dengan flow yang benar
    fun uploadAvatar(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingAvatar = true)

            try {
                // 1. Upload image ke Firebase Storage
                val uploadResult = useCase.uploadAttachment(
                    localUri = imageUri,
                    remotePath = "avatars/${System.currentTimeMillis()}.jpg"
                )

                if (uploadResult.isSuccess) {
                    val avatarUrl = uploadResult.getOrNull()!!

                    // 2. Get current profile
                    val currentProfile = useCase.getProfile().getOrNull()
                    if (currentProfile != null) {
                        // 3. Update profile dengan URL baru
                        val updatedProfile = currentProfile.copy(avatarUrl = avatarUrl)
                        val updateResult = useCase.updateProfile(updatedProfile)

                        if (updateResult.isSuccess) {
                            // 4. Update UI state immediately
                            _uiState.value = _uiState.value.copy(
                                avatarUrl = avatarUrl,
                                isUploadingAvatar = false
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isUploadingAvatar = false
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isUploadingAvatar = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isUploadingAvatar = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUploadingAvatar = false)
            }
        }
    }

    fun updateUsername(newName: String) {
        viewModelScope.launch {
            val currentProfile = useCase.getProfile().getOrNull()
            if (currentProfile != null) {
                val updated = currentProfile.copy(displayName = newName)
                val result = useCase.updateProfile(updated)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(username = newName)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            useCase.signOut()
            _uiState.value = _uiState.value.copy(isLoggedOut = true)
        }
    }

    fun toggleCalendarSync(isChecked: Boolean) {
        _uiState.value = _uiState.value.copy(isCalendarSynced = isChecked)
    }

    fun showEditNameDialog() {
        _uiState.value = _uiState.value.copy(showEditNameDialog = true)
    }

    fun hideEditNameDialog() {
        _uiState.value = _uiState.value.copy(showEditNameDialog = false)
    }
}