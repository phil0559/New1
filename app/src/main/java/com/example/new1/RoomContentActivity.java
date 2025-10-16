package com.example.new1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomContentActivity extends Activity {
    private static final String PREFS_NAME = "room_content_prefs";
    private static final String KEY_ROOM_CONTENT_PREFIX = "room_content_";
    private static final int REQUEST_TAKE_PHOTO = 2001;
    private static final int MAX_FORM_PHOTOS = 5;

    public static final String EXTRA_ESTABLISHMENT_NAME = "extra_establishment_name";
    public static final String EXTRA_ROOM_NAME = "extra_room_name";

    private static class FormState {
        @Nullable
        TextView photoLabel;
        @Nullable
        LinearLayout photoContainer;
        @Nullable
        View addPhotoButton;
        @Nullable
        LinearLayout trackContainer;
        final List<String> photos = new ArrayList<>();
        final List<String> tracks = new ArrayList<>();
    }

    private static class DialogController {
        final boolean isEditing;
        final int positionToEdit;
        @Nullable
        final EditText nameInput;
        @Nullable
        final EditText commentInput;
        @Nullable
        final TextView barcodeValueView;
        @Nullable
        final EditText seriesInput;
        @Nullable
        final EditText numberInput;
        @Nullable
        final EditText authorInput;
        @Nullable
        final EditText publisherInput;
        @Nullable
        final EditText editionInput;
        @Nullable
        final EditText publicationDateInput;
        @Nullable
        final EditText summaryInput;
        final String[] selectedTypeHolder;
        final String[] selectedCategoryHolder;
        final FormState formState;
        final AlertDialog dialog;

        DialogController(boolean isEditing,
                         int positionToEdit,
                         @Nullable EditText nameInput,
                         @Nullable EditText commentInput,
                         @Nullable TextView barcodeValueView,
                         @Nullable EditText seriesInput,
                         @Nullable EditText numberInput,
                         @Nullable EditText authorInput,
                         @Nullable EditText publisherInput,
                         @Nullable EditText editionInput,
                         @Nullable EditText publicationDateInput,
                         @Nullable EditText summaryInput,
                         @NonNull String[] selectedTypeHolder,
                         @NonNull String[] selectedCategoryHolder,
                         @NonNull FormState formState,
                         @NonNull AlertDialog dialog) {
            this.isEditing = isEditing;
            this.positionToEdit = positionToEdit;
            this.nameInput = nameInput;
            this.commentInput = commentInput;
            this.barcodeValueView = barcodeValueView;
            this.seriesInput = seriesInput;
            this.numberInput = numberInput;
            this.authorInput = authorInput;
            this.publisherInput = publisherInput;
            this.editionInput = editionInput;
            this.publicationDateInput = publicationDateInput;
            this.summaryInput = summaryInput;
            this.selectedTypeHolder = selectedTypeHolder;
            this.selectedCategoryHolder = selectedCategoryHolder;
            this.formState = formState;
            this.dialog = dialog;
        }
    }

    private static class BarcodeScanContext {
        final FormState formState;
        @Nullable
        final EditText nameInput;
        @Nullable
        final TextView barcodeValueView;
        @Nullable
        final Button selectTypeButton;
        @Nullable
        final View bookFields;
        @Nullable
        final View trackFields;
        @Nullable
        final TextView trackTitle;
        final String[] selectedTypeHolder;
        @Nullable
        final EditText seriesInput;
        @Nullable
        final EditText numberInput;
        @Nullable
        final EditText authorInput;
        @Nullable
        final EditText publisherInput;
        @Nullable
        final EditText editionInput;
        @Nullable
        final EditText publicationDateInput;
        @Nullable
        final EditText summaryInput;

        BarcodeScanContext(@NonNull FormState formState,
                            @Nullable EditText nameInput,
                            @Nullable TextView barcodeValueView,
                            @Nullable Button selectTypeButton,
                            @Nullable View bookFields,
                            @Nullable View trackFields,
                            @Nullable TextView trackTitle,
                            @NonNull String[] selectedTypeHolder,
                            @Nullable EditText seriesInput,
                            @Nullable EditText numberInput,
                            @Nullable EditText authorInput,
                            @Nullable EditText publisherInput,
                            @Nullable EditText editionInput,
                            @Nullable EditText publicationDateInput,
                            @Nullable EditText summaryInput) {
            this.formState = formState;
            this.nameInput = nameInput;
            this.barcodeValueView = barcodeValueView;
            this.selectTypeButton = selectTypeButton;
            this.bookFields = bookFields;
            this.trackFields = trackFields;
            this.trackTitle = trackTitle;
            this.selectedTypeHolder = selectedTypeHolder;
            this.seriesInput = seriesInput;
            this.numberInput = numberInput;
            this.authorInput = authorInput;
            this.publisherInput = publisherInput;
            this.editionInput = editionInput;
            this.publicationDateInput = publicationDateInput;
            this.summaryInput = summaryInput;
        }
    }

    private static class PendingBarcodeResult {
        final boolean editing;
        final int positionToEdit;
        @Nullable
        String name;
        @Nullable
        String comment;
        @Nullable
        String selectedType;
        @Nullable
        String selectedCategory;
        @Nullable
        String barcode;
        @Nullable
        String series;
        @Nullable
        String number;
        @Nullable
        String author;
        @Nullable
        String publisher;
        @Nullable
        String edition;
        @Nullable
        String publicationDate;
        @Nullable
        String summary;
        final ArrayList<String> tracks = new ArrayList<>();
        final ArrayList<String> photos = new ArrayList<>();
        boolean resumeLookup;
        boolean reopenDialog;

        PendingBarcodeResult(boolean editing, int positionToEdit) {
            this.editing = editing;
            this.positionToEdit = positionToEdit;
        }

        boolean matches(boolean editing, int positionToEdit) {
            if (this.editing != editing) {
                return false;
            }
            if (!this.editing) {
                return true;
            }
            return this.positionToEdit == positionToEdit;
        }
    }

    private static class BarcodeLookupResult {
        boolean found;
        boolean networkError;
        @Nullable
        String typeLabel;
        @Nullable
        String title;
        @Nullable
        String author;
        @Nullable
        String publisher;
        @Nullable
        String series;
        @Nullable
        String number;
        @Nullable
        String edition;
        @Nullable
        String publishDate;
        @Nullable
        String summary;
        final List<String> photos = new ArrayList<>();
        @Nullable
        String infoMessage;
        @Nullable
        String errorMessage;
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
    @Nullable
    private DialogController currentDialogController;
    @Nullable
    private BarcodeScanContext barcodeScanContext;
    private final ExecutorService barcodeLookupExecutor = Executors.newSingleThreadExecutor();
    @Nullable
    private PendingBarcodeResult pendingBarcodeResult;
    private boolean barcodeScanAwaitingResult;

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

        restorePendingBarcodeResult(savedInstanceState);
        maybeRestorePendingDialog();
    }

    private void restorePendingBarcodeResult(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null || !savedInstanceState.getBoolean("pending_barcode_exists", false)) {
            return;
        }
        PendingBarcodeResult restored = new PendingBarcodeResult(
                savedInstanceState.getBoolean("pending_barcode_editing", false),
                savedInstanceState.getInt("pending_barcode_position", -1));
        restored.name = savedInstanceState.getString("pending_barcode_name");
        restored.comment = savedInstanceState.getString("pending_barcode_comment");
        restored.selectedType = savedInstanceState.getString("pending_barcode_type");
        restored.selectedCategory = savedInstanceState.getString("pending_barcode_category");
        restored.barcode = savedInstanceState.getString("pending_barcode_value");
        restored.series = savedInstanceState.getString("pending_barcode_series");
        restored.number = savedInstanceState.getString("pending_barcode_number");
        restored.author = savedInstanceState.getString("pending_barcode_author");
        restored.publisher = savedInstanceState.getString("pending_barcode_publisher");
        restored.edition = savedInstanceState.getString("pending_barcode_edition");
        restored.publicationDate = savedInstanceState.getString("pending_barcode_publication_date");
        restored.summary = savedInstanceState.getString("pending_barcode_summary");
        ArrayList<String> photos = savedInstanceState.getStringArrayList("pending_barcode_photos");
        if (photos != null) {
            restored.photos.addAll(photos);
        }
        ArrayList<String> tracks = savedInstanceState.getStringArrayList("pending_barcode_tracks");
        if (tracks != null) {
            restored.tracks.addAll(tracks);
        }
        restored.resumeLookup = savedInstanceState.getBoolean("pending_barcode_resume_lookup", false);
        restored.reopenDialog = savedInstanceState.getBoolean("pending_barcode_reopen", false);
        pendingBarcodeResult = restored;
        barcodeScanAwaitingResult = savedInstanceState.getBoolean("pending_barcode_waiting", false);
    }

    private void maybeRestorePendingDialog() {
        if (pendingBarcodeResult == null || !pendingBarcodeResult.reopenDialog || barcodeScanAwaitingResult) {
            return;
        }
        RoomContentItem itemToEdit = null;
        int positionToEdit = pendingBarcodeResult.positionToEdit;
        if (pendingBarcodeResult.editing) {
            if (positionToEdit >= 0 && positionToEdit < roomContentItems.size()) {
                itemToEdit = roomContentItems.get(positionToEdit);
            } else {
                positionToEdit = -1;
            }
        }
        showRoomContentDialog(itemToEdit, positionToEdit);
        if (pendingBarcodeResult != null) {
            pendingBarcodeResult.reopenDialog = false;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentDialogController != null) {
            DialogController controller = currentDialogController;
            PendingBarcodeResult existing = pendingBarcodeResult;
            PendingBarcodeResult snapshot = capturePendingBarcodeResult(controller.isEditing,
                    controller.positionToEdit,
                    controller.nameInput,
                    controller.commentInput,
                    controller.barcodeValueView,
                    controller.selectedTypeHolder,
                    controller.selectedCategoryHolder,
                    controller.seriesInput,
                    controller.numberInput,
                    controller.authorInput,
                    controller.publisherInput,
                    controller.editionInput,
                    controller.publicationDateInput,
                    controller.summaryInput,
                    controller.formState);
            if (existing != null && existing.resumeLookup) {
                snapshot.resumeLookup = true;
            } else if (barcodeScanContext != null
                    && barcodeScanContext.formState == controller.formState) {
                snapshot.resumeLookup = true;
            }
            if (existing != null && TextUtils.isEmpty(snapshot.barcode)
                    && !TextUtils.isEmpty(existing.barcode)) {
                snapshot.barcode = existing.barcode;
            }
            snapshot.reopenDialog = controller.dialog.isShowing();
            pendingBarcodeResult = snapshot;
        }
        if (pendingBarcodeResult != null) {
            outState.putBoolean("pending_barcode_exists", true);
            outState.putBoolean("pending_barcode_editing", pendingBarcodeResult.editing);
            outState.putInt("pending_barcode_position", pendingBarcodeResult.positionToEdit);
            outState.putString("pending_barcode_name", pendingBarcodeResult.name);
            outState.putString("pending_barcode_comment", pendingBarcodeResult.comment);
            outState.putString("pending_barcode_type", pendingBarcodeResult.selectedType);
            outState.putString("pending_barcode_category", pendingBarcodeResult.selectedCategory);
            outState.putString("pending_barcode_value", pendingBarcodeResult.barcode);
            outState.putString("pending_barcode_series", pendingBarcodeResult.series);
            outState.putString("pending_barcode_number", pendingBarcodeResult.number);
            outState.putString("pending_barcode_author", pendingBarcodeResult.author);
            outState.putString("pending_barcode_publisher", pendingBarcodeResult.publisher);
            outState.putString("pending_barcode_edition", pendingBarcodeResult.edition);
            outState.putString("pending_barcode_publication_date", pendingBarcodeResult.publicationDate);
            outState.putString("pending_barcode_summary", pendingBarcodeResult.summary);
            outState.putStringArrayList("pending_barcode_photos", new ArrayList<>(pendingBarcodeResult.photos));
            outState.putStringArrayList("pending_barcode_tracks", new ArrayList<>(pendingBarcodeResult.tracks));
            outState.putBoolean("pending_barcode_resume_lookup", pendingBarcodeResult.resumeLookup);
            outState.putBoolean("pending_barcode_waiting", barcodeScanAwaitingResult);
            outState.putBoolean("pending_barcode_reopen", pendingBarcodeResult.reopenDialog);
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
        LinearLayout trackListContainer = dialogView.findViewById(R.id.container_track_list);
        EditText seriesInput = dialogView.findViewById(R.id.input_series);
        EditText numberInput = dialogView.findViewById(R.id.input_number);
        EditText authorInput = dialogView.findViewById(R.id.input_author);
        EditText publisherInput = dialogView.findViewById(R.id.input_publisher);
        EditText editionInput = dialogView.findViewById(R.id.input_edition);
        EditText publicationDateInput = dialogView.findViewById(R.id.input_publication_date);
        EditText summaryInput = dialogView.findViewById(R.id.input_summary);
        TextView dialogTitle = dialogView.findViewById(R.id.text_dialog_room_content_title);
        PendingBarcodeResult restoreData = pendingBarcodeResult != null
                && pendingBarcodeResult.matches(isEditing, positionToEdit)
                ? pendingBarcodeResult
                : null;

        if (pendingBarcodeResult != null
                && pendingBarcodeResult.matches(isEditing, positionToEdit)) {
            pendingBarcodeResult.reopenDialog = false;
        }

        if (dialogTitle != null) {
            dialogTitle.setText(isEditing
                    ? R.string.dialog_edit_room_content_title
                    : R.string.dialog_add_room_content_title);
        }

        if (barcodeValueView != null) {
            if (restoreData != null && !TextUtils.isEmpty(restoreData.barcode)) {
                barcodeValueView.setText(restoreData.barcode);
            } else if (isEditing && itemToEdit != null) {
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
            if (seriesInput != null) {
                String series = itemToEdit.getSeries();
                seriesInput.setText(series);
                if (series != null) {
                    seriesInput.setSelection(series.length());
                }
            }
            if (numberInput != null) {
                String number = itemToEdit.getNumber();
                numberInput.setText(number);
                if (number != null) {
                    numberInput.setSelection(number.length());
                }
            }
            if (authorInput != null) {
                String author = itemToEdit.getAuthor();
                authorInput.setText(author);
                if (author != null) {
                    authorInput.setSelection(author.length());
                }
            }
            if (publisherInput != null) {
                String publisher = itemToEdit.getPublisher();
                publisherInput.setText(publisher);
                if (publisher != null) {
                    publisherInput.setSelection(publisher.length());
                }
            }
            if (editionInput != null) {
                String edition = itemToEdit.getEdition();
                editionInput.setText(edition);
                if (edition != null) {
                    editionInput.setSelection(edition.length());
                }
            }
            if (publicationDateInput != null) {
                String publicationDate = itemToEdit.getPublicationDate();
                publicationDateInput.setText(publicationDate);
                if (publicationDate != null) {
                    publicationDateInput.setSelection(publicationDate.length());
                }
            }
            if (summaryInput != null) {
                String summary = itemToEdit.getSummary();
                summaryInput.setText(summary);
                if (summary != null) {
                    summaryInput.setSelection(summary.length());
                }
            }
        }

        if (restoreData != null) {
            if (nameInput != null) {
                String value = restoreData.name != null ? restoreData.name : "";
                nameInput.setText(value);
                nameInput.setSelection(value.length());
            }
            if (commentInput != null) {
                commentInput.setText(restoreData.comment != null ? restoreData.comment : "");
            }
            if (seriesInput != null) {
                seriesInput.setText(restoreData.series != null ? restoreData.series : "");
                if (restoreData.series != null) {
                    seriesInput.setSelection(restoreData.series.length());
                }
            }
            if (numberInput != null) {
                numberInput.setText(restoreData.number != null ? restoreData.number : "");
                if (restoreData.number != null) {
                    numberInput.setSelection(restoreData.number.length());
                }
            }
            if (authorInput != null) {
                authorInput.setText(restoreData.author != null ? restoreData.author : "");
                if (restoreData.author != null) {
                    authorInput.setSelection(restoreData.author.length());
                }
            }
            if (publisherInput != null) {
                publisherInput.setText(restoreData.publisher != null ? restoreData.publisher : "");
                if (restoreData.publisher != null) {
                    publisherInput.setSelection(restoreData.publisher.length());
                }
            }
            if (editionInput != null) {
                editionInput.setText(restoreData.edition != null ? restoreData.edition : "");
                if (restoreData.edition != null) {
                    editionInput.setSelection(restoreData.edition.length());
                }
            }
            if (publicationDateInput != null) {
                publicationDateInput.setText(restoreData.publicationDate != null
                        ? restoreData.publicationDate
                        : "");
                if (restoreData.publicationDate != null) {
                    publicationDateInput.setSelection(restoreData.publicationDate.length());
                }
            }
            if (summaryInput != null) {
                summaryInput.setText(restoreData.summary != null ? restoreData.summary : "");
                if (restoreData.summary != null) {
                    summaryInput.setSelection(restoreData.summary.length());
                }
            }
            if (barcodeValueView != null) {
                if (!TextUtils.isEmpty(restoreData.barcode)) {
                    barcodeValueView.setText(restoreData.barcode);
                } else {
                    barcodeValueView.setText(R.string.dialog_label_barcode_placeholder);
                }
            }
        }

        final FormState formState = new FormState();
        formState.photoLabel = dialogView.findViewById(R.id.text_room_content_photos_label);
        formState.photoContainer = photoContainer;
        formState.addPhotoButton = addPhotoButton;
        formState.trackContainer = trackListContainer;
        currentFormState = formState;
        if (isEditing && itemToEdit != null) {
            formState.photos.addAll(itemToEdit.getPhotos());
            formState.tracks.addAll(itemToEdit.getTracks());
        }
        if (restoreData != null) {
            formState.photos.clear();
            formState.photos.addAll(restoreData.photos);
            formState.tracks.clear();
            formState.tracks.addAll(restoreData.tracks);
        }
        refreshTrackInputs(formState);

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
        if (restoreData != null && !TextUtils.isEmpty(restoreData.selectedType)) {
            if (!typeOptions.contains(restoreData.selectedType)) {
                typeOptions.add(restoreData.selectedType);
            }
            selectedTypeHolder[0] = restoreData.selectedType;
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
        if (restoreData != null && !TextUtils.isEmpty(restoreData.selectedCategory)) {
            if (!categoryOptions.contains(restoreData.selectedCategory)) {
                categoryOptions.add(restoreData.selectedCategory);
            }
            selectedCategoryHolder[0] = restoreData.selectedCategory;
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

                String seriesValue = null;
                if (seriesInput != null) {
                    CharSequence seriesText = seriesInput.getText();
                    String trimmedSeries = seriesText != null ? seriesText.toString().trim() : "";
                    if (!trimmedSeries.isEmpty()) {
                        seriesValue = trimmedSeries;
                    }
                }

                String numberValue = null;
                if (numberInput != null) {
                    CharSequence numberText = numberInput.getText();
                    String trimmedNumber = numberText != null ? numberText.toString().trim() : "";
                    if (!trimmedNumber.isEmpty()) {
                        numberValue = trimmedNumber;
                    }
                }

                String authorValue = null;
                if (authorInput != null) {
                    CharSequence authorText = authorInput.getText();
                    String trimmedAuthor = authorText != null ? authorText.toString().trim() : "";
                    if (!trimmedAuthor.isEmpty()) {
                        authorValue = trimmedAuthor;
                    }
                }

                String publisherValue = null;
                if (publisherInput != null) {
                    CharSequence publisherText = publisherInput.getText();
                    String trimmedPublisher = publisherText != null ? publisherText.toString().trim() : "";
                    if (!trimmedPublisher.isEmpty()) {
                        publisherValue = trimmedPublisher;
                    }
                }

                String editionValue = null;
                if (editionInput != null) {
                    CharSequence editionText = editionInput.getText();
                    String trimmedEdition = editionText != null ? editionText.toString().trim() : "";
                    if (!trimmedEdition.isEmpty()) {
                        editionValue = trimmedEdition;
                    }
                }

                String publicationDateValue = null;
                if (publicationDateInput != null) {
                    CharSequence publicationDateText = publicationDateInput.getText();
                    String trimmedPublicationDate = publicationDateText != null
                            ? publicationDateText.toString().trim()
                            : "";
                    if (!trimmedPublicationDate.isEmpty()) {
                        publicationDateValue = trimmedPublicationDate;
                    }
                }

                String summaryValue = null;
                if (summaryInput != null) {
                    CharSequence summaryText = summaryInput.getText();
                    String trimmedSummary = summaryText != null ? summaryText.toString().trim() : "";
                    if (!trimmedSummary.isEmpty()) {
                        summaryValue = trimmedSummary;
                    }
                }

                if (currentFormState != null && currentFormState != formState) {
                    dialog.dismiss();
                    return;
                }

                List<String> trackValues = collectTracks(formState);

                RoomContentItem item = new RoomContentItem(trimmedName,
                        trimmedComment,
                        selectedTypeHolder[0],
                        selectedCategoryHolder[0],
                        barcodeValue,
                        seriesValue,
                        numberValue,
                        authorValue,
                        publisherValue,
                        editionValue,
                        publicationDateValue,
                        summaryValue,
                        trackValues,
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
            if (barcodeScanContext != null && barcodeScanContext.formState == formState) {
                barcodeScanContext = null;
            }
            if (pendingBarcodeResult != null
                    && pendingBarcodeResult.matches(isEditing, positionToEdit)
                    && !barcodeScanAwaitingResult) {
                pendingBarcodeResult = null;
            }
            if (currentDialogController != null && currentDialogController.dialog == dialog) {
                currentDialogController = null;
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
                if (formState.photos.size() >= MAX_FORM_PHOTOS) {
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
            barcodeButton.setOnClickListener(v -> {
                if (currentFormState == null || currentFormState != formState) {
                    return;
                }
                pendingBarcodeResult = capturePendingBarcodeResult(isEditing,
                        positionToEdit,
                        nameInput,
                        commentInput,
                        barcodeValueView,
                        selectedTypeHolder,
                        selectedCategoryHolder,
                        seriesInput,
                        numberInput,
                        authorInput,
                        publisherInput,
                        editionInput,
                        publicationDateInput,
                        summaryInput,
                        formState);
                barcodeScanContext = createBarcodeScanContext(formState,
                        nameInput,
                        barcodeValueView,
                        selectTypeButton,
                        bookFields,
                        trackFields,
                        trackTitle,
                        selectedTypeHolder,
                        seriesInput,
                        numberInput,
                        authorInput,
                        publisherInput,
                        editionInput,
                        publicationDateInput,
                        summaryInput);
                barcodeScanAwaitingResult = true;
                launchBarcodeScanner();
            });
        }

        if (addTrackButton != null) {
            addTrackButton.setOnClickListener(v -> {
                formState.tracks.add("");
                appendTrackInput(formState, formState.tracks.size() - 1, true);
            });
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

        currentDialogController = new DialogController(isEditing,
                positionToEdit,
                nameInput,
                commentInput,
                barcodeValueView,
                seriesInput,
                numberInput,
                authorInput,
                publisherInput,
                editionInput,
                publicationDateInput,
                summaryInput,
                selectedTypeHolder,
                selectedCategoryHolder,
                formState,
                dialog);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (restoreData != null && restoreData.resumeLookup
                && !TextUtils.isEmpty(restoreData.barcode)) {
            barcodeScanContext = createBarcodeScanContext(formState,
                    nameInput,
                    barcodeValueView,
                    selectTypeButton,
                    bookFields,
                    trackFields,
                    trackTitle,
                    selectedTypeHolder,
                    seriesInput,
                    numberInput,
                    authorInput,
                    publisherInput,
                    editionInput,
                    publicationDateInput,
                    summaryInput);
            Toast.makeText(this, R.string.dialog_barcode_lookup_in_progress, Toast.LENGTH_SHORT).show();
            fetchMetadataForBarcode(restoreData.barcode, barcodeScanContext);
            restoreData.resumeLookup = false;
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
            formState.addPhotoButton.setEnabled(formState.photos.size() < MAX_FORM_PHOTOS);
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

    private void refreshTrackInputs(@NonNull FormState formState) {
        LinearLayout container = formState.trackContainer;
        if (container == null) {
            return;
        }
        container.removeAllViews();
        for (int i = 0; i < formState.tracks.size(); i++) {
            appendTrackInput(formState, i, false);
        }
    }

    private void appendTrackInput(@NonNull FormState formState, int index, boolean requestFocus) {
        LinearLayout container = formState.trackContainer;
        if (container == null) {
            return;
        }
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_track_input, container, false);
        TextView numberView = view.findViewById(R.id.text_track_number);
        if (numberView != null) {
            numberView.setText(getString(R.string.dialog_track_field_label, index + 1));
        }
        EditText input = view.findViewById(R.id.edit_track_name);
        if (input == null) {
            return;
        }
        input.setHint(getString(R.string.dialog_track_field_hint, index + 1));
        String value = formState.tracks.get(index);
        if (value != null && !value.isEmpty()) {
            input.setText(value);
            input.setSelection(value.length());
        }
        final int trackIndex = index;
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable != null ? editable.toString() : "";
                formState.tracks.set(trackIndex, text);
            }
        });
        container.addView(view);
        if (requestFocus) {
            input.requestFocus();
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
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            handleBarcodeScanResult(scanResult);
            return;
        }

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

        if (currentFormState.photos.size() >= MAX_FORM_PHOTOS) {
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

    private void launchBarcodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(getString(R.string.dialog_barcode_prompt));
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setCaptureActivity(PortraitCaptureActivity.class);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    private void handleBarcodeScanResult(@NonNull IntentResult result) {
        String contents = result.getContents();
        if (contents == null) {
            Toast.makeText(this, R.string.dialog_barcode_scan_cancelled, Toast.LENGTH_SHORT).show();
            barcodeScanAwaitingResult = false;
            pendingBarcodeResult = null;
            return;
        }
        String trimmed = contents.trim();
        if (trimmed.isEmpty()) {
            Toast.makeText(this, R.string.dialog_barcode_scan_failed, Toast.LENGTH_SHORT).show();
            barcodeScanAwaitingResult = false;
            return;
        }
        BarcodeScanContext context = barcodeScanContext;
        if (context == null || currentFormState == null || currentFormState != context.formState) {
            barcodeScanAwaitingResult = false;
            if (pendingBarcodeResult != null) {
                pendingBarcodeResult.barcode = trimmed;
                pendingBarcodeResult.resumeLookup = true;
                if (pendingBarcodeResult.editing) {
                    RoomContentItem itemToEdit = null;
                    if (pendingBarcodeResult.positionToEdit >= 0
                            && pendingBarcodeResult.positionToEdit < roomContentItems.size()) {
                        itemToEdit = roomContentItems.get(pendingBarcodeResult.positionToEdit);
                    }
                    showRoomContentDialog(itemToEdit, pendingBarcodeResult.positionToEdit);
                } else {
                    showRoomContentDialog(null, -1);
                }
            } else {
                Toast.makeText(this, R.string.dialog_barcode_scan_lost_context, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        barcodeScanAwaitingResult = false;
        if (pendingBarcodeResult != null) {
            pendingBarcodeResult.barcode = trimmed;
            pendingBarcodeResult.resumeLookup = false;
        }
        if (context.barcodeValueView != null) {
            context.barcodeValueView.setText(trimmed);
        }
        Toast.makeText(this, R.string.dialog_barcode_lookup_in_progress, Toast.LENGTH_SHORT).show();
        fetchMetadataForBarcode(trimmed, context);
    }

    private void fetchMetadataForBarcode(@NonNull String barcode,
                                         @NonNull BarcodeScanContext context) {
        barcodeLookupExecutor.execute(() -> {
            BarcodeLookupResult lookupResult = performLookup(barcode);
            runOnUiThread(() -> applyBarcodeLookupResult(barcode, context, lookupResult));
        });
    }

    @NonNull
    private BarcodeLookupResult performLookup(@NonNull String barcode) {
        BarcodeLookupResult bookResult = lookupBook(barcode);
        if (bookResult != null && bookResult.found) {
            return bookResult;
        }
        BarcodeLookupResult musicResult = lookupMusic(barcode);
        if (musicResult != null && musicResult.found) {
            return musicResult;
        }
        if (bookResult != null) {
            if (bookResult.errorMessage != null || bookResult.infoMessage != null) {
                return bookResult;
            }
        }
        if (musicResult != null) {
            if (musicResult.errorMessage != null || musicResult.infoMessage != null) {
                return musicResult;
            }
        }
        BarcodeLookupResult fallback = new BarcodeLookupResult();
        fallback.infoMessage = getString(R.string.dialog_barcode_lookup_not_found, barcode);
        return fallback;
    }

    @Nullable
    private BarcodeLookupResult lookupBook(@NonNull String barcode) {
        if (!isIsbnCandidate(barcode)) {
            return null;
        }
        HttpURLConnection connection = null;
        try {
            String urlValue = "https://www.googleapis.com/books/v1/volumes?q=isbn:" +
                    URLEncoder.encode(barcode, StandardCharsets.UTF_8.name());
            URL url = new URL(urlValue);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "New1App/1.0 (Barcode lookup)");
            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();
            if (stream == null) {
                BarcodeLookupResult error = new BarcodeLookupResult();
                error.errorMessage = getString(R.string.dialog_barcode_lookup_error);
                error.networkError = true;
                return error;
            }
            String response = readStream(stream);
            if (responseCode >= 400) {
                BarcodeLookupResult error = new BarcodeLookupResult();
                error.errorMessage = getString(R.string.dialog_barcode_lookup_error);
                error.networkError = true;
                return error;
            }
            JSONObject root = new JSONObject(response);
            JSONArray items = root.optJSONArray("items");
            if (items == null || items.length() == 0) {
                BarcodeLookupResult fallback = new BarcodeLookupResult();
                enrichBookWithOpenLibrary(barcode, fallback);
                return fallback;
            }
            JSONObject firstItem = items.getJSONObject(0);
            JSONObject volumeInfo = firstItem.optJSONObject("volumeInfo");
            if (volumeInfo == null) {
                BarcodeLookupResult fallback = new BarcodeLookupResult();
                enrichBookWithOpenLibrary(barcode, fallback);
                return fallback;
            }
            BarcodeLookupResult result = new BarcodeLookupResult();
            result.found = true;
            String title = volumeInfo.optString("title", null);
            if (!TextUtils.isEmpty(title)) {
                result.title = title;
            }
            JSONArray authorsArray = volumeInfo.optJSONArray("authors");
            if (authorsArray != null && authorsArray.length() > 0) {
                List<String> authors = new ArrayList<>();
                for (int i = 0; i < authorsArray.length(); i++) {
                    String value = authorsArray.optString(i, null);
                    if (value != null) {
                        String trimmed = value.trim();
                        if (!trimmed.isEmpty()) {
                            authors.add(trimmed);
                        }
                    }
                }
                if (!authors.isEmpty()) {
                    result.author = TextUtils.join(", ", authors);
                }
            }
            String publisher = volumeInfo.optString("publisher", null);
            if (!TextUtils.isEmpty(publisher)) {
                result.publisher = publisher;
            }
            String publishedDate = volumeInfo.optString("publishedDate", null);
            if (!TextUtils.isEmpty(publishedDate)) {
                result.publishDate = publishedDate;
            }
            String description = resolveDescription(volumeInfo.opt("description"));
            if (!TextUtils.isEmpty(description)) {
                result.summary = description;
            }
            String subtitle = volumeInfo.optString("subtitle", null);
            if (!TextUtils.isEmpty(subtitle)) {
                String trimmedSubtitle = subtitle.trim();
                if (TextUtils.isEmpty(result.summary)) {
                    result.summary = trimmedSubtitle;
                }
                String loweredSubtitle = trimmedSubtitle.toLowerCase(Locale.getDefault());
                if (loweredSubtitle.contains("édition")
                        || loweredSubtitle.contains("edition")
                        || loweredSubtitle.contains("éd.")) {
                    result.edition = trimmedSubtitle;
                }
            }
            JSONObject seriesInfo = volumeInfo.optJSONObject("seriesInfo");
            if (seriesInfo != null) {
                String series = seriesInfo.optString("series", null);
                if (!TextUtils.isEmpty(series)) {
                    result.series = series;
                }
                String number = seriesInfo.optString("bookDisplayNumber", null);
                if (!TextUtils.isEmpty(number)) {
                    result.number = number;
                }
            }
            JSONArray categories = volumeInfo.optJSONArray("categories");
            boolean isComic = false;
            if (categories != null) {
                for (int i = 0; i < categories.length(); i++) {
                    String category = categories.optString(i, "");
                    String lowered = category.toLowerCase(Locale.getDefault());
                    if (lowered.contains("comic")
                            || lowered.contains("bande dessin")
                            || lowered.contains("manga")) {
                        isComic = true;
                        break;
                    }
                }
            }
            result.typeLabel = getString(isComic
                    ? R.string.dialog_type_comic
                    : R.string.dialog_type_book);
            JSONObject imageLinks = volumeInfo.optJSONObject("imageLinks");
            if (imageLinks != null) {
                String[] keys = new String[]{"extraLarge", "large", "medium", "thumbnail", "smallThumbnail"};
                for (String key : keys) {
                    String urlCandidate = imageLinks.optString(key, null);
                    if (urlCandidate == null || urlCandidate.trim().isEmpty()) {
                        continue;
                    }
                    String sanitizedUrl = urlCandidate.replace("http://", "https://");
                    String photo = downloadImageAsBase64(sanitizedUrl);
                    if (photo != null && !result.photos.contains(photo)) {
                        result.photos.add(photo);
                        if (result.photos.size() >= MAX_FORM_PHOTOS) {
                            break;
                        }
                    }
                }
            }
            enrichBookWithOpenLibrary(barcode, result);
            return result;
        } catch (IOException e) {
            BarcodeLookupResult error = new BarcodeLookupResult();
            error.errorMessage = getString(R.string.dialog_barcode_lookup_error);
            error.networkError = true;
            return error;
        } catch (JSONException e) {
            BarcodeLookupResult error = new BarcodeLookupResult();
            error.errorMessage = getString(R.string.dialog_barcode_lookup_error);
            return error;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void enrichBookWithOpenLibrary(@NonNull String isbn,
                                           @NonNull BarcodeLookupResult result) {
        HttpURLConnection connection = null;
        boolean hasData = result.found;
        try {
            String urlValue = "https://openlibrary.org/isbn/" +
                    URLEncoder.encode(isbn, StandardCharsets.UTF_8.name()) + ".json";
            URL url = new URL(urlValue);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "New1App/1.0 (Barcode lookup)");
            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();
            if (stream == null || responseCode >= 400) {
                return;
            }
            String response = readStream(stream);
            JSONObject root = new JSONObject(response);

            if (TextUtils.isEmpty(result.title)) {
                String title = root.optString("title", null);
                if (!TextUtils.isEmpty(title)) {
                    result.title = title;
                    hasData = true;
                }
            }

            if (TextUtils.isEmpty(result.publisher)) {
                JSONArray publishers = root.optJSONArray("publishers");
                if (publishers != null && publishers.length() > 0) {
                    Set<String> publisherSet = new LinkedHashSet<>();
                    for (int i = 0; i < publishers.length(); i++) {
                        String value = publishers.optString(i, null);
                        if (value == null) {
                            continue;
                        }
                        String trimmed = value.trim();
                        if (!trimmed.isEmpty()) {
                            publisherSet.add(trimmed);
                        }
                    }
                    if (!publisherSet.isEmpty()) {
                        result.publisher = TextUtils.join(", ", new ArrayList<>(publisherSet));
                        hasData = true;
                    }
                }
            }

            if (TextUtils.isEmpty(result.series)) {
                JSONArray seriesArray = root.optJSONArray("series");
                if (seriesArray != null && seriesArray.length() > 0) {
                    Set<String> seriesSet = new LinkedHashSet<>();
                    for (int i = 0; i < seriesArray.length(); i++) {
                        String value = seriesArray.optString(i, null);
                        if (value == null) {
                            continue;
                        }
                        String trimmed = value.trim();
                        if (!trimmed.isEmpty()) {
                            seriesSet.add(trimmed);
                        }
                    }
                    if (!seriesSet.isEmpty()) {
                        result.series = TextUtils.join(", ", new ArrayList<>(seriesSet));
                        hasData = true;
                    }
                }
            }

            if (TextUtils.isEmpty(result.number)) {
                String volume = root.optString("series_number", null);
                if (TextUtils.isEmpty(volume)) {
                    volume = root.optString("volume", null);
                }
                if (TextUtils.isEmpty(volume)) {
                    volume = root.optString("number", null);
                }
                if (!TextUtils.isEmpty(volume)) {
                    result.number = volume;
                    hasData = true;
                }
            }

            if (TextUtils.isEmpty(result.edition)) {
                String edition = root.optString("edition_name", null);
                if (!TextUtils.isEmpty(edition)) {
                    result.edition = edition;
                    hasData = true;
                }
            }

            if (TextUtils.isEmpty(result.publishDate)) {
                String publishDate = root.optString("publish_date", null);
                if (!TextUtils.isEmpty(publishDate)) {
                    result.publishDate = publishDate;
                    hasData = true;
                }
            }

            if (TextUtils.isEmpty(result.summary)) {
                String description = resolveDescription(root.opt("description"));
                if (TextUtils.isEmpty(description)) {
                    description = resolveDescription(root.opt("notes"));
                }
                if (!TextUtils.isEmpty(description)) {
                    result.summary = description;
                    hasData = true;
                }
            }

            if (TextUtils.isEmpty(result.author)) {
                JSONArray authors = root.optJSONArray("authors");
                if (authors != null && authors.length() > 0) {
                    Set<String> authorNames = new LinkedHashSet<>();
                    for (int i = 0; i < authors.length(); i++) {
                        JSONObject authorObject = authors.optJSONObject(i);
                        if (authorObject == null) {
                            continue;
                        }
                        String key = authorObject.optString("key", null);
                        String authorName = fetchOpenLibraryAuthorName(key);
                        if (!TextUtils.isEmpty(authorName)) {
                            authorNames.add(authorName);
                        }
                    }
                    if (!authorNames.isEmpty()) {
                        result.author = TextUtils.join(", ", new ArrayList<>(authorNames));
                        hasData = true;
                    }
                }
            }

            JSONArray covers = root.optJSONArray("covers");
            if (covers != null) {
                for (int i = 0; i < covers.length(); i++) {
                    if (result.photos.size() >= MAX_FORM_PHOTOS) {
                        break;
                    }
                    int coverId = covers.optInt(i, -1);
                    if (coverId <= 0) {
                        continue;
                    }
                    if (addOpenLibraryCoverPhoto(coverId, result.photos)) {
                        hasData = true;
                    }
                }
            }

            if (hasData) {
                result.found = true;
            }
        } catch (IOException | JSONException ignored) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Nullable
    private String fetchOpenLibraryAuthorName(@Nullable String authorKey) {
        if (authorKey == null) {
            return null;
        }
        String normalizedKey = authorKey.trim();
        if (normalizedKey.isEmpty()) {
            return null;
        }
        if (!normalizedKey.startsWith("/")) {
            normalizedKey = "/" + normalizedKey;
        }
        if (!normalizedKey.endsWith(".json")) {
            normalizedKey = normalizedKey + ".json";
        }
        HttpURLConnection connection = null;
        try {
            String urlValue = "https://openlibrary.org" + normalizedKey;
            URL url = new URL(urlValue);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "New1App/1.0 (Barcode lookup)");
            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                return null;
            }
            InputStream stream = connection.getInputStream();
            if (stream == null) {
                return null;
            }
            String response = readStream(stream);
            JSONObject authorObject = new JSONObject(response);
            String name = authorObject.optString("name", null);
            if (TextUtils.isEmpty(name)) {
                name = authorObject.optString("personal_name", null);
            }
            if (TextUtils.isEmpty(name)) {
                return null;
            }
            return name.trim();
        } catch (IOException | JSONException ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean addOpenLibraryCoverPhoto(int coverId, @NonNull List<String> destination) {
        if (coverId <= 0 || destination.size() >= MAX_FORM_PHOTOS) {
            return false;
        }
        String url = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
        String photo = downloadImageAsBase64(url);
        if (photo == null || destination.contains(photo)) {
            return false;
        }
        destination.add(photo);
        return true;
    }

    @Nullable
    private String resolveDescription(@Nullable Object descriptionValue) {
        if (descriptionValue instanceof JSONObject) {
            JSONObject object = (JSONObject) descriptionValue;
            String value = object.optString("value", null);
            return sanitizeDescription(value);
        }
        if (descriptionValue instanceof String) {
            return sanitizeDescription((String) descriptionValue);
        }
        return null;
    }

    @Nullable
    private String sanitizeDescription(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String withoutHtml = trimmed.replaceAll("<[^>]+>", "").replace("\r", "").trim();
        return withoutHtml.isEmpty() ? null : withoutHtml;
    }

    private boolean isIsbnCandidate(@NonNull String barcode) {
        String trimmed = barcode.replaceAll("\\s", "");
        if (trimmed.length() == 10) {
            return true;
        }
        if (trimmed.length() == 13) {
            return trimmed.startsWith("978") || trimmed.startsWith("979");
        }
        return false;
    }

    @Nullable
    private BarcodeLookupResult lookupMusic(@NonNull String barcode) {
        HttpURLConnection connection = null;
        try {
            String urlValue = "https://musicbrainz.org/ws/2/release/?query=barcode:" +
                    URLEncoder.encode(barcode, StandardCharsets.UTF_8.name()) +
                    "&fmt=json&limit=1";
            URL url = new URL(urlValue);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "New1App/1.0 (Barcode lookup)");
            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();
            if (stream == null) {
                BarcodeLookupResult error = new BarcodeLookupResult();
                error.errorMessage = getString(R.string.dialog_barcode_lookup_error);
                error.networkError = true;
                return error;
            }
            String response = readStream(stream);
            if (responseCode >= 400) {
                BarcodeLookupResult error = new BarcodeLookupResult();
                error.errorMessage = getString(R.string.dialog_barcode_lookup_error);
                error.networkError = true;
                return error;
            }
            JSONObject root = new JSONObject(response);
            JSONArray releases = root.optJSONArray("releases");
            if (releases == null || releases.length() == 0) {
                return new BarcodeLookupResult();
            }
            JSONObject release = releases.getJSONObject(0);
            BarcodeLookupResult result = new BarcodeLookupResult();
            result.found = true;
            String title = release.optString("title", null);
            if (!TextUtils.isEmpty(title)) {
                result.title = title;
            }
            JSONArray artistCredit = release.optJSONArray("artist-credit");
            if (artistCredit != null && artistCredit.length() > 0) {
                List<String> artists = new ArrayList<>();
                for (int i = 0; i < artistCredit.length(); i++) {
                    JSONObject credit = artistCredit.optJSONObject(i);
                    if (credit == null) {
                        continue;
                    }
                    String name = credit.optString("name", null);
                    if (!TextUtils.isEmpty(name)) {
                        artists.add(name.trim());
                        continue;
                    }
                    JSONObject artistObject = credit.optJSONObject("artist");
                    if (artistObject != null) {
                        String artistName = artistObject.optString("name", null);
                        if (!TextUtils.isEmpty(artistName)) {
                            artists.add(artistName.trim());
                        }
                    }
                }
                if (!artists.isEmpty()) {
                    result.author = TextUtils.join(", ", artists);
                }
            }
            JSONArray mediaArray = release.optJSONArray("media");
            boolean hasDiscFormat = false;
            boolean hasCdFormat = false;
            if (mediaArray != null) {
                for (int i = 0; i < mediaArray.length(); i++) {
                    JSONObject media = mediaArray.optJSONObject(i);
                    if (media == null) {
                        continue;
                    }
                    String format = media.optString("format", "");
                    String lowered = format.toLowerCase(Locale.getDefault());
                    if (lowered.contains("dvd") || lowered.contains("blu-ray") || lowered.contains("video")) {
                        hasDiscFormat = true;
                    }
                    if (lowered.contains("cd") || lowered.contains("sacd") || lowered.contains("audio")) {
                        hasCdFormat = true;
                    }
                }
            }
            if (hasDiscFormat && !hasCdFormat) {
                result.typeLabel = getString(R.string.dialog_type_disc);
            } else {
                result.typeLabel = getString(R.string.dialog_type_cd);
            }
            JSONArray labelInfo = release.optJSONArray("label-info");
            if (labelInfo != null) {
                for (int i = 0; i < labelInfo.length(); i++) {
                    JSONObject info = labelInfo.optJSONObject(i);
                    if (info == null) {
                        continue;
                    }
                    JSONObject label = info.optJSONObject("label");
                    if (label == null) {
                        continue;
                    }
                    String labelName = label.optString("name", null);
                    if (!TextUtils.isEmpty(labelName)) {
                        result.publisher = labelName;
                        break;
                    }
                }
            }
            String releaseId = release.optString("id", null);
            if (!TextUtils.isEmpty(releaseId)) {
                addCoverArtFromMusicBrainz(releaseId, result.photos);
            }
            return result;
        } catch (IOException e) {
            BarcodeLookupResult error = new BarcodeLookupResult();
            error.errorMessage = getString(R.string.dialog_barcode_lookup_error);
            error.networkError = true;
            return error;
        } catch (JSONException e) {
            BarcodeLookupResult error = new BarcodeLookupResult();
            error.errorMessage = getString(R.string.dialog_barcode_lookup_error);
            return error;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void addCoverArtFromMusicBrainz(@NonNull String releaseId,
                                            @NonNull List<String> destination) {
        if (destination.size() >= MAX_FORM_PHOTOS) {
            return;
        }
        String base = "https://coverartarchive.org/release/" + releaseId + "/";
        String[] paths = new String[]{"front-500", "front"};
        for (String path : paths) {
            if (destination.size() >= MAX_FORM_PHOTOS) {
                break;
            }
            String photo = downloadImageAsBase64(base + path);
            if (photo != null && !destination.contains(photo)) {
                destination.add(photo);
            }
        }
    }

    private void applyBarcodeLookupResult(@NonNull String barcode,
                                          @NonNull BarcodeScanContext context,
                                          @NonNull BarcodeLookupResult result) {
        if (currentFormState == null || currentFormState != context.formState) {
            barcodeScanContext = null;
            pendingBarcodeResult = null;
            return;
        }
        if (context.barcodeValueView != null) {
            context.barcodeValueView.setText(barcode);
        }
        if (result.errorMessage != null) {
            Toast.makeText(this, result.errorMessage, Toast.LENGTH_LONG).show();
            barcodeScanContext = null;
            pendingBarcodeResult = null;
            return;
        }
        if (!result.found) {
            String message = result.infoMessage != null
                    ? result.infoMessage
                    : getString(R.string.dialog_barcode_lookup_not_found, barcode);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            barcodeScanContext = null;
            pendingBarcodeResult = null;
            refreshPhotoSection(context.formState);
            return;
        }
        if (!TextUtils.isEmpty(result.title) && context.nameInput != null) {
            context.nameInput.setText(result.title);
            context.nameInput.setSelection(result.title.length());
        }
        if (!TextUtils.isEmpty(result.author) && context.authorInput != null) {
            context.authorInput.setText(result.author);
        }
        if (!TextUtils.isEmpty(result.publisher) && context.publisherInput != null) {
            context.publisherInput.setText(result.publisher);
        }
        if (!TextUtils.isEmpty(result.series) && context.seriesInput != null) {
            context.seriesInput.setText(result.series);
        }
        if (!TextUtils.isEmpty(result.number) && context.numberInput != null) {
            context.numberInput.setText(result.number);
        }
        if (!TextUtils.isEmpty(result.edition) && context.editionInput != null) {
            context.editionInput.setText(result.edition);
        }
        if (!TextUtils.isEmpty(result.publishDate) && context.publicationDateInput != null) {
            context.publicationDateInput.setText(result.publishDate);
        }
        if (!TextUtils.isEmpty(result.summary) && context.summaryInput != null) {
            context.summaryInput.setText(result.summary);
        }
        if (!TextUtils.isEmpty(result.typeLabel)) {
            context.selectedTypeHolder[0] = result.typeLabel;
            updateTypeSpecificFields(context.bookFields, context.trackFields, context.trackTitle,
                    result.typeLabel);
            updateSelectionButtonText(context.selectTypeButton, result.typeLabel,
                    R.string.dialog_button_choose_type);
        }
        boolean photosAdded = false;
        if (!result.photos.isEmpty()) {
            photosAdded = addPhotosToForm(context.formState, result.photos);
        }
        if (!photosAdded) {
            refreshPhotoSection(context.formState);
        }
        String message = result.infoMessage != null
                ? result.infoMessage
                : getString(R.string.dialog_barcode_lookup_success);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        barcodeScanContext = null;
        pendingBarcodeResult = null;
    }

    @NonNull
    private BarcodeScanContext createBarcodeScanContext(@NonNull FormState formState,
                                                        @Nullable EditText nameInput,
                                                        @Nullable TextView barcodeValueView,
                                                        @Nullable Button selectTypeButton,
                                                        @Nullable View bookFields,
                                                        @Nullable View trackFields,
                                                        @Nullable TextView trackTitle,
                                                        @NonNull String[] selectedTypeHolder,
                                                        @Nullable EditText seriesInput,
                                                        @Nullable EditText numberInput,
                                                        @Nullable EditText authorInput,
                                                        @Nullable EditText publisherInput,
                                                        @Nullable EditText editionInput,
                                                        @Nullable EditText publicationDateInput,
                                                        @Nullable EditText summaryInput) {
        return new BarcodeScanContext(formState,
                nameInput,
                barcodeValueView,
                selectTypeButton,
                bookFields,
                trackFields,
                trackTitle,
                selectedTypeHolder,
                seriesInput,
                numberInput,
                authorInput,
                publisherInput,
                editionInput,
                publicationDateInput,
                summaryInput);
    }

    private PendingBarcodeResult capturePendingBarcodeResult(boolean isEditing,
                                                             int positionToEdit,
                                                             @Nullable EditText nameInput,
                                                             @Nullable EditText commentInput,
                                                             @Nullable TextView barcodeValueView,
                                                             @NonNull String[] selectedTypeHolder,
                                                             @NonNull String[] selectedCategoryHolder,
                                                             @Nullable EditText seriesInput,
                                                             @Nullable EditText numberInput,
                                                             @Nullable EditText authorInput,
                                                             @Nullable EditText publisherInput,
                                                             @Nullable EditText editionInput,
                                                             @Nullable EditText publicationDateInput,
                                                             @Nullable EditText summaryInput,
                                                             @NonNull FormState formState) {
        PendingBarcodeResult result = new PendingBarcodeResult(isEditing, positionToEdit);
        result.name = extractText(nameInput);
        result.comment = extractText(commentInput);
        result.series = extractText(seriesInput);
        result.number = extractText(numberInput);
        result.author = extractText(authorInput);
        result.publisher = extractText(publisherInput);
        result.edition = extractText(editionInput);
        result.publicationDate = extractText(publicationDateInput);
        result.summary = extractText(summaryInput);
        result.selectedType = selectedTypeHolder[0];
        result.selectedCategory = selectedCategoryHolder[0];
        result.barcode = extractBarcodeValue(barcodeValueView);
        result.tracks.clear();
        result.tracks.addAll(collectTracks(formState));
        result.photos.addAll(formState.photos);
        result.resumeLookup = false;
        return result;
    }

    @Nullable
    private String extractText(@Nullable EditText input) {
        if (input == null) {
            return null;
        }
        CharSequence text = input.getText();
        if (text == null) {
            return null;
        }
        return text.toString();
    }

    @NonNull
    private List<String> collectTracks(@NonNull FormState formState) {
        List<String> result = new ArrayList<>();
        for (String track : formState.tracks) {
            if (track == null) {
                continue;
            }
            String trimmed = track.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    @Nullable
    private String extractBarcodeValue(@Nullable TextView barcodeValueView) {
        if (barcodeValueView == null) {
            return null;
        }
        CharSequence value = barcodeValueView.getText();
        if (value == null) {
            return null;
        }
        String trimmed = value.toString().trim();
        if (trimmed.isEmpty()
                || trimmed.equals(getString(R.string.dialog_label_barcode_placeholder))) {
            return null;
        }
        return trimmed;
    }

    private boolean addPhotosToForm(@NonNull FormState formState,
                                    @NonNull List<String> photos) {
        boolean added = false;
        for (String photo : photos) {
            if (photo == null || photo.trim().isEmpty()) {
                continue;
            }
            if (formState.photos.contains(photo)) {
                continue;
            }
            if (formState.photos.size() >= MAX_FORM_PHOTOS) {
                break;
            }
            formState.photos.add(photo);
            added = true;
        }
        if (added) {
            refreshPhotoSection(formState);
        }
        return added;
    }

    @NonNull
    private String readStream(@NonNull InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                StandardCharsets.UTF_8))) {
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
        }
        return builder.toString();
    }

    @Nullable
    private Bitmap downloadBitmap(@NonNull String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "New1App/1.0 (Barcode lookup)");
            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                return null;
            }
            try (InputStream stream = new BufferedInputStream(connection.getInputStream())) {
                return BitmapFactory.decodeStream(stream);
            }
        } catch (IOException ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Nullable
    private String downloadImageAsBase64(@NonNull String urlString) {
        Bitmap bitmap = downloadBitmap(urlString);
        if (bitmap == null) {
            return null;
        }
        return encodePhoto(bitmap);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        barcodeLookupExecutor.shutdownNow();
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
