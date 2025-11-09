package com.example.new1;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Affiche une boîte de dialogue permettant d'effectuer une recherche dans un établissement.
 */
final class EstablishmentSearchDialog {

    private static final Pattern DIACRITICS_PATTERN =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final String TAG = "EstablishmentSearch";

    private EstablishmentSearchDialog() {
    }

    static void show(@NonNull Activity activity, @Nullable String establishmentName) {
        if (activity.isFinishing()) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(activity);
        View contentView = inflater.inflate(R.layout.dialog_establishment_search, null);
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(contentView);

        TextView titleView = contentView.findViewById(R.id.text_search_title);
        EditText queryInput = contentView.findViewById(R.id.input_search_query);
        Button searchButton = contentView.findViewById(R.id.button_run_search);
        TextView statusView = contentView.findViewById(R.id.text_search_status);
        TableLayout resultTable = contentView.findViewById(R.id.table_search_results);
        View establishmentContainer = contentView.findViewById(R.id.container_establishment_selector);
        Spinner establishmentSpinner = contentView.findViewById(R.id.spinner_establishment_selector);

        String normalizedEstablishment = normalizeEstablishmentName(establishmentName);
        boolean requiresSelection = TextUtils.isEmpty(normalizedEstablishment);
        final boolean[] hasEstablishments = new boolean[]{true};

        EstablishmentSearchViewModel viewModel;
        try {
            viewModel = new ViewModelProvider(
                    activity,
                    EstablishmentSearchViewModel.provideFactory(activity.getApplication())
            ).get(EstablishmentSearchViewModel.class);
        } catch (RuntimeException | UnsatisfiedLinkError | ExceptionInInitializerError exception) {
            Log.e(TAG, "Impossible d'initialiser la recherche sécurisée.", exception);
            Toast.makeText(activity, R.string.search_storage_error, Toast.LENGTH_LONG).show();
            dialog.dismiss();
            return;
        }

        List<Establishment> establishmentRecords = viewModel.loadEstablishmentsSnapshot();
        Map<String, Establishment> establishmentsByNormalizedName = new HashMap<>();
        List<String> establishmentNames = new ArrayList<>();
        for (Establishment establishment : establishmentRecords) {
            if (establishment == null) {
                continue;
            }
            String name = establishment.getName();
            if (name == null) {
                continue;
            }
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!containsIgnoreCase(establishmentNames, trimmed)) {
                establishmentNames.add(trimmed);
            }
            establishmentsByNormalizedName.put(normalize(trimmed), establishment);
        }
        Collections.sort(establishmentNames, (left, right) -> left.compareToIgnoreCase(right));

