package com.example.new1

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class EstablishmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_establishment)
    }

    fun onBackArrowClicked(view: View) {
        onBackPressedDispatcher.onBackPressed()
    }
}
