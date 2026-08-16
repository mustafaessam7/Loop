package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.local.CatalogItemEntity
import com.example.data.local.InvoiceEntity

object NotificationHelper {

    const val CHANNEL_ID = "loop_push_notifications"
    const val CHANNEL_NAME = "تنبيهات ورشة لوب"
    const val CHANNEL_DESC = "تنبيهات مواعيد الصيانة للزبائن ومستويات المواد المنخفضة بالمخزون"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendPushNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        subText: String = "ورشة لوب"
    ) {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun sendMaintenanceReminderNotification(context: Context, invoice: InvoiceEntity) {
        val car = invoice.vehicleModel.ifBlank { "السيارة" }
        val customer = invoice.customerName.ifBlank { "الزبون" }
        val title = "تذكير صيانة قريب 🔔 ($customer)"
        val message = "موعد صيانة سيارة $customer ($car) قريب! الموعد: ${invoice.nextServiceDate.ifBlank { "اليوم" }}."
        sendPushNotification(context, invoice.id.hashCode(), title, message, "تذكير الزبائن")
    }

    fun sendLowStockNotification(context: Context, item: CatalogItemEntity) {
        val title = "تنبيه نقص مخزون ⚠️ (${item.name})"
        val message = "الكمية المتبقية من ${item.name} هي ${item.stockQuantity} ${item.unitType} فقط! (الحد الأدنى: ${item.minStockAlert})"
        sendPushNotification(context, item.id.hashCode(), title, message, "المخزون والقطع")
    }

    fun sendTestNotification(context: Context) {
        sendPushNotification(
            context = context,
            notificationId = 9991,
            title = "تجربة الإشعارات الفورية (Push Notification) 🔔",
            message = "نظام التنبيهات الفورية في ورشة لوب يعمل بنجاح! ستصلك التنبيهات لمواعيد الصيانة والنقص بالمخزون.",
            subText = "اختبار الإشعار"
        )
    }

    fun scanAndTriggerNotifications(
        context: Context,
        reminders: List<InvoiceEntity>,
        inventory: List<CatalogItemEntity>
    ) {
        if (!hasNotificationPermission(context)) return

        // 1. Scan low stock items
        val lowStockItems = inventory.filter { it.stockQuantity <= it.minStockAlert }
        lowStockItems.take(3).forEach { item ->
            sendLowStockNotification(context, item)
        }

        // 2. Scan upcoming reminders
        reminders.take(3).forEach { invoice ->
            sendMaintenanceReminderNotification(context, invoice)
        }
    }
}