        if (requiresSelection) {
            if (establishmentContainer != null) {
                establishmentContainer.setVisibility(View.VISIBLE);
            }
            if (establishmentNames.isEmpty()) {
                hasEstablishments[0] = false;
                updateDialogTitle(titleView, activity, null);
                if (statusView != null) {
                    statusView.setText(R.string.search_dialog_status_no_establishments);
                    statusView.setVisibility(View.VISIBLE);
                }
                if (searchButton != null) {
                    searchButton.setEnabled(false);
                }
            } else if (establishmentSpinner != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                        android.R.layout.simple_spinner_item, establishmentNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                establishmentSpinner.setAdapter(adapter);
                if (statusView != null) {
                    statusView.setVisibility(View.GONE);
                }
                if (searchButton != null) {
                    searchButton.setEnabled(true);
                }
                updateDialogTitle(titleView, activity,
                        (String) establishmentSpinner.getSelectedItem());
                establishmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Object selected = parent.getItemAtPosition(position);
                        updateDialogTitle(titleView, activity,
                                selected instanceof String ? (String) selected : null);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        updateDialogTitle(titleView, activity, null);
                    }
                });
            }
        } else {
            if (!establishmentsByNormalizedName.containsKey(normalizedEstablishment)) {
                hasEstablishments[0] = false;
            }
            updateDialogTitle(titleView, activity, normalizedEstablishment);
            if (establishmentContainer != null) {
                establishmentContainer.setVisibility(View.GONE);
            }
        }

        View closeButton = contentView.findViewById(R.id.button_close_search);
        if (closeButton != null) {
            closeButton.setOnClickListener(view -> dialog.dismiss());
        }

        Runnable triggerSearch = () -> {
            if (queryInput == null || searchButton == null || statusView == null
                    || resultTable == null) {
                return;
            }
            String rawQuery = queryInput.getText() != null
                    ? queryInput.getText().toString().trim()
                    : "";
            if (rawQuery.isEmpty()) {
                statusView.setText(R.string.search_dialog_status_empty_query);
                statusView.setVisibility(View.VISIBLE);
                resultTable.removeAllViews();
                return;
            }
            hideKeyboard(activity, queryInput);
            if (requiresSelection && !hasEstablishments[0]) {
                statusView.setText(R.string.search_dialog_status_no_establishments);
                statusView.setVisibility(View.VISIBLE);
                resultTable.removeAllViews();
                return;
            }
            String selectedEstablishment = resolveEstablishmentName(normalizedEstablishment,
                    establishmentSpinner);
            if (TextUtils.isEmpty(selectedEstablishment)) {
                statusView.setText(R.string.search_dialog_status_missing_establishment);
                statusView.setVisibility(View.VISIBLE);
                resultTable.removeAllViews();
                return;
            }
            final Establishment selectedRecord = establishmentsByNormalizedName.get(
                    normalize(selectedEstablishment));
            if (selectedRecord == null || selectedRecord.getId() == null
                    || selectedRecord.getId().trim().isEmpty()) {
                statusView.setText(R.string.search_dialog_status_missing_establishment);
                statusView.setVisibility(View.VISIBLE);
                resultTable.removeAllViews();
                return;
            }
            statusView.setText(R.string.search_dialog_status_loading);
            statusView.setVisibility(View.VISIBLE);
            searchButton.setEnabled(false);
            resultTable.removeAllViews();
            EXECUTOR.execute(() -> {
                List<SearchMatch> matches = performSearch(activity.getApplicationContext(),
                        viewModel, selectedRecord, rawQuery);
                MAIN_HANDLER.post(() -> {
                    if (!activity.isFinishing()) {
                        populateResults(activity, resultTable, statusView, matches);
                        searchButton.setEnabled(true);
                    }
                });
            });
        };

        if (searchButton != null) {
            searchButton.setOnClickListener(view -> triggerSearch.run());
        }

        if (queryInput != null) {
            queryInput.setOnEditorActionListener((textView, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    triggerSearch.run();
                    return true;
                }
                return false;
            });
        }

        dialog.show();
    }

    private static void hideKeyboard(@NonNull Activity activity, @NonNull View target) {
        InputMethodManager imm = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(target.getWindowToken(), 0);
        }
    }

    @NonNull
    private static String normalizeEstablishmentName(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    @Nullable
    private static String resolveEstablishmentName(@NonNull String normalized,
            @Nullable Spinner spinner) {
        if (!normalized.isEmpty()) {
            return normalized;
        }
        if (spinner == null) {
            return null;
        }
        Object selected = spinner.getSelectedItem();
        if (!(selected instanceof String)) {
            return null;
        }
        String trimmed = ((String) selected).trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void updateDialogTitle(@Nullable TextView titleView,
            @NonNull Activity activity,
            @Nullable String establishment) {
        if (titleView == null) {
            return;
        }
        String normalized = establishment != null ? establishment.trim() : "";
        if (normalized.isEmpty()) {
            titleView.setText(R.string.search_dialog_title_generic);
        } else {
            titleView.setText(activity.getString(R.string.search_dialog_title_with_name, normalized));
        }
    }

    private static boolean containsIgnoreCase(@NonNull List<String> values, @NonNull String target) {
        for (String value : values) {
            if (value != null && value.trim().equalsIgnoreCase(target.trim())) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private static List<SearchMatch> performSearch(@NonNull Context context,
            @NonNull EstablishmentSearchViewModel viewModel,
            @NonNull Establishment establishment,
            @NonNull String query) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) {
            return Collections.emptyList();
        }
        List<RoomRecord> rooms = loadRooms(viewModel, establishment);
        if (rooms.isEmpty()) {
            SearchMatch placeholder = SearchMatch.noData();
            return Collections.singletonList(placeholder);
        }
        String establishmentName = establishment.getName();
        List<SearchMatch> matches = new ArrayList<>();
        for (RoomRecord room : rooms) {
            List<RoomContentItem> items = loadRoomContent(viewModel, establishment, room.name);
            if (items.isEmpty()) {
                continue;
            }
            Map<Long, RoomContentItem> byRank = new HashMap<>();
            for (RoomContentItem item : items) {
                byRank.put(item.getRank(), item);
            }
            for (RoomContentItem item : items) {
                evaluateFurniture(matches, room.name, item, byRank, normalizedQuery);
                evaluateContainer(matches, room.name, item, byRank, normalizedQuery);
                evaluateElement(matches, room.name, item, byRank, normalizedQuery);
            }
        }
        Collections.sort(matches, buildComparator());
        return matches;
    }

    private static void evaluateFurniture(@NonNull List<SearchMatch> matches,
            @NonNull String roomName,
            @NonNull RoomContentItem item,
            @NonNull Map<Long, RoomContentItem> byRank,
            @NonNull String normalizedQuery) {
        if (!item.isFurniture()) {
            return;
        }
        if (matchesField(item.getName(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_name, item.getName(), false));
        }
        if (matchesField(item.getComment(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_comment, item.getComment(), false));
        }
    }

    private static void evaluateContainer(@NonNull List<SearchMatch> matches,
            @NonNull String roomName,
            @NonNull RoomContentItem item,
            @NonNull Map<Long, RoomContentItem> byRank,
            @NonNull String normalizedQuery) {
        if (!item.isContainer() || item.isFurniture()) {
            return;
        }
        if (matchesField(item.getName(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_name, item.getName(), true));
        }
        if (matchesField(item.getComment(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_comment, item.getComment(), true));
        }
    }

    private static void evaluateElement(@NonNull List<SearchMatch> matches,
            @NonNull String roomName,
            @NonNull RoomContentItem item,
            @NonNull Map<Long, RoomContentItem> byRank,
            @NonNull String normalizedQuery) {
        if (item.isFurniture() || item.isContainer()) {
            return;
        }
        if (matchesField(item.getName(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_name, item.getName(), false));
        }
        if (matchesField(item.getComment(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_comment, item.getComment(), false));
        }
        if (matchesField(item.getType(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_type, item.getType(), false));
        }
        if (matchesField(item.getCategory(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_category, item.getCategory(), false));
        }
        if (matchesField(item.getBarcode(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_barcode, item.getBarcode(), false));
        }
        if (matchesField(item.getSeries(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_series, item.getSeries(), false));
        }
        if (matchesField(item.getNumber(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_number, item.getNumber(), false));
        }
        if (matchesField(item.getAuthor(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_author, item.getAuthor(), false));
        }
        if (matchesField(item.getPublisher(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_publisher, item.getPublisher(), false));
        }
        if (matchesField(item.getEdition(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_edition, item.getEdition(), false));
        }
        if (matchesField(item.getPublicationDate(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_publication_date, item.getPublicationDate(), false));
        }
        if (matchesField(item.getSummary(), normalizedQuery)) {
            matches.add(buildMatch(roomName, item, item, byRank,
                    R.string.search_field_summary, item.getSummary(), false));
        }
        for (String track : item.getTracks()) {
            if (matchesField(track, normalizedQuery)) {
                matches.add(buildMatch(roomName, item, item, byRank,
                        R.string.search_field_track, track, false));
            }
        }
    }

    private static boolean matchesField(@Nullable String value, @NonNull String normalizedQuery) {
        String normalized = normalize(value);
        return !normalized.isEmpty() && normalized.contains(normalizedQuery);
    }

    @NonNull
    private static Comparator<SearchMatch> buildComparator() {
        return (first, second) -> {
            int roomComparison = compareStrings(first.roomName, second.roomName);
            if (roomComparison != 0) {
                return roomComparison;
            }
            int furnitureComparison = compareStrings(first.furnitureName, second.furnitureName);
            if (furnitureComparison != 0) {
                return furnitureComparison;
            }
            int towerComparison = compareStrings(first.storageTowerName, second.storageTowerName);
            if (towerComparison != 0) {
                return towerComparison;
            }
            int containerComparison = compareStrings(first.containerPath, second.containerPath);
            if (containerComparison != 0) {
                return containerComparison;
            }
            int elementComparison = compareStrings(first.elementName, second.elementName);
            if (elementComparison != 0) {
                return elementComparison;
            }
            int fieldComparison = Integer.compare(first.fieldLabelRes, second.fieldLabelRes);
            if (fieldComparison != 0) {
                return fieldComparison;
            }
            fieldComparison = compareStrings(first.fieldLabel, second.fieldLabel);
            if (fieldComparison != 0) {
                return fieldComparison;
            }
            return compareStrings(first.fieldValue, second.fieldValue);
        };
    }

    private static int compareStrings(@Nullable String first, @Nullable String second) {
        String left = first != null ? first : "";
        String right = second != null ? second : "";
        return left.compareToIgnoreCase(right);
    }

    @NonNull
    private static SearchMatch buildMatch(@NonNull String roomName,
            @NonNull RoomContentItem target,
            @NonNull RoomContentItem itemForContext,
            @NonNull Map<Long, RoomContentItem> byRank,
            @StringRes int fieldLabelRes,
            @Nullable String fieldValue,
            boolean includeContainerSelf) {
        RoomContentItem furniture = findFurnitureContext(itemForContext, byRank);
        RoomContentItem locationSource = findLocationSource(itemForContext, furniture, byRank);
        Integer column = locationSource != null ? locationSource.getContainerColumn() : null;
        Integer level = locationSource != null ? locationSource.getContainerLevel() : null;
        String furnitureName = furniture != null ? furniture.getName() : "";
        boolean isStorageTower = furniture != null && furniture.isStorageTower();
        String storageTowerName = isStorageTower ? furniture.getName() : "";
        Integer furnitureColumn = isStorageTower ? null : column;
        Integer furnitureLevel = isStorageTower ? null : level;
        Integer storageTowerColumn = isStorageTower ? column : null;
        Integer storageTowerDrawer = isStorageTower ? level : null;
        String containerPath = buildContainerPath(target, byRank, includeContainerSelf);
        String elementName = (!target.isFurniture() && !target.isContainer())
                ? target.getName()
                : "";
        return new SearchMatch(roomName,
                furnitureName,
                furnitureColumn,
                furnitureLevel,
                storageTowerName,
                storageTowerColumn,
                storageTowerDrawer,
                containerPath,
                elementName,
                fieldLabelRes,
                fieldValue);
    }

    @Nullable
    private static RoomContentItem findFurnitureContext(@NonNull RoomContentItem item,
            @NonNull Map<Long, RoomContentItem> byRank) {
        RoomContentItem current = item;
        Set<Long> visited = new HashSet<>();
        while (current != null) {
            if (current.isFurniture()) {
                return current;
            }
            Long parentRank = current.getParentRank();
            if (parentRank == null || !visited.add(parentRank)) {
                break;
            }
            current = byRank.get(parentRank);
        }
        return null;
    }

    @Nullable
    private static RoomContentItem findLocationSource(@NonNull RoomContentItem item,
            @Nullable RoomContentItem furniture,
            @NonNull Map<Long, RoomContentItem> byRank) {
        RoomContentItem current = item;
        Set<Long> visited = new HashSet<>();
        while (current != null && current != furniture) {
            if ((current.getContainerColumn() != null && current.getContainerColumn() > 0)
                    || (current.getContainerLevel() != null
                    && current.getContainerLevel() >= RoomContentItem.FURNITURE_BOTTOM_LEVEL)) {
                return current;
            }
            Long parentRank = current.getParentRank();
            if (parentRank == null || !visited.add(parentRank)) {
                break;
            }
            current = byRank.get(parentRank);
        }
        return null;
    }

    @NonNull
    private static String buildContainerPath(@NonNull RoomContentItem item,
            @NonNull Map<Long, RoomContentItem> byRank,
            boolean includeSelf) {
        List<String> segments = new ArrayList<>();
        RoomContentItem current = includeSelf ? item : getParent(item, byRank);
        Set<Long> visited = new HashSet<>();
        while (current != null) {
            if (current.isContainer() && !current.isFurniture()) {
                segments.add(0, safeName(current.getName()));
            }
            Long parentRank = current.getParentRank();
            if (parentRank == null || !visited.add(parentRank)) {
                break;
            }
            current = byRank.get(parentRank);
        }
        return TextUtils.join("/", segments);
    }

    @Nullable
    private static RoomContentItem getParent(@NonNull RoomContentItem item,
            @NonNull Map<Long, RoomContentItem> byRank) {
        Long parentRank = item.getParentRank();
        if (parentRank == null) {
            return null;
        }
        return byRank.get(parentRank);
    }

    @NonNull
    private static List<RoomRecord> loadRooms(@NonNull EstablishmentSearchViewModel viewModel,
            @NonNull Establishment establishment) {
        String establishmentId = establishment.getId();
        if (establishmentId == null || establishmentId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<Room> storedRooms = viewModel.loadRoomsSnapshot(establishmentId);
        if (storedRooms.isEmpty()) {
            return Collections.emptyList();
        }
        List<RoomRecord> rooms = new ArrayList<>();
        for (Room room : storedRooms) {
            if (room == null) {
                continue;
            }
            String name = room.getName();
            if (name == null) {
                continue;
            }
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            rooms.add(new RoomRecord(trimmed));
        }
        return rooms;
    }

    @NonNull
    private static List<RoomContentItem> loadRoomContent(
            @NonNull EstablishmentSearchViewModel viewModel,
            @NonNull Establishment establishment,
            @Nullable String roomName) {
        String establishmentName = establishment.getName();
        List<RoomContentItem> items = viewModel.loadRoomContentSnapshot(establishmentName, roomName);
        return items.isEmpty() ? Collections.emptyList() : items;
    }

    private static void populateResults(@NonNull Activity activity,
            @NonNull TableLayout table,
            @NonNull TextView statusView,
            @NonNull List<SearchMatch> matches) {
        table.removeAllViews();
        if (matches.isEmpty()) {
            statusView.setText(R.string.search_dialog_status_no_results);
            statusView.setVisibility(View.VISIBLE);
            return;
        }
        if (matches.size() == 1 && matches.get(0).isNoDataMarker()) {
            statusView.setText(R.string.search_dialog_status_no_data);
            statusView.setVisibility(View.VISIBLE);
            return;
        }
        statusView.setVisibility(View.GONE);
        table.addView(createHeaderRow(activity));
        for (SearchMatch match : matches) {
            table.addView(createDataRow(activity, match));
        }
    }

    @NonNull
    private static TableRow createHeaderRow(@NonNull Context context) {
        TableRow row = new TableRow(context);
        row.addView(createHeaderCell(context, R.string.search_dialog_header_room));
        row.addView(createHeaderCell(context, R.string.search_dialog_header_furniture));
        row.addView(createHeaderCell(context, R.string.search_dialog_header_furniture_column));
        row.addView(createHeaderCell(context, R.string.search_dialog_header_furniture_level));
        row.addView(createHeaderCell(context, R.string.search_dialog_header_storage_tower));
        row.addView(createHeaderCell(context, R.string.search_dialog_header_storage_column));
        row.addView(createHeaderCell(context, R.string.search_dialog_header_storage_drawer));
        row.addView(createHeaderCell(context, R.string.search_dialog_header_container));
        row.addView(createHeaderCell(context, R.string.search_dialog_header_element));
        row.addView(createHeaderCell(context, R.string.search_dialog_header_field));
        return row;
    }

    @NonNull
    private static TableRow createDataRow(@NonNull Context context, @NonNull SearchMatch match) {
        TableRow row = new TableRow(context);
        row.addView(createDataCell(context, match.roomName));
        row.addView(createDataCell(context, match.furnitureName));
        row.addView(createDataCell(context, formatInteger(match.furnitureColumn)));
        row.addView(createDataCell(context, formatInteger(match.furnitureLevel)));
        row.addView(createDataCell(context, match.storageTowerName));
        row.addView(createDataCell(context, formatInteger(match.storageTowerColumn)));
        row.addView(createDataCell(context, formatInteger(match.storageTowerDrawer)));
        row.addView(createDataCell(context, match.containerPath));
        row.addView(createDataCell(context, match.elementName));
        String fieldLabel = match.fieldLabelRes != 0
                ? context.getString(match.fieldLabelRes)
                : match.fieldLabel;
        String fieldText = TextUtils.isEmpty(match.fieldValue)
                ? fieldLabel
                : context.getString(R.string.search_dialog_field_with_value,
                        fieldLabel, match.fieldValue);
        row.addView(createDataCell(context, fieldText));
        return row;
    }

    @NonNull
    private static TextView createHeaderCell(@NonNull Context context, @StringRes int textRes) {
        TextView view = new TextView(context);
        view.setText(textRes);
        view.setPadding(dpToPx(context, 12), dpToPx(context, 8), dpToPx(context, 12), dpToPx(context, 8));
        view.setTextAppearance(context,
                com.google.android.material.R.style.TextAppearance_MaterialComponents_Subtitle2);
        return view;
    }

    @NonNull
    private static TextView createDataCell(@NonNull Context context, @Nullable String value) {
        TextView view = new TextView(context);
        view.setText(formatValue(value));
        view.setPadding(dpToPx(context, 12), dpToPx(context, 8), dpToPx(context, 12), dpToPx(context, 8));
        view.setTextAppearance(context,
                com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2);
        return view;
    }

    @NonNull
    private static String formatValue(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    @Nullable
    private static String formatInteger(@Nullable Integer value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

    private static int dpToPx(@NonNull Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @NonNull
    private static String safeName(@Nullable String value) {
        return value != null ? value.trim() : "";
    }

    @NonNull
    private static String normalize(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        return withoutDiacritics.toLowerCase(Locale.ROOT);
    }

    private static final class RoomRecord {
        final String name;

        RoomRecord(@NonNull String name) {
            this.name = name;
        }
    }

    private static final class SearchMatch {
        final String roomName;
        final String furnitureName;
        final Integer furnitureColumn;
        final Integer furnitureLevel;
        final String storageTowerName;
        final Integer storageTowerColumn;
        final Integer storageTowerDrawer;
        final String containerPath;
        final String elementName;
        final int fieldLabelRes;
        final String fieldLabel;
        final String fieldValue;

        private SearchMatch(String roomName,
                String furnitureName,
                Integer furnitureColumn,
                Integer furnitureLevel,
                String storageTowerName,
                Integer storageTowerColumn,
                Integer storageTowerDrawer,
                String containerPath,
                String elementName,
                int fieldLabelRes,
                String fieldValue) {
            this.roomName = formatValue(roomName);
            this.furnitureName = formatValue(furnitureName);
            this.furnitureColumn = furnitureColumn;
            this.furnitureLevel = furnitureLevel;
            this.storageTowerName = formatValue(storageTowerName);
            this.storageTowerColumn = storageTowerColumn;
            this.storageTowerDrawer = storageTowerDrawer;
            this.containerPath = formatValue(containerPath);
            this.elementName = formatValue(elementName);
            this.fieldLabelRes = fieldLabelRes;
            this.fieldLabel = null;
            this.fieldValue = formatValue(fieldValue);
        }

        private SearchMatch(String fieldLabel) {
            this.roomName = "";
            this.furnitureName = "";
            this.furnitureColumn = null;
            this.furnitureLevel = null;
            this.storageTowerName = "";
            this.storageTowerColumn = null;
            this.storageTowerDrawer = null;
            this.containerPath = "";
            this.elementName = "";
            this.fieldLabelRes = 0;
            this.fieldLabel = fieldLabel;
            this.fieldValue = "";
        }

        static SearchMatch noData() {
            return new SearchMatch("__NO_DATA__");
        }

        boolean isNoDataMarker() {
            return "__NO_DATA__".equals(fieldLabel);
        }
    }
}
