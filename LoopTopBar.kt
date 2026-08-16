package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.data.local.WorkshopEntity
import com.example.model.UserRole
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopDangerRed
import com.example.ui.theme.LoopSuccessGreen
import com.example.ui.theme.LoopTealDark
import com.example.ui.theme.LoopTealPrimary

@Composable
fun LoopTopBar(
    workshop: WorkshopEntity?,
    currentUser: UserEntity?,
    isSessionConflict: Boolean,
    isOnline: Boolean = true,
    unsyncedCount: Int = 0,
    onOpenDrawer: () -> Unit = {},
    onOpenSyncStatus: () -> Unit = {},
    onOpenWhatsAppSupport: () -> Unit,
    onOpenSessionManager: () -> Unit,
    onOpenFirestoreDocs: () -> Unit,
    onOpenAiChat: () -> Unit = {},
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Drawer Menu Button & Brand Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Menu Drawer Trigger Button
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("open_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "القائمة الجانبية (Drawer)",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Loop Logo badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LoopTealPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "∞",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Loop",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = LoopTealDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LoopAmberSecondary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "POS & ERP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LoopAmberSecondary
                            )
                        }
                    }
                    Text(
                        text = workshop?.name ?: "ورشة لوب لخدمات السيارات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Actions & Role indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Offline / Sync Status Badge Button
                IconButton(
                    onClick = onOpenSyncStatus,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (unsyncedCount > 0) LoopAmberSecondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("open_sync_status_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unsyncedCount > 0) {
                                Badge(containerColor = LoopAmberSecondary) {
                                    Text("$unsyncedCount", color = Color.White)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (!isOnline) Icons.Default.CloudOff else if (unsyncedCount > 0) Icons.Default.CloudSync else Icons.Default.CloudDone,
                            contentDescription = "حالة المزامنة والعمل بدون نت",
                            tint = if (!isOnline) LoopAmberSecondary else if (unsyncedCount > 0) LoopAmberSecondary else LoopSuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Firestore Docs & Rules Button
                IconButton(
                    onClick = onOpenFirestoreDocs,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("firestore_docs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "قواعد وبيانات Firestore",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Single Active Session Device Status Button
                IconButton(
                    onClick = onOpenSessionManager,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isSessionConflict) LoopDangerRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("session_manager_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (isSessionConflict) {
                                Badge(containerColor = LoopDangerRed) {
                                    Text("!", color = Color.White)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = "حالة الجلسة والأجهزة",
                            tint = if (isSessionConflict) LoopDangerRed else LoopSuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // AI Chat Assistant Button (Gemini Powered)
                Button(
                    onClick = onOpenAiChat,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LoopTealPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("ai_chat_assistant_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "المساعد الذكي",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "المساعد الذكي 🤖",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Smart WhatsApp Support Button (Requested Feature)
                Button(
                    onClick = onOpenWhatsAppSupport,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("whatsapp_support_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "دعم واتساب الذكي",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "دعم واتساب",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Active User & Role Chip
                if (currentUser != null) {
                    val roleColor = when (currentUser.role) {
                        UserRole.MASTER_DEVELOPER -> Color(0xFF8B5CF6)
                        UserRole.OWNER -> LoopAmberSecondary
                        UserRole.HEAD_CASHIER -> LoopTealPrimary
                        UserRole.STAFF -> Color(0xFF3B82F6)
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = roleColor.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .clickable { onUserClick() }
                            .testTag("user_profile_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "المستخدم",
                                tint = roleColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = currentUser.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (currentUser.role) {
                                        UserRole.MASTER_DEVELOPER -> "مطور النظام 👑"
                                        UserRole.OWNER -> "المالك"
                                        UserRole.HEAD_CASHIER -> "كاشير رئيسي"
                                        UserRole.STAFF -> "فني / مندوب"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = roleColor,
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
