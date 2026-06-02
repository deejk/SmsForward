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

    /**
     * 카드별 월 누적 사용액에 승인 금액을 가산한다.
     * 저장된 기간([period], 예: "2026-06")이 바뀌면 모든 카드 누적을 리셋한다.
     */
    fun addCardSpending(card: String, amount: Long, period: String) {
        if (prefs.getString(KEY_PERIOD, "") != period) {
            val editor = prefs.edit()
            prefs.all.keys
                .filter { it.startsWith(PREFIX_TOTAL) }
                .forEach { editor.remove(it) }
            editor.putString(KEY_PERIOD, period)
            editor.apply()
        }
        val key = PREFIX_TOTAL + card
        val current = prefs.getLong(key, 0L)
        prefs.edit().putLong(key, current + amount).apply()
    }

    /**
     * 현재 기간([period])의 카드별 누적 사용액을 반환한다.
     * 저장된 기간이 다르면(달이 바뀐 직후) 빈 맵을 반환한다.
     */
    fun getMonthlySummary(period: String): Map<String, Long> {
        if (prefs.getString(KEY_PERIOD, "") != period) return emptyMap()
        return prefs.all
            .filterKeys { it.startsWith(PREFIX_TOTAL) }
            .mapNotNull { (k, v) -> (v as? Long)?.let { k.removePrefix(PREFIX_TOTAL) to it } }
            .toMap()
    }

    companion object {
        private const val KEY_PERIOD = "monthly_period"
        private const val PREFIX_TOTAL = "monthly_total_"
    }
}
