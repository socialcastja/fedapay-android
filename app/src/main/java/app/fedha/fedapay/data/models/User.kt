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
    val id: Int,
    @SerializedName("wallet_address")
    val walletAddress: String,
    val balance: Double,
    val currency: String = "FTK"
)

data class Transaction(
    val id: Int,
    val type: String,
    val amount: Double,
    val description: String?,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("from_address")
    val fromAddress: String? = null,
    @SerializedName("to_address")
    val toAddress: String? = null
) {
    val isIncoming: Boolean
        get() = type.lowercase() in listOf("receive", "credit", "reward")
}

data class DashboardStats(
    @SerializedName("today_sales")
    val todaySales: Double,
    @SerializedName("total_transactions")
    val totalTransactions: Int,
    @SerializedName("wallet_balance")
    val walletBalance: Double,
    @SerializedName("recent_transactions")
    val recentTransactions: List<Transaction>
)
