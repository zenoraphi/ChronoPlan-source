package com.chronoplan.ui.user.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.domain.usecase.ChronoUseCase
import com.google.firebase.auth.FirebaseAuth
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
        _uiState.value = _uiState.value.copy(displayName = v, errorMessage = null)
    }

    fun onEmailChange(v: String) {
        _uiState.value = _uiState.value.copy(email = v, errorMessage = null)
    }

    fun onPasswordChange(v: String) {
        _uiState.value = _uiState.value.copy(password = v, errorMessage = null)
    }

    fun checkAutoLogin(onAlreadyLoggedIn: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            onAlreadyLoggedIn()
        }
    }

    fun signUp() {
        val state = _uiState.value

        // Validasi input
        when {
            state.displayName.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "Nama harus diisi")
                return
            }
            state.email.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "Email harus diisi")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> {
                _uiState.value = state.copy(errorMessage = "Format email tidak valid")
                return
            }
            state.password.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "Password harus diisi")
                return
            }
            state.password.length < 6 -> {
                _uiState.value = state.copy(errorMessage = "Password minimal 6 karakter")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                val result = useCase.registerUser(state.email, state.password, state.displayName)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isSignedUp = true, isLoading = false)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Gagal mendaftar"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = when {
                            errorMsg.contains("email address is already in use", ignoreCase = true) ->
                                "Email sudah terdaftar"
                            errorMsg.contains("network", ignoreCase = true) ->
                                "Tidak ada koneksi internet"
                            else -> errorMsg
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error tidak diketahui"
                )
            }
        }
    }
}