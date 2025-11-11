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
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun signIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val res = useCase.signInWithEmail(_uiState.value.email, _uiState.value.password)

            if (res.isSuccess) {
                // Cek verifikasi email
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && user.isEmailVerified) {
                    _uiState.update { it.copy(isLoading = false, isSignedIn = true) }
                } else {
                    // Belum verifikasi
                    FirebaseAuth.getInstance().signOut()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Email belum diverifikasi. Silakan cek inbox email Anda."
                        )
                    }
                }
            } else {
                val errorMsg = res.exceptionOrNull()?.message ?: "Login gagal"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = when {
                            errorMsg.contains("no user record", ignoreCase = true) ||
                                    errorMsg.contains("invalid-credential", ignoreCase = true) ->
                                "Email atau password salah"
                            errorMsg.contains("network", ignoreCase = true) ->
                                "Tidak ada koneksi internet"
                            else -> errorMsg
                        }
                    )
                }
            }
        }
    }

    fun checkAutoLogin(onAlreadyLoggedIn: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.isEmailVerified) {
            onAlreadyLoggedIn()
        }
    }
}