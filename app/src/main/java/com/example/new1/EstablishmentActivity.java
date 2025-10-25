package com.example.new1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

import java.io.ByteArrayOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EstablishmentActivity extends Activity {
    private static final String PREFS_NAME = "establishments_prefs";
    private static final String KEY_ESTABLISHMENTS = "establishments";
    private static final int REQUEST_TAKE_PHOTO = 1001;

    private final List<Establishment> establishments = new ArrayList<>();
    private EstablishmentAdapter establishmentAdapter;
    private RecyclerView establishmentList;
    private TextView emptyPlaceholder;
    private FormState currentFormState;

    private static class FormState {
        TextView photoLabel;
        LinearLayout photoContainer;
        View addPhotoButton;
        final List<String> photos = new ArrayList<>();
    }

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
                new EstablishmentAdapter.OnEstablishmentInteractionListener() {
                    @Override
                    public void onOpenEstablishment(Establishment establishment, int position) {
                        openEstablishmentContent(establishment);
                    }

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

    private void openEstablishmentContent(@Nullable Establishment establishment) {
        if (establishment == null) {
            return;
        }
        startActivity(EstablishmentContentActivity.createIntent(this, establishment));
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
        TextView photoLabel = dialogView.findViewById(R.id.text_establishment_photos_label);
        View addPhotoButton = dialogView.findViewById(R.id.button_add_photo);
        LinearLayout photoContainer = dialogView.findViewById(R.id.container_establishment_photos);

        boolean isEditMode = existingEstablishment != null && position >= 0;

        FormState formState = new FormState();
        formState.photoLabel = photoLabel;
        formState.photoContainer = photoContainer;
        formState.addPhotoButton = addPhotoButton;
        if (existingEstablishment != null) {
            formState.photos.addAll(existingEstablishment.getPhotos());
        }
        currentFormState = formState;

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

        if (addPhotoButton != null) {
            addPhotoButton.setOnClickListener(view -> {
                if (currentFormState == null) {
                    return;
                }
                if (currentFormState.photos.size() >= 5) {
                    Toast.makeText(this, R.string.dialog_error_max_photos_reached, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                }
            });
        }

        refreshPhotoSection(formState);

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

                if (currentFormState != null && currentFormState != formState) {
                    dialog.dismiss();
                    return;
                }

                if (isEditMode) {
                    if (position < 0 || position >= establishments.size()) {
                        dialog.dismiss();
                        return;
                    }
                    Establishment updatedEstablishment = new Establishment(
                            name,
                            comment,
                            new ArrayList<>(formState.photos)
                    );
                    migrateEstablishmentRooms(existingEstablishment, updatedEstablishment);
                    establishments.set(position, updatedEstablishment);
                    establishmentAdapter.notifyItemChanged(position);
                } else {
                    establishments.add(new Establishment(name, comment, new ArrayList<>(formState.photos)));
                    establishmentAdapter.notifyItemInserted(establishments.size() - 1);
                }

                saveEstablishments();
                updateEmptyState();
                dialog.dismiss();
            });
        });

        dialog.setOnDismissListener(d -> {
            if (currentFormState == formState) {
                currentFormState = null;
            }
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
                JSONArray photosArray = item.optJSONArray("photos");
                List<String> photos = new ArrayList<>();
                if (photosArray != null) {
                    for (int j = 0; j < photosArray.length(); j++) {
                        String photoValue = photosArray.optString(j, null);
                        if (photoValue != null && !photoValue.isEmpty()) {
                            photos.add(photoValue);
                        }
                    }
                }
                establishments.add(new Establishment(name, comment, photos));
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
                JSONArray photosArray = new JSONArray();
                for (String photo : establishment.getPhotos()) {
                    photosArray.put(photo);
                }
                item.put("photos", photosArray);
                array.put(item);
            } catch (JSONException ignored) {
                // En pratique cela ne devrait pas arriver car nous n'utilisons que des chaînes.
            }
        }

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putString(KEY_ESTABLISHMENTS, array.toString()).apply();
    }

    private void migrateEstablishmentRooms(@Nullable Establishment previousEstablishment,
            @NonNull Establishment updatedEstablishment) {
        if (previousEstablishment == null) {
            return;
        }

        String previousName = previousEstablishment.getName();
        String newName = updatedEstablishment.getName();
        if (previousName == null || newName == null) {
            return;
        }

        String trimmedPrevious = previousName.trim();
        String trimmedNew = newName.trim();
        if (trimmedPrevious.equals(trimmedNew)) {
            return;
        }

        String oldRoomsKey = EstablishmentContentActivity.buildRoomsKey(trimmedPrevious);
        String newRoomsKey = EstablishmentContentActivity.buildRoomsKey(trimmedNew);
        if (oldRoomsKey.equals(newRoomsKey)) {
            return;
        }

        SharedPreferences roomsPreferences =
                getSharedPreferences(EstablishmentContentActivity.PREFS_NAME, MODE_PRIVATE);
        String storedRoomsValue = roomsPreferences.getString(oldRoomsKey, null);
        if (storedRoomsValue == null) {
            return;
        }

        SharedPreferences.Editor roomsEditor = roomsPreferences.edit();
        roomsEditor.putString(newRoomsKey, storedRoomsValue);
        roomsEditor.remove(oldRoomsKey);
        roomsEditor.apply();

        migrateRoomContentsForEstablishment(trimmedPrevious, trimmedNew, storedRoomsValue);
    }

    private void migrateRoomContentsForEstablishment(String previousEstablishmentName,
            String newEstablishmentName, String storedRoomsValue) {
        SharedPreferences contentPreferences =
                getSharedPreferences(RoomContentStorage.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor contentEditor = contentPreferences.edit();
        boolean hasChanges = false;

        try {
            JSONArray roomsArray = new JSONArray(storedRoomsValue);
            for (int i = 0; i < roomsArray.length(); i++) {
                JSONObject item = roomsArray.optJSONObject(i);
                if (item == null) {
                    continue;
                }

                String roomName = item.optString("name", "");
                if (roomName.isEmpty()) {
                    continue;
                }

                String primaryOldKey = RoomContentStorage.buildKey(previousEstablishmentName, roomName);
                String legacyOldKey = RoomContentStorage.buildLegacyKey(previousEstablishmentName, roomName);
                String resolvedOldKey = primaryOldKey;
                String storedContent = contentPreferences.getString(primaryOldKey, null);
                if (storedContent == null && !legacyOldKey.equals(primaryOldKey)) {
                    storedContent = contentPreferences.getString(legacyOldKey, null);
                    if (storedContent != null) {
                        resolvedOldKey = legacyOldKey;
                    }
                }
                if (storedContent == null) {
                    continue;
                }

                String primaryNewKey = RoomContentStorage.buildKey(newEstablishmentName, roomName);
                String legacyNewKey = RoomContentStorage.buildLegacyKey(newEstablishmentName, roomName);
                contentEditor.putString(primaryNewKey, storedContent);
                if (!resolvedOldKey.equals(primaryNewKey)) {
                    contentEditor.remove(resolvedOldKey);
                }
                if (!legacyOldKey.equals(resolvedOldKey) && !legacyOldKey.equals(primaryNewKey)) {
                    contentEditor.remove(legacyOldKey);
                }
                if (!legacyNewKey.equals(primaryNewKey)) {
                    contentEditor.remove(legacyNewKey);
                }
                hasChanges = true;
            }
        } catch (JSONException ignored) {
        }

        if (hasChanges) {
            contentEditor.apply();
        }
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

    private void refreshPhotoSection(FormState formState) {
        if (formState.photoLabel != null) {
            formState.photoLabel.setText(getString(R.string.dialog_label_establishment_photos_template,
                    formState.photos.size()));
        }

        if (formState.addPhotoButton != null) {
            boolean canAddPhoto = formState.photos.size() < 5;
            formState.addPhotoButton.setEnabled(canAddPhoto);
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

    private void showPhotoPreview(FormState formState, int startIndex) {
        if (formState.photos.isEmpty()) {
            return;
        }

        View previewView = LayoutInflater.from(this).inflate(R.layout.dialog_photo_preview, null);
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
                previousButton.setEnabled(formState.photos.size() > 1);
                nextButton.setEnabled(formState.photos.size() > 1);
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
    private Bitmap decodePhoto(String data) {
        try {
            byte[] decoded = Base64.decode(data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String encodePhoto(Bitmap bitmap) {
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

        Bitmap bitmap = ImageProcessingUtils.cropObjectOnWhiteBackground((Bitmap) value);
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
}
