package com.example.new1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
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

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EstablishmentContentActivity extends Activity {
    static final String PREFS_NAME = "rooms_prefs";
    static final String KEY_ROOMS = "rooms";
    private static final int REQUEST_TAKE_PHOTO = 2001;

    public static final String EXTRA_ESTABLISHMENT_NAME = "extra_establishment_name";

    private final List<Room> rooms = new ArrayList<>();
    private RoomAdapter roomAdapter;
    private RecyclerView roomList;
    private TextView emptyPlaceholder;
    private TextView subtitleView;
    private FormState currentFormState;
    private String establishmentName;

    private static class FormState {
        TextView photoLabel;
        LinearLayout photoContainer;
        View addPhotoButton;
        final List<String> photos = new ArrayList<>();
    }

    public static Intent createIntent(Context context, Establishment establishment) {
        Intent intent = new Intent(context, EstablishmentContentActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (establishment != null) {
            String name = establishment.getName();
            if (name != null) {
                intent.putExtra(EXTRA_ESTABLISHMENT_NAME, name);
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
        setContentView(R.layout.activity_establishment_content);

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
        establishmentName = intent != null
                ? intent.getStringExtra(EXTRA_ESTABLISHMENT_NAME)
                : null;
        TextView titleView = findViewById(R.id.text_establishment_title);
        if (titleView != null) {
            if (establishmentName == null || establishmentName.trim().isEmpty()) {
                titleView.setText(R.string.establishment_content_title);
            } else {
                titleView.setText(getString(R.string.establishment_content_title_with_name,
                        establishmentName));
            }
        }

        subtitleView = findViewById(R.id.text_establishment_subtitle);
        applyEmptySubtitle();

        roomList = findViewById(R.id.list_rooms);
        emptyPlaceholder = findViewById(R.id.text_rooms_placeholder);

        roomAdapter = new RoomAdapter(this, rooms, new RoomAdapter.OnRoomInteractionListener() {
            @Override
            public void onOpenRoom(@NonNull Room room, int position) {
                openRoomContent(room);
            }

            @Override
            public void onEditRoom(@NonNull Room room, int position) {
                showRoomDialog(room, position);
            }

            @Override
            public void onDeleteRoom(@NonNull Room room, int position) {
                showDeleteRoomConfirmation(room, position);
            }
        });
        if (roomList != null) {
            roomList.setLayoutManager(new LinearLayoutManager(this));
            roomList.setAdapter(roomAdapter);
        }

        View addButton = findViewById(R.id.button_add_content);
        if (addButton != null) {
            addButton.setOnClickListener(view -> showRoomDialog(null, -1));
        }

        loadRooms();
        updateEmptyState();
    }

    private void openRoomContent(@NonNull Room room) {
        Intent intent = RoomContentActivity.createIntent(this, establishmentName, room);
        startActivity(intent);
    }

    private void showRoomDialog(@Nullable Room roomToEdit, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_room, null);
        TextView titleView = dialogView.findViewById(R.id.text_dialog_title);
        TextView photoLabel = dialogView.findViewById(R.id.text_room_photos_label);
        View addPhotoButton = dialogView.findViewById(R.id.button_add_photo);
        LinearLayout photoContainer = dialogView.findViewById(R.id.container_room_photos);
        EditText nameInput = dialogView.findViewById(R.id.input_room_name);
        EditText commentInput = dialogView.findViewById(R.id.input_room_comment);

        FormState formState = new FormState();
        formState.photoLabel = photoLabel;
        formState.photoContainer = photoContainer;
        formState.addPhotoButton = addPhotoButton;
        currentFormState = formState;

        boolean isEditing = roomToEdit != null && position >= 0 && position < rooms.size();

        if (titleView != null) {
            titleView.setText(isEditing ? R.string.dialog_edit_room_title : R.string.dialog_add_room_title);
        }

        if (isEditing) {
            if (nameInput != null) {
                nameInput.setText(roomToEdit.getName());
                nameInput.setSelection(nameInput.getText().length());
            }
            if (commentInput != null) {
                commentInput.setText(roomToEdit.getComment());
            }
            formState.photos.addAll(roomToEdit.getPhotos());
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
                String comment = commentInput.getText() != null
                        ? commentInput.getText().toString().trim()
                        : "";
                if (name.isEmpty()) {
                    nameInput.setError(getString(R.string.error_room_name_required));
                    return;
                }

                nameInput.setError(null);

                if (currentFormState != null && currentFormState != formState) {
                    dialog.dismiss();
                    return;
                }

                Room updatedRoom = new Room(name, comment, new ArrayList<>(formState.photos));
                if (isEditing) {
                    migrateRoomContent(roomToEdit, updatedRoom);
                    rooms.set(position, updatedRoom);
                    roomAdapter.notifyItemChanged(position);
                } else {
                    rooms.add(updatedRoom);
                    roomAdapter.notifyItemInserted(rooms.size() - 1);
                }
                saveRooms();
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

    private void migrateRoomContent(@Nullable Room previousRoom, @NonNull Room updatedRoom) {
        if (previousRoom == null) {
            return;
        }
        String previousName = previousRoom.getName();
        String newName = updatedRoom.getName();
        if (previousName == null || newName == null) {
            return;
        }
        String trimmedPrevious = previousName.trim();
        String trimmedNew = newName.trim();
        if (trimmedPrevious.equals(trimmedNew)) {
            return;
        }
        SharedPreferences preferences = getSharedPreferences(RoomContentStorage.PREFS_NAME, MODE_PRIVATE);
        String primaryOldKey = RoomContentStorage.buildKey(establishmentName, previousName);
        String legacyOldKey = RoomContentStorage.buildLegacyKey(establishmentName, previousName);
        String resolvedOldKey = primaryOldKey;
        String storedValue = preferences.getString(primaryOldKey, null);
        if (storedValue == null && !legacyOldKey.equals(primaryOldKey)) {
            storedValue = preferences.getString(legacyOldKey, null);
            if (storedValue != null) {
                resolvedOldKey = legacyOldKey;
            }
        }
        if (storedValue == null) {
            return;
        }

        String primaryNewKey = RoomContentStorage.buildKey(establishmentName, newName);
        String legacyNewKey = RoomContentStorage.buildLegacyKey(establishmentName, newName);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(primaryNewKey, storedValue);
        if (!resolvedOldKey.equals(primaryNewKey)) {
            editor.remove(resolvedOldKey);
        }
        if (!legacyOldKey.equals(resolvedOldKey) && !legacyOldKey.equals(primaryNewKey)) {
            editor.remove(legacyOldKey);
        }
        if (!legacyNewKey.equals(primaryNewKey)) {
            editor.remove(legacyNewKey);
        }
        editor.apply();
    }

    private void showDeleteRoomConfirmation(@NonNull Room room, int position) {
        if (position < 0 || position >= rooms.size()) {
            return;
        }

        String name = room.getName();
        if (name == null) {
            name = "";
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_room_title)
                .setMessage(getString(R.string.dialog_delete_room_message, name))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.dialog_delete_room_confirm, (dialog, which) -> {
                    rooms.remove(position);
                    roomAdapter.notifyItemRemoved(position);
                    saveRooms();
                    updateEmptyState();
                })
                .show();
    }

    private void loadRooms() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String storedValue = preferences.getString(getRoomsKey(), null);
        rooms.clear();

        if (storedValue == null || storedValue.isEmpty()) {
            roomAdapter.notifyDataSetChanged();
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
                List<String> photos = new ArrayList<>();
                JSONArray photosArray = item.optJSONArray("photos");
                if (photosArray != null) {
                    for (int j = 0; j < photosArray.length(); j++) {
                        String value = photosArray.optString(j, null);
                        if (value != null && !value.isEmpty()) {
                            photos.add(value);
                        }
                    }
                }
                rooms.add(new Room(name, comment, photos));
            }
        } catch (JSONException ignored) {
        }

        roomAdapter.notifyDataSetChanged();
    }

    private void saveRooms() {
        JSONArray array = new JSONArray();
        for (Room room : rooms) {
            JSONObject item = new JSONObject();
            try {
                item.put("name", room.getName());
                item.put("comment", room.getComment());
                JSONArray photosArray = new JSONArray();
                for (String photo : room.getPhotos()) {
                    photosArray.put(photo);
                }
                item.put("photos", photosArray);
                array.put(item);
            } catch (JSONException ignored) {
            }
        }

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putString(getRoomsKey(), array.toString()).apply();
    }

    private void updateEmptyState() {
        if (emptyPlaceholder == null || roomList == null) {
            return;
        }
        if (rooms.isEmpty()) {
            String name = getTrimmedEstablishmentName();
            if (name.isEmpty()) {
                emptyPlaceholder.setText(R.string.rooms_empty_state);
            } else {
                emptyPlaceholder.setText(getString(R.string.rooms_empty_state_with_name, name));
            }
            emptyPlaceholder.setVisibility(View.VISIBLE);
            roomList.setVisibility(View.GONE);
            applyEmptySubtitle();
        } else {
            emptyPlaceholder.setVisibility(View.GONE);
            roomList.setVisibility(View.VISIBLE);
            applyRoomsSubtitle();
        }
    }

    private String getTrimmedEstablishmentName() {
        return establishmentName != null ? establishmentName.trim() : "";
    }

    private void applyEmptySubtitle() {
        if (subtitleView == null) {
            return;
        }
        subtitleView.setVisibility(View.VISIBLE);
        String name = getTrimmedEstablishmentName();
        if (name.isEmpty()) {
            subtitleView.setText(R.string.establishment_content_placeholder);
        } else {
            subtitleView.setText(getString(R.string.establishment_content_placeholder_with_name, name));
        }
    }

    private void applyRoomsSubtitle() {
        if (subtitleView == null) {
            return;
        }
        subtitleView.setText("");
        subtitleView.setVisibility(View.GONE);
    }

    private void refreshPhotoSection(FormState formState) {
        if (formState.photoLabel != null) {
            formState.photoLabel.setText(getString(R.string.dialog_label_room_photos_template,
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

    static String buildRoomsKey(@Nullable String establishmentName) {
        String name = establishmentName != null ? establishmentName.trim() : "";
        if (name.isEmpty()) {
            return KEY_ROOMS + "_default";
        }
        return KEY_ROOMS + "_" + name;
    }

    private String getRoomsKey() {
        return buildRoomsKey(establishmentName);
    }
}
