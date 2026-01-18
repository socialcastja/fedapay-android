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

    @GET("auth/profile")
    suspend fun getProfile(): ProfileResponse

    @POST("auth/logout")
    suspend fun logout(): Map<String, Any>

    // Wallet
    @GET("wallet/balance")
    suspend fun getWalletBalance(): WalletBalanceResponse

    @GET("wallet/transactions")
    suspend fun getTransactions(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): TransactionsResponse

    @POST("wallet/transfer")
    suspend fun transfer(
        @Body request: TransferRequest
    ): TransferResponse

    // PIN Management
    @GET("pin/status")
    suspend fun getPinStatus(): PinStatusResponse

    @POST("pin/set")
    suspend fun setPin(
        @Body request: Map<String, String>
    ): ApiResponse

    @POST("pin/change")
    suspend fun changePin(
        @Body request: Map<String, String>
    ): ApiResponse

    @POST("pin/verify")
    suspend fun verifyPin(
        @Body request: Map<String, String>
    ): ApiResponse

    // Payments
    @POST("payments/create-request")
    suspend fun createPaymentRequest(
        @Body request: PaymentRequestBody
    ): PaymentRequestResponse

    @POST("payments/process")
    suspend fun processPayment(
        @Body payment: Map<String, Any>
    ): TransferResponse

    @GET("payments/verify/{code}")
    suspend fun verifyPayment(
        @Path("code") paymentCode: String
    ): PaymentVerifyResponse

    @GET("payments/history")
    suspend fun getPaymentHistory(
        @Query("limit") limit: Int = 50
    ): PaymentHistoryResponse

    // POS (Merchant receives payment from customer)
    @POST("payments/pos")
    suspend fun processPosPayment(
        @Body payment: PosPaymentRequest
    ): TransferResponse

    // NFC
    @POST("nfc/register-device")
    suspend fun registerNfcDevice(
        @Body request: Map<String, String>
    ): ApiResponse

    @POST("nfc/generate-token")
    suspend fun generateNfcPaymentToken(
        @Body request: Map<String, Any>
    ): NfcTokenResponse

    @POST("nfc/validate")
    suspend fun validateNfcPayment(
        @Body request: Map<String, String>
    ): NfcValidationResponse

    // Merchant
    @GET("merchant/dashboard")
    suspend fun getMerchantDashboard(): DashboardResponse

    @GET("merchant/settings")
    suspend fun getMerchantSettings(): MerchantSettingsResponse

    @POST("merchant/settings")
    suspend fun updateMerchantSettings(
        @Body settings: Map<String, Any>
    ): ApiResponse

    // Notifications
    @GET("notifications")
    suspend fun getNotifications(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): NotificationsResponse

    @POST("notifications/mark-read")
    suspend fun markNotificationRead(
        @Body request: Map<String, Int>
    ): ApiResponse

    @POST("notifications/mark-all-read")
    suspend fun markAllNotificationsRead(): ApiResponse

    // Transfer Requests
    @POST("transfer-requests/create")
    suspend fun createTransferRequest(
        @Body request: TransferRequestBody
    ): TransferRequestResponse

    @GET("transfer-requests/pending")
    suspend fun getPendingTransferRequests(): TransferRequestsListResponse

    @POST("transfer-requests/approve")
    suspend fun approveTransferRequest(
        @Body request: Map<String, Any>
    ): TransferResponse

    @POST("transfer-requests/reject")
    suspend fun rejectTransferRequest(
        @Body request: Map<String, Int>
    ): ApiResponse

    @GET("transfer-requests/search-users")
    suspend fun searchUsersForTransfer(
        @Query("q") query: String
    ): UserSearchResponse
}
