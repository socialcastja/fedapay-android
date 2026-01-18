package app.fedha.fedapay.data.models

import com.google.gson.annotations.SerializedName

// Generic API response
data class ApiResponse(
    val success: Boolean,
    val message: String?
)

// Auth responses
data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user: User?
)

data class RegisterResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user: User?,
    val welcomeBonus: Double? = null
)

data class ProfileResponse(
    val success: Boolean,
    val message: String?,
    val id: Int?,
    val username: String?,
    val email: String?,
    val fullName: String?,
    val role: String?,
    val merchantSource: String?
) {
    fun toUser(): User? {
        return if (id != null && username != null && email != null && fullName != null && role != null) {
            User(id, username, email, fullName, role, merchantSource)
        } else null
    }
}

// Wallet responses
data class WalletBalanceResponse(
    val success: Boolean,
    val message: String?,
    val balance: Double?,
    @SerializedName("locked_balance")
    val lockedBalance: Double?,
    @SerializedName("lifetime_earned")
    val lifetimeEarned: Double?,
    @SerializedName("lifetime_spent")
    val lifetimeSpent: Double?,
    @SerializedName("wallet_address")
    val walletAddress: String?,
    @SerializedName("entity_name")
    val entityName: String?,
    @SerializedName("entity_type")
    val entityType: String?
)

data class TransactionsResponse(
    val success: Boolean,
    val message: String?,
    val transactions: List<Transaction>?
)

data class TransferResponse(
    val success: Boolean,
    val message: String?,
    @SerializedName("transaction_hash")
    val transactionHash: String?,
    val amount: Double?,
    val fee: Double?,
    val recipient: String?,
    @SerializedName("new_balance")
    val newBalance: Double?
)

// Dashboard - Merchant Dashboard API response
data class DashboardResponse(
    val success: Boolean,
    val message: String?,
    val merchant: MerchantInfo?,
    val wallet: WalletInfo?,
    val stats: StatsInfo?,
    val recentPayments: List<RecentPayment>?
) {
    // Convert API response to UI-friendly DashboardStats
    fun toDashboardStats(): DashboardStats {
        return DashboardStats(
            todaySales = stats?.today?.amount ?: 0.0,
            totalTransactions = stats?.today?.transactions ?: 0,
            walletBalance = wallet?.balance ?: 0.0,
            pendingPayments = 0,
            recentTransactions = recentPayments?.map { payment ->
                Transaction(
                    id = payment.id,
                    hash = null,
                    type = "incoming",
                    transactionType = "payment",
                    amount = payment.amount,
                    fee = 0.0,
                    counterparty = payment.from,
                    description = "Payment from ${payment.from ?: "Customer"}",
                    status = "completed",
                    createdAt = payment.createdAt ?: "",
                    completedAt = payment.createdAt
                )
            } ?: emptyList()
        )
    }
}

// PIN responses
data class PinStatusResponse(
    val success: Boolean,
    val message: String?,
    @SerializedName("has_pin")
    val hasPin: Boolean?,
    @SerializedName("is_locked")
    val isLocked: Boolean?,
    @SerializedName("locked_until")
    val lockedUntil: String?
)

// Payment responses
data class PaymentRequestResponse(
    val success: Boolean,
    val message: String?,
    @SerializedName("request_code")
    val requestCode: String?,
    @SerializedName("qr_data")
    val qrData: String?,
    val amount: Double?,
    val currency: String?,
    @SerializedName("expires_at")
    val expiresAt: String?
)

data class PaymentVerifyResponse(
    val success: Boolean,
    val message: String?,
    val valid: Boolean?,
    val amount: Double?,
    val currency: String?,
    @SerializedName("merchant_name")
    val merchantName: String?,
    val description: String?
)

data class PaymentHistoryResponse(
    val success: Boolean,
    val message: String?,
    val payments: List<PaymentRecord>?
)

data class PaymentRecord(
    val id: Int,
    @SerializedName("request_code")
    val requestCode: String?,
    val amount: Double,
    val currency: String?,
    val description: String?,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("paid_at")
    val paidAt: String?
)

