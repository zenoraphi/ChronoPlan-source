package com.chronoplan.ui.akun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.domain.usecase.ChronoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AkunUiState(
    val username: String = "Guest",
    val level: String = "Newbie",
    val isCalendarSynced: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false
)

class AkunViewModel(private val useCase: ChronoUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(AkunUiState())
    val uiState: StateFlow<AkunUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = useCase.getProfile()
            if (res.isSuccess) {
                val p = res.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    username = p.displayName,
                    level = p.level,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
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
}
