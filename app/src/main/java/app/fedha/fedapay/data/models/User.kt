package app.fedha.fedapay.data.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val fullName: String,
    val role: String,
    val merchantSource: String? = null
) {
    val isMerchant: Boolean
        get() = role.lowercase() == "merchant"
}

data class Wallet(
    val id: Int? = null,
    @SerializedName("wallet_address")
    val walletAddress: String,
    val balance: Double,
    @SerializedName("locked_balance")
    val lockedBalance: Double = 0.0,
    @SerializedName("lifetime_earned")
    val lifetimeEarned: Double = 0.0,
    @SerializedName("lifetime_spent")
    val lifetimeSpent: Double = 0.0,
    @SerializedName("entity_name")
    val entityName: String? = null,
    @SerializedName("entity_type")
    val entityType: String? = null,
    val currency: String = "FTK"
)

data class Transaction(
    val id: Int,
    val hash: String? = null,
    val type: String,  // "incoming" or "outgoing"
    @SerializedName("transactionType")
    val transactionType: String? = null,  // "transfer", "reward", "payment", etc.
    val amount: Double,
    val fee: Double = 0.0,
    val counterparty: String? = null,  // Name of other party
    val description: String?,
    val status: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("completedAt")
    val completedAt: String? = null
) {
    val isIncoming: Boolean
        get() = type.lowercase() == "incoming"
}

data class DashboardStats(
    @SerializedName("today_sales")
    val todaySales: Double = 0.0,
    @SerializedName("total_transactions")
    val totalTransactions: Int = 0,
    @SerializedName("wallet_balance")
    val walletBalance: Double = 0.0,
    @SerializedName("pending_payments")
    val pendingPayments: Int = 0,
    @SerializedName("recent_transactions")
    val recentTransactions: List<Transaction> = emptyList()
)
