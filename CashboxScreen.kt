package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CashboxTransactionEntity
import com.example.data.local.UserEntity
import com.example.data.local.WorkshopEntity
import com.example.model.ExpenseCategory
import com.example.model.UserRole
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopDangerRed
import com.example.ui.theme.LoopSuccessGreen
import com.example.ui.theme.LoopTealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CashboxScreen(
    transactions: List<CashboxTransactionEntity>,
    currentUser: UserEntity?,
    workshop: WorkshopEntity? = null,
    onAddTransaction: (type: String, amount: Double, note: String) -> Unit,
    onRecordPettyExpense: (amount: Double, category: ExpenseCategory, note: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isStaff = currentUser?.role == UserRole.STAFF
    val currency = workshop?.currency ?: "د.ع"

    var isPettyExpenseDialogOpen by remember { mutableStateOf(false) }
    var isDepositDialogOpen by remember { mutableStateOf(false) }
    var isEndOfDaySummaryOpen by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, SALES, DEBT_COLLECTION, EXPENSES

    val salesInflows = transactions.filter { it.type == "SALE" || (it.type == "INFLOW" && it.category == "مبيعات نقدية") }.sumOf { it.amount }
    val debtRepaymentsInflows = transactions.filter { it.category == "تسديد ديون" || (it.type == "INFLOW" && it.description.contains("تسديد")) }.sumOf { it.amount }
    val otherInflows = transactions.filter { it.type == "INFLOW" && it.category != "مبيعات نقدية" && !it.description.contains("تسديد") }.sumOf { it.amount }
    val totalInflows = salesInflows + debtRepaymentsInflows + otherInflows

    val totalExpenses = transactions.filter { it.type == "EXPENSE" || it.type == "WITHDRAWAL" }.sumOf { it.amount }
    val currentBalance = totalInflows - totalExpenses

    val filteredTransactions = transactions.filter { tx ->
        when (selectedFilter) {
            "SALES" -> tx.type == "SALE" || tx.category == "مبيعات نقدية"
            "DEBT_COLLECTION" -> tx.category == "تسديد ديون" || tx.description.contains("تسديد")
            "EXPENSES" -> tx.type == "EXPENSE" || tx.type == "WITHDRAWAL"
            else -> true
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الصندوق والمصروفات اليومية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "مطابقة النقدية، المصروفات النثرية، وإقفال الوردية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isStaff) {
                        Button(
                            onClick = { isPettyExpenseDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LoopDangerRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("record_petty_expense_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسجيل مصروف نثري", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { isDepositDialogOpen = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = LoopTealPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إيداع كاش", fontSize = 12.sp, color = LoopTealPrimary)
                        }
                    }

                    IconButton(
                        onClick = { isEndOfDaySummaryOpen = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(LoopTealPrimary.copy(alpha = 0.12f))
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "إقفال اليومية", tint = LoopTealPrimary)
                    }
                }
            }

            // Summary KPI Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cashbox Drawer Balance
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LoopSuccessGreen.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalAtm, contentDescription = null, tint = LoopSuccessGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("رصيد الدرج الفعلي", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LoopSuccessGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%,.0f", currentBalance)} $currency",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = LoopSuccessGreen
                        )
                        Text(
                            text = "نقدية حالية في القاصة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Inflow
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LoopTealPrimary.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = LoopTealPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إجمالي المقبوضات", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LoopTealPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%,.0f", totalInflows)} $currency",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = LoopTealPrimary
                        )
                        Text(
                            text = "مبيعات + تسديد ديون",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Expenses
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LoopDangerRed.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = LoopDangerRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("المصروفات النثرية", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LoopDangerRed)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%,.0f", totalExpenses)} $currency",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = LoopDangerRed
                        )
                        Text(
                            text = "طعام، وقود، نثريات",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Filters
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("كافة الحركات (${transactions.size})") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "SALES",
                        onClick = { selectedFilter = "SALES" },
                        label = { Text("مبيعات نقدية") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "DEBT_COLLECTION",
                        onClick = { selectedFilter = "DEBT_COLLECTION" },
                        label = { Text("تسديد ديون مقبوضة") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "EXPENSES",
                        onClick = { selectedFilter = "EXPENSES" },
                        label = { Text("المصروفات والسحوبات") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Transactions History List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    val dateStr = SimpleDateFormat("hh:mm a - yyyy/MM/dd", Locale.getDefault()).format(Date(tx.timestamp))
                    val isInflow = tx.type == "INFLOW" || tx.type == "SALE" || tx.type == "DEPOSIT"
                    val isDebtRepayment = tx.category == "تسديد ديون" || tx.description.contains("تسديد")

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isInflow) LoopSuccessGreen.copy(alpha = 0.15f)
                                            else LoopDangerRed.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isInflow) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = if (isInflow) LoopSuccessGreen else LoopDangerRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tx.description.ifBlank { if (isInflow) "مبيعات نقدية" else "مصروفات عامة" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (tx.category.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (isDebtRepayment) LoopTealPrimary.copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tx.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isDebtRepayment) LoopTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${tx.cashierName} | $dateStr",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "${if (isInflow) "+" else "-"}${String.format(Locale.US, "%,.0f", tx.amount)} $currency",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isInflow) LoopSuccessGreen else LoopDangerRed
                            )
                        }
                    }
                }
            }
        }

        // Petty Cash Expense Dialog
        if (isPettyExpenseDialogOpen) {
            var amountStr by remember { mutableStateOf("") }
            var selectedCategory by remember { mutableStateOf(ExpenseCategory.MEALS) }
            var noteStr by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { isPettyExpenseDialogOpen = false },
                title = {
                    Text("تسجيل مصروف نثري من الصندوق", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "نوع المصروف:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Category Chips with icons
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ExpenseCategory.values().forEach { cat ->
                                val isSel = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) LoopDangerRed else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${cat.icon} ${cat.labelAr}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("مبلغ المصروف ($currency)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("petty_expense_amount_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Quick presets for expenses
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(5000.0, 10000.0, 15000.0, 25000.0).forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { amountStr = String.format(Locale.US, "%.0f", preset) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${String.format(Locale.US, "%,.0f", preset)} د.ع",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = noteStr,
                            onValueChange = { noteStr = it },
                            label = { Text("البيان / تفاصيل المصروف") },
                            placeholder = { Text("مثال: وجبة غداء للعمال، كاز للمولد، أدوات صيانة...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                onRecordPettyExpense(amount, selectedCategory, noteStr.trim())
                                isPettyExpenseDialogOpen = false
                            }
                        },
                        enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = LoopDangerRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_petty_expense_button")
                    ) {
                        Text("خصم من الصندوق وحفظ")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { isPettyExpenseDialogOpen = false }, shape = RoundedCornerShape(10.dp)) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // Manual Deposit Dialog
        if (isDepositDialogOpen) {
            var amountStr by remember { mutableStateOf("") }
            var noteStr by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { isDepositDialogOpen = false },
                title = { Text("إيداع كاش في الصندوق", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("المبلغ المودع ($currency)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = noteStr,
                            onValueChange = { noteStr = it },
                            label = { Text("سبب الإيداع / البيان") },
                            placeholder = { Text("مثال: عهدة افتتاحية، إيداع مالك...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                onAddTransaction("INFLOW", amount, noteStr.ifBlank { "إيداع نقدي" })
                                isDepositDialogOpen = false
                            }
                        },
                        enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إيداع")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { isDepositDialogOpen = false }, shape = RoundedCornerShape(10.dp)) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // End of Day Closing Summary Dialog
        if (isEndOfDaySummaryOpen) {
            val dateStr = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()).format(Date())
            val summaryText = """
                📊 *تقرير إقفال الصندوق اليومي - ${workshop?.name ?: "ورشة لوب"}*
                ------------------------------------
                التاريخ: $dateStr
                المسؤول: ${currentUser?.name ?: "الكاشير"} (${currentUser?.role?.labelAr ?: ""})
                ------------------------------------
                💵 مبيعات الفواتير النقدية: ${String.format(Locale.US, "%,.0f", salesInflows)} $currency
                📥 تسديدات ديون مقبوضة: ${String.format(Locale.US, "%,.0f", debtRepaymentsInflows)} $currency
                ➕ إيداعات أخرى: ${String.format(Locale.US, "%,.0f", otherInflows)} $currency
                ------------------------------------
                💰 *إجمالي المقبوضات:* *${String.format(Locale.US, "%,.0f", totalInflows)} $currency*
                🔻 *إجمالي المصروفات النثرية:* *${String.format(Locale.US, "%,.0f", totalExpenses)} $currency*
                ------------------------------------
                ⚖️ *صافي النقدية المتوفرة في القاصة (الدرج):*
                *${String.format(Locale.US, "%,.0f", currentBalance)} $currency*
                ------------------------------------
                نظام إدارة الورش الذكي Loop Auto Workshop
            """.trimIndent()

            AlertDialog(
                onDismissRequest = { isEndOfDaySummaryOpen = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = LoopTealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إقفال الصندوق ومطابقة الدرج اليومي", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "مطابقة رصيد القاصة الفعلي بنهاية اليومية:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("المبيعات النقدية:", style = MaterialTheme.typography.bodySmall)
                                    Text("${String.format(Locale.US, "%,.0f", salesInflows)} $currency", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("تسديدات الديون المقبوضة:", style = MaterialTheme.typography.bodySmall)
                                    Text("${String.format(Locale.US, "%,.0f", debtRepaymentsInflows)} $currency", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = LoopTealPrimary)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("المصروفات النثرية والوجبات:", style = MaterialTheme.typography.bodySmall)
                                    Text("-${String.format(Locale.US, "%,.0f", totalExpenses)} $currency", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = LoopDangerRed)
                                }
                                HorizontalDivider()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("صافي رصيد القاصة النهائي:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${String.format(Locale.US, "%,.0f", currentBalance)} $currency",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = LoopSuccessGreen
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                val uri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(summaryText)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } catch (e: Exception) {
                                clipboardManager.setText(AnnotatedString(summaryText))
                                Toast.makeText(context, "تم نسخ تقرير الإقفال إلى الحافظة", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LoopSuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("share_cashbox_closing_summary_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة التقرير عبر واتساب للمالك")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { isEndOfDaySummaryOpen = false }, shape = RoundedCornerShape(10.dp)) {
                        Text("إغلاق")
                    }
                }
            )
        }
    }
}
