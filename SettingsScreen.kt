package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.local.CatalogItemEntity
import com.example.data.local.DEFAULT_EMOTIONAL_TEMPLATE
import com.example.data.local.InvoiceEntity
import com.example.data.local.UserEntity
import com.example.data.local.WorkshopEntity
import com.example.model.UserRole
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopDangerRed
import com.example.ui.theme.LoopSuccessGreen
import com.example.ui.theme.LoopTealPrimary
import com.example.util.NotificationHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    workshop: WorkshopEntity?,
    currentUser: UserEntity?,
    reminders: List<InvoiceEntity> = emptyList(),
    catalogItems: List<CatalogItemEntity> = emptyList(),
    users: List<UserEntity> = emptyList(),
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onCreateUser: (String, String, String, UserRole) -> Unit = { _, _, _, _ -> },
    onUpdateUserPin: (String, String) -> Unit = { _, _ -> },
    onDeleteUser: (String) -> Unit = {},
    onActivateLicense: (String) -> Unit = {},
    onToggleAllowCashierViewCosts: (Boolean) -> Unit = {},
    onSaveTemplate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isMasterDev = currentUser?.email.equals("mustafa000j@gmail.com", ignoreCase = true) || currentUser?.role == UserRole.MASTER_DEVELOPER
    val isOwner = currentUser?.role == UserRole.OWNER || isMasterDev

    // Push Notification permission & toggle states
    var hasPermission by remember { mutableStateOf(NotificationHelper.hasNotificationPermission(context)) }
    var pushNotificationsEnabled by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "تم تفعيل صلاحية الإشعارات بنجاح! 🔔", Toast.LENGTH_SHORT).show()
            NotificationHelper.sendTestNotification(context)
        } else {
            Toast.makeText(context, "لم يتم منح صلاحية الإشعارات. يمكنك تفعيلها من إعدادات النظام.", Toast.LENGTH_LONG).show()
        }
    }

    // Template state
    val savedTemplate = workshop?.whatsappReminderTemplate?.ifBlank { DEFAULT_EMOTIONAL_TEMPLATE }
        ?: DEFAULT_EMOTIONAL_TEMPLATE

    var templateText by remember(savedTemplate) { mutableStateOf(savedTemplate) }

    // Live Sanitization Testing States
    var testCustomerName by remember { mutableStateOf("مصطفى") }
    var testCarModel by remember { mutableStateOf("تويوتا كامري 2023") }
    var testServiceType by remember { mutableStateOf("الصيانة الدورية وتبديل الدهن") }

    val workshopName = workshop?.name?.ifBlank { "ورشة لوب" } ?: "ورشة لوب"

    // Sanitized output derived evaluation
    val testRenderedMessage by remember(templateText, testCustomerName, testCarModel, testServiceType, workshopName) {
        derivedStateOf {
            val formattedCustomer = testCustomerName.trim().ifBlank { "عزيزنا الزبون" }
            val isCarBlank = testCarModel.trim().isBlank()
            val formattedCar = if (isCarBlank) "سيارتك" else testCarModel.trim()
            val formattedService = testServiceType.trim().ifBlank { "الصيانة الدورية وتبديل الدهن" }

            var replaced = templateText
                .replace("{customer_name}", formattedCustomer)
                .replace("{car_model}", formattedCar)
                .replace("{service_type}", formattedService)
                .replace("{workshop_name}", workshopName)

            // Dynamic sanitization to avoid double "سيارتك سيارتك"
            while (replaced.contains("سيارتك سيارتك")) {
                replaced = replaced.replace("سيارتك سيارتك", "سيارتك")
            }
            replaced.replace("  ", " ").trim()
        }
    }

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
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "إعدادات النظام والورشة ⚙️",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "إدارة بيانات الورشة وقالب رسائل الواتساب الافتراضي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOwner) LoopAmberSecondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOwner) Icons.Default.Shield else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isOwner) LoopAmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOwner) "صلاحية المالك (Owner) 👑" else "مستخدم عادي (قراءة فقط) 🔒",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOwner) LoopAmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 1. Dark Mode & Visual Appearance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "المظهر والوضع الليلي (Dark Mode) 🌙",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تغير المظهر بين الفاتح والداكن لراحة العين أثناء العمل بورشة الصيانة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode(it) },
                        modifier = Modifier.testTag("settings_dark_mode_switch")
                    )
                }
            }

            // 1.1 RBAC Permission Switch Card (Owner Feature)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "السماح للكاشير برؤية تكاليف الشراء والأرباح 🔐",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (workshop?.allowCashierViewCosts == true)
                                "مفعل: الكاشير والفنيون يمكنهم رؤية تكاليف الشراء وهامش الربح."
                            else
                                "معطل (الافتراضي): أسعار الشراء والربح الصافي محجوبة تماماً عن الكاشير والموظفين.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = workshop?.allowCashierViewCosts ?: false,
                        onCheckedChange = { onToggleAllowCashierViewCosts(it) },
                        enabled = isOwner,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LoopTealPrimary,
                            checkedTrackColor = LoopTealPrimary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("allow_cashier_view_costs_switch")
                    )
                }
            }

            // 2. Subscription & License Activation Card
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "حالة الاشتراك والترخيص 🔑",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "معرف الورشة والجهاز: ${workshop?.id ?: "LOOP-WS-7789"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (workshop?.isActivated == true) LoopSuccessGreen.copy(alpha = 0.15f) else LoopAmberSecondary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (workshop?.isActivated == true) "مفعّل بالكامل 🚀" else "وضع المعاينة (Demo Mode)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (workshop?.isActivated == true) LoopSuccessGreen else LoopAmberSecondary
                            )
                        }
                    }

                    if (workshop?.isActivated != true) {
                        var licenseInput by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = licenseInput,
                                onValueChange = { licenseInput = it },
                                label = { Text("مفتاح الترخيص (License Key)") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("license_key_settings_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    if (licenseInput.trim().isNotBlank()) {
                                        onActivateLicense(licenseInput.trim())
                                        licenseInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("activate_license_settings_button")
                            ) {
                                Text("تفعيل 🚀", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = "مفتاح الترخيص الحالي: ${workshop?.licenseKey?.ifBlank { "LOOP-ACTIVATED-PRO" } ?: "LOOP-ACTIVATED-PRO"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LoopTealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. Staff Accounts Management Card (Owner Only)
            if (isOwner) {
                var isAddUserDialogOpen by remember { mutableStateOf(false) }
                var newStaffName by remember { mutableStateOf("") }
                var newStaffEmail by remember { mutableStateOf("") }
                var newStaffPin by remember { mutableStateOf("") }
                var newStaffRole by remember { mutableStateOf(UserRole.STAFF) }

                var editingPinUserId by remember { mutableStateOf<String?>(null) }
                var editPinInput by remember { mutableStateOf("") }

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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "إدارة حسابات الكادر والرمز السري (Staff Accounts) 👥",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "إضافة وتعديل صلاحيات العمال، الكاشير الرئيسي والرمز السري (PIN)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { isAddUserDialogOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("add_staff_account_button")
                            ) {
                                Text("+ إضافة كادر", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Users List
                        users.forEach { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(10.dp)
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
                                            text = "${user.name} (${if (user.role == UserRole.OWNER) "المالك 👑" else if (user.role == UserRole.HEAD_CASHIER) "الكاشير الرئيسي 💼" else "عامل صيانة 🛠️"})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "الرمز السري PIN: ${user.pinCode} | الايميل: ${user.email}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = {
                                                editingPinUserId = user.id
                                                editPinInput = user.pinCode
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("تعديل PIN", fontSize = 10.sp)
                                        }

                                        if (user.role != UserRole.OWNER) {
                                            IconButton(onClick = { onDeleteUser(user.id) }) {
                                                Icon(imageVector = Icons.Default.Clear, contentDescription = "حذف", tint = LoopDangerRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add User Dialog
                if (isAddUserDialogOpen) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { isAddUserDialogOpen = false },
                        title = { Text("إضافة حساب كادر جديد", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = newStaffName,
                                    onValueChange = { newStaffName = it },
                                    label = { Text("اسم الموظف/العامل") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("new_staff_name_input")
                                )
                                OutlinedTextField(
                                    value = newStaffEmail,
                                    onValueChange = { newStaffEmail = it },
                                    label = { Text("الاسم المستعار/الايميل") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("new_staff_email_input")
                                )
                                OutlinedTextField(
                                    value = newStaffPin,
                                    onValueChange = { newStaffPin = it },
                                    label = { Text("الرمز السري (PIN / Password)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("new_staff_pin_input")
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = newStaffRole == UserRole.STAFF,
                                        onClick = { newStaffRole = UserRole.STAFF },
                                        label = { Text("عامل / كادر صيانة") }
                                    )
                                    FilterChip(
                                        selected = newStaffRole == UserRole.HEAD_CASHIER,
                                        onClick = { newStaffRole = UserRole.HEAD_CASHIER },
                                        label = { Text("الكاشير الرئيسي") }
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newStaffName.isNotBlank() && newStaffPin.isNotBlank()) {
                                        onCreateUser(newStaffName.trim(), newStaffEmail.trim(), newStaffPin.trim(), newStaffRole)
                                        isAddUserDialogOpen = false
                                        newStaffName = ""
                                        newStaffEmail = ""
                                        newStaffPin = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary)
                            ) {
                                Text("إنشاء الحساب")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { isAddUserDialogOpen = false }) {
                                Text("إلغاء")
                            }
                        }
                    )
                }

                // Edit PIN Dialog
                if (editingPinUserId != null) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { editingPinUserId = null },
                        title = { Text("تحديث الرمز السري PIN", fontWeight = FontWeight.Bold) },
                        text = {
                            OutlinedTextField(
                                value = editPinInput,
                                onValueChange = { editPinInput = it },
                                label = { Text("الرمز السري الجديد (PIN)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("edit_staff_pin_input")
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (editPinInput.isNotBlank()) {
                                        onUpdateUserPin(editingPinUserId!!, editPinInput.trim())
                                        editingPinUserId = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary)
                            ) {
                                Text("حفظ الرمز السري")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { editingPinUserId = null }) {
                                Text("إلغاء")
                            }
                        }
                    )
                }
            }

            // Owner Restriction Warning Banner for Non-Owners
            if (!isOwner) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LoopDangerRed.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = LoopDangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تنبيه الأمان والصلاحيات 🔒",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = LoopDangerRed
                            )
                            Text(
                                text = "تعديل قالب رسائل الواتساب متاح حصراً لحساب مالك الورشة (Owner). يمكنك معاينة النص فقط.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Push Notifications System Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("push_notifications_settings_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(LoopTealPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = LoopTealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تنبيهات النظام والإشعارات الفورية 🔔",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "إشعار تلقائي بمواعيد صيانة الزبائن وتنبيهات نقص المخزون",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = pushNotificationsEnabled && hasPermission,
                            onCheckedChange = { isChecked ->
                                if (isChecked && !hasPermission) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        hasPermission = true
                                        pushNotificationsEnabled = true
                                    }
                                } else {
                                    pushNotificationsEnabled = isChecked
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = LoopTealPrimary
                            ),
                            modifier = Modifier.testTag("toggle_push_notifications_switch")
                        )
                    }

                    // Permission Status Badge
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasPermission) LoopSuccessGreen.copy(alpha = 0.12f) else LoopAmberSecondary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (hasPermission) Icons.Default.Check else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (hasPermission) LoopSuccessGreen else LoopAmberSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (hasPermission) "صلاحية الإشعارات مفعّلة في الجهاز 🔔" else "صلاحية إشعارات الجهاز غير الممنوحة ⚠️",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (hasPermission) LoopSuccessGreen else LoopAmberSecondary
                                    )
                                    Text(
                                        text = if (hasPermission) "يتلقى الجهاز التنبيهات الفورية تلقائياً" else "انقر لمنح صلاحية إرسال التنبيهات على جهازك",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LoopAmberSecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("request_notification_permission_btn")
                                ) {
                                    Text("تفعيل الصلاحية", fontSize = 11.sp, color = Color.Black)
                                }
                            }
                        }
                    }

                    // Push Notification Testing Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    NotificationHelper.sendTestNotification(context)
                                    Toast.makeText(context, "تم إرسال إشعار تجريبي 🔔", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_push_notification_btn")
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إشعار تجريبي 🔔", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    NotificationHelper.scanAndTriggerNotifications(context, reminders, catalogItems)
                                    Toast.makeText(context, "تم فحص وإرسال تنبيهات الصيانة والمخزون!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("scan_push_notifications_btn")
                        ) {
                            Text("فحص وتنبيه صيانة/مخزون 🚀", fontSize = 11.sp)
                        }
                    }
                }
            }

            // WhatsApp Default Template Manager Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("whatsapp_template_settings_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "قالب تذكير الواتساب الافتراضي (WhatsApp Template)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "الرسالة التلقائية للخدمة العاطفية والتنبيهات المباشرة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isOwner) {
                            OutlinedButton(
                                onClick = {
                                    templateText = DEFAULT_EMOTIONAL_TEMPLATE
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("reset_default_template_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استعادة الافتراضي", fontSize = 11.sp)
                            }
                        }
                    }

                    // Available Dynamic Variables Chips
                    Text(
                        text = "المتغيرات الديناميكية المتاحة (Dynamic Variables):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = LoopTealPrimary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("{customer_name}", "{car_model}", "{service_type}", "{workshop_name}").forEach { tag ->
                            FilterChip(
                                selected = false,
                                enabled = isOwner,
                                onClick = { if (isOwner) templateText += " $tag" },
                                label = { Text(tag, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    // Template Editor Text Field
                    OutlinedTextField(
                        value = templateText,
                        onValueChange = { if (isOwner) templateText = it },
                        readOnly = !isOwner,
                        enabled = isOwner,
                        label = { Text(if (isOwner) "محرر قالب الرسالة العاطفية (Owner Edit)" else "معاينة القالب الحالي (للقراءة فقط)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("settings_template_text_field"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 7
                    )

                    // Action Save Button for Owner
                    if (isOwner) {
                        Button(
                            onClick = {
                                onSaveTemplate(templateText)
                                Toast.makeText(context, "تم حفظ قالب الواتساب الافتراضي بنجاح 💾", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LoopTealPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_whatsapp_template_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حفظ قالب الواتساب الافتراضي (Save Template)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Live Sanitization Tester Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_sanitization_tester_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LoopAmberSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مختبر المعاينة والتعقيم الديناميكي (Dynamic Output Tester)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "جرب إدخال أو إفراغ موديل السيارة لتتأكد من استبدال {car_model} بـ 'سيارتك' وتفادي تكرار 'سيارتك سيارتك':",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Customer Name Test Input
                        OutlinedTextField(
                            value = testCustomerName,
                            onValueChange = { testCustomerName = it },
                            label = { Text("اسم الزبون") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_customer_name_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        // Car Model Test Input
                        OutlinedTextField(
                            value = testCarModel,
                            onValueChange = { testCarModel = it },
                            label = { Text("موديل السيارة {car_model}") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("test_car_model_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    // Quick Toggle buttons for Testing Blank Car Model
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { testCarModel = "تويوتا كامري 2023" },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تويوتا كامري 🚗", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { testCarModel = "" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("clear_car_model_test_btn")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إفراغ الموديل (Blank Test)", fontSize = 11.sp, color = LoopDangerRed)
                        }
                    }

                    // Rendered Result Display Box
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
                                        text = "النتيجة المعقمة النهائية (Sanitized Output):",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF25D366)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(testRenderedMessage))
                                        Toast.makeText(context, "تم نسخ النتيجة المعاينة", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = testRenderedMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.testTag("sanitized_output_preview_text")
                            )
                        }
                    }
                }
            }

            // Workshop Entity Profile Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = LoopTealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بيانات ملف الورشة الرسمي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("اسم الورشة:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(workshopName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("السجل التجاري:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(workshop?.commercialReg ?: "1098425", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("رقم الهاتف:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(workshop?.phone ?: "+9647701234567", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("العنوان:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(workshop?.address ?: "بغداد - الكرادة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
