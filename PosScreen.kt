package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CatalogItemEntity
import com.example.data.local.UserEntity
import com.example.model.CartItem
import com.example.model.CustomerLedgerEntry
import com.example.model.ItemCategory
import com.example.model.PaymentMethod
import com.example.model.UnitType
import com.example.model.UserRole
import com.example.model.VehicleServiceInfo
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopDangerRed
import com.example.ui.theme.LoopSuccessGreen
import com.example.ui.theme.LoopTealPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    catalogItems: List<CatalogItemEntity>,
    cartItems: List<CartItem>,
    vehicleInfo: VehicleServiceInfo,
    discountAmount: Double,
    taxRate: Double,
    selectedPaymentMethod: PaymentMethod,
    searchQuery: String,
    selectedCategory: ItemCategory?,
    currentUser: UserEntity?,
    customerLedgers: List<CustomerLedgerEntry> = emptyList(),
    onSearchChange: (String) -> Unit,
    onCategorySelect: (ItemCategory?) -> Unit,
    onOpenFractionalDialog: (CatalogItemEntity) -> Unit,
    onAddToCartQuick: (CatalogItemEntity) -> Unit,
    onUpdateQuantity: (catalogItemId: Long, Double) -> Unit,
    onRemoveFromCart: (catalogItemId: Long) -> Unit,
    onClearCart: () -> Unit,
    onSetDiscount: (Double) -> Unit,
    onSetPaymentMethod: (PaymentMethod) -> Unit,
    onOpenVehicleDialog: () -> Unit,
    onOpenBarcodeScanner: () -> Unit = {},
    onSelectCustomer: (CustomerLedgerEntry) -> Unit = {},
    onCompleteSale: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMobileCartSheetOpen by remember { mutableStateOf(false) }
    var isCustomerPickerOpen by remember { mutableStateOf(false) }

    val matchedCustomer = remember(vehicleInfo.customerName, vehicleInfo.customerPhone, customerLedgers) {
        customerLedgers.firstOrNull {
            (it.customerPhone.isNotBlank() && it.customerPhone == vehicleInfo.customerPhone) ||
                    (it.customerName.isNotBlank() && it.customerName.equals(vehicleInfo.customerName, ignoreCase = true))
        }
    }

    val subtotal = cartItems.sumOf { it.total }
    val discountedSubtotal = maxOf(0.0, subtotal - discountAmount)
    val tax = discountedSubtotal * taxRate
    val grandTotal = discountedSubtotal + tax

    val isOwner = currentUser?.role == UserRole.OWNER
    val isStaff = currentUser?.role == UserRole.STAFF

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth

        when {
            // Desktop / PC Wide View (> 960dp): 3-column / Expanded Dashboard with shortcuts
            screenWidth > 960.dp -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Column: Catalog Search & Filter Pane
                    Column(
                        modifier = Modifier
                            .weight(1.8f)
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        CatalogHeader(
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            onSearchChange = onSearchChange,
                            onCategorySelect = onCategorySelect,
                            onOpenCustomerSearch = { isCustomerPickerOpen = true },
                            onOpenBarcodeScanner = onOpenBarcodeScanner
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(catalogItems, key = { it.id }) { item ->
                                CatalogItemCard(
                                    item = item,
                                    onOpenFractional = { onOpenFractionalDialog(item) },
                                    onQuickAdd = { onAddToCartQuick(item) }
                                )
                            }
                        }
                    }

                    // Right Column: Active Invoice Terminal & Shortcuts
                    Surface(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        InvoiceDraftTerminal(
                            cartItems = cartItems,
                            vehicleInfo = vehicleInfo,
                            discountAmount = discountAmount,
                            taxRate = taxRate,
                            grandTotal = grandTotal,
                            selectedPaymentMethod = selectedPaymentMethod,
                            matchedCustomer = matchedCustomer,
                            isOwner = isOwner,
                            isStaff = isStaff,
                            showKeyboardShortcuts = true,
                            onUpdateQuantity = onUpdateQuantity,
                            onRemoveFromCart = onRemoveFromCart,
                            onClearCart = onClearCart,
                            onSetDiscount = onSetDiscount,
                            onSetPaymentMethod = onSetPaymentMethod,
                            onOpenVehicleDialog = onOpenVehicleDialog,
                            onOpenCustomerSearch = { isCustomerPickerOpen = true },
                            onCompleteSale = onCompleteSale
                        )
                    }
                }
            }

            // Tablet / iPad View (600dp - 960dp): 2-Column Split Pane
            screenWidth >= 600.dp -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Catalog Column
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .padding(12.dp)
                    ) {
                        CatalogHeader(
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            onSearchChange = onSearchChange,
                            onCategorySelect = onCategorySelect,
                            onOpenCustomerSearch = { isCustomerPickerOpen = true },
                            onOpenBarcodeScanner = onOpenBarcodeScanner
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(catalogItems, key = { it.id }) { item ->
                                CatalogItemCard(
                                    item = item,
                                    onOpenFractional = { onOpenFractionalDialog(item) },
                                    onQuickAdd = { onAddToCartQuick(item) }
                                )
                            }
                        }
                    }

                    // Invoice Column
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        InvoiceDraftTerminal(
                            cartItems = cartItems,
                            vehicleInfo = vehicleInfo,
                            discountAmount = discountAmount,
                            taxRate = taxRate,
                            grandTotal = grandTotal,
                            selectedPaymentMethod = selectedPaymentMethod,
                            matchedCustomer = matchedCustomer,
                            isOwner = isOwner,
                            isStaff = isStaff,
                            showKeyboardShortcuts = false,
                            onUpdateQuantity = onUpdateQuantity,
                            onRemoveFromCart = onRemoveFromCart,
                            onClearCart = onClearCart,
                            onSetDiscount = onSetDiscount,
                            onSetPaymentMethod = onSetPaymentMethod,
                            onOpenVehicleDialog = onOpenVehicleDialog,
                            onOpenCustomerSearch = { isCustomerPickerOpen = true },
                            onCompleteSale = onCompleteSale
                        )
                    }
                }
            }

            // Mobile View (< 600dp): Fast single-hand vertical flow with Bottom Sheet
            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        CatalogHeader(
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            onSearchChange = onSearchChange,
                            onCategorySelect = onCategorySelect,
                            onOpenCustomerSearch = { isCustomerPickerOpen = true },
                            onOpenBarcodeScanner = onOpenBarcodeScanner
                        )

                        // Vehicle Quick Chip Indicator
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = LoopTealPrimary.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenVehicleDialog() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = LoopTealPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = if (vehicleInfo.plateNumber.isNotBlank())
                                                "لوحة: ${vehicleInfo.plateNumber} (${vehicleInfo.vehicleModel.ifBlank { "سيارة" }})"
                                            else
                                                "إضافة بيانات المركبة والعميل والصيانة",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (matchedCustomer != null && matchedCustomer.remainingDebt > 0) {
                                            Text(
                                                text = "⚠️ عليه دين: ${String.format(Locale.US, "%,.0f", matchedCustomer.remainingDebt)} د.ع",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = LoopDangerRed
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (vehicleInfo.nextServiceDate.isNotBlank()) "📅 ${vehicleInfo.nextServiceDate}" else "تعديل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LoopTealPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Product Grid / List
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(catalogItems, key = { it.id }) { item ->
                                CatalogItemCard(
                                    item = item,
                                    onOpenFractional = { onOpenFractionalDialog(item) },
                                    onQuickAdd = { onAddToCartQuick(item) }
                                )
                            }
                        }
                    }

                    // Mobile Floating Bottom Cart Bar
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { isMobileCartSheetOpen = true }
                            ) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = LoopTealPrimary) {
                                            Text("${cartItems.size}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "السلة",
                                        tint = LoopTealPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "المجموع: ${String.format(Locale.US, "%.0f", grandTotal)} د.ع",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${cartItems.size} أصناف مختارة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (cartItems.isNotEmpty()) {
                                        isMobileCartSheetOpen = true
                                    }
                                },
                                enabled = cartItems.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("mobile_checkout_drawer_button")
                            ) {
                                Text("إتمام الفاتورة", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Mobile Modal Bottom Sheet for Invoice Checkout
                    if (isMobileCartSheetOpen) {
                        ModalBottomSheet(
                            onDismissRequest = { isMobileCartSheetOpen = false },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        ) {
                            InvoiceDraftTerminal(
                                cartItems = cartItems,
                                vehicleInfo = vehicleInfo,
                                discountAmount = discountAmount,
                                taxRate = taxRate,
                                grandTotal = grandTotal,
                                selectedPaymentMethod = selectedPaymentMethod,
                                matchedCustomer = matchedCustomer,
                                isOwner = isOwner,
                                isStaff = isStaff,
                                showKeyboardShortcuts = false,
                                onUpdateQuantity = onUpdateQuantity,
                                onRemoveFromCart = onRemoveFromCart,
                                onClearCart = onClearCart,
                                onSetDiscount = onSetDiscount,
                                onSetPaymentMethod = onSetPaymentMethod,
                                onOpenVehicleDialog = onOpenVehicleDialog,
                                onOpenCustomerSearch = {
                                    isMobileCartSheetOpen = false
                                    isCustomerPickerOpen = true
                                },
                                onCompleteSale = {
                                    isMobileCartSheetOpen = false
                                    onCompleteSale()
                                },
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Customer Picker Dialog
        if (isCustomerPickerOpen) {
            QuickCustomerPickerDialog(
                customers = customerLedgers,
                onDismiss = { isCustomerPickerOpen = false },
                onSelectCustomer = { customer ->
                    onSelectCustomer(customer)
                    isCustomerPickerOpen = false
                }
            )
        }
    }
}

@Composable
private fun CatalogHeader(
    searchQuery: String,
    selectedCategory: ItemCategory?,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (ItemCategory?) -> Unit,
    onOpenCustomerSearch: () -> Unit = {},
    onOpenBarcodeScanner: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Search Input, Barcode Scanner & Customer Search Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("بحث أو قراءة باركود...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("catalog_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = onOpenBarcodeScanner,
                colors = ButtonDefaults.buttonColors(containerColor = LoopAmberSecondary.copy(alpha = 0.2f), contentColor = LoopAmberSecondary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                modifier = Modifier.testTag("open_barcode_scanner_button")
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "مسح باركود", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("باركود 📷", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onOpenCustomerSearch,
                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary.copy(alpha = 0.15f), contentColor = LoopTealPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.PersonSearch, contentDescription = "بحث الزبائن", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("الزبائن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Categories Horizontal Bar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelect(null) },
                    label = { Text("الكل") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            items(ItemCategory.values()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelect(category) },
                    label = { Text(category.labelAr) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
private fun CatalogItemCard(
    item: CatalogItemEntity,
    onOpenFractional: () -> Unit,
    onQuickAdd: () -> Unit
) {
    val isLowStock = item.unitType != UnitType.SERVICE && item.stockQuantity <= item.minStockAlert

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) LoopDangerRed.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (item.unitType.isFractional) onOpenFractional() else onQuickAdd()
            }
            .testTag("catalog_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Category & Unit Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (item.unitType.isFractional) LoopTealPrimary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.unitType.isFractional) "كسور (${item.unitType.symbolAr})" else item.unitType.symbolAr,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (item.unitType.isFractional) LoopTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = item.code,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${String.format(Locale.US, "%.0f", item.salePrice)} د.ع",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = LoopTealPrimary
                    )
                    if (item.unitType != UnitType.SERVICE) {
                        Text(
                            text = if (isLowStock) "⚠️ متبقي: ${item.stockQuantity}" else "متوفر: ${item.stockQuantity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLowStock) LoopDangerRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (item.unitType.isFractional) onOpenFractional() else onQuickAdd()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(LoopTealPrimary)
                ) {
                    Icon(
                        imageVector = if (item.unitType.isFractional) Icons.Default.Opacity else Icons.Default.Add,
                        contentDescription = "إضافة",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InvoiceDraftTerminal(
    cartItems: List<CartItem>,
    vehicleInfo: VehicleServiceInfo,
    discountAmount: Double,
    taxRate: Double,
    grandTotal: Double,
    selectedPaymentMethod: PaymentMethod,
    matchedCustomer: CustomerLedgerEntry?,
    isOwner: Boolean,
    isStaff: Boolean,
    showKeyboardShortcuts: Boolean,
    onUpdateQuantity: (Long, Double) -> Unit,
    onRemoveFromCart: (Long) -> Unit,
    onClearCart: () -> Unit,
    onSetDiscount: (Double) -> Unit,
    onSetPaymentMethod: (PaymentMethod) -> Unit,
    onOpenVehicleDialog: () -> Unit,
    onOpenCustomerSearch: () -> Unit,
    onCompleteSale: () -> Unit,
    modifier: Modifier = Modifier
) {
    var discountInputText by remember { mutableStateOf(if (discountAmount > 0) discountAmount.toString() else "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = LoopTealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مسودة الفاتورة الحالية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (cartItems.isNotEmpty()) {
                    IconButton(onClick = onClearCart) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "مسح السلة", tint = LoopDangerRed)
                    }
                }
            }

            // Owner Restriction Warning Banner
            if (isOwner) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LoopAmberSecondary.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = LoopAmberSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "حساب المالك للمراجعة والتقارير - يُمنع إصدار الفواتير مباشرة لحماية الصندوق",
                            style = MaterialTheme.typography.labelSmall,
                            color = LoopAmberSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Customer Debt Warning Banner if matched customer has debt
            if (matchedCustomer != null && matchedCustomer.remainingDebt > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LoopDangerRed.copy(alpha = 0.12f))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = LoopDangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚠️ تنبيه: الزبون (${matchedCustomer.customerName}) لديه ديون سابقة بقيمة ${String.format(Locale.US, "%,.0f", matchedCustomer.remainingDebt)} د.ع",
                            style = MaterialTheme.typography.labelSmall,
                            color = LoopDangerRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Vehicle Service Card with quick Customer selector
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenVehicleDialog() }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚗 بيانات المركبة والعميل",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "اختيار زبون 🔍",
                                style = MaterialTheme.typography.labelSmall,
                                color = LoopTealPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onOpenCustomerSearch() }
                            )
                            Text(
                                text = "تعديل",
                                style = MaterialTheme.typography.labelSmall,
                                color = LoopTealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (vehicleInfo.plateNumber.isNotBlank() || vehicleInfo.customerName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "العميل: ${vehicleInfo.customerName.ifBlank { "عميل نقدي" }} | اللوحة: ${vehicleInfo.plateNumber.ifBlank { "-" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (vehicleInfo.nextServiceDate.isNotBlank()) {
                            Text(
                                text = "موعد الصيانة القادم: ${vehicleInfo.nextServiceDate} (عند ${vehicleInfo.nextServiceMileage} كم)",
                                style = MaterialTheme.typography.labelSmall,
                                color = LoopTealPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "اضغط هنا لإدخال اللوحة، رقم الجوال، وتعيين موعد الصيانة الدورية",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Items List
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "السلة فارغة، أضف المواد والخدمات من القائمة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cartItems.forEach { item ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.quantity} ${item.unitType.symbolAr} × ${String.format(Locale.US, "%.0f", item.unitPrice)} د.ع",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val step = if (item.unitType.isFractional) 0.25 else 1.0
                                            onUpdateQuantity(item.catalogItemId, item.quantity - step)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = "${item.quantity}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            val step = if (item.unitType.isFractional) 0.25 else 1.0
                                            onUpdateQuantity(item.catalogItemId, item.quantity + step)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = "${String.format(Locale.US, "%.0f", item.total)} د.ع",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Black,
                                        color = LoopTealPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment Method Selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "طريقة الدفع:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethod.values().forEach { method ->
                        val isSelected = selectedPaymentMethod == method
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LoopTealPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onSetPaymentMethod(method) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = method.labelAr,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Discount Input (Locked for Staff)
            if (!isStaff) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = discountInputText,
                        onValueChange = {
                            discountInputText = it
                            onSetDiscount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("قيمة الخصم (د.ع)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Desktop Keyboard Shortcuts Helper
            if (showKeyboardShortcuts) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("F2: فاتورة جديدة", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Text("F4: كاش", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Text("F7: آجل", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Text("Enter: حفظ وطباعة", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                    }
                }
            }
        }

        // Bottom Totals & Submit Button
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("المجموع:", style = MaterialTheme.typography.bodySmall)
                Text("${String.format(Locale.US, "%.0f", cartItems.sumOf { it.total })} د.ع", style = MaterialTheme.typography.bodySmall)
            }

            if (discountAmount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الخصم:", style = MaterialTheme.typography.bodySmall, color = LoopAmberSecondary)
                    Text("-${String.format(Locale.US, "%.0f", discountAmount)} د.ع", style = MaterialTheme.typography.bodySmall, color = LoopAmberSecondary)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LoopTealPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الإجمالي المستحق:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(
                    "${String.format(Locale.US, "%.0f", grandTotal)} د.ع",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = LoopTealPrimary
                )
            }

            Button(
                onClick = onCompleteSale,
                enabled = cartItems.isNotEmpty() && !isOwner,
                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("complete_invoice_button")
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOwner) "إصدار الفواتير محظور على المالك" else "إصدار الفاتورة وتوليد الواتساب (Enter)",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuickCustomerPickerDialog(
    customers: List<CustomerLedgerEntry>,
    onDismiss: () -> Unit,
    onSelectCustomer: (CustomerLedgerEntry) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = customers.filter {
        query.isBlank() ||
                it.customerName.contains(query, ignoreCase = true) ||
                it.customerPhone.contains(query, ignoreCase = true) ||
                it.vehiclePlates.any { p -> p.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonSearch, contentDescription = null, tint = LoopTealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("البحث السريع عن زبون مسجل", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("بحث بالاسم، الهاتف، أو رقم اللوحة...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا يوجد زبون مطابق", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered, key = { "${it.customerName}_${it.customerPhone}" }) { cust ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectCustomer(cust) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(cust.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        if (cust.customerPhone.isNotBlank()) {
                                            Text(cust.customerPhone, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (cust.vehiclePlates.isNotEmpty()) {
                                            Text("اللوحة: ${cust.vehiclePlates.joinToString("، ")}", style = MaterialTheme.typography.labelSmall, color = LoopTealPrimary)
                                        }
                                    }

                                    if (cust.remainingDebt > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(LoopDangerRed.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "دين: ${String.format(Locale.US, "%,.0f", cust.remainingDebt)}",
                                                color = LoopDangerRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("إغلاق")
            }
        }
    )
}
