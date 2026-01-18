package app.fedha.fedapay.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fedha.fedapay.data.models.Transaction
import app.fedha.fedapay.data.models.Wallet
import app.fedha.fedapay.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val isLoading: Boolean = true,
    val wallet: Wallet? = null,
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null,
    val transferSuccess: Boolean = false,
    val transferMessage: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    fun loadWallet() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = walletRepository.getWallet()
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        wallet = response.wallet,
                        transactions = response.transactions ?: emptyList()
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load wallet"
                    )
                }
            )
        }
    }

    fun transfer(toAddress: String, amount: Double, description: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = walletRepository.transfer(toAddress, amount, description)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transferSuccess = true,
                        transferMessage = response.message
                    )
                    // Reload wallet to get updated balance
                    loadWallet()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Transfer failed"
                    )
                }
            )
        }
    }

    fun clearTransferState() {
        _uiState.value = _uiState.value.copy(transferSuccess = false, transferMessage = null)
    }
}
