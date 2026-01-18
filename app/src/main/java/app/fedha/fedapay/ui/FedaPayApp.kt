package app.fedha.fedapay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.fedha.fedapay.ui.auth.AuthViewModel
import app.fedha.fedapay.ui.auth.LoginScreen
import app.fedha.fedapay.ui.auth.RegisterScreen
import app.fedha.fedapay.ui.main.DashboardScreen
import app.fedha.fedapay.ui.main.SettingsScreen
import app.fedha.fedapay.ui.main.WalletScreen
import app.fedha.fedapay.ui.merchant.POSScreen
import app.fedha.fedapay.ui.merchant.QRScannerScreen
import app.fedha.fedapay.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object POS : Screen("pos", "POS", Icons.Default.PointOfSale)
    object NFC : Screen("nfc", "NFC", Icons.Default.Nfc)
    object QR : Screen("qr", "QR", Icons.Default.QrCodeScanner)
    object Wallet : Screen("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun FedaPayApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val user by authViewModel.user.collectAsState()

    if (isLoading) {
        LoadingScreen()
    } else if (!isAuthenticated) {
        AuthNavigation(authViewModel)
    } else {
        val isMerchant = user?.isMerchant == true
        MainNavigation(
            userName = user?.fullName ?: "User",
            isMerchant = isMerchant,
            user = user,
            onLogout = { authViewModel.logout() }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Accent)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading FedaPay...", color = Color.White)
        }
    }
}

@Composable
private fun AuthNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                viewModel = authViewModel
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainNavigation(
    userName: String,
    isMerchant: Boolean,
    user: app.fedha.fedapay.data.models.User?,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    val merchantTabs = listOf(
        Screen.Dashboard,
        Screen.POS,
        Screen.NFC,
        Screen.QR,
        Screen.Wallet
    )

    val userTabs = listOf(
        Screen.Dashboard,
        Screen.QR,
        Screen.Wallet,
        Screen.Settings
    )

    val tabs = if (isMerchant) merchantTabs else userTabs

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Background,
                contentColor = Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontSize = 10.sp) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent,
                            selectedTextColor = Accent,
                            unselectedIconColor = SecondaryText,
                            unselectedTextColor = SecondaryText,
                            indicatorColor = Background
                        )
                    )
                }
            }
        },
        containerColor = Background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    userName = userName,
                    isMerchant = isMerchant,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.POS.route) {
                POSScreen()
            }

            composable(Screen.NFC.route) {
                // NFC Screen - similar to POS with NFC focus
                POSScreen() // Reuse POS with NFC selected
            }

            composable(Screen.QR.route) {
                QRScannerScreen(isMerchant = isMerchant)
            }

            composable(Screen.Wallet.route) {
                WalletScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    user = user,
                    onLogout = onLogout
                )
            }
        }
    }
}
