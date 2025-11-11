package com.chronoplan.ui.user.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.domain.usecase.ChronoUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SignUpUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSignedUp: Boolean = false,
    val errorMessage: String? = null,
    val showVerificationDialog: Boolean = false
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
        // Cek verifikasi email
        if (user != null && user.isEmailVerified) {
            onAlreadyLoggedIn()
        }
    }

    fun signUp() {
        val state = _uiState.value

        // Validasi ketat
        when {
            state.displayName.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "Nama harus diisi")
                return
            }
            state.displayName.length < 3 -> {
                _uiState.value = state.copy(errorMessage = "Nama minimal 3 karakter")
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
            isDummyEmail(state.email) -> {
                _uiState.value = state.copy(errorMessage = "Gunakan email asli yang valid")
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
            !isStrongPassword(state.password) -> {
                _uiState.value = state.copy(
                    errorMessage = "Password harus mengandung huruf dan angka"
                )
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                // Register + kirim verifikasi email
                val result = useCase.registerUser(state.email, state.password, state.displayName)

                if (result.isSuccess) {
                    // Kirim email verifikasi
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.sendEmailVerification()?.await()

                    // Logout dan tampilkan dialog
                    FirebaseAuth.getInstance().signOut()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showVerificationDialog = true
                    )
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Gagal mendaftar"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = parseErrorMessage(errorMsg)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }

    private fun isDummyEmail(email: String): Boolean {
        val dummyPatterns = listOf(
            "test", "dummy", "fake", "asdf", "qwerty",
            "aaaa", "bbbb", "cccc", "dddd", "eeee",
            "1234", "sample", "example"
        )

        val emailLower = email.lowercase()
        return dummyPatterns.any { emailLower.contains(it) }
    }

    private fun isStrongPassword(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }

    private fun parseErrorMessage(error: String): String {
        return when {
            error.contains("email address is already in use", ignoreCase = true) ->
                "Email sudah terdaftar. Gunakan email lain."
            error.contains("network", ignoreCase = true) ->
                "Tidak ada koneksi internet"
            error.contains("weak-password", ignoreCase = true) ->
                "Password terlalu lemah"
            error.contains("invalid-email", ignoreCase = true) ->
                "Format email tidak valid"
            else -> error
        }
    }

    fun dismissVerificationDialog() {
        _uiState.value = _uiState.value.copy(showVerificationDialog = false)
    }
}