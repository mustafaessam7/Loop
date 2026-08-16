package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.data.local.CashboxTransactionEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.WorkshopEntity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfPrintAdapter(private val file: File) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }
        val pdi = PrintDocumentInfo.Builder(file.name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()
        callback?.onLayoutFinished(pdi, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        try {
            FileInputStream(file).use { input ->
                FileOutputStream(destination?.fileDescriptor).use { output ->
                    input.copyTo(output)
                }
            }
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
        }
    }
}

object PdfInvoiceGenerator {

    /**
     * Builds pre-filled WhatsApp message for PDF sharing with dynamic sanitization
     */
    fun buildWhatsAppPdfCaption(
        customerName: String,
        carModel: String?,
        workshopName: String
    ): String {
        val formattedCustomer = customerName.trim().ifBlank { "عزيزنا الزبون" }
        val formattedWorkshop = workshopName.trim().ifBlank { "ورشة لوب" }
        val isCarBlank = carModel.isNullOrBlank() || carModel.trim().isBlank()
        val formattedCar = if (isCarBlank) "سيارتك" else carModel!!.trim()

        val template = """أهلاً وسهلاً أستاذ {customer_name} 🌹
مرفق لكم فاتورة صيانة سيارتك {car_model} من ورشة {workshop_name}.
شكراً لثقتكم بنا، ونتمنى لكم قيادة آمنة دائماً! ❤️"""

        var replaced = template
            .replace("{customer_name}", formattedCustomer)
            .replace("{car_model}", formattedCar)
            .replace("{workshop_name}", formattedWorkshop)

        // Dynamic sanitization to eliminate double "سيارتك سيارتك"
        while (replaced.contains("سيارتك سيارتك")) {
            replaced = replaced.replace("سيارتك سيارتك", "سيارتك")
        }
        return replaced.replace("  ", " ").trim()
    }

