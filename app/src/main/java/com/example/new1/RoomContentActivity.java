package com.example.new1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;
import androidx.core.widget.PopupWindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.new1.data.metadata.MetadataStorage;
import com.example.new1.RoomContentViewModel;
import com.example.new1.BarcodeLookupResult;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class RoomContentActivity extends AppCompatActivity {
    private static final String TAG = "RoomContentActivity";
    private static final int REQUEST_TAKE_PHOTO = 2001;
    private static final int MAX_FORM_PHOTOS = 5;

    private static final String FIELD_NAME = "name";
    private static final String FIELD_COMMENT = "comment";
    private static final String FIELD_PHOTOS = "photos";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_CUSTOM_1 = "custom_field_1";
    private static final String FIELD_CUSTOM_2 = "custom_field_2";

    private static final String DATE_FORMAT_EUROPEAN = "dd/MM/yyyy";
    private static final String DATE_FORMAT_ENGLISH = "MM/dd/yyyy";
    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd";

    private static final int GENERATED_BARCODE_LENGTH = 13;
    private static final SecureRandom BARCODE_RANDOM = new SecureRandom();

    public static final String EXTRA_ESTABLISHMENT_NAME = "extra_establishment_name";
    public static final String EXTRA_ROOM_NAME = "extra_room_name";

    private static final String STATE_SELECTION_MODE = "state_selection_mode";
    private static final String STATE_SELECTED_RANKS = "state_selected_ranks";
    private static final String STATE_ADD_MENU_VISIBLE = "state_add_menu_visible";
    private static final String STATE_ADD_MENU_ANCHOR_TYPE = "state_add_menu_anchor_type";
    private static final String STATE_ADD_MENU_HAS_FORCED_PARENT = "state_add_menu_has_forced_parent";
    private static final String STATE_ADD_MENU_FORCED_PARENT = "state_add_menu_forced_parent";
    private static final String STATE_ADD_MENU_HAS_FORCED_LEVEL = "state_add_menu_has_forced_level";
    private static final String STATE_ADD_MENU_FORCED_LEVEL = "state_add_menu_forced_level";
    private static final String STATE_ADD_MENU_INCLUDE_FURNITURE = "state_add_menu_include_furniture";
    private static final String STATE_ADD_MENU_INCLUDE_STORAGE_TOWER =
            "state_add_menu_include_storage_tower";
    private static final String STATE_ADD_MENU_HAS_TARGET_POSITION = "state_add_menu_has_target_position";
    private static final String STATE_ADD_MENU_TARGET_POSITION = "state_add_menu_target_position";
    private static final String STATE_ADD_MENU_HAS_FURNITURE_LEVEL = "state_add_menu_has_furniture_level";
    private static final String STATE_ADD_MENU_FURNITURE_LEVEL = "state_add_menu_furniture_level";
    private static final String STATE_CONTAINER_POPUP_POSITION = "state_container_popup_position";
    private static final String STATE_CONTAINER_POPUP_MASK = "state_container_popup_mask";
    private static final String STATE_FURNITURE_POPUP_POSITION = "state_furniture_popup_position";
    private static final String STATE_FURNITURE_POPUP_HAS_RANK = "state_furniture_popup_has_rank";
    private static final String STATE_FURNITURE_POPUP_RANK = "state_furniture_popup_rank";
    private static final String STATE_FURNITURE_POPUP_HAS_LEVEL = "state_furniture_popup_has_level";
    private static final String STATE_FURNITURE_POPUP_LEVEL = "state_furniture_popup_level";
    private static final String STATE_FURNITURE_POPUP_HAS_COLUMN = "state_furniture_popup_has_column";
    private static final String STATE_FURNITURE_POPUP_COLUMN = "state_furniture_popup_column";
    private static final String STATE_OPTIONS_POPUP_POSITION = "state_options_popup_position";
    private static final int ADD_MENU_ANCHOR_MAIN = 0;
    private static final int ADD_MENU_ANCHOR_FURNITURE_LEVEL = 1;
    private static final int ADD_MENU_ANCHOR_CONTAINER = 2;
    private static final float ACTION_DISABLED_ALPHA = 0.4f;
    private static final Pattern DIACRITICS_PATTERN =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

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
        @StringRes
        int photoLabelTemplateRes = R.string.dialog_label_room_content_photos_template;
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
        final ImageView barcodePreviewView;
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
        @Nullable
        final Long forcedParentRank;
        @Nullable
        final Integer forcedFurnitureLevel;

        DialogController(boolean isEditing,
                         int positionToEdit,
                         @Nullable EditText nameInput,
                         @Nullable EditText commentInput,
                         @Nullable TextView barcodeValueView,
                         @Nullable ImageView barcodePreviewView,
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
                         @NonNull AlertDialog dialog,
                         @Nullable Long forcedParentRank,
                         @Nullable Integer forcedFurnitureLevel) {
            this.isEditing = isEditing;
            this.positionToEdit = positionToEdit;
            this.nameInput = nameInput;
            this.commentInput = commentInput;
            this.barcodeValueView = barcodeValueView;
            this.barcodePreviewView = barcodePreviewView;
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
            this.forcedParentRank = forcedParentRank;
            this.forcedFurnitureLevel = forcedFurnitureLevel;
        }
    }

    private static final class AddMenuRestoreState {
        final int anchorType;
        @Nullable
        final Long forcedParentRank;
        @Nullable
        final Integer forcedFurnitureLevel;
        final boolean includeFurnitureOption;
        final boolean includeStorageTowerOption;
        @Nullable
        final Integer targetAdapterPosition;
        @Nullable
        final Integer furnitureLevelIndex;

        AddMenuRestoreState(int anchorType,
                @Nullable Long forcedParentRank,
                @Nullable Integer forcedFurnitureLevel,
                boolean includeFurnitureOption,
                boolean includeStorageTowerOption,
                @Nullable Integer targetAdapterPosition,
                @Nullable Integer furnitureLevelIndex) {
            this.anchorType = anchorType;
            this.forcedParentRank = forcedParentRank;
            this.forcedFurnitureLevel = forcedFurnitureLevel;
            this.includeFurnitureOption = includeFurnitureOption;
            this.includeStorageTowerOption = includeStorageTowerOption;
            this.targetAdapterPosition = targetAdapterPosition;
            this.furnitureLevelIndex = furnitureLevelIndex;
        }
    }

    private static class TypeFieldViews {
        @Nullable
        final TextView nameLabel;
        @Nullable
        final EditText nameInput;
        @Nullable
        final TextView commentLabel;
        @Nullable
        final EditText commentInput;
        @Nullable
        final TextView dateLabel;
        @Nullable
        final EditText dateInput;
        @Nullable
        final View customSection;
        @Nullable
        final View customDivider;
        @Nullable
        final View customFieldOne;
        @Nullable
        final View customFieldTwo;
        @Nullable
        final EditText customValueOne;
        @Nullable
        final EditText customValueTwo;

        TypeFieldViews(@Nullable TextView nameLabel,
                       @Nullable EditText nameInput,
                       @Nullable TextView commentLabel,
                       @Nullable EditText commentInput,
                       @Nullable TextView dateLabel,
                       @Nullable EditText dateInput,
                       @Nullable View customSection,
                       @Nullable View customDivider,
                       @Nullable View customFieldOne,
                       @Nullable View customFieldTwo,
                       @Nullable EditText customValueOne,
                       @Nullable EditText customValueTwo) {
            this.nameLabel = nameLabel;
            this.nameInput = nameInput;
            this.commentLabel = commentLabel;
            this.commentInput = commentInput;
            this.dateLabel = dateLabel;
            this.dateInput = dateInput;
            this.customSection = customSection;
            this.customDivider = customDivider;
            this.customFieldOne = customFieldOne;
            this.customFieldTwo = customFieldTwo;
            this.customValueOne = customValueOne;
            this.customValueTwo = customValueTwo;
        }
    }

    private static class TypeFieldOption {
        @NonNull
        final String key;
        @NonNull
        final String label;
        final boolean mandatory;

        TypeFieldOption(@NonNull String key, @NonNull String label, boolean mandatory) {
            this.key = key;
            this.label = label;
            this.mandatory = mandatory;
        }
    }

    private static class DateFormatOption {
        @NonNull
        final String key;
        @StringRes
        final int labelRes;

        DateFormatOption(@NonNull String key, @StringRes int labelRes) {
            this.key = key;
            this.labelRes = labelRes;
        }
    }

    private interface OnDateFormatSelectedListener {
        void onDateFormatSelected(@NonNull String formatKey);
    }

    private static class BarcodeScanContext {
        final FormState formState;
        @Nullable
        final EditText nameInput;
        @Nullable
        final TextView barcodeValueView;
        @Nullable
        final ImageView barcodePreviewView;
        @Nullable
        final ImageButton clearBarcodeButton;
        @Nullable
        final Button selectTypeButton;
        @Nullable
        final View bookFields;
        @Nullable
        final View trackFields;
        @Nullable
        final TextView trackTitle;
        final String[] selectedTypeHolder;
        final TypeFieldViews typeFieldViews;
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
                            @Nullable ImageView barcodePreviewView,
                            @Nullable ImageButton clearBarcodeButton,
                            @Nullable Button selectTypeButton,
                            @Nullable View bookFields,
                            @Nullable View trackFields,
                            @Nullable TextView trackTitle,
                            @NonNull String[] selectedTypeHolder,
                            @NonNull TypeFieldViews typeFieldViews,
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
            this.barcodePreviewView = barcodePreviewView;
            this.clearBarcodeButton = clearBarcodeButton;
            this.selectTypeButton = selectTypeButton;
            this.bookFields = bookFields;
            this.trackFields = trackFields;
            this.trackTitle = trackTitle;
            this.selectedTypeHolder = selectedTypeHolder;
            this.typeFieldViews = typeFieldViews;
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
        @Nullable
        Long forcedParentRank;
        @Nullable
        Integer forcedFurnitureLevel;
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

    @Nullable
    private String establishmentName;
    @Nullable
    private String roomName;

    private final List<RoomContentItem> roomContentItems = new ArrayList<>();
    private final Map<String, Set<String>> typeFieldConfigurations = new HashMap<>();
    private final Map<String, String> typeDateFormats = new HashMap<>();
    @Nullable
    private MetadataStorage metadataStorage;
    @Nullable
    private RecyclerView contentList;
    @Nullable
    private TextView placeholderView;
    @Nullable
    private RoomContentAdapter roomContentAdapter;
    @Nullable
    private ImageView searchButton;
    @Nullable
    private ImageView moveSelectedButton;
    @Nullable
    private ImageView deleteSelectedButton;
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
    @Nullable
    private PopupWindow addMenuPopup;
    @Nullable
    private AddMenuRestoreState currentAddMenuState;
    @Nullable
    private AddMenuRestoreState pendingAddMenuRestoreState;
    @Nullable
    private RoomContentAdapter.ContainerPopupRestoreState pendingContainerPopupState;
    @Nullable
    private RoomContentAdapter.FurniturePopupRestoreState pendingFurniturePopupState;
    @Nullable
    private RoomContentAdapter.OptionsPopupRestoreState pendingOptionsPopupState;
    private RoomContentViewModel roomContentViewModel;
    private boolean selectionModeEnabled;
    private boolean suppressFurnitureLevelSelectionCallbacks;
    private boolean suppressFurnitureColumnSelectionCallbacks;

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

        roomContentViewModel = new ViewModelProvider(
                this,
                RoomContentViewModel.provideFactory(getApplication())
        ).get(RoomContentViewModel.class);

        try {
            metadataStorage = new MetadataStorage(this);
        } catch (RuntimeException | UnsatisfiedLinkError exception) {
            Log.e(TAG, "Impossible d'initialiser le stockage des métadonnées.", exception);
            metadataStorage = null;
            Toast.makeText(this, R.string.room_content_metadata_error, Toast.LENGTH_LONG).show();
        }

        loadTypeFieldConfigurationsFromStorage();
        loadTypeDateFormatsFromStorage();
        ensureDefaultTypeConfigurations();

        ImageView backButton = findViewById(R.id.button_back);
        if (backButton != null) {
            backButton.setOnClickListener(view -> {
                if (selectionModeEnabled) {
                    exitSelectionMode();
                } else {
                    finish();
                }
            });
        }

        searchButton = findViewById(R.id.button_search);
        if (searchButton != null) {
            searchButton.setOnClickListener(view ->
                    EstablishmentSearchDialog.show(this, establishmentName));
        }

        moveSelectedButton = findViewById(R.id.button_move_selected);
        if (moveSelectedButton != null) {
            moveSelectedButton.setOnClickListener(view -> handleMoveSelection());
        }

        deleteSelectedButton = findViewById(R.id.button_delete_selected);
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setOnClickListener(view -> handleDeleteSelection());
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
                        public void onCopyRoomContent(@NonNull RoomContentItem item, int position) {
                            showCopyRoomContentDialog(item);
                        }

                        @Override
                        public void onMoveRoomContent(@NonNull RoomContentItem item, int position) {
                            showMoveRoomContentDialog(item, position);
                        }

                        @Override
                        public void onEditRoomContent(@NonNull RoomContentItem item, int position) {
                            showEditRoomContentDialog(item, position);
                        }

                        @Override
                        public void onDeleteRoomContent(@NonNull RoomContentItem item, int position) {
                            showDeleteRoomContentConfirmation(item, position);
                        }

                        @Override
                        public void onMoveRoomContentSelection(@NonNull List<RoomContentItem> items) {
                            showMoveRoomContentDialogForSelection(items);
                        }

                        @Override
                        public void onDeleteRoomContentSelection(@NonNull List<RoomContentItem> items) {
                            showDeleteRoomContentSelectionConfirmation(items);
                        }

                        @Override
                        public void onAddRoomContentToContainer(@NonNull RoomContentItem container,
                                                                  int position,
                                                                  @NonNull View anchor) {
                            showContainerAddMenu(anchor, container, position);
                        }

                        @Override
                        public void onAddRoomContentToFurnitureTop(@NonNull RoomContentItem furniture,
                                                                      int position,
                                                                      @NonNull View anchor) {
                            showFurnitureTopAddMenu(anchor, furniture);
                        }

                        @Override
                        public void onAddRoomContentToFurnitureLevel(@NonNull RoomContentItem furniture,
                                                                         int position,
                                                                         int level,
                                                                         @NonNull View anchor) {
                            showFurnitureLevelAddMenu(anchor, furniture, level);
                        }

                        @Override
                        public void onAddRoomContentToFurnitureBottom(@NonNull RoomContentItem furniture,
                                                                        int position,
                                                                        @NonNull View anchor) {
                            showFurnitureBottomAddMenu(anchor, furniture);
                        }

                        @Override
                        public void onAddBookToFurnitureLevel(@NonNull RoomContentItem furniture,
                                                               int position,
                                                               int level,
                                                               @NonNull View anchor) {
                            showFurnitureLevelAddBookDialog(anchor, furniture, level);
                        }

                        @Override
                        public void onRequestSelectionMode(@NonNull RoomContentItem item,
                                                            int position) {
                            enterSelectionMode();
                            if (roomContentAdapter != null) {
                                roomContentAdapter.selectItemAt(position);
                            }
                        }
                    });
            roomContentAdapter.setSelectionChangedListener(this::updateSelectionActions);
            contentList.setAdapter(roomContentAdapter);

            RecyclerView.ItemAnimator animator = contentList.getItemAnimator();
            if (animator != null) {
                animator.setAddDuration(0);
                animator.setRemoveDuration(0);
                animator.setMoveDuration(0);
                animator.setChangeDuration(0);
                if (animator instanceof SimpleItemAnimator) {
                    ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
                }
            }
        }

        loadRoomContent();
        updateEmptyState();

        View addButton = findViewById(R.id.button_add_room_content);
        if (addButton != null) {
            addButton.setOnClickListener(this::toggleAddRoomContentMenu);
        }

        restorePendingBarcodeResult(savedInstanceState);
        maybeRestorePendingDialog();

        if (savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_SELECTION_MODE, false)
                && roomContentAdapter != null) {
            restoreSelectionState(savedInstanceState);
        } else {
            refreshSelectionUi();
            updateSelectionActions(roomContentAdapter != null
                    ? roomContentAdapter.getSelectedItemCount()
                    : 0);
        }
        if (savedInstanceState != null) {
            restorePopupWindows(savedInstanceState);
        }
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
        if (savedInstanceState.getBoolean("pending_barcode_has_forced_parent_rank", false)) {
            restored.forcedParentRank = savedInstanceState.getLong(
                    "pending_barcode_forced_parent_rank");
        }
        if (savedInstanceState.getBoolean("pending_barcode_has_forced_furniture_level", false)) {
            restored.forcedFurnitureLevel = savedInstanceState.getInt(
                    "pending_barcode_forced_furniture_level");
        }
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
        showRoomContentDialog(itemToEdit, positionToEdit, pendingBarcodeResult.editing,
                pendingBarcodeResult.forcedParentRank,
                pendingBarcodeResult.forcedFurnitureLevel);
        if (pendingBarcodeResult != null) {
            pendingBarcodeResult.reopenDialog = false;
        }
    }

    private void restorePopupWindows(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState.getBoolean(STATE_ADD_MENU_VISIBLE, false)) {
            Long forcedParent = null;
            if (savedInstanceState.getBoolean(STATE_ADD_MENU_HAS_FORCED_PARENT, false)) {
                forcedParent = savedInstanceState.getLong(STATE_ADD_MENU_FORCED_PARENT);
            }
            Integer forcedLevel = null;
            if (savedInstanceState.getBoolean(STATE_ADD_MENU_HAS_FORCED_LEVEL, false)) {
                forcedLevel = savedInstanceState.getInt(STATE_ADD_MENU_FORCED_LEVEL);
            }
            boolean includeFurniture = savedInstanceState
                    .getBoolean(STATE_ADD_MENU_INCLUDE_FURNITURE, true);
            boolean includeStorageTower = savedInstanceState
                    .getBoolean(STATE_ADD_MENU_INCLUDE_STORAGE_TOWER, true);
            Integer storedPosition = null;
            if (savedInstanceState.getBoolean(STATE_ADD_MENU_HAS_TARGET_POSITION, false)) {
                storedPosition = savedInstanceState.getInt(STATE_ADD_MENU_TARGET_POSITION);
            }
            Integer storedLevel = null;
            if (savedInstanceState.getBoolean(STATE_ADD_MENU_HAS_FURNITURE_LEVEL, false)) {
                storedLevel = savedInstanceState.getInt(STATE_ADD_MENU_FURNITURE_LEVEL);
            }
            int anchorType = savedInstanceState.getInt(STATE_ADD_MENU_ANCHOR_TYPE,
                    ADD_MENU_ANCHOR_MAIN);
            pendingAddMenuRestoreState = new AddMenuRestoreState(anchorType,
                    forcedParent, forcedLevel, includeFurniture, includeStorageTower,
                    storedPosition, storedLevel);
        }
        if (savedInstanceState.containsKey(STATE_CONTAINER_POPUP_POSITION)) {
            int containerPosition = savedInstanceState.getInt(STATE_CONTAINER_POPUP_POSITION,
                    RecyclerView.NO_POSITION);
            if (containerPosition != RecyclerView.NO_POSITION) {
                int visibilityMask = savedInstanceState.getInt(STATE_CONTAINER_POPUP_MASK, 3);
                pendingContainerPopupState = new RoomContentAdapter.ContainerPopupRestoreState(
                        containerPosition, visibilityMask);
            }
        }
        if (savedInstanceState.containsKey(STATE_FURNITURE_POPUP_POSITION)) {
            int furniturePosition = savedInstanceState.getInt(STATE_FURNITURE_POPUP_POSITION,
                    RecyclerView.NO_POSITION);
            if (furniturePosition != RecyclerView.NO_POSITION) {
                long furnitureRank = -1L;
                if (savedInstanceState.getBoolean(STATE_FURNITURE_POPUP_HAS_RANK, false)) {
                    furnitureRank = savedInstanceState.getLong(STATE_FURNITURE_POPUP_RANK, -1L);
                }
                Integer level = null;
                if (savedInstanceState.getBoolean(STATE_FURNITURE_POPUP_HAS_LEVEL, false)) {
                    level = savedInstanceState.getInt(STATE_FURNITURE_POPUP_LEVEL);
                }
                Integer column = null;
                if (savedInstanceState.getBoolean(STATE_FURNITURE_POPUP_HAS_COLUMN, false)) {
                    column = savedInstanceState.getInt(STATE_FURNITURE_POPUP_COLUMN);
                }
                pendingFurniturePopupState = new RoomContentAdapter.FurniturePopupRestoreState(
                        furniturePosition, furnitureRank, level, column, false);
            }
        }
        if (savedInstanceState.containsKey(STATE_OPTIONS_POPUP_POSITION)) {
            int optionsPosition = savedInstanceState.getInt(STATE_OPTIONS_POPUP_POSITION,
                    RecyclerView.NO_POSITION);
            if (optionsPosition != RecyclerView.NO_POSITION) {
                pendingOptionsPopupState = new RoomContentAdapter.OptionsPopupRestoreState(
                        optionsPosition);
            }
        }
        if (pendingContainerPopupState != null || pendingFurniturePopupState != null
                || pendingOptionsPopupState != null || pendingAddMenuRestoreState != null) {
            schedulePendingPopupRestores();
        }
    }

    private void schedulePendingPopupRestores() {
        View target = contentList != null ? contentList : findViewById(android.R.id.content);
        if (target != null) {
            target.post(this::applyPendingPopupRestores);
        }
    }

    private void applyPendingPopupRestores() {
        RoomContentAdapter adapter = roomContentAdapter;
        RecyclerView list = contentList;
        if (adapter == null || list == null) {
            return;
        }
        if (pendingContainerPopupState != null) {
            adapter.restoreContainerPopup(list, pendingContainerPopupState);
            pendingContainerPopupState = null;
        }
        if (pendingFurniturePopupState != null) {
            adapter.restoreFurniturePopup(list, pendingFurniturePopupState);
            pendingFurniturePopupState = null;
        }
        if (pendingOptionsPopupState != null) {
            adapter.restoreOptionsPopup(list, pendingOptionsPopupState);
            pendingOptionsPopupState = null;
        }
        if (pendingAddMenuRestoreState != null) {
            AddMenuRestoreState state = pendingAddMenuRestoreState;
            pendingAddMenuRestoreState = null;
            restoreAddMenuPopup(state);
        }
    }

    private void restoreAddMenuPopup(@NonNull AddMenuRestoreState state) {
        if (state.anchorType == ADD_MENU_ANCHOR_MAIN) {
            View anchor = findViewById(R.id.button_add_room_content);
            if (anchor == null) {
                pendingAddMenuRestoreState = state;
                schedulePendingPopupRestores();
                return;
            }
            anchor.post(() -> showAddRoomContentMenu(anchor, state.forcedParentRank,
                    state.forcedFurnitureLevel, state.includeFurnitureOption,
                    state.includeStorageTowerOption, state.anchorType, null,
                    state.furnitureLevelIndex));
            return;
        }
        if (state.anchorType == ADD_MENU_ANCHOR_CONTAINER) {
            attemptRestoreContainerAddMenu(state, 0);
            return;
        }
        attemptRestoreFurnitureAddMenu(state, 0);
    }

    private void attemptRestoreFurnitureAddMenu(@NonNull AddMenuRestoreState state, int attempt) {
        if (roomContentAdapter == null || contentList == null) {
            pendingAddMenuRestoreState = state;
            schedulePendingPopupRestores();
            return;
        }
        int adapterPosition = state.targetAdapterPosition != null
                ? state.targetAdapterPosition
                : (state.forcedParentRank != null
                        ? findAdapterPositionForRank(state.forcedParentRank)
                        : RecyclerView.NO_POSITION);
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return;
        }
        RecyclerView.ViewHolder holder = contentList
                .findViewHolderForAdapterPosition(adapterPosition);
        if (!(holder instanceof RoomContentAdapter.ViewHolder)) {
            if (attempt >= 5) {
                return;
            }
            contentList.scrollToPosition(adapterPosition);
            contentList.postDelayed(() -> attemptRestoreFurnitureAddMenu(state, attempt + 1), 50);
            return;
        }
        RoomContentAdapter.ViewHolder viewHolder = (RoomContentAdapter.ViewHolder) holder;
        viewHolder.reopenFurniturePopup(state.furnitureLevelIndex, null);
        contentList.postDelayed(() -> {
            View anchor = viewHolder.findFurnitureAddAnchor(state.furnitureLevelIndex);
            if (anchor != null) {
                showAddRoomContentMenu(anchor, state.forcedParentRank,
                        state.forcedFurnitureLevel, state.includeFurnitureOption,
                        state.includeStorageTowerOption, state.anchorType,
                        adapterPosition, state.furnitureLevelIndex);
            } else if (attempt < 5) {
                attemptRestoreFurnitureAddMenu(state, attempt + 1);
            }
        }, 100);
    }

    private void attemptRestoreContainerAddMenu(@NonNull AddMenuRestoreState state, int attempt) {
        if (roomContentAdapter == null || contentList == null) {
            pendingAddMenuRestoreState = state;
            schedulePendingPopupRestores();
            return;
        }
        int adapterPosition = state.targetAdapterPosition != null
                ? state.targetAdapterPosition
                : (state.forcedParentRank != null
                        ? findAdapterPositionForRank(state.forcedParentRank)
                        : RecyclerView.NO_POSITION);
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return;
        }
        RecyclerView.ViewHolder holder = contentList
                .findViewHolderForAdapterPosition(adapterPosition);
        if (!(holder instanceof RoomContentAdapter.ViewHolder)) {
            if (attempt >= 5) {
                return;
            }
            contentList.scrollToPosition(adapterPosition);
            contentList.postDelayed(() -> attemptRestoreContainerAddMenu(state, attempt + 1), 50);
            return;
        }
        RoomContentAdapter.ViewHolder viewHolder = (RoomContentAdapter.ViewHolder) holder;
        View anchor = viewHolder.findContainerPopupAddAnchor();
        if (anchor != null) {
            showAddRoomContentMenu(anchor, state.forcedParentRank,
                    state.forcedFurnitureLevel, state.includeFurnitureOption,
                    state.includeStorageTowerOption, state.anchorType,
                    adapterPosition, state.furnitureLevelIndex);
        } else if (attempt < 5) {
            contentList.postDelayed(() -> attemptRestoreContainerAddMenu(state, attempt + 1), 50);
        }
    }

    private void enterSelectionMode() {
        if (selectionModeEnabled) {
            return;
        }
        selectionModeEnabled = true;
        if (roomContentAdapter != null) {
            roomContentAdapter.setSelectionModeEnabled(true);
        }
        refreshSelectionUi();
        updateSelectionActions(roomContentAdapter != null
                ? roomContentAdapter.getSelectedItemCount()
                : 0);
    }

    private void exitSelectionMode() {
        if (!selectionModeEnabled) {
            return;
        }
        selectionModeEnabled = false;
        if (roomContentAdapter != null) {
            roomContentAdapter.setSelectionModeEnabled(false);
        }
        refreshSelectionUi();
        applyTitle();
        applySubtitle();
    }

    private void refreshSelectionUi() {
        if (moveSelectedButton != null) {
            moveSelectedButton.setVisibility(selectionModeEnabled ? View.VISIBLE : View.GONE);
        }
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setVisibility(selectionModeEnabled ? View.VISIBLE : View.GONE);
        }
        if (searchButton != null) {
            searchButton.setVisibility(selectionModeEnabled ? View.GONE : View.VISIBLE);
        }
    }

    private void updateSelectionActions(int selectedCount) {
        boolean hasSelection = selectionModeEnabled && selectedCount > 0;
        if (moveSelectedButton != null) {
            moveSelectedButton.setEnabled(hasSelection);
            moveSelectedButton.setAlpha(hasSelection ? 1f : ACTION_DISABLED_ALPHA);
        }
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setEnabled(hasSelection);
            deleteSelectedButton.setAlpha(hasSelection ? 1f : ACTION_DISABLED_ALPHA);
        }
        if (selectionModeEnabled) {
            TextView titleView = findViewById(R.id.text_room_title);
            if (titleView != null) {
                if (hasSelection) {
                    titleView.setText(getResources().getQuantityString(
                            R.plurals.room_content_selection_count,
                            selectedCount,
                            selectedCount));
                } else {
                    titleView.setText(R.string.room_content_selection_title);
                }
            }
            TextView subtitleView = findViewById(R.id.text_room_subtitle);
            if (subtitleView != null) {
                subtitleView.setText(R.string.room_content_selection_subtitle);
            }
        }
    }

    private void handleMoveSelection() {
        if (!selectionModeEnabled || roomContentAdapter == null) {
            return;
        }
        List<RoomContentItem> selectedItems = roomContentAdapter.getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }
        showMoveRoomContentDialogForSelection(selectedItems);
    }

    private void handleDeleteSelection() {
        if (!selectionModeEnabled || roomContentAdapter == null) {
            return;
        }
        List<RoomContentItem> selectedItems = roomContentAdapter.getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }
        showDeleteRoomContentSelectionConfirmation(selectedItems);
    }

    private void restoreSelectionState(@NonNull Bundle savedInstanceState) {
        if (roomContentAdapter == null) {
            selectionModeEnabled = false;
            refreshSelectionUi();
            return;
        }
        long[] storedRanks = savedInstanceState.getLongArray(STATE_SELECTED_RANKS);
        Set<Long> ranks = new HashSet<>();
        if (storedRanks != null) {
            for (long rank : storedRanks) {
                ranks.add(rank);
            }
        }
        enterSelectionMode();
        if (!ranks.isEmpty()) {
            roomContentAdapter.restoreSelectionByRanks(ranks);
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
                    controller.formState,
                    controller.forcedParentRank,
                    controller.forcedFurnitureLevel);
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
            if (existing != null && snapshot.forcedParentRank == null) {
                snapshot.forcedParentRank = existing.forcedParentRank;
            }
            if (existing != null && snapshot.forcedFurnitureLevel == null) {
                snapshot.forcedFurnitureLevel = existing.forcedFurnitureLevel;
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
            if (pendingBarcodeResult.forcedParentRank != null) {
                outState.putBoolean("pending_barcode_has_forced_parent_rank", true);
                outState.putLong("pending_barcode_forced_parent_rank",
                        pendingBarcodeResult.forcedParentRank);
            } else {
                outState.putBoolean("pending_barcode_has_forced_parent_rank", false);
            }
            if (pendingBarcodeResult.forcedFurnitureLevel != null) {
                outState.putBoolean("pending_barcode_has_forced_furniture_level", true);
                outState.putInt("pending_barcode_forced_furniture_level",
                        pendingBarcodeResult.forcedFurnitureLevel);
            } else {
                outState.putBoolean("pending_barcode_has_forced_furniture_level", false);
            }
        }
        outState.putBoolean(STATE_SELECTION_MODE, selectionModeEnabled);
        if (selectionModeEnabled && roomContentAdapter != null) {
            List<Long> selectedRanks = roomContentAdapter.getSelectedItemRanks();
            outState.putLongArray(STATE_SELECTED_RANKS, toLongArray(selectedRanks));
        }
        if (addMenuPopup != null && addMenuPopup.isShowing() && currentAddMenuState != null) {
            outState.putBoolean(STATE_ADD_MENU_VISIBLE, true);
            outState.putInt(STATE_ADD_MENU_ANCHOR_TYPE, currentAddMenuState.anchorType);
            if (currentAddMenuState.forcedParentRank != null) {
                outState.putBoolean(STATE_ADD_MENU_HAS_FORCED_PARENT, true);
                outState.putLong(STATE_ADD_MENU_FORCED_PARENT,
                        currentAddMenuState.forcedParentRank);
            } else {
                outState.putBoolean(STATE_ADD_MENU_HAS_FORCED_PARENT, false);
            }
            if (currentAddMenuState.forcedFurnitureLevel != null) {
                outState.putBoolean(STATE_ADD_MENU_HAS_FORCED_LEVEL, true);
                outState.putInt(STATE_ADD_MENU_FORCED_LEVEL,
                        currentAddMenuState.forcedFurnitureLevel);
            } else {
                outState.putBoolean(STATE_ADD_MENU_HAS_FORCED_LEVEL, false);
            }
            outState.putBoolean(STATE_ADD_MENU_INCLUDE_FURNITURE,
                    currentAddMenuState.includeFurnitureOption);
            outState.putBoolean(STATE_ADD_MENU_INCLUDE_STORAGE_TOWER,
                    currentAddMenuState.includeStorageTowerOption);
            if (currentAddMenuState.targetAdapterPosition != null) {
                outState.putBoolean(STATE_ADD_MENU_HAS_TARGET_POSITION, true);
                outState.putInt(STATE_ADD_MENU_TARGET_POSITION,
                        currentAddMenuState.targetAdapterPosition);
            } else {
                outState.putBoolean(STATE_ADD_MENU_HAS_TARGET_POSITION, false);
            }
            if (currentAddMenuState.furnitureLevelIndex != null) {
                outState.putBoolean(STATE_ADD_MENU_HAS_FURNITURE_LEVEL, true);
                outState.putInt(STATE_ADD_MENU_FURNITURE_LEVEL,
                        currentAddMenuState.furnitureLevelIndex);
            } else {
                outState.putBoolean(STATE_ADD_MENU_HAS_FURNITURE_LEVEL, false);
            }
        } else {
            outState.putBoolean(STATE_ADD_MENU_VISIBLE, false);
        }
        if (roomContentAdapter != null) {
            RoomContentAdapter.ContainerPopupRestoreState containerState =
                    roomContentAdapter.captureActiveContainerPopupState();
            if (containerState != null) {
                outState.putInt(STATE_CONTAINER_POPUP_POSITION,
                        containerState.containerPosition);
                outState.putInt(STATE_CONTAINER_POPUP_MASK,
                        containerState.visibilityMask);
            }
            RoomContentAdapter.FurniturePopupRestoreState furnitureState =
                    roomContentAdapter.captureActiveFurniturePopupState();
            if (furnitureState != null) {
                outState.putInt(STATE_FURNITURE_POPUP_POSITION,
                        furnitureState.furniturePosition);
                if (furnitureState.furnitureRank >= 0) {
                    outState.putBoolean(STATE_FURNITURE_POPUP_HAS_RANK, true);
                    outState.putLong(STATE_FURNITURE_POPUP_RANK, furnitureState.furnitureRank);
                } else {
                    outState.putBoolean(STATE_FURNITURE_POPUP_HAS_RANK, false);
                }
                if (furnitureState.levelToExpand != null) {
                    outState.putBoolean(STATE_FURNITURE_POPUP_HAS_LEVEL, true);
                    outState.putInt(STATE_FURNITURE_POPUP_LEVEL,
                            furnitureState.levelToExpand);
                } else {
                    outState.putBoolean(STATE_FURNITURE_POPUP_HAS_LEVEL, false);
                }
                if (furnitureState.columnToDisplay != null) {
                    outState.putBoolean(STATE_FURNITURE_POPUP_HAS_COLUMN, true);
                    outState.putInt(STATE_FURNITURE_POPUP_COLUMN,
                            furnitureState.columnToDisplay);
                } else {
                    outState.putBoolean(STATE_FURNITURE_POPUP_HAS_COLUMN, false);
                }
            }
            RoomContentAdapter.OptionsPopupRestoreState optionsState =
                    roomContentAdapter.captureActiveOptionsPopupState();
            if (optionsState != null) {
                outState.putInt(STATE_OPTIONS_POPUP_POSITION, optionsState.adapterPosition);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (selectionModeEnabled) {
            exitSelectionMode();
            return;
        }
        super.onBackPressed();
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
        subtitleView.setVisibility(View.VISIBLE);
        if (trimmedEstablishment.isEmpty()) {
            subtitleView.setText(R.string.room_content_placeholder);
        } else {
            subtitleView.setText(getString(
                    R.string.room_content_subtitle_with_establishment,
                    trimmedEstablishment));
        }
    }

    private void showRoomContentDialog(@Nullable RoomContentItem initialItem,
            int positionToEdit,
            boolean shouldEditExisting,
            @Nullable Long forcedParentRank,
            @Nullable Integer forcedFurnitureLevel) {
        final boolean isEditing = shouldEditExisting
                && initialItem != null
                && positionToEdit >= 0
                && positionToEdit < roomContentItems.size();
        final RoomContentItem prefillItem = initialItem;
        View dialogView = inflateDialogView(R.layout.dialog_room_content_add);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        final RoomContentAdapter.ContainerPopupRestoreState restorePopupState = roomContentAdapter != null
                ? roomContentAdapter.consumePendingContainerPopupRestoreState()
                : null;
        final RoomContentAdapter.FurniturePopupRestoreState restoreFurnitureState = roomContentAdapter != null
                ? roomContentAdapter.consumePendingFurniturePopupRestoreState()
                : null;

        dialog.show();


        ScrollView dialogScrollView = dialogView.findViewById(R.id.scroll_container_dialog);
        if (dialogScrollView != null) {
            final int initialPaddingBottom = dialogScrollView.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(dialogScrollView, (view, insets) -> {
                int imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                int systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                int targetPaddingBottom = initialPaddingBottom + Math.max(imeInset, systemInset);
                if (view.getPaddingBottom() != targetPaddingBottom) {
                    view.setPadding(view.getPaddingLeft(),
                            view.getPaddingTop(),
                            view.getPaddingRight(),
                            targetPaddingBottom);
                }
                return insets;
            });
            ViewCompat.requestApplyInsets(dialogScrollView);
        }
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        }

        EditText nameInput = dialogView.findViewById(R.id.input_room_content_name);
        EditText commentInput = dialogView.findViewById(R.id.input_room_content_comment);
        TextView barcodeValueView = dialogView.findViewById(R.id.text_barcode_value);
        ImageView barcodePreviewView = dialogView.findViewById(R.id.image_barcode_preview);
        ImageButton clearBarcodeButton = dialogView.findViewById(R.id.button_clear_barcode);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button addPhotoButton = dialogView.findViewById(R.id.button_add_room_content_photo);
        LinearLayout photoContainer = dialogView.findViewById(R.id.container_room_content_photos);
        Button barcodeButton = dialogView.findViewById(R.id.button_barcode);
        Button generateBarcodeButton = dialogView.findViewById(R.id.button_generate_barcode);
        Button addTrackButton = dialogView.findViewById(R.id.button_add_track);
        Button addTrackListButton = dialogView.findViewById(R.id.button_add_track_list);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category);
        Button selectTypeButton = dialogView.findViewById(R.id.button_select_type);
        ImageButton openTypeListButton = dialogView.findViewById(R.id.button_open_type_list);
        ImageButton editCustomTypeButton = dialogView.findViewById(R.id.button_edit_custom_type);
        ImageButton deleteCustomTypeButton = dialogView.findViewById(R.id.button_delete_custom_type);
        ImageButton closeDialogButton = dialogView.findViewById(R.id.button_close_room_content_dialog);
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
        TextView nameLabel = dialogView.findViewById(R.id.label_room_content_name);
        TextView commentLabel = dialogView.findViewById(R.id.label_room_content_comment);
        TextView publicationDateLabel = dialogView.findViewById(R.id.label_publication_date);
        View customFieldsSection = dialogView.findViewById(R.id.container_custom_fields_section);
        View customFieldDivider = dialogView.findViewById(R.id.divider_custom_fields);
        View customFieldOne = dialogView.findViewById(R.id.container_custom_field_1);
        View customFieldTwo = dialogView.findViewById(R.id.container_custom_field_2);
        EditText customFieldValueOne = dialogView.findViewById(R.id.input_custom_field_value_1);
        EditText customFieldValueTwo = dialogView.findViewById(R.id.input_custom_field_value_2);
        TextView dialogTitle = dialogView.findViewById(R.id.text_dialog_room_content_title);
        if (closeDialogButton != null) {
            closeDialogButton.setOnClickListener(v -> dialog.dismiss());
        }
        final TypeFieldViews typeFieldViews = new TypeFieldViews(nameLabel,
                nameInput,
                commentLabel,
                commentInput,
                publicationDateLabel,
                publicationDateInput,
                customFieldsSection,
                customFieldDivider,
                customFieldOne,
                customFieldTwo,
                customFieldValueOne,
                customFieldValueTwo);
        PendingBarcodeResult restoreData = pendingBarcodeResult != null
                && pendingBarcodeResult.matches(isEditing, positionToEdit)
                ? pendingBarcodeResult
                : null;

        final Long appliedForcedParentRank;
        final Integer appliedForcedFurnitureLevel;
        if (isEditing) {
            appliedForcedParentRank = null;
            appliedForcedFurnitureLevel = null;
        } else if (restoreData != null && restoreData.forcedParentRank != null) {
            appliedForcedParentRank = restoreData.forcedParentRank;
            appliedForcedFurnitureLevel = restoreData.forcedFurnitureLevel;
        } else {
            appliedForcedParentRank = forcedParentRank;
            appliedForcedFurnitureLevel = forcedFurnitureLevel;
        }

        if (pendingBarcodeResult != null
                && pendingBarcodeResult.matches(isEditing, positionToEdit)) {
            pendingBarcodeResult.reopenDialog = false;
        }

        if (dialogTitle != null) {
            dialogTitle.setText(isEditing
                    ? R.string.dialog_edit_room_content_title
                    : R.string.dialog_add_room_content_title);
        }

        String initialBarcode = null;
        if (restoreData != null) {
            initialBarcode = restoreData.barcode;
        } else if (prefillItem != null) {
            initialBarcode = prefillItem.getBarcode();
        }

        if (prefillItem != null) {
            if (nameInput != null) {
                String name = prefillItem.getName();
                nameInput.setText(name);
                if (!TextUtils.isEmpty(name)) {
                    nameInput.setSelection(name.length());
                }
            }
            if (commentInput != null) {
                commentInput.setText(prefillItem.getComment());
            }
            if (seriesInput != null) {
                String series = prefillItem.getSeries();
                seriesInput.setText(series);
                if (series != null) {
                    seriesInput.setSelection(series.length());
                }
            }
            if (numberInput != null) {
                String number = prefillItem.getNumber();
                numberInput.setText(number);
                if (number != null) {
                    numberInput.setSelection(number.length());
                }
            }
            if (authorInput != null) {
                String author = prefillItem.getAuthor();
                authorInput.setText(author);
                if (author != null) {
                    authorInput.setSelection(author.length());
                }
            }
            if (publisherInput != null) {
                String publisher = prefillItem.getPublisher();
                publisherInput.setText(publisher);
                if (publisher != null) {
                    publisherInput.setSelection(publisher.length());
                }
            }
            if (editionInput != null) {
                String edition = prefillItem.getEdition();
                editionInput.setText(edition);
                if (edition != null) {
                    editionInput.setSelection(edition.length());
                }
            }
            if (publicationDateInput != null) {
                String publicationDate = prefillItem.getPublicationDate();
                publicationDateInput.setText(publicationDate);
                if (publicationDate != null) {
                    publicationDateInput.setSelection(publicationDate.length());
                }
            }
            if (summaryInput != null) {
                String summary = prefillItem.getSummary();
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
        }

        if (clearBarcodeButton != null) {
            clearBarcodeButton.setOnClickListener(v ->
                    clearBarcodeValue(barcodeValueView, barcodePreviewView, clearBarcodeButton));
        }

        bindBarcodeValue(barcodeValueView, barcodePreviewView, clearBarcodeButton, initialBarcode);

        final FormState formState = new FormState();
        formState.photoLabel = dialogView.findViewById(R.id.text_room_content_photos_label);
        formState.photoContainer = photoContainer;
        formState.addPhotoButton = addPhotoButton;
        formState.trackContainer = trackListContainer;
        currentFormState = formState;
        if (prefillItem != null) {
            formState.photos.addAll(prefillItem.getPhotos());
            formState.tracks.addAll(prefillItem.getTracks());
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
        final Set<String> lockedTypes = new LinkedHashSet<>();

        String otherTypeDefault = getString(R.string.dialog_type_other);
        if (!containsIgnoreCase(typeOptions, otherTypeDefault)) {
            typeOptions.add(otherTypeDefault);
        }
        lockedTypes.add(otherTypeDefault);

        String keyTypeDefault = getString(R.string.dialog_type_key);
        if (!containsIgnoreCase(typeOptions, keyTypeDefault)) {
            typeOptions.add(keyTypeDefault);
        }
        lockedTypes.add(keyTypeDefault);

        String bookTypeDefault = getString(R.string.dialog_type_book);
        if (!containsIgnoreCase(typeOptions, bookTypeDefault)) {
            typeOptions.add(bookTypeDefault);
        }
        lockedTypes.add(bookTypeDefault);

        String cdTypeDefault = getString(R.string.dialog_type_cd);
        if (!containsIgnoreCase(typeOptions, cdTypeDefault)) {
            typeOptions.add(cdTypeDefault);
        }
        lockedTypes.add(cdTypeDefault);

        String discTypeDefault = getString(R.string.dialog_type_disc);
        if (!containsIgnoreCase(typeOptions, discTypeDefault)) {
            typeOptions.add(discTypeDefault);
        }
        lockedTypes.add(discTypeDefault);

        String comicTypeDefault = getString(R.string.dialog_type_comic);
        if (!containsIgnoreCase(typeOptions, comicTypeDefault)) {
            typeOptions.add(comicTypeDefault);
        }
        lockedTypes.add(comicTypeDefault);

        String magazineTypeDefault = getString(R.string.dialog_type_magazine);
        if (!containsIgnoreCase(typeOptions, magazineTypeDefault)) {
            typeOptions.add(magazineTypeDefault);
        }
        lockedTypes.add(magazineTypeDefault);
        if (prefillItem != null) {
            String existingType = prefillItem.getType();
            if (existingType != null && !existingType.trim().isEmpty()
                    && !typeOptions.contains(existingType)) {
                typeOptions.add(existingType);
            }
        }
        final String[] selectedTypeHolder = new String[1];
        if (prefillItem != null) {
            String existingType = prefillItem.getType();
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
        List<String> storedCategories = loadStoredCategories();
        for (String value : storedCategories) {
            if (!containsIgnoreCase(categoryOptions, value)) {
                categoryOptions.add(value);
            }
        }
        if (prefillItem != null) {
            String existingCategory = prefillItem.getCategory();
            if (existingCategory != null && !existingCategory.trim().isEmpty()
                    && !categoryOptions.contains(existingCategory)) {
                categoryOptions.add(existingCategory);
            }
        }
        final String[] selectedCategoryHolder = new String[1];
        if (prefillItem != null) {
            String existingCategory = prefillItem.getCategory();
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

        final AtomicReference<ArrayAdapter<String>> categoryAdapterRef = new AtomicReference<>();

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                Set<String> selectedFields = resolveFieldsForType(selectedTypeHolder[0]);
                String trimmedName = "";
                if (nameInput != null) {
                    CharSequence nameValue = nameInput.getText();
                    trimmedName = nameValue != null ? nameValue.toString().trim() : "";
                    if (selectedFields.contains(FIELD_NAME) && trimmedName.isEmpty()) {
                        nameInput.setError(getString(R.string.error_room_content_name_required));
                        nameInput.requestFocus();
                        return;
                    }
                }
                String trimmedComment = "";
                if (commentInput != null && selectedFields.contains(FIELD_COMMENT)) {
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
                List<String> photoValues = selectedFields.contains(FIELD_PHOTOS)
                        ? new ArrayList<>(formState.photos)
                        : new ArrayList<>();

                Set<RoomContentItem> containersToRefresh = new LinkedHashSet<>();

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
                        photoValues,
                        false);
                RoomContentItem targetContainer = null;
                if (!isEditing) {
                    targetContainer = appliedForcedParentRank != null
                            ? findContainerByRank(roomContentItems, appliedForcedParentRank)
                            : null;
                    RoomContentHierarchyHelper.attachToContainer(item, targetContainer);
                    updateFurniturePlacement(item, targetContainer, appliedForcedFurnitureLevel,
                            null);
                    if (targetContainer != null) {
                        containersToRefresh.add(targetContainer);
                    }
                }
                if (isEditing) {
                    if (positionToEdit < 0 || positionToEdit >= roomContentItems.size()) {
                        dialog.dismiss();
                        return;
                    }
                    RoomContentItem existingItem = roomContentItems.get(positionToEdit);
                    preserveHierarchyMetadata(existingItem, item);
                    roomContentItems.set(positionToEdit, item);
                } else {
                    if (targetContainer != null) {
                        int insertionIndex = findInsertionIndexForContainer(targetContainer);
                        roomContentItems.add(insertionIndex, item);
                    } else {
                        roomContentItems.add(item);
                    }
                }
                RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
                sortRoomContentItems();
                RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
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
                scheduleContainerIndicatorRefresh(containersToRefresh);
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
            if (restorePopupState != null && roomContentAdapter != null && contentList != null) {
                roomContentAdapter.restoreContainerPopup(contentList, restorePopupState);
            }
            if (restoreFurnitureState != null && roomContentAdapter != null && contentList != null) {
                roomContentAdapter.restoreFurniturePopup(contentList, restoreFurnitureState);
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

        if (generateBarcodeButton != null) {
            generateBarcodeButton.setOnClickListener(v -> {
                if (currentFormState == null || currentFormState != formState) {
                    return;
                }
                showBarcodeCreationDialog(barcodeValueView, barcodePreviewView, clearBarcodeButton);
            });
        }

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
                        formState,
                        appliedForcedParentRank,
                        appliedForcedFurnitureLevel);
                barcodeScanContext = createBarcodeScanContext(formState,
                        nameInput,
                        barcodeValueView,
                        barcodePreviewView,
                        clearBarcodeButton,
                        selectTypeButton,
                        bookFields,
                        trackFields,
                        trackTitle,
                        selectedTypeHolder,
                        typeFieldViews,
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
            addTrackListButton.setOnClickListener(v -> {
                if (currentFormState == null || currentFormState != formState) {
                    return;
                }
                showTrackListDialog(formState);
            });
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
            categoryAdapterRef.set(categoryAdapter);
        }

        applyTypeConfiguration(selectedTypeHolder[0], formState, bookFields, trackFields,
                trackTitle, typeFieldViews);
        updateSelectionButtonText(selectTypeButton, selectedTypeHolder[0],
                R.string.dialog_button_choose_type);
        updateSelectionButtonText(selectCategoryButton, selectedCategoryHolder[0],
                R.string.dialog_button_choose_category);

        View.OnClickListener typeDialogLauncher = v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            prepareBottomSheetDialog(bottomSheetDialog);
            View sheetView = inflateDialogView(R.layout.dialog_type_selector);
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
                                applyTypeConfiguration(type, formState, bookFields, trackFields,
                                        trackTitle, typeFieldViews);
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
                                        formState,
                                        bookFields,
                                        trackFields,
                                        trackTitle,
                                        lockedTypes,
                                        typeFieldViews);
                            }

                            @Override
                            public void onDeleteType(String type, int position) {
                                showDeleteTypeConfirmation(typeOptions,
                                        adapterHolder[0],
                                        position,
                                        selectTypeButton,
                                        selectedTypeHolder,
                                        formState,
                                        bookFields,
                                        trackFields,
                                        trackTitle,
                                        lockedTypes,
                                        typeFieldViews);
                            }
                        }, lockedTypes);
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
            prepareBottomSheetDialog(bottomSheetDialog);
            View sheetView = inflateDialogView(R.layout.dialog_category_selector);
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
                                        categoryAdapterRef.get(),
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
                                        categoryAdapterRef.get(),
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
                                categoryAdapterRef.get(),
                                selectCategoryButton,
                                selectedCategoryHolder,
                                categorySpinner));
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
                barcodePreviewView,
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
                dialog,
                appliedForcedParentRank,
                appliedForcedFurnitureLevel);

        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        if (restoreData != null && restoreData.resumeLookup
                && !TextUtils.isEmpty(restoreData.barcode)) {
            barcodeScanContext = createBarcodeScanContext(formState,
                    nameInput,
                    barcodeValueView,
                    barcodePreviewView,
                    clearBarcodeButton,
                    selectTypeButton,
                    bookFields,
                    trackFields,
                    trackTitle,
                    selectedTypeHolder,
                    typeFieldViews,
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

    private void showContainerDialog(@Nullable RoomContentItem itemToEdit,
            int positionToEdit,
            @Nullable Long forcedParentRank,
            @Nullable Integer forcedFurnitureLevel) {
        RoomContentItem containerToEdit = itemToEdit != null && itemToEdit.isContainer()
                ? itemToEdit
                : null;
        final boolean isEditing = containerToEdit != null
                && positionToEdit >= 0
                && positionToEdit < roomContentItems.size();

        View dialogView = inflateDialogView(R.layout.dialog_room_container_add);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        final RoomContentAdapter.ContainerPopupRestoreState restorePopupState = roomContentAdapter != null
                ? roomContentAdapter.consumePendingContainerPopupRestoreState()
                : null;
        final RoomContentAdapter.FurniturePopupRestoreState restoreFurnitureState = roomContentAdapter != null
                ? roomContentAdapter.consumePendingFurniturePopupRestoreState()
                : null;

        dialog.show();

        ScrollView containerScrollView = dialogView.findViewById(R.id.scroll_container_dialog);
        if (containerScrollView != null) {
            final int initialPaddingBottom = containerScrollView.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(containerScrollView, (view, insets) -> {
                int imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                int systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                int targetPaddingBottom = initialPaddingBottom + Math.max(imeInset, systemInset);
                if (view.getPaddingBottom() != targetPaddingBottom) {
                    view.setPadding(view.getPaddingLeft(),
                            view.getPaddingTop(),
                            view.getPaddingRight(),
                            targetPaddingBottom);
                }
                return insets;
            });
            ViewCompat.requestApplyInsets(containerScrollView);
        }

        TextView titleView = dialogView.findViewById(R.id.text_dialog_container_title);
        TextView nameLabel = dialogView.findViewById(R.id.label_container_name);
        EditText nameInput = dialogView.findViewById(R.id.input_container_name);
        TextView commentLabel = dialogView.findViewById(R.id.label_container_comment);
        EditText commentInput = dialogView.findViewById(R.id.input_container_comment);
        Button selectTypeButton = dialogView.findViewById(R.id.button_select_container_type);
        ImageButton openTypeListButton = dialogView.findViewById(R.id.button_open_container_type_list);
        Button addPhotoButton = dialogView.findViewById(R.id.button_add_container_photo);
        LinearLayout photoContainer = dialogView.findViewById(R.id.container_container_photos);
        TextView photoLabel = dialogView.findViewById(R.id.text_container_photos_label);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);
        ImageButton closeDialogButton = dialogView.findViewById(R.id.button_close_container_dialog);

        if (titleView != null) {
            titleView.setText(isEditing
                    ? R.string.dialog_edit_container_title
                    : R.string.dialog_add_container_title);
        }

        if (closeDialogButton != null) {
            closeDialogButton.setOnClickListener(v -> dialog.dismiss());
        }

        final FormState formState = new FormState();
        formState.photoLabel = photoLabel;
        formState.photoContainer = photoContainer;
        formState.addPhotoButton = addPhotoButton;
        formState.photoLabelTemplateRes = R.string.dialog_label_container_photos_template;
        currentFormState = formState;

        final TypeFieldViews typeFieldViews = new TypeFieldViews(nameLabel,
                nameInput,
                commentLabel,
                commentInput,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        if (containerToEdit != null) {
            if (nameInput != null) {
                String name = containerToEdit.getName();
                nameInput.setText(name);
                if (name != null) {
                    nameInput.setSelection(name.length());
                }
            }
            if (commentInput != null) {
                String comment = containerToEdit.getComment();
                commentInput.setText(comment);
                if (comment != null) {
                    commentInput.setSelection(comment.length());
                }
            }
            formState.photos.addAll(containerToEdit.getPhotos());
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

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        List<String> typeOptions = new ArrayList<>();
        Set<String> lockedTypes = new LinkedHashSet<>();
        String[] defaults = getResources().getStringArray(R.array.room_container_type_defaults);
        for (String value : defaults) {
            if (value != null && !value.trim().isEmpty()) {
                typeOptions.add(value);
                lockedTypes.add(value);
            }
        }

        final String[] selectedTypeHolder = new String[1];
        if (containerToEdit != null && !TextUtils.isEmpty(containerToEdit.getType())) {
            String existingType = containerToEdit.getType();
            if (!containsIgnoreCase(typeOptions, existingType)) {
                typeOptions.add(existingType);
            }
            selectedTypeHolder[0] = existingType;
        } else {
            selectedTypeHolder[0] = !typeOptions.isEmpty() ? typeOptions.get(0) : null;
        }

        updateSelectionButtonText(selectTypeButton, selectedTypeHolder[0],
                R.string.dialog_button_choose_container_type);
        applyTypeConfiguration(selectedTypeHolder[0], formState, null, null, null,
                typeFieldViews);

        View.OnClickListener typeDialogLauncher = v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            prepareBottomSheetDialog(bottomSheetDialog);
            View sheetView = inflateDialogView(R.layout.dialog_type_selector);
            bottomSheetDialog.setContentView(sheetView);

            if (sheetView != null) {
                sheetView.setBackgroundResource(R.drawable.bg_room_container_popup);
                TextView sheetTitle = sheetView.findViewById(R.id.text_type_selector_title);
                if (sheetTitle != null) {
                    sheetTitle.setText(R.string.dialog_container_type_selector_title);
                }
            }

            RecyclerView recyclerView = sheetView.findViewById(R.id.recycler_type_options);
            Button addTypeButtonSheet = sheetView.findViewById(R.id.button_add_type);
            final TypeSelectorAdapter[] adapterHolder = new TypeSelectorAdapter[1];

            if (recyclerView != null) {
                TypeSelectorAdapter adapter = new TypeSelectorAdapter(typeOptions,
                        new TypeSelectorAdapter.TypeActionListener() {
                            @Override
                            public void onTypeSelected(String type) {
                                selectedTypeHolder[0] = type;
                                applyTypeConfiguration(type, formState, null, null, null,
                                        typeFieldViews);
                                updateSelectionButtonText(selectTypeButton, type,
                                        R.string.dialog_button_choose_container_type);
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
                                        formState,
                                        null,
                                        null,
                                        null,
                                        lockedTypes,
                                        typeFieldViews);
                            }

                            @Override
                            public void onDeleteType(String type, int position) {
                                showDeleteTypeConfirmation(typeOptions,
                                        adapterHolder[0],
                                        position,
                                        selectTypeButton,
                                        selectedTypeHolder,
                                        formState,
                                        null,
                                        null,
                                        null,
                                        lockedTypes,
                                        typeFieldViews);
                            }
                        },
                        lockedTypes);
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

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                if (nameInput == null) {
                    dialog.dismiss();
                    return;
                }
                Set<String> selectedFields = resolveFieldsForType(selectedTypeHolder[0]);
                CharSequence nameValue = nameInput.getText();
                String trimmedName = nameValue != null ? nameValue.toString().trim() : "";
                if (selectedFields.contains(FIELD_NAME) && trimmedName.isEmpty()) {
                    nameInput.setError(getString(R.string.error_room_content_name_required));
                    nameInput.requestFocus();
                    return;
                }
                String trimmedComment = "";
                if (commentInput != null && selectedFields.contains(FIELD_COMMENT)) {
                    CharSequence commentValue = commentInput.getText();
                    trimmedComment = commentValue != null ? commentValue.toString().trim() : "";
                }
                String selectedType = selectedTypeHolder[0];
                if (TextUtils.isEmpty(selectedType) && !typeOptions.isEmpty()) {
                    selectedType = typeOptions.get(0);
                }
                if (currentFormState != null && currentFormState != formState) {
                    dialog.dismiss();
                    return;
                }
                List<String> photoValues = selectedFields.contains(FIELD_PHOTOS)
                        ? new ArrayList<>(formState.photos)
                        : new ArrayList<>();

                final boolean copyingContainer = !isEditing && containerToEdit != null;
                RoomContentItem newItem = new RoomContentItem(trimmedName,
                        trimmedComment,
                        selectedType,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        photoValues,
                        true,
                        0);
                Set<RoomContentItem> containersToRefresh = new LinkedHashSet<>();
                RoomContentItem targetContainer = null;
                if (isEditing) {
                    if (positionToEdit < 0 || positionToEdit >= roomContentItems.size()) {
                        dialog.dismiss();
                        return;
                    }
                    RoomContentItem existingItem = roomContentItems.get(positionToEdit);
                    preserveHierarchyMetadata(existingItem, newItem);
                    roomContentItems.set(positionToEdit, newItem);
                } else {
                    targetContainer = forcedParentRank != null
                            ? findContainerByRank(roomContentItems, forcedParentRank)
                            : null;
                    RoomContentHierarchyHelper.attachToContainer(newItem, targetContainer);
                    updateFurniturePlacement(newItem, targetContainer, forcedFurnitureLevel, null);
                    if (targetContainer != null) {
                        int insertionIndex = findInsertionIndexForContainer(targetContainer);
                        roomContentItems.add(insertionIndex, newItem);
                        containersToRefresh.add(targetContainer);
                    } else {
                        roomContentItems.add(newItem);
                    }
                    if (copyingContainer) {
                        RoomContentHierarchyHelper.ensureRanks(roomContentItems);
                        duplicateContainerContents(containerToEdit, newItem, containersToRefresh);
                    }
                }
                RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
                sortRoomContentItems();
                RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
                if (roomContentAdapter != null) {
                    roomContentAdapter.notifyDataSetChanged();
                }
                if (contentList != null) {
                    int targetPosition = roomContentItems.indexOf(newItem);
                    if (targetPosition >= 0) {
                        final int scrollPosition = targetPosition;
                        contentList.post(() -> contentList.smoothScrollToPosition(scrollPosition));
                    }
                }
                saveRoomContent();
                updateEmptyState();
                scheduleContainerIndicatorRefresh(containersToRefresh);
                if (copyingContainer) {
                    Toast.makeText(this,
                            R.string.room_container_copied_confirmation,
                            Toast.LENGTH_LONG)
                            .show();
                } else {
                    int messageRes = isEditing
                            ? R.string.room_container_updated_confirmation
                            : R.string.room_container_added_confirmation;
                    Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
                }
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

        dialog.setOnDismissListener(d -> {
            if (currentFormState == formState) {
                currentFormState = null;
            }
            if (restorePopupState != null && roomContentAdapter != null && contentList != null) {
                roomContentAdapter.restoreContainerPopup(contentList, restorePopupState);
            }
            if (restoreFurnitureState != null && roomContentAdapter != null && contentList != null) {
                roomContentAdapter.restoreFurniturePopup(contentList, restoreFurnitureState);
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    private void showStorageTowerDialog(@Nullable RoomContentItem itemToEdit, int positionToEdit) {
        boolean isEditing = itemToEdit != null && positionToEdit >= 0;
        View dialogView = inflateDialogView(R.layout.dialog_add_storage_tower);
        if (dialogView == null) {
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        final RoomContentAdapter.ContainerPopupRestoreState restorePopupState = roomContentAdapter != null
                ? roomContentAdapter.consumePendingContainerPopupRestoreState()
                : null;
        final RoomContentAdapter.FurniturePopupRestoreState restoreFurnitureState = roomContentAdapter != null
                ? roomContentAdapter.consumePendingFurniturePopupRestoreState()
                : null;
        dialog.show();

        TextView titleView = dialogView.findViewById(R.id.text_dialog_storage_tower_title);
        Spinner typeSpinner = dialogView.findViewById(R.id.spinner_storage_tower_type);
        EditText customTypeInput = dialogView.findViewById(R.id.input_storage_tower_custom_type);
        EditText nameInput = dialogView.findViewById(R.id.input_storage_tower_name);
        EditText commentInput = dialogView.findViewById(R.id.input_storage_tower_comment);
        TextView photoLabel = dialogView.findViewById(R.id.text_storage_tower_photos_label);
        Button addPhotoButton = dialogView.findViewById(R.id.button_add_storage_tower_photo);
        LinearLayout photoContainer = dialogView.findViewById(R.id.container_storage_tower_photos);
        EditText drawersInput = dialogView.findViewById(R.id.input_storage_tower_drawers);
        EditText columnsInput = dialogView.findViewById(R.id.input_storage_tower_columns);
        CheckBox topCheckBox = dialogView.findViewById(R.id.checkbox_storage_tower_has_top);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel_storage_tower);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm_storage_tower);
        ImageButton closeDialogButton = dialogView.findViewById(R.id.button_close_storage_tower_dialog);

        if (titleView != null) {
            titleView.setText(isEditing
                    ? R.string.dialog_edit_storage_tower_title
                    : R.string.dialog_add_storage_tower_title);
        }

        if (closeDialogButton != null) {
            closeDialogButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (typeSpinner != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.storage_tower_type_defaults, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);
            if (itemToEdit != null) {
                String existingType = itemToEdit.getFurnitureType();
                if (TextUtils.isEmpty(existingType)) {
                    existingType = itemToEdit.getType();
                }
                if (!TextUtils.isEmpty(existingType)) {
                    int count = adapter.getCount();
                    for (int i = 0; i < count; i++) {
                        CharSequence value = adapter.getItem(i);
                        if (value != null && existingType.equalsIgnoreCase(value.toString())) {
                            typeSpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }
        }

        final FormState formState = new FormState();
        formState.photoLabel = photoLabel;
        formState.photoContainer = photoContainer;
        formState.addPhotoButton = addPhotoButton;
        formState.photoLabelTemplateRes = R.string.dialog_label_storage_tower_photos_template;
        currentFormState = formState;

        if (itemToEdit != null) {
            if (nameInput != null) {
                String existingName = itemToEdit.getName();
                nameInput.setText(existingName);
                if (!TextUtils.isEmpty(existingName)) {
                    nameInput.setSelection(existingName.length());
                }
            }
            if (commentInput != null) {
                String existingComment = itemToEdit.getComment();
                commentInput.setText(existingComment);
                if (!TextUtils.isEmpty(existingComment)) {
                    commentInput.setSelection(existingComment.length());
                }
            }
            if (customTypeInput != null) {
                String existingCustom = itemToEdit.getFurnitureCustomType();
                if (!TextUtils.isEmpty(existingCustom)) {
                    customTypeInput.setText(existingCustom);
                    customTypeInput.setSelection(existingCustom.length());
                }
            }
            if (drawersInput != null) {
                Integer existingLevels = itemToEdit.getFurnitureLevels();
                if (existingLevels != null) {
                    String value = String.valueOf(existingLevels);
                    drawersInput.setText(value);
                    drawersInput.setSelection(value.length());
                }
            }
            if (columnsInput != null) {
                Integer existingColumns = itemToEdit.getFurnitureColumns();
                if (existingColumns != null) {
                    String value = String.valueOf(existingColumns);
                    columnsInput.setText(value);
                    columnsInput.setSelection(value.length());
                }
            }
            if (topCheckBox != null) {
                topCheckBox.setChecked(itemToEdit.hasFurnitureTop());
            }
            formState.photos.addAll(itemToEdit.getPhotos());
        }

        refreshPhotoSection(formState);

        if (addPhotoButton != null) {
            addPhotoButton.setOnClickListener(v -> {
                if (currentFormState == null || currentFormState != formState) {
                    return;
                }
                if (formState.photos.size() >= MAX_FORM_PHOTOS) {
                    Toast.makeText(this, R.string.dialog_error_max_photos_reached,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                }
            });
        }

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                View firstInvalidField = null;

                String trimmedName = null;
                if (nameInput != null && nameInput.getText() != null) {
                    trimmedName = nameInput.getText().toString().trim();
                }
                if (TextUtils.isEmpty(trimmedName)) {
                    if (nameInput != null) {
                        nameInput.setError(getString(R.string.error_storage_tower_name_required));
                        firstInvalidField = nameInput;
                    }
                } else if (nameInput != null) {
                    nameInput.setError(null);
                }

                Integer drawersValue = null;
                if (drawersInput != null) {
                    CharSequence drawersText = drawersInput.getText();
                    String rawDrawers = drawersText != null ? drawersText.toString().trim() : "";
                    if (rawDrawers.isEmpty()) {
                        drawersInput.setError(getString(R.string.error_storage_tower_drawers_required));
                        if (firstInvalidField == null) {
                            firstInvalidField = drawersInput;
                        }
                    } else {
                        try {
                            int parsed = Integer.parseInt(rawDrawers);
                            if (parsed > 0) {
                                drawersValue = parsed;
                                drawersInput.setError(null);
                            } else {
                                drawersInput.setError(getString(R.string.error_storage_tower_drawers_required));
                                if (firstInvalidField == null) {
                                    firstInvalidField = drawersInput;
                                }
                            }
                        } catch (NumberFormatException ignored) {
                            drawersInput.setError(getString(R.string.error_storage_tower_drawers_required));
                            if (firstInvalidField == null) {
                                firstInvalidField = drawersInput;
                            }
                        }
                    }
                }

                Integer columnsValue = null;
                if (columnsInput != null) {
                    CharSequence columnsText = columnsInput.getText();
                    String rawColumns = columnsText != null ? columnsText.toString().trim() : "";
                    if (rawColumns.isEmpty()) {
                        columnsInput.setError(getString(R.string.error_storage_tower_columns_required));
                        if (firstInvalidField == null) {
                            firstInvalidField = columnsInput;
                        }
                    } else {
                        try {
                            int parsed = Integer.parseInt(rawColumns);
                            if (parsed > 0) {
                                columnsValue = parsed;
                                columnsInput.setError(null);
                            } else {
                                columnsInput.setError(getString(R.string.error_storage_tower_columns_required));
                                if (firstInvalidField == null) {
                                    firstInvalidField = columnsInput;
                                }
                            }
                        } catch (NumberFormatException ignored) {
                            columnsInput.setError(getString(R.string.error_storage_tower_columns_required));
                            if (firstInvalidField == null) {
                                firstInvalidField = columnsInput;
                            }
                        }
                    }
                }

                if (firstInvalidField != null) {
                    firstInvalidField.requestFocus();
                    updateStorageTowerConfirmButtonState(confirmButton, nameInput, drawersInput, columnsInput);
                    return;
                }

                if (trimmedName == null) {
                    trimmedName = "";
                }

                String commentValue = null;
                if (commentInput != null && commentInput.getText() != null) {
                    commentValue = commentInput.getText().toString().trim();
                    if (commentValue.isEmpty()) {
                        commentValue = null;
                    }
                }

                String selectedType = null;
                if (typeSpinner != null && typeSpinner.getSelectedItem() != null) {
                    selectedType = typeSpinner.getSelectedItem().toString();
                }
                if (TextUtils.isEmpty(selectedType)) {
                    selectedType = getString(R.string.dialog_storage_tower_type_default);
                }

                String customTypeValue = null;
                if (customTypeInput != null && customTypeInput.getText() != null) {
                    String rawValue = customTypeInput.getText().toString().trim();
                    if (!rawValue.isEmpty()) {
                        customTypeValue = rawValue;
                    }
                }

                boolean hasTop = topCheckBox != null && topCheckBox.isChecked();

                if (currentFormState != null && currentFormState != formState) {
                    dialog.dismiss();
                    return;
                }

                List<String> photoValues = new ArrayList<>(formState.photos);

                RoomContentItem newItem = RoomContentItem.createStorageTower(trimmedName,
                        commentValue,
                        selectedType,
                        customTypeValue,
                        photoValues,
                        drawersValue,
                        columnsValue,
                        hasTop);

                if (isEditing) {
                    if (positionToEdit < 0 || positionToEdit >= roomContentItems.size()) {
                        dialog.dismiss();
                        return;
                    }
                    RoomContentItem existingItem = roomContentItems.get(positionToEdit);
                    preserveHierarchyMetadata(existingItem, newItem);
                    roomContentItems.set(positionToEdit, newItem);
                } else {
                    roomContentItems.add(newItem);
                }

                RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
                sortRoomContentItems();
                RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
                if (roomContentAdapter != null) {
                    roomContentAdapter.notifyDataSetChanged();
                }
                if (contentList != null) {
                    int targetPosition = roomContentItems.indexOf(newItem);
                    if (targetPosition >= 0) {
                        final int scrollPosition = targetPosition;
                        contentList.post(() -> contentList.smoothScrollToPosition(scrollPosition));
                    }
                }
                saveRoomContent();
                updateEmptyState();
                int messageRes = isEditing
                        ? R.string.room_storage_tower_updated_confirmation
                        : R.string.room_storage_tower_added_confirmation;
                Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.setOnDismissListener(d -> {
            if (currentFormState == formState) {
                currentFormState = null;
            }
            if (restorePopupState != null && roomContentAdapter != null && contentList != null) {
                roomContentAdapter.restoreContainerPopup(contentList, restorePopupState);
            }
            if (restoreFurnitureState != null && roomContentAdapter != null && contentList != null) {
                roomContentAdapter.restoreFurniturePopup(contentList, restoreFurnitureState);
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
                public void afterTextChanged(Editable editable) {
                    if (nameInput == null) {
                        return;
                    }
                    CharSequence value = editable;
                    if (value == null || value.toString().trim().isEmpty()) {
                        nameInput.setError(getString(R.string.error_storage_tower_name_required));
                    } else {
                        nameInput.setError(null);
                    }
                    updateStorageTowerConfirmButtonState(confirmButton, nameInput, drawersInput,
                            columnsInput);
                }
            });
        }

        if (drawersInput != null) {
            drawersInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (drawersInput == null) {
                        return;
                    }
                    String value = editable != null ? editable.toString().trim() : "";
                    if (value.isEmpty()) {
                        drawersInput.setError(getString(R.string.error_storage_tower_drawers_required));
                    } else {
                        try {
                            int parsed = Integer.parseInt(value);
                            if (parsed > 0) {
                                drawersInput.setError(null);
                            } else {
                                drawersInput.setError(getString(R.string.error_storage_tower_drawers_required));
                            }
                        } catch (NumberFormatException e) {
                            drawersInput.setError(getString(R.string.error_storage_tower_drawers_required));
                        }
                    }
                    updateStorageTowerConfirmButtonState(confirmButton, nameInput, drawersInput,
                            columnsInput);
                }
            });
        }

        if (columnsInput != null) {
            columnsInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (columnsInput == null) {
                        return;
                    }
                    String value = editable != null ? editable.toString().trim() : "";
                    if (value.isEmpty()) {
                        columnsInput.setError(getString(R.string.error_storage_tower_columns_required));
                    } else {
                        try {
                            int parsed = Integer.parseInt(value);
                            if (parsed > 0) {
                                columnsInput.setError(null);
                            } else {
                                columnsInput.setError(getString(R.string.error_storage_tower_columns_required));
                            }
                        } catch (NumberFormatException e) {
                            columnsInput.setError(getString(R.string.error_storage_tower_columns_required));
                        }
                    }
                    updateStorageTowerConfirmButtonState(confirmButton, nameInput, drawersInput,
                            columnsInput);
                }
            });
        }

        updateStorageTowerConfirmButtonState(confirmButton, nameInput, drawersInput, columnsInput);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void updateStorageTowerConfirmButtonState(@Nullable Button confirmButton,
            @Nullable EditText nameInput, @Nullable EditText drawersInput,
            @Nullable EditText columnsInput) {
        if (confirmButton == null) {
            return;
        }
        boolean enabled = hasNonEmptyText(nameInput)
                && hasPositiveInteger(drawersInput)
                && hasPositiveInteger(columnsInput);
        confirmButton.setEnabled(enabled);
        confirmButton.setAlpha(enabled ? 1f : ACTION_DISABLED_ALPHA);
    }

    private void updateFurnitureConfirmButtonState(@Nullable Button confirmButton,
            @Nullable EditText nameInput, @Nullable EditText levelsInput,
            @Nullable EditText columnsInput) {
        if (confirmButton == null) {
            return;
        }
        boolean enabled = hasNonEmptyText(nameInput)
                && hasPositiveInteger(levelsInput)
                && hasPositiveInteger(columnsInput);
        confirmButton.setEnabled(enabled);
        confirmButton.setAlpha(enabled ? 1f : ACTION_DISABLED_ALPHA);
    }

    private boolean hasNonEmptyText(@Nullable EditText input) {
        if (input == null || input.getText() == null) {
            return false;
        }
        return !input.getText().toString().trim().isEmpty();
    }

    private boolean hasPositiveInteger(@Nullable EditText input) {
        if (input == null || input.getText() == null) {
            return false;
        }
        String value = input.getText().toString().trim();
        if (value.isEmpty()) {
            return false;
        }
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showFurnitureDialog(@Nullable RoomContentItem itemToEdit, int positionToEdit) {
        if (itemToEdit != null && itemToEdit.isStorageTower()) {
            showStorageTowerDialog(itemToEdit, positionToEdit);
            return;
        }
        boolean isEditing = itemToEdit != null && positionToEdit >= 0;
        View dialogView = inflateDialogView(R.layout.dialog_add_furniture);
        if (dialogView == null) {
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        final RoomContentAdapter.ContainerPopupRestoreState restorePopupState = roomContentAdapter != null
                ? roomContentAdapter.consumePendingContainerPopupRestoreState()
                : null;
        final RoomContentAdapter.FurniturePopupRestoreState restoreFurnitureState = roomContentAdapter != null
                ? roomContentAdapter.consumePendingFurniturePopupRestoreState()
                : null;
        dialog.show();

        TextView titleView = dialogView.findViewById(R.id.text_dialog_furniture_title);
        Spinner typeSpinner = dialogView.findViewById(R.id.spinner_furniture_type);
        EditText customTypeInput = dialogView.findViewById(R.id.input_furniture_custom_type);
        EditText nameInput = dialogView.findViewById(R.id.input_furniture_name);
        EditText commentInput = dialogView.findViewById(R.id.input_furniture_comment);
        TextView photoLabel = dialogView.findViewById(R.id.text_furniture_photos_label);
        Button addPhotoButton = dialogView.findViewById(R.id.button_add_furniture_photo);
        LinearLayout photoContainer = dialogView.findViewById(R.id.container_furniture_photos);
        EditText levelsInput = dialogView.findViewById(R.id.input_furniture_levels);
        EditText columnsInput = dialogView.findViewById(R.id.input_furniture_columns);
        CheckBox topCheckBox = dialogView.findViewById(R.id.checkbox_furniture_has_top);
        CheckBox bottomCheckBox = dialogView.findViewById(R.id.checkbox_furniture_has_bottom);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel_furniture);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm_furniture);

        if (titleView != null) {
            titleView.setText(isEditing
                    ? R.string.dialog_edit_furniture_title
                    : R.string.dialog_add_furniture_title);
        }

        if (typeSpinner != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.furniture_type_defaults, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);
            if (itemToEdit != null) {
                String existingType = itemToEdit.getFurnitureType();
                if (TextUtils.isEmpty(existingType)) {
                    existingType = itemToEdit.getType();
                }
                if (!TextUtils.isEmpty(existingType)) {
                    int count = adapter.getCount();
                    for (int i = 0; i < count; i++) {
                        CharSequence value = adapter.getItem(i);
                        if (value != null && existingType.equalsIgnoreCase(value.toString())) {
                            typeSpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }
        }

        final FormState formState = new FormState();
        formState.photoLabel = photoLabel;
        formState.photoContainer = photoContainer;
        formState.addPhotoButton = addPhotoButton;
        formState.photoLabelTemplateRes = R.string.dialog_label_furniture_photos_template;
        currentFormState = formState;

        if (itemToEdit != null) {
            if (nameInput != null) {
                String existingName = itemToEdit.getName();
                nameInput.setText(existingName);
                if (!TextUtils.isEmpty(existingName)) {
                    nameInput.setSelection(existingName.length());
                }
            }
            if (commentInput != null) {
                String existingComment = itemToEdit.getComment();
                commentInput.setText(existingComment);
                if (!TextUtils.isEmpty(existingComment)) {
                    commentInput.setSelection(existingComment.length());
                }
            }
            if (customTypeInput != null) {
                String existingCustom = itemToEdit.getFurnitureCustomType();
                if (!TextUtils.isEmpty(existingCustom)) {
                    customTypeInput.setText(existingCustom);
                    customTypeInput.setSelection(existingCustom.length());
                }
            }
            if (levelsInput != null) {
                Integer existingLevels = itemToEdit.getFurnitureLevels();
                if (existingLevels != null) {
                    String value = String.valueOf(existingLevels);
                    levelsInput.setText(value);
                    levelsInput.setSelection(value.length());
                }
            }
            if (columnsInput != null) {
                Integer existingColumns = itemToEdit.getFurnitureColumns();
                if (existingColumns != null) {
                    String value = String.valueOf(existingColumns);
                    columnsInput.setText(value);
                    columnsInput.setSelection(value.length());
                }
            }
            if (topCheckBox != null) {
                topCheckBox.setChecked(itemToEdit.hasFurnitureTop());
            }
            if (bottomCheckBox != null) {
                bottomCheckBox.setChecked(itemToEdit.hasFurnitureBottom());
            }
            formState.photos.addAll(itemToEdit.getPhotos());
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

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                String trimmedName = null;
                if (nameInput != null && nameInput.getText() != null) {
                    trimmedName = nameInput.getText().toString().trim();
                }
                if (trimmedName == null || trimmedName.isEmpty()) {
                    if (nameInput != null) {
                        nameInput.setError(getString(R.string.error_furniture_name_required));
                        nameInput.requestFocus();
                    }
                    return;
                }
                if (nameInput != null) {
                    nameInput.setError(null);
                }

                String commentValue = null;
                if (commentInput != null && commentInput.getText() != null) {
                    commentValue = commentInput.getText().toString().trim();
                    if (commentValue.isEmpty()) {
                        commentValue = null;
                    }
                }

                String selectedType = null;
                if (typeSpinner != null && typeSpinner.getSelectedItem() != null) {
                    selectedType = typeSpinner.getSelectedItem().toString();
                }
                if (TextUtils.isEmpty(selectedType)) {
                    selectedType = getString(R.string.dialog_furniture_type_shelf);
                }

                String customTypeValue = null;
                if (customTypeInput != null && customTypeInput.getText() != null) {
                    String rawValue = customTypeInput.getText().toString().trim();
                    if (!rawValue.isEmpty()) {
                        customTypeValue = rawValue;
                    }
                }

                Integer levelsValue = null;
                if (levelsInput != null && levelsInput.getText() != null) {
                    String rawLevels = levelsInput.getText().toString().trim();
                    if (!rawLevels.isEmpty()) {
                        try {
                            int parsed = Integer.parseInt(rawLevels);
                            if (parsed > 0) {
                                levelsValue = parsed;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                if (levelsValue == null) {
                    if (levelsInput != null) {
                        levelsInput.setError(getString(R.string.error_furniture_levels_required));
                        levelsInput.requestFocus();
                    }
                    return;
                } else if (levelsInput != null) {
                    levelsInput.setError(null);
                }

                Integer columnsValue = null;
                if (columnsInput != null && columnsInput.getText() != null) {
                    String rawColumns = columnsInput.getText().toString().trim();
                    if (!rawColumns.isEmpty()) {
                        try {
                            int parsed = Integer.parseInt(rawColumns);
                            if (parsed > 0) {
                                columnsValue = parsed;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                if (columnsValue == null) {
                    if (columnsInput != null) {
                        columnsInput.setError(getString(R.string.error_furniture_columns_required));
                        columnsInput.requestFocus();
                    }
                    return;
                } else if (columnsInput != null) {
                    columnsInput.setError(null);
                }

                boolean hasTop = topCheckBox != null && topCheckBox.isChecked();
                boolean hasBottom = bottomCheckBox != null && bottomCheckBox.isChecked();

                if (currentFormState != null && currentFormState != formState) {
                    dialog.dismiss();
                    return;
                }

                List<String> photoValues = new ArrayList<>(formState.photos);

                RoomContentItem newItem = RoomContentItem.createFurniture(trimmedName,
                        commentValue,
                        selectedType,
                        customTypeValue,
                        photoValues,
                        levelsValue,
                        columnsValue,
                        hasTop,
                        hasBottom);

                if (isEditing) {
                    if (positionToEdit < 0 || positionToEdit >= roomContentItems.size()) {
                        dialog.dismiss();
                        return;
                    }
                    RoomContentItem existingItem = roomContentItems.get(positionToEdit);
                    preserveHierarchyMetadata(existingItem, newItem);
                    roomContentItems.set(positionToEdit, newItem);
                } else {
                    roomContentItems.add(newItem);
                }

                RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
                sortRoomContentItems();
                RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
                if (roomContentAdapter != null) {
                    roomContentAdapter.notifyDataSetChanged();
                }
                if (contentList != null) {
                    int targetPosition = roomContentItems.indexOf(newItem);
                    if (targetPosition >= 0) {
                        final int scrollPosition = targetPosition;
                        contentList.post(() -> contentList.smoothScrollToPosition(scrollPosition));
                    }
                }
                saveRoomContent();
                updateEmptyState();
                int messageRes = isEditing
                        ? R.string.room_furniture_updated_confirmation
                        : R.string.room_furniture_added_confirmation;
                Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        updateFurnitureConfirmButtonState(confirmButton, nameInput, levelsInput, columnsInput);

        dialog.setOnDismissListener(d -> {
            if (currentFormState == formState) {
                currentFormState = null;
            }
            if (restorePopupState != null && roomContentAdapter != null && contentList != null) {
                roomContentAdapter.restoreContainerPopup(contentList, restorePopupState);
            }
            if (restoreFurnitureState != null && roomContentAdapter != null && contentList != null) {
                roomContentAdapter.restoreFurniturePopup(contentList, restoreFurnitureState);
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
                public void afterTextChanged(Editable editable) {
                    if (nameInput == null) {
                        return;
                    }
                    CharSequence value = editable;
                    if (value == null || value.toString().trim().isEmpty()) {
                        nameInput.setError(getString(R.string.error_furniture_name_required));
                    } else {
                        nameInput.setError(null);
                    }
                    updateFurnitureConfirmButtonState(confirmButton, nameInput, levelsInput,
                            columnsInput);
                }
            });
        }

        if (levelsInput != null) {
            levelsInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (levelsInput == null) {
                        return;
                    }
                    String value = editable != null ? editable.toString().trim() : "";
                    if (value.isEmpty()) {
                        levelsInput.setError(getString(R.string.error_furniture_levels_required));
                    } else {
                        try {
                            int parsed = Integer.parseInt(value);
                            if (parsed > 0) {
                                levelsInput.setError(null);
                            } else {
                                levelsInput.setError(getString(R.string.error_furniture_levels_required));
                            }
                        } catch (NumberFormatException e) {
                            levelsInput.setError(getString(R.string.error_furniture_levels_required));
                        }
                    }
                    updateFurnitureConfirmButtonState(confirmButton, nameInput, levelsInput,
                            columnsInput);
                }
            });
        }

        if (columnsInput != null) {
            columnsInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (columnsInput == null) {
                        return;
                    }
                    String value = editable != null ? editable.toString().trim() : "";
                    if (value.isEmpty()) {
                        columnsInput.setError(getString(R.string.error_furniture_columns_required));
                    } else {
                        try {
                            int parsed = Integer.parseInt(value);
                            if (parsed > 0) {
                                columnsInput.setError(null);
                            } else {
                                columnsInput.setError(getString(R.string.error_furniture_columns_required));
                            }
                        } catch (NumberFormatException e) {
                            columnsInput.setError(getString(R.string.error_furniture_columns_required));
                        }
                    }
                    updateFurnitureConfirmButtonState(confirmButton, nameInput, levelsInput,
                            columnsInput);
                }
            });
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void showBarcodeCreationDialog(@Nullable TextView barcodeValueView,
                                           @Nullable ImageView barcodePreviewView,
                                           @Nullable ImageButton clearBarcodeButton) {
        if (barcodeValueView == null && barcodePreviewView == null) {
            return;
        }
        final EditText input = new EditText(this);
        input.setHint(R.string.dialog_generate_barcode_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine(true);
        final int backgroundColor = ContextCompat.getColor(this, R.color.barcode_dialog_background);
        final int textColor = ContextCompat.getColor(this, R.color.barcode_dialog_text);
        final int hintColor = ContextCompat.getColor(this, R.color.barcode_dialog_hint);
        input.setTextColor(textColor);
        input.setHintTextColor(hintColor);
        String existing = extractBarcodeValue(barcodeValueView);
        if (!TextUtils.isEmpty(existing)) {
            input.setText(existing);
            input.setSelection(existing.length());
        }
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                input.setError(null);
            }
        });
        int padding = getResources().getDimensionPixelSize(R.dimen.dialog_barcode_input_padding);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding, padding, 0);
        container.setBackgroundColor(backgroundColor);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(input, params);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_generate_barcode_title)
                .setView(container)
                .setNegativeButton(android.R.string.cancel, (d, which) -> d.dismiss())
                .setPositiveButton(R.string.dialog_generate_barcode_action, null)
                .setNeutralButton(R.string.dialog_generate_barcode_random, null)
                .create();

        dialog.setOnShowListener(dlg -> {
            TextView titleView = dialog.findViewById(
                    Resources.getSystem().getIdentifier("alertTitle", "id", "android"));
            if (titleView != null) {
                titleView.setTextColor(textColor);
            }
            View contentPanel = dialog.findViewById(
                    Resources.getSystem().getIdentifier("contentPanel", "id", "android"));
            if (contentPanel != null) {
                contentPanel.setBackgroundColor(backgroundColor);
            }
            View buttonPanel = dialog.findViewById(
                    Resources.getSystem().getIdentifier("buttonPanel", "id", "android"));
            if (buttonPanel != null) {
                buttonPanel.setBackgroundColor(backgroundColor);
            }
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setTextColor(textColor);
                positive.setOnClickListener(v -> {
                    if (applyBarcodeValue(input, barcodeValueView, barcodePreviewView, clearBarcodeButton)) {
                        dialog.dismiss();
                    }
                });
            }
            Button neutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            if (neutral != null) {
                neutral.setTextColor(textColor);
                neutral.setOnClickListener(v -> {
                    String generated = generateRandomBarcodeValue();
                    input.setText(generated);
                    input.setSelection(generated.length());
                    if (applyBarcodeValue(input, barcodeValueView, barcodePreviewView, clearBarcodeButton)) {
                        dialog.dismiss();
                    }
                });
            }
            Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (negative != null) {
                negative.setTextColor(textColor);
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
        }
    }

    private boolean applyBarcodeValue(@NonNull EditText input,
                                      @Nullable TextView barcodeValueView,
                                      @Nullable ImageView barcodePreviewView,
                                      @Nullable ImageButton clearBarcodeButton) {
        CharSequence text = input.getText();
        String value = text != null ? text.toString().trim() : "";
        if (value.isEmpty()) {
            input.setError(getString(R.string.dialog_error_generate_barcode_empty));
            return false;
        }
        if (!updateBarcodePreview(barcodePreviewView, value, true)) {
            input.setError(getString(R.string.dialog_error_generate_barcode_failed));
            return false;
        }
        if (barcodeValueView != null) {
            barcodeValueView.setText(value);
        }
        updateBarcodeClearButton(clearBarcodeButton, true);
        input.setError(null);
        return true;
    }

    @NonNull
    private String generateRandomBarcodeValue() {
        int[] digits = new int[GENERATED_BARCODE_LENGTH - 1];
        for (int i = 0; i < digits.length; i++) {
            digits[i] = BARCODE_RANDOM.nextInt(10);
        }
        int checksum = computeEan13CheckDigit(digits);
        StringBuilder builder = new StringBuilder(GENERATED_BARCODE_LENGTH);
        for (int digit : digits) {
            builder.append(digit);
        }
        builder.append(checksum);
        return builder.toString();
    }

    private int computeEan13CheckDigit(@NonNull int[] digits) {
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            int digit = digits[i];
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int mod = sum % 10;
        return (10 - mod) % 10;
    }

    private void toggleAddRoomContentMenu(@NonNull View anchor) {
        if (addMenuPopup != null && addMenuPopup.isShowing()) {
            addMenuPopup.dismiss();
            return;
        }
        showAddRoomContentMenu(anchor, null, null, true, true, ADD_MENU_ANCHOR_MAIN, null, null);
    }

    private void showContainerAddMenu(@NonNull View anchor,
                                      @NonNull RoomContentItem container,
                                      int adapterPosition) {
        showAddRoomContentMenu(anchor, container.getRank(), null, false, false,
                ADD_MENU_ANCHOR_CONTAINER, adapterPosition, null);
    }

    private void showFurnitureTopAddMenu(@NonNull View anchor,
                                         @NonNull RoomContentItem furniture) {
        int adapterPosition = findAdapterPositionForItem(furniture);
        Integer resolvedPosition = adapterPosition != RecyclerView.NO_POSITION
                ? adapterPosition
                : null;
        boolean includeStorageTowerOption = !furniture.isStorageTower();
        showAddRoomContentMenu(anchor, furniture.getRank(), null, false,
                includeStorageTowerOption,
                ADD_MENU_ANCHOR_FURNITURE_LEVEL, resolvedPosition,
                RoomContentAdapter.FURNITURE_SECTION_INDEX_TOP);
    }

    private void showFurnitureBottomAddMenu(@NonNull View anchor,
                                            @NonNull RoomContentItem furniture) {
        int adapterPosition = findAdapterPositionForItem(furniture);
        Integer resolvedPosition = adapterPosition != RecyclerView.NO_POSITION
                ? adapterPosition
                : null;
        boolean includeStorageTowerOption = !furniture.isStorageTower();
        showAddRoomContentMenu(anchor, furniture.getRank(),
                RoomContentItem.FURNITURE_BOTTOM_LEVEL, false,
                includeStorageTowerOption,
                ADD_MENU_ANCHOR_FURNITURE_LEVEL, resolvedPosition,
                RoomContentAdapter.FURNITURE_SECTION_INDEX_BOTTOM);
    }

    private void showAddRoomContentMenu(@NonNull View anchor,
                                        @Nullable Long forcedParentRank,
                                        @Nullable Integer forcedFurnitureLevel,
                                        boolean includeFurnitureOption,
                                        boolean includeStorageTowerOption,
                                        int anchorType,
                                        @Nullable Integer targetAdapterPosition,
                                        @Nullable Integer furnitureLevelIndex) {
        if (addMenuPopup != null && addMenuPopup.isShowing()) {
            addMenuPopup.dismiss();
        }

        View popupContent = inflateDialogView(R.layout.popup_add_room_content_menu);
        currentAddMenuState = new AddMenuRestoreState(anchorType, forcedParentRank,
                forcedFurnitureLevel, includeFurnitureOption, includeStorageTowerOption,
                targetAdapterPosition, furnitureLevelIndex);
        TextView titleView = popupContent.findViewById(R.id.text_popup_add_room_content_title);
        if (titleView != null) {
            titleView.setText(R.string.popup_add_room_content_title);
        }

        View elementButton = popupContent.findViewById(R.id.button_popup_add_room_content_element);
        if (elementButton != null) {
            elementButton.setOnClickListener(view -> {
                if (addMenuPopup != null) {
                    addMenuPopup.dismiss();
                }
                if (anchorType == ADD_MENU_ANCHOR_CONTAINER && roomContentAdapter != null) {
                    if (targetAdapterPosition != null) {
                        roomContentAdapter.preparePendingContainerPopupRestore(
                                targetAdapterPosition);
                    }
                    roomContentAdapter.dismissActiveContainerPopup();
                } else if (anchorType == ADD_MENU_ANCHOR_FURNITURE_LEVEL) {
                    prepareFurniturePopupRestore(targetAdapterPosition, forcedParentRank,
                            furnitureLevelIndex);
                }
                showAddRoomContentDialog(forcedParentRank, forcedFurnitureLevel);
            });
        }

        View containerButton = popupContent.findViewById(R.id.button_popup_add_room_content_container);
        if (containerButton != null) {
            containerButton.setOnClickListener(view -> {
                if (addMenuPopup != null) {
                    addMenuPopup.dismiss();
                }
                if (anchorType == ADD_MENU_ANCHOR_CONTAINER && roomContentAdapter != null) {
                    if (targetAdapterPosition != null) {
                        roomContentAdapter.preparePendingContainerPopupRestore(
                                targetAdapterPosition);
                    }
                    roomContentAdapter.dismissActiveContainerPopup();
                } else if (anchorType == ADD_MENU_ANCHOR_FURNITURE_LEVEL) {
                    prepareFurniturePopupRestore(targetAdapterPosition, forcedParentRank,
                            furnitureLevelIndex);
                }
                showAddContainerDialog(forcedParentRank, forcedFurnitureLevel);
            });
        }

        View storageTowerButton = popupContent
                .findViewById(R.id.button_popup_add_room_content_storage_tower);
        if (storageTowerButton != null) {
            if (includeStorageTowerOption) {
                storageTowerButton.setVisibility(View.VISIBLE);
                storageTowerButton.setOnClickListener(view -> {
                    if (addMenuPopup != null) {
                        addMenuPopup.dismiss();
                    }
                    showAddStorageTowerDialog();
                });
            } else {
                storageTowerButton.setVisibility(View.GONE);
                storageTowerButton.setOnClickListener(null);
            }
        }

        View furnitureButton = popupContent.findViewById(R.id.button_popup_add_room_content_furniture);
        if (furnitureButton != null) {
            if (includeFurnitureOption) {
                furnitureButton.setVisibility(View.VISIBLE);
                furnitureButton.setOnClickListener(view -> {
                    if (addMenuPopup != null) {
                        addMenuPopup.dismiss();
                    }
                    showFurnitureDialog(null, -1);
                });
            } else {
                furnitureButton.setVisibility(View.GONE);
                furnitureButton.setOnClickListener(null);
            }
        }

        addMenuPopup = new PopupWindow(
                popupContent,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        addMenuPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addMenuPopup.setOutsideTouchable(true);
        addMenuPopup.setOnDismissListener(() -> {
            addMenuPopup = null;
            currentAddMenuState = null;
        });

        popupContent.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int popupHeight = popupContent.getMeasuredHeight();
        int verticalOffset = (int) (getResources().getDisplayMetrics().density * 12);

        Rect displayFrame = new Rect();
        anchor.getWindowVisibleDisplayFrame(displayFrame);
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int anchorBottom = location[1] + anchor.getHeight();
        int spaceBelow = displayFrame.bottom - anchorBottom;
        int spaceAbove = location[1] - displayFrame.top;

        int yOffset = -anchor.getHeight() - popupHeight - verticalOffset;
        if (spaceBelow >= popupHeight + verticalOffset) {
            yOffset = verticalOffset;
        } else if (spaceAbove < popupHeight + verticalOffset) {
            yOffset = -anchor.getHeight() - popupHeight;
        }

        PopupWindowCompat.showAsDropDown(addMenuPopup, anchor, 0, yOffset, Gravity.END);
    }

    private void prepareFurniturePopupRestore(@Nullable Integer targetAdapterPosition,
                                              @Nullable Long forcedParentRank,
                                              @Nullable Integer furnitureLevelIndex) {
        if (roomContentAdapter == null || furnitureLevelIndex == null) {
            return;
        }
        int adapterPosition = targetAdapterPosition != null
                ? targetAdapterPosition
                : (forcedParentRank != null
                        ? findAdapterPositionForRank(forcedParentRank)
                        : RecyclerView.NO_POSITION);
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return;
        }
        RoomContentAdapter.FurniturePopupRestoreState activeState =
                roomContentAdapter.captureActiveFurniturePopupState();
        Integer columnToDisplay = null;
        if (activeState != null && activeState.furniturePosition == adapterPosition) {
            columnToDisplay = activeState.columnToDisplay;
        }
        roomContentAdapter.preparePendingFurniturePopupRestore(adapterPosition,
                furnitureLevelIndex, columnToDisplay, false);
    }

    private void showFurnitureLevelAddMenu(@NonNull View anchor,
                                           @NonNull RoomContentItem furniture,
                                           int level) {
        int adapterPosition = findAdapterPositionForItem(furniture);
        Integer resolvedPosition = adapterPosition != RecyclerView.NO_POSITION
                ? adapterPosition
                : null;
        boolean includeStorageTowerOption = !furniture.isStorageTower();
        showAddRoomContentMenu(anchor, furniture.getRank(), level, false,
                includeStorageTowerOption,
                ADD_MENU_ANCHOR_FURNITURE_LEVEL, resolvedPosition, level);
    }

    private void showFurnitureLevelAddBookDialog(@NonNull View anchor,
                                                 @NonNull RoomContentItem furniture,
                                                 int level) {
        if (addMenuPopup != null && addMenuPopup.isShowing()) {
            addMenuPopup.dismiss();
        }
        int adapterPosition = findAdapterPositionForItem(furniture);
        Integer resolvedPosition = adapterPosition != RecyclerView.NO_POSITION
                ? adapterPosition
                : null;
        prepareFurniturePopupRestore(resolvedPosition, furniture.getRank(), level);
        RoomContentItem prefillItem = new RoomContentItem(
                "",
                "",
                getString(R.string.dialog_type_book),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
        showRoomContentDialog(prefillItem, -1, false, furniture.getRank(), level);
    }

    private void showAddRoomContentDialog() {
        showAddRoomContentDialog(null, null);
    }

    private void showAddRoomContentDialog(@Nullable Long forcedParentRank) {
        showAddRoomContentDialog(forcedParentRank, null);
    }

    private void showAddRoomContentDialog(@Nullable Long forcedParentRank,
                                          @Nullable Integer forcedFurnitureLevel) {
        showRoomContentDialog(null, -1, false, forcedParentRank, forcedFurnitureLevel);
    }

    private void showAddStorageTowerDialog() {
        showStorageTowerDialog(null, -1);
    }

    private void showAddContainerDialog() {
        showAddContainerDialog(null, null);
    }

    private void showAddContainerDialog(@Nullable Long forcedParentRank,
                                        @Nullable Integer forcedFurnitureLevel) {
        showContainerDialog(null, -1, forcedParentRank, forcedFurnitureLevel);
    }

    private int findAdapterPositionForItem(@NonNull RoomContentItem item) {
        int directIndex = roomContentItems.indexOf(item);
        if (directIndex >= 0) {
            return directIndex;
        }
        Long rank = item.getRank();
        if (rank != null) {
            return findAdapterPositionForRank(rank);
        }
        return RecyclerView.NO_POSITION;
    }

    private int findAdapterPositionForRank(long rank) {
        int size = roomContentItems.size();
        for (int index = 0; index < size; index++) {
            RoomContentItem candidate = roomContentItems.get(index);
            if (candidate == null) {
                continue;
            }
            Long candidateRank = candidate.getRank();
            if (candidateRank != null && candidateRank.equals(rank)) {
                return index;
            }
        }
        return RecyclerView.NO_POSITION;
    }


private void showMoveRoomContentDialog(@NonNull RoomContentItem item, int position) {
    showMoveRoomContentDialogInternal(Collections.singletonList(item));
}

private void showMoveRoomContentDialogForSelection(@NonNull List<RoomContentItem> items) {
    showMoveRoomContentDialogInternal(new ArrayList<>(items));
}

private void showMoveRoomContentDialogInternal(@NonNull List<RoomContentItem> items) {
    if (items.isEmpty()) {
        return;
    }
    RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
    RoomContentItem primary = items.get(0);
    int position = roomContentItems.indexOf(primary);
    if (position < 0) {
        Toast.makeText(this, R.string.dialog_move_room_content_missing_items,
                Toast.LENGTH_SHORT).show();
        return;
    }

    View dialogView = inflateDialogView(R.layout.dialog_move_room_content);
    Spinner establishmentSpinner = dialogView.findViewById(R.id.spinner_move_establishment);
    Spinner roomSpinner = dialogView.findViewById(R.id.spinner_move_room);
    RadioGroup containerRadioGroup = dialogView.findViewById(R.id.radio_group_move_container);
    TextView containerLabel = dialogView.findViewById(R.id.label_move_container);
    View furnitureDetailsContainer = dialogView.findViewById(R.id.container_move_furniture_details);
    View furnitureLevelContainer = dialogView.findViewById(R.id.container_move_furniture_level);
    Spinner furnitureLevelSpinner = dialogView.findViewById(R.id.spinner_move_furniture_level);
    View furnitureColumnContainer = dialogView.findViewById(R.id.container_move_furniture_column);
    Spinner furnitureColumnSpinner = dialogView.findViewById(R.id.spinner_move_furniture_column);

    List<String> establishmentOptions = roomContentViewModel.loadEstablishmentNames(establishmentName);
    if (establishmentOptions.isEmpty()) {
        Toast.makeText(this, R.string.dialog_move_room_content_empty_establishments,
                Toast.LENGTH_SHORT).show();
        return;
    }
    ArrayAdapter<String> establishmentAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, establishmentOptions);
    establishmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    establishmentSpinner.setAdapter(establishmentAdapter);

    int establishmentIndex = findIndexIgnoreCase(establishmentOptions, establishmentName);
    if (establishmentIndex >= 0) {
        establishmentSpinner.setSelection(establishmentIndex);
    }

    final boolean multipleSelection = items.size() > 1;
    final List<RoomContentItem> selection = new ArrayList<>(items);
    final boolean movingAnyFurniture = containsFurniture(selection);
    final String[] selectedEstablishmentHolder = new String[1];
    final String[] selectedRoomHolder = new String[1];
    final ContainerSelection selectedContainerHolder = new ContainerSelection();
    Long initialRank = primary.getParentRank();
    if (multipleSelection) {
        for (int index = 1; index < selection.size(); index++) {
            RoomContentItem current = selection.get(index);
            Long parentRank = current.getParentRank();
            if (initialRank == null ? parentRank != null : !initialRank.equals(parentRank)) {
                initialRank = null;
                break;
            }
        }
        selectedContainerHolder.desiredRank = initialRank;
        selectedContainerHolder.desiredLevel = null;
        selectedContainerHolder.desiredColumn = null;
    } else {
        selectedContainerHolder.desiredRank = initialRank;
        selectedContainerHolder.desiredLevel = primary.getContainerLevel();
        selectedContainerHolder.desiredColumn = primary.getContainerColumn();
    }

    if (movingAnyFurniture) {
        selectedContainerHolder.selectedOption = null;
        selectedContainerHolder.desiredRank = null;
        selectedContainerHolder.desiredLevel = null;
        selectedContainerHolder.desiredColumn = null;
    }

    final Set<Long> additionalExcludedRanks = multipleSelection ? new HashSet<>() : null;
    if (multipleSelection && additionalExcludedRanks != null) {
        for (int index = 1; index < selection.size(); index++) {
            RoomContentItem current = selection.get(index);
            int currentPosition = roomContentItems.indexOf(current);
            if (currentPosition >= 0) {
                additionalExcludedRanks.addAll(collectGroupRanks(roomContentItems, currentPosition));
            }
        }
    }

    if (furnitureColumnSpinner != null) {
        furnitureColumnSpinner.setOnItemSelectedListener(null);
    }

    int titleRes = multipleSelection
            ? R.string.dialog_move_room_content_selection_title
            : R.string.dialog_move_room_content_title;
    AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setView(dialogView)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.dialog_move_room_content_move_button, null)
            .create();

    AdapterView.OnItemSelectedListener establishmentListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {
            Object value = parent.getItemAtPosition(spinnerPosition);
            selectedEstablishmentHolder[0] = value != null ? value.toString() : null;
            updateMoveDialogRooms(dialog, roomSpinner, selectedEstablishmentHolder[0],
                    selectedRoomHolder[0]);
            selectedRoomHolder[0] = roomSpinner.getSelectedItem() != null
                    ? roomSpinner.getSelectedItem().toString()
                    : null;
            updateMoveDialogContainers(dialog, containerRadioGroup, containerLabel,
                    furnitureDetailsContainer, furnitureLevelContainer, furnitureLevelSpinner,
                    furnitureColumnContainer, furnitureColumnSpinner,
                    selectedEstablishmentHolder[0], selectedRoomHolder[0], primary, position,
                    selectedContainerHolder, additionalExcludedRanks, movingAnyFurniture);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            selectedEstablishmentHolder[0] = null;
            updateMoveDialogRooms(dialog, roomSpinner, null, selectedRoomHolder[0]);
            selectedRoomHolder[0] = roomSpinner.getSelectedItem() != null
                    ? roomSpinner.getSelectedItem().toString()
                    : null;
            updateMoveDialogContainers(dialog, containerRadioGroup, containerLabel,
                    furnitureDetailsContainer, furnitureLevelContainer, furnitureLevelSpinner,
                    furnitureColumnContainer, furnitureColumnSpinner,
                    null, selectedRoomHolder[0], primary, position, selectedContainerHolder,
                    additionalExcludedRanks, movingAnyFurniture);
        }
    };
    establishmentSpinner.setOnItemSelectedListener(establishmentListener);

    AdapterView.OnItemSelectedListener roomListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {
            Object value = parent.getItemAtPosition(spinnerPosition);
            selectedRoomHolder[0] = value != null ? value.toString() : null;
            updateMoveDialogContainers(dialog, containerRadioGroup, containerLabel,
                    furnitureDetailsContainer, furnitureLevelContainer, furnitureLevelSpinner,
                    furnitureColumnContainer, furnitureColumnSpinner,
                    selectedEstablishmentHolder[0], selectedRoomHolder[0], primary, position,
                    selectedContainerHolder, additionalExcludedRanks, movingAnyFurniture);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            selectedRoomHolder[0] = null;
            updateMoveDialogContainers(dialog, containerRadioGroup, containerLabel,
                    furnitureDetailsContainer, furnitureLevelContainer, furnitureLevelSpinner,
                    furnitureColumnContainer, furnitureColumnSpinner,
                    selectedEstablishmentHolder[0], null, primary, position,
                    selectedContainerHolder, additionalExcludedRanks, movingAnyFurniture);
        }
    };
    roomSpinner.setOnItemSelectedListener(roomListener);

    selectedEstablishmentHolder[0] = establishmentSpinner.getSelectedItem() != null
            ? establishmentSpinner.getSelectedItem().toString()
            : null;
    selectedRoomHolder[0] = roomName;

    updateMoveDialogRooms(dialog, roomSpinner, selectedEstablishmentHolder[0],
            selectedRoomHolder[0]);
    selectedRoomHolder[0] = roomSpinner.getSelectedItem() != null
            ? roomSpinner.getSelectedItem().toString()
            : selectedRoomHolder[0];
    updateMoveDialogContainers(dialog, containerRadioGroup, containerLabel,
            furnitureDetailsContainer, furnitureLevelContainer, furnitureLevelSpinner,
            furnitureColumnContainer, furnitureColumnSpinner,
            selectedEstablishmentHolder[0], selectedRoomHolder[0], primary, position,
            selectedContainerHolder, additionalExcludedRanks, movingAnyFurniture);

    dialog.setOnShowListener(d -> {
        updateMoveButtonState(dialog, roomSpinner);
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton == null) {
            return;
        }
        positiveButton.setOnClickListener(v -> {
            String targetEstablishment = selectedEstablishmentHolder[0];
            if (TextUtils.isEmpty(targetEstablishment)
                    && establishmentSpinner.getSelectedItem() != null) {
                targetEstablishment = establishmentSpinner.getSelectedItem().toString();
            }
            String targetRoom = selectedRoomHolder[0];
            if (TextUtils.isEmpty(targetRoom) && roomSpinner.getSelectedItem() != null) {
                targetRoom = roomSpinner.getSelectedItem().toString();
            }
            if (TextUtils.isEmpty(targetRoom)) {
                Toast.makeText(RoomContentActivity.this,
                        R.string.dialog_move_room_content_empty_rooms,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            ContainerOption targetOption = selectedContainerHolder.selectedOption;
            Integer levelValue = selectedContainerHolder.desiredLevel;
            Integer columnValue = selectedContainerHolder.desiredColumn;
            if (movingAnyFurniture) {
                targetOption = null;
                levelValue = null;
                columnValue = null;
                selectedContainerHolder.selectedOption = null;
                selectedContainerHolder.desiredRank = null;
                selectedContainerHolder.desiredLevel = null;
                selectedContainerHolder.desiredColumn = null;
            }
            if (targetOption != null && targetOption.container != null
                    && targetOption.container.isFurniture()) {
                RoomContentItem furniture = targetOption.container;
                Integer maxLevels = furniture.getFurnitureLevels();
                Integer maxColumns = furniture.getFurnitureColumns();
                if (maxLevels == null || maxLevels > 0) {
                    Integer parsedLevel = selectedContainerHolder.desiredLevel;
                    boolean acceptsBottom = furniture.hasFurnitureBottom();
                    boolean supportsTopPlacement = furniture.hasFurnitureTop();
                    boolean levelValid;
                    if (parsedLevel == null) {
                        boolean topOptionAvailable = supportsTopPlacement;
                        if (!topOptionAvailable) {
                            List<LevelOption> levelOptions = buildLevelOptions(furniture,
                                    targetOption.contents);
                            for (LevelOption option : levelOptions) {
                                if (option != null && option.value == null) {
                                    topOptionAvailable = true;
                                    break;
                                }
                            }
                        }
                        levelValid = topOptionAvailable;
                    } else {
                        levelValid = true;
                        if (parsedLevel == RoomContentItem.FURNITURE_BOTTOM_LEVEL) {
                            levelValid = acceptsBottom;
                        } else if (parsedLevel <= 0
                                || (maxLevels != null && parsedLevel > maxLevels)) {
                            levelValid = false;
                        }
                    }
                    if (!levelValid) {
                        Toast.makeText(RoomContentActivity.this,
                                R.string.error_move_room_content_invalid_level,
                                Toast.LENGTH_SHORT).show();
                        if (furnitureLevelSpinner != null) {
                            furnitureLevelSpinner.requestFocus();
                        }
                        return;
                    }
                    levelValue = parsedLevel;
                    selectedContainerHolder.desiredLevel = parsedLevel;
                } else {
                    levelValue = null;
                    selectedContainerHolder.desiredLevel = null;
                }
                if (maxColumns != null && maxColumns > 0) {
                    Integer parsedColumn = selectedContainerHolder.desiredColumn;
                    if (parsedColumn == null || parsedColumn <= 0
                            || (maxColumns != null && parsedColumn > maxColumns)) {
                        Toast.makeText(RoomContentActivity.this,
                                R.string.error_move_room_content_invalid_column,
                                Toast.LENGTH_SHORT).show();
                        if (furnitureColumnSpinner != null) {
                            furnitureColumnSpinner.performClick();
                        }
                        return;
                    }
                    columnValue = parsedColumn;
                    selectedContainerHolder.desiredColumn = parsedColumn;
                } else {
                    columnValue = null;
                    selectedContainerHolder.desiredColumn = null;
                }
            } else {
                levelValue = null;
                columnValue = null;
                selectedContainerHolder.desiredLevel = null;
                selectedContainerHolder.desiredColumn = null;
            }
            if (multipleSelection) {
                moveRoomContentItems(selection, targetEstablishment, targetRoom,
                        selectedContainerHolder.selectedOption, levelValue, columnValue);
                dialog.dismiss();
            } else {
                if (moveRoomContentItem(primary, position, targetEstablishment, targetRoom,
                        selectedContainerHolder.selectedOption, levelValue, columnValue)) {
                    dialog.dismiss();
                }
            }
        });
    });

        dialog.show();
    }

    private void showCopyRoomContentDialog(@NonNull RoomContentItem item) {
        if (item.isFurniture()) {
            showFurnitureDialog(item, -1);
            return;
        }

        Long parentRank = item.getParentRank();
        RoomContentItem parentContainer = parentRank != null
                ? findContainerByRank(roomContentItems, parentRank)
                : null;
        Integer forcedFurnitureLevel = null;
        if (parentContainer != null && parentContainer.isFurniture()) {
            forcedFurnitureLevel = item.getContainerLevel();
        }

        if (item.isContainer()) {
            showContainerDialog(item, -1, parentRank, forcedFurnitureLevel);
        } else {
            showRoomContentDialog(item, -1, false, parentRank, forcedFurnitureLevel);
        }
    }

    private void showEditRoomContentDialog(@NonNull RoomContentItem item, int position) {
        if (item.isFurniture()) {
            showFurnitureDialog(item, position);
            return;
        }
        if (item.isContainer()) {
            showContainerDialog(item, position, null, null);
        } else {
            showRoomContentDialog(item, position, true, null, null);
        }
    }

    private void showDeleteRoomContentConfirmation(@NonNull RoomContentItem item, int position) {
        String name = item.getName();
        if (name == null || name.trim().isEmpty()) {
            name = getString(R.string.dialog_room_content_item_placeholder);
        }
        final int targetPosition = position;
        int messageRes = item.isFurniture()
                ? R.string.dialog_delete_room_furniture_message
                : item.isContainer()
                ? R.string.dialog_delete_room_container_message
                : R.string.dialog_delete_room_content_message;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_room_content_title)
                .setMessage(getString(messageRes, name))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialogInterface, which) ->
                        deleteRoomContent(targetPosition))
                .create();
        dialog.setOnDismissListener(dismissDialog -> {
            if (roomContentAdapter == null) {
                return;
            }
            RoomContentAdapter.ContainerPopupRestoreState restoreState =
                    roomContentAdapter.consumePendingContainerPopupRestoreState();
            if (restoreState != null && contentList != null) {
                roomContentAdapter.restoreContainerPopup(contentList, restoreState);
            }
        });
        dialog.show();
    }

    private void showDeleteRoomContentSelectionConfirmation(@NonNull List<RoomContentItem> items) {
        if (items.isEmpty()) {
            return;
        }
        int count = items.size();
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_room_content_selection_title)
                .setMessage(getResources().getQuantityString(
                        R.plurals.dialog_delete_room_content_selection_message, count, count))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) ->
                        deleteRoomContentSelection(items))
                .show();
    }

    private void deleteRoomContent(int position) {
        if (position < 0 || position >= roomContentItems.size()) {
            return;
        }
        RoomContentItem itemToRemove = roomContentItems.get(position);
        if (itemToRemove == null) {
            return;
        }
        boolean wasContainer = itemToRemove.isContainer();
        Set<RoomContentItem> containersToRefresh = new LinkedHashSet<>();
        Long parentRank = itemToRemove.getParentRank();
        if (parentRank != null) {
            RoomContentItem parent = findContainerByRank(roomContentItems, parentRank);
            if (parent != null) {
                containersToRefresh.add(parent);
            }
        }
        if (itemToRemove.isContainer()) {
            List<RoomContentItem> descendants = collectDescendants(itemToRemove);
            removeItemsFromRoomContent(descendants);
        }
        removeItemsFromRoomContent(Collections.singleton(itemToRemove));
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        sortRoomContentItems();
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        if (roomContentAdapter != null) {
            roomContentAdapter.notifyDataSetChanged();
        }
        saveRoomContent();
        updateEmptyState();
        scheduleContainerIndicatorRefresh(containersToRefresh);
        int messageRes = wasContainer
                ? R.string.room_container_deleted_confirmation
                : R.string.room_content_deleted_confirmation;
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
    }

    private void deleteRoomContentSelection(@NonNull List<RoomContentItem> items) {
        if (items.isEmpty()) {
            return;
        }
        Set<RoomContentItem> selection = Collections.newSetFromMap(new IdentityHashMap<>());
        selection.addAll(items);
        Set<RoomContentItem> containers = Collections.newSetFromMap(new IdentityHashMap<>());
        for (RoomContentItem candidate : selection) {
            if (candidate != null && candidate.isContainer()) {
                containers.add(candidate);
            }
        }
        for (RoomContentItem container : containers) {
            List<RoomContentItem> descendants = collectDescendants(container);
            selection.addAll(descendants);
        }
        boolean removedAny = false;
        int removedContainers = 0;
        int removedItems = 0;
        Set<RoomContentItem> containersToRefresh = new LinkedHashSet<>();
        Iterator<RoomContentItem> iterator = roomContentItems.iterator();
        while (iterator.hasNext()) {
            RoomContentItem current = iterator.next();
            if (selection.contains(current)) {
                removedAny = true;
                if (current.isContainer()) {
                    removedContainers++;
                } else {
                    removedItems++;
                }
                Long parentRank = current.getParentRank();
                if (parentRank != null) {
                    RoomContentItem parent = findContainerByRank(roomContentItems, parentRank);
                    if (parent != null && !selection.contains(parent)) {
                        containersToRefresh.add(parent);
                    }
                }
                iterator.remove();
            }
        }
        if (!removedAny) {
            return;
        }
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        sortRoomContentItems();
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        if (roomContentAdapter != null) {
            roomContentAdapter.notifyDataSetChanged();
        }
        saveRoomContent();
        updateEmptyState();
        scheduleContainerIndicatorRefresh(containersToRefresh);
        int messageRes;
        if (removedContainers > 0 && removedItems == 0) {
            messageRes = R.string.room_content_deleted_selection_confirmation_containers;
        } else if (removedItems > 0 && removedContainers == 0) {
            messageRes = R.string.room_content_deleted_selection_confirmation_items;
        } else {
            messageRes = R.string.room_content_deleted_selection_confirmation;
        }
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
        exitSelectionMode();
    }

    @NonNull
    private List<RoomContentItem> collectDescendants(@NonNull RoomContentItem parent) {
        List<RoomContentItem> descendants = new ArrayList<>();
        long parentRank = parent.getRank();
        Map<Long, List<RoomContentItem>> childrenByParent = new HashMap<>();
        for (RoomContentItem item : roomContentItems) {
            Long candidateParentRank = item.getParentRank();
            if (candidateParentRank == null) {
                continue;
            }
            List<RoomContentItem> children = childrenByParent.get(candidateParentRank);
            if (children == null) {
                children = new ArrayList<>();
                childrenByParent.put(candidateParentRank, children);
            }
            children.add(item);
        }
        List<RoomContentItem> directChildren = childrenByParent.get(parentRank);
        if (directChildren == null || directChildren.isEmpty()) {
            return descendants;
        }
        Set<RoomContentItem> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        ArrayDeque<RoomContentItem> stack = new ArrayDeque<>(directChildren);
        while (!stack.isEmpty()) {
            RoomContentItem current = stack.pop();
            if (current == null || !seen.add(current)) {
                continue;
            }
            descendants.add(current);
            if (!current.isContainer()) {
                continue;
            }
            List<RoomContentItem> nestedChildren = childrenByParent.get(current.getRank());
            if (nestedChildren != null && !nestedChildren.isEmpty()) {
                stack.addAll(nestedChildren);
            }
        }
        return descendants;
    }

    private void duplicateContainerContents(@NonNull RoomContentItem sourceContainer,
            @NonNull RoomContentItem newContainer,
            @NonNull Set<RoomContentItem> containersToRefresh) {
        List<RoomContentItem> directChildren = sourceContainer.getChildren();
        if (directChildren.isEmpty()) {
            containersToRefresh.add(newContainer);
            return;
        }
        Map<Long, RoomContentItem> clonedParents = new LinkedHashMap<>();
        clonedParents.put(sourceContainer.getRank(), newContainer);
        ArrayDeque<RoomContentItem> stack = new ArrayDeque<>(directChildren);
        while (!stack.isEmpty()) {
            RoomContentItem originalChild = stack.removeFirst();
            Long parentRank = originalChild.getParentRank();
            RoomContentItem parentClone = parentRank != null ? clonedParents.get(parentRank) : null;
            RoomContentItem clonedChild = cloneRoomContentItem(originalChild);
            RoomContentHierarchyHelper.attachToContainer(clonedChild, parentClone);
            if (parentClone != null) {
                updateFurniturePlacement(clonedChild,
                        parentClone,
                        originalChild.getContainerLevel(),
                        originalChild.getContainerColumn());
                containersToRefresh.add(parentClone);
            } else {
                updateFurniturePlacement(clonedChild, null, null, null);
            }
            roomContentItems.add(clonedChild);
            if (originalChild.isContainer()) {
                clonedParents.put(originalChild.getRank(), clonedChild);
                stack.addAll(originalChild.getChildren());
            }
        }
        containersToRefresh.add(newContainer);
    }

    @NonNull
    private RoomContentItem cloneRoomContentItem(@NonNull RoomContentItem original) {
        try {
            RoomContentItem copy = RoomContentItem.fromJson(original.toJson());
            copy.setRank(-1L);
            copy.setParentRank(null);
            copy.setDisplayRank(null);
            copy.setDisplayed(false);
            copy.setAttachedItemCount(0);
            copy.clearChildren();
            return copy;
        } catch (JSONException exception) {
            String fallbackName = original.getName() != null ? original.getName() : "";
            RoomContentItem copy = new RoomContentItem(fallbackName,
                    original.getComment(),
                    original.getType(),
                    original.getCategory(),
                    original.getBarcode(),
                    original.getSeries(),
                    original.getNumber(),
                    original.getAuthor(),
                    original.getPublisher(),
                    original.getEdition(),
                    original.getPublicationDate(),
                    original.getSummary(),
                    new ArrayList<>(original.getTracks()),
                    new ArrayList<>(original.getPhotos()),
                    original.isContainer(),
                    0);
            copy.setRank(-1L);
            copy.setParentRank(null);
            copy.setDisplayRank(null);
            copy.setDisplayed(false);
            copy.setContainerLevel(original.getContainerLevel());
            copy.setContainerColumn(original.getContainerColumn());
            return copy;
        }
    }

    private void removeItemsFromRoomContent(@NonNull Collection<RoomContentItem> itemsToRemove) {
        if (itemsToRemove.isEmpty()) {
            return;
        }
        Set<RoomContentItem> removalSet = Collections.newSetFromMap(new IdentityHashMap<>());
        removalSet.addAll(itemsToRemove);
        Iterator<RoomContentItem> iterator = roomContentItems.iterator();
        while (iterator.hasNext()) {
            RoomContentItem current = iterator.next();
            if (removalSet.contains(current)) {
                iterator.remove();
            }
        }
    }

    private boolean moveRoomContentItem(@NonNull RoomContentItem item, int position,
            @Nullable String targetEstablishment,
            @Nullable String targetRoom,
            @Nullable ContainerOption targetContainer,
            @Nullable Integer targetLevel,
            @Nullable Integer targetColumn) {
        return moveRoomContentItem(item, position, targetEstablishment, targetRoom,
                targetContainer, targetLevel, targetColumn, true);
    }

    private boolean moveRoomContentItem(@NonNull RoomContentItem item, int position,
            @Nullable String targetEstablishment,
            @Nullable String targetRoom,
            @Nullable ContainerOption targetContainer,
            @Nullable Integer targetLevel,
            @Nullable Integer targetColumn,
            boolean showToast) {
        String normalizedTargetRoom = normalizeName(targetRoom);
        if (normalizedTargetRoom.isEmpty()) {
            return false;
        }
        String normalizedTargetEstablishment = normalizeName(targetEstablishment);
        String normalizedSourceEstablishment = normalizeName(establishmentName);
        String normalizedSourceRoom = normalizeName(roomName);
        boolean movingWithinSameRoom = normalizedSourceEstablishment
                .equalsIgnoreCase(normalizedTargetEstablishment)
                && normalizedSourceRoom.equalsIgnoreCase(normalizedTargetRoom);
        if (movingWithinSameRoom) {
            moveWithinCurrentRoom(item, position, targetContainer, targetLevel, targetColumn);
        } else {
            moveToDifferentRoom(item, position, targetEstablishment, targetRoom, targetContainer,
                    targetLevel, targetColumn);
        }
        if (showToast) {
            Toast.makeText(this, R.string.dialog_move_room_content_success, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void moveRoomContentItems(@NonNull List<RoomContentItem> items,
            @Nullable String targetEstablishment,
            @Nullable String targetRoom,
            @Nullable ContainerOption targetContainer,
            @Nullable Integer targetLevel,
            @Nullable Integer targetColumn) {
        if (items.isEmpty()) {
            return;
        }
        Set<RoomContentItem> selection = Collections.newSetFromMap(new IdentityHashMap<>());
        selection.addAll(items);
        List<RoomContentItem> ordered = new ArrayList<>();
        for (RoomContentItem current : roomContentItems) {
            if (selection.contains(current)) {
                ordered.add(current);
            }
        }
        for (RoomContentItem current : items) {
            if (!ordered.contains(current)) {
                ordered.add(current);
            }
        }
        boolean movedAny = false;
        for (RoomContentItem current : ordered) {
            int position = roomContentItems.indexOf(current);
            if (position < 0) {
                continue;
            }
            boolean moved = moveRoomContentItem(current, position, targetEstablishment, targetRoom,
                    targetContainer, targetLevel, targetColumn, false);
            movedAny = movedAny || moved;
        }
        if (movedAny) {
            Toast.makeText(this, R.string.dialog_move_room_content_success_multiple,
                    Toast.LENGTH_SHORT).show();
            exitSelectionMode();
        }
    }

    private void moveWithinCurrentRoom(@NonNull RoomContentItem item, int position,
            @Nullable ContainerOption targetContainer,
            @Nullable Integer targetLevel,
            @Nullable Integer targetColumn) {
        if (position < 0 || position >= roomContentItems.size()) {
            return;
        }
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        MovementGroup group = extractMovementGroup(roomContentItems, position);
        markItemsAsDisplayed(group.items);
        RoomContentItem root = group.items.isEmpty() ? null : group.items.get(0);
        removeGroupAtPosition(roomContentItems, position);
        RoomContentItem target = null;
        if (targetContainer != null) {
            if (targetContainer.container != null) {
                target = targetContainer.container;
            } else {
                target = findContainerByRank(roomContentItems, targetContainer.rank);
            }
        }
        if (target != null) {
            int containerIndex = roomContentItems.indexOf(target);
            int insertionIndex;
            if (containerIndex >= 0) {
                int groupSize = RoomContentGroupingManager.computeGroupSize(roomContentItems,
                        containerIndex);
                insertionIndex = containerIndex + Math.max(1, groupSize);
                if (insertionIndex > roomContentItems.size()) {
                    insertionIndex = roomContentItems.size();
                }
            } else {
                insertionIndex = roomContentItems.size();
            }
            roomContentItems.addAll(insertionIndex, group.items);
            applyReparenting(root, target, group.items);
            updateFurniturePlacement(root, target, targetLevel, targetColumn);
        } else {
            roomContentItems.addAll(group.items);
            applyReparenting(root, null, group.items);
            updateFurniturePlacement(root, null, null, null);
        }
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        sortRoomContentItems();
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        if (roomContentAdapter != null) {
            roomContentAdapter.notifyDataSetChanged();
        }
        saveRoomContent();
        updateEmptyState();
    }

    private void moveToDifferentRoom(@NonNull RoomContentItem item, int position,
            @Nullable String targetEstablishment,
            @Nullable String targetRoom,
            @Nullable ContainerOption targetContainer,
            @Nullable Integer targetLevel,
            @Nullable Integer targetColumn) {
        if (position < 0 || position >= roomContentItems.size()) {
            return;
        }
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        MovementGroup group = extractMovementGroup(roomContentItems, position);
        markItemsAsDisplayed(group.items);
        RoomContentItem root = group.items.isEmpty() ? null : group.items.get(0);
        removeGroupAtPosition(roomContentItems, position);
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        sortRoomContentItems();
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        if (roomContentAdapter != null) {
            roomContentAdapter.notifyDataSetChanged();
        }
        saveRoomContent();
        updateEmptyState();

        List<RoomContentItem> targetItems = roomContentViewModel.loadRoomContentFor(targetEstablishment, targetRoom);
        RoomContentItem target = null;
        if (targetContainer != null) {
            target = findContainerByRank(targetItems, targetContainer.rank);
        }
        if (target != null) {
            int containerIndex = targetItems.indexOf(target);
            int insertionIndex;
            if (containerIndex >= 0) {
                int groupSize = RoomContentGroupingManager.computeGroupSize(targetItems, containerIndex);
                insertionIndex = containerIndex + Math.max(1, groupSize);
                if (insertionIndex > targetItems.size()) {
                    insertionIndex = targetItems.size();
                }
            } else {
                insertionIndex = targetItems.size();
            }
            targetItems.addAll(insertionIndex, group.items);
            applyReparenting(root, target, group.items);
            updateFurniturePlacement(root, target, targetLevel, targetColumn);
        } else {
            targetItems.addAll(group.items);
            applyReparenting(root, null, group.items);
            updateFurniturePlacement(root, null, null, null);
        }
        RoomContentHierarchyHelper.normalizeHierarchy(targetItems);
        sortRoomContentItems(targetItems);
        RoomContentHierarchyHelper.normalizeHierarchy(targetItems);
        roomContentViewModel.saveRoomContentFor(targetEstablishment, targetRoom, targetItems);
    }

    @NonNull
    private MovementGroup extractMovementGroup(@NonNull List<RoomContentItem> items, int position) {
        List<RoomContentItem> groupItems = RoomContentGroupingManager.extractGroup(items, position);
        if (groupItems.isEmpty() && position >= 0 && position < items.size()) {
            groupItems.add(items.get(position));
        }
        return new MovementGroup(groupItems);
    }

    private void applyReparenting(@Nullable RoomContentItem item,
            @Nullable RoomContentItem newParent,
            @NonNull List<RoomContentItem> movedItems) {
        if (item == null) {
            return;
        }
        RoomContentHierarchyHelper.attachToContainer(item, newParent);
        if (item.isContainer()) {
            rewriteParentLinks(item, movedItems);
        }
    }

    private void rewriteParentLinks(@NonNull RoomContentItem root,
            @NonNull List<RoomContentItem> movedItems) {
        if (movedItems.size() <= 1) {
            return;
        }
        List<ContainerTraversalState> stack = new ArrayList<>();
        if (root.isContainer()) {
            stack.add(new ContainerTraversalState(root, root.getAttachedItemCount()));
        }
        for (int index = 1; index < movedItems.size(); index++) {
            RoomContentItem current = movedItems.get(index);
            while (!stack.isEmpty() && stack.get(stack.size() - 1).remainingChildren <= 0) {
                stack.remove(stack.size() - 1);
            }
            RoomContentItem parent = stack.isEmpty() ? null : stack.get(stack.size() - 1).container;
            RoomContentHierarchyHelper.attachToContainer(current, parent);
            if (!stack.isEmpty()) {
                ContainerTraversalState state = stack.get(stack.size() - 1);
                state.remainingChildren = Math.max(0, state.remainingChildren - 1);
            }
            if (current.isContainer()) {
                stack.add(new ContainerTraversalState(current, current.getAttachedItemCount()));
            }
        }
    }

    private void updateFurniturePlacement(@Nullable RoomContentItem item,
            @Nullable RoomContentItem targetContainer,
            @Nullable Integer targetLevel,
            @Nullable Integer targetColumn) {
        if (item == null) {
            return;
        }
        if (targetContainer != null && targetContainer.isFurniture()) {
            Integer appliedLevel = normalizeFurnitureLevel(targetLevel, targetContainer);
            Integer appliedColumn = sanitizePlacementValue(targetColumn,
                    targetContainer.getFurnitureColumns());
            if (targetContainer.isStorageTower()
                    && appliedLevel != null
                    && appliedLevel > RoomContentItem.FURNITURE_BOTTOM_LEVEL
                    && appliedColumn == null) {
                appliedColumn = 1;
            }
            item.setContainerLevel(appliedLevel);
            item.setContainerColumn(appliedColumn);
        } else {
            item.setContainerLevel(null);
            item.setContainerColumn(null);
        }
    }

    @Nullable
    private Integer sanitizePlacementValue(@Nullable Integer value,
            @Nullable Integer maxValue) {
        if (value == null) {
            return null;
        }
        int sanitized = Math.max(1, value);
        if (maxValue != null && maxValue > 0 && sanitized > maxValue) {
            sanitized = maxValue;
        }
        return sanitized;
    }

    @Nullable
    private Integer normalizeFurnitureLevel(@Nullable Integer value,
            @Nullable RoomContentItem container) {
        if (value == null) {
            return null;
        }
        int sanitized = value;
        if (sanitized <= 0) {
            if (container != null && container.hasFurnitureBottom()) {
                return RoomContentItem.FURNITURE_BOTTOM_LEVEL;
            }
            sanitized = 1;
        }
        Integer maxValue = container != null ? container.getFurnitureLevels() : null;
        if (maxValue != null && maxValue > 0 && sanitized > maxValue) {
            sanitized = maxValue;
        }
        return sanitized;
    }

    private static final class ContainerTraversalState {
        final RoomContentItem container;
        int remainingChildren;

        ContainerTraversalState(@NonNull RoomContentItem container, int remainingChildren) {
            this.container = container;
            this.remainingChildren = Math.max(0, remainingChildren);
        }
    }

    private void removeGroupAtPosition(@NonNull List<RoomContentItem> items, int startPosition) {
        RoomContentGroupingManager.removeGroup(items, startPosition);
    }

    @NonNull
    private long[] toLongArray(@NonNull List<Long> values) {
        long[] result = new long[values.size()];
        for (int index = 0; index < values.size(); index++) {
            result[index] = values.get(index);
        }
        return result;
    }

    private int findIndexIgnoreCase(@NonNull List<String> values, @Nullable String target) {
        if (target == null) {
            return -1;
        }
        String normalizedTarget = target.trim();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (value != null && value.trim().equalsIgnoreCase(normalizedTarget)) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    private String normalizeName(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    @Nullable
    private static Integer parseNonNegativeInteger(@Nullable CharSequence value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.toString().trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(trimmed);
            if (parsed < 0) {
                return null;
            }
            return parsed;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Nullable
    private static Integer parsePositiveInteger(@Nullable CharSequence value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.toString().trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(trimmed);
            if (parsed <= 0) {
                return null;
            }
            return parsed;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void updateMoveDialogRooms(@NonNull AlertDialog dialog,
            @NonNull Spinner roomSpinner,
            @Nullable String establishment,
            @Nullable String preferredRoom) {
        List<String> roomNames = roomContentViewModel.loadRoomNames(establishment, establishmentName, roomName);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roomNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomSpinner.setAdapter(adapter);
        if (!roomNames.isEmpty()) {
            int index = findIndexIgnoreCase(roomNames, preferredRoom);
            roomSpinner.setSelection(index >= 0 ? index : 0);
            roomSpinner.setEnabled(true);
        } else {
            roomSpinner.setEnabled(false);
        }
    }

    private void updateMoveDialogContainers(@NonNull AlertDialog dialog,
            @Nullable RadioGroup containerGroup,
            @Nullable TextView containerLabel,
            @Nullable View furnitureDetailsContainer,
            @Nullable View furnitureLevelContainer,
            @Nullable Spinner furnitureLevelSpinner,
            @Nullable View furnitureColumnContainer,
            @Nullable Spinner furnitureColumnSpinner,
            @Nullable String establishment,
            @Nullable String room,
            @NonNull RoomContentItem item,
            int position,
            @NonNull ContainerSelection selection,
            @Nullable Set<Long> additionalExcludedRanks,
            boolean movingFurniture) {
        if (containerGroup == null) {
            refreshFurniturePlacementInputs(selection.selectedOption, item, furnitureDetailsContainer,
                    furnitureLevelContainer, furnitureLevelSpinner, furnitureColumnContainer,
                    furnitureColumnSpinner, selection);
            return;
        }
        List<ContainerOption> options = buildContainerOptions(establishment, room, item, position,
                additionalExcludedRanks, movingFurniture);
        Context context = containerGroup.getContext();
        containerGroup.setOnCheckedChangeListener(null);
        containerGroup.removeAllViews();
        if (containerLabel != null) {
            containerLabel.setVisibility(View.VISIBLE);
        }

        SparseArray<ContainerOption> optionMap = new SparseArray<>();
        int topMargin = (int) (context.getResources().getDisplayMetrics().density * 8);

        MaterialRadioButton noContainerButton = new MaterialRadioButton(context);
        noContainerButton.setId(View.generateViewId());
        noContainerButton.setUseMaterialThemeColors(true);
        noContainerButton.setText(R.string.dialog_move_room_content_no_container);
        RadioGroup.LayoutParams noneParams = new RadioGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noneParams.topMargin = 0;
        containerGroup.addView(noContainerButton, noneParams);

        Long desiredRank = selection.desiredRank != null
                ? selection.desiredRank
                : item.getParentRank();
        int checkedId = View.NO_ID;
        if (desiredRank == null) {
            checkedId = noContainerButton.getId();
            selection.selectedOption = null;
            selection.desiredRank = null;
        }

        for (ContainerOption option : options) {
            MaterialRadioButton optionButton = new MaterialRadioButton(context);
            optionButton.setId(View.generateViewId());
            optionButton.setUseMaterialThemeColors(true);
            optionButton.setText(option.label);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = topMargin;
            containerGroup.addView(optionButton, params);
            optionMap.put(optionButton.getId(), option);
            if (desiredRank != null && option.rank == desiredRank) {
                checkedId = optionButton.getId();
                selection.selectedOption = option;
                selection.desiredRank = desiredRank;
            }
        }

        final int noContainerId = noContainerButton.getId();
        if (checkedId == View.NO_ID) {
            checkedId = noContainerId;
            selection.selectedOption = null;
            selection.desiredRank = null;
            selection.desiredLevel = null;
            selection.desiredColumn = null;
        }

        containerGroup.setOnCheckedChangeListener((group, checkedRadioButtonId) -> {
            if (checkedRadioButtonId == noContainerId) {
                selection.selectedOption = null;
                selection.desiredRank = null;
                selection.desiredLevel = null;
                selection.desiredColumn = null;
            } else {
                ContainerOption selected = optionMap.get(checkedRadioButtonId);
                if (selected != null) {
                    selection.selectedOption = selected;
                    selection.desiredRank = selected.rank;
                }
            }
            refreshFurniturePlacementInputs(selection.selectedOption, item,
                    furnitureDetailsContainer, furnitureLevelContainer, furnitureLevelSpinner,
                    furnitureColumnContainer, furnitureColumnSpinner, selection);
        });
        containerGroup.check(checkedId);
        refreshFurniturePlacementInputs(selection.selectedOption, item,
                furnitureDetailsContainer, furnitureLevelContainer, furnitureLevelSpinner,
                furnitureColumnContainer, furnitureColumnSpinner, selection);
    }

    private void refreshFurniturePlacementInputs(@Nullable ContainerOption option,
            @NonNull RoomContentItem item,
            @Nullable View detailsContainer,
            @Nullable View levelContainer,
            @Nullable Spinner levelSpinner,
            @Nullable View columnContainer,
            @Nullable Spinner columnSpinner,
            @NonNull ContainerSelection selection) {
        if (detailsContainer == null) {
            return;
        }
        if (option == null || option.container == null || !option.container.isFurniture()) {
            detailsContainer.setVisibility(View.GONE);
            if (levelContainer != null) {
                levelContainer.setVisibility(View.GONE);
            }
            if (columnContainer != null) {
                columnContainer.setVisibility(View.GONE);
            }
            if (levelSpinner != null) {
                levelSpinner.setOnItemSelectedListener(null);
                levelSpinner.setAdapter(null);
            }
            if (columnSpinner != null) {
                columnSpinner.setOnItemSelectedListener(null);
                columnSpinner.setAdapter(null);
            }
            suppressFurnitureColumnSelectionCallbacks = false;
            selection.desiredLevel = null;
            selection.desiredColumn = null;
            return;
        }
        RoomContentItem furniture = option.container;
        Integer maxLevels = furniture.getFurnitureLevels();
        Integer maxColumns = furniture.getFurnitureColumns();
        List<LevelOption> levelOptions = buildLevelOptions(furniture, option.contents);
        List<ColumnOption> columnOptions = buildColumnOptions(furniture, option.contents);
        boolean showLevel = !levelOptions.isEmpty();
        boolean showColumn = !columnOptions.isEmpty();
        if (!showLevel && !showColumn) {
            detailsContainer.setVisibility(View.GONE);
            if (levelContainer != null) {
                levelContainer.setVisibility(View.GONE);
            }
            if (columnContainer != null) {
                columnContainer.setVisibility(View.GONE);
            }
            if (levelSpinner != null) {
                levelSpinner.setOnItemSelectedListener(null);
                levelSpinner.setAdapter(null);
            }
            if (columnSpinner != null) {
                columnSpinner.setOnItemSelectedListener(null);
                columnSpinner.setAdapter(null);
            }
            suppressFurnitureColumnSelectionCallbacks = false;
            selection.desiredLevel = null;
            selection.desiredColumn = null;
            return;
        }
        detailsContainer.setVisibility(View.VISIBLE);
        if (levelContainer != null) {
            levelContainer.setVisibility(showLevel ? View.VISIBLE : View.GONE);
        }
        if (columnContainer != null) {
            columnContainer.setVisibility(showColumn ? View.VISIBLE : View.GONE);
        }
        if (showLevel) {
            Integer levelValue = selection.desiredLevel;
            boolean supportsTopPlacement = furniture.hasFurnitureTop();
            if (levelValue == null) {
                boolean sameContainer = item.getParentRank() != null
                        && item.getParentRank().equals(option.rank);
                if (sameContainer) {
                    Integer currentLevel = item.getContainerLevel();
                    if (currentLevel != null) {
                        levelValue = currentLevel;
                    } else if (!supportsTopPlacement) {
                        levelValue = 1;
                    }
                } else if (!supportsTopPlacement) {
                    levelValue = 1;
                }
            }
            if (maxLevels != null && maxLevels > 0 && levelValue != null && levelValue > maxLevels) {
                levelValue = maxLevels;
            }
            selection.desiredLevel = levelValue;
            if (levelSpinner != null) {
                configureFurnitureLevelSpinner(levelSpinner, levelValue, selection,
                        levelOptions);
            }
        } else {
            selection.desiredLevel = null;
            if (levelSpinner != null) {
                levelSpinner.setOnItemSelectedListener(null);
                levelSpinner.setAdapter(null);
            }
        }
        if (showColumn) {
            Integer columnValue = selection.desiredColumn;
            if (columnValue == null) {
                boolean sameContainer = item.getParentRank() != null
                        && item.getParentRank().equals(option.rank);
                if (sameContainer && item.getContainerColumn() != null) {
                    columnValue = item.getContainerColumn();
                }
                if (columnValue == null) {
                    columnValue = 1;
                }
            }
            if (maxColumns != null && maxColumns > 0 && columnValue != null
                    && columnValue > maxColumns) {
                columnValue = maxColumns;
            }
            selection.desiredColumn = columnValue;
            if (columnSpinner != null) {
                configureFurnitureColumnSpinner(columnSpinner, columnValue, selection,
                        columnOptions);
            }
        } else {
            selection.desiredColumn = null;
            if (columnSpinner != null) {
                columnSpinner.setOnItemSelectedListener(null);
                columnSpinner.setAdapter(null);
            }
            suppressFurnitureColumnSelectionCallbacks = false;
        }
    }

    private void configureFurnitureLevelSpinner(@NonNull Spinner levelSpinner,
            @Nullable Integer desiredLevel,
            @NonNull ContainerSelection selection,
            @NonNull List<LevelOption> levelOptions) {
        LevelSpinnerAdapter adapter;
        if (levelSpinner.getAdapter() instanceof LevelSpinnerAdapter) {
            adapter = (LevelSpinnerAdapter) levelSpinner.getAdapter();
            adapter.updateOptions(levelOptions);
        } else {
            adapter = new LevelSpinnerAdapter(this, levelOptions);
            levelSpinner.setAdapter(adapter);
        }
        if (adapter.getCount() == 0) {
            levelSpinner.setOnItemSelectedListener(null);
            selection.desiredLevel = null;
            return;
        }
        int selectionIndex = adapter.findPositionByValue(desiredLevel);
        if (selectionIndex < 0) {
            selectionIndex = 0;
        }
        suppressFurnitureLevelSelectionCallbacks = true;
        levelSpinner.setSelection(selectionIndex, false);
        suppressFurnitureLevelSelectionCallbacks = false;
        LevelOption currentOption = adapter.getItem(selectionIndex);
        selection.desiredLevel = currentOption != null ? currentOption.value : null;
        LevelSpinnerAdapter finalAdapter = adapter;
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressFurnitureLevelSelectionCallbacks) {
                    return;
                }
                LevelOption option = finalAdapter.getItem(position);
                selection.desiredLevel = option != null ? option.value : null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (!suppressFurnitureLevelSelectionCallbacks) {
                    selection.desiredLevel = null;
                }
            }
        });
    }

    private void configureFurnitureColumnSpinner(@NonNull Spinner columnSpinner,
            @Nullable Integer desiredColumn,
            @NonNull ContainerSelection selection,
            @NonNull List<ColumnOption> columnOptions) {
        ColumnSpinnerAdapter adapter;
        if (columnSpinner.getAdapter() instanceof ColumnSpinnerAdapter) {
            adapter = (ColumnSpinnerAdapter) columnSpinner.getAdapter();
            adapter.updateOptions(columnOptions);
        } else {
            adapter = new ColumnSpinnerAdapter(this, columnOptions);
            columnSpinner.setAdapter(adapter);
        }
        if (adapter.getCount() == 0) {
            columnSpinner.setOnItemSelectedListener(null);
            selection.desiredColumn = null;
            return;
        }
        int selectionIndex = -1;
        if (desiredColumn != null) {
            selectionIndex = adapter.findPositionByValue(desiredColumn);
        }
        if (selectionIndex < 0) {
            selectionIndex = 0;
        }
        suppressFurnitureColumnSelectionCallbacks = true;
        columnSpinner.setSelection(selectionIndex, false);
        suppressFurnitureColumnSelectionCallbacks = false;
        ColumnOption currentOption = adapter.getItem(selectionIndex);
        selection.desiredColumn = currentOption != null ? currentOption.value : null;
        ColumnSpinnerAdapter finalAdapter = adapter;
        columnSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressFurnitureColumnSelectionCallbacks) {
                    return;
                }
                ColumnOption option = finalAdapter.getItem(position);
                selection.desiredColumn = option != null ? option.value : null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (!suppressFurnitureColumnSelectionCallbacks) {
                    selection.desiredColumn = null;
                }
            }
        });
    }

    @NonNull
    private List<LevelOption> buildLevelOptions(@NonNull RoomContentItem furniture,
            @NonNull List<RoomContentItem> contents) {
        LinkedHashSet<Integer> values = new LinkedHashSet<>();
        boolean includeTopOption = furniture.hasFurnitureTop();
        if (!includeTopOption) {
            for (RoomContentItem child : contents) {
                if (child == null) {
                    continue;
                }
                if (child.getContainerLevel() == null) {
                    includeTopOption = true;
                    break;
                }
            }
        }
        if (furniture.hasFurnitureBottom()) {
            values.add(RoomContentItem.FURNITURE_BOTTOM_LEVEL);
        }
        Integer maxLevels = furniture.getFurnitureLevels();
        if (maxLevels != null && maxLevels > 0) {
            for (int index = 1; index <= maxLevels; index++) {
                values.add(index);
            }
        } else {
            for (RoomContentItem child : contents) {
                if (child == null) {
                    continue;
                }
                Integer level = child.getContainerLevel();
                if (level != null && level >= RoomContentItem.FURNITURE_BOTTOM_LEVEL) {
                    values.add(level);
                }
            }
            if (values.isEmpty()) {
                values.add(1);
            }
        }
        List<LevelOption> options = new ArrayList<>(values.size() + (includeTopOption ? 1 : 0));
        if (includeTopOption) {
            String topLabel = getString(R.string.furniture_popup_top_title);
            options.add(new LevelOption(null, topLabel));
        }
        for (Integer value : values) {
            if (value == null) {
                continue;
            }
            String label;
            if (value == RoomContentItem.FURNITURE_BOTTOM_LEVEL) {
                label = getString(R.string.furniture_popup_bottom_title);
            } else if (furniture.isStorageTower()) {
                label = getString(R.string.storage_tower_popup_drawer_title, value);
            } else {
                label = getString(R.string.furniture_popup_level_title, value);
            }
            options.add(new LevelOption(value, label));
        }
        return options;
    }

    @NonNull
    private List<ColumnOption> buildColumnOptions(@NonNull RoomContentItem furniture,
            @NonNull List<RoomContentItem> contents) {
        LinkedHashSet<Integer> values = new LinkedHashSet<>();
        Integer maxColumns = furniture.getFurnitureColumns();
        if (maxColumns != null && maxColumns > 0) {
            for (int index = 1; index <= maxColumns; index++) {
                values.add(index);
            }
        } else {
            for (RoomContentItem child : contents) {
                if (child == null) {
                    continue;
                }
                Integer column = child.getContainerColumn();
                if (column != null && column > 0) {
                    values.add(column);
                }
            }
            if (values.isEmpty()) {
                values.add(1);
            }
        }
        List<Integer> sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues);
        List<ColumnOption> options = new ArrayList<>(sortedValues.size());
        for (Integer value : sortedValues) {
            if (value == null) {
                continue;
            }
            String label = getString(R.string.room_content_furniture_column_short, value);
            options.add(new ColumnOption(value, label));
        }
        return options;
    }

    private static final class LevelOption {
        @Nullable
        final Integer value;
        @NonNull
        final String label;

        LevelOption(@Nullable Integer value, @NonNull String label) {
            this.value = value;
            this.label = label;
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }
    }

    private static final class LevelSpinnerAdapter extends ArrayAdapter<LevelOption> {
        LevelSpinnerAdapter(@NonNull Context context, @NonNull List<LevelOption> options) {
            super(context, android.R.layout.simple_spinner_item, new ArrayList<>(options));
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        void updateOptions(@NonNull List<LevelOption> options) {
            clear();
            addAll(options);
            notifyDataSetChanged();
        }

        int findPositionByValue(@Nullable Integer value) {
            for (int index = 0; index < getCount(); index++) {
                LevelOption option = getItem(index);
                if (option == null) {
                    continue;
                }
                if (option.value == null) {
                    if (value == null) {
                        return index;
                    }
                } else if (value != null && option.value.equals(value)) {
                    return index;
                }
            }
            return -1;
        }
    }

    private static final class ColumnOption {
        final int value;
        @NonNull
        final String label;

        ColumnOption(int value, @NonNull String label) {
            this.value = value;
            this.label = label;
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }
    }

    private static final class ColumnSpinnerAdapter extends ArrayAdapter<ColumnOption> {
        ColumnSpinnerAdapter(@NonNull Context context, @NonNull List<ColumnOption> options) {
            super(context, R.layout.item_furniture_column_spinner, new ArrayList<>(options));
            setDropDownViewResource(R.layout.item_furniture_column_spinner_dropdown);
        }

        void updateOptions(@NonNull List<ColumnOption> options) {
            clear();
            addAll(options);
            notifyDataSetChanged();
        }

        int findPositionByValue(int value) {
            for (int index = 0; index < getCount(); index++) {
                ColumnOption option = getItem(index);
                if (option != null && option.value == value) {
                    return index;
                }
            }
            return -1;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(
                        R.layout.item_furniture_column_spinner, parent, false);
            }
            TextView textView = view.findViewById(R.id.text_column_spinner);
            ColumnOption option = getItem(position);
            if (textView != null && option != null) {
                textView.setText(option.label);
            }
            return view;
        }

        @NonNull
        @Override
        public View getDropDownView(int position, @Nullable View convertView,
                @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(
                        R.layout.item_furniture_column_spinner_dropdown, parent, false);
            }
            TextView textView = view.findViewById(R.id.text_column_spinner_dropdown);
            ColumnOption option = getItem(position);
            if (textView != null && option != null) {
                textView.setText(option.label);
            }
            return view;
        }
    }

    private void updateMoveButtonState(@NonNull AlertDialog dialog, @NonNull Spinner roomSpinner) {
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton == null) {
            return;
        }
        boolean hasRooms = roomSpinner.getAdapter() != null
                && roomSpinner.getAdapter().getCount() > 0;
        positiveButton.setEnabled(hasRooms);
    }

    private static boolean containsFurniture(@NonNull List<RoomContentItem> items) {
        for (RoomContentItem current : items) {
            if (!current.isFurniture()) {
                continue;
            }
            if (current.isStorageTower()) {
                // Les tours de rangement doivent pouvoir être déplacées vers un autre
                // mobilier : ne pas les compter comme du mobilier "bloquant".
                continue;
            }
            return true;
        }
        return false;
    }

    @NonNull
    private List<ContainerOption> buildContainerOptions(@Nullable String establishment,
            @Nullable String room,
            @NonNull RoomContentItem item,
            int position,
            @Nullable Set<Long> additionalExcludedRanks,
            boolean movingFurniture) {
        List<ContainerOption> result = new ArrayList<>();
        if (movingFurniture) {
            return result;
        }
        String normalizedEstablishment = normalizeName(establishment);
        String normalizedCurrentEstablishment = normalizeName(establishmentName);
        String normalizedRoom = normalizeName(room);
        String normalizedCurrentRoom = normalizeName(roomName);
        boolean sameEstablishment = normalizedEstablishment.equalsIgnoreCase(normalizedCurrentEstablishment);
        boolean sameRoom = sameEstablishment && normalizedRoom.equalsIgnoreCase(normalizedCurrentRoom);
        boolean movingStorageTower = item.isStorageTower();
        if (sameRoom) {
            RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
            Set<Long> excludedRanks = collectExcludedContainerRanks(roomContentItems, position);
            if (additionalExcludedRanks != null && !additionalExcludedRanks.isEmpty()) {
                excludedRanks.addAll(additionalExcludedRanks);
            }
            for (RoomContentItem candidate : roomContentItems) {
                if (!candidate.isContainer()) {
                    continue;
                }
                if (movingFurniture && candidate.isStorageTower()) {
                    continue;
                }
                if (movingStorageTower) {
                    // Une tour de rangement ne peut pas être placée dans un contenant ni dans
                    // une autre tour : seules les options de mobilier sont conservées.
                    if (!candidate.isFurniture()) {
                        continue;
                    }
                    if (candidate.isStorageTower()) {
                        continue;
                    }
                }
                if (excludedRanks.contains(candidate.getRank())) {
                    continue;
                }
                String label = candidate.getName();
                if (label == null || label.trim().isEmpty()) {
                    label = getString(R.string.dialog_room_content_item_placeholder);
                }
                List<RoomContentItem> children = collectChildrenForRank(roomContentItems,
                        candidate.getRank());
                result.add(new ContainerOption(label, candidate.getRank(), candidate, children));
            }
        } else {
            List<RoomContentItem> targetItems = roomContentViewModel.loadRoomContentFor(establishment, room);
            RoomContentHierarchyHelper.normalizeHierarchy(targetItems);
            for (RoomContentItem candidate : targetItems) {
                if (!candidate.isContainer()) {
                    continue;
                }
                if (movingFurniture && candidate.isStorageTower()) {
                    continue;
                }
                if (movingStorageTower) {
                    if (!candidate.isFurniture()) {
                        continue;
                    }
                    if (candidate.isStorageTower()) {
                        continue;
                    }
                }
                String label = candidate.getName();
                if (label == null || label.trim().isEmpty()) {
                    label = getString(R.string.dialog_room_content_item_placeholder);
                }
                List<RoomContentItem> children = collectChildrenForRank(targetItems,
                        candidate.getRank());
                result.add(new ContainerOption(label, candidate.getRank(), candidate, children));
            }
        }
        return result;
    }

    @NonNull
    private List<RoomContentItem> collectChildrenForRank(@NonNull List<RoomContentItem> items,
            long parentRank) {
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        List<RoomContentItem> matches = new ArrayList<>();
        for (RoomContentItem candidate : items) {
            if (candidate == null) {
                continue;
            }
            Long candidateParent = candidate.getParentRank();
            if (candidateParent != null && candidateParent == parentRank) {
                matches.add(candidate);
            }
        }
        return matches;
    }

    @NonNull
    static Set<Long> collectExcludedContainerRanks(@NonNull List<RoomContentItem> items,
            int position) {
        Set<Long> ranks = collectGroupRanks(items, position);
        return ranks;
    }

    @NonNull
    private static Set<Long> collectGroupRanks(@NonNull List<RoomContentItem> items, int position) {
        Set<Long> ranks = new HashSet<>();
        List<RoomContentItem> groupItems = RoomContentGroupingManager.extractGroup(items, position);
        if (groupItems.isEmpty() && position >= 0 && position < items.size()) {
            groupItems.add(items.get(position));
        }
        for (RoomContentItem current : groupItems) {
            ranks.add(current.getRank());
        }
        return ranks;
    }

    @Nullable
    private static RoomContentItem findContainerByRank(@NonNull List<RoomContentItem> items, long rank) {
        for (RoomContentItem candidate : items) {
            if (!candidate.isContainer()) {
                continue;
            }
            if (candidate.getRank() == rank) {
                return candidate;
            }
        }
        return null;
    }

    private void loadRoomContent() {
        SharedPreferences preferences = getSharedPreferences(RoomContentStorage.PREFS_NAME, MODE_PRIVATE);
        String resolvedKey = RoomContentStorage.resolveKey(preferences, establishmentName, roomName);
        String storedValue = preferences.getString(resolvedKey, null);
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
            RoomContentStorage.ensureCanonicalKey(preferences, establishmentName, roomName, resolvedKey);
        } catch (JSONException e) {
            roomContentItems.clear();
            preferences.edit().remove(resolvedKey).apply();
        }
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        removeKitchenBiblioFurnitureIfNeeded();
        sortRoomContentItems();
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        if (roomContentAdapter != null) {
            roomContentAdapter.collapseAllContainers();
            roomContentAdapter.notifyDataSetChanged();
        }
    }

    private void preserveHierarchyMetadata(@NonNull RoomContentItem source,
                                           @NonNull RoomContentItem destination) {
        destination.setRank(source.getRank());
        destination.setParentRank(source.getParentRank());
        destination.setDisplayed(source.isDisplayed());
        destination.setAttachedItemCount(source.getAttachedItemCount());
        destination.setDisplayRank(source.getDisplayRank());
        destination.setContainerLevel(source.getContainerLevel());
        destination.setContainerColumn(source.getContainerColumn());
    }

    private void saveRoomContent() {
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        JSONArray array = new JSONArray();
        for (RoomContentItem item : roomContentItems) {
            array.put(item.toJson());
        }
        SharedPreferences preferences = getSharedPreferences(RoomContentStorage.PREFS_NAME, MODE_PRIVATE);
        preferences.edit()
                .putString(buildRoomContentKey(), array.toString())
                .apply();
    }

    private void sortRoomContentItems() {
        sortRoomContentItems(roomContentItems);
    }

    private void sortRoomContentItems(@NonNull List<RoomContentItem> items) {
        RoomContentHierarchyHelper.normalizeHierarchy(items);
        Comparator<RoomContentItem> comparator = new Comparator<RoomContentItem>() {
            @Override
            public int compare(RoomContentItem first, RoomContentItem second) {
                int locationComparison = compareContainerLocation(first, second);
                if (locationComparison != 0) {
                    return locationComparison;
                }
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
                int nameComparison = firstName.compareToIgnoreCase(secondName);
                if (nameComparison != 0) {
                    return nameComparison;
                }
                return Long.compare(first.getRank(), second.getRank());
            }
        };
        RoomContentGroupingManager.sortWithComparator(items, comparator);
        RoomContentHierarchyHelper.normalizeHierarchy(items);
    }

    @NonNull
    private String normalizeForSort(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    private int compareContainerLocation(@NonNull RoomContentItem first,
            @NonNull RoomContentItem second) {
        Integer firstLevel = first.getContainerLevel();
        Integer secondLevel = second.getContainerLevel();
        if (firstLevel == null && secondLevel == null) {
            return 0;
        }
        if (firstLevel == null) {
            return -1;
        }
        if (secondLevel == null) {
            return 1;
        }
        int firstColumn = normalizeContainerColumn(first.getContainerColumn());
        int secondColumn = normalizeContainerColumn(second.getContainerColumn());
        if (firstColumn != secondColumn) {
            return Integer.compare(firstColumn, secondColumn);
        }
        int normalizedFirstLevel = normalizeContainerLevel(firstLevel);
        int normalizedSecondLevel = normalizeContainerLevel(secondLevel);
        if (normalizedFirstLevel != normalizedSecondLevel) {
            return Integer.compare(normalizedFirstLevel, normalizedSecondLevel);
        }
        return 0;
    }

    private int normalizeContainerLevel(@Nullable Integer level) {
        if (level == null) {
            return Integer.MIN_VALUE;
        }
        if (level <= RoomContentItem.FURNITURE_BOTTOM_LEVEL) {
            return RoomContentItem.FURNITURE_BOTTOM_LEVEL + 1;
        }
        return level;
    }

    private int normalizeContainerColumn(@Nullable Integer column) {
        if (column == null || column <= 0) {
            return 1;
        }
        return column;
    }

    private static final class MovementGroup {
        @NonNull
        final List<RoomContentItem> items;

        MovementGroup(@NonNull List<RoomContentItem> items) {
            this.items = items;
        }
    }

    private static final class ContainerOption {
        @NonNull
        final String label;
        final long rank;
        @Nullable
        final RoomContentItem container;
        @NonNull
        final List<RoomContentItem> contents;

        ContainerOption(@NonNull String label, long rank, @Nullable RoomContentItem container,
                @Nullable List<RoomContentItem> contents) {
            this.label = label;
            this.rank = rank;
            this.container = container;
            if (contents == null || contents.isEmpty()) {
                this.contents = Collections.emptyList();
            } else {
                this.contents = new ArrayList<>(contents);
            }
        }
    }

    private static final class ContainerSelection {
        @Nullable
        ContainerOption selectedOption;
        @Nullable
        Long desiredRank;
        @Nullable
        Integer desiredLevel;
        @Nullable
        Integer desiredColumn;
    }

    private void markItemsAsDisplayed(@NonNull List<RoomContentItem> items) {
        for (RoomContentItem item : items) {
            item.setDisplayed(true);
        }
    }

    private int findInsertionIndexForContainer(@NonNull RoomContentItem container) {
        RoomContentHierarchyHelper.normalizeHierarchy(roomContentItems);
        int containerIndex = roomContentItems.indexOf(container);
        if (containerIndex < 0) {
            return roomContentItems.size();
        }
        int groupSize = RoomContentGroupingManager.computeGroupSize(roomContentItems, containerIndex);
        if (groupSize <= 0) {
            groupSize = 1;
        }
        int insertionIndex = containerIndex + groupSize;
        if (insertionIndex > roomContentItems.size()) {
            insertionIndex = roomContentItems.size();
        }
        return insertionIndex;
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

    private void scheduleContainerIndicatorRefresh(@NonNull Set<RoomContentItem> containers) {
        if (containers.isEmpty()) {
            return;
        }
        RecyclerView list = contentList;
        RoomContentAdapter adapter = roomContentAdapter;
        if (list == null || adapter == null) {
            return;
        }
        list.post(() -> {
            RoomContentAdapter currentAdapter = roomContentAdapter;
            if (currentAdapter == null) {
                return;
            }
            for (RoomContentItem container : containers) {
                if (container == null) {
                    continue;
                }
                int position = findAdapterPositionForItem(container);
                if (position == RecyclerView.NO_POSITION) {
                    continue;
                }
                currentAdapter.notifyItemChanged(position);
            }
        });
    }

    private void refreshPhotoSection(@NonNull FormState formState) {
        if (formState.photoLabel != null) {
            int templateRes = formState.photoLabelTemplateRes != 0
                    ? formState.photoLabelTemplateRes
                    : R.string.dialog_label_room_content_photos_template;
            formState.photoLabel.setText(getString(templateRes, formState.photos.size()));
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

    private void showTrackListDialog(@NonNull FormState formState) {
        View dialogView = inflateDialogView(R.layout.dialog_track_list_input);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText trackListInput = dialogView.findViewById(R.id.input_track_list);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel_track_list);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm_track_list);

        if (trackListInput != null && !formState.tracks.isEmpty()) {
            trackListInput.setText(TextUtils.join("\n", formState.tracks));
            trackListInput.setSelection(trackListInput.getText().length());
        }

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> {
                String rawText = trackListInput != null && trackListInput.getText() != null
                        ? trackListInput.getText().toString()
                        : "";
                String trimmedText = rawText.trim();
                if (trimmedText.isEmpty()) {
                    if (trackListInput != null) {
                        trackListInput.setError(null);
                    }
                    formState.tracks.clear();
                    refreshTrackInputs(formState);
                    if (formState.trackContainer != null) {
                        formState.trackContainer.setVisibility(View.GONE);
                    }
                    dialog.dismiss();
                    return;
                }

                String[] lines = rawText.split("\r?\n");
                List<String> parsedTracks = new ArrayList<>();
                for (String line : lines) {
                    String trimmed = line != null ? line.trim() : "";
                    if (!trimmed.isEmpty()) {
                        parsedTracks.add(trimmed);
                    }
                }

                if (parsedTracks.isEmpty()) {
                    if (trackListInput != null) {
                        trackListInput.setError(getString(R.string.dialog_track_list_error_empty));
                        trackListInput.requestFocus();
                    }
                    return;
                }

                formState.tracks.clear();
                formState.tracks.addAll(parsedTracks);
                refreshTrackInputs(formState);
                if (formState.trackContainer != null) {
                    formState.trackContainer.setVisibility(View.VISIBLE);
                }
                if (trackListInput != null) {
                    trackListInput.setError(null);
                }
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void showPhotoPreview(@NonNull FormState formState, int startIndex) {
        if (formState.photos.isEmpty()) {
            return;
        }

        View previewView = inflateDialogView(R.layout.dialog_photo_preview);
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

    private String buildRoomContentKeyFor(@Nullable String establishment, @Nullable String room) {
        return RoomContentStorage.buildKey(establishment, room);
    }

    private String buildRoomContentKey() {
        return RoomContentStorage.buildKey(establishmentName, roomName);
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
                    showRoomContentDialog(itemToEdit, pendingBarcodeResult.positionToEdit,
                            pendingBarcodeResult.editing,
                            pendingBarcodeResult.forcedParentRank,
                            pendingBarcodeResult.forcedFurnitureLevel);
                } else {
                    showRoomContentDialog(null, -1, false,
                            pendingBarcodeResult != null
                                    ? pendingBarcodeResult.forcedParentRank
                                    : null,
                            pendingBarcodeResult != null
                                    ? pendingBarcodeResult.forcedFurnitureLevel
                                    : null);
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
        updateBarcodePreview(context.barcodePreviewView, trimmed, false);
        updateBarcodeClearButton(context.clearBarcodeButton, true);
        Toast.makeText(this, R.string.dialog_barcode_lookup_in_progress, Toast.LENGTH_SHORT).show();
        fetchMetadataForBarcode(trimmed, context);
    }

    private void fetchMetadataForBarcode(@NonNull String barcode,
                                         @NonNull BarcodeScanContext context) {
        barcodeLookupExecutor.execute(() -> {
            BarcodeLookupResult lookupResult = roomContentViewModel.performLookup(barcode, MAX_FORM_PHOTOS);
            runOnUiThread(() -> applyBarcodeLookupResult(barcode, context, lookupResult));
        });
    }

    private void applyBarcodeLookupResult(@NonNull String barcode,
                                          @NonNull BarcodeScanContext context,
                                          @NonNull BarcodeLookupResult result) {
        updateBarcodePreview(context.barcodePreviewView, barcode, false);
        updateBarcodeClearButton(context.clearBarcodeButton, true);
        if (result.getErrorMessage() != null) {
            Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
            barcodeScanContext = null;
            pendingBarcodeResult = null;
            return;
        }
        if (!result.getFound()) {
            String message = result.getInfoMessage() != null
                    ? result.getInfoMessage()
                    : getString(R.string.dialog_barcode_lookup_not_found, barcode);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            barcodeScanContext = null;
            pendingBarcodeResult = null;
            refreshPhotoSection(context.formState);
            return;
        }
        if (!TextUtils.isEmpty(result.getTitle()) && context.nameInput != null) {
            context.nameInput.setText(result.getTitle());
            context.nameInput.setSelection(result.getTitle().length());
        }
        if (!TextUtils.isEmpty(result.getAuthor()) && context.authorInput != null) {
            context.authorInput.setText(result.getAuthor());
        }
        if (!TextUtils.isEmpty(result.getPublisher()) && context.publisherInput != null) {
            context.publisherInput.setText(result.getPublisher());
        }
        if (!TextUtils.isEmpty(result.getSeries()) && context.seriesInput != null) {
            context.seriesInput.setText(result.getSeries());
        }
        if (!TextUtils.isEmpty(result.getNumber()) && context.numberInput != null) {
            context.numberInput.setText(result.getNumber());
        }
        if (!TextUtils.isEmpty(result.getEdition()) && context.editionInput != null) {
            context.editionInput.setText(result.getEdition());
        }
        if (!TextUtils.isEmpty(result.getPublishDate()) && context.publicationDateInput != null) {
            context.publicationDateInput.setText(result.getPublishDate());
        }
        if (!TextUtils.isEmpty(result.getSummary()) && context.summaryInput != null) {
            context.summaryInput.setText(result.getSummary());
        }
        if (!TextUtils.isEmpty(result.getTypeLabel())) {
            context.selectedTypeHolder[0] = result.getTypeLabel();
            applyTypeConfiguration(result.getTypeLabel(), context.formState, context.bookFields,
                    context.trackFields, context.trackTitle, context.typeFieldViews);
            updateSelectionButtonText(context.selectTypeButton, result.getTypeLabel(),
                    R.string.dialog_button_choose_type);
        }
        boolean photosAdded = false;
        if (!result.getPhotos().isEmpty()) {
            photosAdded = addPhotosToForm(context.formState, result.getPhotos());
        }
        if (!photosAdded) {
            refreshPhotoSection(context.formState);
        }
        String message = result.getInfoMessage() != null
                ? result.getInfoMessage()
                : getString(R.string.dialog_barcode_lookup_success);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        barcodeScanContext = null;
        pendingBarcodeResult = null;
    }

    private void bindBarcodeValue(@Nullable TextView barcodeValueView,
                                  @Nullable ImageView barcodePreviewView,
                                  @Nullable ImageButton clearBarcodeButton,
                                  @Nullable String barcode) {
        String trimmed = barcode != null ? barcode.trim() : "";
        if (barcodeValueView != null) {
            if (trimmed.isEmpty()) {
                barcodeValueView.setText(R.string.dialog_label_barcode_placeholder);
            } else {
                barcodeValueView.setText(trimmed);
            }
        }
        if (trimmed.isEmpty()) {
            updateBarcodePreview(barcodePreviewView, null, false);
            updateBarcodeClearButton(clearBarcodeButton, false);
        } else {
            updateBarcodePreview(barcodePreviewView, trimmed, false);
            updateBarcodeClearButton(clearBarcodeButton, true);
        }
    }

    private void clearBarcodeValue(@Nullable TextView barcodeValueView,
                                   @Nullable ImageView barcodePreviewView,
                                   @Nullable ImageButton clearBarcodeButton) {
        if (barcodeValueView != null) {
            barcodeValueView.setText(R.string.dialog_label_barcode_placeholder);
        }
        updateBarcodePreview(barcodePreviewView, null, false);
        updateBarcodeClearButton(clearBarcodeButton, false);
        if (pendingBarcodeResult != null) {
            pendingBarcodeResult.barcode = null;
            pendingBarcodeResult.resumeLookup = false;
        }
    }

    private void updateBarcodeClearButton(@Nullable ImageButton clearBarcodeButton,
                                          boolean hasValue) {
        if (clearBarcodeButton == null) {
            return;
        }
        clearBarcodeButton.setEnabled(hasValue);
        clearBarcodeButton.setVisibility(hasValue ? View.VISIBLE : View.GONE);
    }

    private boolean updateBarcodePreview(@Nullable ImageView previewView,
                                         @Nullable String barcodeValue,
                                         boolean notifyOnError) {
        if (previewView == null) {
            return true;
        }
        String trimmed = barcodeValue != null ? barcodeValue.trim() : "";
        if (trimmed.isEmpty()) {
            previewView.setImageDrawable(null);
            previewView.setVisibility(View.GONE);
            previewView.setContentDescription(getString(R.string.dialog_barcode_preview_placeholder_description));
            return true;
        }
        try {
            Bitmap bitmap = createBarcodeBitmap(trimmed);
            previewView.setImageBitmap(bitmap);
            previewView.setVisibility(View.VISIBLE);
            previewView.setContentDescription(getString(R.string.dialog_barcode_preview_content_description, trimmed));
            return true;
        } catch (WriterException | IllegalArgumentException e) {
            previewView.setImageDrawable(null);
            previewView.setVisibility(View.GONE);
            previewView.setContentDescription(getString(R.string.dialog_barcode_preview_placeholder_description));
            if (notifyOnError) {
                Toast.makeText(this, R.string.dialog_error_generate_barcode_failed, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

    @NonNull
    private Bitmap createBarcodeBitmap(@NonNull String value) throws WriterException {
        int width = getResources().getDimensionPixelSize(R.dimen.barcode_preview_width);
        int height = getResources().getDimensionPixelSize(R.dimen.barcode_preview_height);
        BitMatrix matrix = new MultiFormatWriter().encode(value,
                BarcodeFormat.CODE_128,
                width,
                height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    @NonNull
    private BarcodeScanContext createBarcodeScanContext(@NonNull FormState formState,
                                                        @Nullable EditText nameInput,
                                                        @Nullable TextView barcodeValueView,
                                                        @Nullable ImageView barcodePreviewView,
                                                        @Nullable ImageButton clearBarcodeButton,
                                                        @Nullable Button selectTypeButton,
                                                        @Nullable View bookFields,
                                                        @Nullable View trackFields,
                                                        @Nullable TextView trackTitle,
                                                        @NonNull String[] selectedTypeHolder,
                                                        @NonNull TypeFieldViews typeFieldViews,
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
                barcodePreviewView,
                clearBarcodeButton,
                selectTypeButton,
                bookFields,
                trackFields,
                trackTitle,
                selectedTypeHolder,
                typeFieldViews,
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
                                                             @NonNull FormState formState,
                                                             @Nullable Long forcedParentRank,
                                                             @Nullable Integer forcedFurnitureLevel) {
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
        result.forcedParentRank = forcedParentRank;
        result.forcedFurnitureLevel = forcedFurnitureLevel;
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

    private void showEditCategoryDialog(List<String> categoryOptions,
                                        @Nullable CategorySelectorAdapter adapter,
                                        @Nullable ArrayAdapter<String> spinnerAdapter,
                                        int position,
                                        @NonNull String currentLabel,
                                        @Nullable Button selectCategoryButton,
                                        @NonNull String[] selectedCategoryHolder,
                                        @Nullable Spinner categorySpinner) {
        View dialogView = inflateDialogView(R.layout.dialog_add_category);
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
                if (containsIgnoreCase(categoryOptions, trimmedName, position)) {
                    categoryNameInput.setError(getString(R.string.error_category_name_duplicate));
                    categoryNameInput.requestFocus();
                    return;
                }
                categoryOptions.set(position, trimmedName);
                persistCategoryOptions(categoryOptions);
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
        if (addMenuPopup != null) {
            addMenuPopup.dismiss();
            addMenuPopup = null;
        }
        currentAddMenuState = null;
        pendingAddMenuRestoreState = null;
        pendingContainerPopupState = null;
        pendingFurniturePopupState = null;
        pendingOptionsPopupState = null;
        if (metadataStorage != null) {
            metadataStorage.close();
            metadataStorage = null;
        }
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
                    persistCategoryOptions(categoryOptions);
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
                                    @NonNull FormState formState,
                                    @Nullable View bookFields,
                                    @Nullable View trackFields,
                                    @Nullable TextView trackTitle,
                                    @NonNull Set<String> lockedTypes,
                                    @NonNull TypeFieldViews typeFieldViews) {
        View dialogView = inflateDialogView(R.layout.dialog_add_type);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        TextView titleView = dialogView.findViewById(R.id.text_add_type_title);
        EditText typeNameInput = dialogView.findViewById(R.id.input_new_type_name);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);
        final Map<String, CheckBox> fieldCheckboxes = buildTypeFieldCheckboxes(dialogView,
                resolveFieldsForType(currentLabel));
        final String[] selectedDateFormatHolder = new String[] {
                resolveDateFormatForType(currentLabel)
        };
        CheckBox dateCheckBox = fieldCheckboxes.get(FIELD_DATE);
        if (dateCheckBox != null) {
            String baseLabel = getString(R.string.dialog_type_field_date);
            setupDateFieldSelection(dateCheckBox, baseLabel, selectedDateFormatHolder);
        }

        if (lockedTypes.contains(currentLabel)) {
            dialog.dismiss();
            return;
        }

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
                if (containsIgnoreCase(typeOptions, trimmedName, position)) {
                    typeNameInput.setError(getString(R.string.error_type_name_duplicate));
                    typeNameInput.requestFocus();
                    return;
                }
                Set<String> selectedFields = collectSelectedFields(fieldCheckboxes);
                if (!selectedFields.contains(FIELD_DATE)) {
                    selectedDateFormatHolder[0] = null;
                } else if (sanitizeDateFormatSelection(selectedDateFormatHolder[0]) == null) {
                    selectedDateFormatHolder[0] = getDefaultDateFormatKey();
                }
                typeOptions.set(position, trimmedName);
                saveTypeFieldConfiguration(trimmedName, selectedFields, selectedDateFormatHolder[0]);
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
                    applyTypeConfiguration(trimmedName, formState, bookFields, trackFields,
                            trackTitle, typeFieldViews);
                } else if (selectedTypeHolder[0] != null
                        && selectedTypeHolder[0].equals(trimmedName)) {
                    applyTypeConfiguration(trimmedName, formState, bookFields, trackFields,
                            trackTitle, typeFieldViews);
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
                                            @NonNull FormState formState,
                                            @Nullable View bookFields,
                                            @Nullable View trackFields,
                                            @Nullable TextView trackTitle,
                                            @NonNull Set<String> lockedTypes,
                                            @NonNull TypeFieldViews typeFieldViews) {
        if (position < 0 || position >= typeOptions.size()) {
            return;
        }
        String typeLabel = typeOptions.get(position);
        if (lockedTypes.contains(typeLabel)) {
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.dialog_delete_type_message, typeLabel))
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    String removed = typeOptions.remove(position);
                    removeTypeFieldConfiguration(removed);
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
                        applyTypeConfiguration(null, formState, bookFields, trackFields,
                                trackTitle, typeFieldViews);
                    }
                })
                .create()
                .show();
    }

    private void showAddCategoryDialog(List<String> categoryOptions,
                                       @Nullable CategorySelectorAdapter adapter,
                                       @Nullable RecyclerView recyclerView,
                                       @Nullable ArrayAdapter<String> spinnerAdapter,
                                       @Nullable Button selectCategoryButton,
                                       @NonNull String[] selectedCategoryHolder,
                                       @Nullable Spinner categorySpinner) {
        View dialogView = inflateDialogView(R.layout.dialog_add_category);
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
                if (containsIgnoreCase(categoryOptions, trimmedName)) {
                    categoryNameInput.setError(getString(R.string.error_category_name_duplicate));
                    categoryNameInput.requestFocus();
                    return;
                }
                categoryOptions.add(trimmedName);
                persistCategoryOptions(categoryOptions);
                if (adapter != null) {
                    adapter.addCategory(trimmedName);
                    adapter.setSelectedCategory(trimmedName);
                    if (recyclerView != null) {
                        recyclerView.post(() ->
                                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1));
                    }
                }
                if (spinnerAdapter != null) {
                    spinnerAdapter.notifyDataSetChanged();
                    if (categorySpinner != null) {
                        int index = categoryOptions.indexOf(trimmedName);
                        if (index >= 0) {
                            categorySpinner.setSelection(index);
                        }
                    }
                }
                selectedCategoryHolder[0] = trimmedName;
                updateSelectionButtonText(selectCategoryButton, trimmedName,
                        R.string.dialog_button_choose_category);
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
        View dialogView = inflateDialogView(R.layout.dialog_add_type);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        EditText typeNameInput = dialogView.findViewById(R.id.input_new_type_name);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);
        final Map<String, CheckBox> fieldCheckboxes = buildTypeFieldCheckboxes(dialogView,
                getDefaultTypeFields());
        final String[] selectedDateFormatHolder = new String[1];
        CheckBox dateCheckBox = fieldCheckboxes.get(FIELD_DATE);
        if (dateCheckBox != null) {
            String baseLabel = getString(R.string.dialog_type_field_date);
            setupDateFieldSelection(dateCheckBox, baseLabel, selectedDateFormatHolder);
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
                if (containsIgnoreCase(typeOptions, trimmedName)) {
                    typeNameInput.setError(getString(R.string.error_type_name_duplicate));
                    typeNameInput.requestFocus();
                    return;
                }
                Set<String> selectedFields = collectSelectedFields(fieldCheckboxes);
                if (!selectedFields.contains(FIELD_DATE)) {
                    selectedDateFormatHolder[0] = null;
                } else if (sanitizeDateFormatSelection(selectedDateFormatHolder[0]) == null) {
                    selectedDateFormatHolder[0] = getDefaultDateFormatKey();
                }
                typeOptions.add(trimmedName);
                saveTypeFieldConfiguration(trimmedName, selectedFields, selectedDateFormatHolder[0]);
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

    private void loadTypeFieldConfigurationsFromStorage() {
        typeFieldConfigurations.clear();
        if (metadataStorage == null) {
            return;
        }
        Map<String, Set<String>> stored = metadataStorage.loadTypeFieldConfigurations();
        for (Map.Entry<String, Set<String>> entry : stored.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            String trimmed = key.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            typeFieldConfigurations.put(trimmed, sanitizeFieldSelection(entry.getValue()));
        }
    }

    private void persistTypeFieldConfigurations() {
        if (metadataStorage == null) {
            return;
        }
        Map<String, Set<String>> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : typeFieldConfigurations.entrySet()) {
            String label = entry.getKey();
            if (label == null) {
                continue;
            }
            String trimmed = label.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            snapshot.put(trimmed, sanitizeFieldSelection(entry.getValue()));
        }
        metadataStorage.saveTypeFieldConfigurations(snapshot);
    }

    private void loadTypeDateFormatsFromStorage() {
        typeDateFormats.clear();
        if (metadataStorage == null) {
            return;
        }
        Map<String, String> stored = metadataStorage.loadTypeDateFormats();
        for (Map.Entry<String, String> entry : stored.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            String trimmed = key.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String sanitized = sanitizeDateFormatSelection(entry.getValue());
            if (sanitized != null) {
                typeDateFormats.put(trimmed, sanitized);
            }
        }
    }

    private void persistTypeDateFormats() {
        if (metadataStorage == null) {
            return;
        }
        Map<String, String> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : typeDateFormats.entrySet()) {
            String label = entry.getKey();
            if (label == null) {
                continue;
            }
            String trimmed = label.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String sanitized = sanitizeDateFormatSelection(entry.getValue());
            if (sanitized != null) {
                snapshot.put(trimmed, sanitized);
            }
        }
        metadataStorage.saveTypeDateFormats(snapshot);
    }

    @NonNull
    private LinkedHashSet<String> getDefaultTypeFields() {
        LinkedHashSet<String> defaults = new LinkedHashSet<>();
        defaults.add(FIELD_NAME);
        defaults.add(FIELD_COMMENT);
        defaults.add(FIELD_PHOTOS);
        return defaults;
    }

    @NonNull
    private LinkedHashSet<String> sanitizeFieldSelection(@Nullable Set<String> fields) {
        if (fields == null) {
            return getDefaultTypeFields();
        }
        LinkedHashSet<String> sanitized = new LinkedHashSet<>();
        sanitized.add(FIELD_NAME);
        for (String field : fields) {
            if (field == null) {
                continue;
            }
            String trimmed = field.trim();
            if (trimmed.isEmpty() || FIELD_NAME.equals(trimmed)) {
                continue;
            }
            if (FIELD_COMMENT.equals(trimmed)
                    || FIELD_DATE.equals(trimmed)
                    || FIELD_PHOTOS.equals(trimmed)
                    || FIELD_CUSTOM_1.equals(trimmed)
                    || FIELD_CUSTOM_2.equals(trimmed)) {
                sanitized.add(trimmed);
            }
        }
        return sanitized;
    }

    @Nullable
    private String findMatchingKeyIgnoreCase(@NonNull Map<String, ?> map, @NonNull String key) {
        for (String entryKey : map.keySet()) {
            if (entryKey != null && entryKey.trim().equalsIgnoreCase(key)) {
                return entryKey;
            }
        }
        return null;
    }

    private boolean mapContainsKeyIgnoreCase(@NonNull Map<String, ?> map, @NonNull String key) {
        return findMatchingKeyIgnoreCase(map, key) != null;
    }

    private void ensureDefaultTypeConfigurations() {
        boolean fieldsChanged = false;
        boolean formatsChanged = false;
        String[] defaults = new String[] {
                getString(R.string.dialog_type_book),
                getString(R.string.dialog_type_magazine),
                getString(R.string.dialog_type_comic)
        };
        for (String value : defaults) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String existingFieldKey = findMatchingKeyIgnoreCase(typeFieldConfigurations, trimmed);
            if (existingFieldKey == null) {
                LinkedHashSet<String> defaultFields = getDefaultTypeFields();
                defaultFields.add(FIELD_DATE);
                typeFieldConfigurations.put(trimmed, sanitizeFieldSelection(defaultFields));
                fieldsChanged = true;
            } else {
                Set<String> currentFields = typeFieldConfigurations.get(existingFieldKey);
                LinkedHashSet<String> sanitized = sanitizeFieldSelection(currentFields);
                if (!sanitized.contains(FIELD_DATE)) {
                    sanitized.add(FIELD_DATE);
                    typeFieldConfigurations.put(existingFieldKey, sanitized);
                    fieldsChanged = true;
                }
            }
            if (!mapContainsKeyIgnoreCase(typeDateFormats, trimmed)) {
                typeDateFormats.put(trimmed, getDefaultDateFormatKey());
                formatsChanged = true;
            }
        }
        if (fieldsChanged) {
            persistTypeFieldConfigurations();
        }
        if (formatsChanged) {
            persistTypeDateFormats();
        }
    }

    private boolean removeTypeFieldConfigurationInternal(@NonNull String typeLabel) {
        String trimmed = typeLabel.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        Iterator<Map.Entry<String, Set<String>>> iterator = typeFieldConfigurations.entrySet().iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            Map.Entry<String, Set<String>> entry = iterator.next();
            String key = entry.getKey();
            if (key != null && key.trim().equalsIgnoreCase(trimmed)) {
                iterator.remove();
                removed = true;
            }
        }
        Iterator<Map.Entry<String, String>> dateIterator = typeDateFormats.entrySet().iterator();
        boolean formatRemoved = false;
        while (dateIterator.hasNext()) {
            Map.Entry<String, String> entry = dateIterator.next();
            String key = entry.getKey();
            if (key != null && key.trim().equalsIgnoreCase(trimmed)) {
                dateIterator.remove();
                formatRemoved = true;
            }
        }
        return removed || formatRemoved;
    }

    private void removeTypeFieldConfiguration(@Nullable String typeLabel) {
        if (typeLabel == null) {
            return;
        }
        if (removeTypeFieldConfigurationInternal(typeLabel)) {
            persistTypeFieldConfigurations();
            persistTypeDateFormats();
        }
    }

    private void saveTypeFieldConfiguration(@NonNull String typeLabel,
                                            @NonNull Set<String> fields,
                                            @Nullable String dateFormat) {
        String trimmed = typeLabel.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        removeTypeFieldConfigurationInternal(trimmed);
        typeFieldConfigurations.put(trimmed, sanitizeFieldSelection(fields));
        String sanitizedFormat = sanitizeDateFormatSelection(dateFormat);
        if (sanitizedFormat != null) {
            typeDateFormats.put(trimmed, sanitizedFormat);
        }
        persistTypeFieldConfigurations();
        persistTypeDateFormats();
    }

    @NonNull
    private Set<String> resolveFieldsForType(@Nullable String typeLabel) {
        if (typeLabel == null) {
            return getDefaultTypeFields();
        }
        String trimmed = typeLabel.trim();
        if (trimmed.isEmpty()) {
            return getDefaultTypeFields();
        }
        for (Map.Entry<String, Set<String>> entry : typeFieldConfigurations.entrySet()) {
            String key = entry.getKey();
            if (key != null && key.trim().equalsIgnoreCase(trimmed)) {
                return new LinkedHashSet<>(sanitizeFieldSelection(entry.getValue()));
            }
        }
        return getDefaultTypeFields();
    }

    @Nullable
    private String resolveDateFormatForType(@Nullable String typeLabel) {
        if (typeLabel == null) {
            return null;
        }
        String trimmed = typeLabel.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : typeDateFormats.entrySet()) {
            String key = entry.getKey();
            if (key != null && key.trim().equalsIgnoreCase(trimmed)) {
                return sanitizeDateFormatSelection(entry.getValue());
            }
        }
        return null;
    }

    @NonNull
    private List<String> getCustomFieldLabels() {
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.dialog_custom_field_label_1));
        labels.add(getString(R.string.dialog_custom_field_label_2));
        return labels;
    }

    @NonNull
    private List<TypeFieldOption> getTypeFieldOptions() {
        List<TypeFieldOption> options = new ArrayList<>();
        options.add(new TypeFieldOption(FIELD_NAME, getString(R.string.dialog_type_field_name), true));
        options.add(new TypeFieldOption(FIELD_COMMENT, getString(R.string.dialog_type_field_comment), false));
        options.add(new TypeFieldOption(FIELD_DATE, getString(R.string.dialog_type_field_date), false));
        options.add(new TypeFieldOption(FIELD_PHOTOS, getString(R.string.dialog_type_field_photos), false));
        List<String> customLabels = getCustomFieldLabels();
        if (!customLabels.isEmpty()) {
            options.add(new TypeFieldOption(FIELD_CUSTOM_1, customLabels.get(0), false));
            if (customLabels.size() > 1) {
                options.add(new TypeFieldOption(FIELD_CUSTOM_2, customLabels.get(1), false));
            }
        }
        return options;
    }

    @NonNull
    private List<DateFormatOption> getDateFormatOptions() {
        List<DateFormatOption> options = new ArrayList<>();
        options.add(new DateFormatOption(DATE_FORMAT_EUROPEAN, R.string.dialog_date_format_european));
        options.add(new DateFormatOption(DATE_FORMAT_ENGLISH, R.string.dialog_date_format_english));
        options.add(new DateFormatOption(DATE_FORMAT_ISO, R.string.dialog_date_format_iso));
        return options;
    }

    @NonNull
    private String getDefaultDateFormatKey() {
        return DATE_FORMAT_EUROPEAN;
    }

    @Nullable
    private DateFormatOption findDateFormatOption(@Nullable String key) {
        if (key == null) {
            return null;
        }
        String trimmed = key.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        for (DateFormatOption option : getDateFormatOptions()) {
            if (option.key.equals(trimmed)) {
                return option;
            }
        }
        return null;
    }

    @Nullable
    private String sanitizeDateFormatSelection(@Nullable String formatKey) {
        DateFormatOption option = findDateFormatOption(formatKey);
        return option != null ? option.key : null;
    }

    @Nullable
    private String getDateFormatDisplayName(@Nullable String formatKey) {
        DateFormatOption option = findDateFormatOption(formatKey);
        return option != null ? getString(option.labelRes) : null;
    }

    private void updateDateFieldCheckboxLabel(@NonNull CheckBox checkBox,
                                              @NonNull String baseLabel,
                                              @Nullable String formatKey) {
        String display = getDateFormatDisplayName(formatKey);
        if (display == null) {
            checkBox.setText(baseLabel);
        } else {
            checkBox.setText(getString(R.string.dialog_type_field_date_with_format, display));
        }
    }

    private void setupDateFieldSelection(@NonNull CheckBox dateCheckBox,
                                         @NonNull String baseLabel,
                                         @NonNull String[] selectedDateFormatHolder) {
        if (dateCheckBox.isChecked()) {
            String sanitized = sanitizeDateFormatSelection(selectedDateFormatHolder[0]);
            if (sanitized == null) {
                sanitized = getDefaultDateFormatKey();
            }
            selectedDateFormatHolder[0] = sanitized;
        } else {
            selectedDateFormatHolder[0] = null;
        }
        updateDateFieldCheckboxLabel(dateCheckBox, baseLabel, selectedDateFormatHolder[0]);
        final CompoundButton.OnCheckedChangeListener[] listenerHolder = new CompoundButton.OnCheckedChangeListener[1];
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) {
                showDateFormatSelectionDialog(selectedDateFormatHolder[0], formatKey -> {
                    String sanitized = sanitizeDateFormatSelection(formatKey);
                    if (sanitized == null) {
                        sanitized = getDefaultDateFormatKey();
                    }
                    selectedDateFormatHolder[0] = sanitized;
                    updateDateFieldCheckboxLabel(dateCheckBox, baseLabel, selectedDateFormatHolder[0]);
                }, () -> {
                    buttonView.setOnCheckedChangeListener(null);
                    buttonView.setChecked(false);
                    buttonView.setOnCheckedChangeListener(listenerHolder[0]);
                    selectedDateFormatHolder[0] = null;
                    updateDateFieldCheckboxLabel(dateCheckBox, baseLabel, null);
                });
            } else {
                selectedDateFormatHolder[0] = null;
                updateDateFieldCheckboxLabel(dateCheckBox, baseLabel, null);
            }
        };
        listenerHolder[0] = listener;
        dateCheckBox.setOnCheckedChangeListener(listener);
        dateCheckBox.setOnLongClickListener(v -> {
            if (!dateCheckBox.isChecked()) {
                return false;
            }
            showDateFormatSelectionDialog(selectedDateFormatHolder[0], formatKey -> {
                String sanitized = sanitizeDateFormatSelection(formatKey);
                if (sanitized == null) {
                    sanitized = getDefaultDateFormatKey();
                }
                selectedDateFormatHolder[0] = sanitized;
                updateDateFieldCheckboxLabel(dateCheckBox, baseLabel, selectedDateFormatHolder[0]);
            }, () -> {
            });
            return true;
        });
    }

    private void showDateFormatSelectionDialog(@Nullable String currentSelection,
                                               @NonNull OnDateFormatSelectedListener listener,
                                               @NonNull Runnable onCancelled) {
        List<DateFormatOption> options = getDateFormatOptions();
        CharSequence[] items = new CharSequence[options.size()];
        int selectedIndex = -1;
        for (int i = 0; i < options.size(); i++) {
            DateFormatOption option = options.get(i);
            items[i] = getString(option.labelRes);
            if (option.key.equals(currentSelection)) {
                selectedIndex = i;
            }
        }
        final String[] selectedHolder = new String[1];
        if (selectedIndex >= 0) {
            selectedHolder[0] = options.get(selectedIndex).key;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_date_format_title)
                .setSingleChoiceItems(items, selectedIndex, (d, which) -> {
                    selectedHolder[0] = options.get(which).key;
                })
                .setPositiveButton(R.string.action_confirm, (d, which) -> {
                    String value = selectedHolder[0];
                    if (value == null && !options.isEmpty()) {
                        value = options.get(0).key;
                    }
                    if (value != null) {
                        listener.onDateFormatSelected(value);
                    }
                })
                .setNegativeButton(R.string.action_cancel, (d, which) -> onCancelled.run())
                .setOnCancelListener(d -> onCancelled.run())
                .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    ContextCompat.getDrawable(this, R.drawable.bg_room_content_popup));
        }
    }

    @NonNull
    private Map<String, CheckBox> buildTypeFieldCheckboxes(@NonNull View dialogView,
                                                           @NonNull Set<String> selectedFields) {
        LinearLayout container = dialogView.findViewById(R.id.container_type_fields);
        TextView titleView = dialogView.findViewById(R.id.text_type_fields_title);
        Map<String, CheckBox> checkBoxes = new LinkedHashMap<>();
        if (container == null) {
            if (titleView != null) {
                titleView.setVisibility(View.GONE);
            }
            return checkBoxes;
        }
        container.removeAllViews();
        List<TypeFieldOption> options = getTypeFieldOptions();
        for (TypeFieldOption option : options) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(option.label);
            boolean checked = option.mandatory || selectedFields.contains(option.key);
            checkBox.setChecked(checked);
            checkBox.setEnabled(!option.mandatory);
            checkBox.setTag(option.key);
            container.addView(checkBox);
            checkBoxes.put(option.key, checkBox);
        }
        if (titleView != null) {
            titleView.setVisibility(options.isEmpty() ? View.GONE : View.VISIBLE);
        }
        return checkBoxes;
    }

    @NonNull
    private Set<String> collectSelectedFields(@NonNull Map<String, CheckBox> checkBoxes) {
        LinkedHashSet<String> selected = new LinkedHashSet<>();
        for (Map.Entry<String, CheckBox> entry : checkBoxes.entrySet()) {
            CheckBox checkBox = entry.getValue();
            if (checkBox != null && checkBox.isChecked()) {
                selected.add(entry.getKey());
            }
        }
        return selected;
    }

    private void applyTypeConfiguration(@Nullable String selectedType,
                                        @NonNull FormState formState,
                                        @Nullable View bookFields,
                                        @Nullable View trackFields,
                                        @Nullable TextView trackTitle,
                                        @NonNull TypeFieldViews typeFieldViews) {
        updateTypeSpecificFields(bookFields, trackFields, trackTitle, selectedType);
        applyTypeFieldConfiguration(selectedType, formState, typeFieldViews);
    }

    private void applyTypeFieldConfiguration(@Nullable String selectedType,
                                             @NonNull FormState formState,
                                             @NonNull TypeFieldViews views) {
        Set<String> selectedFields = resolveFieldsForType(selectedType);
        String dateFormat = resolveDateFormatForType(selectedType);

        boolean showName = selectedFields.contains(FIELD_NAME);
        if (views.nameLabel != null) {
            views.nameLabel.setVisibility(showName ? View.VISIBLE : View.GONE);
        }
        if (views.nameInput != null) {
            views.nameInput.setVisibility(showName ? View.VISIBLE : View.GONE);
            if (!showName) {
                views.nameInput.setText("");
                views.nameInput.setError(null);
            }
        }

        boolean showComment = selectedFields.contains(FIELD_COMMENT);
        if (views.commentLabel != null) {
            views.commentLabel.setVisibility(showComment ? View.VISIBLE : View.GONE);
        }
        if (views.commentInput != null) {
            views.commentInput.setVisibility(showComment ? View.VISIBLE : View.GONE);
            if (!showComment) {
                views.commentInput.setText("");
                views.commentInput.setError(null);
            }
        }

        boolean showDate = selectedFields.contains(FIELD_DATE);
        String dateFormatDisplay = getDateFormatDisplayName(dateFormat);
        if (views.dateLabel != null) {
            if (showDate) {
                if (dateFormatDisplay != null) {
                    views.dateLabel.setText(
                            getString(R.string.dialog_label_publication_date_with_format, dateFormatDisplay));
                } else {
                    views.dateLabel.setText(R.string.dialog_label_publication_date);
                }
                views.dateLabel.setVisibility(View.VISIBLE);
            } else {
                views.dateLabel.setVisibility(View.GONE);
                views.dateLabel.setText(R.string.dialog_label_publication_date);
            }
        }
        if (views.dateInput != null) {
            if (showDate) {
                views.dateInput.setHint(dateFormatDisplay);
                views.dateInput.setVisibility(View.VISIBLE);
            } else {
                views.dateInput.setVisibility(View.GONE);
                views.dateInput.setText("");
                views.dateInput.setError(null);
                views.dateInput.setHint(null);
            }
        }

        boolean showPhotos = selectedFields.contains(FIELD_PHOTOS);
        if (formState.photoLabel != null) {
            formState.photoLabel.setVisibility(showPhotos ? View.VISIBLE : View.GONE);
        }
        if (formState.addPhotoButton != null) {
            formState.addPhotoButton.setVisibility(showPhotos ? View.VISIBLE : View.GONE);
            formState.addPhotoButton.setEnabled(showPhotos
                    && formState.photos.size() < MAX_FORM_PHOTOS);
        }
        if (formState.photoContainer != null) {
            if (showPhotos) {
                formState.photoContainer.setVisibility(formState.photos.isEmpty()
                        ? View.GONE
                        : View.VISIBLE);
            } else {
                formState.photoContainer.setVisibility(View.GONE);
            }
        }

        boolean showCustomFieldOne = selectedFields.contains(FIELD_CUSTOM_1);
        if (views.customFieldOne != null) {
            views.customFieldOne.setVisibility(showCustomFieldOne ? View.VISIBLE : View.GONE);
        }
        if (!showCustomFieldOne && views.customValueOne != null) {
            views.customValueOne.setText("");
        }

        boolean showCustomFieldTwo = selectedFields.contains(FIELD_CUSTOM_2);
        if (views.customFieldTwo != null) {
            views.customFieldTwo.setVisibility(showCustomFieldTwo ? View.VISIBLE : View.GONE);
        }
        if (!showCustomFieldTwo && views.customValueTwo != null) {
            views.customValueTwo.setText("");
        }

        if (views.customSection != null) {
            views.customSection.setVisibility((showCustomFieldOne || showCustomFieldTwo)
                    ? View.VISIBLE
                    : View.GONE);
        }
        if (views.customDivider != null) {
            views.customDivider.setVisibility(showCustomFieldOne && showCustomFieldTwo
                    ? View.VISIBLE
                    : View.GONE);
        }
    }

    private void prepareBottomSheetDialog(@NonNull BottomSheetDialog bottomSheetDialog) {
        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog sheetDialog = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = sheetDialog
                    .findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                behavior.setFitToContents(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    @NonNull
    private View inflateDialogView(@LayoutRes int layoutResId) {
        ViewGroup root = findViewById(android.R.id.content);
        LayoutInflater inflater = LayoutInflater.from(this);
        if (root != null) {
            return inflater.inflate(layoutResId, root, false);
        }
        FrameLayout fallbackRoot = new FrameLayout(this);
        return inflater.inflate(layoutResId, fallbackRoot, false);
    }

    private void persistCategoryOptions(@NonNull List<String> categoryOptions) {
        if (metadataStorage == null) {
            return;
        }
        metadataStorage.saveCategories(categoryOptions);
    }

    @NonNull
    private List<String> loadStoredCategories() {
        if (metadataStorage == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(metadataStorage.loadCategories());
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
        String magazineType = getString(R.string.dialog_type_magazine);
        String comicType = getString(R.string.dialog_type_comic);
        String cdType = getString(R.string.dialog_type_cd);
        String discType = getString(R.string.dialog_type_disc);

        boolean showBookFields = selectedType != null
                && (selectedType.equals(bookType)
                || selectedType.equals(magazineType)
                || selectedType.equals(comicType));
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

    private boolean containsIgnoreCase(@NonNull List<String> values,
                                       @Nullable String candidate) {
        return containsIgnoreCase(values, candidate, -1);
    }

    private boolean containsIgnoreCase(@NonNull List<String> values,
                                       @Nullable String candidate,
                                       int ignoreIndex) {
        if (candidate == null) {
            return false;
        }
        String target = candidate.trim();
        if (target.isEmpty()) {
            return false;
        }
        for (int i = 0; i < values.size(); i++) {
            if (i == ignoreIndex) {
                continue;
            }
            String value = values.get(i);
            if (value != null && value.trim().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }
}
