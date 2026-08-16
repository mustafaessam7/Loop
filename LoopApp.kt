package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CatalogItemEntity
import com.example.data.local.InvoiceWithItems
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import com.example.model.UserRole
import com.example.ui.components.FirestoreDocsDialog
import com.example.ui.components.FractionalQuantityDialog
import com.example.ui.components.InvoiceReceiptDialog
import com.example.ui.components.LoopTopBar
import com.example.ui.components.SessionDeviceDialog
import com.example.ui.components.VehicleServiceDialog
import com.example.ui.components.WhatsAppSupportDialog
import com.example.ui.dialogs.AiChatDialog
import com.example.ui.dialogs.BarcodeScannerDialog
import com.example.ui.dialogs.EmotionalReminderDialog
import com.example.ui.dialogs.SubscriptionRequiredDialog
import com.example.ui.dialogs.SyncStatusDialog
import com.example.ui.theme.LoopTheme
import com.example.ui.screens.AuditLogsScreen
import com.example.ui.screens.CashboxScreen
import com.example.ui.screens.CustomersDebtsScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoicesHistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MaintenanceRemindersScreen
import com.example.ui.screens.PackagesScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LoopDangerRed
import com.example.ui.theme.LoopSuccessGreen
import com.example.ui.theme.LoopTealPrimary
import com.example.ui.viewmodel.LoopViewModel
import com.example.util.NotificationHelper

enum class AppDestination(val titleAr: String, val icon: ImageVector) {
    POS("نقاط البيع", Icons.Default.PointOfSale),
    PACKAGES("الباقات ⭐", Icons.Default.Star),
    INVOICES("الفواتير", Icons.Default.ReceiptLong),
    CUSTOMERS("الزبائن والديون", Icons.Default.People),
    REMINDERS("الصيانة والزيت", Icons.Default.NotificationsActive),
    INVENTORY("المخزون والخدمات", Icons.Default.Inventory),
    CASHBOX("الصندوق والمصاريف", Icons.Default.LocalAtm),
    REPORTS("التقارير", Icons.Default.Assessment),
    SETTINGS("الإعدادات ⚙️", Icons.Default.Settings),
    AUDIT_LOGS("الأمان 🛡️", Icons.Default.Shield),
    MASTER_DEV("المطور 👑", Icons.Default.Shield)
}

