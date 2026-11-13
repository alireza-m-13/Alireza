package com.alireza.aismsassistant

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val required = arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS)
    private val prefs by lazy { getSharedPreferences("assistant", MODE_PRIVATE) }
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 32)
        }
        layout.addView(TextView(this).apply {
            text = "دستیار هوشمند تماس‌های ازدست‌رفته"
            textSize = 23f
        })
        layout.addView(TextView(this).apply {
            text = "پس از تماس بی‌پاسخ، فقط اگر شماره در مخاطبین ذخیره شده باشد، پیامک می‌فرستد. برای تشخیص تماس بی‌پاسخ باید دسترسی اعلان‌ها را فعال کنی."
            textSize = 16f
            setPadding(0, 24, 0, 24)
        })
        status = TextView(this).apply { textSize = 18f }
        layout.addView(status)
        layout.addView(Button(this).apply {
            text = "فعال‌سازی دسترسی اعلان تماس‌ها"
            setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        })
        layout.addView(Button(this).apply {
            text = "دادن مجوز پیامک و مخاطبین"
            setOnClickListener { requestMissingPermissions() }
        })
        layout.addView(Button(this).apply {
            text = "روشن / خاموش کردن دستیار"
            setOnClickListener {
                val granted = required.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
                if (!granted) {
                    Toast.makeText(this@MainActivity, "ابتدا مجوز پیامک و مخاطبین را بده", Toast.LENGTH_LONG).show()
                } else if (!hasNotificationAccess()) {
                    Toast.makeText(this@MainActivity, 
"ابتدا دسترسی اعلان تماس‌ها را فعال کن", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                } else {
                    prefs.edit().putBoolean("enabled", !prefs.getBoolean("enabled", false)).apply()
                    refreshStatus()
                }
            }
        })
        layout.addView(TextView(this).apply {
            text = "\nمتن پیام خودکار:
\nسلام، الان سر کار کاشی‌کاری هستم و امکان پاسخ‌گویی ندارم؛ بعداً با شما تماس می‌گیرم.\n\nدستیار هوش مصنوعی"
            textSize = 18f
        })
        setContentView(layout)
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) refreshStatus()
    }

    private fun requestMissingPermissions() {
        val missing = required.filter 
{ checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isEmpty()) {
            Toast.makeText(this, "مجوز پیامک و مخاطبین قبلاً داده شده است", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissions(missing.toTypedArray(), 10)
        }
    }

    private fun hasNotificationAccess(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: 
return false
        val component = ComponentName(this, MissedCallNotificationListener::class.java).flattenToString()
        return enabled.split(':').any { it.equals(component, ignoreCase = true) }
    }

    private fun refreshStatus() {
        status.text = "وضعیت دستیار: " +
            if (prefs.getBoolean("enabled", false) && hasNotificationAccess()) "فعال" else "خاموش یا نیازمند مجوز"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            val granted = required.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
            Toast.makeText(this, if (granted) "مجوزها تأیید شدند" else "هر دو مجوز لازم‌اند", Toast.LENGTH_LONG).show()
            refreshStatus()
        }
    }
}
