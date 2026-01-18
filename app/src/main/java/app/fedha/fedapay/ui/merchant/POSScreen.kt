package app.fedha.fedapay.ui.merchant

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.fedha.fedapay.ui.theme.*

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val ftkPrice: Double,
    val category: String,
    val image: String? = null,
    val stock: Int = 99
)

data class CartItem(
    val product: Product,
    var quantity: Int
)

enum class Currency(val symbol: String, val icon: String) {
    FTK("FTK", "🪙"),
    JMD("J$", "🇯🇲")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    var currency by remember { mutableStateOf(Currency.FTK) }
    var cart by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var customerName by remember { mutableStateOf("") }
    var salesRep by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var showManualEntry by remember { mutableStateOf(false) }
    var manualAmount by remember { mutableStateOf("") }
    var showQRScreen by remember { mutableStateOf(false) }

    // Sample products - in real app, load from API
    val products = remember {
        listOf(
            Product(1, "Coffee", 350.0, 3.50, "Beverages"),
            Product(2, "Sandwich", 750.0, 7.50, "Food"),
            Product(3, "Pastry", 450.0, 4.50, "Food"),
            Product(4, "Juice", 300.0, 3.00, "Beverages"),
            Product(5, "Salad", 650.0, 6.50, "Food"),
            Product(6, "Water", 150.0, 1.50, "Beverages"),
        )
    }

    val categories = remember { listOf("all") + products.map { it.category }.distinct() }

    val filteredProducts = products.filter { product ->
        (selectedCategory == "all" || product.category == selectedCategory) &&
                (searchQuery.isEmpty() || product.name.lowercase().contains(searchQuery.lowercase()))
    }

