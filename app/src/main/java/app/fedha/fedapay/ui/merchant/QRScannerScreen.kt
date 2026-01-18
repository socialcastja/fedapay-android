package app.fedha.fedapay.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.fedha.fedapay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    isMerchant: Boolean
) {
    var torchOn by remember { mutableStateOf(false) }
    var showPaymentConfirm by remember { mutableStateOf(false) }
    var scannedAmount by remember { mutableStateOf(0.0) }
    var scannedAddress by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isMerchant) "Scan QR" else "Pay", fontWeight = FontWeight.Bold) },
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
        ) {
            // Camera Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Camera preview would go here
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📷", fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Camera Preview", color = SecondaryText)
                }

                // Scan Frame
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    // Corner brackets would be drawn here
                }

                // Torch Toggle
                IconButton(
                    onClick = { torchOn = !torchOn },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(50))
                ) {
                    Icon(
                        if (torchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle flash",
                        tint = Color.White
                    )
                }
            }

            // Bottom Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .background(Background)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Scan QR Code",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    if (isMerchant) "Scan customer's payment QR code"
                    else "Scan merchant's QR code to pay",
                    fontSize = 14.sp,
                    color = SecondaryText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = { /* Show manual entry */ }) {
                    Text("⌨️ Enter Code Manually", color = Accent, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Demo: Simulate scan
                Button(
                    onClick = {
                        scannedAmount = 25.00
                        scannedAddress = "ftk_merchant_demo123"
                        showPaymentConfirm = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Demo: Simulate Scan", fontWeight = FontWeight.Bold, color = Background)
                }
            }
        }
    }

    // Payment Confirmation
    if (showPaymentConfirm) {
        PaymentConfirmDialog(
            amount = scannedAmount,
            recipientAddress = scannedAddress,
            onDismiss = { showPaymentConfirm = false },
            onConfirm = {
                // Process payment
                showPaymentConfirm = false
            }
        )
    }
}

@Composable
private fun PaymentConfirmDialog(
    amount: Double,
    recipientAddress: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Confirm Payment", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    String.format("%.2f FTK", amount),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Accent
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("To", fontSize = 14.sp, color = SecondaryText)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "${recipientAddress.take(20)}...",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isProcessing = true
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Background, modifier = Modifier.size(20.dp))
                } else {
                    Text("✓ Confirm & Pay", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryText)
            }
        }
    )
}
