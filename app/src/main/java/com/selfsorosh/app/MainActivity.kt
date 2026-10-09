package com.selfsorosh.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val status = findViewById<TextView>(R.id.status)
        val start = findViewById<Button>(R.id.start)
        val stop = findViewById<Button>(R.id.stop)

        start.setOnClickListener {
            running = true
            status.text = "● در حال اجرا"
            status.setTextColor(getColor(R.color.green))
        }

        stop.setOnClickListener {
            running = false
            status.text = "● متوقف"
            status.setTextColor(getColor(R.color.red))
        }
    }
}