    /**
     * Generates a clean PDF invoice file on device storage
     */
    fun generatePdfInvoiceFile(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        workshop: WorkshopEntity?
    ): File? {
        return try {
            val inv = invoiceWithItems.invoice
            val items = invoiceWithItems.items
            val wsName = workshop?.name?.ifBlank { "ورشة لوب لصيانة السيارات" } ?: "ورشة لوب لصيانة السيارات"
            val wsPhone = workshop?.phone?.ifBlank { "+964 770 123 4567" } ?: "+964 770 123 4567"
            val wsAddress = workshop?.address?.ifBlank { "بغداد - الكرادة" } ?: "بغداد - الكرادة"
            val numberFormat = NumberFormat.getNumberInstance(Locale.US)

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint().apply {
                isAntiAlias = true
            }

            // Background canvas
            canvas.drawColor(Color.WHITE)

            // Header Top Bar
            paint.color = Color.parseColor("#008080") // Loop Teal
            canvas.drawRect(0f, 0f, 595f, 90f, paint)

            paint.color = Color.WHITE
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(wsName, 30f, 42f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("$wsAddress | هاتف: $wsPhone", 30f, 65f, paint)

            // Invoice Title Right
            paint.color = Color.parseColor("#FFD700") // Gold Accent
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("فاتورة صيانة واستلام", 420f, 45f, paint)
            paint.textSize = 10f
            paint.color = Color.WHITE
            canvas.drawText("INVOICE RECEIPT", 420f, 65f, paint)

            var y = 120f

            // Invoice Meta Information Box
            paint.color = Color.parseColor("#F4F6F8")
            canvas.drawRoundRect(25f, y, 570f, y + 65f, 10f, 10f, paint)

            paint.color = Color.parseColor("#333333")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(inv.timestamp))
            canvas.drawText("رقم الفاتورة: #${inv.id}", 40f, y + 25f, paint)
            canvas.drawText("التاريخ: $dateStr", 220f, y + 25f, paint)
            canvas.drawText("طريقة الدفع: ${inv.paymentMethod}", 420f, y + 25f, paint)

            val formattedCar = inv.vehicleModel.ifBlank { "غير محدد" }
            val formattedPlate = inv.vehiclePlate.ifBlank { "بدون لوحة" }
            val formattedMileage = if (inv.currentMileage > 0) "${numberFormat.format(inv.currentMileage)} كم" else "غير مسجل"

            canvas.drawText("اسم الزبون: ${inv.customerName.ifBlank { "زبون عام" }}", 40f, y + 48f, paint)
            canvas.drawText("السيارة: $formattedCar ($formattedPlate)", 220f, y + 48f, paint)
            canvas.drawText("العداد: $formattedMileage", 420f, y + 48f, paint)

            y += 85f

            // Items Table Header
            paint.color = Color.parseColor("#008080")
            canvas.drawRoundRect(25f, y, 570f, y + 28f, 6f, 6f, paint)

            paint.color = Color.WHITE
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("#", 35f, y + 18f, paint)
            canvas.drawText("اسم المادة / الخدمة", 65f, y + 18f, paint)
            canvas.drawText("الكمية", 320f, y + 18f, paint)
            canvas.drawText("السعر (د.ع)", 410f, y + 18f, paint)
            canvas.drawText("المجموع (د.ع)", 490f, y + 18f, paint)

            y += 35f

            // Table Rows
            paint.color = Color.parseColor("#222222")
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 10f

            items.forEachIndexed { index, item ->
                if (index % 2 == 1) {
                    val bgPaint = Paint().apply { color = Color.parseColor("#FAFBFD") }
                    canvas.drawRect(25f, y - 12f, 570f, y + 14f, bgPaint)
                }

                canvas.drawText("${index + 1}", 35f, y, paint)

                val itemName = if (item.itemName.length > 35) item.itemName.take(33) + ".." else item.itemName
                canvas.drawText(itemName, 65f, y, paint)
                canvas.drawText("${item.quantity} ${item.unitType}", 320f, y, paint)
                canvas.drawText("${numberFormat.format(item.unitPrice.toLong())} د.ع", 410f, y, paint)
                canvas.drawText("${numberFormat.format(item.totalPrice.toLong())} د.ع", 490f, y, paint)

                y += 22f
            }

            // Divider Line
            y += 10f
            paint.color = Color.parseColor("#DDDDDD")
            canvas.drawLine(25f, y, 570f, y, paint)
            y += 20f

            // Totals Summary Box Right
            paint.color = Color.parseColor("#F8F9FA")
            canvas.drawRoundRect(320f, y, 570f, y + 80f, 8f, 8f, paint)

            paint.color = Color.parseColor("#333333")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val subtotalFormatted = numberFormat.format(inv.subtotal.toLong())
            val discountFormatted = numberFormat.format(inv.discount.toLong())
            val totalFormatted = numberFormat.format(inv.total.toLong())

            canvas.drawText("المجموع الكلي:", 335f, y + 22f, paint)
            canvas.drawText("$subtotalFormatted د.ع", 480f, y + 22f, paint)

            canvas.drawText("الخصم الممنوح:", 335f, y + 42f, paint)
            canvas.drawText("$discountFormatted د.ع", 480f, y + 42f, paint)

            paint.color = Color.parseColor("#008080")
            paint.textSize = 13f
            canvas.drawText("الصافي المدفوع:", 335f, y + 66f, paint)
            canvas.drawText("$totalFormatted د.ع", 470f, y + 66f, paint)

            // Notes section left
            if (inv.notes.isNotBlank()) {
                paint.color = Color.parseColor("#555555")
                paint.textSize = 9f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("ملاحظات الصيانة: ${inv.notes}", 30f, y + 25f, paint)
            }

            y += 110f

            // Footer Note
            paint.color = Color.parseColor("#008080")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val footerMsg = "شكراً لزيارتكم ورشة $wsName - نتمنى لكم قيادة آمنة دائماً! ❤️"
            canvas.drawText(footerMsg, 60f, y, paint)

            pdfDocument.finishPage(page)

            // Save PDF File
            val pdfDir = File(context.cacheDir, "pdf_invoices").apply { if (!exists()) mkdirs() }
            val pdfFile = File(pdfDir, "Invoice_${inv.id}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Triggers Android share intent with attached PDF file and pre-filled WhatsApp message
     */
    fun sharePdfInvoiceViaWhatsApp(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        workshop: WorkshopEntity?
    ) {
        val pdfFile = generatePdfInvoiceFile(context, invoiceWithItems, workshop)
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(context, "خطأ في إنشاء ملف الـ PDF", Toast.LENGTH_SHORT).show()
            return
        }

        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

        val captionMessage = buildWhatsAppPdfCaption(
            customerName = invoiceWithItems.invoice.customerName,
            carModel = invoiceWithItems.invoice.vehicleModel,
            workshopName = workshop?.name ?: "ورشة لوب"
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, captionMessage)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            val chooser = Intent.createChooser(shareIntent, "مشاركة فاتورة PDF عبر واتساب")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح تطبيق الواتساب أو مشاركة الملف", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generates a compact Thermal Receipt PDF (80mm width = 226 points)
     */
    fun generateThermalReceiptFile(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        workshop: WorkshopEntity?
    ): File? {
        return try {
            val inv = invoiceWithItems.invoice
            val items = invoiceWithItems.items
            val wsName = workshop?.name?.ifBlank { "ورشة لوب لصيانة السيارات" } ?: "ورشة لوب لصيانة السيارات"
            val wsPhone = workshop?.phone?.ifBlank { "+964 770 123 4567" } ?: "+964 770 123 4567"
            val numberFormat = NumberFormat.getNumberInstance(Locale.US)

            // 80mm thermal paper width = 226pt. Calculate height based on items
            val pageHeight = maxOf(400, 220 + (items.size * 22) + 120)
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(226, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
            }

            canvas.drawColor(Color.WHITE)

            // Header Title
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(wsName, 10f, 25f, paint)

            paint.textSize = 8f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("هاتف: $wsPhone", 10f, 38f, paint)

            paint.color = Color.DKGRAY
            canvas.drawLine(10f, 48f, 216f, 48f, paint)

            // Invoice Info
            paint.color = Color.BLACK
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(inv.timestamp))
            canvas.drawText("وصل: #${inv.id}  |  $dateStr", 10f, 62f, paint)
            canvas.drawText("الزبون: ${inv.customerName.ifBlank { "زبون عام" }}", 10f, 75f, paint)
            if (inv.vehiclePlate.isNotBlank()) {
                canvas.drawText("المركبة: ${inv.vehicleModel} (${inv.vehiclePlate})", 10f, 88f, paint)
            }

            canvas.drawLine(10f, 96f, 216f, 96f, paint)

            var y = 110f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("المادة/الخدمة", 10f, y, paint)
            canvas.drawText("الكمية", 120f, y, paint)
            canvas.drawText("المجموع", 165f, y, paint)

            y += 12f
            canvas.drawLine(10f, y, 216f, y, paint)
            y += 14f

            paint.typeface = Typeface.DEFAULT
            items.forEach { item ->
                val nameTrimmed = if (item.itemName.length > 16) item.itemName.take(15) + ".." else item.itemName
                canvas.drawText(nameTrimmed, 10f, y, paint)
                canvas.drawText("${item.quantity}", 125f, y, paint)
                canvas.drawText("${numberFormat.format(item.totalPrice.toLong())}", 165f, y, paint)
                y += 18f
            }

            canvas.drawLine(10f, y, 216f, y, paint)
            y += 16f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("المجموع:", 10f, y, paint)
            canvas.drawText("${numberFormat.format(inv.subtotal.toLong())} د.ع", 130f, y, paint)

            if (inv.discount > 0) {
                y += 14f
                canvas.drawText("الخصم:", 10f, y, paint)
                canvas.drawText("-${numberFormat.format(inv.discount.toLong())} د.ع", 130f, y, paint)
            }

            y += 16f
            paint.textSize = 10f
            canvas.drawText("الصافي:", 10f, y, paint)
            canvas.drawText("${numberFormat.format(inv.total.toLong())} د.ع", 130f, y, paint)

            y += 24f
            paint.textSize = 8f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("الكاشير: ${inv.cashierName}", 10f, y, paint)
            y += 14f
            canvas.drawText("شكراً لزيارتكم ورشة $wsName! ❤️", 10f, y, paint)

            pdfDocument.finishPage(page)

            val pdfDir = File(context.cacheDir, "thermal_receipts").apply { if (!exists()) mkdirs() }
            val pdfFile = File(pdfDir, "Thermal_Receipt_${inv.id}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Sends A4 PDF to Android PrintManager
     */
    fun printA4PdfInvoice(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        workshop: WorkshopEntity?
    ) {
        val pdfFile = generatePdfInvoiceFile(context, invoiceWithItems, workshop)
        if (pdfFile != null && pdfFile.exists()) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val jobName = "A4_Invoice_${invoiceWithItems.invoice.id}"
                printManager.print(jobName, PdfPrintAdapter(pdfFile), PrintAttributes.Builder().build())
            } else {
                Toast.makeText(context, "خدمة الطباعة غير متوفرة على هذا الجهاز", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Sends Thermal Receipt to Android PrintManager
     */
    fun printThermalReceipt(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        workshop: WorkshopEntity?
    ) {
        val pdfFile = generateThermalReceiptFile(context, invoiceWithItems, workshop)
        if (pdfFile != null && pdfFile.exists()) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val jobName = "Thermal_Receipt_${invoiceWithItems.invoice.id}"
                printManager.print(jobName, PdfPrintAdapter(pdfFile), PrintAttributes.Builder().build())
            } else {
                Toast.makeText(context, "خدمة الطباعة غير متوفرة على هذا الجهاز", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Generates a PDF Monthly Financial Report
     */
    fun generateMonthlyFinancialReportPdf(
        context: Context,
        workshop: WorkshopEntity?,
        transactions: List<CashboxTransactionEntity>,
        invoices: List<InvoiceWithItems>,
        monthTitle: String = "التقرير المالي الشهري"
    ): File? {
        return try {
            val wsName = workshop?.name?.ifBlank { "ورشة لوب لصيانة السيارات" } ?: "ورشة لوب لصيانة السيارات"
            val numberFormat = NumberFormat.getNumberInstance(Locale.US)

            val totalRevenue = invoices.sumOf { it.invoice.total }
            val totalExpenses = transactions.filter { it.type == "EXPENSE" || it.type == "WITHDRAWAL" }.sumOf { it.amount }
            val estimatedCosts = invoices.flatMap { it.items }.sumOf { it.totalPrice * 0.60 }
            val netProfit = maxOf(0.0, totalRevenue - estimatedCosts - totalExpenses)

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint().apply { isAntiAlias = true }

            canvas.drawColor(Color.WHITE)

            // Top Header Bar
            paint.color = Color.parseColor("#008080") // Loop Teal
            canvas.drawRect(0f, 0f, 595f, 90f, paint)

            paint.color = Color.WHITE
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(wsName, 30f, 42f, paint)

            paint.textSize = 12f
            paint.color = Color.parseColor("#FFD700")
            canvas.drawText("تقرير الأرباح والمؤشرات المالية - $monthTitle", 30f, 68f, paint)

            var y = 120f

            // Summary KPI Cards
            paint.color = Color.parseColor("#F4F6F8")
            canvas.drawRoundRect(25f, y, 185f, y + 65f, 8f, 8f, paint)
            canvas.drawRoundRect(205f, y, 365f, y + 65f, 8f, 8f, paint)
            canvas.drawRoundRect(385f, y, 570f, y + 65f, 8f, 8f, paint)

            paint.textSize = 10f
            paint.color = Color.parseColor("#555555")
            paint.typeface = Typeface.DEFAULT

            canvas.drawText("إجمالي المبيعات", 35f, y + 22f, paint)
            canvas.drawText("إجمالي المصاريف", 215f, y + 22f, paint)
            canvas.drawText("الربح الصافي التقديري", 395f, y + 22f, paint)

            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            paint.color = Color.parseColor("#008080")
            canvas.drawText("${numberFormat.format(totalRevenue.toLong())} د.ع", 35f, y + 48f, paint)

            paint.color = Color.parseColor("#EF4444")
            canvas.drawText("${numberFormat.format(totalExpenses.toLong())} د.ع", 215f, y + 48f, paint)

            paint.color = Color.parseColor("#10B981")
            canvas.drawText("${numberFormat.format(netProfit.toLong())} د.ع", 395f, y + 48f, paint)

            y += 90f

            // Performance Statistics
            paint.color = Color.parseColor("#333333")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("إحصائيات الأداء التشغيلي:", 30f, y, paint)

            y += 20f
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("• عدد الفواتير الصادرة: ${invoices.size} فاتورة", 40f, y, paint)
            y += 18f
            canvas.drawText("• معدل قيمة الفاتورة (Average Ticket): ${numberFormat.format(if (invoices.isNotEmpty()) (totalRevenue / invoices.size).toLong() else 0L)} د.ع", 40f, y, paint)
            y += 18f
            canvas.drawText("• إجمالي الحركات النقدية في الصندوق: ${transactions.size} حركة", 40f, y, paint)

            y += 30f
            paint.color = Color.parseColor("#DDDDDD")
            canvas.drawLine(25f, y, 570f, y, paint)
            y += 20f

            // Footer note
            paint.color = Color.parseColor("#008080")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("تم استخراج التقرير آلياً عبر نظام إدارة الورش LOOP Auto Care", 30f, y, paint)

            pdfDocument.finishPage(page)

            val pdfDir = File(context.cacheDir, "financial_reports").apply { if (!exists()) mkdirs() }
            val pdfFile = File(pdfDir, "Monthly_Report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Prints or exports Monthly Financial Report
     */
    fun printMonthlyFinancialReport(
        context: Context,
        workshop: WorkshopEntity?,
        transactions: List<CashboxTransactionEntity>,
        invoices: List<InvoiceWithItems>,
        monthTitle: String = "التقرير المالي الشهري"
    ) {
        val pdfFile = generateMonthlyFinancialReportPdf(context, workshop, transactions, invoices, monthTitle)
        if (pdfFile != null && pdfFile.exists()) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                printManager.print("Financial_Report_$monthTitle", PdfPrintAdapter(pdfFile), PrintAttributes.Builder().build())
            } else {
                Toast.makeText(context, "تم حفظ الملف بنجاح", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "تعذر إنشاء ملف التقرير المالي", Toast.LENGTH_SHORT).show()
        }
    }
}