    fun addToCart(product: Product) {
        val existing = cart.find { it.product.id == product.id }
        cart = if (existing != null) {
            cart.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it }
        } else {
            cart + CartItem(product, 1)
        }
    }

    fun updateQuantity(productId: Int, delta: Int) {
        cart = cart.mapNotNull { item ->
            if (item.product.id == productId) {
                val newQty = item.quantity + delta
                if (newQty > 0) item.copy(quantity = newQty) else null
            } else item
        }
    }

    fun removeFromCart(productId: Int) {
        cart = cart.filter { it.product.id != productId }
    }

    fun clearCart() {
        cart = emptyList()
        customerName = ""
        manualAmount = ""
    }

    fun getCartTotal(): Double {
        return cart.sumOf { item ->
            val price = if (currency == Currency.FTK) item.product.ftkPrice else item.product.price
            price * item.quantity
        }
    }

    fun getCartItemCount(): Int = cart.sumOf { it.quantity }

    // QR Payment Screen
    if (showQRScreen) {
        QRPaymentScreen(
            amount = if (showManualEntry) manualAmount.toDoubleOrNull() ?: 0.0 else getCartTotal(),
            currency = currency,
            customerName = customerName,
            onCancel = { showQRScreen = false },
            onNewSale = {
                showQRScreen = false
                clearCart()
            }
        )
        return
    }

    // Tablet/Landscape: Split view with Products on left, Cart on right
    if (isTablet || isLandscape) {
        Row(modifier = Modifier.fillMaxSize().background(Background)) {
            // Left Panel - Products
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                // Header with currency toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Products", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    CurrencyToggle(currency) { currency = it }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search products...", color = SecondaryText) },
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
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Categories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        CategoryChip(
                            text = if (cat == "all") "All" else cat,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                    CategoryChip(
                        text = "💰 Manual",
                        isSelected = showManualEntry,
                        onClick = { showManualEntry = !showManualEntry },
                        isManual = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Products Grid or Manual Entry
                if (showManualEntry) {
                    ManualEntryView(
                        amount = manualAmount,
                        currency = currency,
                        onAmountChange = { manualAmount = it }
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProducts) { product ->
                            ProductCard(
                                product = product,
                                currency = currency,
                                onClick = { addToCart(product) }
                            )
                        }
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(DividerColor)
            )

            // Right Panel - Cart
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(CardBackground.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                // Cart Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cart (${getCartItemCount()})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    if (cart.isNotEmpty()) {
                        TextButton(onClick = { clearCart() }) {
                            Text("Clear", color = ErrorRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Customer Info
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    placeholder = { Text("Customer name (optional)", color = SecondaryText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = CardBackground,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = salesRep,
                    onValueChange = { salesRep = it },
                    placeholder = { Text("Sales rep (optional)", color = SecondaryText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = CardBackground,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cart Items
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (cart.isEmpty() && !showManualEntry) {
                        item {
                            Text(
                                "Tap products to add",
                                color = SecondaryText,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (showManualEntry) {
                        item {
                            Text(
                                "Using manual entry",
                                color = SecondaryText,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(cart) { item ->
                            CartItemRow(
                                item = item,
                                currency = currency,
                                onIncrease = { updateQuantity(item.product.id, 1) },
                                onDecrease = { updateQuantity(item.product.id, -1) },
                                onRemove = { removeFromCart(item.product.id) }
                            )
                        }
                    }
                }

                // Total Section
                Divider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", color = SecondaryText)
                    Text(
                        "${currency.icon} ${String.format("%.2f", if (showManualEntry) manualAmount.toDoubleOrNull() ?: 0.0 else getCartTotal())}",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "${currency.icon} ${String.format("%.2f", if (showManualEntry) manualAmount.toDoubleOrNull() ?: 0.0 else getCartTotal())}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Charge Button
                val total = if (showManualEntry) manualAmount.toDoubleOrNull() ?: 0.0 else getCartTotal()
                Button(
                    onClick = { showQRScreen = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (total > 0) Accent else CardBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = total > 0
                ) {
                    Text(
                        "Charge ${currency.icon} ${String.format("%.2f", total)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (total > 0) Background else SecondaryText
                    )
                }
            }
        }
    } else {
        // Phone Portrait Layout
        PhonePOSLayout(
            currency = currency,
            onCurrencyChange = { currency = it },
            showManualEntry = showManualEntry,
            onToggleManualEntry = { showManualEntry = !showManualEntry },
            manualAmount = manualAmount,
            onManualAmountChange = { manualAmount = it },
            products = filteredProducts,
            cart = cart,
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            onAddToCart = { addToCart(it) },
            onCharge = { showQRScreen = true },
            getCartTotal = { getCartTotal() },
            getCartItemCount = { getCartItemCount() }
        )
    }
}

@Composable
private fun CurrencyToggle(currency: Currency, onToggle: (Currency) -> Unit) {
    Card(
        modifier = Modifier.clickable {
            onToggle(if (currency == Currency.FTK) Currency.JMD else Currency.FTK)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (currency == Currency.FTK) Accent else CardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            "${currency.icon} ${currency.symbol}",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isManual: Boolean = false
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isManual -> SuccessGreen
                isSelected -> Accent
                else -> CardBackground
            }
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ProductCard(product: Product, currency: Currency, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Background, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("📦", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                product.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                if (currency == Currency.FTK) "🪙 ${String.format("%.2f", product.ftkPrice)}"
                else "J$${String.format("%.2f", product.price)}",
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            if (product.stock < 10) {
                Text("${product.stock} left", color = ErrorRed, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    currency: Currency,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.product.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (currency == Currency.FTK) "🪙 ${String.format("%.2f", item.product.ftkPrice)}"
                    else "J$${String.format("%.2f", item.product.price)}",
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }

            // Quantity controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(28.dp).background(Background, CircleShape)
                ) {
                    Text("-", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    "${item.quantity}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(28.dp).background(Background, CircleShape)
                ) {
                    Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Text("✕", color = ErrorRed, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ManualEntryView(amount: String, currency: Currency, onAmountChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter Amount", color = SecondaryText)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "${currency.icon} ${currency.symbol} ${amount.ifEmpty { "0.00" }}",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(30.dp))

        // Numpad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", "⌫")
        )

        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                row.forEach { key ->
                    Card(
                        modifier = Modifier
                            .size(70.dp)
                            .clickable {
                                when (key) {
                                    "⌫" -> onAmountChange(amount.dropLast(1))
                                    "." -> if (!amount.contains(".")) onAmountChange(amount + ".")
                                    else -> {
                                        if (amount.contains(".")) {
                                            val parts = amount.split(".")
                                            if (parts.size < 2 || parts[1].length < 2) {
                                                onAmountChange(amount + key)
                                            }
                                        } else if (amount.length < 8) {
                                            onAmountChange(amount + key)
                                        }
                                    }
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(key, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhonePOSLayout(
    currency: Currency,
    onCurrencyChange: (Currency) -> Unit,
    showManualEntry: Boolean,
    onToggleManualEntry: () -> Unit,
    manualAmount: String,
    onManualAmountChange: (String) -> Unit,
    products: List<Product>,
    cart: List<CartItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onCharge: () -> Unit,
    getCartTotal: () -> Double,
    getCartItemCount: () -> Int
) {
    Scaffold(containerColor = Background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CurrencyToggle(currency, onCurrencyChange)
                Card(
                    modifier = Modifier.clickable(onClick = onToggleManualEntry),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        if (showManualEntry) "📦 Products" else "💰 Manual",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White
                    )
                }
            }

            if (showManualEntry) {
                ManualEntryView(manualAmount, currency, onManualAmountChange)
            } else {
                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search products...", color = SecondaryText) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = CardBackground,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Products Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(products) { product ->
                        ProductCard(product, currency) { onAddToCart(product) }
                    }
                }
            }

            // Cart Summary Bar
            val total = if (showManualEntry) manualAmount.toDoubleOrNull() ?: 0.0 else getCartTotal()
            if (cart.isNotEmpty() || (showManualEntry && total > 0)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (showManualEntry) "Manual Entry" else "${getCartItemCount()} items",
                            color = SecondaryText,
                            fontSize = 12.sp
                        )
                        Text(
                            "${currency.icon} ${String.format("%.2f", total)}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onCharge,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Charge", fontWeight = FontWeight.Bold, color = Background)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QRPaymentScreen(
    amount: Double,
    currency: Currency,
    customerName: String,
    onCancel: () -> Unit,
    onNewSale: () -> Unit
) {
    Scaffold(containerColor = Background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Scan to Pay", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Customer can scan QR or tap NFC", fontSize = 14.sp, color = SecondaryText)

            Spacer(modifier = Modifier.height(24.dp))

            // QR Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // QR Placeholder - replace with actual QR generation
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📱", fontSize = 120.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "${currency.icon} ${currency.symbol} ${String.format("%.2f", amount)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Background
                    )

                    if (customerName.isNotEmpty()) {
                        Text("Customer: $customerName", fontSize = 14.sp, color = SecondaryText)
                    }

                    Text("Code: PAY-XXXX", fontSize = 12.sp, color = SecondaryText)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Button(
                onClick = { /* Share */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📤 Share Payment Link", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* NFC */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📡 Waiting for NFC...", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onNewSale) {
                Text("Cancel & New Sale", color = ErrorRed, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
