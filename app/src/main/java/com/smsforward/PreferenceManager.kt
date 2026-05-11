package com.smsforward

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sms_forward_prefs", Context.MODE_PRIVATE)

    var targetPhoneNumber: String
        get() = prefs.getString("target_phone", "") ?: ""
        set(value) = prefs.edit().putString("target_phone", value).apply()

    var filterKeywords: String
        get() = prefs.getString(
            "filter_keywords",
            "신한카드,삼성카드,현대카드,KB카드,롯데카드,하나카드,우리카드,BC카드,씨티카드,농협카드,승인,결제,카드"
        ) ?: ""
        set(value) = prefs.edit().putString("filter_keywords", value).apply()

    fun getKeywordList(): List<String> =
        filterKeywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    var serviceEnabled: Boolean
        get() = prefs.getBoolean("service_enabled", false)
        set(value) = prefs.edit().putBoolean("service_enabled", value).apply()
}
