package app.fedha.fedapay.data.models

import com.google.gson.annotations.SerializedName

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

data class WalletResponse(
    val success: Boolean,
    val message: String?,
    val wallet: Wallet?,
    val transactions: List<Transaction>?
)

data class TransferResponse(
    val success: Boolean,
    val message: String?,
    val transaction: Transaction?,
    @SerializedName("new_balance")
    val newBalance: Double?
)

data class DashboardResponse(
    val success: Boolean,
    val stats: DashboardStats?
)

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
    @SerializedName("to_address")
    val toAddress: String,
    val amount: Double,
    val description: String? = null
)
