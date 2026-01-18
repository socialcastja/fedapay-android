package app.fedha.fedapay.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.fedha.fedapay.data.models.DashboardStats
import app.fedha.fedapay.data.models.Transaction
import app.fedha.fedapay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String,
    isMerchant: Boolean,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
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
                // Welcome Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Welcome back,", fontSize = 14.sp, color = SecondaryText)
                        Text(userName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            userName.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Background
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Accent)
                    }
                }
            } else if (uiState.error != null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(uiState.error!!, color = SecondaryText)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.loadDashboard() }) {
                            Text("Retry", color = Accent)
                        }
                    }
                }
            } else {
                uiState.stats?.let { stats ->
                    // Stats Cards (Merchant only)
                    if (isMerchant) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = "💰",
                                    title = "Today's Sales",
                                    value = String.format("%.2f FTK", stats.todaySales)
                                )
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = "📊",
                                    title = "Transactions",
                                    value = stats.totalTransactions.toString()
                                )
                            }
                        }
                    }

                    // Wallet Balance Card
                    item {
                        WalletBalanceCard(stats.walletBalance, isMerchant)
                    }

                    // Quick Actions for Merchants
                    if (isMerchant) {
                        item {
                            Column {
                                Text(
                                    "Quick Actions",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    QuickActionCard(
                                        modifier = Modifier.weight(1f),
                                        icon = "📡",
                                        title = "NFC Payment",
                                        subtitle = "Tap to Pay"
                                    )
                                    QuickActionCard(
                                        modifier = Modifier.weight(1f),
                                        icon = "📷",
                                        title = "Scan QR",
                                        subtitle = "Quick Scan"
                                    )
                                }
                            }
                        }
                    }

                    // Recent Transactions
                    item {
                        Text(
                            "Recent Transactions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    if (stats.recentTransactions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📋", fontSize = 40.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No transactions yet", color = SecondaryText)
                                }
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
                                    stats.recentTransactions.take(5).forEachIndexed { index, transaction ->
                                        TransactionRow(transaction)
                                        if (index < stats.recentTransactions.take(5).lastIndex) {
                                            HorizontalDivider(color = DividerColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, color = SecondaryText)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun WalletBalanceCard(balance: Double, isMerchant: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Wallet Balance", fontSize = 14.sp, color = SecondaryText)
                    Text(
                        String.format("%.2f FTK", balance),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text("🪙", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(Modifier.weight(1f), "⬆️", "Send")
                ActionButton(Modifier.weight(1f), "⬇️", "Receive")
                if (isMerchant) {
                    ActionButton(Modifier.weight(1f), "💳", "Charge")
                }
            }
        }
    }
}

@Composable
private fun ActionButton(modifier: Modifier = Modifier, icon: String, label: String) {
    Button(
        onClick = { },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Background),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Text(label, fontSize = 12.sp, color = Color.White)
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    subtitle: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(subtitle, fontSize = 12.sp, color = SecondaryText)
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (transaction.isIncoming) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (transaction.isIncoming) "↓" else "↑", color = if (transaction.isIncoming) SuccessGreen else ErrorRed)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.description ?: transaction.type.replaceFirstChar { it.uppercase() },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(transaction.createdAt, fontSize = 12.sp, color = SecondaryText)
        }

        Text(
            "${if (transaction.isIncoming) "+" else "-"}${String.format("%.2f", transaction.amount)} FTK",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (transaction.isIncoming) SuccessGreen else ErrorRed
        )
    }
}
