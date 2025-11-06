package com.chronoplan.ui.user.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.domain.usecase.ChronoUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignedIn: Boolean = false
)

class SignInViewModel(private val useCase: ChronoUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun signIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = useCase.signInWithEmail(_uiState.value.email, _uiState.value.password)
            if (res.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSignedIn = true) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = res.exceptionOrNull()?.message)
                }
            }
        }
    }
    fun checkAutoLogin(onAlreadyLoggedIn: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            onAlreadyLoggedIn()
        }
    }

}