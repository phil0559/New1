package com.example.new1

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private var languagePopup: PopupWindow? = null
    private lateinit var flagIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val flagContainer: View = findViewById(R.id.flag_container)
        flagContainer.setOnClickListener { toggleLanguagePopup(it) }

        flagIcon = findViewById(R.id.selected_flag_icon)
        updateFlagIconForCurrentLocale()

        val establishmentButton: View = findViewById(R.id.button_establishment)
        establishmentButton.setOnClickListener {
            startActivity(Intent(this, EstablishmentActivity::class.java))
        }
    }

    override fun onDestroy() {
        languagePopup?.dismiss()
        languagePopup = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        updateFlagIconForCurrentLocale()
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

        contentView.findViewById<ImageView>(R.id.flag_fr).setOnClickListener {
            applyLanguageSelection("fr")
        }
        contentView.findViewById<ImageView>(R.id.flag_en).setOnClickListener {
            applyLanguageSelection("en-GB")
        }
        contentView.findViewById<ImageView>(R.id.flag_de).setOnClickListener {
            applyLanguageSelection("de")
        }
        contentView.findViewById<ImageView>(R.id.flag_es).setOnClickListener {
            applyLanguageSelection("es")
        }

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = contentView.measuredWidth
        val anchorWidth = anchor.width
        val xOffset = anchorWidth - popupWidth
        val yOffset = (8 * resources.displayMetrics.density).roundToInt()

        popupWindow.showAsDropDown(anchor, xOffset, yOffset)
        languagePopup = popupWindow
    }

    private fun applyLanguageSelection(languageTag: String) {
        val locales = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(locales)
        flagIcon.setImageResource(flagIconForTag(languageTag))
        languagePopup?.dismiss()
    }

    private fun updateFlagIconForCurrentLocale() {
        val locales = AppCompatDelegate.getApplicationLocales()
        val primaryTag = if (!locales.isEmpty) {
            locales[0]?.toLanguageTag()
        } else {
            resources.configuration.locales[0]?.toLanguageTag()
        }
        val languageTag = primaryTag ?: Locale.getDefault().toLanguageTag()
        flagIcon.setImageResource(flagIconForTag(languageTag))
    }

    private fun flagIconForTag(languageTag: String): Int {
        val normalized = languageTag.lowercase(Locale.ROOT)
        return when {
            normalized.startsWith("en") -> R.drawable.ic_flag_united_kingdom
            normalized.startsWith("de") -> R.drawable.ic_flag_germany
            normalized.startsWith("es") -> R.drawable.ic_flag_spain
            else -> R.drawable.ic_flag_france
        }
    }
}