// NFC responses
data class NfcTokenResponse(
    val success: Boolean,
    val message: String?,
    @SerializedName("payment_token")
    val paymentToken: String?,
    @SerializedName("nfc_data")
    val nfcData: String?,
    val amount: Double?,
    @SerializedName("expires_at")
    val expiresAt: String?
)

data class NfcValidationResponse(
    val success: Boolean,
    val message: String?,
    val valid: Boolean?,
    @SerializedName("sender_wallet")
    val senderWallet: String?,
    @SerializedName("sender_name")
    val senderName: String?,
    val amount: Double?,
    @SerializedName("payment_token")
    val paymentToken: String?
)

// Merchant responses - API returns settings fields at top level, not nested
data class MerchantSettingsResponse(
    val success: Boolean,
    val message: String?,
    @SerializedName("merchant_name")
    val merchantName: String?,
    @SerializedName("merchant_source")
    val merchantSource: String?,
    val email: String?,
    val phone: String?,
    val status: String?,
    val logo: String?,
    @SerializedName("accept_ftk")
    val acceptFtk: Boolean?,
    @SerializedName("accept_card")
    val acceptCard: Boolean?,
    @SerializedName("nfc_enabled")
    val nfcEnabled: Boolean?,
    @SerializedName("qr_enabled")
    val qrEnabled: Boolean?
)

// Legacy MerchantSettings - for backwards compatibility
data class MerchantSettings(
    @SerializedName("merchant_name")
    val merchantName: String?,
    @SerializedName("business_name")
    val businessName: String?,
    @SerializedName("merchant_email")
    val merchantEmail: String?,
    @SerializedName("merchant_phone")
    val merchantPhone: String?,
    @SerializedName("accept_ftk")
    val acceptFtk: Boolean?,
    @SerializedName("auto_convert")
    val autoConvert: Boolean?
)

// Notification responses
data class NotificationsResponse(
    val success: Boolean,
    val message: String?,
    val notifications: List<Notification>?,
    @SerializedName("unread_count")
    val unreadCount: Int?
)

data class Notification(
    val id: Int,
    @SerializedName("notification_type")
    val notificationType: String?,
    val title: String,
    val message: String,
    val icon: String?,
    val link: String?,
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("created_at")
    val createdAt: String?
)

// Transfer request responses
data class TransferRequestResponse(
    val success: Boolean,
    val message: String?,
    @SerializedName("request_id")
    val requestId: Int?,
    @SerializedName("request_code")
    val requestCode: String?
)

data class TransferRequestsListResponse(
    val success: Boolean,
    val message: String?,
    val requests: List<TransferRequestItem>?
)

data class TransferRequestItem(
    val id: Int,
    @SerializedName("request_code")
    val requestCode: String?,
    @SerializedName("requester_name")
    val requesterName: String?,
    @SerializedName("requester_wallet")
    val requesterWallet: String?,
    val amount: Double,
    val description: String?,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("expires_at")
    val expiresAt: String?
)

data class UserSearchResponse(
    val success: Boolean,
    val message: String?,
    val users: List<SearchedUser>?
)

data class SearchedUser(
    val id: Int,
    val username: String,
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("wallet_address")
    val walletAddress: String?
)

// Request bodies
data class RegisterRequest(
    @SerializedName("account_type")
    val accountType: String,
    @SerializedName("full_name")
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    @SerializedName("company_name")
    val companyName: String? = null,
    val city: String? = null,
    val country: String? = "Jamaica"
)

data class TransferRequest(
    @SerializedName("recipient_wallet")
    val recipientWallet: String,
    val amount: Double,
    val description: String? = null,
    val pin: String
)

data class PaymentRequestBody(
    val amount: Double,
    val currency: String = "FTK",
    val description: String? = null
)

data class PosPaymentRequest(
    @SerializedName("sender_wallet")
    val senderWallet: String,
    val amount: Double,
    @SerializedName("payment_token")
    val paymentToken: String,
    val method: String // "nfc" or "qr"
)

data class TransferRequestBody(
    @SerializedName("recipient_id")
    val recipientId: Int,
    val amount: Double,
    val description: String? = null
)
