package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DEFAULT_EMOTIONAL_TEMPLATE
import com.example.data.local.InvoiceEntity
import com.example.ui.theme.LoopTealPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmotionalReminderDialog(
    initialInvoice: InvoiceEntity? = null,
    initialWorkshopName: String = "ورشة لوب",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Dynamic Variables State
    var customerName by remember { mutableStateOf(initialInvoice?.customerName ?: "") }
    var carModel by remember { mutableStateOf(initialInvoice?.vehicleModel ?: "") }
    var serviceType by remember {
        mutableStateOf(
            if (initialInvoice?.notes?.isNotBlank() == true) initialInvoice.notes
            else "الصيانة الدورية وتبديل الدهن"
        )
    }
    var workshopName by remember { mutableStateOf(initialWorkshopName) }
    var customerPhone by remember { mutableStateOf(initialInvoice?.customerPhone ?: "") }

    // Preset Emotional Templates
    val presets = listOf(
        "ودّي ودافئ (الافتراضي) ❤️" to DEFAULT_EMOTIONAL_TEMPLATE,

        "سلامة وأمان 🚗✨" to """سلامكم وراحة بالكم هي أهم ما لدينا عزيزنا {customer_name} 🌟

حرصاً منا على أداء أسطوري لـ {car_model}، نذكركم بموعد {service_type} في {workshop_name}.

فريقنا الفني جاهز لخدمتكم بأعلى معايير الجودة والدقة! 🛠️❤️""",

        "تقدير الزبائن VIP 🏅" to """عزيزنا {customer_name}، شريك نجاحنا الدائم 🏅

نعتز بتعاملكم مع {workshop_name} ونحب أن نذكركم بالموعد القادم لـ {service_type} لسيارتكم {car_model}.

بانتظار تشريفكم لنمنح سيارتكم الاهتمام الفائق الذي تستحقه! 🚗❤️"""
    )

    var selectedPresetIndex by remember { mutableStateOf(0) }
    var templateText by remember { mutableStateOf(presets[0].second) }

    // Rendered Output evaluation using exact dynamic replacement rule
    val renderedMessage by remember {
        derivedStateOf {
            val formattedCustomer = customerName.trim().ifBlank { "عزيزنا الزبون" }
            val isCarBlank = carModel.trim().isBlank()
            val formattedCar = if (isCarBlank) "سيارتك" else carModel.trim()
            val formattedService = serviceType.trim().ifBlank { "الصيانة الدورية وتبديل الدهن" }
            val formattedWorkshop = workshopName.trim().ifBlank { "ورشة لوب" }

            var replaced = templateText
                .replace("{customer_name}", formattedCustomer)
                .replace("{car_model}", formattedCar)
                .replace("{service_type}", formattedService)
                .replace("{workshop_name}", formattedWorkshop)

            // Dynamic sanitization to avoid duplicate "سيارتك سيارتك"
            while (replaced.contains("سيارتك سيارتك")) {
                replaced = replaced.replace("سيارتك سيارتك", "سيارتك")
            }
            replaced = replaced.replace("  ", " ")

            val extraDetails = buildString {
                if (initialInvoice != null && initialInvoice.nextServiceDate.isNotBlank()) {
                    append("\n📅 الموعد المقترح: *${initialInvoice.nextServiceDate}*")
                }
                if (initialInvoice != null && initialInvoice.nextServiceMileage > 0) {
                    append("\n🛣️ العداد المتوقع: *${initialInvoice.nextServiceMileage} كم*")
                }
            }

            (replaced + extraDetails).trim()
        }
    }

    fun sendViaWhatsApp() {
        val cleanPhone = customerPhone.replace(Regex("[^0-9+]"), "")
        val encodedText = Uri.encode(renderedMessage)
        val uri = if (cleanPhone.isNotBlank()) {
            Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText")
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "لم يتم العثور على تطبيق واتساب", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE91E63).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تذكير الصيانة العاطفي (WhatsApp)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "رسائل موجهة بالمتغيرات الديناميكية لبناء ولاء الزبائن",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Presets Selection
                Text(
                    text = "اختر النموذج العاطفي الجاهز:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEachIndexed { index, (title, text) ->
                        FilterChip(
                            selected = (selectedPresetIndex == index),
                            onClick = {
                                selectedPresetIndex = index
                                templateText = text
                            },
                            label = { Text(title, fontSize = 12.sp) },
                            leadingIcon = if (selectedPresetIndex == index) {
                                { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Dynamic Variables Inputs Grid
                Text(
                    text = "المتغيرات الديناميكية (Dynamic Variables):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = LoopTealPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Customer Name
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("اسم الزبون {customer_name}") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_name"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Car Model (with "سيارتك" fallback note)
                    OutlinedTextField(
                        value = carModel,
                        onValueChange = { carModel = it },
                        label = { Text("موديل السيارة {car_model} (أو 'سيارتك' إذا تُرك فارغاً)") },
                        placeholder = { Text("مثال: تويوتا كامري 2023 (يستبدل بـ 'سيارتك' إن كان فارغاً)") },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_car_model"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Service Type
                    OutlinedTextField(
                        value = serviceType,
                        onValueChange = { serviceType = it },
                        label = { Text("نوع الخدمة الصيانة {service_type}") },
                        placeholder = { Text("مثال: تبديل الدهن والفلتر والصدرية") },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_service_type"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Workshop Name
                    OutlinedTextField(
                        value = workshopName,
                        onValueChange = { workshopName = it },
                        label = { Text("اسم الورشة {workshop_name}") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_workshop_name"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Customer Phone
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("رقم هاتف الزبون (لإرسال الواتساب المباشر)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_phone"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                // Variable Tags Helper Chips
                Text(
                    text = "انقر لإضافة وسوم المتغيرات داخل نص القالب:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("{customer_name}", "{car_model}", "{service_type}", "{workshop_name}").forEach { tag ->
                        FilterChip(
                            selected = false,
                            onClick = { templateText += " $tag" },
                            label = { Text(tag, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                // Template Editor Box
                OutlinedTextField(
                    value = templateText,
                    onValueChange = { templateText = it },
                    label = { Text("محرر قالب الرسالة العاطفية") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("template_editor_text"),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 5
                )

                // Live Rendered Message Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF25D366).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "معاينة الرسالة النهائية عبر واتساب:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF25D366)
                                )
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(renderedMessage))
                                    Toast.makeText(context, "تم نسخ الرسالة للحافظة", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "نسخ النص",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = renderedMessage,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.testTag("rendered_emotional_message_preview")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { sendViaWhatsApp() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("send_emotional_whatsapp_btn")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إرسال عبر واتساب 📲", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("إغلاق")
            }
        }
    )
}
