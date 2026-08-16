package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.data.local.WorkshopEntity
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopTealPrimary

@Composable
fun SubscriptionRequiredDialog(
    workshop: WorkshopEntity?,
    currentUser: UserEntity?,
    onActivateLicense: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputLicenseKey by remember { mutableStateOf("") }
    var ownerNameInput by remember { mutableStateOf(currentUser?.name ?: "") }
    var ownerPhoneInput by remember { mutableStateOf(workshop?.phone ?: "") }
    var cityInput by remember { mutableStateOf(workshop?.address ?: "بغداد") }

    val workshopId = workshop?.id ?: "LOOP-WS-7789"
    val workshopName = workshop?.name ?: "ورشة لوب"

    fun openWhatsAppActivation() {
        val message = """مرحباً فريق LOOP Auto Care 👋
أود تفعيل الاشتراك النهائي للورشة:
• معرف الورشة (User ID): $workshopId
• اسم الورشة: $workshopName
• اسم المالك: ${ownerNameInput.ifBlank { currentUser?.name ?: "غير محدد" }}
• هاتف التواصل: ${ownerPhoneInput.ifBlank { workshop?.phone ?: "غير محدد" }}
• المدينة: $cityInput

يرجى تزويدي بمفتاح التفعيل الخاص بنا. شكراً لكم! ❤️"""

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        try {
            val chooser = Intent.createChooser(sendIntent, "طلب تفعيل الاشتراك عبر واتساب")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح الواتساب", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = LoopAmberSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "تفعيل الاشتراك مطلوب (Demo Mode)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LoopAmberSecondary.copy(alpha = 0.12f))
                        .border(1.dp, LoopAmberSecondary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "أنت تستخدم حالياً وضع المعاينة التجريبي. لفتح إصدار الفواتير وحفظها، وتصدير التقارير، يرجى تفعيل ترخيص التطبيق الخاص بورشتك.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Hardware User ID Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "معرّف الورشة والجهاز (User ID):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = workshopId,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = LoopTealPrimary
                            )
                        }
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(workshopId))
                                Toast.makeText(context, "تم نسخ معرف الورشة بنجاح! 📋", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نسخ المعرف", fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider()

                // License Key Input
                Text(
                    text = "أدخل مفتاح التفعيل (License Key):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = inputLicenseKey,
                    onValueChange = { inputLicenseKey = it },
                    label = { Text("مفتاح الترخيص (مثال: LOOP-PRO-2026)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = LoopTealPrimary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("license_key_input_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        if (inputLicenseKey.trim().length >= 4) {
                            onActivateLicense(inputLicenseKey.trim())
                        } else {
                            Toast.makeText(context, "يرجى إدخال مفتاح ترخيص صحيح (4 خانات على الأقل)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activate_license_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhonelinkSetup, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تفعيل الترخيص الآن 🚀", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))

                // Direct WhatsApp Activation Button
                Text(
                    text = "ليس لديك مفتاح تفعيل؟ تواصل معنا مباشرة:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { openWhatsAppActivation() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("whatsapp_direct_activation_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تفعيل مباشر عبر الواتساب 📲", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("إلغاء ومتابعة المعاينة")
            }
        }
    )
}
