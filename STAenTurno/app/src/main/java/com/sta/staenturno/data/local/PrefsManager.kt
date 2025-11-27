package com.sta.staenturno.data.local

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sta_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_DEVICE_ID = "device_id"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }
    
    fun saveDeviceId(deviceId: String) {
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }

    fun getDeviceId(): String? {
        return prefs.getString(KEY_DEVICE_ID, null)
    }
}
