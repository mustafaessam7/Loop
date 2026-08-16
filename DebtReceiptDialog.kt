package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WorkshopEntity
import com.example.model.PaymentMethod
import com.example.ui.theme.LoopDangerRed
import com.example.ui.theme.LoopSuccessGreen
import com.example.ui.theme.LoopTealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebtReceiptDialog(
    workshop: WorkshopEntity?,
    customerName: String,
    customerPhone: String,
    amountPaid: Double,
    remainingBalance: Double,
    paymentMethod: PaymentMethod,
    cashierName: String,
    notes: String,
    whatsAppMessage: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currency = workshop?.currency ?: "د.ع"
    val dateStr = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                    Text(
                        text = "وصل قبض وتسديد حساب",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Success checkmark badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(LoopSuccessGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = LoopSuccessGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "تم تسجيل الدفعة بنجاح في الصندوق",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = LoopSuccessGreen
                )

                // Thermal receipt paper card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = workshop?.name ?: "ورشة لوب لخدمات وصيانة السيارات",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "هاتف: ${workshop?.phone ?: "+9647701234567"} | ${workshop?.address ?: "بغداد"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        ReceiptRow(label = "التاريخ:", value = dateStr)
                        ReceiptRow(label = "اسم الزبون:", value = customerName, isBold = true)
                        if (customerPhone.isNotBlank()) {
                            ReceiptRow(label = "رقم الهاتف:", value = customerPhone)
                        }
                        ReceiptRow(label = "طريقة التسديد:", value = paymentMethod.labelAr)
                        ReceiptRow(label = "الكاشير المستلم:", value = cashierName)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Amount Paid Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LoopSuccessGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المبلغ المسدد:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = LoopSuccessGreen
                            )
                            Text(
                                text = "${String.format(Locale.US, "%,.0f", amountPaid)} $currency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = LoopSuccessGreen
                            )
                        }

                        // Remaining Debt Balance Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (remainingBalance > 0) LoopDangerRed.copy(alpha = 0.12f)
                                    else LoopSuccessGreen.copy(alpha = 0.12f)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (remainingBalance > 0) "الدين المتبقي:" else "حالة الحساب:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBalance > 0) LoopDangerRed else LoopSuccessGreen
                            )
                            Text(
                                text = if (remainingBalance > 0)
                                    "${String.format(Locale.US, "%,.0f", remainingBalance)} $currency"
                                else
                                    "خالص الذمة (0 د.ع) ✅",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (remainingBalance > 0) LoopDangerRed else LoopSuccessGreen
                            )
                        }

                        if (notes.isNotBlank()) {
                            ReceiptRow(label = "ملاحظات:", value = notes)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val cleanPhone = customerPhone.replace("+", "").replace(" ", "").trim()
                        val targetUri = if (cleanPhone.isNotBlank()) {
                            Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(whatsAppMessage)}")
                        } else {
                            Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(whatsAppMessage)}")
                        }
                        val intent = Intent(Intent.ACTION_VIEW, targetUri)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        clipboardManager.setText(AnnotatedString(whatsAppMessage))
                        Toast.makeText(context, "تم نسخ وصل القبض إلى الحافظة", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LoopSuccessGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_whatsapp_debt_receipt_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إرسال الوصل للزبون عبر واتساب", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تم وإغلاق")
            }
        }
    )
}

@Composable
private fun ReceiptRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
