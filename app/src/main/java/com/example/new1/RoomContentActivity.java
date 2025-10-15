package com.example.new1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RoomContentActivity extends Activity {
    private static final String PREFS_NAME = "room_content_prefs";
    private static final String KEY_ROOM_CONTENT_PREFIX = "room_content_";
    private static final int REQUEST_TAKE_PHOTO = 2001;

    public static final String EXTRA_ESTABLISHMENT_NAME = "extra_establishment_name";
    public static final String EXTRA_ROOM_NAME = "extra_room_name";

    private static class FormState {
        @Nullable
        TextView photoLabel;
        @Nullable
        LinearLayout photoContainer;
        @Nullable
        View addPhotoButton;
        final List<String> photos = new ArrayList<>();
    }

    @Nullable
    private String establishmentName;
    @Nullable
    private String roomName;

    private final List<RoomContentItem> roomContentItems = new ArrayList<>();
    @Nullable
    private RecyclerView contentList;
    @Nullable
    private TextView placeholderView;
    @Nullable
    private RoomContentAdapter roomContentAdapter;
    @Nullable
    private FormState currentFormState;

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

        placeholderView = findViewById(R.id.text_room_placeholder);
        if (placeholderView != null) {
            placeholderView.setText(R.string.room_content_empty_state);
        }

        contentList = findViewById(R.id.list_room_content);
        if (contentList != null) {
            contentList.setLayoutManager(new LinearLayoutManager(this));
            roomContentAdapter = new RoomContentAdapter(this, roomContentItems,
                    new RoomContentAdapter.OnRoomContentInteractionListener() {
                        @Override
                        public void onEditRoomContent(@NonNull RoomContentItem item, int position) {
                            showEditRoomContentDialog(item, position);
                        }

                        @Override
                        public void onDeleteRoomContent(@NonNull RoomContentItem item, int position) {
                            showDeleteRoomContentConfirmation(item, position);
                        }
                    });
            contentList.setAdapter(roomContentAdapter);
        }

        loadRoomContent();
        updateEmptyState();

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

    private void showRoomContentDialog(@Nullable RoomContentItem itemToEdit, int positionToEdit) {
        final boolean isEditing = itemToEdit != null
                && positionToEdit >= 0
                && positionToEdit < roomContentItems.size();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_content_add, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        EditText nameInput = dialogView.findViewById(R.id.input_room_content_name);
        EditText commentInput = dialogView.findViewById(R.id.input_room_content_comment);
        TextView barcodeValueView = dialogView.findViewById(R.id.text_barcode_value);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button addPhotoButton = dialogView.findViewById(R.id.button_add_room_content_photo);
        LinearLayout photoContainer = dialogView.findViewById(R.id.container_room_content_photos);
        Button barcodeButton = dialogView.findViewById(R.id.button_barcode);
        Button addTrackButton = dialogView.findViewById(R.id.button_add_track);
        Button addTrackListButton = dialogView.findViewById(R.id.button_add_track_list);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category);
        Button selectTypeButton = dialogView.findViewById(R.id.button_select_type);
        ImageButton openTypeListButton = dialogView.findViewById(R.id.button_open_type_list);
        ImageButton editCustomTypeButton = dialogView.findViewById(R.id.button_edit_custom_type);
        ImageButton deleteCustomTypeButton = dialogView.findViewById(R.id.button_delete_custom_type);
        Button selectCategoryButton = dialogView.findViewById(R.id.button_select_category);
        ImageButton openCategoryListButton = dialogView.findViewById(R.id.button_open_category_list);
        ImageButton editCustomCategoryButton = dialogView.findViewById(R.id.button_edit_custom_category);
        ImageButton deleteCustomCategoryButton = dialogView.findViewById(R.id.button_delete_custom_category);
        View bookFields = dialogView.findViewById(R.id.container_book_fields);
        View trackFields = dialogView.findViewById(R.id.container_track_fields);
        TextView trackTitle = dialogView.findViewById(R.id.text_track_title);
        TextView dialogTitle = dialogView.findViewById(R.id.text_dialog_room_content_title);

        if (dialogTitle != null) {
            dialogTitle.setText(isEditing
                    ? R.string.dialog_edit_room_content_title
                    : R.string.dialog_add_room_content_title);
        }

        if (barcodeValueView != null) {
            if (isEditing && itemToEdit != null) {
                String barcode = itemToEdit.getBarcode();
                if (barcode != null && !barcode.trim().isEmpty()) {
                    barcodeValueView.setText(barcode);
                } else {
                    barcodeValueView.setText(R.string.dialog_label_barcode_placeholder);
                }
            } else {
                barcodeValueView.setText(R.string.dialog_label_barcode_placeholder);
            }
        }

        if (isEditing && itemToEdit != null) {
            if (nameInput != null) {
                String name = itemToEdit.getName();
                nameInput.setText(name);
                if (name != null) {
                    nameInput.setSelection(name.length());
                }
            }
            if (commentInput != null) {
                commentInput.setText(itemToEdit.getComment());
            }
        }

        final FormState formState = new FormState();
        formState.photoLabel = dialogView.findViewById(R.id.text_room_content_photos_label);
        formState.photoContainer = photoContainer;
        formState.addPhotoButton = addPhotoButton;
        currentFormState = formState;
        if (isEditing && itemToEdit != null) {
            formState.photos.addAll(itemToEdit.getPhotos());
        }

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        List<String> typeOptions = new ArrayList<>(Arrays.asList(
                getResources().getStringArray(R.array.room_content_type_options)));
        if (isEditing && itemToEdit != null) {
            String existingType = itemToEdit.getType();
            if (existingType != null && !existingType.trim().isEmpty()
                    && !typeOptions.contains(existingType)) {
                typeOptions.add(existingType);
            }
        }
        final String[] selectedTypeHolder = new String[1];
        if (isEditing && itemToEdit != null) {
            String existingType = itemToEdit.getType();
            selectedTypeHolder[0] = existingType != null && !existingType.trim().isEmpty()
                    ? existingType
                    : (!typeOptions.isEmpty() ? typeOptions.get(0) : null);
        } else {
            selectedTypeHolder[0] = !typeOptions.isEmpty() ? typeOptions.get(0) : null;
        }

        List<String> categoryOptions = new ArrayList<>(Arrays.asList(
                getResources().getStringArray(R.array.room_content_category_options)));
        if (isEditing && itemToEdit != null) {
            String existingCategory = itemToEdit.getCategory();
            if (existingCategory != null && !existingCategory.trim().isEmpty()
                    && !categoryOptions.contains(existingCategory)) {
                categoryOptions.add(existingCategory);
            }
        }
        final String[] selectedCategoryHolder = new String[1];
        if (isEditing && itemToEdit != null) {
            String existingCategory = itemToEdit.getCategory();
            selectedCategoryHolder[0] = existingCategory != null && !existingCategory.trim().isEmpty()
                    ? existingCategory
                    : null;
        } else {
            selectedCategoryHolder[0] = null;
        }

        final ArrayAdapter<String>[] categoryAdapterHolder = new ArrayAdapter[]{null};

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                String trimmedName = "";
                if (nameInput != null) {
                    CharSequence nameValue = nameInput.getText();
                    trimmedName = nameValue != null ? nameValue.toString().trim() : "";
                    if (trimmedName.isEmpty()) {
                        nameInput.setError(getString(R.string.error_room_content_name_required));
                        nameInput.requestFocus();
                        return;
                    }
                }
                String trimmedComment = "";
                if (commentInput != null) {
                    CharSequence commentValue = commentInput.getText();
                    trimmedComment = commentValue != null ? commentValue.toString().trim() : "";
                }
                String barcodeValue = "";
                if (barcodeValueView != null) {
                    CharSequence barcodeText = barcodeValueView.getText();
                    String trimmedBarcode = barcodeText != null ? barcodeText.toString().trim() : "";
                    if (!trimmedBarcode.isEmpty()
                            && !trimmedBarcode.equals(getString(R.string.dialog_label_barcode_placeholder))) {
                        barcodeValue = trimmedBarcode;
                    }
                }

                if (currentFormState != null && currentFormState != formState) {
                    dialog.dismiss();
                    return;
                }

                RoomContentItem item = new RoomContentItem(trimmedName,
                        trimmedComment,
                        selectedTypeHolder[0],
                        selectedCategoryHolder[0],
                        barcodeValue,
                        new ArrayList<>(formState.photos));
                if (isEditing) {
                    if (positionToEdit < 0 || positionToEdit >= roomContentItems.size()) {
                        dialog.dismiss();
                        return;
                    }
                    roomContentItems.remove(positionToEdit);
                }
                roomContentItems.add(item);
                sortRoomContentItems();
                if (roomContentAdapter != null) {
                    roomContentAdapter.notifyDataSetChanged();
                }
                if (contentList != null) {
                    int targetPosition = roomContentItems.indexOf(item);
                    if (targetPosition >= 0) {
                        final int scrollPosition = targetPosition;
                        contentList.post(() -> contentList.smoothScrollToPosition(scrollPosition));
                    }
                }
                saveRoomContent();
                updateEmptyState();
                int messageRes = isEditing
                        ? R.string.room_content_updated_confirmation
                        : R.string.room_content_added_confirmation;
                Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.setOnDismissListener(d -> {
            if (currentFormState == formState) {
                currentFormState = null;
            }
        });

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

        refreshPhotoSection(formState);

        if (addPhotoButton != null) {
            addPhotoButton.setOnClickListener(v -> {
                if (currentFormState == null || currentFormState != formState) {
                    return;
                }
                if (formState.photos.size() >= 5) {
                    Toast.makeText(this, R.string.dialog_error_max_photos_reached, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                }
            });
        }

        View.OnClickListener comingSoonListener = v ->
                Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show();

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
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item,
                    categoryOptions);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);
            if (!categoryOptions.isEmpty() && selectedCategoryHolder[0] != null) {
                int selectedIndex = categoryOptions.indexOf(selectedCategoryHolder[0]);
                if (selectedIndex >= 0) {
                    categorySpinner.setSelection(selectedIndex);
                }
            }
            categoryAdapterHolder[0] = categoryAdapter;
        }

        updateTypeSpecificFields(bookFields, trackFields, trackTitle, selectedTypeHolder[0]);
        updateSelectionButtonText(selectTypeButton, selectedTypeHolder[0],
                R.string.dialog_button_choose_type);
        updateSelectionButtonText(selectCategoryButton, selectedCategoryHolder[0],
                R.string.dialog_button_choose_category);

        View.OnClickListener typeDialogLauncher = v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View sheetView = getLayoutInflater().inflate(R.layout.dialog_type_selector, null);
            bottomSheetDialog.setContentView(sheetView);

            RecyclerView recyclerView = sheetView.findViewById(R.id.recycler_type_options);
            Button addTypeButtonSheet = sheetView.findViewById(R.id.button_add_type);
            final TypeSelectorAdapter[] adapterHolder = new TypeSelectorAdapter[1];

            if (recyclerView != null) {
                TypeSelectorAdapter adapter = new TypeSelectorAdapter(typeOptions,
                        new TypeSelectorAdapter.TypeActionListener() {
                            @Override
                            public void onTypeSelected(String type) {
                                selectedTypeHolder[0] = type;
                                updateTypeSpecificFields(bookFields, trackFields, trackTitle, type);
                                updateSelectionButtonText(selectTypeButton, type,
                                        R.string.dialog_button_choose_type);
                                bottomSheetDialog.dismiss();
                            }

                            @Override
                            public void onEditType(String type, int position) {
                                showEditTypeDialog(typeOptions,
                                        adapterHolder[0],
                                        position,
                                        type,
                                        selectTypeButton,
                                        selectedTypeHolder,
                                        bookFields,
                                        trackFields,
                                        trackTitle);
                            }

                            @Override
                            public void onDeleteType(String type, int position) {
                                showDeleteTypeConfirmation(typeOptions,
                                        adapterHolder[0],
                                        position,
                                        selectTypeButton,
                                        selectedTypeHolder,
                                        bookFields,
                                        trackFields,
                                        trackTitle);
                            }
                        });
                adapter.setSelectedType(selectedTypeHolder[0]);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(adapter);
                adapterHolder[0] = adapter;
            }

            if (addTypeButtonSheet != null) {
                addTypeButtonSheet.setOnClickListener(view ->
                        showAddTypeDialog(typeOptions, adapterHolder[0], recyclerView));
            }

            bottomSheetDialog.show();
        };

        if (selectTypeButton != null) {
            selectTypeButton.setOnClickListener(typeDialogLauncher);
        }

        if (openTypeListButton != null) {
            openTypeListButton.setOnClickListener(typeDialogLauncher);
        }

        if (editCustomTypeButton != null) {
            editCustomTypeButton.setOnClickListener(comingSoonListener);
        }

        if (deleteCustomTypeButton != null) {
            deleteCustomTypeButton.setOnClickListener(comingSoonListener);
        }

        View.OnClickListener categoryDialogLauncher = v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View sheetView = getLayoutInflater().inflate(R.layout.dialog_category_selector, null);
            bottomSheetDialog.setContentView(sheetView);

            RecyclerView recyclerView = sheetView.findViewById(R.id.recycler_category_options);
            Button addCategoryButtonSheet = sheetView.findViewById(R.id.button_add_category);
            final CategorySelectorAdapter[] adapterHolder = new CategorySelectorAdapter[1];

            if (recyclerView != null) {
                CategorySelectorAdapter adapter = new CategorySelectorAdapter(categoryOptions,
                        new CategorySelectorAdapter.CategoryActionListener() {
                            @Override
                            public void onCategorySelected(String category) {
                                selectedCategoryHolder[0] = category;
                                updateSelectionButtonText(selectCategoryButton, category,
                                        R.string.dialog_button_choose_category);
                                if (categorySpinner != null) {
                                    int index = categoryOptions.indexOf(category);
                                    if (index >= 0) {
                                        categorySpinner.setSelection(index);
                                    }
                                }
                                bottomSheetDialog.dismiss();
                            }

                            @Override
                            public void onEditCategory(String category, int position) {
                                showEditCategoryDialog(categoryOptions,
                                        adapterHolder[0],
                                        categoryAdapterHolder[0],
                                        position,
                                        category,
                                        selectCategoryButton,
                                        selectedCategoryHolder,
                                        categorySpinner);
                            }

                            @Override
                            public void onDeleteCategory(String category, int position) {
                                showDeleteCategoryConfirmation(categoryOptions,
                                        adapterHolder[0],
                                        categoryAdapterHolder[0],
                                        position,
                                        selectCategoryButton,
                                        selectedCategoryHolder,
                                        categorySpinner);
                            }
                        });
                adapter.setSelectedCategory(selectedCategoryHolder[0]);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(adapter);
                adapterHolder[0] = adapter;
            }

            if (addCategoryButtonSheet != null) {
                addCategoryButtonSheet.setOnClickListener(view ->
                        showAddCategoryDialog(categoryOptions,
                                adapterHolder[0],
                                recyclerView,
                                categoryAdapterHolder[0]));
            }

            bottomSheetDialog.show();
        };

        if (selectCategoryButton != null) {
            selectCategoryButton.setOnClickListener(categoryDialogLauncher);
        }

        if (openCategoryListButton != null) {
            openCategoryListButton.setOnClickListener(categoryDialogLauncher);
        }

        if (editCustomCategoryButton != null) {
            editCustomCategoryButton.setOnClickListener(comingSoonListener);
        }

        if (deleteCustomCategoryButton != null) {
            deleteCustomCategoryButton.setOnClickListener(comingSoonListener);
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void showAddRoomContentDialog() {
        showRoomContentDialog(null, -1);
    }

    private void showEditRoomContentDialog(@NonNull RoomContentItem item, int position) {
        showRoomContentDialog(item, position);
    }

    private void showDeleteRoomContentConfirmation(@NonNull RoomContentItem item, int position) {
        String name = item.getName();
        if (name == null || name.trim().isEmpty()) {
            name = getString(R.string.dialog_room_content_item_placeholder);
        }
        final int targetPosition = position;
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_room_content_title)
                .setMessage(getString(R.string.dialog_delete_room_content_message, name))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) ->
                        deleteRoomContent(targetPosition))
                .show();
    }

    private void deleteRoomContent(int position) {
        if (position < 0 || position >= roomContentItems.size()) {
            return;
        }
        roomContentItems.remove(position);
        sortRoomContentItems();
        if (roomContentAdapter != null) {
            roomContentAdapter.notifyDataSetChanged();
        }
        saveRoomContent();
        updateEmptyState();
        Toast.makeText(this, R.string.room_content_deleted_confirmation, Toast.LENGTH_SHORT).show();
    }

    private void loadRoomContent() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String storedValue = preferences.getString(buildRoomContentKey(), null);
        roomContentItems.clear();
        if (storedValue == null || storedValue.trim().isEmpty()) {
            if (roomContentAdapter != null) {
                roomContentAdapter.notifyDataSetChanged();
            }
            return;
        }
        try {
            JSONArray array = new JSONArray(storedValue);
            for (int i = 0; i < array.length(); i++) {
                JSONObject itemObject = array.getJSONObject(i);
                RoomContentItem item = RoomContentItem.fromJson(itemObject);
                roomContentItems.add(item);
            }
        } catch (JSONException e) {
            roomContentItems.clear();
            preferences.edit().remove(buildRoomContentKey()).apply();
        }
        sortRoomContentItems();
        if (roomContentAdapter != null) {
            roomContentAdapter.notifyDataSetChanged();
        }
    }

    private void saveRoomContent() {
        JSONArray array = new JSONArray();
        for (RoomContentItem item : roomContentItems) {
            array.put(item.toJson());
        }
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit()
                .putString(buildRoomContentKey(), array.toString())
                .apply();
    }

    private void sortRoomContentItems() {
        if (roomContentItems.size() <= 1) {
            return;
        }
        Collections.sort(roomContentItems, new Comparator<RoomContentItem>() {
            @Override
            public int compare(RoomContentItem first, RoomContentItem second) {
                String firstType = normalizeForSort(first.getType());
                String secondType = normalizeForSort(second.getType());
                boolean firstHasType = !firstType.isEmpty();
                boolean secondHasType = !secondType.isEmpty();
                if (firstHasType && secondHasType) {
                    int typeComparison = firstType.compareToIgnoreCase(secondType);
                    if (typeComparison != 0) {
                        return typeComparison;
                    }
                } else if (firstHasType) {
                    return -1;
                } else if (secondHasType) {
                    return 1;
                }
                String firstName = normalizeForSort(first.getName());
                String secondName = normalizeForSort(second.getName());
                return firstName.compareToIgnoreCase(secondName);
            }
        });
    }

    @NonNull
    private String normalizeForSort(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    private void updateEmptyState() {
        boolean isEmpty = roomContentItems.isEmpty();
        if (placeholderView != null) {
            placeholderView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (contentList != null) {
            contentList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void refreshPhotoSection(@NonNull FormState formState) {
        if (formState.photoLabel != null) {
            formState.photoLabel.setText(getString(R.string.dialog_label_room_content_photos_template,
                    formState.photos.size()));
        }

        if (formState.addPhotoButton != null) {
            formState.addPhotoButton.setEnabled(formState.photos.size() < 5);
        }

        if (formState.photoContainer == null) {
            return;
        }

        formState.photoContainer.removeAllViews();
        if (formState.photos.isEmpty()) {
            formState.photoContainer.setVisibility(View.GONE);
            return;
        }

        formState.photoContainer.setVisibility(View.VISIBLE);
        int size = getResources().getDimensionPixelSize(R.dimen.dialog_photo_thumbnail_size);
        int margin = getResources().getDimensionPixelSize(R.dimen.dialog_photo_thumbnail_spacing);

        for (int i = 0; i < formState.photos.size(); i++) {
            String photoData = formState.photos.get(i);
            Bitmap bitmap = decodePhoto(photoData);
            if (bitmap == null) {
                continue;
            }
            ImageView thumbnail = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            if (i > 0) {
                params.setMarginStart(margin);
            }
            thumbnail.setLayoutParams(params);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbnail.setImageBitmap(bitmap);
            final int index = i;
            thumbnail.setOnClickListener(view -> showPhotoPreview(formState, index));
            formState.photoContainer.addView(thumbnail);
        }
    }

    private void showPhotoPreview(@NonNull FormState formState, int startIndex) {
        if (formState.photos.isEmpty()) {
            return;
        }

        View previewView = getLayoutInflater().inflate(R.layout.dialog_photo_preview, null);
        ImageView previewImage = previewView.findViewById(R.id.image_photo_preview);
        ImageButton previousButton = previewView.findViewById(R.id.button_previous_photo);
        ImageButton nextButton = previewView.findViewById(R.id.button_next_photo);
        ImageButton deleteButton = previewView.findViewById(R.id.button_delete_photo);

        AlertDialog previewDialog = new AlertDialog.Builder(this)
                .setView(previewView)
                .create();

        final int[] currentIndex = {Math.max(0, Math.min(startIndex, formState.photos.size() - 1))};

        Runnable updateImage = new Runnable() {
            @Override
            public void run() {
                if (formState.photos.isEmpty()) {
                    previewDialog.dismiss();
                    refreshPhotoSection(formState);
                    return;
                }
                Bitmap bitmap = decodePhoto(formState.photos.get(currentIndex[0]));
                if (bitmap != null) {
                    previewImage.setImageBitmap(bitmap);
                }
                boolean hasMultiple = formState.photos.size() > 1;
                previousButton.setEnabled(hasMultiple);
                nextButton.setEnabled(hasMultiple);
            }
        };

        previousButton.setOnClickListener(view -> {
            if (formState.photos.size() <= 1) {
                return;
            }
            currentIndex[0] = (currentIndex[0] - 1 + formState.photos.size()) % formState.photos.size();
            updateImage.run();
        });

        nextButton.setOnClickListener(view -> {
            if (formState.photos.size() <= 1) {
                return;
            }
            currentIndex[0] = (currentIndex[0] + 1) % formState.photos.size();
            updateImage.run();
        });

        deleteButton.setOnClickListener(view -> {
            if (formState.photos.isEmpty()) {
                return;
            }
            formState.photos.remove(currentIndex[0]);
            if (formState.photos.isEmpty()) {
                previewDialog.dismiss();
            } else {
                currentIndex[0] = currentIndex[0] % formState.photos.size();
                updateImage.run();
            }
            refreshPhotoSection(formState);
        });

        previewDialog.setOnDismissListener(dialog -> {
            if (!isFinishing()) {
                refreshPhotoSection(formState);
            }
        });

        updateImage.run();
        previewDialog.show();
    }

    @Nullable
    private Bitmap decodePhoto(@NonNull String data) {
        try {
            byte[] decoded = Base64.decode(data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @NonNull
    private String encodePhoto(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        byte[] bytes = outputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_TAKE_PHOTO || resultCode != RESULT_OK || currentFormState == null) {
            return;
        }

        if (data == null || data.getExtras() == null) {
            return;
        }

        Object value = data.getExtras().get("data");
        if (!(value instanceof Bitmap)) {
            return;
        }

        Bitmap bitmap = (Bitmap) value;
        if (bitmap == null) {
            return;
        }

        if (currentFormState.photos.size() >= 5) {
            Toast.makeText(this, R.string.dialog_error_max_photos_reached, Toast.LENGTH_SHORT).show();
            return;
        }

        currentFormState.photos.add(encodePhoto(bitmap));
        refreshPhotoSection(currentFormState);
    }

    private String buildRoomContentKey() {
        return KEY_ROOM_CONTENT_PREFIX
                + sanitizeForKey(establishmentName)
                + "_"
                + sanitizeForKey(roomName);
    }

    private String sanitizeForKey(@Nullable String value) {
        if (value == null) {
            return "default";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "default";
        }
        return trimmed.replaceAll("[^A-Za-z0-9]", "_");
    }

    private void showEditCategoryDialog(List<String> categoryOptions,
                                        @Nullable CategorySelectorAdapter adapter,
                                        @Nullable ArrayAdapter<String> spinnerAdapter,
                                        int position,
                                        @NonNull String currentLabel,
                                        @Nullable Button selectCategoryButton,
                                        @NonNull String[] selectedCategoryHolder,
                                        @Nullable Spinner categorySpinner) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        TextView titleView = dialogView.findViewById(R.id.text_add_category_title);
        EditText categoryNameInput = dialogView.findViewById(R.id.input_new_category_name);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);

        if (titleView != null) {
            titleView.setText(R.string.dialog_edit_category_title);
        }

        if (categoryNameInput != null) {
            categoryNameInput.setText(currentLabel);
            categoryNameInput.setSelection(currentLabel.length());
        }

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                if (categoryNameInput == null) {
                    dialog.dismiss();
                    return;
                }
                CharSequence nameValue = categoryNameInput.getText();
                String trimmedName = nameValue != null ? nameValue.toString().trim() : "";
                if (trimmedName.isEmpty()) {
                    categoryNameInput.setError(getString(R.string.error_category_name_required));
                    categoryNameInput.requestFocus();
                    return;
                }
                categoryOptions.set(position, trimmedName);
                if (adapter != null) {
                    adapter.updateCategory(position, trimmedName);
                }
                if (spinnerAdapter != null) {
                    spinnerAdapter.notifyDataSetChanged();
                }
                if (selectedCategoryHolder[0] != null
                        && selectedCategoryHolder[0].equals(currentLabel)) {
                    selectedCategoryHolder[0] = trimmedName;
                    if (adapter != null) {
                        adapter.setSelectedCategory(trimmedName);
                    }
                    updateSelectionButtonText(selectCategoryButton, trimmedName,
                            R.string.dialog_button_choose_category);
                    if (categorySpinner != null) {
                        int index = categoryOptions.indexOf(trimmedName);
                        if (index >= 0) {
                            categorySpinner.setSelection(index);
                        }
                    }
                }
                dialog.dismiss();
            });
        }

        if (categoryNameInput != null) {
            categoryNameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    categoryNameInput.setError(null);
                }
            });
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void showDeleteCategoryConfirmation(List<String> categoryOptions,
                                                @Nullable CategorySelectorAdapter adapter,
                                                @Nullable ArrayAdapter<String> spinnerAdapter,
                                                int position,
                                                @Nullable Button selectCategoryButton,
                                                @NonNull String[] selectedCategoryHolder,
                                                @Nullable Spinner categorySpinner) {
        if (position < 0 || position >= categoryOptions.size()) {
            return;
        }
        String categoryLabel = categoryOptions.get(position);
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.dialog_delete_category_message, categoryLabel))
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    String removed = categoryOptions.remove(position);
                    if (adapter != null) {
                        adapter.removeCategory(position);
                    }
                    if (spinnerAdapter != null) {
                        spinnerAdapter.notifyDataSetChanged();
                    }
                    boolean removedSelected = selectedCategoryHolder[0] != null
                            && selectedCategoryHolder[0].equals(removed);
                    if (removedSelected) {
                        selectedCategoryHolder[0] = null;
                        if (adapter != null) {
                            adapter.setSelectedCategory(null);
                        }
                        updateSelectionButtonText(selectCategoryButton, null,
                                R.string.dialog_button_choose_category);
                    } else if (selectedCategoryHolder[0] != null && spinnerAdapter != null
                            && categorySpinner != null) {
                        int newIndex = categoryOptions.indexOf(selectedCategoryHolder[0]);
                        if (newIndex >= 0) {
                            categorySpinner.setSelection(newIndex);
                        }
                    }
                })
                .create()
                .show();
    }

    private void showEditTypeDialog(List<String> typeOptions,
                                    @Nullable TypeSelectorAdapter adapter,
                                    int position,
                                    @NonNull String currentLabel,
                                    @Nullable Button selectTypeButton,
                                    @NonNull String[] selectedTypeHolder,
                                    @Nullable View bookFields,
                                    @Nullable View trackFields,
                                    @Nullable TextView trackTitle) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_type, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        TextView titleView = dialogView.findViewById(R.id.text_add_type_title);
        EditText typeNameInput = dialogView.findViewById(R.id.input_new_type_name);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);

        if (titleView != null) {
            titleView.setText(R.string.dialog_edit_type_title);
        }

        if (typeNameInput != null) {
            typeNameInput.setText(currentLabel);
            typeNameInput.setSelection(currentLabel.length());
        }

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                if (typeNameInput == null) {
                    dialog.dismiss();
                    return;
                }
                CharSequence nameValue = typeNameInput.getText();
                String trimmedName = nameValue != null ? nameValue.toString().trim() : "";
                if (trimmedName.isEmpty()) {
                    typeNameInput.setError(getString(R.string.error_type_name_required));
                    typeNameInput.requestFocus();
                    return;
                }
                typeOptions.set(position, trimmedName);
                if (adapter != null) {
                    adapter.updateType(position, trimmedName);
                }
                if (selectedTypeHolder[0] != null
                        && selectedTypeHolder[0].equals(currentLabel)) {
                    selectedTypeHolder[0] = trimmedName;
                    if (adapter != null) {
                        adapter.setSelectedType(trimmedName);
                    }
                    updateSelectionButtonText(selectTypeButton, trimmedName,
                            R.string.dialog_button_choose_type);
                    updateTypeSpecificFields(bookFields, trackFields, trackTitle, trimmedName);
                }
                dialog.dismiss();
            });
        }

        if (typeNameInput != null) {
            typeNameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    typeNameInput.setError(null);
                }
            });
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void showDeleteTypeConfirmation(List<String> typeOptions,
                                            @Nullable TypeSelectorAdapter adapter,
                                            int position,
                                            @Nullable Button selectTypeButton,
                                            @NonNull String[] selectedTypeHolder,
                                            @Nullable View bookFields,
                                            @Nullable View trackFields,
                                            @Nullable TextView trackTitle) {
        if (position < 0 || position >= typeOptions.size()) {
            return;
        }
        String typeLabel = typeOptions.get(position);
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.dialog_delete_type_message, typeLabel))
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    String removed = typeOptions.remove(position);
                    if (adapter != null) {
                        adapter.removeType(position);
                    }
                    if (selectedTypeHolder[0] != null && selectedTypeHolder[0].equals(removed)) {
                        selectedTypeHolder[0] = null;
                        if (adapter != null) {
                            adapter.setSelectedType(null);
                        }
                        updateSelectionButtonText(selectTypeButton, null,
                                R.string.dialog_button_choose_type);
                        updateTypeSpecificFields(bookFields, trackFields, trackTitle, null);
                    }
                })
                .create()
                .show();
    }

    private void showAddCategoryDialog(List<String> categoryOptions,
                                       @Nullable CategorySelectorAdapter adapter,
                                       @Nullable RecyclerView recyclerView,
                                       @Nullable ArrayAdapter<String> spinnerAdapter) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        EditText categoryNameInput = dialogView.findViewById(R.id.input_new_category_name);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                if (categoryNameInput == null) {
                    dialog.dismiss();
                    return;
                }
                CharSequence nameValue = categoryNameInput.getText();
                String trimmedName = nameValue != null ? nameValue.toString().trim() : "";
                if (trimmedName.isEmpty()) {
                    categoryNameInput.setError(getString(R.string.error_category_name_required));
                    categoryNameInput.requestFocus();
                    return;
                }
                categoryOptions.add(trimmedName);
                if (adapter != null) {
                    adapter.addCategory(trimmedName);
                    if (recyclerView != null) {
                        recyclerView.post(() ->
                                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1));
                    }
                }
                if (spinnerAdapter != null) {
                    spinnerAdapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            });
        }

        if (categoryNameInput != null) {
            categoryNameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    categoryNameInput.setError(null);
                }
            });
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void showAddTypeDialog(List<String> typeOptions,
                                   @Nullable TypeSelectorAdapter adapter,
                                   @Nullable RecyclerView recyclerView) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_type, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        EditText typeNameInput = dialogView.findViewById(R.id.input_new_type_name);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                if (typeNameInput == null) {
                    dialog.dismiss();
                    return;
                }
                CharSequence nameValue = typeNameInput.getText();
                String trimmedName = nameValue != null ? nameValue.toString().trim() : "";
                if (trimmedName.isEmpty()) {
                    typeNameInput.setError(getString(R.string.error_type_name_required));
                    typeNameInput.requestFocus();
                    return;
                }
                typeOptions.add(trimmedName);
                if (adapter != null) {
                    adapter.addType(trimmedName);
                    if (recyclerView != null) {
                        recyclerView.post(() ->
                                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1));
                    }
                }
                dialog.dismiss();
            });
        }

        if (typeNameInput != null) {
            typeNameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    typeNameInput.setError(null);
                }
            });
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void updateSelectionButtonText(@Nullable Button button,
                                           @Nullable Object selectedItem,
                                           int fallbackTextResId) {
        if (button == null) {
            return;
        }
        if (selectedItem != null) {
            button.setText(selectedItem.toString());
        } else {
            button.setText(fallbackTextResId);
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
