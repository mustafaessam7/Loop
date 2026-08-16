package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class InvoiceWithItems(
    @androidx.room.Embedded val invoice: InvoiceEntity,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>
)

data class PackageWithItems(
    @androidx.room.Embedded val bundle: PackageBundleEntity,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "packageId"
    )
    val items: List<PackageItemEntity>
)

@Dao
interface WorkshopDao {
    @Query("SELECT * FROM workshops LIMIT 1")
    fun getWorkshop(): Flow<WorkshopEntity?>

    @Query("SELECT * FROM workshops WHERE id = :id")
    suspend fun getWorkshopById(id: String): WorkshopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkshop(workshop: WorkshopEntity)

    @Update
    suspend fun updateWorkshop(workshop: WorkshopEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE pinCode = :pin LIMIT 1")
    suspend fun getUserByPin(pin: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface CatalogDao {
    @Query("SELECT * FROM catalog_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<CatalogItemEntity>>

    @Query("SELECT * FROM catalog_items WHERE category = :category ORDER BY name ASC")
    fun getItemsByCategory(category: String): Flow<List<CatalogItemEntity>>

    @Query("SELECT * FROM catalog_items WHERE id = :id")
    suspend fun getItemById(id: Long): CatalogItemEntity?

    @Query("SELECT * FROM catalog_items WHERE barcode = :barcode OR code = :barcode LIMIT 1")
    suspend fun getItemByBarcode(barcode: String): CatalogItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CatalogItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CatalogItemEntity): Long

    @Update
    suspend fun updateItem(item: CatalogItemEntity)

    @Query("UPDATE catalog_items SET stockQuantity = stockQuantity - :amount WHERE id = :id")
    suspend fun reduceStock(id: Long, amount: Double)

    @Query("DELETE FROM catalog_items WHERE id = :id")
    suspend fun deleteItem(id: Long)
}

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems?

    @Transaction
    @Query("SELECT * FROM invoices WHERE isSynced = 0")
    suspend fun getUnsyncedInvoices(): List<InvoiceWithItems>

    @Query("SELECT * FROM invoices WHERE nextServiceDate != '' ORDER BY timestamp DESC")
    fun getInvoicesWithReminders(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("UPDATE invoices SET isSynced = :isSynced WHERE id = :id")
    suspend fun updateInvoiceSyncStatus(id: Long, isSynced: Boolean)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoice(id: Long)

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getInvoiceCount(): Int
}

@Dao
interface CashboxDao {
    @Query("SELECT * FROM cashbox_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<CashboxTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: CashboxTransactionEntity)

    @Query("SELECT SUM(amount) FROM cashbox_transactions WHERE type IN ('SALE', 'DEPOSIT', 'DEBT_PAYMENT', 'INFLOW')")
    fun getTotalInflow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM cashbox_transactions WHERE type IN ('EXPENSE', 'WITHDRAWAL')")
    fun getTotalExpenses(): Flow<Double?>
}

@Dao
interface DebtPaymentDao {
    @Query("SELECT * FROM debt_payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<DebtPaymentEntity>>

    @Query("SELECT * FROM debt_payments WHERE customerPhone = :phone OR customerName = :name ORDER BY timestamp DESC")
    fun getPaymentsForCustomer(phone: String, name: String): Flow<List<DebtPaymentEntity>>

    @Query("SELECT SUM(amountPaid) FROM debt_payments WHERE customerPhone = :phone OR customerName = :name")
    suspend fun getTotalPaidByCustomer(phone: String, name: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: DebtPaymentEntity): Long
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE actionType = :actionType ORDER BY timestamp DESC")
    fun getLogsByAction(actionType: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long
}

@Dao
interface PackageDao {
    @Transaction
    @Query("SELECT * FROM package_bundles WHERE isActive = 1 ORDER BY id ASC")
    fun getAllActivePackages(): Flow<List<PackageWithItems>>

    @Transaction
    @Query("SELECT * FROM package_bundles ORDER BY id ASC")
    fun getAllPackages(): Flow<List<PackageWithItems>>

    @Transaction
    @Query("SELECT * FROM package_bundles WHERE id = :id")
    suspend fun getPackageById(id: Long): PackageWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundle(bundle: PackageBundleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundleItems(items: List<PackageItemEntity>)

    @Update
    suspend fun updateBundle(bundle: PackageBundleEntity)

    @Query("DELETE FROM package_items WHERE packageId = :packageId")
    suspend fun deleteBundleItems(packageId: Long)

    @Query("DELETE FROM package_bundles WHERE id = :id")
    suspend fun deleteBundle(id: Long)
}

