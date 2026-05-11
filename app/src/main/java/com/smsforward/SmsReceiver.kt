package com.smsforward

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: "알 수 없음"
        val body = messages.joinToString("") { it.messageBody }

        val pref = PreferenceManager(context)

        if (!isCardMessage(body, pref.getKeywordList())) {
            Log.d(TAG, "카드 문자 아님 → 무시")
            return
        }

        val targetNumber = pref.targetPhoneNumber
        if (targetNumber.isEmpty()) {
            Log.e(TAG, "전달 번호 미설정")
            return
        }

        val forwardMessage = "[카드문자 전달]\n발신: $sender\n\n$body"

        try {
            SmsSender.send(context, targetNumber, forwardMessage)
            Log.d(TAG, "문자 전달 성공 → $targetNumber")
        } catch (e: Exception) {
            Log.e(TAG, "문자 전달 실패: ${e.message}")
        }
    }

    private fun isCardMessage(body: String, keywords: List<String>): Boolean =
        keywords.any { body.contains(it, ignoreCase = true) }
}
