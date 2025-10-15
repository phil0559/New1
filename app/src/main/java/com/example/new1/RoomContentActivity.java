package com.example.new1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;

public class RoomContentActivity extends Activity {
    public static final String EXTRA_ESTABLISHMENT_NAME = "extra_establishment_name";
    public static final String EXTRA_ROOM_NAME = "extra_room_name";

    @Nullable
    private String establishmentName;
    @Nullable
    private String roomName;

    public static Intent createIntent(Context context, @Nullable String establishmentName, @Nullable Room room) {
        Intent intent = new Intent(context, RoomContentActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (establishmentName != null && !establishmentName.trim().isEmpty()) {
            intent.putExtra(EXTRA_ESTABLISHMENT_NAME, establishmentName);
        }
        if (room != null) {
            String name = room.getName();
            if (name != null && !name.trim().isEmpty()) {
                intent.putExtra(EXTRA_ROOM_NAME, name);
            }
        }
        return intent;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.apply(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_content);

        ImageView backButton = findViewById(R.id.button_back);
        if (backButton != null) {
            backButton.setOnClickListener(view -> finish());
        }

        ImageView searchButton = findViewById(R.id.button_search);
        if (searchButton != null) {
            searchButton.setOnClickListener(view ->
                    Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());
        }

        Intent intent = getIntent();
        establishmentName = intent != null ? intent.getStringExtra(EXTRA_ESTABLISHMENT_NAME) : null;
        roomName = intent != null ? intent.getStringExtra(EXTRA_ROOM_NAME) : null;

        applyTitle();
        applySubtitle();

        TextView placeholderView = findViewById(R.id.text_room_placeholder);
        if (placeholderView != null) {
            placeholderView.setText(R.string.room_content_empty_state);
        }

        View addButton = findViewById(R.id.button_add_room_content);
        if (addButton != null) {
            addButton.setOnClickListener(view -> showAddRoomContentDialog());
        }
    }

    private void applyTitle() {
        TextView titleView = findViewById(R.id.text_room_title);
        if (titleView == null) {
            return;
        }
        if (roomName == null || roomName.trim().isEmpty()) {
            titleView.setText(R.string.room_content_title);
        } else {
            titleView.setText(getString(R.string.room_content_title_with_name, roomName));
        }
    }

    private void applySubtitle() {
        TextView subtitleView = findViewById(R.id.text_room_subtitle);
        if (subtitleView == null) {
            return;
        }
        String trimmedEstablishment = establishmentName != null ? establishmentName.trim() : "";
        if (trimmedEstablishment.isEmpty()) {
            subtitleView.setVisibility(View.VISIBLE);
            subtitleView.setText(R.string.room_content_placeholder);
        } else {
            subtitleView.setText("");
            subtitleView.setVisibility(View.GONE);
        }
    }

    private void showAddRoomContentDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_content_add, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        EditText nameInput = dialogView.findViewById(R.id.input_room_content_name);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button addPhotoButton = dialogView.findViewById(R.id.button_add_room_content_photo);
        Button barcodeButton = dialogView.findViewById(R.id.button_barcode);
        Button addTrackButton = dialogView.findViewById(R.id.button_add_track);
        Button addTrackListButton = dialogView.findViewById(R.id.button_add_track_list);
        Spinner typeSpinner = dialogView.findViewById(R.id.spinner_type);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category);
        View bookFields = dialogView.findViewById(R.id.container_book_fields);
        View trackFields = dialogView.findViewById(R.id.container_track_fields);
        TextView trackTitle = dialogView.findViewById(R.id.text_track_title);

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                if (nameInput != null) {
                    CharSequence nameValue = nameInput.getText();
                    String trimmedName = nameValue != null ? nameValue.toString().trim() : "";
                    if (trimmedName.isEmpty()) {
                        nameInput.setError(getString(R.string.error_room_content_name_required));
                        nameInput.requestFocus();
                        return;
                    }
                }
                Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        if (nameInput != null) {
            nameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    nameInput.setError(null);
                }
            });
        }

        View.OnClickListener comingSoonListener = v ->
                Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show();

        if (addPhotoButton != null) {
            addPhotoButton.setOnClickListener(comingSoonListener);
        }

        if (barcodeButton != null) {
            barcodeButton.setOnClickListener(comingSoonListener);
        }

        if (addTrackButton != null) {
            addTrackButton.setOnClickListener(comingSoonListener);
        }

        if (addTrackListButton != null) {
            addTrackListButton.setOnClickListener(comingSoonListener);
        }

        if (categorySpinner != null) {
            ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.room_content_category_options,
                    android.R.layout.simple_spinner_item);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);
        }

        if (typeSpinner != null) {
            ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.room_content_type_options,
                    android.R.layout.simple_spinner_item);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(typeAdapter);
            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Object item = parent.getItemAtPosition(position);
                    updateTypeSpecificFields(bookFields, trackFields, trackTitle,
                            item != null ? item.toString() : null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    updateTypeSpecificFields(bookFields, trackFields, trackTitle, null);
                }
            });
            Object initialSelection = typeSpinner.getSelectedItem();
            updateTypeSpecificFields(bookFields, trackFields, trackTitle,
                    initialSelection != null ? initialSelection.toString() : null);
        } else {
            updateTypeSpecificFields(bookFields, trackFields, trackTitle, null);
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void updateTypeSpecificFields(@Nullable View bookFields,
                                          @Nullable View trackFields,
                                          @Nullable TextView trackTitle,
                                          @Nullable String selectedType) {
        String bookType = getString(R.string.dialog_type_book);
        String comicType = getString(R.string.dialog_type_comic);
        String cdType = getString(R.string.dialog_type_cd);
        String discType = getString(R.string.dialog_type_disc);

        boolean showBookFields = selectedType != null
                && (selectedType.equals(bookType) || selectedType.equals(comicType));
        boolean showTrackFields = selectedType != null
                && (selectedType.equals(cdType) || selectedType.equals(discType));

        if (bookFields != null) {
            bookFields.setVisibility(showBookFields ? View.VISIBLE : View.GONE);
        }

        if (trackFields != null) {
            if (showTrackFields) {
                trackFields.setVisibility(View.VISIBLE);
                if (trackTitle != null) {
                    int titleRes = selectedType != null && selectedType.equals(discType)
                            ? R.string.dialog_tracks_disc_title
                            : R.string.dialog_tracks_cd_title;
                    trackTitle.setText(titleRes);
                }
            } else {
                trackFields.setVisibility(View.GONE);
            }
        }
    }
}
