package app.fedha.fedapay.data.repository

import app.fedha.fedapay.data.api.FedaPayApi
import app.fedha.fedapay.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val api: FedaPayApi
) {
    suspend fun getWallet(): Result<WalletResponse> {
        return try {
            val response = api.getWallet()
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load wallet"))
            }
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun transfer(toAddress: String, amount: Double, description: String?): Result<TransferResponse> {
        return try {
            val request = TransferRequest(toAddress, amount, description)
            val response = api.transfer(request)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Transfer failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboard(): Result<DashboardStats> {
        return try {
            val response = api.getDashboard()
            if (response.success && response.stats != null) {
                Result.success(response.stats)
            } else {
                Result.failure(Exception("Failed to load dashboard"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processPayment(amount: Double, customerWallet: String?, paymentMethod: String): Result<TransferResponse> {
        return try {
            val payment = mapOf(
                "amount" to amount,
                "customer_wallet" to (customerWallet ?: ""),
                "payment_method" to paymentMethod
            )
            val response = api.processPayment(payment)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Payment failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
