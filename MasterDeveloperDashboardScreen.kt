package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.WorkshopEntity
import com.example.data.repository.ActivationRequest
import com.example.data.repository.FirebaseAuthRestManager
import com.example.data.repository.FirebaseSyncManager
import com.example.data.repository.FirestoreLicenseKey
import com.example.data.repository.FirestoreWorkshop
import com.example.data.repository.SupportContact
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopDangerRed
import com.example.ui.theme.LoopSuccessGreen
import com.example.ui.theme.LoopTealPrimary
import kotlinx.coroutines.launch

@Composable
fun MasterDeveloperDashboardScreen(
    currentWorkshop: WorkshopEntity?,
    firestoreWorkshops: List<FirestoreWorkshop>,
    activationRequests: List<ActivationRequest>,
    generatedLicenses: List<FirestoreLicenseKey>,
    onActivateWorkshopLocal: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var genWorkshopId by remember { mutableStateOf(currentWorkshop?.id ?: "WS-LOOP-7789") }
    var genWorkshopName by remember { mutableStateOf(currentWorkshop?.name ?: "ورشة جديدة") }
    var genOwnerEmail by remember { mutableStateOf("Mustafa000j@gmail.com") }
    var customKeyInput by remember { mutableStateOf("") }
    var newlyCreatedKey by remember { mutableStateOf("") }

    // Dynamic Support Contacts State
    var supportContactsList = remember { mutableStateListOf(*FirebaseAuthRestManager.supportContacts.toTypedArray()) }
    var newContactLabel by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Compact Header Title & Minimal Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "لوحة التحكم للمطور الرئيسي 👑",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المسؤول الأول: Mustafa000j@gmail.com (وصول كامل)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Super Admin Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6)
                        )
                    }
                }
            }

            // Section 1: License Key Generator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = LoopTealPrimary)
                        Text(
                            text = "توليد وتفعيل مفاتيح التراخيص (License Key Generator)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = genWorkshopId,
                        onValueChange = { genWorkshopId = it },
                        label = { Text("معرف الورشة Target Workshop ID") },
                        modifier = Modifier.fillMaxWidth().testTag("master_gen_ws_id"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = genWorkshopName,
                        onValueChange = { genWorkshopName = it },
                        label = { Text("اسم الورشة Workshop Name") },
                        modifier = Modifier.fillMaxWidth().testTag("master_gen_ws_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = genOwnerEmail,
                        onValueChange = { genOwnerEmail = it },
                        label = { Text("ايميل المالك Owner Email") },
                        modifier = Modifier.fillMaxWidth().testTag("master_gen_owner_email"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = customKeyInput,
                        onValueChange = { customKeyInput = it },
                        label = { Text("مفتاح مخصص (اختياري، اتركه فارغاً لتوليد آلي)") },
                        modifier = Modifier.fillMaxWidth().testTag("master_gen_custom_key"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                val createdKey = FirebaseSyncManager.generateLicenseKey(
                                    workshopId = genWorkshopId.trim(),
                                    workshopName = genWorkshopName.trim(),
                                    ownerEmail = genOwnerEmail.trim(),
                                    customKey = customKeyInput.trim()
                                )
                                newlyCreatedKey = createdKey
                                onActivateWorkshopLocal(genWorkshopId.trim(), createdKey)
                                Toast.makeText(context, "تم توليد وتفعيل المفتاح بنجاح! 🚀", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("master_generate_key_button")
                    ) {
                        Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("توليد مفتاح تفعيل وشحن الرخصة 🚀", fontWeight = FontWeight.Bold)
                    }

                    if (newlyCreatedKey.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LoopSuccessGreen.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("المفتاح المولد حديثاً:", fontSize = 11.sp, color = LoopSuccessGreen)
                                    Text(newlyCreatedKey, fontSize = 16.sp, fontWeight = FontWeight.Black, color = LoopSuccessGreen)
                                }
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(newlyCreatedKey))
                                        Toast.makeText(context, "تم نسخ المفتاح 📋", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LoopSuccessGreen)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("نسخ المفتاح", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Dynamic WhatsApp Support Numbers Management (Requirement 4)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContactPhone, contentDescription = null, tint = Color(0xFF25D366))
                        Text(
                            text = "إدارة أرقام واتساب الدعم الفني والتفعيل (Support Numbers)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "يمكنك إضافة أو تعديل أو اختيار رقم الواتساب المفعل الذي يتم توجيه زبائن الورش إليه تلقائياً عند طلب الدعم أو التفعيل.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Current Support Numbers List
                    supportContactsList.forEachIndexed { index, contact ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (contact.isActive) Color(0xFF25D366).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = if (contact.isActive) Color(0xFF25D366) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = contact.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = contact.phone,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            // Toggle active state
                                            val updated = supportContactsList.mapIndexed { idx, item ->
                                                if (idx == index) item.copy(isActive = true)
                                                else item.copy(isActive = false)
                                            }
                                            supportContactsList.clear()
                                            supportContactsList.addAll(updated)
                                            FirebaseAuthRestManager.supportContacts = supportContactsList.toMutableList()
                                            Toast.makeText(context, "تم تعيين الرقم الرئيسي بنجاح! 🟢", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (contact.isActive) Color(0xFF25D366) else MaterialTheme.colorScheme.outline
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (contact.isActive) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(if (contact.isActive) "الرئيسي 🟢" else "تحديد كـ رئيسي", fontSize = 11.sp)
                                    }

                                    IconButton(
                                        onClick = {
                                            if (supportContactsList.size > 1) {
                                                supportContactsList.removeAt(index)
                                                FirebaseAuthRestManager.supportContacts = supportContactsList.toMutableList()
                                                Toast.makeText(context, "تم حذف الرقم", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "يجب الإبقاء على رقم دعم واحد على الأقل", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = LoopDangerRed)
                                    }
                                }
                            }
                        }
                    }

                    // Add New Support Number Form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newContactLabel,
                            onValueChange = { newContactLabel = it },
                            label = { Text("عنوان القسم (مثال: الدعم الفني)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = newContactPhone,
                            onValueChange = { newContactPhone = it },
                            label = { Text("رقم الواتساب (+964...)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (newContactLabel.isNotBlank() && newContactPhone.isNotBlank()) {
                                val newContact = SupportContact(newContactLabel.trim(), newContactPhone.trim(), false)
                                supportContactsList.add(newContact)
                                FirebaseAuthRestManager.supportContacts = supportContactsList.toMutableList()
                                newContactLabel = ""
                                newContactPhone = ""
                                Toast.makeText(context, "تمت إضافة رقم الدعم بنجاح! 📱", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة رقم دعم واتساب جديد 📱", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 3: WhatsApp Activation Requests Queue
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PendingActions, contentDescription = null, tint = LoopAmberSecondary)
                        Text(
                            text = "طلبات التفعيل عبر الواتساب (${activationRequests.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (activationRequests.isEmpty()) {
                        Text(
                            text = "لا توجد طلبات تفعيل معلقة حالياً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        activationRequests.forEach { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${req.workshopName} (${req.city})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "المالك: ${req.ownerName} | هاتف: ${req.ownerPhone} | المعرف: ${req.workshopId}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                FirebaseSyncManager.approveActivationRequest(
                                                    requestId = req.id,
                                                    workshopId = req.workshopId,
                                                    workshopName = req.workshopName,
                                                    ownerEmail = req.ownerPhone
                                                )
                                                onActivateWorkshopLocal(req.workshopId, "LOOP-PRO-8899")
                                                Toast.makeText(context, "تمت الموافقة وتفعيل الورشة 🚀", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = LoopSuccessGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("تفعيل بنقرة 🚀", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Registered Workshops Management
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = LoopTealPrimary)
                        Text(
                            text = "إدارة الورش المسجلة في السحابة (${firestoreWorkshops.size + 1})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Primary Default Workshop
                    val wsLocal = currentWorkshop
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${wsLocal?.name ?: "ورشة جديدة"} (الورشة الحالية)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (wsLocal?.isActivated == true) LoopSuccessGreen.copy(alpha = 0.2f) else LoopAmberSecondary.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (wsLocal?.isActivated == true) "مفعلة 🟢" else "وضع المعاينة 🟡",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (wsLocal?.isActivated == true) LoopSuccessGreen else LoopAmberSecondary
                                    )
                                }
                            }
                            Text("المعرف: ${wsLocal?.id ?: "WS-LOOP-7789"} | الهاتف: ${wsLocal?.phone} | الايميل: ${wsLocal?.email}", fontSize = 11.sp)
                            Text("مفتاح الترخيص: ${wsLocal?.licenseKey?.ifBlank { "LOOP-PRO-MAIN" } ?: "LOOP-PRO-MAIN"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LoopTealPrimary)
                        }
                    }

                    // Firestore Remote Workshops
                    firestoreWorkshops.forEach { remoteWs ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = remoteWs.name.ifBlank { remoteWs.id },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (remoteWs.isActivated) LoopSuccessGreen.copy(alpha = 0.2f) else LoopDangerRed.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = remoteWs.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (remoteWs.isActivated) LoopSuccessGreen else LoopDangerRed
                                        )
                                    }
                                }
                                Text("المالك: ${remoteWs.ownerName} | ${remoteWs.ownerEmail} | ${remoteWs.phone}", fontSize = 11.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("المفتاح: ${remoteWs.licenseKey}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LoopTealPrimary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    FirebaseSyncManager.updateWorkshopStatus(remoteWs.id, "Active", true)
                                                    Toast.makeText(context, "تم تفعيل الورشة 🟢", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = LoopSuccessGreen),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("تفعيل", fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    FirebaseSyncManager.updateWorkshopStatus(remoteWs.id, "Suspended", false)
                                                    Toast.makeText(context, "تم إيقاف الورشة 🔴", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = LoopDangerRed),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("إيقاف", fontSize = 10.sp)
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
}
