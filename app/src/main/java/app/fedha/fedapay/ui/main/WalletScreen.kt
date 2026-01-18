package app.fedha.fedapay.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.fedha.fedapay.data.models.Transaction
import app.fedha.fedapay.data.models.Wallet
import app.fedha.fedapay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSendSheet by remember { mutableStateOf(false) }
    var showReceiveSheet by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.loadWallet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Wallet Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CardBackground, CardBackgroundSelected)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙", fontSize = 48.sp)

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("FTK Balance", fontSize = 14.sp, color = SecondaryText)
                                    Text(
                                        String.format("%.2f", uiState.wallet?.balance ?: 0.0),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Wallet Address
                            uiState.wallet?.let { wallet ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Address: ", fontSize = 12.sp, color = SecondaryText)
                                    Text(
                                        wallet.walletAddress.take(12) + "..." + wallet.walletAddress.takeLast(8),
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(wallet.walletAddress))
                                        }
                                    ) {
                                        Text("📋", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showSendSheet = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("⬆️ Send", fontWeight = FontWeight.SemiBold, color = Background)
                                }

                                Button(
                                    onClick = { showReceiveSheet = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CardBackgroundSelected),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("⬇️ Receive", fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Transaction History
            item {
                Text(
                    "Transaction History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Accent)
                    }
                }
            } else if (uiState.transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No transactions yet", fontSize = 16.sp, color = SecondaryText)
                        Text(
                            "Your transaction history will appear here",
                            fontSize = 14.sp,
                            color = SecondaryText.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            uiState.transactions.forEachIndexed { index, transaction ->
                                TransactionDetailRow(transaction)
                                if (index < uiState.transactions.lastIndex) {
                                    Divider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = DividerColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // Send Sheet
    if (showSendSheet) {
        SendTokensSheet(
            balance = uiState.wallet?.balance ?: 0.0,
            onDismiss = { showSendSheet = false },
            onSend = { address, amount, description ->
                viewModel.transfer(address, amount, description)
                showSendSheet = false
            }
        )
    }

    // Receive Sheet
    if (showReceiveSheet) {
        ReceiveTokensSheet(
            walletAddress = uiState.wallet?.walletAddress ?: "",
            onDismiss = { showReceiveSheet = false }
        )
    }
}

@Composable
private fun TransactionDetailRow(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (transaction.isIncoming) SuccessGreen.copy(alpha = 0.2f)
                    else ErrorRed.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (transaction.isIncoming) "↓" else "↑",
                fontSize = 20.sp,
                color = if (transaction.isIncoming) SuccessGreen else ErrorRed
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.description ?: transaction.type.replaceFirstChar { it.uppercase() },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(transaction.createdAt, fontSize = 12.sp, color = SecondaryText)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${if (transaction.isIncoming) "+" else "-"}${String.format("%.2f", transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isIncoming) SuccessGreen else ErrorRed
            )
            Text("FTK", fontSize = 12.sp, color = SecondaryText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendTokensSheet(
    balance: Double,
    onDismiss: () -> Unit,
    onSend: (String, Double, String?) -> Unit
) {
    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Send Tokens", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Available: ${String.format("%.2f", balance)} FTK", fontSize = 14.sp, color = SecondaryText)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = recipientAddress,
                onValueChange = { recipientAddress = it },
                label = { Text("Recipient Address or @merchantid") },
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (FTK)") },
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
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
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (recipientAddress.isNotBlank() && amountValue > 0) {
                        onSend(recipientAddress, amountValue, description.takeIf { it.isNotBlank() })
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
                enabled = recipientAddress.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("✈️ Send FTK", fontWeight = FontWeight.Bold, color = Background)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiveTokensSheet(
    walletAddress: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

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
            Text("Receive Tokens", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Spacer(modifier = Modifier.height(32.dp))

            // QR Code Placeholder
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("📱", fontSize = 100.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Your Wallet Address", fontSize = 14.sp, color = SecondaryText)

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    walletAddress,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { clipboardManager.setText(AnnotatedString(walletAddress)) }
            ) {
                Text("📋 Copy Address", color = Accent, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
