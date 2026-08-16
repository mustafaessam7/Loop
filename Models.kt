package com.example.model

enum class UserRole(val labelAr: String, val descriptionAr: String) {
    MASTER_DEVELOPER("مطور النظام الرئيسي (Master Dev)", "إشراف تام على كافة الورش، توليد مفاتيح التفعيل، والوصول الكامل"),
    OWNER("المالك (Owner)", "إدارة كاملة، تقارير أرباح، إشراف على الصندوق، ويُمنع من إصدار الفواتير"),
    HEAD_CASHIER("الكاشير الرئيسي (Head Cashier)", "إصدار فواتير كاملة، إدارة الصندوق، الخصومات، ومتابعة الديون"),
    STAFF("فني / مندوب (Staff)", "إصدار فواتير بيع وخدمات بأسعار ثابتة محددة مسبقاً فقط")
}

enum class UnitType(val labelAr: String, val symbolAr: String, val isFractional: Boolean) {
    LITER("لتر", "لتر", true),
    PIECE("قطعة", "حبة", false),
    KG("كيلوجرام", "كجم", true),
    BOTTLE("عبوة/علبة", "علبة", false),
    HOUR("ساعة عمل", "ساعة", true),
    SERVICE("خدمة يدوية", "خدمة", false)
}

enum class ItemCategory(val labelAr: String, val iconName: String) {
    OILS("زيوت وسوائل", "oil_barrel"),
    FILTERS("فلاتر وبواجي", "filter_alt"),
    BRAKES("فرامل ومكابح", "disc_full"),
    TIRES("إطارات وميزان", "tire_repair"),
    BATTERIES("بطاريات وكهرباء", "battery_charging_full"),
    LABOR("أجور وخدمات فنية", "build")
}

enum class PaymentMethod(val labelAr: String) {
    CASH("نقداً (كاش)"),
    ZAIN_CASH("زين كاش"),
    CARD("ماستر كارد / كي كارد"),
    DEBT("آجل / ديون")
}

enum class NavigationSection(val titleAr: String) {
    POS("نقطة البيع (POS)"),
    PACKAGES("الباقات والعروض ⭐"),
    INVOICES("سجل الفواتير"),
    CUSTOMERS("الزبائن والديون"),
    MAINTENANCE("مواعيد الصيانة"),
    INVENTORY("المخزون والخدمات"),
    CASHBOX("الصندوق والمصاريف"),
    REPORTS("التقارير المالي"),
    AUDIT_LOGS("سجل أمان المالك 🛡️"),
    MASTER_DEV("لوحة المطور الرئيسي 👑")
}

data class CartItem(
    val catalogItemId: Long,
    val name: String,
    val unitType: UnitType,
    val unitPrice: Double,
    val originalPrice: Double,
    val quantity: Double,
    val category: ItemCategory,
    val notes: String = ""
) {
    val total: Double get() = quantity * unitPrice
}

enum class ExpenseCategory(val labelAr: String, val icon: String) {
    MEALS("وجبات طعام وغداء", "restaurant"),
    HOSPITALITY("شاي وضيافة زبائن", "local_cafe"),
    UTILITIES("كهرباء ومولد ومحروقات", "bolt"),
    CLEANING("مواد تنظيف واستهلاكية", "cleaning_services"),
    TOOLS("عدد وأدوات ورشة", "build"),
    MAINTENANCE_SUPPLIES("صيانة ومشتريات فورية", "shopping_cart"),
    OTHER("مصروفات أخرى", "payments")
}

data class VehicleServiceInfo(
    val plateNumber: String = "",
    val vehicleModel: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val currentMileage: Int = 0,
    val nextServiceMileage: Int = 0,
    val nextServiceDate: String = "",
    val serviceNotes: String = ""
)

data class CustomerLedgerEntry(
    val customerName: String,
    val customerPhone: String,
    val totalDebtInvoiced: Double,
    val totalPaid: Double,
    val remainingDebt: Double,
    val vehiclePlates: List<String>,
    val vehicleModels: List<String>,
    val lastServiceDate: String,
    val lastServiceMileage: Int,
    val totalInvoicesCount: Int,
    val hasOutstandingDebt: Boolean
)
