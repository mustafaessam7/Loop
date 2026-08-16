package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvoiceWithItems
import com.example.data.local.UserEntity
import com.example.data.local.WorkshopEntity
import com.example.model.CustomerLedgerEntry
import com.example.model.PaymentMethod
import com.example.model.UserRole
import com.example.ui.components.DebtReceiptDialog
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopDangerRed
import com.example.ui.theme.LoopSuccessGreen
import com.example.ui.theme.LoopTealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomersDebtsScreen(
    customerLedgers: List<CustomerLedgerEntry>,
    invoices: List<InvoiceWithItems>,
    workshop: WorkshopEntity?,
    currentUser: UserEntity?,
    onRecordPayment: (customerName: String, customerPhone: String, invoiceNo: String, amount: Double, remaining: Double, method: PaymentMethod, notes: String, onSuccess: (Long) -> Unit) -> Unit,
    onSelectCustomerForPos: (CustomerLedgerEntry) -> Unit,
    buildDebtReceiptMessage: (customerName: String, customerPhone: String, amountPaid: Double, remainingBalance: Double, paymentMethod: PaymentMethod, receiptId: Long, notes: String) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isStaff = currentUser?.role == UserRole.STAFF

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, WITH_DEBT, SETTLED
    var customerToPay by remember { mutableStateOf<CustomerLedgerEntry?>(null) }
    var activeReceiptData by remember { mutableStateOf<DebtReceiptState?>(null) }

    val filteredCustomers = customerLedgers.filter { customer ->
        val query = searchQuery.trim()
        val matchesQuery = query.isBlank() ||
                customer.customerName.contains(query, ignoreCase = true) ||
                customer.customerPhone.contains(query, ignoreCase = true) ||
                customer.vehiclePlates.any { it.contains(query, ignoreCase = true) } ||
                customer.vehicleModels.any { it.contains(query, ignoreCase = true) }

        val matchesFilter = when (selectedFilter) {
            "WITH_DEBT" -> customer.remainingDebt > 0
            "SETTLED" -> customer.remainingDebt <= 0
            else -> true
        }

        matchesQuery && matchesFilter
    }

    val totalWorkshopDebt = customerLedgers.sumOf { it.remainingDebt }
    val debtorsCount = customerLedgers.count { it.remainingDebt > 0 }
    val totalCollected = customerLedgers.sumOf { it.totalPaid }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Screen Title & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "سجل الزبائن والديون والتسديد",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "كشف حساب العملاء، الديون المستحقة، وتسجيل الدفعات الجزئية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary KPI Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Outstanding Debt
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LoopDangerRed.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = LoopDangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إجمالي الديون القائمة",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LoopDangerRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%,.0f", totalWorkshopDebt)} د.ع",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = LoopDangerRed
                        )
                        Text(
                            text = "$debtorsCount زبائن عليهم ديون",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Total Repayments Collected
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LoopSuccessGreen.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = LoopSuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "المقبوضات المسددة",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LoopSuccessGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%,.0f", totalCollected)} د.ع",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = LoopSuccessGreen
                        )
                        Text(
                            text = "تسديدات ديون مقبوضة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Total Customers
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LoopTealPrimary.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = LoopTealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إجمالي الزبائن",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LoopTealPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${customerLedgers.size} عميل",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = LoopTealPrimary
                        )
                        Text(
                            text = "مسجلين بالنظام",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث باسم الزبون، رقم الهاتف (+964)، أو رقم اللوحة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_debt_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("كافة الزبائن (${customerLedgers.size})") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "WITH_DEBT",
                        onClick = { selectedFilter = "WITH_DEBT" },
                        label = { Text("عليهم ديون قائمة ($debtorsCount)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "SETTLED",
                        onClick = { selectedFilter = "SETTLED" },
                        label = { Text("خالص الذمة (${customerLedgers.size - debtorsCount})") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Customer Cards List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد نتائج مطابقة لبحثك",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { "${it.customerName}_${it.customerPhone}" }) { customer ->
                        CustomerCard(
                            customer = customer,
                            invoices = invoices.filter {
                                (customer.customerPhone.isNotBlank() && it.invoice.customerPhone == customer.customerPhone) ||
                                        it.invoice.customerName.equals(customer.customerName, ignoreCase = true)
                            },
                            isStaff = isStaff,
                            onPayDebt = { customerToPay = customer },
                            onSelectForPos = { onSelectCustomerForPos(customer) },
                            onShareStatement = {
                                val statement = buildCustomerStatementMessage(customer, workshop)
                                try {
                                    val cleanPhone = customer.customerPhone.replace("+", "").replace(" ", "").trim()
                                    val uri = if (cleanPhone.isNotBlank()) {
                                        Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(statement)}")
                                    } else {
                                        Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(statement)}")
                                    }
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                } catch (e: Exception) {
                                    clipboardManager.setText(AnnotatedString(statement))
                                    Toast.makeText(context, "تم نسخ كشف الحساب إلى الحافظة", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Repayment Dialog
        customerToPay?.let { cust ->
            PartialRepaymentDialog(
                customer = cust,
                workshop = workshop,
                onDismiss = { customerToPay = null },
                onConfirm = { amount, method, notes ->
                    val remainingAfter = maxOf(0.0, cust.remainingDebt - amount)
                    onRecordPayment(
                        cust.customerName,
                        cust.customerPhone,
                        "",
                        amount,
                        remainingAfter,
                        method,
                        notes
                    ) { paymentId ->
                        val receiptMsg = buildDebtReceiptMessage(
                            cust.customerName,
                            cust.customerPhone,
                            amount,
                            remainingAfter,
                            method,
                            paymentId,
                            notes
                        )
                        activeReceiptData = DebtReceiptState(
                            customerName = cust.customerName,
                            customerPhone = cust.customerPhone,
                            amountPaid = amount,
                            remainingBalance = remainingAfter,
                            paymentMethod = method,
                            cashierName = currentUser?.name ?: "الكاشير",
                            notes = notes,
                            whatsAppMessage = receiptMsg
                        )
                        customerToPay = null
                    }
                }
            )
        }

        // Active Debt Receipt Dialog
        activeReceiptData?.let { data ->
            DebtReceiptDialog(
                workshop = workshop,
                customerName = data.customerName,
                customerPhone = data.customerPhone,
                amountPaid = data.amountPaid,
                remainingBalance = data.remainingBalance,
                paymentMethod = data.paymentMethod,
                cashierName = data.cashierName,
                notes = data.notes,
                whatsAppMessage = data.whatsAppMessage,
                onDismiss = { activeReceiptData = null }
            )
        }
    }
}

private data class DebtReceiptState(
    val customerName: String,
    val customerPhone: String,
    val amountPaid: Double,
    val remainingBalance: Double,
    val paymentMethod: PaymentMethod,
    val cashierName: String,
    val notes: String,
    val whatsAppMessage: String
)

@Composable
private fun CustomerCard(
    customer: CustomerLedgerEntry,
    invoices: List<InvoiceWithItems>,
    isStaff: Boolean,
    onPayDebt: () -> Unit,
    onSelectForPos: () -> Unit,
    onShareStatement: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Name, Phone & Debt Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (customer.remainingDebt > 0) LoopDangerRed.copy(alpha = 0.15f)
                                else LoopTealPrimary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (customer.remainingDebt > 0) LoopDangerRed else LoopTealPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = customer.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (customer.customerPhone.isNotBlank()) {
                            Text(
                                text = customer.customerPhone,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Remaining Debt Status Badge
                if (customer.remainingDebt > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LoopDangerRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "دين: ${String.format(Locale.US, "%,.0f", customer.remainingDebt)} د.ع",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = LoopDangerRed
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LoopSuccessGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "خالص الذمة ✅",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LoopSuccessGreen
                        )
                    }
                }
            }

            // Vehicles & Plates List
            if (customer.vehiclePlates.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = LoopTealPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "المركبات:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    customer.vehiclePlates.forEach { plate ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = plate,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Last Service Details
            if (customer.lastServiceDate.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "آخر صيانة: ${customer.lastServiceDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (customer.lastServiceMileage > 0) {
                        Text(
                            text = "العداد: ${customer.lastServiceMileage} كم",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (customer.remainingDebt > 0) {
                    Button(
                        onClick = onPayDebt,
                        colors = ButtonDefaults.buttonColors(containerColor = LoopSuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("pay_debt_button_${customer.customerName}")
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تسديد دفعة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onSelectForPos,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp), tint = LoopTealPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فاتورة جديدة", style = MaterialTheme.typography.labelMedium, color = LoopTealPrimary)
                }

                IconButton(
                    onClick = onShareStatement,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LoopTealPrimary.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Share, contentDescription = "كشف حساب واتساب", tint = LoopTealPrimary, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "عرض الفواتير السابقة",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable History of Invoices & Repairs
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "سجل الفواتير والصيانة السابقة (${invoices.size}):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (invoices.isEmpty()) {
                        Text(
                            text = "لا توجد فواتير سابقة مسجلة لهذا العميل.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        invoices.forEach { invWithItems ->
                            val inv = invWithItems.invoice
                            val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(inv.timestamp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#${inv.invoiceNumber} | $dateStr",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%,.0f", inv.total)} د.ع (${inv.paymentMethod.labelAr})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (inv.paymentMethod == PaymentMethod.DEBT) LoopDangerRed else LoopTealPrimary
                                        )
                                    }
                                    if (invWithItems.items.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = invWithItems.items.joinToString("، ") { it.itemName },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PartialRepaymentDialog(
    customer: CustomerLedgerEntry,
    workshop: WorkshopEntity?,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: PaymentMethod, notes: String) -> Unit
) {
    val currency = workshop?.currency ?: "د.ع"
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var notesText by remember { mutableStateOf("") }

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val remainingAfter = maxOf(0.0, customer.remainingDebt - amountDouble)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسديد دفعة / سند قبض",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Customer Info Banner
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "الزبون: ${customer.customerName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (customer.customerPhone.isNotBlank()) {
                            Text(
                                text = "الهاتف: ${customer.customerPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "إجمالي الدين الحالي: ${String.format(Locale.US, "%,.0f", customer.remainingDebt)} $currency",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = LoopDangerRed
                        )
                    }
                }

                // Quick Amount Presets
                Text(
                    text = "مبالغ سريعة:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(25000.0, 50000.0, 100000.0).forEach { preset ->
                        if (preset <= customer.remainingDebt) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LoopTealPrimary.copy(alpha = 0.12f))
                                    .clickable { amountText = String.format(Locale.US, "%.0f", preset) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%,.0f", preset)} د.ع",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LoopTealPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    // Full settlement button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LoopSuccessGreen.copy(alpha = 0.15f))
                            .clickable { amountText = String.format(Locale.US, "%.0f", customer.remainingDebt) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "تسديد كامل الدين",
                            style = MaterialTheme.typography.labelSmall,
                            color = LoopSuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Amount to pay input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ المدفوع ($currency)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debt_repayment_amount_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp)
                )

                // Real-time remaining balance calculation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (remainingAfter > 0) LoopDangerRed.copy(alpha = 0.1f)
                            else LoopSuccessGreen.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الرصيد المتبقي بعد التسديد:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", remainingAfter)} $currency",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = if (remainingAfter > 0) LoopDangerRed else LoopSuccessGreen
                    )
                }

                // Payment Method Selector
                Text("طريقة استلام المبلغ:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(PaymentMethod.CASH, PaymentMethod.ZAIN_CASH, PaymentMethod.CARD).forEach { method ->
                        val isSel = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) LoopTealPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedMethod = method }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = method.labelAr,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Notes input
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("ملاحظات (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountDouble > 0) {
                        onConfirm(amountDouble, selectedMethod, notesText.trim())
                    }
                },
                enabled = amountDouble > 0,
                colors = ButtonDefaults.buttonColors(containerColor = LoopSuccessGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_debt_repayment_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تأكيد التسديد وطباعة الوصل")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("إلغاء")
            }
        }
    )
}

private fun buildCustomerStatementMessage(customer: CustomerLedgerEntry, workshop: WorkshopEntity?): String {
    val wsName = workshop?.name ?: "ورشة لوب لخدمات السيارات"
    val currency = workshop?.currency ?: "د.ع"
    val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())

    return """
        📋 *كشف حساب العميل - $wsName*
        ------------------------------------
        التاريخ: $dateStr
        👤 اسم الزبون: *${customer.customerName}*
        📱 الهاتف: ${customer.customerPhone.ifBlank { "-" }}
        🚗 المركبات: ${customer.vehiclePlates.joinToString(" ، ").ifBlank { "-" }}
        ------------------------------------
        إجمالي الفواتير الآجلة: ${String.format(Locale.US, "%,.0f", customer.totalDebtInvoiced)} $currency
        إجمالي المدفوع والمسدد: ${String.format(Locale.US, "%,.0f", customer.totalPaid)} $currency
        ------------------------------------
        ⚖️ *المبلغ المتبقي بذمتكم:* *${String.format(Locale.US, "%,.0f", customer.remainingDebt)} $currency*
        ------------------------------------
        شاكرين حسن تعاملكم والتزامكم معنا!
        📍 ${workshop?.address ?: "بغداد"} | 📞 ${workshop?.phone ?: "+9647701234567"}
    """.trimIndent()
}
