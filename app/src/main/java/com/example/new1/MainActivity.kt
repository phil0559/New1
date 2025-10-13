package com.example.new1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private var languagePopup: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val flagContainer: View = findViewById(R.id.flag_container)
        flagContainer.setOnClickListener { toggleLanguagePopup(it) }
    }

    override fun onDestroy() {
        languagePopup?.dismiss()
        languagePopup = null
        super.onDestroy()
    }

    private fun toggleLanguagePopup(anchor: View) {
        languagePopup?.let { popup ->
            if (popup.isShowing) {
                popup.dismiss()
                return
            }
        }

        val contentView = LayoutInflater.from(this).inflate(R.layout.popup_language_selector, null)
        val popupWindow = PopupWindow(
            contentView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.elevation = 12f
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_language_popup))
        popupWindow.setOnDismissListener { languagePopup = null }

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = contentView.measuredWidth
        val anchorWidth = anchor.width
        val xOffset = anchorWidth - popupWidth
        val yOffset = (8 * resources.displayMetrics.density).roundToInt()

        popupWindow.showAsDropDown(anchor, xOffset, yOffset)
        languagePopup = popupWindow
    }
}
