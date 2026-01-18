package app.fedha.fedapay.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.fedha.fedapay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var step by remember { mutableStateOf(1) }
    var accountType by remember { mutableStateOf("user") }

    // Form fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            errorMessage = it
            showError = true
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.registrationMessage) {
        uiState.registrationMessage?.let {
            showSuccess = true
        }
    }

    val passwordStrength = remember(password) {
        if (password.isEmpty()) Triple(0, "", SecondaryText)
        else {
            var strength = 0
            if (password.length >= 8) strength++
            if (password.any { it.isUpperCase() }) strength++
            if (password.any { it.isDigit() }) strength++
            if (password.any { !it.isLetterOrDigit() }) strength++

            when {
                strength < 2 -> Triple(1, "Weak", ErrorRed)
                strength < 4 -> Triple(2, "Medium", Accent)
                else -> Triple(3, "Strong", SuccessGreen)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step == 1) onNavigateBack() else step = 1
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            if (step == 1) {
                // Step 1: Account Type Selection
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🪙", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Create Account",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        "Choose how you want to use FedaPay",
                        fontSize = 14.sp,
                        color = SecondaryText
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Account Type Cards
                AccountTypeCard(
                    icon = "👤",
                    title = "Customer",
                    description = "Buy products & pay with FTK tokens",
                    features = listOf("Buy from merchants", "Send & receive tokens", "Trade FTK tokens"),
                    isSelected = accountType == "user",
                    onClick = { accountType = "user" }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AccountTypeCard(
                    icon = "🏪",
                    title = "Merchant",
                    description = "Sell products & accept payments",
                    features = listOf("Create your shop", "Accept card & FTK", "POS & NFC payments"),
                    isSelected = accountType == "merchant",
                    onClick = { accountType = "merchant" }
                )

                Spacer(modifier = Modifier.height(24.dp))

                WelcomeBonusBanner()

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { step = 2 },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Background)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Already have an account? ", color = SecondaryText)
                    Text("Sign In", color = Accent, fontWeight = FontWeight.SemiBold)
                }

            } else {
                // Step 2: Account Details
                Row(
                    modifier = Modifier
                        .background(CardBackground, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (accountType == "merchant") "🏪" else "👤", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (accountType == "merchant") "Merchant" else "Customer",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Your Details",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Form Fields
                FormField("Full Name *", "John Doe", fullName) { fullName = it }
                FormField("Email Address *", "john@example.com", email, KeyboardType.Email) { email = it }
                FormField("Phone Number", "+1 876 555 0000", phone, KeyboardType.Phone) { phone = it }

                if (accountType == "merchant") {
                    FormField("Business Name *", "Your Business Name", companyName) { companyName = it }
                }

                FormField("City", "Kingston", city) { city = it }

                // Password Field with Strength
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Password *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = SecondaryText)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Min. 8 characters", color = SecondaryText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = CardBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    if (password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { passwordStrength.first / 3f },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = passwordStrength.third,
                            trackColor = CardBackground
                        )
                        Text(passwordStrength.second, fontSize = 12.sp, color = passwordStrength.third)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Confirm Password
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Confirm Password *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = SecondaryText)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Re-enter password", color = SecondaryText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = CardBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text("Passwords do not match", fontSize = 12.sp, color = ErrorRed)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                WelcomeBonusBanner()

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { step = 1 },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Back")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            // Validation
                            when {
                                fullName.isBlank() -> {
                                    errorMessage = "Please enter your full name"
                                    showError = true
                                }
                                email.isBlank() || !email.contains("@") -> {
                                    errorMessage = "Please enter a valid email"
                                    showError = true
                                }
                                password.length < 8 -> {
                                    errorMessage = "Password must be at least 8 characters"
                                    showError = true
                                }
                                password != confirmPassword -> {
                                    errorMessage = "Passwords do not match"
                                    showError = true
                                }
                                accountType == "merchant" && companyName.isBlank() -> {
                                    errorMessage = "Please enter your business name"
                                    showError = true
                                }
                                else -> {
                                    viewModel.register(
                                        accountType,
                                        fullName,
                                        email.lowercase(),
                                        password,
                                        phone,
                                        companyName,
                                        city
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(2f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Background, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Create Account", fontWeight = FontWeight.Bold, color = Background)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Already have an account? ", color = SecondaryText)
                    Text("Sign In", color = Accent, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Error Dialog
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK", color = Accent)
                }
            },
            containerColor = CardBackground,
            titleContentColor = Color.White,
            textContentColor = SecondaryText
        )
    }

    // Success Dialog
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {
                showSuccess = false
                viewModel.clearRegistrationMessage()
            },
            title = { Text("Welcome!") },
            text = { Text(uiState.registrationMessage ?: "Account created successfully!") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccess = false
                    viewModel.clearRegistrationMessage()
                }) {
                    Text("Get Started", color = Accent)
                }
            },
            containerColor = CardBackground,
            titleContentColor = Color.White,
            textContentColor = SecondaryText
        )
    }
}

@Composable
private fun AccountTypeCard(
    icon: String,
    title: String,
    description: String,
    features: List<String>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, Accent, RoundedCornerShape(16.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CardBackgroundSelected else CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(icon, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(description, fontSize = 14.sp, color = SecondaryText)
            Spacer(modifier = Modifier.height(12.dp))
            features.forEach { feature ->
                Text("✓ $feature", fontSize = 13.sp, color = SecondaryText)
            }
        }
    }
}

@Composable
private fun WelcomeBonusBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BonusBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎁", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Welcome Bonus!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BonusTitleColor)
                Text("Get 10 FTK free when you create your account", fontSize = 12.sp, color = BonusTextColor)
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    placeholder: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = SecondaryText)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = SecondaryText) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedBorderColor = Accent,
                unfocusedBorderColor = CardBackground,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
    Spacer(modifier = Modifier.height(18.dp))
}
