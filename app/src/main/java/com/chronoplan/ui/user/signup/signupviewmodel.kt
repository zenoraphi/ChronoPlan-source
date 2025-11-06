package com.chronoplan.ui.user.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.domain.usecase.ChronoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSignedUp: Boolean = false,
    val errorMessage: String? = null
)

class SignUpViewModel(private val useCase: ChronoUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun onDisplayNameChange(v: String) {
        _uiState.value = _uiState.value.copy(displayName = v)
    }

    fun onEmailChange(v: String) {
        _uiState.value = _uiState.value.copy(email = v)
    }

    fun onPasswordChange(v: String) {
        _uiState.value = _uiState.value.copy(password = v)
    }

    fun signUp() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank() || state.displayName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Semua kolom wajib diisi.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                val result = useCase.registerUser(state.email, state.password, state.displayName)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isSignedUp = true, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal mendaftar."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error tak dikenal"
                )
            }
        }
    }
}
