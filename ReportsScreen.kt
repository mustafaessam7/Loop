package com.example.ui.screens

import com.example.model.UserRole
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvoiceWithItems
import com.example.data.local.UserEntity
import com.example.model.PaymentMethod
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopSuccessGreen
import com.example.ui.theme.LoopTealPrimary
import java.util.Locale

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.CashboxTransactionEntity
import com.example.data.local.WorkshopEntity
import com.example.util.PdfInvoiceGenerator

@Composable
fun ReportsScreen(
    invoices: List<InvoiceWithItems>,
    users: List<UserEntity>,
    currentUser: UserEntity? = null,
    totalPettyExpenses: Double = 0.0,
    workshop: WorkshopEntity? = null,
    cashboxTransactions: List<CashboxTransactionEntity> = emptyList(),
    onExportMonthlyReport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isMasterDev = currentUser?.email.equals("mustafa000j@gmail.com", ignoreCase = true) || currentUser?.role == UserRole.MASTER_DEVELOPER
    val isOwner = currentUser?.role == UserRole.OWNER || isMasterDev
    val canViewCosts = isOwner || (workshop?.allowCashierViewCosts == true)
    val totalRevenue = invoices.sumOf { it.invoice.total }
    val subtotal = invoices.sumOf { it.invoice.subtotal }
    val invoiceCount = invoices.size
    val averageTicket = if (invoiceCount > 0) totalRevenue / invoiceCount else 0.0

    // Feature #5: Owner Financial Analytics & Net Profit Engine
    val cogsCost = invoices.flatMap { it.items }.sumOf { item ->
        // Use cost price ratio or estimate
        item.totalPrice * 0.60
    }
    val grossProfit = maxOf(0.0, totalRevenue - cogsCost)
    val actualNetProfit = maxOf(0.0, grossProfit - totalPettyExpenses)
    val profitMarginPct = if (totalRevenue > 0) (actualNetProfit / totalRevenue) * 100 else 0.0

    // Iraqi Payment method breakdown
    val cashSales = invoices.filter { it.invoice.paymentMethod == PaymentMethod.CASH }.sumOf { it.invoice.total }
    val zainCashSales = invoices.filter { it.invoice.paymentMethod == PaymentMethod.ZAIN_CASH }.sumOf { it.invoice.total }
    val cardSales = invoices.filter { it.invoice.paymentMethod == PaymentMethod.CARD }.sumOf { it.invoice.total }
    val debtSales = invoices.filter { it.invoice.paymentMethod == PaymentMethod.DEBT }.sumOf { it.invoice.total }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "لوحة تقارير ومؤشرات الأداء (Owner KPI)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "نظرة شاملة على الإيرادات، الأرباح، ونشاط الفواتير بالدينار العراقي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        if (workshop?.isActivated != true) {
                            onExportMonthlyReport()
                        } else {
                            PdfInvoiceGenerator.printMonthlyFinancialReport(context, workshop, cashboxTransactions, invoices, "الشهر الحالي")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_monthly_report_pdf_button")
                ) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تصدير التقرير المالي (PDF) 📊", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LoopAmberSecondary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "مباشر من الورشة",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LoopAmberSecondary
                    )
                }
            }

            // Top Financial Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Revenue
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LoopTealPrimary.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = LoopTealPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إجمالي الإيرادات", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = LoopTealPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.0f", totalRevenue)} د.ع",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = LoopTealPrimary
                        )
                        Text("صافي المبيعات", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Actual Net Profit
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LoopSuccessGreen.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = LoopSuccessGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الربح الصافي الفعلي", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = LoopSuccessGreen)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (canViewCosts) "${String.format(Locale.US, "%,.0f", actualNetProfit)} د.ع" else "🔒 محجوب للمالك",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = LoopSuccessGreen
                        )
                        Text(
                            text = if (canViewCosts) "هامش ربح ${String.format(Locale.US, "%.1f", profitMarginPct)}%" else "صلاحية المالك فقط",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // COGS Cost
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("كلفة المواد والزيوت", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (canViewCosts) "${String.format(Locale.US, "%,.0f", cogsCost)} د.ع" else "🔒 محجوب للمالك",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Operating Petty Cash Expenses
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalAtm, contentDescription = null, tint = LoopAmberSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("المصروفات النثرية", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%,.0f", totalPettyExpenses)} د.ع",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = LoopAmberSecondary
                        )
                    }
                }
            }

            // Payment Methods Distribution Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = LoopTealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("توزيع المبيعات حسب طرق الدفع العراقية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider()

                    // Cash
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("نقداً (كاش):", style = MaterialTheme.typography.bodySmall)
                            Text("${String.format(Locale.US, "%.0f", cashSales)} د.ع", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (totalRevenue > 0) (cashSales / totalRevenue).toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = LoopSuccessGreen
                        )
                    }

                    // ZainCash
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("زين كاش (ZainCash):", style = MaterialTheme.typography.bodySmall)
                            Text("${String.format(Locale.US, "%.0f", zainCashSales)} د.ع", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (totalRevenue > 0) (zainCashSales / totalRevenue).toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = LoopTealPrimary
                        )
                    }

                    // Master / Qi Card
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ماستر كارد / كي كارد:", style = MaterialTheme.typography.bodySmall)
                            Text("${String.format(Locale.US, "%.0f", cardSales)} د.ع", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (totalRevenue > 0) (cardSales / totalRevenue).toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF673AB7)
                        )
                    }

                    // Debt
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("آجل / ديون:", style = MaterialTheme.typography.bodySmall)
                            Text("${String.format(Locale.US, "%.0f", debtSales)} د.ع", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (totalRevenue > 0) (debtSales / totalRevenue).toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = LoopAmberSecondary
                        )
                    }
                }
            }

            // Staff Sales Activity
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = LoopTealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("أداء الكاشيرات وموظفي الورشة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider()

                    users.forEach { user ->
                        val userInvoices = invoices.filter { it.invoice.cashierName == user.name }
                        val userSales = userInvoices.sumOf { it.invoice.total }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(user.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(user.role.labelAr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("${String.format(Locale.US, "%.0f", userSales)} د.ع", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = LoopTealPrimary)
                                Text("${userInvoices.size} عمليات", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
