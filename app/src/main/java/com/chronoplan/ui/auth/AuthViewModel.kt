package com.chronoplan.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.domain.usecase.ChronoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel(private val useCase: ChronoUseCase) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun setEmail(e: String) { _ui.value = _ui.value.copy(email = e) }
    fun setPassword(p: String) { _ui.value = _ui.value.copy(password = p) }

    fun signIn() {
        val email = _ui.value.email.trim()
        val pwd = _ui.value.password
        if (email.isEmpty() || pwd.isEmpty()) {
            _ui.value = _ui.value.copy(errorMessage = "Email atau password kosong")
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, errorMessage = null)
            val res = useCase.signInWithEmail(email, pwd)
            if (res.isSuccess) {
                _ui.value = _ui.value.copy(isLoading = false, isAuthenticated = true)
            } else {
                _ui.value = _ui.value.copy(isLoading = false, errorMessage = res.exceptionOrNull()?.message)
            }
        }
    }

    fun signUp(displayName: String? = null) {
        val email = _ui.value.email.trim()
        val pwd = _ui.value.password
        if (email.isEmpty() || pwd.isEmpty()) {
            _ui.value = _ui.value.copy(errorMessage = "Email atau password kosong")
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, errorMessage = null)
            val res = useCase.signUpWithEmail(email, pwd, displayName)
            if (res.isSuccess) {
                _ui.value = _ui.value.copy(isLoading = false, isAuthenticated = true)
            } else {
                _ui.value = _ui.value.copy(isLoading = false, errorMessage = res.exceptionOrNull()?.message)
            }
        }
    }
}
