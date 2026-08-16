package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.ItemCategory
import com.example.model.PaymentMethod
import com.example.model.UnitType
import com.example.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.STAFF
    }

    @TypeConverter
    fun fromUnitType(unit: UnitType): String = unit.name

    @TypeConverter
    fun toUnitType(value: String): UnitType = try {
        UnitType.valueOf(value)
    } catch (e: Exception) {
        UnitType.PIECE
    }

    @TypeConverter
    fun fromItemCategory(cat: ItemCategory): String = cat.name

    @TypeConverter
    fun toItemCategory(value: String): ItemCategory = try {
        ItemCategory.valueOf(value)
    } catch (e: Exception) {
        ItemCategory.OILS
    }

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String = method.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = try {
        PaymentMethod.valueOf(value)
    } catch (e: Exception) {
        PaymentMethod.CASH
    }
}

@Database(
    entities = [
        WorkshopEntity::class,
        UserEntity::class,
        CatalogItemEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        CashboxTransactionEntity::class,
        DebtPaymentEntity::class,
        AuditLogEntity::class,
        PackageBundleEntity::class,
        PackageItemEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LoopDatabase : RoomDatabase() {
    abstract fun workshopDao(): WorkshopDao
    abstract fun userDao(): UserDao
    abstract fun catalogDao(): CatalogDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun cashboxDao(): CashboxDao
    abstract fun debtPaymentDao(): DebtPaymentDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun packageDao(): PackageDao

    companion object {
        @Volatile
        private var INSTANCE: LoopDatabase? = null

        fun getInstance(context: Context): LoopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LoopDatabase::class.java,
                    "loop_workshop_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).populateInitialData()
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun populateInitialData() {
        InitialData.seed(this)
    }
}
