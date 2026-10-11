package com.selftelegram.app

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputType

class MainActivity : Activity() {
    private val bg = Color.rgb(16, 14, 20)
    private val panel = Color.rgb(33, 27, 42)
    private val purple = Color.rgb(133, 82, 199)
    private val pale = Color.rgb(215, 184, 255)
    private val mainText = Color.rgb(244, 236, 255)
    private val muted = Color.rgb(185, 173, 200)
    private val line = Color.rgb(64, 52, 77)
    private val prefs by lazy { getSharedPreferences("selftelegram", MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var ticks = 0
    private var intervalRunnable: Runnable? = null
    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private lateinit var messageInput: EditText
    private lateinit var linkInput: EditText
    private lateinit var secondsInput: EditText
    private lateinit var logView: TextView
    private lateinit var counterView: TextView
    private val templates = mutableListOf<String>()
    private val destinations = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMain()
    }

    private fun showMain() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(18), dp(18), dp(18), dp(12))
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(panel)
        }
        nav.addView(button("ارسال", true) { showMain() }, weightParams())
        nav.addView(button("مدیریت", false) { showManagement() }, weightParams())
        outer.addView(nav)
        setContentView(outer)

        root.addView(title("✦  Self Telegram", 25, pale))
        root.addView(label("پنل ارسال خودکار به گپ"))
        root.addView(spacer(12))
        root.addView(label("لینک گپ تلگرام"))
        linkInput = edit(prefs.getString("link", "") ?: "", "https://t.me/...")
        linkInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        root.addView(linkInput, matchWrap())
        root.addView(label("متن پیام"))
        messageInput = edit(prefs.getString("message", "") ?: "", "متن دلخواهت را بنویس")
        messageInput.minLines = 3
        messageInput.gravity = Gravity.TOP
        root.addView(messageInput, matchWrap())
        root.addView(label("فاصله زمانی (ثانیه)"))
        secondsInput = edit(prefs.getString("seconds", "20") ?: "20", "20")
        secondsInput.inputType = InputType.TYPE_CLASS_NUMBER
        root.addView(secondsInput, matchWrap())
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(button("▶  شروع", true) { startDemo() }, weightParams())
        actions.addView(button("■  توقف", false) { stopDemo() }, weightParams())
        root.addView(actions)
        status = label(if (running) "وضعیت: فعال (دمو؛ ارسال واقعی وصل نیست)" else "وضعیت: متوقف")
        status.setTextColor(pale)
        root.addView(status)
        counterView = label("شمارندهٔ رویدادهای آزمایشی: $ticks")
        root.addView(counterView)
        root.addView(label("توجه: این نسخه هنوز به تلگرام متصل نیست و پیام واقعی ارسال نمی‌کند."))
        root.addView(spacer(8))
        root.addView(button("ارتباط با سازنده", false) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/")))
        })
        persist()
    }

    private fun showManagement() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(18), dp(18), dp(18), dp(12))
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        val outer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8)); setBackgroundColor(panel)
        }
        nav.addView(button("ارسال", false) { showMain() }, weightParams())
        nav.addView(button("مدیریت", true) { showManagement() }, weightParams())
        outer.addView(nav)
        setContentView(outer)

        root.addView(title("مدیریت برنامه", 24, pale))
        root.addView(label("ابزارها، پیام‌ها و گزارش‌ها"))
        root.addView(spacer(10))
        root.addView(title("متن‌های ذخیره‌شده", 18, mainText))
        val templateInput = edit("", "متن جدید برای ذخیره")
        templateInput.minLines = 2
        root.addView(templateInput, matchWrap())
        root.addView(button("ذخیره متن فعلی", true) {
            val value = templateInput.text.toString().trim()
            if (value.isNotEmpty()) {
                templates.add(value)
                prefs.edit().putString("templates", templates.joinToString("\n---\n")).apply()
                Toast.makeText(this, "متن در این نشست ذخیره شد", Toast.LENGTH_SHORT).show()
                templateInput.setText("")
                addLog("متن جدید ذخیره شد")
            } else Toast.makeText(this, "اول متن را وارد کن", Toast.LENGTH_SHORT).show()
        })
        root.addView(spacer(12))
        root.addView(title("مقصدهای ذخیره‌شده", 18, mainText))
        val destInput = edit("", "لینک گپ مجاز")
        root.addView(destInput, matchWrap())
        root.addView(button("ذخیره مقصد", true) {
            val value = destInput.text.toString().trim()
            if (value.startsWith("https://t.me/") || value.startsWith("t.me/")) {
                destinations.add(value)
                prefs.edit().putString("destinations", destinations.joinToString("\n")).apply()
                addLog("مقصد ذخیره شد")
                Toast.makeText(this, "مقصد ذخیره شد", Toast.LENGTH_SHORT).show()
                destInput.setText("")
            } else Toast.makeText(this, "لینک t.me معتبر وارد کن", Toast.LENGTH_SHORT).show()
        })
        root.addView(spacer(12))
        root.addView(title("گزارش فعالیت", 18, mainText))
        logView = label(prefs.getString("logs", "هنوز گزارشی ثبت نشده") ?: "")
        root.addView(logView)
        root.addView(label("شمارندهٔ رویدادهای آزمایشی: $ticks"))
        root.addView(spacer(8))
        root.addView(button("پشتیبان‌گیری ساده از تنظیمات", false) {
            val data = "link=${prefs.getString("link","")}\nseconds=${prefs.getString("seconds","20")}\nmessage=${prefs.getString("message","")}"
            val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, data) }
            startActivity(Intent.createChooser(send, "اشتراک‌گذاری تنظیمات"))
        })
        root.addView(button("ارتباط با سازنده", false) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/")))
        })
    }

    private fun startDemo() {
        persist()
        val seconds = secondsInput.text.toString().toLongOrNull()
        if (linkInput.text.toString().isBlank() || messageInput.text.toString().isBlank()) {
            Toast.makeText(this, "لینک مقصد و متن پیام را وارد کن", Toast.LENGTH_SHORT).show()
            return
        }
        if (seconds == null || seconds < 5) {
            Toast.makeText(this, "فاصله را حداقل ۵ ثانیه بگذار", Toast.LENGTH_SHORT).show()
            return
        }
        running = true
        addLog("دموی زمان‌بندی شروع شد؛ پیام واقعی ارسال نمی‌شود")
        intervalRunnable?.let { handler.removeCallbacks(it) }
        val task = object : Runnable {
            override fun run() {
                if (!running) return
                ticks++
                if (::counterView.isInitialized) counterView.text = "شمارندهٔ رویدادهای آزمایشی: $ticks"
                addLog("تیک آزمایشی شمارهٔ $ticks")
                handler.postDelayed(this, seconds * 1000)
            }
        }
        intervalRunnable = task
        handler.postDelayed(task, seconds * 1000)
        status.text = "وضعیت: دمو فعال است؛ ارسال واقعی وصل نیست"
        Toast.makeText(this, "دموی زمان‌بندی شروع شد", Toast.LENGTH_SHORT).show()
    }

    private fun stopDemo() {
        running = false
        intervalRunnable?.let { handler.removeCallbacks(it) }
        intervalRunnable = null
        if (::status.isInitialized) status.text = "وضعیت: متوقف"
        addLog("دمو متوقف شد")
        Toast.makeText(this, "متوقف شد", Toast.LENGTH_SHORT).show()
    }

    private fun persist() {
        if (::linkInput.isInitialized && ::messageInput.isInitialized && ::secondsInput.isInitialized) {
            prefs.edit().putString("link", linkInput.text.toString())
                .putString("message", messageInput.text.toString())
                .putString("seconds", secondsInput.text.toString()).apply()
        }
    }

    private fun addLog(message: String) {
        val old = prefs.getString("logs", "") ?: ""
        val lineText = "${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())} — $message"
        val updated = (lineText + "\n" + old).lines().take(30).joinToString("\n")
        prefs.edit().putString("logs", updated).apply()
        if (::logView.isInitialized) logView.text = updated
    }

    private fun title(text: String, size: Int, color: Int): TextView =
        TextView(this).apply { this.text = text; textSize = size.toFloat(); setTextColor(color); typeface = Typeface.DEFAULT_BOLD; setPadding(0, dp(5), 0, dp(4)) }
    private fun label(text: String): TextView =
        TextView(this).apply { this.text = text; textSize = 14f; setTextColor(muted); setPadding(0, dp(9), 0, dp(5)) }
    private fun edit(value: String, hintText: String): EditText =
        EditText(this).apply {
            setText(value); hint = hintText; setTextColor(mainText); setHintTextColor(muted)
            textSize = 15f; setPadding(dp(12), dp(8), dp(12), dp(8))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(panel); cornerRadius = dp(12).toFloat(); setStroke(dp(1), line)
            }
        }
    private fun button(text: String, primary: Boolean, action: () -> Unit): Button =
        Button(this).apply {
            this.text = text; isAllCaps = false; textSize = 14f; setTextColor(mainText)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(if (primary) purple else panel); cornerRadius = dp(12).toFloat()
                setStroke(dp(1), if (primary) purple else line)
            }
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnClickListener { action() }
        }
    private fun spacer(h: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(h)) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(5) }
    private fun weightParams() = LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(3), dp(8), dp(3), dp(8)) }
}
