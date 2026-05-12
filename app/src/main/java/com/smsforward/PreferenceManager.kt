package com.smsforward

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sms_forward_prefs", Context.MODE_PRIVATE)

    var targetPhoneNumber: String
        get() = prefs.getString("target_phone", "") ?: ""
        set(value) = prefs.edit().putString("target_phone", value).apply()

    var serviceEnabled: Boolean
        get() = prefs.getBoolean("service_enabled", false)
        set(value) = prefs.edit().putBoolean("service_enabled", value).apply()
}
