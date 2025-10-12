package com.example.new1

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.new1.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bonjourButton: Button = findViewById(R.id.button_bonjour)
        bonjourButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_bonjour), Toast.LENGTH_SHORT).show()
        }
    }
}
