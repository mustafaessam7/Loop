package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.InvoiceWithItems
import com.example.data.local.UserEntity
import com.example.data.local.WorkshopEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FirestoreWorkshop(
    val id: String = "",
    val name: String = "",
    val ownerName: String = "",
    val ownerEmail: String = "",
    val phone: String = "",
    val address: String = "",
    val isActivated: Boolean = false,
    val licenseKey: String = "",
    val expiryDate: String = "2027-12-31",
    val status: String = "Active", // Active, Suspended, Demo
    val createdAt: Long = System.currentTimeMillis()
)

data class FirestoreLicenseKey(
    val id: String = "",
    val key: String = "",
    val workshopId: String = "",
    val workshopName: String = "",
    val ownerEmail: String = "",
    val isActivated: Boolean = true,
    val expiryDate: String = "2027-12-31",
    val generatedBy: String = "Mustafa000j@gmail.com",
    val timestamp: Long = System.currentTimeMillis()
)

data class ActivationRequest(
    val id: String = "",
    val workshopId: String = "",
    val workshopName: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val city: String = "بغداد",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val requestedAt: Long = System.currentTimeMillis()
)

object FirebaseSyncManager {
    private const val TAG = "FirebaseSyncManager"

    private val _firestoreWorkshops = MutableStateFlow<List<FirestoreWorkshop>>(emptyList())
    val firestoreWorkshops: StateFlow<List<FirestoreWorkshop>> = _firestoreWorkshops.asStateFlow()

    private val _activationRequests = MutableStateFlow<List<ActivationRequest>>(emptyList())
    val activationRequests: StateFlow<List<ActivationRequest>> = _activationRequests.asStateFlow()

    private val _generatedLicenses = MutableStateFlow<List<FirestoreLicenseKey>>(emptyList())
    val generatedLicenses: StateFlow<List<FirestoreLicenseKey>> = _generatedLicenses.asStateFlow()

    fun checkPlayServices(context: Context): Boolean {
        return false
    }

    fun initRealtimeListeners(context: Context, scope: CoroutineScope) {
        Log.d(TAG, "Local Sync Manager initialized successfully.")
    }

    suspend fun syncWorkshopToCloud(workshop: WorkshopEntity, ownerUser: UserEntity?) {
        val existing = _firestoreWorkshops.value.toMutableList()
        existing.removeAll { it.id == workshop.id }
        existing.add(
            FirestoreWorkshop(
                id = workshop.id,
                name = workshop.name,
                phone = workshop.phone,
                address = workshop.address,
                isActivated = workshop.isActivated,
                licenseKey = workshop.licenseKey,
                ownerName = ownerUser?.name ?: "مصطفى (المالك)",
                ownerEmail = ownerUser?.email ?: "Mustafa000j@gmail.com",
                status = if (workshop.isActivated) "Active" else "Demo"
            )
        )
        _firestoreWorkshops.value = existing
    }

    suspend fun sendActivationRequestToCloud(
        workshopId: String,
        workshopName: String,
        ownerName: String,
        ownerPhone: String,
        city: String
    ) {
        val reqId = "REQ-${System.currentTimeMillis().toString().takeLast(6)}"
        val req = ActivationRequest(
            id = reqId,
            workshopId = workshopId,
            workshopName = workshopName,
            ownerName = ownerName,
            ownerPhone = ownerPhone,
            city = city,
            status = "PENDING",
            requestedAt = System.currentTimeMillis()
        )
        _activationRequests.value = _activationRequests.value + req
    }

    suspend fun generateLicenseKey(
        workshopId: String,
        workshopName: String,
        ownerEmail: String,
        customKey: String = "",
        expiryDate: String = "2027-12-31"
    ): String {
        val key = if (customKey.isNotBlank()) customKey.uppercase().trim() else "LOOP-PRO-${(1000..9999).random()}"
        val licenseId = "LIC-${System.currentTimeMillis().toString().takeLast(6)}"
        val license = FirestoreLicenseKey(
            id = licenseId,
            key = key,
            workshopId = workshopId,
            workshopName = workshopName,
            ownerEmail = ownerEmail,
            isActivated = true,
            expiryDate = expiryDate,
            generatedBy = "Mustafa000j@gmail.com",
            timestamp = System.currentTimeMillis()
        )
        _generatedLicenses.value = _generatedLicenses.value + license

        if (workshopId.isNotBlank()) {
            val updated = _firestoreWorkshops.value.map { ws ->
                if (ws.id == workshopId) ws.copy(isActivated = true, licenseKey = key, expiryDate = expiryDate, status = "Active")
                else ws
            }
            _firestoreWorkshops.value = updated
        }
        return key
    }

    suspend fun approveActivationRequest(requestId: String, workshopId: String, workshopName: String, ownerEmail: String) {
        generateLicenseKey(workshopId, workshopName, ownerEmail)
        _activationRequests.value = _activationRequests.value.map { req ->
            if (req.id == requestId) req.copy(status = "APPROVED") else req
        }
    }

    suspend fun updateWorkshopStatus(workshopId: String, status: String, isActivated: Boolean) {
        _firestoreWorkshops.value = _firestoreWorkshops.value.map { ws ->
            if (ws.id == workshopId) ws.copy(status = status, isActivated = isActivated) else ws
        }
    }

    suspend fun syncInvoiceToCloud(invoiceWithItems: InvoiceWithItems) {
        // Local state recording
    }
}
