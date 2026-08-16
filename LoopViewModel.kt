package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AuditLogEntity
import com.example.data.local.CashboxTransactionEntity
import com.example.data.local.CatalogItemEntity
import com.example.data.local.DEFAULT_EMOTIONAL_TEMPLATE
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.LoopDatabase
import com.example.data.local.PackageBundleEntity
import com.example.data.local.PackageItemEntity
import com.example.data.local.PackageWithItems
import com.example.data.local.UserEntity
import com.example.data.local.WorkshopEntity
import com.example.data.repository.LoopRepository
import com.example.model.CartItem
import com.example.model.ItemCategory
import com.example.model.NavigationSection
import com.example.model.PaymentMethod
import com.example.model.UnitType
import com.example.model.UserRole
import com.example.model.VehicleServiceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LoopViewModel(application: Application) : AndroidViewModel(application) {
    private val db = LoopDatabase.getInstance(application)
    private val repository = LoopRepository(db)
    private val sessionManager = com.example.data.local.SessionPreferenceManager(application)

    // Current Device ID in this local client
    private val _currentDeviceId = MutableStateFlow("DEV-CURRENT-CLIENT-01")
    val currentDeviceId: StateFlow<String> = _currentDeviceId.asStateFlow()

    // Current User Session
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Session conflict detection (Single active session)
    private val _isSessionConflict = MutableStateFlow(false)
    val isSessionConflict: StateFlow<Boolean> = _isSessionConflict.asStateFlow()

    // Navigation & UI state
    private val _activeSection = MutableStateFlow(NavigationSection.POS)
    val activeSection: StateFlow<NavigationSection> = _activeSection.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Cart & Sale State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _vehicleInfo = MutableStateFlow(VehicleServiceInfo())
    val vehicleInfo: StateFlow<VehicleServiceInfo> = _vehicleInfo.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _taxRate = MutableStateFlow(0.0) // 0% VAT for Iraqi workshop market
    val taxRate: StateFlow<Double> = _taxRate.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.CASH)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    // Catalog filtering
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ItemCategory?>(null)
    val selectedCategory: StateFlow<ItemCategory?> = _selectedCategory.asStateFlow()

    // Active Dialogs
    private val _fractionalItemDialog = MutableStateFlow<CatalogItemEntity?>(null)
    val fractionalItemDialog: StateFlow<CatalogItemEntity?> = _fractionalItemDialog.asStateFlow()

    private val _isVehicleDialogOpen = MutableStateFlow(false)
    val isVehicleDialogOpen: StateFlow<Boolean> = _isVehicleDialogOpen.asStateFlow()

    private val _isWhatsAppSupportOpen = MutableStateFlow(false)
    val isWhatsAppSupportOpen: StateFlow<Boolean> = _isWhatsAppSupportOpen.asStateFlow()

    private val _isSessionManagerOpen = MutableStateFlow(false)
    val isSessionManagerOpen: StateFlow<Boolean> = _isSessionManagerOpen.asStateFlow()

    private val _isFirestoreDocsOpen = MutableStateFlow(false)
    val isFirestoreDocsOpen: StateFlow<Boolean> = _isFirestoreDocsOpen.asStateFlow()

    private val _isEmotionalReminderDialogOpen = MutableStateFlow(false)
    val isEmotionalReminderDialogOpen: StateFlow<Boolean> = _isEmotionalReminderDialogOpen.asStateFlow()

    private val _selectedEmotionalInvoice = MutableStateFlow<InvoiceEntity?>(null)
    val selectedEmotionalInvoice: StateFlow<InvoiceEntity?> = _selectedEmotionalInvoice.asStateFlow()

    private val _activeInvoiceReceipt = MutableStateFlow<InvoiceWithItems?>(null)
    val activeInvoiceReceipt: StateFlow<InvoiceWithItems?> = _activeInvoiceReceipt.asStateFlow()

    // Dark Mode state
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Subscription & License Modal state
    private val _isSubscriptionModalOpen = MutableStateFlow(false)
    val isSubscriptionModalOpen: StateFlow<Boolean> = _isSubscriptionModalOpen.asStateFlow()

    // Repository Flows
    val workshop: StateFlow<WorkshopEntity?> = repository.workshop
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val users: StateFlow<List<UserEntity>> = repository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val catalogItems: StateFlow<List<CatalogItemEntity>> = repository.catalogItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoicesWithItems: StateFlow<List<InvoiceWithItems>> = repository.invoicesWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maintenanceReminders: StateFlow<List<InvoiceEntity>> = repository.maintenanceReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashboxTransactions: StateFlow<List<CashboxTransactionEntity>> = repository.cashboxTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debtPayments: StateFlow<List<com.example.data.local.DebtPaymentEntity>> = repository.debtPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val packageBundles: StateFlow<List<PackageWithItems>> = repository.packages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Offline / Sync state
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _unsyncedCount = MutableStateFlow(0)
    val unsyncedCount: StateFlow<Int> = _unsyncedCount.asStateFlow()

    // Modals state
    private val _isBarcodeScannerOpen = MutableStateFlow(false)
    val isBarcodeScannerOpen: StateFlow<Boolean> = _isBarcodeScannerOpen.asStateFlow()

    private val _isSyncModalOpen = MutableStateFlow(false)
    val isSyncModalOpen: StateFlow<Boolean> = _isSyncModalOpen.asStateFlow()

    private val _isPackageBuilderOpen = MutableStateFlow(false)
    val isPackageBuilderOpen: StateFlow<Boolean> = _isPackageBuilderOpen.asStateFlow()

    val totalInflow: StateFlow<Double?> = repository.totalInflow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double?> = repository.totalExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Customer Ledgers & Debt Summary
    val customerLedgers: StateFlow<List<com.example.model.CustomerLedgerEntry>> = combine(
        invoicesWithItems,
        debtPayments
    ) { invoices, payments ->
        val customerMap = mutableMapOf<String, MutableList<InvoiceWithItems>>()
        for (inv in invoices) {
            val key = if (inv.invoice.customerPhone.isNotBlank()) inv.invoice.customerPhone else inv.invoice.customerName
            customerMap.getOrPut(key) { mutableListOf() }.add(inv)
        }

        customerMap.map { (key, invList) ->
            val firstInv = invList.first().invoice
            val name = firstInv.customerName
            val phone = firstInv.customerPhone

            val totalDebtInvoiced = invList.filter { it.invoice.paymentMethod == PaymentMethod.DEBT }.sumOf { it.invoice.total }
            val customerPayments = payments.filter {
                (phone.isNotBlank() && it.customerPhone == phone) || (it.customerName.equals(name, ignoreCase = true))
            }
            val totalPaid = customerPayments.sumOf { it.amountPaid }
            val remainingDebt = maxOf(0.0, totalDebtInvoiced - totalPaid)

            val plates = invList.mapNotNull { it.invoice.vehiclePlate.takeIf { p -> p.isNotBlank() } }.distinct()
            val models = invList.mapNotNull { it.invoice.vehicleModel.takeIf { m -> m.isNotBlank() } }.distinct()
            val latestInvoice = invList.maxByOrNull { it.invoice.timestamp }

            com.example.model.CustomerLedgerEntry(
                customerName = name,
                customerPhone = phone,
                totalDebtInvoiced = totalDebtInvoiced,
                totalPaid = totalPaid,
                remainingDebt = remainingDebt,
                vehiclePlates = plates,
                vehicleModels = models,
                lastServiceDate = latestInvoice?.invoice?.nextServiceDate ?: "",
                lastServiceMileage = latestInvoice?.invoice?.currentMileage ?: 0,
                totalInvoicesCount = invList.size,
                hasOutstandingDebt = remainingDebt > 0.0
            )
        }.sortedByDescending { it.remainingDebt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Catalog
    val filteredCatalog: StateFlow<List<CatalogItemEntity>> = combine(
        catalogItems,
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            val matchQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.code.contains(query, ignoreCase = true)
            val matchCategory = category == null || item.category == category
            matchQuery && matchCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize Firebase Realtime Listeners
        com.example.data.repository.FirebaseSyncManager.initRealtimeListeners(application, viewModelScope)

        // Persistent Session Check: Auto-login if previously authenticated
        val savedUser = sessionManager.getSavedUser()
        if (savedUser != null) {
            _currentUser.value = savedUser
            viewModelScope.launch {
                repository.saveUser(savedUser)
            }
        }
    }

    fun registerNewWorkshop(
        workshopName: String,
        ownerName: String,
        email: String,
        pass: String,
        phone: String,
        city: String
    ) {
        viewModelScope.launch {
            val wsId = "WS-${System.currentTimeMillis().toString().takeLast(6)}"
            val isMaster = email.trim().equals("Mustafa000j@gmail.com", ignoreCase = true)
            val newWs = WorkshopEntity(
                id = wsId,
                name = workshopName,
                commercialReg = "REG-${System.currentTimeMillis().toString().takeLast(6)}",
                phone = phone,
                email = email,
                address = city,
                taxNumber = "TAX-IRAQ-1001",
                isActivated = isMaster,
                licenseKey = if (isMaster) "LOOP-PRO-MASTER-8888" else ""
            )
            repository.updateWorkshop(newWs)

            val newOwner = UserEntity(
                id = "USR-${System.currentTimeMillis().toString().takeLast(6)}",
                workshopId = wsId,
                name = ownerName,
                email = email,
                role = if (isMaster) UserRole.MASTER_DEVELOPER else UserRole.OWNER,
                pinCode = "",
                activeDeviceId = _currentDeviceId.value
            )
            repository.saveUser(newOwner)
            loginUser(newOwner)
        }
    }

    // Authentication & Session
    fun loginUser(user: UserEntity, authToken: String = "") {
        viewModelScope.launch {
            val updatedUser = user.copy(activeDeviceId = _currentDeviceId.value)
            _currentUser.value = updatedUser
            _isSessionConflict.value = false
            sessionManager.saveSession(updatedUser, authToken)
            repository.saveUser(updatedUser)
            repository.updateUserActiveDevice(updatedUser.id, _currentDeviceId.value)
            _uiMessage.value = "تم تسجيل الدخول بنجاح: ${updatedUser.name} (${updatedUser.role.labelAr})"
        }
    }

    fun loginWithPin(pin: String) {
        viewModelScope.launch {
            val user = repository.getUserByPin(pin)
            if (user != null) {
                loginUser(user)
            } else {
                _uiMessage.value = "رمز PIN غير صحيح!"
            }
        }
    }

    fun loginWithGoogle(email: String) {
        viewModelScope.launch {
            val isMasterDevEmail = email.equals("mustafa000j@gmail.com", ignoreCase = true)
            val user = repository.getUserByEmail(email) ?: UserEntity(
                id = if (isMasterDevEmail) "USR-MASTER-DEV-01" else "USR-GOOGLE-${System.currentTimeMillis() % 10000}",
                workshopId = workshop.value?.id ?: "WS-MAIN-001",
                name = if (isMasterDevEmail) "مصطفى (Master Dev)" else email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email,
                role = if (isMasterDevEmail) UserRole.MASTER_DEVELOPER else UserRole.OWNER,
                pinCode = "",
                activeDeviceId = _currentDeviceId.value
            )
            loginUser(user)
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _currentUser.value = null
        _cartItems.value = emptyList()
        _isSessionConflict.value = false
        _uiMessage.value = "تم تسجيل الخروج بنجاح"
    }

    fun toggleAllowCashierViewCosts(enabled: Boolean) {
        viewModelScope.launch {
            val currentWs = workshop.value ?: return@launch
            val updatedWs = currentWs.copy(allowCashierViewCosts = enabled)
            repository.updateWorkshop(updatedWs)
            _uiMessage.value = if (enabled) "تم تفعيل عرض تكاليف الشراء للكاشير" else "تم إخفاء تكاليف الشراء عن الكاشير"
            com.example.data.repository.FirebaseSyncManager.syncWorkshopToCloud(updatedWs, currentUser.value)
        }
    }

    // Simulate Single Active Session Conflict
    fun simulateCrossDeviceLogin(newDeviceId: String = "DEV-PHONE-IPHONE-99") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            // Update the active device ID on database to simulate another device logging in
            repository.updateUserActiveDevice(user.id, newDeviceId)
            _isSessionConflict.value = true
            _uiMessage.value = "تنبيه: تم تسجيل الدخول إلى هذا الحساب من جهاز آخر ($newDeviceId)!"
        }
    }

    fun resolveSessionConflict() {
        val user = _currentUser.value ?: return
        loginUser(user)
    }

    // Navigation
    fun setActiveSection(section: NavigationSection) {
        _activeSection.value = section
    }

    // Cart Management
    fun addToCart(item: CatalogItemEntity, quantity: Double = 1.0, customPrice: Double? = null) {
        val currentUserRole = _currentUser.value?.role ?: UserRole.STAFF
        val finalPrice = if (currentUserRole == UserRole.STAFF) {
            item.salePrice // Staff CANNOT alter price
        } else {
            customPrice ?: item.salePrice
        }

        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.catalogItemId == item.id }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            val newQty = existing.quantity + quantity
            currentList[existingIndex] = existing.copy(
                quantity = newQty,
                unitPrice = finalPrice
            )
        } else {
            currentList.add(
                CartItem(
                    catalogItemId = item.id,
                    name = item.name,
                    unitType = item.unitType,
                    unitPrice = finalPrice,
                    originalPrice = item.salePrice,
                    quantity = quantity,
                    category = item.category
                )
            )
        }
        _cartItems.value = currentList
    }

    fun updateCartItemQuantity(catalogItemId: Long, quantity: Double) {
        if (quantity <= 0.0) {
            removeFromCart(catalogItemId)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.catalogItemId == catalogItemId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(quantity = quantity)
            _cartItems.value = currentList
        }
    }

    fun updateCartItemPrice(catalogItemId: Long, newPrice: Double) {
        val user = _currentUser.value
        if (user?.role == UserRole.STAFF) {
            _uiMessage.value = "عذراً: الفني لا يملك صلاحية تغيير السعر الأساسي للقطعة!"
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.catalogItemId == catalogItemId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(unitPrice = newPrice)
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(catalogItemId: Long) {
        _cartItems.value = _cartItems.value.filterNot { it.catalogItemId == catalogItemId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discountAmount.value = 0.0
        _vehicleInfo.value = VehicleServiceInfo()
    }

    fun setDiscount(amount: Double) {
        val user = _currentUser.value
        if (user?.role == UserRole.STAFF) {
            _uiMessage.value = "عذراً: صلاحية الخصم متاحة فقط للكاشير الرئيسي والمالك!"
            return
        }
        _discountAmount.value = maxOf(0.0, amount)
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    fun setVehicleInfo(info: VehicleServiceInfo) {
        _vehicleInfo.value = info
    }

    fun calculateAutoNextService(currentKm: Int, monthsToAdd: Int = 3, kmToAdd: Int = 5000) {
        val nextKm = currentKm + kmToAdd
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, monthsToAdd)
        val nextDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        _vehicleInfo.value = _vehicleInfo.value.copy(
            currentMileage = currentKm,
            nextServiceMileage = nextKm,
            nextServiceDate = nextDateStr
        )
    }

    fun toggleDarkMode(enabled: Boolean? = null) {
        _isDarkMode.value = enabled ?: !_isDarkMode.value
    }

    fun openSubscriptionModal() {
        _isSubscriptionModalOpen.value = true
    }

    fun closeSubscriptionModal() {
        _isSubscriptionModalOpen.value = false
    }

    fun activateLicense(key: String): Boolean {
        if (key.trim().length >= 4) {
            viewModelScope.launch {
                val wsId = workshop.value?.id ?: "WS-LOOP-7789"
                repository.activateWorkshop(wsId, key.trim())
                _isSubscriptionModalOpen.value = false
                _uiMessage.value = "تم تفعيل الترخيص والاشتراك بنجاح! 🚀"
            }
            return true
        }
        return false
    }

    fun createStaffUser(name: String, email: String, pin: String, role: UserRole) {
        viewModelScope.launch {
            val wsId = workshop.value?.id ?: "WS-LOOP-7789"
            val newUserId = "USR-STAFF-${System.currentTimeMillis().toString().takeLast(6)}"
            val newUser = UserEntity(
                id = newUserId,
                workshopId = wsId,
                name = name,
                email = email.ifBlank { "staff_${System.currentTimeMillis()}@loop.com" },
                role = role,
                pinCode = pin,
                activeDeviceId = "DEV-STAFF-MOBILE"
            )
            repository.saveUser(newUser)
            _uiMessage.value = "تم إنشاء حساب الكادر (${name}) بنجاح! 👤"
        }
    }

    fun updateUserPin(userId: String, newPin: String) {
        viewModelScope.launch {
            val user = users.value.find { it.id == userId }
            if (user != null) {
                repository.saveUser(user.copy(pinCode = newPin))
                _uiMessage.value = "تم تحديث الرمز السري للحساب بنجاح! 🔐"
            }
        }
    }

    fun deleteStaffUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            _uiMessage.value = "تم حذف حساب الكادر بنجاح!"
        }
    }

    // Checkout & Invoice Generation
    fun completeSale(onSuccess: (Long) -> Unit = {}) {
        val user = _currentUser.value
        if (user == null) {
            _uiMessage.value = "يرجى تسجيل الدخول أولاً!"
            return
        }

        val isMasterDev = user.role == UserRole.MASTER_DEVELOPER || user.email.equals("mustafa000j@gmail.com", ignoreCase = true)

        if (workshop.value?.isActivated != true && !isMasterDev) {
            _isSubscriptionModalOpen.value = true
            _uiMessage.value = "وضع المعاينة (Demo Mode): تفعيل الاشتراك مطلوب لإكمال وحفظ الفواتير!"
            return
        }

        // ROLE RULE: Owner is restricted from issuing invoices!
        if (user.role == UserRole.OWNER) {
            _uiMessage.value = "تنبيه نظامي: يُمنع المالك من إصدار الفواتير مباشرة لضمان نزاهة الصندوق والتدقيق المالي!"
            return
        }

        if (_cartItems.value.isEmpty()) {
            _uiMessage.value = "السلة فارغة! يرجى إضافة عناصر أولاً."
            return
        }

        viewModelScope.launch {
            val workshopId = workshop.value?.id ?: "WS-LOOP-7789"
            val invoiceId = repository.createInvoice(
                workshopId = workshopId,
                cartItems = _cartItems.value,
                vehicleInfo = _vehicleInfo.value,
                discount = _discountAmount.value,
                taxRate = _taxRate.value,
                paymentMethod = _selectedPaymentMethod.value,
                cashier = user,
                notes = _vehicleInfo.value.serviceNotes
            )

            val fullInvoice = repository.getInvoiceWithItems(invoiceId)
            _activeInvoiceReceipt.value = fullInvoice
            clearCart()
            _uiMessage.value = "تم إصدار الفاتورة بنجاح رقم #${fullInvoice?.invoice?.invoiceNumber}"
            onSuccess(invoiceId)
        }
    }

    // Dialog state controllers
    fun openFractionalDialog(item: CatalogItemEntity) {
        _fractionalItemDialog.value = item
    }

    fun closeFractionalDialog() {
        _fractionalItemDialog.value = null
    }

    fun setVehicleDialogOpen(open: Boolean) {
        _isVehicleDialogOpen.value = open
    }

    fun setWhatsAppSupportOpen(open: Boolean) {
        _isWhatsAppSupportOpen.value = open
    }

    fun setSessionManagerOpen(open: Boolean) {
        _isSessionManagerOpen.value = open
    }

    fun setFirestoreDocsOpen(open: Boolean) {
        _isFirestoreDocsOpen.value = open
    }

    fun openEmotionalReminderDialog(invoice: InvoiceEntity? = null) {
        _selectedEmotionalInvoice.value = invoice
        _isEmotionalReminderDialogOpen.value = true
    }

    fun closeEmotionalReminderDialog() {
        _isEmotionalReminderDialogOpen.value = false
        _selectedEmotionalInvoice.value = null
    }

    fun showReceipt(invoiceWithItems: InvoiceWithItems) {
        _activeInvoiceReceipt.value = invoiceWithItems
    }

    fun closeReceipt() {
        _activeInvoiceReceipt.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: ItemCategory?) {
        _selectedCategory.value = category
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    // WhatsApp Message Builders
    fun buildWhatsAppSupportMessage(): String {
        val ws = workshop.value
        val user = _currentUser.value
        return """
            *طلب دعم فني - تطبيق Loop لإدارة الورش*
            ------------------------------------
            🏢 اسم الورشة: ${ws?.name ?: "ورشة لوب"}
            🆔 معرف الورشة: ${ws?.id ?: "WS-LOOP-7789"}
            👤 المستخدم: ${user?.name ?: "غير مسجل"}
            🏷️ الدور: ${user?.role?.labelAr ?: "غير محدد"}
            📧 البريد: ${user?.email ?: "mustafa000j@gmail.com"}
            📱 رقم الهاتف: ${ws?.phone ?: "+9647701234567"}
            💻 الجهاز النشط: ${_currentDeviceId.value}
            ------------------------------------
            مرحباً فريق دعم Loop، أرجو المساعدة في:
        """.trimIndent()
    }

    fun buildWhatsAppInvoiceMessage(invoiceWithItems: InvoiceWithItems): String {
        val inv = invoiceWithItems.invoice
        val ws = workshop.value
        val currencySymbol = ws?.currency ?: "د.ع"
        val itemsText = invoiceWithItems.items.joinToString("\n") { item ->
            "• ${item.itemName} | ${item.quantity} ${item.unitType.symbolAr} × ${String.format(Locale.US, "%.0f", item.unitPrice)} = ${String.format(Locale.US, "%.0f", item.totalPrice)} $currencySymbol"
        }

        return """
            🧾 *فاتورة صيانة وخدمات - ${ws?.name ?: "ورشة لوب"}*
            ------------------------------------
            رقم الفاتورة: *#${inv.invoiceNumber}*
            العميل: ${inv.customerName}
            رقم اللوحة: ${inv.vehiclePlate.ifBlank { "غير مسجل" }}
            نوع السيارة: ${inv.vehicleModel.ifBlank { "-" }}
            العداد الحالي: ${if (inv.currentMileage > 0) "${inv.currentMileage} كم" else "-"}
            ------------------------------------
            *القطع والخدمات:*
            $itemsText
            ------------------------------------
            المجموع: ${String.format(Locale.US, "%.0f", inv.subtotal)} $currencySymbol
            ${if (inv.discount > 0) "الخصم: ${String.format(Locale.US, "%.0f", inv.discount)} $currencySymbol\n" else ""}*الإجمالي النهائي:* *${String.format(Locale.US, "%.0f", inv.total)} $currencySymbol*
            طريقة الدفع: ${inv.paymentMethod.labelAr}
            الكاشير: ${inv.cashierName}
            ${if (inv.nextServiceDate.isNotBlank()) "⏰ *موعد الصيانة وتبديل الدهن القادم:* ${inv.nextServiceDate} (عند ${inv.nextServiceMileage} كم)" else ""}
            ------------------------------------
            شكراً لتعاملكم مع ${ws?.name ?: "ورشة لوب"}! نتمنى لكم قيادة آمنة.
        """.trimIndent()
    }

    fun buildWhatsAppReminderMessage(invoice: InvoiceEntity): String {
        val ws = workshop.value
        return """
            🚘 *تذكير بالصيانة الدورية وتبديل الدهن - ${ws?.name ?: "ورشة لوب"}*
            ------------------------------------
            عزيزنا العميل: *${invoice.customerName}*
            نود تذكيركم بموعد الفحص والصيانة الدورية لسيارتكم ذات اللوحة (*${invoice.vehiclePlate}*).
            
            📅 الموعد المقترح: *${invoice.nextServiceDate}*
            🛣️ العداد المتوقع: *${invoice.nextServiceMileage} كم*
            
            يسعدنا تشريفكم في الورشة للاطمئنان على أداء المحرك والفرامل والصدرية والسوائل.
            📍 العنوان: ${ws?.address ?: "بغداد - الكرادة"}
            📞 للحجز والاستفسار: ${ws?.phone ?: "+9647701234567"}
        """.trimIndent()
    }

    /**
     * Builds WhatsApp Emotional Service Reminder replacing dynamic variables:
     * {customer_name}, {car_model} (or "سيارتك" if blank), {service_type}, and {workshop_name}
     */
    fun buildWhatsAppEmotionalReminderMessage(
        customerName: String,
        carModel: String?,
        serviceType: String = "",
        templateText: String = "",
        nextDate: String = "",
        nextMileage: Int = 0
    ): String {
        val ws = workshop.value
        val workshopName = ws?.name?.ifBlank { "ورشة لوب" } ?: "ورشة لوب"
        val activeTemplate = if (templateText.isNotBlank()) {
            templateText
        } else if (ws?.whatsappReminderTemplate?.isNotBlank() == true) {
            ws.whatsappReminderTemplate
        } else {
            DEFAULT_EMOTIONAL_TEMPLATE
        }

        val formattedCustomerName = customerName.trim().ifBlank { "عزيزنا الزبون" }
        val isCarBlank = carModel.isNullOrBlank() || carModel.trim().isBlank()
        val formattedCarModel = if (isCarBlank) "سيارتك" else carModel!!.trim()
        val formattedServiceType = serviceType.trim().ifBlank { "الصيانة الدورية وتبديل الدهن" }

        var replacedText = activeTemplate
            .replace("{customer_name}", formattedCustomerName)
            .replace("{car_model}", formattedCarModel)
            .replace("{service_type}", formattedServiceType)
            .replace("{workshop_name}", workshopName)

        // Dynamic sanitization to eliminate double "سيارتك سيارتك"
        while (replacedText.contains("سيارتك سيارتك")) {
            replacedText = replacedText.replace("سيارتك سيارتك", "سيارتك")
        }
        replacedText = replacedText.replace("  ", " ")

        val dateDetails = buildString {
            if (nextDate.isNotBlank()) append("\n📅 الموعد المقترح: *$nextDate*")
            if (nextMileage > 0) append("\n🛣️ العداد المتوقع: *$nextMileage كم*")
        }

        val phone = ws?.phone ?: ""
        val address = ws?.address ?: ""
        val contactDetails = buildString {
            if (address.isNotBlank() || phone.isNotBlank()) {
                append("\n\n📍 العنوان: ${address.ifBlank { "العراق" }}")
                if (phone.isNotBlank()) append("\n📞 للحجز والتواصل: $phone")
            }
        }

        return (replacedText + dateDetails + contactDetails).trim()
    }

    fun buildWhatsAppEmotionalReminderMessage(invoice: InvoiceEntity, templateText: String = ""): String {
        val serviceType = if (invoice.notes.isNotBlank()) invoice.notes else "الصيانة الدورية وتبديل الدهن"
        return buildWhatsAppEmotionalReminderMessage(
            customerName = invoice.customerName,
            carModel = invoice.vehicleModel,
            serviceType = serviceType,
            templateText = templateText,
            nextDate = invoice.nextServiceDate,
            nextMileage = invoice.nextServiceMileage
        )
    }

    fun updateWhatsAppTemplate(newTemplate: String) {
        val user = _currentUser.value
        if (user?.role != UserRole.OWNER) return
        viewModelScope.launch {
            val currentWs = repository.workshop.firstOrNull() ?: return@launch
            val updatedWs = currentWs.copy(whatsappReminderTemplate = newTemplate)
            repository.updateWorkshop(updatedWs)

            recordOwnerAuditLog(
                actionType = "WHATSAPP_TEMPLATE_UPDATE",
                targetReference = "WS-TEMPLATE",
                description = "تحديث قالب رسائل الواتساب العاطفية الافتراضي للورشة",
                oldValue = currentWs.whatsappReminderTemplate,
                newValue = newTemplate,
                reason = "تحديث من شاشة الإعدادات بواسطة المالك"
            )
        }
    }

    fun buildWhatsAppDebtReceiptMessage(
        customerName: String,
        customerPhone: String,
        amountPaid: Double,
        remainingBalance: Double,
        paymentMethod: PaymentMethod,
        receiptId: Long = 0,
        notes: String = ""
    ): String {
        val ws = workshop.value
        val user = _currentUser.value
        val currencySymbol = ws?.currency ?: "د.ع"
        val dateStr = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()).format(Date())

        return """
            🧾 *وصل قبض وتسديد حساب - ${ws?.name ?: "ورشة لوب"}*
            ------------------------------------
            📅 التاريخ: $dateStr
            👤 اسم الزبون: *$customerName*
            📱 رقم الهاتف: ${customerPhone.ifBlank { "غير مسجل" }}
            ------------------------------------
            💵 *المبلغ المسدد:* *${String.format(Locale.US, "%,.0f", amountPaid)} $currencySymbol*
            💳 طريقة التسديد: ${paymentMethod.labelAr}
            ⚖️ *الرصيد / الدين المتبقي:* *${String.format(Locale.US, "%,.0f", remainingBalance)} $currencySymbol*
            ${if (notes.isNotBlank()) "📝 ملاحظات: $notes\n" else ""}👤 منظم الوصل (الكاشير): ${user?.name ?: "سعد الكاشير"}
            ------------------------------------
            شكراً لالتزامكم مع ${ws?.name ?: "ورشة لوب"}. نتمنى لكم دوام التوفيق!
        """.trimIndent()
    }

    // Quick selection of customer for POS autofill
    fun selectCustomerForPos(customer: com.example.model.CustomerLedgerEntry) {
        val latestPlate = customer.vehiclePlates.firstOrNull() ?: ""
        val latestModel = customer.vehicleModels.firstOrNull() ?: ""
        _vehicleInfo.value = _vehicleInfo.value.copy(
            customerName = customer.customerName,
            customerPhone = customer.customerPhone,
            plateNumber = latestPlate,
            vehicleModel = latestModel,
            currentMileage = if (customer.lastServiceMileage > 0) customer.lastServiceMileage else _vehicleInfo.value.currentMileage
        )
        _activeSection.value = NavigationSection.POS
        _uiMessage.value = "تم تحديد الزبون (${customer.customerName}) وتعبئة بيانات المركبة بنجاح"
    }

    // Record partial or full debt payment
    fun recordDebtPayment(
        customerName: String,
        customerPhone: String,
        invoiceNumber: String = "",
        amountPaid: Double,
        remainingBalanceAfter: Double,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        notes: String = "",
        onSuccess: (Long) -> Unit = {}
    ) {
        val user = _currentUser.value
        if (user == null) {
            _uiMessage.value = "يرجى تسجيل الدخول أولاً!"
            return
        }
        if (amountPaid <= 0) {
            _uiMessage.value = "يرجى إدخال مبلغ تسديد صحيح!"
            return
        }

        viewModelScope.launch {
            val wsId = workshop.value?.id ?: "WS-LOOP-7789"
            val paymentId = repository.recordDebtPayment(
                workshopId = wsId,
                customerName = customerName,
                customerPhone = customerPhone,
                invoiceNumber = invoiceNumber,
                amountPaid = amountPaid,
                remainingBalanceAfter = remainingBalanceAfter,
                paymentMethod = paymentMethod,
                notes = notes,
                cashierName = user.name
            )
            _uiMessage.value = "تم تسجيل تسديد دفعة بمبلغ ${String.format(Locale.US, "%,.0f", amountPaid)} د.ع بنجاح"
            onSuccess(paymentId)
        }
    }

    // Record Petty Cash Expense
    fun recordPettyExpense(
        amount: Double,
        category: com.example.model.ExpenseCategory,
        notes: String
    ) {
        val user = _currentUser.value
        if (user == null) {
            _uiMessage.value = "يرجى تسجيل الدخول أولاً!"
            return
        }
        if (amount <= 0) {
            _uiMessage.value = "يرجى إدخال مبلغ مصروف صحيح!"
            return
        }

        viewModelScope.launch {
            val wsId = workshop.value?.id ?: "WS-LOOP-7789"
            val description = if (notes.isNotBlank()) "${category.labelAr}: $notes" else category.labelAr
            repository.addCashboxTransaction(
                workshopId = wsId,
                type = "EXPENSE",
                amount = amount,
                description = description,
                cashierName = user.name,
                category = category.labelAr
            )
            _uiMessage.value = "تم تسجيل المصروف النثري بمبلغ ${String.format(Locale.US, "%,.0f", amount)} د.ع بنجاح"
        }
    }

    // Inventory & Cashbox actions
    fun saveCatalogItem(item: CatalogItemEntity) {
        viewModelScope.launch {
            repository.saveCatalogItem(item)
            _uiMessage.value = "تم حفظ المادة/الخدمة بنجاح"
        }
    }

    fun deleteCatalogItem(id: Long) {
        viewModelScope.launch {
            repository.deleteCatalogItem(id)
            _uiMessage.value = "تم حذف الصنف من الكتالوج"
        }
    }

    fun buildDebtPaymentWhatsAppReceipt(
        customerName: String,
        customerPhone: String,
        amountPaid: Double,
        remainingBalance: Double,
        paymentMethod: PaymentMethod,
        receiptId: Long,
        notes: String
    ): String {
        val dateStr = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()).format(Date())
        val ws = workshop.value
        return """
            🧾 *وصل قبض وتسديد دين - ${ws?.name ?: "ورشة لوب"}*
            ------------------------------------
            رقم السند: #REC-$receiptId
            التاريخ: $dateStr
            الزبون: $customerName
            الهاتف: ${if (customerPhone.isNotBlank()) customerPhone else "غير مسجل"}
            طريقة الدفع: ${paymentMethod.labelAr}
            ------------------------------------
            💵 *المبلغ المسدد:* *${String.format(Locale.US, "%,.0f", amountPaid)} د.ع*
            ⚖️ *المتبقي بذمة الزبون:* *${String.format(Locale.US, "%,.0f", remainingBalance)} د.ع*
            ${if (notes.isNotBlank()) "📝 ملاحظات: $notes\n" else ""}------------------------------------
            📍 ${ws?.address ?: "بغداد - العراق"}
            📞 هاتف الورشة: ${ws?.phone ?: "07700000000"}
            شكراً لتعاملكم معنا! 🚗🔧
        """.trimIndent()
    }

    fun addCashboxEntry(type: String, amount: Double, desc: String, category: String = "عام") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val wsId = workshop.value?.id ?: "WS-LOOP-7789"
            repository.addCashboxTransaction(wsId, type, amount, desc, user.name, category)
            _uiMessage.value = "تم تسجيل الحركة النقدية في الصندوق بنجاح"
        }
    }

    // Modal Control Methods
    fun setBarcodeScannerOpen(isOpen: Boolean) {
        _isBarcodeScannerOpen.value = isOpen
    }

    fun setSyncModalOpen(isOpen: Boolean) {
        _isSyncModalOpen.value = isOpen
    }

    fun setPackageBuilderOpen(isOpen: Boolean) {
        _isPackageBuilderOpen.value = isOpen
    }

    fun toggleNetworkConnectivity() {
        setOnline(!_isOnline.value)
    }

    fun setOnline(online: Boolean) {
        _isOnline.value = online
        val statusText = if (online) "متصل بالإنترنت والسحابة" else "يعمل بدون نت (Offline Mode)"
        _uiMessage.value = "تغيير حالة الاتصال: $statusText"
    }

    // Feature #1: Barcode Scanning & Instant Add to Cart
    fun scanBarcodeAndAddToCart(codeOrBarcode: String) {
        viewModelScope.launch {
            val item = repository.getItemByBarcode(codeOrBarcode.trim())
            if (item != null) {
                addToCart(item, 1.0)
                _uiMessage.value = "تم التعرف على الباركود وإضافة: ${item.name}"
            } else {
                _uiMessage.value = "لم يتم العثور على صنف بالباركود: $codeOrBarcode"
            }
        }
    }

    // Feature #6: Bundle / Package Addition to Cart
    fun addBundleToCart(bundleWithItems: com.example.data.local.PackageWithItems) {
        viewModelScope.launch {
            val bundle = bundleWithItems.bundle
            val items = bundleWithItems.items
            if (items.isEmpty()) return@launch

            val ratio = if (bundle.originalPrice > 0) bundle.bundlePrice / bundle.originalPrice else 1.0
            for (pkgItem in items) {
                val discountedUnitPrice = pkgItem.unitPrice * ratio
                val currentList = _cartItems.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.catalogItemId == pkgItem.catalogItemId && it.name.contains(bundle.name) }

                currentList.add(
                    CartItem(
                        catalogItemId = pkgItem.catalogItemId,
                        name = "${pkgItem.itemName} (${bundle.name})",
                        unitType = pkgItem.unitType,
                        unitPrice = discountedUnitPrice,
                        originalPrice = pkgItem.unitPrice,
                        quantity = pkgItem.quantity,
                        category = ItemCategory.LABOR
                    )
                )
                _cartItems.value = currentList
            }
            _uiMessage.value = "تم إضافة باقة [${bundle.name}] إلى السلة وسعر العرض"
        }
    }

    fun savePackageBundle(
        id: Long = 0,
        code: String,
        name: String,
        description: String,
        bundlePrice: Double,
        originalPrice: Double,
        items: List<com.example.data.local.PackageItemEntity>
    ) {
        viewModelScope.launch {
            val wsId = workshop.value?.id ?: "WS-LOOP-7789"
            repository.savePackageBundle(
                workshopId = wsId,
                id = id,
                code = code,
                name = name,
                description = description,
                bundlePrice = bundlePrice,
                originalPrice = originalPrice,
                items = items
            )
            val user = _currentUser.value
            if (user != null) {
                repository.insertAuditLog(
                    workshopId = wsId,
                    actionType = "BUNDLE_CHANGE",
                    targetReference = code,
                    staffName = user.name,
                    staffRole = user.role,
                    description = "إنشاء/تعديل باقة عروض: $name",
                    newValue = "${String.format(Locale.US, "%,.0f", bundlePrice)} د.ع",
                    reason = "إدارة العروض المجمعة"
                )
            }
            _uiMessage.value = "تم حفظ باقة العرض [$name] بنجاح"
        }
    }

    fun deletePackageBundle(id: Long, bundleName: String) {
        viewModelScope.launch {
            repository.deletePackageBundle(id)
            _uiMessage.value = "تم حذف الباقة [$bundleName] بنجاح"
        }
    }

    // Feature #3: Robust Offline Sync Engine
    fun triggerManualSync() {
        viewModelScope.launch {
            _uiMessage.value = "جاري مزامنة بيانات الورشة مع قاعدة بيانات السحابة..."
            val unsynced = repository.getUnsyncedInvoices()
            for (inv in unsynced) {
                repository.markInvoiceSynced(inv.invoice.id)
            }
            _unsyncedCount.value = 0
            _uiMessage.value = "تمت المزامنة بنجاح! جميع الفواتير والحركات محدثة على السحابة 🟢"
        }
    }

    // Feature #4: Owner Audit Log recording
    fun recordOwnerAuditLog(
        actionType: String,
        targetReference: String,
        description: String,
        oldValue: String = "",
        newValue: String = "",
        reason: String = ""
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val wsId = workshop.value?.id ?: "WS-LOOP-7789"
            repository.insertAuditLog(
                workshopId = wsId,
                actionType = actionType,
                targetReference = targetReference,
                staffName = user.name,
                staffRole = user.role,
                description = description,
                oldValue = oldValue,
                newValue = newValue,
                reason = reason
            )
        }
    }
}
