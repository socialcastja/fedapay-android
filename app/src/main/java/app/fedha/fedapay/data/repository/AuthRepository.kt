package app.fedha.fedapay.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.fedha.fedapay.data.api.FedaPayApi
import app.fedha.fedapay.data.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: FedaPayApi,
    @ApplicationContext private val context: Context
) {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isMerchant: Boolean
        get() = _user.value?.isMerchant == true

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "fedapay_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getToken(): String? {
        return sharedPrefs.getString("auth_token", null)
    }

    private fun saveToken(token: String) {
        sharedPrefs.edit().putString("auth_token", token).apply()
    }

    private fun clearToken() {
        sharedPrefs.edit().remove("auth_token").apply()
    }

    suspend fun checkAuth() {
        _isLoading.value = true
        try {
            val token = getToken()
            if (token == null) {
                _user.value = null
                _isAuthenticated.value = false
                return
            }

            val response = api.verifyToken()
            if (response.success) {
                _user.value = response.toUser()
                _isAuthenticated.value = _user.value != null
            } else {
                logout()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logout()
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = api.login(mapOf("username" to username, "password" to password))

            if (response.success && response.token != null && response.user != null) {
                saveToken(response.token)
                _user.value = response.user
                _isAuthenticated.value = true
                Result.success(response.user)
            } else {
                Result.failure(Exception(response.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Connection error: ${e.message}"))
        }
    }

    suspend fun register(request: RegisterRequest): Result<Pair<User, String?>> {
        return try {
            val response = api.register(request)

            if (response.success && response.token != null && response.user != null) {
                saveToken(response.token)
                _user.value = response.user
                _isAuthenticated.value = true
                Result.success(Pair(response.user, response.message))
            } else {
                Result.failure(Exception(response.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Connection error: ${e.message}"))
        }
    }

    fun logout() {
        clearToken()
        _user.value = null
        _isAuthenticated.value = false
    }
}
