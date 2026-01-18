package app.fedha.fedapay.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fedha.fedapay.data.models.RegisterRequest
import app.fedha.fedapay.data.models.User
import app.fedha.fedapay.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val registrationMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isAuthenticated = authRepository.isAuthenticated
    val isLoading = authRepository.isLoading
    val user = authRepository.user

    init {
        viewModelScope.launch {
            authRepository.checkAuth()
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter both username and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = authRepository.login(username, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(isLoading = false, user = user)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Login failed"
                    )
                }
            )
        }
    }

    fun register(
        accountType: String,
        fullName: String,
        email: String,
        password: String,
        phone: String?,
        companyName: String?,
        city: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val request = RegisterRequest(
                accountType = accountType,
                fullName = fullName,
                email = email,
                password = password,
                phone = phone?.takeIf { it.isNotBlank() },
                companyName = companyName?.takeIf { it.isNotBlank() },
                city = city?.takeIf { it.isNotBlank() }
            )

            val result = authRepository.register(request)
            result.fold(
                onSuccess = { (user, message) ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        registrationMessage = message
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Registration failed"
                    )
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearRegistrationMessage() {
        _uiState.value = _uiState.value.copy(registrationMessage = null)
    }
}
