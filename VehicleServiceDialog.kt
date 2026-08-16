package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.VehicleServiceInfo
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopTealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun VehicleServiceDialog(
    initialInfo: VehicleServiceInfo,
    onDismiss: () -> Unit,
    onConfirm: (VehicleServiceInfo) -> Unit
) {
    var plateNumber by remember { mutableStateOf(initialInfo.plateNumber) }
    var vehicleModel by remember { mutableStateOf(initialInfo.vehicleModel) }
    var customerName by remember { mutableStateOf(initialInfo.customerName) }
    var customerPhone by remember { mutableStateOf(initialInfo.customerPhone) }
    var currentMileageStr by remember { mutableStateOf(if (initialInfo.currentMileage > 0) initialInfo.currentMileage.toString() else "") }
    var nextMileageStr by remember { mutableStateOf(if (initialInfo.nextServiceMileage > 0) initialInfo.nextServiceMileage.toString() else "") }
    var nextServiceDate by remember { mutableStateOf(initialInfo.nextServiceDate) }
    var serviceNotes by remember { mutableStateOf(initialInfo.serviceNotes) }

    fun autoCalculate(months: Int = 3, km: Int = 5000) {
        val currentKm = currentMileageStr.toIntOrNull() ?: 0
        val calculatedNextKm = if (currentKm > 0) currentKm + km else 5000
        nextMileageStr = calculatedNextKm.toString()

        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, months)
        nextServiceDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

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
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = LoopTealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "بيانات المركبة والصيانة الدورية",
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
                // Vehicle Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it },
                        label = { Text("رقم اللوحة (مثال: 12345 / بغداد)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vehicle_plate_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = vehicleModel,
                        onValueChange = { vehicleModel = it },
                        label = { Text("نوع وموديل السيارة") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Customer Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("اسم الزبون") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("رقم الهاتف (مثال: 07701234567)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Mileage & Service Calculator Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LoopTealPrimary.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حساب موعد الصيانة وتبديل الدهن:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = LoopTealPrimary
                            )

                            OutlinedButton(
                                onClick = { autoCalculate(months = 3, km = 5000) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("توليد تلقائي (+5,000 كم)", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = currentMileageStr,
                                onValueChange = { currentMileageStr = it },
                                label = { Text("العداد الحالي (كم)") },
                                leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = nextMileageStr,
                                onValueChange = { nextMileageStr = it },
                                label = { Text("عداد الصيانة القادمة") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = nextServiceDate,
                            onValueChange = { nextServiceDate = it },
                            label = { Text("تاريخ الصيانة القادمة (YYYY-MM-DD)") },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = serviceNotes,
                    onValueChange = { serviceNotes = it },
                    label = { Text("ملاحظات الفحص والورشة") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = VehicleServiceInfo(
                        plateNumber = plateNumber.trim(),
                        vehicleModel = vehicleModel.trim(),
                        customerName = customerName.trim(),
                        customerPhone = customerPhone.trim(),
                        currentMileage = currentMileageStr.toIntOrNull() ?: 0,
                        nextServiceMileage = nextMileageStr.toIntOrNull() ?: 0,
                        nextServiceDate = nextServiceDate.trim(),
                        serviceNotes = serviceNotes.trim()
                    )
                    onConfirm(updated)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_vehicle_info_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ البيانات")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("إلغاء")
            }
        }
    )
}
