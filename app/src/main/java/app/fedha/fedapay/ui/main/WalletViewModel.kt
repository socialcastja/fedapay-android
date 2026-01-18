package app.fedha.fedapay.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fedha.fedapay.data.models.Transaction
import app.fedha.fedapay.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val isLoading: Boolean = true,
    val balance: Double = 0.0,
    val lockedBalance: Double = 0.0,
    val lifetimeEarned: Double = 0.0,
    val lifetimeSpent: Double = 0.0,
    val walletAddress: String = "",
    val entityName: String = "",
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null,
    val transferSuccess: Boolean = false,
    val transferMessage: String? = null,
    val hasPin: Boolean = false,
    val isPinLocked: Boolean = false,
    val showPinSetup: Boolean = false,
    val pinError: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadWallet()
        checkPinStatus()
    }

    fun loadWallet() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Load balance
            val balanceResult = walletRepository.getWalletBalance()
            balanceResult.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        balance = response.balance ?: 0.0,
                        lockedBalance = response.lockedBalance ?: 0.0,
                        lifetimeEarned = response.lifetimeEarned ?: 0.0,
                        lifetimeSpent = response.lifetimeSpent ?: 0.0,
                        walletAddress = response.walletAddress ?: "",
                        entityName = response.entityName ?: ""
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to load wallet"
                    )
                }
            )

            // Load transactions
            val transactionsResult = walletRepository.getTransactions()
            transactionsResult.fold(
                onSuccess = { transactions ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transactions = transactions
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = _uiState.value.error ?: exception.message ?: "Failed to load transactions"
                    )
                }
            )
        }
    }

    fun checkPinStatus() {
        viewModelScope.launch {
            val result = walletRepository.getPinStatus()
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        hasPin = response.hasPin ?: false,
                        isPinLocked = response.isLocked ?: false,
                        showPinSetup = !(response.hasPin ?: false)
                    )
                },
                onFailure = {
                    // PIN status check failed, assume no PIN
                    _uiState.value = _uiState.value.copy(
                        hasPin = false,
                        showPinSetup = true
                    )
                }
            )
        }
    }

    fun setPin(pin: String, confirmPin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, pinError = null)

            val result = walletRepository.setPin(pin, confirmPin)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasPin = true,
                        showPinSetup = false,
                        transferMessage = "PIN set successfully"
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pinError = exception.message ?: "Failed to set PIN"
                    )
                }
            )
        }
    }

    fun transfer(recipientWallet: String, amount: Double, description: String?, pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = walletRepository.transfer(recipientWallet, amount, description, pin)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transferSuccess = true,
                        transferMessage = response.message ?: "Transfer successful",
                        balance = response.newBalance ?: _uiState.value.balance
                    )
                    // Reload wallet to get updated data
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
        _uiState.value = _uiState.value.copy(
            transferSuccess = false,
            transferMessage = null,
            error = null,
            pinError = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
