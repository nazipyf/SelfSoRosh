package com.selfsorosh.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val status = findViewById<TextView>(R.id.status)
        val start = findViewById<Button>(R.id.start)
        val stop = findViewById<Button>(R.id.stop)
        val link = findViewById<EditText>(R.id.link)
        val message = findViewById<EditText>(R.id.message)
        val seconds = findViewById<EditText>(R.id.seconds)
        val creator = findViewById<TextView>(R.id.creator)

        start.setOnClickListener {
            val destination = link.text.toString().trim()
            val text = message.text.toString().trim()
            val interval = seconds.text.toString().toLongOrNull()

            when {
                destination.isEmpty() -> {
                    link.error = "لینک مقصد را وارد کن"
                }
                text.isEmpty() -> {
                    message.error = "متن پیام را وارد کن"
                }
                interval == null || interval < 1 -> {
                    seconds.error = "فاصله زمانی باید حداقل ۱ ثانیه باشد"
                }
                else -> {
                    val intent = Intent(this, SendService::class.java).apply {
                        putExtra("destination", destination)
                        putExtra("message", text)
                        putExtra("interval_ms", interval * 1000L)
                    }

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
                        status.text = "● سرویس فعال شد؛ ارسال هنوز متصل نیست"
                        status.setTextColor(getColor(R.color.green))
                    } catch (e: Exception) {
                        status.text = "● شروع سرویس ناموفق بود"
                        status.setTextColor(getColor(R.color.red))
                        Toast.makeText(this, e.message ?: "خطا در شروع سرویس", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        stop.setOnClickListener {
            stopService(Intent(this, SendService::class.java))
            status.text = "● متوقف"
            status.setTextColor(getColor(R.color.red))
        }

        creator.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://splus.ir/Yaldaw_i"))
            startActivity(intent)
        }
    }
}
