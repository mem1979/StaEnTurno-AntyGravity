package com.sta.staenturno.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.sta.staenturno.data.local.PrefsManager
import java.util.UUID

class DeviceIdProvider(private val context: Context, private val prefsManager: PrefsManager) {

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        // Try to get from prefs first
        var deviceId = prefsManager.getDeviceId()
        if (!deviceId.isNullOrEmpty()) {
            return deviceId
        }

        // Try ANDROID_ID
        deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        
        // Fallback if ANDROID_ID is null or empty (rare but possible)
        if (deviceId.isNullOrEmpty()) {
            deviceId = UUID.randomUUID().toString()
        }

        // Save for persistence
        prefsManager.saveDeviceId(deviceId)
        return deviceId
    }
}
