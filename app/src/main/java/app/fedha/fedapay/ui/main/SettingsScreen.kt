package app.fedha.fedapay.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.fedha.fedapay.data.models.User
import app.fedha.fedapay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: User?,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
        ) {
            // User Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (user?.fullName ?: "U").first().uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Background
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    user?.fullName ?: "User",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    user?.email ?: "",
                    fontSize = 14.sp,
                    color = SecondaryText
                )
            }

            // Settings Sections
            SettingsSection("Account") {
                SettingsRow(icon = "👤", label = "Profile", value = user?.fullName ?: user?.username)
                SettingsRow(icon = "📧", label = "Email", value = user?.email)
                SettingsRow(icon = "💼", label = "Merchant ID", value = user?.merchantSource ?: "N/A")
            }

            SettingsSection("App Settings") {
                SettingsRow(icon = "🔔", label = "Notifications", value = "Enabled", onClick = { })
                if (user?.isMerchant == true) {
                    SettingsRow(icon = "📡", label = "NFC Settings", value = "Auto", onClick = { })
                }
                SettingsRow(icon = "🌐", label = "Open Web Dashboard", isAction = true) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fedha.app/dashboard.php"))
                    context.startActivity(intent)
                }
            }

            SettingsSection("Support") {
                SettingsRow(icon = "❓", label = "Help & FAQ", isAction = true) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fedha.app/help"))
                    context.startActivity(intent)
                }
                SettingsRow(icon = "📞", label = "Contact Support", isAction = true) {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@fedha.app")
                    }
                    context.startActivity(intent)
                }
                SettingsRow(icon = "📋", label = "Terms of Service", isAction = true) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fedha.app/terms"))
                    context.startActivity(intent)
                }
                SettingsRow(icon = "🔒", label = "Privacy Policy", isAction = true) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fedha.app/privacy"))
                    context.startActivity(intent)
                }
            }

            // App Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("FedaPay Merchant", fontSize = 14.sp, color = SecondaryText)
                Text("Version 1.0.0", fontSize = 12.sp, color = SecondaryText.copy(alpha = 0.7f))
            }

            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Sign Out", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = CardBackground,
            titleContentColor = Color.White,
            textContentColor = SecondaryText
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = SecondaryText,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    label: String,
    value: String? = null,
    isAction: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            label,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        value?.let {
            Text(
                it,
                fontSize = 14.sp,
                color = SecondaryText,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        if (onClick != null || isAction) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
