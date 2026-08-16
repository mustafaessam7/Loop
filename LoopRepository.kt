package com.example.data.repository

import com.example.data.local.AuditLogEntity
import com.example.data.local.CashboxTransactionEntity
import com.example.data.local.CatalogItemEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceItemEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.LoopDatabase
import com.example.data.local.PackageBundleEntity
import com.example.data.local.PackageItemEntity
import com.example.data.local.PackageWithItems
import com.example.data.local.UserEntity
import com.example.data.local.WorkshopEntity
import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.model.UserRole
import com.example.model.VehicleServiceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoopRepository(private val db: LoopDatabase) {
    val workshop: Flow<WorkshopEntity?> = db.workshopDao().getWorkshop()
    val users: Flow<List<UserEntity>> = db.userDao().getAllUsers()
    val catalogItems: Flow<List<CatalogItemEntity>> = db.catalogDao().getAllItems()
    val invoicesWithItems: Flow<List<InvoiceWithItems>> = db.invoiceDao().getAllInvoicesWithItems()
    val maintenanceReminders: Flow<List<InvoiceEntity>> = db.invoiceDao().getInvoicesWithReminders()
    val cashboxTransactions: Flow<List<CashboxTransactionEntity>> = db.cashboxDao().getAllTransactions()
    val debtPayments: Flow<List<com.example.data.local.DebtPaymentEntity>> = db.debtPaymentDao().getAllPayments()
    val totalInflow: Flow<Double?> = db.cashboxDao().getTotalInflow()
    val totalExpenses: Flow<Double?> = db.cashboxDao().getTotalExpenses()
    val auditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getAllLogs()
    val packages: Flow<List<PackageWithItems>> = db.packageDao().getAllActivePackages()
    val allPackages: Flow<List<PackageWithItems>> = db.packageDao().getAllPackages()

    suspend fun insertAuditLog(
        workshopId: String,
        actionType: String,
        targetReference: String,
        staffName: String,
        staffRole: UserRole,
        description: String,
        oldValue: String = "",
        newValue: String = "",
        reason: String = ""
    ) = withContext(Dispatchers.IO) {
        db.auditLogDao().insertLog(
            com.example.data.local.AuditLogEntity(
                workshopId = workshopId,
                actionType = actionType,
                targetReference = targetReference,
                staffName = staffName,
                staffRole = staffRole,
                timestamp = System.currentTimeMillis(),
                description = description,
                oldValue = oldValue,
                newValue = newValue,
                reason = reason
            )
        )
    }

    suspend fun savePackageBundle(
        workshopId: String,
        id: Long = 0,
        code: String,
        name: String,
        description: String,
        bundlePrice: Double,
        originalPrice: Double,
        items: List<com.example.data.local.PackageItemEntity>
    ) = withContext(Dispatchers.IO) {
        val bundleEntity = com.example.data.local.PackageBundleEntity(
            id = id,
            workshopId = workshopId,
            code = code,
            name = name,
            description = description,
            bundlePrice = bundlePrice,
            originalPrice = originalPrice,
            isActive = true
        )
        if (id == 0L) {
            val bundleId = db.packageDao().insertBundle(bundleEntity)
            val bundleItems = items.map { it.copy(packageId = bundleId) }
            db.packageDao().insertBundleItems(bundleItems)
        } else {
            db.packageDao().updateBundle(bundleEntity)
            db.packageDao().deleteBundleItems(id)
            val bundleItems = items.map { it.copy(packageId = id) }
            db.packageDao().insertBundleItems(bundleItems)
        }
    }

    suspend fun deletePackageBundle(id: Long) = withContext(Dispatchers.IO) {
        db.packageDao().deleteBundle(id)
    }

    suspend fun getUnsyncedInvoices() = withContext(Dispatchers.IO) {
        db.invoiceDao().getUnsyncedInvoices()
    }

    suspend fun markInvoiceSynced(id: Long) = withContext(Dispatchers.IO) {
        db.invoiceDao().updateInvoiceSyncStatus(id, true)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        db.userDao().getUserByEmail(email)
    }

    suspend fun getUserByPin(pin: String): UserEntity? = withContext(Dispatchers.IO) {
        db.userDao().getUserByPin(pin)
    }

    suspend fun updateUserActiveDevice(userId: String, deviceId: String) = withContext(Dispatchers.IO) {
        val user = db.userDao().getUserByPin(userId) ?: db.userDao().getUserByEmail(userId)
        if (user != null) {
            db.userDao().updateUser(user.copy(activeDeviceId = deviceId, lastLoginTimestamp = System.currentTimeMillis()))
        }
    }

    suspend fun saveUser(user: UserEntity) = withContext(Dispatchers.IO) {
        db.userDao().insertUser(user)
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        db.userDao().deleteUser(userId)
    }

    suspend fun activateWorkshop(workshopId: String, licenseKey: String) = withContext(Dispatchers.IO) {
        val current = db.workshopDao().getWorkshopById(workshopId)
        if (current != null) {
            db.workshopDao().updateWorkshop(current.copy(isActivated = true, licenseKey = licenseKey))
        }
    }

    suspend fun updateWorkshop(workshop: WorkshopEntity) = withContext(Dispatchers.IO) {
        db.workshopDao().updateWorkshop(workshop)
    }

    suspend fun saveCatalogItem(item: CatalogItemEntity) = withContext(Dispatchers.IO) {
        if (item.id == 0L) {
            db.catalogDao().insertItem(item)
        } else {
            db.catalogDao().updateItem(item)
        }
    }

    suspend fun getItemByBarcode(barcode: String): CatalogItemEntity? = withContext(Dispatchers.IO) {
        db.catalogDao().getItemByBarcode(barcode.trim())
    }

    suspend fun deleteCatalogItem(id: Long) = withContext(Dispatchers.IO) {
        db.catalogDao().deleteItem(id)
    }

    suspend fun logAudit(
        workshopId: String,
        actionType: String,
        targetReference: String,
        staffName: String,
        staffRole: UserRole,
        description: String,
        oldValue: String = "",
        newValue: String = "",
        reason: String = ""
    ) = withContext(Dispatchers.IO) {
        db.auditLogDao().insertLog(
            AuditLogEntity(
                workshopId = workshopId,
                actionType = actionType,
                targetReference = targetReference,
                staffName = staffName,
                staffRole = staffRole,
                timestamp = System.currentTimeMillis(),
                description = description,
                oldValue = oldValue,
                newValue = newValue,
                reason = reason
            )
        )
    }

    suspend fun savePackage(
        workshopId: String,
        bundle: PackageBundleEntity,
        items: List<PackageItemEntity>
    ): Long = withContext(Dispatchers.IO) {
        val bundleId = if (bundle.id == 0L) {
            db.packageDao().insertBundle(bundle.copy(workshopId = workshopId))
        } else {
            db.packageDao().updateBundle(bundle)
            db.packageDao().deleteBundleItems(bundle.id)
            bundle.id
        }
        val itemsWithId = items.map { it.copy(packageId = bundleId) }
        db.packageDao().insertBundleItems(itemsWithId)
        bundleId
    }

    suspend fun deletePackage(id: Long) = withContext(Dispatchers.IO) {
        db.packageDao().deleteBundle(id)
    }

    suspend fun createInvoice(
        workshopId: String,
        cartItems: List<CartItem>,
        vehicleInfo: VehicleServiceInfo,
        discount: Double,
        taxRate: Double,
        paymentMethod: PaymentMethod,
        cashier: UserEntity,
        notes: String = "",
        isOnline: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        val count = db.invoiceDao().getInvoiceCount() + 1
        val datePrefix = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val invoiceNumber = "INV-$datePrefix-${String.format(Locale.US, "%04d", count)}"

        val subtotal = cartItems.sumOf { it.total }
        val discountedSubtotal = maxOf(0.0, subtotal - discount)
        val tax = (discountedSubtotal * taxRate)
        val total = discountedSubtotal + tax

        val invoice = InvoiceEntity(
            workshopId = workshopId,
            invoiceNumber = invoiceNumber,
            customerName = vehicleInfo.customerName.ifBlank { "عميل نقدي" },
            customerPhone = vehicleInfo.customerPhone,
            vehiclePlate = vehicleInfo.plateNumber,
            vehicleModel = vehicleInfo.vehicleModel,
            currentMileage = vehicleInfo.currentMileage,
            nextServiceMileage = vehicleInfo.nextServiceMileage,
            nextServiceDate = vehicleInfo.nextServiceDate,
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            total = total,
            paymentMethod = paymentMethod,
            cashierName = cashier.name,
            cashierRole = cashier.role,
            timestamp = System.currentTimeMillis(),
            isSynced = isOnline,
            notes = notes
        )

        val invoiceId = db.invoiceDao().insertInvoice(invoice)

        val invoiceItems = cartItems.map { cartItem ->
            InvoiceItemEntity(
                invoiceId = invoiceId,
                catalogItemId = cartItem.catalogItemId,
                itemName = cartItem.name,
                unitType = cartItem.unitType,
                quantity = cartItem.quantity,
                unitPrice = cartItem.unitPrice,
                totalPrice = cartItem.total
            )
        }
        db.invoiceDao().insertInvoiceItems(invoiceItems)

        // Reduce stock for physical inventory items
        for (item in cartItems) {
            if (item.catalogItemId > 0) {
                db.catalogDao().reduceStock(item.catalogItemId, item.quantity)
            }
        }

        // Record in cashbox if payment is cash
        if (paymentMethod == PaymentMethod.CASH) {
            db.cashboxDao().insertTransaction(
                CashboxTransactionEntity(
                    workshopId = workshopId,
                    type = "SALE",
                    amount = total,
                    description = "مبيعات فاتورة رقم $invoiceNumber",
                    timestamp = System.currentTimeMillis(),
                    cashierName = cashier.name
                )
            )
        }

        // Log discount if applied
        if (discount > 0) {
            logAudit(
                workshopId = workshopId,
                actionType = "DISCOUNT_APPLIED",
                targetReference = invoiceNumber,
                staffName = cashier.name,
                staffRole = cashier.role,
                description = "تم منح خصم بمقدار ${String.format(Locale.US, "%.0f", discount)} د.ع على فاتورة $invoiceNumber",
                oldValue = "${String.format(Locale.US, "%.0f", subtotal)} د.ع",
                newValue = "${String.format(Locale.US, "%.0f", total)} د.ع",
                reason = "خصم مباشر في نقطة البيع"
            )
        }

        invoiceId
    }

    suspend fun syncOfflineInvoices(): Int = withContext(Dispatchers.IO) {
        val unsynced = db.invoiceDao().getUnsyncedInvoices()
        for (inv in unsynced) {
            // Mark synced in local db (representing successful cloud push)
            db.invoiceDao().updateInvoiceSyncStatus(inv.invoice.id, true)
        }
        unsynced.size
    }

    suspend fun getUnsyncedCount(): Int = withContext(Dispatchers.IO) {
        db.invoiceDao().getUnsyncedInvoices().size
    }

    suspend fun addCashboxTransaction(
        workshopId: String,
        type: String,
        amount: Double,
        description: String,
        cashierName: String,
        category: String = "عام"
    ) = withContext(Dispatchers.IO) {
        db.cashboxDao().insertTransaction(
            CashboxTransactionEntity(
                workshopId = workshopId,
                type = type,
                amount = amount,
                description = description,
                category = category,
                timestamp = System.currentTimeMillis(),
                cashierName = cashierName
            )
        )
    }

    suspend fun recordDebtPayment(
        workshopId: String,
        customerName: String,
        customerPhone: String,
        invoiceNumber: String,
        amountPaid: Double,
        remainingBalanceAfter: Double,
        paymentMethod: PaymentMethod,
        notes: String,
        cashierName: String
    ): Long = withContext(Dispatchers.IO) {
        val payment = com.example.data.local.DebtPaymentEntity(
            workshopId = workshopId,
            customerName = customerName,
            customerPhone = customerPhone,
            invoiceNumber = invoiceNumber,
            amountPaid = amountPaid,
            remainingBalanceAfter = remainingBalanceAfter,
            paymentMethod = paymentMethod,
            notes = notes,
            timestamp = System.currentTimeMillis(),
            cashierName = cashierName
        )
        val paymentId = db.debtPaymentDao().insertPayment(payment)

        val txType = if (paymentMethod == PaymentMethod.CASH) "DEBT_PAYMENT" else "INFLOW"
        db.cashboxDao().insertTransaction(
            CashboxTransactionEntity(
                workshopId = workshopId,
                type = txType,
                amount = amountPaid,
                description = "تسديد دين: $customerName (${paymentMethod.labelAr})",
                category = "تسديد ديون",
                timestamp = System.currentTimeMillis(),
                cashierName = cashierName
            )
        )

        paymentId
    }

    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems? = withContext(Dispatchers.IO) {
        db.invoiceDao().getInvoiceWithItemsById(id)
    }
}

