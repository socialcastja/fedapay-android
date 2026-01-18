package app.fedha.fedapay.data.api

import app.fedha.fedapay.data.models.*
import retrofit2.http.*

interface FedaPayApi {

    // Auth
    @POST("auth/login")
    suspend fun login(
        @Body credentials: Map<String, String>
    ): LoginResponse

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    @GET("auth/verify")
    suspend fun verifyToken(): ProfileResponse

    @POST("auth/logout")
    suspend fun logout(): Map<String, Any>

    // Wallet
    @GET("wallet")
    suspend fun getWallet(): WalletResponse

    @GET("wallet/transactions")
    suspend fun getTransactions(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): WalletResponse

    @POST("wallet/transfer")
    suspend fun transfer(
        @Body request: TransferRequest
    ): TransferResponse

    // Dashboard
    @GET("dashboard")
    suspend fun getDashboard(): DashboardResponse

    // POS
    @POST("pos/payment")
    suspend fun processPayment(
        @Body payment: Map<String, Any>
    ): TransferResponse
}
