package com.example.new1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EstablishmentActivity extends Activity {
    private static final String PREFS_NAME = "establishments_prefs";
    private static final String KEY_ESTABLISHMENTS = "establishments";

    private final List<Establishment> establishments = new ArrayList<>();
    private EstablishmentAdapter establishmentAdapter;
    private RecyclerView establishmentList;
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

        establishmentAdapter = new EstablishmentAdapter(this, establishments,
                new EstablishmentAdapter.OnEstablishmentMenuListener() {
                    @Override
                    public void onEditEstablishment(Establishment establishment, int position) {
                        showEditEstablishmentDialog(establishment, position);
                    }

                    @Override
                    public void onDeleteEstablishment(Establishment establishment, int position) {
                        showDeleteConfirmation(establishment, position);
                    }
                });
        establishmentList.setLayoutManager(new LinearLayoutManager(this));
        establishmentList.setAdapter(establishmentAdapter);

        View addButton = findViewById(R.id.button_add_establishment);
        addButton.setOnClickListener(view -> showAddEstablishmentDialog());

        loadEstablishments();
        updateEmptyState();
    }

    private void showAddEstablishmentDialog() {
        showEstablishmentFormDialog(null, -1);
    }

    private void showEditEstablishmentDialog(Establishment establishment, int position) {
        if (establishment == null || position < 0 || position >= establishments.size()) {
            return;
        }
        showEstablishmentFormDialog(establishment, position);
    }

    private void showEstablishmentFormDialog(@Nullable Establishment existingEstablishment, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_establishment, null);
        TextView titleView = dialogView.findViewById(R.id.text_dialog_title);
        EditText nameInput = dialogView.findViewById(R.id.input_establishment_name);
        EditText commentInput = dialogView.findViewById(R.id.input_establishment_comment);

        boolean isEditMode = existingEstablishment != null && position >= 0;

        if (titleView != null) {
            titleView.setText(isEditMode
                    ? getString(R.string.dialog_edit_establishment_title)
                    : getString(R.string.dialog_add_establishment_title));
        }

        if (isEditMode) {
            nameInput.setText(existingEstablishment.getName());
            nameInput.setSelection(nameInput.getText() != null ? nameInput.getText().length() : 0);
            String comment = existingEstablishment.getComment();
            commentInput.setText(comment != null ? comment : "");
        }

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
                    return;
                }

                nameInput.setError(null);

                if (isEditMode) {
                    if (position < 0 || position >= establishments.size()) {
                        dialog.dismiss();
                        return;
                    }
                    establishments.set(position, new Establishment(name, comment));
                    establishmentAdapter.notifyItemChanged(position);
                } else {
                    establishments.add(new Establishment(name, comment));
                    establishmentAdapter.notifyItemInserted(establishments.size() - 1);
                }

                saveEstablishments();
                updateEmptyState();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showDeleteConfirmation(Establishment establishment, int position) {
        if (position < 0 || position >= establishments.size()) {
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_establishment_title)
            .setMessage(getString(R.string.dialog_delete_establishment_message, establishment.getName()))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                establishments.remove(position);
                establishmentAdapter.notifyItemRemoved(position);
                saveEstablishments();
                updateEmptyState();
            })
            .show();
    }

    private void loadEstablishments() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String storedValue = preferences.getString(KEY_ESTABLISHMENTS, null);
        establishments.clear();

        if (storedValue == null || storedValue.isEmpty()) {
            establishmentAdapter.notifyDataSetChanged();
            return;
        }

        try {
            JSONArray array = new JSONArray(storedValue);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) {
                    continue;
                }

                String name = item.optString("name", "");
                if (name.isEmpty()) {
                    continue;
                }

                String comment = item.optString("comment", "");
                establishments.add(new Establishment(name, comment));
            }
        } catch (JSONException ignored) {
            // Données corrompues : on les ignore simplement.
        }

        establishmentAdapter.notifyDataSetChanged();
    }

    private void saveEstablishments() {
        JSONArray array = new JSONArray();
        for (Establishment establishment : establishments) {
            JSONObject item = new JSONObject();
            try {
                item.put("name", establishment.getName());
                item.put("comment", establishment.getComment());
                array.put(item);
            } catch (JSONException ignored) {
                // En pratique cela ne devrait pas arriver car nous n'utilisons que des chaînes.
            }
        }

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putString(KEY_ESTABLISHMENTS, array.toString()).apply();
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
