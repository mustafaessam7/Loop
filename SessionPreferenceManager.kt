package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.model.UserRole

class SessionPreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "loop_session_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_WORKSHOP_ID = "workshop_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_ACTIVE_DEVICE_ID = "active_device_id"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    fun saveSession(user: UserEntity, authToken: String = "") {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, user.id)
            putString(KEY_WORKSHOP_ID, user.workshopId)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_ROLE, user.role.name)
            putString(KEY_ACTIVE_DEVICE_ID, user.activeDeviceId)
            putString(KEY_AUTH_TOKEN, authToken)
            apply()
        }
    }

    fun getSavedUser(): UserEntity? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val email = prefs.getString(KEY_USER_EMAIL, null)
        val id = prefs.getString(KEY_USER_ID, null)

        if (!isLoggedIn || email.isNullOrBlank() || id.isNullOrBlank()) {
            return null
        }

        val name = prefs.getString(KEY_USER_NAME, email.substringBefore("@")) ?: "المستخدم"
        val workshopId = prefs.getString(KEY_WORKSHOP_ID, "WS-MAIN-001") ?: "WS-MAIN-001"
        val roleStr = prefs.getString(KEY_USER_ROLE, UserRole.OWNER.name) ?: UserRole.OWNER.name
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            if (email.equals("Mustafa000j@gmail.com", ignoreCase = true)) UserRole.MASTER_DEVELOPER else UserRole.OWNER
        }
        val activeDeviceId = prefs.getString(KEY_ACTIVE_DEVICE_ID, "DEV-PRIMARY-CLIENT") ?: "DEV-PRIMARY-CLIENT"

        return UserEntity(
            id = id,
            workshopId = workshopId,
            name = name,
            email = email,
            role = role,
            pinCode = "",
            activeDeviceId = activeDeviceId
        )
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && !prefs.getString(KEY_USER_EMAIL, null).isNullOrBlank()
    }

    fun getAuthToken(): String {
        return prefs.getString(KEY_AUTH_TOKEN, "") ?: ""
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
