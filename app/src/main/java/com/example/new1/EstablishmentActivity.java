package com.example.new1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EstablishmentActivity extends Activity {
    private final List<Establishment> establishments = new ArrayList<>();
    private EstablishmentAdapter establishmentAdapter;
    private ListView establishmentList;
    private TextView emptyPlaceholder;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.apply(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establishment);

        ImageView backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(view -> finish());

        establishmentList = findViewById(R.id.list_establishments);
        emptyPlaceholder = findViewById(R.id.text_placeholder);

        establishmentAdapter = new EstablishmentAdapter(this, establishments);
        establishmentList.setAdapter(establishmentAdapter);

        Button addButton = findViewById(R.id.button_add_establishment);
        addButton.setOnClickListener(view -> showAddEstablishmentDialog());

        updateEmptyState();
    }

    private void showAddEstablishmentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_establishment, null);
        EditText nameInput = dialogView.findViewById(R.id.input_establishment_name);
        EditText commentInput = dialogView.findViewById(R.id.input_establishment_comment);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_confirm, null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
                String comment = commentInput.getText() != null ? commentInput.getText().toString().trim() : "";

                if (name.isEmpty()) {
                    nameInput.setError(getString(R.string.error_establishment_name_required));
                } else {
                    nameInput.setError(null);
                    establishments.add(new Establishment(name, comment));
                    establishmentAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void updateEmptyState() {
        if (establishments.isEmpty()) {
            emptyPlaceholder.setVisibility(View.VISIBLE);
            establishmentList.setVisibility(View.GONE);
        } else {
            emptyPlaceholder.setVisibility(View.GONE);
            establishmentList.setVisibility(View.VISIBLE);
        }
    }
}
