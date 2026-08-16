package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.data.local.WorkshopEntity
import com.example.data.repository.FirebaseAuthRestManager
import com.example.ui.theme.LoopTealDark
import com.example.ui.theme.LoopTealPrimary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    workshop: WorkshopEntity?,
    users: List<UserEntity>,
    currentDeviceId: String,
    onLoginUser: (UserEntity) -> Unit,
    onLoginWithPin: (String) -> Unit = {},
    onLoginWithGoogle: (String) -> Unit,
    onRegisterNewWorkshop: (String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Login, 1: Register New Workshop
    var isLoading by remember { mutableStateOf(false) }

    // Sign In State - Standard Firebase credentials
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Register State
    var regWorkshopName by remember { mutableStateOf("") }
    var regOwnerName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var regPhone by remember { mutableStateOf("") }
    var regCity by remember { mutableStateOf("بغداد") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(LoopTealPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "∞",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "نظام Loop الذكي لإدارة الورش",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "مصادقة موحدة مع Firebase Auth وتكامل سحابي آمن",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Auth Mode Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("تسجيل الدخول", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("إنشاء حساب ورشة جديد", fontWeight = FontWeight.Bold) }
                    )
                }

                if (selectedTabIndex == 0) {
                    // CLEAN STANDARD LOGIN TAB
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "تسجيل الدخول إلى حسابك",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("البريد الإلكتروني (Email)") },
                                placeholder = { Text("example@domain.com") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LoopTealPrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_email_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("كلمة المرور (Password)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LoopTealPrimary) },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (isPasswordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    val email = emailInput.trim()
                                    val password = passwordInput.trim()
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        isLoading = true
                                        scope.launch {
                                            val res = FirebaseAuthRestManager.signInWithEmail(email, password)
                                            isLoading = false
                                            res.onSuccess { firebaseUser ->
                                                Toast.makeText(context, "تم تسجيل الدخول بنجاح! 🚀", Toast.LENGTH_SHORT).show()
                                                onLoginWithGoogle(firebaseUser.email)
                                            }.onFailure { err ->
                                                Toast.makeText(context, err.message ?: "خطأ في تسجيل الدخول، تأكد من صحة البريد وكلمة المرور", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "يرجى إدخال البريد الإلكتروني وكلمة المرور", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isLoading && emailInput.isNotBlank() && passwordInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("firebase_login_submit_btn")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                                } else {
                                    Text("تسجيل الدخول 🔑", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                } else {
                    // REGISTER NEW WORKSHOP TAB
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "تسجيل ورشة جديدة في السحابة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = regWorkshopName,
                                onValueChange = { regWorkshopName = it },
                                label = { Text("اسم الورشة / المركز الفني") },
                                leadingIcon = { Icon(Icons.Default.AddBusiness, contentDescription = null, tint = LoopTealPrimary) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_ws_name_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = regOwnerName,
                                onValueChange = { regOwnerName = it },
                                label = { Text("اسم مالك الورشة / المدير") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LoopTealPrimary) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_owner_name_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("البريد الإلكتروني") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LoopTealPrimary) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("كلمة المرور (6 خانات على الأقل)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LoopTealPrimary) },
                                trailingIcon = {
                                    IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isRegPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (isRegPasswordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it },
                                label = { Text("رقم هاتف الورشة") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = LoopTealPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth().testTag("reg_phone_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    val email = regEmail.trim()
                                    val pass = regPassword.trim()
                                    val wsName = regWorkshopName.trim()
                                    val ownerName = regOwnerName.trim()

                                    if (email.isNotBlank() && pass.length >= 6 && wsName.isNotBlank() && ownerName.isNotBlank()) {
                                        isLoading = true
                                        scope.launch {
                                            val res = FirebaseAuthRestManager.signUpWithEmail(
                                                email = email,
                                                password = pass,
                                                ownerName = ownerName,
                                                workshopName = wsName,
                                                phone = regPhone.trim(),
                                                city = regCity.trim()
                                            )
                                            isLoading = false
                                            res.onSuccess { user ->
                                                Toast.makeText(context, "تم إنشاء حساب الورشة بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                                                onRegisterNewWorkshop(wsName, ownerName, email, pass, regPhone.trim(), regCity.trim())
                                                onLoginWithGoogle(user.email)
                                            }.onFailure { err ->
                                                Toast.makeText(context, err.message ?: "فشل تسجيل الورشة", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "يرجى تعبئة جميع الحقول المطلوبة وكلمة المرور 6 خانات على الأقل", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isLoading && regEmail.isNotBlank() && regPassword.length >= 6 && regWorkshopName.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("reg_submit_btn")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                                } else {
                                    Text("إنشاء حساب الورشة والبدء 🎉", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }

                // Security & Device Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = LoopTealDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "جهاز الجلسة الحالي: $currentDeviceId",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
