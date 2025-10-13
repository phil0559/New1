package com.example.new1

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EstablishmentActivity : AppCompatActivity() {

    private val establishments = mutableListOf<Establishment>()
    private lateinit var establishmentAdapter: EstablishmentAdapter
    private lateinit var establishmentList: RecyclerView
    private lateinit var emptyPlaceholder: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_establishment)

        val backButton: ImageView = findViewById(R.id.button_back)
        backButton.setOnClickListener { finish() }

        establishmentList = findViewById(R.id.list_establishments)
        emptyPlaceholder = findViewById(R.id.text_placeholder)

        establishmentAdapter = EstablishmentAdapter(establishments)
        establishmentList.layoutManager = LinearLayoutManager(this)
        establishmentList.adapter = establishmentAdapter

        findViewById<FloatingActionButton>(R.id.fab_add_establishment).setOnClickListener {
            showAddEstablishmentDialog()
        }

        updateEmptyState()
    }

    private fun showAddEstablishmentDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_establishment, null)
        val nameInputLayout: TextInputLayout = dialogView.findViewById(R.id.input_layout_name)
        val nameInput: TextInputEditText = dialogView.findViewById(R.id.input_establishment_name)
        val commentInput: TextInputEditText = dialogView.findViewById(R.id.input_establishment_comment)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_confirm, null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = nameInput.text?.toString()?.trim().orEmpty()
                val comment = commentInput.text?.toString()?.trim().orEmpty()

                if (name.isEmpty()) {
                    nameInputLayout.error = getString(R.string.error_establishment_name_required)
                } else {
                    nameInputLayout.error = null
                    establishments.add(Establishment(name, comment))
                    establishmentAdapter.notifyItemInserted(establishments.lastIndex)
                    updateEmptyState()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun updateEmptyState() {
        emptyPlaceholder.isVisible = establishments.isEmpty()
        establishmentList.isVisible = establishments.isNotEmpty()
    }
}

