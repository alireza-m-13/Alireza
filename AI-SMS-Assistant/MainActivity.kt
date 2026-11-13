package com.alireza.aismsassistant

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val required = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS
    )
    private val prefs by lazy { getSharedPreferences("assistant", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 32)
        }
        layout.addView(TextView(this).apply {
            text = "دستیار هوشمند پیامک"
            textSize = 24f
        })
        layout.addView(TextView(this).apply {
            text = "فقط به شماره‌های ذخیره‌شده در مخاطبین پاسخ می‌دهد. شماره‌های ناشناس و تبلیغاتی نادیده گرفته می‌شوند. برای جلوگیری از ارسال مکرر، به هر مخاطب حداکثر هر ۲۴ ساعت یک پاسخ می‌رود."
            textSize = 16f
            setPadding(0, 24, 0, 24)
        })
        val status = TextView(this).apply { textSize = 18f }
        fun refreshStatus() {
            status.text = if (prefs.getBoolean("enabled", false)) "وضعیت: فعال" else "وضعیت: خاموش"
        }
        layout.addView(status)
        layout.addView(Button(this).apply {
            text = "دادن مجوزهای لازم"
            setOnClickListener {
                val missing = required.filter {
                    checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
                }
                if (missing.isEmpty()) {
                    Toast.makeText(this@MainActivity, "مجوزها قبلاً داده شده‌اند", Toast.LENGTH_SHORT).show()
                } else requestPermissions(missing.toTypedArray(), 10)
            }
        })
        layout.addView(Button(this).apply {
            text = "روشن / خاموش کردن پاسخ خودکار"
            setOnClickListener {
                val granted = required.all {
                    checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
                }
                if (!granted) {
                    Toast.makeText(this@MainActivity, "ابتدا مجوزهای لازم را بده", Toast.LENGTH_LONG).show()
                } else {
                    prefs.edit().putBoolean("enabled", !prefs.getBoolean("enabled", false)).apply()
                    refreshStatus()
                }
            }
        })
        layout.addView(TextView(this).apply {
    text = "\nمتن پیام خودکار:\nسلام، الان سر کار هستم؛ بعداً با شما تماس می‌گیرم.\n\nدستیار هوش مصنوعی"
    textSize = 18f
})
        
        setContentView(layout)
        refreshStatus()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            val allGranted = required.all {
                checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
            Toast.makeText(
                this,
                if (allGranted) "مجوزها تأیید شدند" else "برای کارکرد برنامه همه مجوزها لازم‌اند",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
