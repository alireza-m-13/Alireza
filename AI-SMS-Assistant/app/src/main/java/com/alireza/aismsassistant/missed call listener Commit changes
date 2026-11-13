
package com.alireza.aismsassistant

import android.app.Notification
import android.content.SharedPreferences
import android.net.Uri
import android.provider.ContactsContract
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import java.util.Locale

class MissedCallNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val prefs = getSharedPreferences("assistant", MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return
        if (sbn.packageName !in PHONE_PACKAGES) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val values = mutableListOf<String>()

        fun add(value: CharSequence?) {
            if (!value.isNullOrBlank()) values.add(value.toString())
        }

        add(extras.getCharSequence(Notification.EXTRA_TITLE))
        add(extras.getCharSequence(Notification.EXTRA_TEXT))
        add(extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
        add(extras.getCharSequence(Notification.EXTRA_BIG_TEXT))

        extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.forEach { add(it) }

        val categoryMissed =
            notification.category == Notification.CATEGORY_MISSED_CALL

        val combined = values.joinToString(" ").lowercase(Locale.ROOT)
        val textMissed = MISSED_WORDS.any { combined.contains(it) }

        if (!categoryMissed && !textMissed) return

        val number = findNumber(values) ?: return
        val savedNumber = findSavedContactNumber(number) ?: return

        sendReplyOnce(savedNumber, prefs)
    }

    private fun findNumber(values: List<String>): String? {
        val pattern = Regex("(?<![\\d])(?:\\+?\\d[\\d\\s().-]{5,}\\d)(?![\\d])")

        for (value in values) {
            val tel = Regex("tel:([^,; ]+)", RegexOption.IGNORE_CASE)
                .find(value)?.groupValues?.get(1)

            val candidate = tel ?: pattern.find(value)?.value ?: continue

            val normalized = PhoneNumberUtils.normalizeNumber(
                Uri.decode(candidate).replace(Regex("[^+\\d]"), "")
            )

            if (normalized.count { it.isDigit() } >= 7) return normalized
        }

        return null
    }

    private fun findSavedContactNumber(number: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )

        contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.NUMBER),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getString(0)
        }

        return null
    }

    private fun sendReplyOnce(
        number: String,
        prefs: SharedPreferences
    ) {
        val key = PhoneNumberUtils.normalizeNumber(number)
        val now = System.currentTimeMillis()

        if (now - prefs.getLong("last_call_$key", 0L) < 24 * 60 * 60 * 1000L) return

        try {
            val message =
                "سلام، الان سر کار هستم؛ بعداً با شما تماس می‌گیرم.\n\nدستیار هوش مصنوعی"

            val manager = if (android.os.Build.VERSION.SDK_INT >= 31) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = manager.divideMessage(message)

            if (parts.size > 1) {
                manager.sendMultipartTextMessage(number, null, parts, null, null)
            } else {
                manager.sendTextMessage(number, null, message, null, null)
            }

            prefs.edit().putLong("last_call_$key", now).apply()
        } catch (_: Exception) {
            // SMS sending failed.
        }
    }

    companion object {
        private val PHONE_PACKAGES = setOf(
            "com.samsung.android.dialer",
            "com.samsung.android.incallui",
            "com.google.android.dialer",
            "com.android.dialer",
            "com.android.phone"
        )

        private val MISSED_WORDS = listOf(
            "missed call",
            "missed calls",
            "تماس از دست رفته",
            "تماس ازدست‌رفته",
            "تماس بی‌پاسخ",
            "تماس پاسخ داده نشده"
        )
    }
}