@Composable
fun LoopApp(
    viewModel: LoopViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val workshop by viewModel.workshop.collectAsState()
    val users by viewModel.users.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSessionConflict by viewModel.isSessionConflict.collectAsState()
    val currentDeviceId by viewModel.currentDeviceId.collectAsState()

    val catalogItems by viewModel.filteredCatalog.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val vehicleInfo by viewModel.vehicleInfo.collectAsState()
    val discountAmount by viewModel.discountAmount.collectAsState()
    val taxRate by viewModel.taxRate.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val invoices by viewModel.invoicesWithItems.collectAsState()
    val customerLedgers by viewModel.customerLedgers.collectAsState()
    val upcomingReminders by viewModel.maintenanceReminders.collectAsState()
    val cashboxTransactions by viewModel.cashboxTransactions.collectAsState()
    val activeInvoiceReceipt by viewModel.activeInvoiceReceipt.collectAsState()

    val isOnline by viewModel.isOnline.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val isBarcodeScannerOpen by viewModel.isBarcodeScannerOpen.collectAsState()
    val isSyncModalOpen by viewModel.isSyncModalOpen.collectAsState()
    val packageBundles by viewModel.packageBundles.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val isEmotionalReminderDialogOpen by viewModel.isEmotionalReminderDialogOpen.collectAsState()
    val selectedEmotionalInvoice by viewModel.selectedEmotionalInvoice.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isSubscriptionModalOpen by viewModel.isSubscriptionModalOpen.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by remember { mutableStateOf(AppDestination.POS) }

    // Dialogs state
    var fractionalItemToEdit by remember { mutableStateOf<CatalogItemEntity?>(null) }
    var isVehicleDialogOpen by remember { mutableStateOf(false) }
    var isWhatsAppSupportDialogOpen by remember { mutableStateOf(false) }
    var isAiChatDialogOpen by remember { mutableStateOf(false) }
    var isSessionDialogOpen by remember { mutableStateOf(false) }
    var isFirestoreDocsDialogOpen by remember { mutableStateOf(false) }
    var selectedInvoiceForReceipt by remember { mutableStateOf<InvoiceWithItems?>(null) }

    val totalIndebtedCustomers = customerLedgers.count { it.remainingDebt > 0 }

    val isMasterDev = currentUser?.email.equals("Mustafa000j@gmail.com", ignoreCase = true) || currentUser?.role == UserRole.MASTER_DEVELOPER
    val isOwner = currentUser?.role == UserRole.OWNER || isMasterDev
    val allowCashierViewCosts = workshop?.allowCashierViewCosts ?: false

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Navigation Safety Guard
    LaunchedEffect(currentScreen, currentUser, allowCashierViewCosts) {
        if (currentScreen == AppDestination.MASTER_DEV && !isMasterDev) {
            currentScreen = AppDestination.POS
        }
        if (currentScreen == AppDestination.SETTINGS && !isOwner) {
            currentScreen = AppDestination.POS
        }
        if (currentScreen == AppDestination.AUDIT_LOGS && !isOwner) {
            currentScreen = AppDestination.POS
        }
        if ((currentScreen == AppDestination.INVENTORY || currentScreen == AppDestination.CASHBOX) && !isOwner && !allowCashierViewCosts) {
            currentScreen = AppDestination.POS
        }
    }

    // Streamlined Bottom Navigation Destinations (Max 3-4 items)
    val bottomBarDestinations = remember(currentUser, allowCashierViewCosts) {
        if (isOwner) {
            listOf(AppDestination.POS, AppDestination.INVOICES, AppDestination.CUSTOMERS, AppDestination.REPORTS)
        } else {
            listOf(AppDestination.POS, AppDestination.INVOICES, AppDestination.CUSTOMERS, AppDestination.PACKAGES)
        }
    }

    // Drawer Navigation Destinations (Filtered strictly by RBAC)
    val drawerDestinations = remember(currentUser, allowCashierViewCosts) {
        when {
            isMasterDev -> listOf(
                AppDestination.POS,
                AppDestination.INVOICES,
                AppDestination.CUSTOMERS,
                AppDestination.PACKAGES,
                AppDestination.REMINDERS,
                AppDestination.INVENTORY,
                AppDestination.CASHBOX,
                AppDestination.REPORTS,
                AppDestination.SETTINGS,
                AppDestination.AUDIT_LOGS,
                AppDestination.MASTER_DEV
            )
            isOwner -> listOf(
                AppDestination.POS,
                AppDestination.INVOICES,
                AppDestination.CUSTOMERS,
                AppDestination.PACKAGES,
                AppDestination.REMINDERS,
                AppDestination.INVENTORY,
                AppDestination.CASHBOX,
                AppDestination.REPORTS,
                AppDestination.SETTINGS,
                AppDestination.AUDIT_LOGS
            )
            else -> {
                val items = mutableListOf(
                    AppDestination.POS,
                    AppDestination.INVOICES,
                    AppDestination.CUSTOMERS,
                    AppDestination.PACKAGES,
                    AppDestination.REMINDERS
                )
                if (allowCashierViewCosts) {
                    items.add(AppDestination.INVENTORY)
                    items.add(AppDestination.CASHBOX)
                }
                items
            }
        }
    }

    // Listen for UI messages
    val uiMessage by viewModel.uiMessage.collectAsState()
    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    // Auto show receipt dialog upon completed invoice
    LaunchedEffect(activeInvoiceReceipt) {
        if (activeInvoiceReceipt != null) {
            selectedInvoiceForReceipt = activeInvoiceReceipt
        }
    }

    // Auto trigger local push notifications for maintenance & stock alerts
    LaunchedEffect(upcomingReminders, catalogItems) {
        if (upcomingReminders.isNotEmpty() || catalogItems.isNotEmpty()) {
            NotificationHelper.scanAndTriggerNotifications(context, upcomingReminders, catalogItems)
        }
    }

    LoopTheme(darkTheme = isDarkMode) {
        // Login Screen if not logged in
        if (currentUser == null) {
            LoginScreen(
                workshop = workshop,
                users = users,
                currentDeviceId = currentDeviceId,
                onLoginUser = { viewModel.loginUser(it) },
                onLoginWithPin = { pin ->
                    viewModel.loginWithPin(pin)
                },
                onLoginWithGoogle = { email ->
                    viewModel.loginWithGoogle(email)
                },
                onRegisterNewWorkshop = { wName, oName, email, pass, phone, city ->
                    viewModel.registerNewWorkshop(wName, oName, email, pass, phone, city)
                }
            )
        } else {
            // Modal Navigation Drawer for Admin & System Options
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = currentUser != null,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.width(300.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Header inside Drawer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(LoopTealPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser?.name?.take(1) ?: "L",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = currentUser?.name ?: "المستخدم",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = currentUser?.role?.labelAr ?: "كاشير",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LoopTealPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "القوائم والإدارات (☰)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )

                            // Drawer items list
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                drawerDestinations.forEach { destination ->
                                    val isSelected = currentScreen == destination
                                    NavigationDrawerItem(
                                        label = {
                                            Text(
                                                destination.titleAr,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        selected = isSelected,
                                        icon = { Icon(destination.icon, contentDescription = destination.titleAr) },
                                        onClick = {
                                            currentScreen = destination
                                            scope.launch { drawerState.close() }
                                        },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = LoopTealPrimary.copy(alpha = 0.15f),
                                            selectedIconColor = LoopTealPrimary,
                                            selectedTextColor = LoopTealPrimary
                                        ),
                                        modifier = Modifier.testTag("drawer_item_${destination.name.lowercase()}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            // Logout action
                            OutlinedButton(
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    viewModel.logout()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LoopDangerRed)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            ) {
                // Main App Scaffold
                BoxWithConstraints(modifier = modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth >= 720.dp

                    Scaffold(
                        topBar = {
                            LoopTopBar(
                                workshop = workshop,
                                currentUser = currentUser,
                                isSessionConflict = isSessionConflict,
                                isOnline = isOnline,
                                unsyncedCount = unsyncedCount,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onOpenSyncStatus = { viewModel.setSyncModalOpen(true) },
                                onOpenWhatsAppSupport = { isWhatsAppSupportDialogOpen = true },
                                onOpenAiChat = { isAiChatDialogOpen = true },
                                onOpenSessionManager = { isSessionDialogOpen = true },
                                onOpenFirestoreDocs = { isFirestoreDocsDialogOpen = true },
                                onUserClick = { viewModel.logout() }
                            )
                        },
                        bottomBar = {
                            if (!isWideScreen) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 6.dp
                                ) {
                                    bottomBarDestinations.forEach { destination ->
                                        val isSelected = currentScreen == destination
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentScreen = destination },
                                            icon = {
                                                if (destination == AppDestination.REMINDERS && upcomingReminders.isNotEmpty()) {
                                                    BadgedBox(badge = { Badge { Text("${upcomingReminders.size}") } }) {
                                                        Icon(destination.icon, contentDescription = destination.titleAr)
                                                    }
                                                } else if (destination == AppDestination.CUSTOMERS && totalIndebtedCustomers > 0) {
                                                    BadgedBox(badge = { Badge(containerColor = LoopDangerRed) { Text("$totalIndebtedCustomers") } }) {
                                                        Icon(destination.icon, contentDescription = destination.titleAr)
                                                    }
                                                } else {
                                                    Icon(destination.icon, contentDescription = destination.titleAr)
                                                }
                                            },
                                            label = { Text(destination.titleAr, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = LoopTealPrimary,
                                                selectedTextColor = LoopTealPrimary,
                                                indicatorColor = LoopTealPrimary.copy(alpha = 0.15f)
                                            ),
                                            modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Wide Screen Navigation Rail (Tablet / PC)
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight(),
                        header = {
                            IconButton(onClick = { viewModel.logout() }) {
                                Icon(Icons.Default.ExitToApp, contentDescription = "تسجيل خروج", tint = LoopDangerRed)
                            }
                        }
                    ) {
                        AppDestination.values().forEach { destination ->
                            val isSelected = currentScreen == destination
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { currentScreen = destination },
                                icon = {
                                    if (destination == AppDestination.REMINDERS && upcomingReminders.isNotEmpty()) {
                                        BadgedBox(badge = { Badge { Text("${upcomingReminders.size}") } }) {
                                            Icon(destination.icon, contentDescription = destination.titleAr)
                                        }
                                    } else if (destination == AppDestination.CUSTOMERS && totalIndebtedCustomers > 0) {
                                        BadgedBox(badge = { Badge(containerColor = LoopDangerRed) { Text("$totalIndebtedCustomers") } }) {
                                            Icon(destination.icon, contentDescription = destination.titleAr)
                                        }
                                    } else {
                                        Icon(destination.icon, contentDescription = destination.titleAr)
                                    }
                                },
                                label = { Text(destination.titleAr, fontSize = 10.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = LoopTealPrimary,
                                    selectedTextColor = LoopTealPrimary,
                                    indicatorColor = LoopTealPrimary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_rail_${destination.name.lowercase()}")
                            )
                        }
                    }
                }

                // Active Screen Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (currentScreen) {
                        AppDestination.POS -> {
                            PosScreen(
                                catalogItems = catalogItems,
                                cartItems = cartItems,
                                vehicleInfo = vehicleInfo,
                                discountAmount = discountAmount,
                                taxRate = taxRate,
                                selectedPaymentMethod = selectedPaymentMethod,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                currentUser = currentUser,
                                customerLedgers = customerLedgers,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onCategorySelect = { viewModel.setSelectedCategory(it) },
                                onOpenFractionalDialog = { fractionalItemToEdit = it },
                                onAddToCartQuick = { item -> viewModel.addToCart(item, 1.0) },
                                onUpdateQuantity = { id, qty -> viewModel.updateCartItemQuantity(id, qty) },
                                onRemoveFromCart = { viewModel.removeFromCart(it) },
                                onClearCart = { viewModel.clearCart() },
                                onSetDiscount = { viewModel.setDiscount(it) },
                                onSetPaymentMethod = { viewModel.setPaymentMethod(it) },
                                onOpenVehicleDialog = { isVehicleDialogOpen = true },
                                onOpenBarcodeScanner = { viewModel.setBarcodeScannerOpen(true) },
                                onSelectCustomer = { cust ->
                                    val primaryPlate = cust.vehiclePlates.firstOrNull() ?: vehicleInfo.plateNumber
                                    viewModel.setVehicleInfo(
                                        vehicleInfo.copy(
                                            customerName = cust.customerName,
                                            customerPhone = cust.customerPhone,
                                            plateNumber = primaryPlate
                                        )
                                    )
                                },
                                onCompleteSale = { viewModel.completeSale() }
                            )
                        }
                        AppDestination.PACKAGES -> {
                            PackagesScreen(
                                packages = packageBundles,
                                currentUserRole = currentUser?.role ?: UserRole.STAFF,
                                onAddBundleToCart = { bundle ->
                                    viewModel.addBundleToCart(bundle)
                                    currentScreen = AppDestination.POS
                                },
                                onCreateNewBundle = { },
                                onDeleteBundle = { _, _ -> }
                            )
                        }
                        AppDestination.INVOICES -> {
                            InvoicesHistoryScreen(
                                invoices = invoices,
                                onSelectInvoice = { selectedInvoiceForReceipt = it },
                                buildWhatsAppMessage = { viewModel.buildWhatsAppInvoiceMessage(it) }
                            )
                        }
                        AppDestination.CUSTOMERS -> {
                            CustomersDebtsScreen(
                                customerLedgers = customerLedgers,
                                invoices = invoices,
                                workshop = workshop,
                                currentUser = currentUser,
                                onRecordPayment = { customerName, customerPhone, invoiceNo, amount, remaining, method, notes, onSuccess ->
                                    viewModel.recordDebtPayment(
                                        customerName = customerName,
                                        customerPhone = customerPhone,
                                        invoiceNumber = invoiceNo,
                                        amountPaid = amount,
                                        remainingBalanceAfter = remaining,
                                        paymentMethod = method,
                                        notes = notes,
                                        onSuccess = onSuccess
                                    )
                                },
                                onSelectCustomerForPos = { cust ->
                                    val primaryPlate = cust.vehiclePlates.firstOrNull() ?: vehicleInfo.plateNumber
                                    viewModel.setVehicleInfo(
                                        vehicleInfo.copy(
                                            customerName = cust.customerName,
                                            customerPhone = cust.customerPhone,
                                            plateNumber = primaryPlate
                                        )
                                    )
                                    currentScreen = AppDestination.POS
                                },
                                buildDebtReceiptMessage = { name, phone, amount, remaining, method, receiptId, notes ->
                                    viewModel.buildDebtPaymentWhatsAppReceipt(
                                        customerName = name,
                                        customerPhone = phone,
                                        amountPaid = amount,
                                        remainingBalance = remaining,
                                        paymentMethod = method,
                                        receiptId = receiptId,
                                        notes = notes
                                    )
                                }
                            )
                        }
                        AppDestination.REMINDERS -> {
                            MaintenanceRemindersScreen(
                                reminders = upcomingReminders,
                                workshopName = workshop?.name ?: "ورشة لوب",
                                buildReminderMessage = { viewModel.buildWhatsAppReminderMessage(it) },
                                buildEmotionalReminderMessage = { viewModel.buildWhatsAppEmotionalReminderMessage(it) },
                                onOpenEmotionalCustomizer = { invoice ->
                                    viewModel.openEmotionalReminderDialog(invoice)
                                }
                            )
                        }
                        AppDestination.INVENTORY -> {
                            InventoryScreen(
                                catalogItems = catalogItems,
                                currentUser = currentUser,
                                workshop = workshop,
                                onSaveItem = { viewModel.saveCatalogItem(it) },
                                onDeleteItem = { viewModel.deleteCatalogItem(it) }
                            )
                        }
                        AppDestination.CASHBOX -> {
                            CashboxScreen(
                                transactions = cashboxTransactions,
                                currentUser = currentUser,
                                workshop = workshop,
                                onAddTransaction = { type, amt, note ->
                                    viewModel.addCashboxEntry(type, amt, note)
                                },
                                onRecordPettyExpense = { amt, category, note ->
                                    viewModel.recordPettyExpense(amt, category, note)
                                }
                            )
                        }
                        AppDestination.REPORTS -> {
                            ReportsScreen(
                                invoices = invoices,
                                users = users,
                                currentUser = currentUser,
                                workshop = workshop,
                                cashboxTransactions = cashboxTransactions,
                                onExportMonthlyReport = { viewModel.openSubscriptionModal() }
                            )
                        }
                        AppDestination.SETTINGS -> {
                            SettingsScreen(
                                workshop = workshop,
                                currentUser = currentUser,
                                reminders = upcomingReminders,
                                catalogItems = catalogItems,
                                users = users,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                onToggleAllowCashierViewCosts = { viewModel.toggleAllowCashierViewCosts(it) },
                                onCreateUser = { name, email, pin, role ->
                                    viewModel.createStaffUser(name, email, pin, role)
                                },
                                onUpdateUserPin = { userId, newPin ->
                                    viewModel.updateUserPin(userId, newPin)
                                },
                                onDeleteUser = { userId ->
                                    viewModel.deleteStaffUser(userId)
                                },
                                onActivateLicense = { key ->
                                    viewModel.activateLicense(key)
                                },
                                onSaveTemplate = { newTemplate ->
                                    viewModel.updateWhatsAppTemplate(newTemplate)
                                }
                            )
                        }
                        AppDestination.AUDIT_LOGS -> {
                            AuditLogsScreen(
                                auditLogs = auditLogs,
                                currentUserRole = currentUser?.role ?: UserRole.STAFF
                            )
                        }
                        AppDestination.MASTER_DEV -> {
                            val firestoreWorkshops by com.example.data.repository.FirebaseSyncManager.firestoreWorkshops.collectAsState()
                            val activationRequests by com.example.data.repository.FirebaseSyncManager.activationRequests.collectAsState()
                            val generatedLicenses by com.example.data.repository.FirebaseSyncManager.generatedLicenses.collectAsState()

                            com.example.ui.screens.MasterDeveloperDashboardScreen(
                                currentWorkshop = workshop,
                                firestoreWorkshops = firestoreWorkshops,
                                activationRequests = activationRequests,
                                generatedLicenses = generatedLicenses,
                                onActivateWorkshopLocal = { wsId, key ->
                                    viewModel.activateLicense(key)
                                }
                            )
                        }
                    }

                    // Single Active Session Lockout Overlay if Conflict Detected
                    if (isSessionConflict) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Black.copy(alpha = 0.75f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .width(420.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = LoopDangerRed,
                                            modifier = Modifier.size(54.dp)
                                        )

                                        Text(
                                            text = "تنبيه أمان الجلسة النشطة (Loop Security)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = "تم رصد تسجيل دخول لحسابك من جهاز آخر. وفق سياسة الأمان الصارمة لنظام Loop، تم إيقاف المعاملات على هذا الجهاز لمنع تضارب الصندوق والمخزون.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )

                                        Button(
                                            onClick = { viewModel.resolveSessionConflict() },
                                            colors = ButtonDefaults.buttonColors(containerColor = LoopSuccessGreen),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("إعادة تفعيل الجلسة على هذا الجهاز")
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.logout() },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("تسجيل الخروج والعودة لشاشة الدخول")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    fractionalItemToEdit?.let { item ->
        FractionalQuantityDialog(
            item = item,
            userRole = currentUser?.role ?: UserRole.STAFF,
            onDismiss = { fractionalItemToEdit = null },
            onConfirm = { qty, price ->
                viewModel.addToCart(item, qty, price)
            }
        )
    }

    if (isVehicleDialogOpen) {
        VehicleServiceDialog(
            initialInfo = vehicleInfo,
            onDismiss = { isVehicleDialogOpen = false },
            onConfirm = { updated ->
                viewModel.setVehicleInfo(updated)
            }
        )
    }

    selectedInvoiceForReceipt?.let { invWithItems ->
        InvoiceReceiptDialog(
            invoiceWithItems = invWithItems,
            workshop = workshop,
            whatsAppMessage = viewModel.buildWhatsAppInvoiceMessage(invWithItems),
            whatsAppEmotionalMessage = viewModel.buildWhatsAppEmotionalReminderMessage(invWithItems.invoice),
            onDismiss = {
                selectedInvoiceForReceipt = null
                viewModel.closeReceipt()
            }
        )
    }

    if (isEmotionalReminderDialogOpen) {
        EmotionalReminderDialog(
            initialInvoice = selectedEmotionalInvoice,
            initialWorkshopName = workshop?.name ?: "ورشة لوب",
            onDismiss = { viewModel.closeEmotionalReminderDialog() }
        )
    }

    if (isWhatsAppSupportDialogOpen) {
        WhatsAppSupportDialog(
            workshop = workshop,
            currentUser = currentUser,
            deviceId = currentDeviceId,
            supportMessage = viewModel.buildWhatsAppSupportMessage(),
            onDismiss = { isWhatsAppSupportDialogOpen = false }
        )
    }

    if (isAiChatDialogOpen) {
        AiChatDialog(onDismiss = { isAiChatDialogOpen = false })
    }

    if (isSessionDialogOpen) {
        SessionDeviceDialog(
            currentUser = currentUser,
            currentDeviceId = currentDeviceId,
            isConflict = isSessionConflict,
            onSimulateConflict = { viewModel.simulateCrossDeviceLogin() },
            onResolveConflict = { viewModel.resolveSessionConflict() },
            onDismiss = { isSessionDialogOpen = false }
        )
    }

    if (isFirestoreDocsDialogOpen) {
        FirestoreDocsDialog(onDismiss = { isFirestoreDocsDialogOpen = false })
    }

    if (isBarcodeScannerOpen) {
        BarcodeScannerDialog(
            onDismiss = { viewModel.setBarcodeScannerOpen(false) },
            onBarcodeScanned = { barcode ->
                viewModel.scanBarcodeAndAddToCart(barcode)
            }
        )
    }

    if (isSyncModalOpen) {
        SyncStatusDialog(
            isOnline = isOnline,
            unsyncedCount = unsyncedCount,
            onToggleOnline = { viewModel.setOnline(!isOnline) },
            onTriggerSync = { viewModel.triggerManualSync() },
            onDismiss = { viewModel.setSyncModalOpen(false) }
        )
    }

    if (isSubscriptionModalOpen) {
        SubscriptionRequiredDialog(
            workshop = workshop,
            currentUser = currentUser,
            onActivateLicense = { key -> viewModel.activateLicense(key) },
            onDismiss = { viewModel.closeSubscriptionModal() }
        )
    }
            }
        }
    }
}
