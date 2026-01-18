package app.fedha.fedapay.data.repository

import app.fedha.fedapay.data.api.FedaPayApi
import app.fedha.fedapay.data.models.*
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val api: FedaPayApi
) {
    suspend fun getWalletBalance(): Result<WalletBalanceResponse> {
        return try {
            val response = api.getWalletBalance()
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load wallet"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to load wallet"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(limit: Int = 50, offset: Int = 0): Result<List<Transaction>> {
        return try {
            val response = api.getTransactions(limit, offset)
            if (response.success) {
                Result.success(response.transactions ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to load transactions"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to load transactions"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun transfer(
        recipientWallet: String,
        amount: Double,
        description: String?,
        pin: String
    ): Result<TransferResponse> {
        return try {
            val request = TransferRequest(recipientWallet, amount, description, pin)
            val response = api.transfer(request)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Transfer failed"))
            }
        } catch (e: HttpException) {
            val errorMessage = parseErrorMessage(e)
            Result.failure(Exception(errorMessage ?: "Transfer failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboard(): Result<DashboardStats> {
        return try {
            val response = api.getMerchantDashboard()
            if (response.success && response.stats != null) {
                Result.success(response.stats)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load dashboard"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to load dashboard"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PIN Management
    suspend fun getPinStatus(): Result<PinStatusResponse> {
        return try {
            val response = api.getPinStatus()
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get PIN status"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to get PIN status"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPin(pin: String, confirmPin: String): Result<ApiResponse> {
        return try {
            val response = api.setPin(mapOf("pin" to pin, "confirm_pin" to confirmPin))
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to set PIN"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to set PIN"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePin(currentPin: String, newPin: String, confirmPin: String): Result<ApiResponse> {
        return try {
            val response = api.changePin(mapOf(
                "current_pin" to currentPin,
                "new_pin" to newPin,
                "confirm_pin" to confirmPin
            ))
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to change PIN"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to change PIN"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPin(pin: String): Result<ApiResponse> {
        return try {
            val response = api.verifyPin(mapOf("pin" to pin))
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Invalid PIN"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Invalid PIN"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Payment operations
    suspend fun createPaymentRequest(
        amount: Double,
        currency: String = "FTK",
        description: String? = null
    ): Result<PaymentRequestResponse> {
        return try {
            val request = PaymentRequestBody(amount, currency, description)
            val response = api.createPaymentRequest(request)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to create payment request"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to create payment request"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processPosPayment(
        senderWallet: String,
        amount: Double,
        paymentToken: String,
        method: String
    ): Result<TransferResponse> {
        return try {
            val request = PosPaymentRequest(senderWallet, amount, paymentToken, method)
            val response = api.processPosPayment(request)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Payment failed"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Payment failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NFC operations
    suspend fun validateNfcPayment(nfcData: String): Result<NfcValidationResponse> {
        return try {
            val response = api.validateNfcPayment(mapOf("nfc_data" to nfcData))
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Invalid NFC payment"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Invalid NFC payment"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Notifications
    suspend fun getNotifications(limit: Int = 50, offset: Int = 0): Result<NotificationsResponse> {
        return try {
            val response = api.getNotifications(limit, offset)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load notifications"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to load notifications"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(notificationId: Int): Result<ApiResponse> {
        return try {
            val response = api.markNotificationRead(mapOf("notification_id" to notificationId))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Transfer Requests
    suspend fun getPendingTransferRequests(): Result<List<TransferRequestItem>> {
        return try {
            val response = api.getPendingTransferRequests()
            if (response.success) {
                Result.success(response.requests ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to load requests"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to load requests"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveTransferRequest(requestId: Int, pin: String): Result<TransferResponse> {
        return try {
            val response = api.approveTransferRequest(mapOf("request_id" to requestId, "pin" to pin))
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to approve request"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to approve request"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectTransferRequest(requestId: Int): Result<ApiResponse> {
        return try {
            val response = api.rejectTransferRequest(mapOf("request_id" to requestId))
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to reject request"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Failed to reject request"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsersForTransfer(query: String): Result<List<SearchedUser>> {
        return try {
            val response = api.searchUsersForTransfer(query)
            if (response.success) {
                Result.success(response.users ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Search failed"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e) ?: "Search failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(e: HttpException): String? {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
            errorResponse?.message
        } catch (_: Exception) {
            null
        }
    }
}
