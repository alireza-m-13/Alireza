package com.alireza.aismsassistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return
        val prefs = context.getSharedPreferences("assistant", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return

        val pending = goAsync()
        Thread {
            try {
                val extras = intent.extras ?: return@Thread
                val pdus = extras.get("pdus") as? Array<*> ?: return@Thread
                val format = extras.getString("format")
                val parts = pdus.mapNotNull { pdu ->
                    (pdu as? ByteArray)?.let {
                        if (format != null) SmsMessage.createFromPdu(it, format)
                        else @Suppress("DEPRECATION") SmsMessage.createFromPdu(it)
                    }
                }
                if (parts.isEmpty()) return@Thread
                val sender = parts.first().originatingAddress ?: return@Thread
                if (!isSavedContact(context, sender)) return@Thread

                val key = PhoneNumberUtils.normalizeNumber(sender)
                val now = System.currentTimeMillis()
                val lastSent = prefs.getLong("last_$key", 0L)
                if (now - lastSent < 24 * 60 * 60 * 1000L) return@Thread

                val reply = "سلام، الان امکان پاسخ‌گویی ندارم؛ بعداً با شما تماس می‌گیرم.\n\nدستیار هوش مصنوعی"
                SmsManager.getDefault().sendTextMessage(sender, null, reply, null, null)
                prefs.edit().putLong("last_$key", now).apply()
            } catch (_: Exception) {
                // Errors are intentionally not surfaced from the background SMS receiver.
            } finally {
                pending.finish()
            }
        }.start()
    }

    private fun isSavedContact(context: Context, number: String): Boolean {
        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        context.contentResolver.query(
            lookupUri,
            arrayOf(ContactsContract.PhoneLookup._ID),
            null,
            null,
            null
        )?.use { cursor ->
            return cursor.moveToFirst()
        }
        return false
    }
}
