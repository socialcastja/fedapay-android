package app.fedha.fedapay.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fedha.fedapay.data.models.DashboardStats
import app.fedha.fedapay.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val stats: DashboardStats? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState(isLoading = true)

            val result = walletRepository.getDashboard()
            result.fold(
                onSuccess = { stats ->
                    _uiState.value = DashboardUiState(isLoading = false, stats = stats)
                },
                onFailure = { exception ->
                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        error = exception.message ?: "Failed to load dashboard"
                    )
                }
            )
        }
    }
}
