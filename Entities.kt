package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.ItemCategory
import com.example.model.PaymentMethod
import com.example.model.UnitType
import com.example.model.UserRole

const val DEFAULT_EMOTIONAL_TEMPLATE = """أهلاً وسهلاً أستاذ {customer_name} 🌹
سيارتك {car_model} أمانتك وأمانة عائلتك بالدرب، وصيانتها بوقتها هي اللي تحميها وتوفر عليك وقت ومصاريف.
حبّينا نذكرك من ورشة {workshop_name} إن موعد {service_type} صار قريب حتى تبقى بقمة سلامتها ونشاطها.
تشرفنا بأي وقت، وراحتك وأمانك هي أولويتنا! ❤️"""

@Entity(tableName = "workshops")
data class WorkshopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val commercialReg: String,
    val phone: String,
    val email: String,
    val address: String,
    val taxNumber: String,
    val currency: String = "د.ع",
    val activeDeviceId: String = "DEV-PRIMARY-01",
    val whatsappReminderTemplate: String = DEFAULT_EMOTIONAL_TEMPLATE,
    val isActivated: Boolean = false,
    val licenseKey: String = "",
    val allowCashierViewCosts: Boolean = false
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val workshopId: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val pinCode: String,
    val activeDeviceId: String,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "catalog_items")
data class CatalogItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workshopId: String,
    val code: String,
    val barcode: String = "",
    val name: String,
    val category: ItemCategory,
    val unitType: UnitType,
    val costPrice: Double,
    val salePrice: Double,
    val stockQuantity: Double,
    val minStockAlert: Double = 5.0
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workshopId: String,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String,
    val vehiclePlate: String,
    val vehicleModel: String,
    val currentMileage: Int,
    val nextServiceMileage: Int,
    val nextServiceDate: String,
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val cashierName: String,
    val cashierRole: UserRole,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true,
    val notes: String = ""
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val catalogItemId: Long,
    val itemName: String,
    val unitType: UnitType,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double
)

@Entity(tableName = "cashbox_transactions")
data class CashboxTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workshopId: String,
    val type: String, // SALE, EXPENSE, DEPOSIT, WITHDRAWAL, DEBT_PAYMENT
    val amount: Double,
    val description: String,
    val category: String = "عام", // وجبات طعام، شاي وضيافة، كهرباء ومولد، مواد تنظيف، عدد وأدوات، أخرى
    val timestamp: Long = System.currentTimeMillis(),
    val cashierName: String
)

@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workshopId: String,
    val customerName: String,
    val customerPhone: String,
    val invoiceNumber: String = "",
    val amountPaid: Double,
    val remainingBalanceAfter: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val cashierName: String
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workshopId: String,
    val actionType: String, // INVOICE_EDIT, INVOICE_DELETE, PRICE_OVERRIDE, DISCOUNT_APPLIED, MANUAL_STOCK_EDIT, CASHBOX_DRAWER, BUNDLE_CHANGE
    val targetReference: String, // e.g. INV-2026-00101
    val staffName: String,
    val staffRole: UserRole,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val oldValue: String = "",
    val newValue: String = "",
    val reason: String = ""
)

@Entity(tableName = "package_bundles")
data class PackageBundleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workshopId: String,
    val code: String,
    val name: String,
    val description: String,
    val bundlePrice: Double,
    val originalPrice: Double,
    val iconName: String = "auto_awesome",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "package_items",
    foreignKeys = [
        ForeignKey(
            entity = PackageBundleEntity::class,
            parentColumns = ["id"],
            childColumns = ["packageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["packageId"])]
)
data class PackageItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageId: Long,
    val catalogItemId: Long,
    val itemName: String,
    val quantity: Double,
    val unitType: UnitType,
    val unitPrice: Double
)

