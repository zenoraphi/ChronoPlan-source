package com.chronoplan.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isVerified: Boolean = false,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val repo: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val result = repo.signUp(email, password)
            _state.value = if (result.isSuccess) {
                AuthState(message = result.getOrNull(), isSuccess = true)
            } else {
                AuthState(message = result.exceptionOrNull()?.message)
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val result = repo.signIn(email, password)
            _state.value = if (result.isSuccess) {
                AuthState(isSuccess = true, isVerified = true)
            } else {
                AuthState(message = result.exceptionOrNull()?.message)
            }
        }
    }

    fun reloadVerification() {
        viewModelScope.launch {
            val verified = repo.reloadUser()
            _state.value = _state.value.copy(
                message = if (verified) "Email sudah diverifikasi" else "Belum diverifikasi",
                isVerified = verified
            )
        }
    }

    fun signOut() {
        repo.signOut()
        _state.value = AuthState()
    }
}
