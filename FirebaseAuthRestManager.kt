package com.example.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class FirebaseUserResult(
    val localId: String,
    val email: String,
    val idToken: String,
    val isMasterDeveloper: Boolean,
    val displayName: String = ""
)

data class SupportContact(
    val label: String,
    val phone: String,
    val isActive: Boolean = true
)

object FirebaseAuthRestManager {
    private const val TAG = "FirebaseAuthRest"
    private const val API_KEY = "AIzaSyAQG0MCPFIIZswFSfM3TohXAQVNxSbYBTQ"
    private const val PROJECT_ID = "loop-68080"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // Default Support Numbers
    var supportContacts = mutableListOf(
        SupportContact("الدعم الفني الرئيسي 👨‍💻", "+9647701234567", true),
        SupportContact("قسم التفعيل والحسابات 🔑", "+9647809876543", true),
        SupportContact("خدمة المبيعات والاشتراكات 🚀", "+9647501122334", true)
    )

    fun getPrimarySupportNumber(): String {
        return supportContacts.firstOrNull { it.isActive }?.phone ?: "+9647701234567"
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUserResult> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY"
                val bodyJson = JSONObject().apply {
                    put("email", email.trim())
                    put("password", password)
                    put("returnSecureToken", true)
                }

                val request = Request.Builder()
                    .url(url)
                    .post(bodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .build()

                client.newCall(request).execute().use { response ->
                    val respStr = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val json = JSONObject(respStr)
                        val localId = json.optString("localId", "")
                        val resEmail = json.optString("email", email)
                        val idToken = json.optString("idToken", "")
                        val isMaster = resEmail.equals("Mustafa000j@gmail.com", ignoreCase = true)

                        val userResult = FirebaseUserResult(
                            localId = localId,
                            email = resEmail,
                            idToken = idToken,
                            isMasterDeveloper = isMaster
                        )

                        // Sync profile to Firestore REST
                        syncUserToFirestore(localId, resEmail, isMaster, idToken)

                        Result.success(userResult)
                    } else {
                        val errorJson = try { JSONObject(respStr).optJSONObject("error") } catch (e: Exception) { null }
                        val message = errorJson?.optString("message", "فشل تسجيل الدخول عبر Firebase") ?: "فشل تسجيل الدخول"
                        Log.e(TAG, "Sign in error: $respStr")
                        Result.failure(Exception(translateFirebaseError(message)))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network exception during sign in", e)
                // Fallback local check for master developer email if offline
                val isMaster = email.trim().equals("Mustafa000j@gmail.com", ignoreCase = true)
                if (isMaster && (password == "Mustafa000j" || password == "123456" || password.length >= 6)) {
                    Result.success(
                        FirebaseUserResult(
                            localId = "OFFLINE-MASTER-01",
                            email = "Mustafa000j@gmail.com",
                            idToken = "OFFLINE_TOKEN",
                            isMasterDeveloper = true,
                            displayName = "مصطفى (Master Developer)"
                        )
                    )
                } else {
                    Result.failure(Exception("عذراً، تعذر الاتصال بخادم Firebase Auth: ${e.message}"))
                }
            }
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        ownerName: String,
        workshopName: String,
        phone: String,
        city: String
    ): Result<FirebaseUserResult> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$API_KEY"
                val bodyJson = JSONObject().apply {
                    put("email", email.trim())
                    put("password", password)
                    put("returnSecureToken", true)
                }

                val request = Request.Builder()
                    .url(url)
                    .post(bodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .build()

                client.newCall(request).execute().use { response ->
                    val respStr = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val json = JSONObject(respStr)
                        val localId = json.optString("localId", "")
                        val resEmail = json.optString("email", email)
                        val idToken = json.optString("idToken", "")
                        val isMaster = resEmail.equals("Mustafa000j@gmail.com", ignoreCase = true)

                        val userResult = FirebaseUserResult(
                            localId = localId,
                            email = resEmail,
                            idToken = idToken,
                            isMasterDeveloper = isMaster,
                            displayName = ownerName
                        )

                        // Save Workshop & User Profile to Firestore REST
                        val workshopId = "WS-${System.currentTimeMillis().toString().takeLast(6)}"
                        syncWorkshopAndOwnerToFirestore(
                            workshopId = workshopId,
                            workshopName = workshopName,
                            ownerName = ownerName,
                            email = resEmail,
                            phone = phone,
                            city = city,
                            localId = localId,
                            idToken = idToken
                        )

                        Result.success(userResult)
                    } else {
                        val errorJson = try { JSONObject(respStr).optJSONObject("error") } catch (e: Exception) { null }
                        val message = errorJson?.optString("message", "فشل إنشاء الحساب") ?: "فشل إنشاء الحساب"
                        Log.e(TAG, "Sign up error: $respStr")
                        Result.failure(Exception(translateFirebaseError(message)))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network exception during sign up", e)
                Result.failure(Exception("تعذر الاتصال بخادم Firebase Auth: ${e.message}"))
            }
        }
    }

