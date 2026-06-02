package com.smsforward

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import java.time.YearMonth

class SmsReceiver : BroadcastReceiver() {

    private data class CardApproval(val name: String, val pattern: Regex)

    companion object {
        private const val TAG = "SmsReceiver"

        private val CARD_PATTERNS = listOf(
            Regex("""삼성\d{4}승인"""),
            Regex("""신한카드\(\d{4}\)승인"""),
            Regex("""신한카드승인거절""")
        )

        // 누적에 가산할 "승인" 패턴 (승인거절은 제외)
        private val APPROVAL_PATTERNS = listOf(
            CardApproval("삼성", Regex("""삼성\d{4}승인""")),
            CardApproval("신한", Regex("""신한카드\(\d{4}\)승인"""))
        )
        private val AMOUNT_PATTERN = Regex("""\d{1,3}(?:,\d{3})*원""")
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: "알 수 없음"
        val body = messages.joinToString("") { it.messageBody }

        if (!isCardMessage(body)) {
            Log.d(TAG, "카드 문자 아님 → 무시")
            return
        }

        val prefs = PreferenceManager(context)
        val targetNumber = prefs.targetPhoneNumber
        if (targetNumber.isEmpty()) {
            Log.e(TAG, "전달 번호 미설정")
            return
        }

        // 달력 월 기준 카드별 누적 갱신 (승인 건만 가산)
        val period = YearMonth.now().toString()  // 예: "2026-06"
        accumulateApproval(prefs, body, period)
        val summary = buildSummary(period, prefs.getMonthlySummary(period))

        val forwardMessage = "[카드문자 전달]\n발신: $sender\n\n$body$summary"

        try {
            SmsSender.send(context, targetNumber, forwardMessage)
            Log.d(TAG, "문자 전달 성공 → $targetNumber")
        } catch (e: Exception) {
            Log.e(TAG, "문자 전달 실패: ${e.message}")
        }
    }

    private fun isCardMessage(body: String): Boolean {
        val hasCardPattern = CARD_PATTERNS.any { it.containsMatchIn(body) }
        val hasAmount = AMOUNT_PATTERN.containsMatchIn(body)
        return hasCardPattern && hasAmount
    }

    /** 승인 문자면 금액을 추출해 카드별 월 누적에 가산한다. (승인거절은 제외) */
    private fun accumulateApproval(prefs: PreferenceManager, body: String, period: String) {
        if (body.contains("거절")) return
        val approval = APPROVAL_PATTERNS.firstOrNull { it.pattern.containsMatchIn(body) } ?: return
        val amount = extractAmount(body, approval.pattern) ?: run {
            Log.d(TAG, "${approval.name} 승인 금액 파싱 실패 → 누적 제외")
            return
        }
        prefs.addCardSpending(approval.name, amount, period)
        Log.d(TAG, "${approval.name} 누적 +${"%,d".format(amount)}원")
    }

    /** 승인 키워드 직후에 나오는 금액을 우선 추출한다. */
    private fun extractAmount(body: String, approvalPattern: Regex): Long? {
        val match = approvalPattern.find(body)
        val after = match?.let { body.substring(it.range.last + 1) }
        val amountMatch = after?.let { AMOUNT_PATTERN.find(it) } ?: AMOUNT_PATTERN.find(body)
        return amountMatch?.value
            ?.removeSuffix("원")
            ?.replace(",", "")
            ?.toLongOrNull()
    }

    /** 전달 메시지에 덧붙일 월 누적 요약 텍스트를 만든다. */
    private fun buildSummary(period: String, totals: Map<String, Long>): String {
        if (totals.isEmpty()) return ""
        val month = period.substringAfter("-").toIntOrNull() ?: return ""
        val lines = totals.entries
            .sortedByDescending { it.value }
            .joinToString("\n") { "${it.key}: ${"%,d".format(it.value)}원" }
        return "\n\n📊 ${month}월 누적 사용\n$lines"
    }
}
