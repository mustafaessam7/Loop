package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LoopTealPrimary

@Composable
fun FirestoreDocsDialog(onDismiss: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val schemaDocumentation = """
// 1. workshops/{workshopId}
{
  "id": "WS-LOOP-7789",
  "name": "ورشة لوب لخدمات وصيانة السيارات",
  "commercial_reg": "1010894521",
  "phone": "+9647701234567",
  "email": "mustafa000j@gmail.com",
  "currency": "د.ع",
  "created_at": Timestamp
}

// 2. workshops/{workshopId}/users/{userId}
{
  "uid": "USR-101",
  "email": "saad.cashier@loop.com",
  "name": "سعد علي",
  "role": "HEAD_CASHIER", // "OWNER" | "HEAD_CASHIER" | "STAFF"
  "pin_code": "2222",
  "active_device_id": "DEV-TABLET-POS-02",
  "last_login_at": Timestamp
}

// 3. workshops/{workshopId}/catalog/{itemId}
{
  "code": "OIL-TY-5W30",
  "name": "دهن محرك تويوتا 5W-30",
  "category": "OILS",
  "unit_type": "LITER", // "LITER" | "PIECE" | "KG" | "HOUR" | "SERVICE"
  "is_fractional": true,
  "cost_price": 28000.0,
  "sale_price": 40000.0,
  "stock_quantity": 185.25
}

// 4. workshops/{workshopId}/invoices/{invoiceId}
{
  "invoice_number": "INV-2026-00101",
  "customer_name": "أحمد البصري",
  "customer_phone": "+9647701112233",
  "vehicle_plate": "12345 / بغداد",
  "current_mileage": 82500,
  "next_service_mileage": 87500,
  "next_service_date": "2026-09-15",
  "subtotal": 125000.0,
  "discount": 5000.0,
  "tax": 0.0,
  "total": 120000.0,
  "payment_method": "ZAIN_CASH", // "CASH" | "ZAIN_CASH" | "MASTER_QI_CARD" | "DEBT"
  "cashier_uid": "USR-101",
  "items": [
    { "name": "دهن محرك تويوتا 5W-30", "unit": "LITER", "qty": 4.5, "price": 40000.0, "total": 180000.0 }
  ],
  "created_at": Timestamp
}
    """.trimIndent()

    val securityRulesDocumentation = """
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    function isAuth() {
      return request.auth != null;
    }
    
    function getUserData(workshopId) {
      return get(/databases/$(database)/documents/workshops/$(workshopId)/users/$(request.auth.uid)).data;
    }
    
    function isOwner(workshopId) {
      return isAuth() && getUserData(workshopId).role == 'OWNER';
    }
    
    function isHeadCashier(workshopId) {
      return isAuth() && getUserData(workshopId).role == 'HEAD_CASHIER';
    }
    
    function isStaff(workshopId) {
      return isAuth() && (getUserData(workshopId).role == 'STAFF' || isHeadCashier(workshopId) || isOwner(workshopId));
    }
    
    function isActiveSession(workshopId) {
      return request.resource.data.active_device_id == getUserData(workshopId).active_device_id;
    }

    match /workshops/{workshopId} {
      allow read: if isAuth();
      allow write: if isOwner(workshopId);

      match /users/{userId} {
        allow read: if isAuth();
        allow write: if isOwner(workshopId) || (isAuth() && request.auth.uid == userId);
      }

      match /catalog/{itemId} {
        allow read: if isAuth();
        allow create, update, delete: if isOwner(workshopId) || isHeadCashier(workshopId);
      }

      match /invoices/{invoiceId} {
        allow read: if isAuth();
        // OWNER IS BLOCKED from issuing sales invoices directly!
        allow create: if isAuth() && (isHeadCashier(workshopId) || getUserData(workshopId).role == 'STAFF');
        allow update, delete: if isOwner(workshopId) || isHeadCashier(workshopId);
      }
    }
  }
}
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = LoopTealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "هندسة وقواعد Firestore لـ Loop",
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
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("هيكل النماذج (Schema)") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("قواعد الأمان (Rules)") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (selectedTab == 0) schemaDocumentation else securityRulesDocumentation,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("تم")
            }
        }
    )
}
