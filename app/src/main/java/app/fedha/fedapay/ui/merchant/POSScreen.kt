package app.fedha.fedapay.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.fedha.fedapay.ui.theme.*

enum class PaymentMethod(val display: String, val icon: String) {
    FTK("FTK Token", "🪙"),
    NFC("NFC Tap", "📡"),
    QR("QR Code", "📷")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen() {
    var amount by remember { mutableStateOf("0.00") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.FTK) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    val keypadButtons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Point of Sale", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            // Amount Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Amount to Charge", fontSize = 14.sp, color = SecondaryText)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        amount,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "FTK",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Payment Method Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentMethod.entries.forEach { method ->
                    PaymentMethodButton(
                        modifier = Modifier.weight(1f),
                        method = method,
                        isSelected = selectedMethod == method,
                        onClick = { selectedMethod = method }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Keypad
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                keypadButtons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { key ->
                            KeypadButton(
                                modifier = Modifier.weight(1f),
                                key = key,
                                onClick = { handleKeyPress(key, amount) { amount = it } }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Charge Button
            val amountValue = amount.toDoubleOrNull() ?: 0.0
            Button(
                onClick = { showPaymentSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (amountValue > 0) Accent else CardBackground,
                    disabledContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = amountValue > 0
            ) {
                Text(
                    "💳 Charge $amount FTK",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (amountValue > 0) Background else SecondaryText
                )
            }
        }
    }

    // Payment Sheet
    if (showPaymentSheet) {
        PaymentProcessSheet(
            amount = amount.toDoubleOrNull() ?: 0.0,
            paymentMethod = selectedMethod,
            onDismiss = { showPaymentSheet = false },
            onSuccess = {
                amount = "0.00"
                showPaymentSheet = false
            }
        )
    }
}

@Composable
private fun PaymentMethodButton(
    modifier: Modifier = Modifier,
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, Accent, RoundedCornerShape(12.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Accent.copy(alpha = 0.3f) else CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(method.icon, fontSize = 24.sp)
            Text(
                method.display,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else SecondaryText
            )
        }
    }
}

@Composable
private fun KeypadButton(
    modifier: Modifier = Modifier,
    key: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(64.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                key,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

private fun handleKeyPress(key: String, currentAmount: String, setAmount: (String) -> Unit) {
    when (key) {
        "⌫" -> {
            val newAmount = if (currentAmount.length > 1) {
                currentAmount.dropLast(1)
            } else {
                "0.00"
            }
            setAmount(if (newAmount.isEmpty() || newAmount == "0") "0.00" else newAmount)
        }
        "." -> {
            if (!currentAmount.contains(".")) {
                setAmount(currentAmount + ".")
            }
        }
        else -> {
            val newAmount = if (currentAmount == "0.00") {
                key
            } else if (currentAmount.contains(".")) {
                val parts = currentAmount.split(".")
                if (parts.size == 1 || parts[1].length < 2) {
                    currentAmount + key
                } else {
                    currentAmount
                }
            } else if (currentAmount.length < 8) {
                currentAmount + key
            } else {
                currentAmount
            }
            setAmount(newAmount)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentProcessSheet(
    amount: Double,
    paymentMethod: PaymentMethod,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var customerWallet by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Charging", fontSize = 16.sp, color = SecondaryText)
            Text(
                String.format("%.2f FTK", amount),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text("via ${paymentMethod.display}", fontSize = 14.sp, color = Accent)

            Spacer(modifier = Modifier.height(32.dp))

            when (paymentMethod) {
                PaymentMethod.FTK -> {
                    OutlinedTextField(
                        value = customerWallet,
                        onValueChange = { customerWallet = it },
                        label = { Text("Customer Wallet Address or @merchantid") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = CardBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = SecondaryText,
                            unfocusedLabelColor = SecondaryText
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isProcessing = true
                            // TODO: Process payment
                            onSuccess()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing && customerWallet.isNotBlank()
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Background, modifier = Modifier.size(24.dp))
                        } else {
                            Text("✓ Confirm Payment", fontWeight = FontWeight.Bold, color = Background)
                        }
                    }
                }

                PaymentMethod.NFC -> {
                    Text("📡", fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ready to receive payment", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text("Ask customer to tap their device", fontSize = 14.sp, color = SecondaryText)

                    if (isProcessing) {
                        Spacer(modifier = Modifier.height(20.dp))
                        CircularProgressIndicator(color = Accent)
                        Text("Waiting for NFC...", fontSize = 14.sp, color = Accent)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { isProcessing = !isProcessing },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (isProcessing) "Cancel" else "📡 Start NFC",
                            fontWeight = FontWeight.Bold,
                            color = Background
                        )
                    }
                }

                PaymentMethod.QR -> {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📱", fontSize = 100.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Customer scans to pay", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    Text("QR code expires in 5 minutes", fontSize = 12.sp, color = SecondaryText)

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { /* Generate new QR */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔄 Generate New QR", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
