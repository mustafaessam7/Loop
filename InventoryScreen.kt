package com.example.ui.screens

import com.example.data.local.WorkshopEntity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CatalogItemEntity
import com.example.data.local.UserEntity
import com.example.model.ItemCategory
import com.example.model.UnitType
import com.example.model.UserRole
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopDangerRed
import com.example.ui.theme.LoopTealPrimary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryScreen(
    catalogItems: List<CatalogItemEntity>,
    currentUser: UserEntity?,
    workshop: WorkshopEntity? = null,
    onSaveItem: (CatalogItemEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<CatalogItemEntity?>(null) }
    var isNewItemDialogOpen by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<ItemCategory?>(null) }
    var filterLowStockOnly by remember { mutableStateOf(false) }

    val isMasterDev = currentUser?.email.equals("mustafa000j@gmail.com", ignoreCase = true) || currentUser?.role == UserRole.MASTER_DEVELOPER
    val isOwner = currentUser?.role == UserRole.OWNER || isMasterDev
    val isStaff = currentUser?.role == UserRole.STAFF
    val canViewCosts = isOwner || (workshop?.allowCashierViewCosts == true)
    val lowStockCount = catalogItems.count { it.unitType != UnitType.SERVICE && it.stockQuantity <= it.minStockAlert }

    val filteredItems = catalogItems.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.code.contains(searchQuery, ignoreCase = true) ||
                item.category.labelAr.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategoryFilter == null || item.category == selectedCategoryFilter
        val matchesLowStock = !filterLowStockOnly || (item.unitType != UnitType.SERVICE && item.stockQuantity <= item.minStockAlert)

        matchesQuery && matchesCategory && matchesLowStock
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إدارة المخزون والخدمات والتنبيهات",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تعريف الزيوت باللتر، الفلاتر، القطع، ومراقبة النواقص",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isStaff) {
                        Button(
                            onClick = { isNewItemDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_new_catalog_item_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة صنف/خدمة")
                        }
                    }
                }

                // Low Stock Alert Banner
                if (lowStockCount > 0) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LoopDangerRed.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { filterLowStockOnly = !filterLowStockOnly }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = LoopDangerRed,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "تنبيه: يوجد $lowStockCount أصناف قاربت على النفاد بالمخزن!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LoopDangerRed
                                    )
                                    Text(
                                        text = "المخزون الحالي أقل من الحد الأدنى للطلب",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (filterLowStockOnly) LoopDangerRed else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (filterLowStockOnly) "عرض الكل" else "عرض النواقص فقط ⚠️",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (filterLowStockOnly) Color.White else LoopDangerRed
                                )
                            }
                        }
                    }
                }

                // Search & Filter
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث في الكتالوج بالاسم، الرمز، التصنيف...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Chips Filter
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null && !filterLowStockOnly,
                            onClick = {
                                selectedCategoryFilter = null
                                filterLowStockOnly = false
                            },
                            label = { Text("الكل (${catalogItems.size})") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    if (lowStockCount > 0) {
                        item {
                            FilterChip(
                                selected = filterLowStockOnly,
                                onClick = { filterLowStockOnly = !filterLowStockOnly },
                                label = { Text("⚠️ النواقص ($lowStockCount)") },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    items(ItemCategory.values()) { cat ->
                        val count = catalogItems.count { it.category == cat }
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                            label = { Text("${cat.labelAr} ($count)") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Items List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val isLowStock = item.unitType != UnitType.SERVICE && item.stockQuantity <= item.minStockAlert

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLowStock) LoopDangerRed.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                                text = item.unitType.labelAr,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.unitType.isFractional) LoopTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item.code,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isLowStock) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(LoopDangerRed)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "⚠️ نقص مخزون",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "سعر البيع: ${String.format(Locale.US, "%,.0f", item.salePrice)} د.ع",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = LoopTealPrimary
                                        )
                                        if (canViewCosts) {
                                            Text(
                                                text = "التكلفة: ${String.format(Locale.US, "%,.0f", item.costPrice)} د.ع",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (item.unitType != UnitType.SERVICE) {
                                            Text(
                                                text = "المخزون: ${item.stockQuantity} ${item.unitType.symbolAr} (تنبيه عند ≤ ${item.minStockAlert})",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isLowStock) LoopDangerRed else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                if (!isStaff) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { editingItem = item }) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = LoopTealPrimary)
                                        }
                                        IconButton(onClick = { onDeleteItem(item.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = LoopDangerRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Edit / Add Item Dialog
            if (isNewItemDialogOpen || editingItem != null) {
                val current = editingItem ?: CatalogItemEntity(
                    workshopId = "WS-LOOP-7789",
                    code = "ITEM-${System.currentTimeMillis() % 10000}",
                    name = "",
                    category = ItemCategory.OILS,
                    unitType = UnitType.LITER,
                    costPrice = 0.0,
                    salePrice = 0.0,
                    stockQuantity = 50.0,
                    minStockAlert = 10.0
                )

                EditCatalogItemDialog(
                    initialItem = current,
                    isNew = editingItem == null,
                    canViewCosts = canViewCosts,
                    onDismiss = {
                        isNewItemDialogOpen = false
                        editingItem = null
                    },
                    onConfirm = { saved ->
                        onSaveItem(saved)
                        isNewItemDialogOpen = false
                        editingItem = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditCatalogItemDialog(
    initialItem: CatalogItemEntity,
    isNew: Boolean,
    canViewCosts: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (CatalogItemEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialItem.name) }
    var code by remember { mutableStateOf(initialItem.code) }
    var category by remember { mutableStateOf(initialItem.category) }
    var unitType by remember { mutableStateOf(initialItem.unitType) }
    var costPriceStr by remember { mutableStateOf(if (initialItem.costPrice > 0) initialItem.costPrice.toString() else "") }
    var salePriceStr by remember { mutableStateOf(if (initialItem.salePrice > 0) initialItem.salePrice.toString() else "") }
    var stockStr by remember { mutableStateOf(initialItem.stockQuantity.toString()) }
    var minStockAlertStr by remember { mutableStateOf(initialItem.minStockAlert.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "إضافة مادة أو خدمة جديدة" else "تعديل المادة / الخدمة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الصنف أو الخدمة") },
                    placeholder = { Text("مثال: زيت تويوتا 5W-30 أصلي") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("رمز / كود الصنف") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("المخزون الحالي") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Minimum Stock Alert Setting
                OutlinedTextField(
                    value = minStockAlertStr,
                    onValueChange = { minStockAlertStr = it },
                    label = { Text("حد التنبيه لنقص المخزون (تنبيه عند الوصول له)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canViewCosts) {
                        OutlinedTextField(
                            value = costPriceStr,
                            onValueChange = { costPriceStr = it },
                            label = { Text("سعر التكلفة (د.ع)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = salePriceStr,
                        onValueChange = { salePriceStr = it },
                        label = { Text("سعر البيع (د.ع)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Category selector
                Text("التصنيف:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ItemCategory.values().forEach { cat ->
                        val isSel = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) LoopTealPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat.labelAr,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Unit Type selector (Liters, Pieces, Kg, Hours, Services)
                Text("نوع الوحدة (يدعم الكسور):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UnitType.values().forEach { unit ->
                        val isSel = unitType == unit
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) LoopTealPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { unitType = unit }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${unit.labelAr} ${if (unit.isFractional) "(كسور)" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val item = initialItem.copy(
                            name = name.trim(),
                            code = code.trim(),
                            category = category,
                            unitType = unitType,
                            costPrice = costPriceStr.toDoubleOrNull() ?: 0.0,
                            salePrice = salePriceStr.toDoubleOrNull() ?: 0.0,
                            stockQuantity = stockStr.toDoubleOrNull() ?: 0.0,
                            minStockAlert = minStockAlertStr.toDoubleOrNull() ?: 10.0
                        )
                        onConfirm(item)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("إلغاء")
            }
        }
    )
}