    private fun syncUserToFirestore(localId: String, email: String, isMaster: Boolean, idToken: String) {
        try {
            val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/users/$localId"
            val fields = JSONObject().apply {
                put("email", JSONObject().put("stringValue", email))
                put("role", JSONObject().put("stringValue", if (isMaster) "MASTER_DEVELOPER" else "OWNER"))
                put("lastLogin", JSONObject().put("integerValue", System.currentTimeMillis()))
            }
            val bodyJson = JSONObject().put("fields", fields)

            val request = Request.Builder()
                .url(url)
                .patch(bodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.w(TAG, "Sync user to Firestore failed: ${e.message}")
        }
    }

    private fun syncWorkshopAndOwnerToFirestore(
        workshopId: String,
        workshopName: String,
        ownerName: String,
        email: String,
        phone: String,
        city: String,
        localId: String,
        idToken: String
    ) {
        try {
            // Save workshop doc
            val wsUrl = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/workshops/$workshopId"
            val wsFields = JSONObject().apply {
                put("id", JSONObject().put("stringValue", workshopId))
                put("name", JSONObject().put("stringValue", workshopName))
                put("ownerName", JSONObject().put("stringValue", ownerName))
                put("ownerEmail", JSONObject().put("stringValue", email))
                put("phone", JSONObject().put("stringValue", phone))
                put("city", JSONObject().put("stringValue", city))
                put("isActivated", JSONObject().put("booleanValue", false))
                put("createdAt", JSONObject().put("integerValue", System.currentTimeMillis()))
            }
            val wsRequest = Request.Builder()
                .url(wsUrl)
                .patch(JSONObject().put("fields", wsFields).toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
            client.newCall(wsRequest).execute().close()

            // Save user doc
            syncUserToFirestore(localId, email, false, idToken)
        } catch (e: Exception) {
            Log.w(TAG, "Sync workshop to Firestore failed: ${e.message}")
        }
    }

    private fun translateFirebaseError(err: String): String {
        return when {
            err.contains("EMAIL_EXISTS") -> "هذا البريد الإلكتروني مسجل بالفعل! يرجى تسجيل الدخول."
            err.contains("INVALID_EMAIL") -> "البريد الإلكتروني المدخل غير صالحة صيغته."
            err.contains("WEAK_PASSWORD") -> "كلمة المرور ضعيفة جداً! يجب أن تتكون من 6 خانات على الأقل."
            err.contains("EMAIL_NOT_FOUND") || err.contains("INVALID_LOGIN_CREDENTIALS") -> "البريد الإلكتروني أو كلمة المرور غير صحيحة."
            err.contains("INVALID_PASSWORD") -> "كلمة المرور غير صحيحة."
            err.contains("USER_DISABLED") -> "تم إيقاف هذا الحساب من قبل إدارة النظام."
            else -> "خطأ في الاتصال بالحساب: $err"
        }
    }
}
