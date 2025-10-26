package com.example.new1;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.PopupWindowCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoomContentAdapter extends RecyclerView.Adapter<RoomContentAdapter.ViewHolder> {

    private static final float LABEL_RELATIVE_SIZE = 0.85f;

    private static final int VISIBILITY_FLAG_CONTAINERS = 1;
    private static final int VISIBILITY_FLAG_ITEMS = 1 << 1;
    private static final int VISIBILITY_DEFAULT_MASK = VISIBILITY_FLAG_CONTAINERS
            | VISIBILITY_FLAG_ITEMS;

    private final List<RoomContentItem> items;
    private final LayoutInflater inflater;

    private final Context context;
    private final int labelColor;
    @Nullable
    private final OnRoomContentInteractionListener interactionListener;
    private final SparseBooleanArray expandedStates = new SparseBooleanArray();
    private final SparseIntArray containerVisibilityStates = new SparseIntArray();
    private final int hierarchyIndentPx;
    private final float cardCornerRadiusPx;
    private final float cardElevationLevel0Px;
    private final float cardElevationLevel1Px;
    private final float cardElevationLevel2Px;
    private final HierarchyStyle[] hierarchyStyles;
    private final Map<String, Integer> containerBannerColorCache = new HashMap<>();
    private final String containerTypeBoxLabel;
    private final String containerTypeBagLabel;
    private final int containerBannerDefaultColor;
    private final int containerBannerBoxColor;
    private final int containerBannerBagColor;
    private final int[] containerBannerPalette;
    private final int[] itemBannerColors;
    @Nullable
    private int[] hierarchyParentPositions;
    @Nullable
    private int[] hierarchyDepths;
    private boolean hierarchyDirty = true;
    @Nullable
    private RecyclerView attachedRecyclerView;
    @Nullable
    private final GroupDecoration groupDecoration;
    private boolean groupDecorationAttached;
    private int activeContainerPopupAdapterPosition = RecyclerView.NO_POSITION;
    private int activeContainerPopupVisibilityMask = VISIBILITY_DEFAULT_MASK;
    @Nullable
    private ContainerPopupRestoreState pendingContainerPopupRestore;
    private final RecyclerView.AdapterDataObserver hierarchyInvalidatingObserver =
            new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    invalidateHierarchyCache();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    invalidateHierarchyCache();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount,
                        @Nullable Object payload) {
                    invalidateHierarchyCache();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    invalidateHierarchyCache();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    invalidateHierarchyCache();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    invalidateHierarchyCache();
                }
            };

    public RoomContentAdapter(@NonNull Context context,
            @NonNull List<RoomContentItem> items) {
        this(context, items, null);
    }

    public RoomContentAdapter(@NonNull Context context,
            @NonNull List<RoomContentItem> items,
            @Nullable OnRoomContentInteractionListener interactionListener) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.interactionListener = interactionListener;
        this.labelColor = ContextCompat.getColor(context, R.color.icon_brown);
        this.hierarchyIndentPx = context.getResources()
                .getDimensionPixelSize(R.dimen.room_content_hierarchy_indent);
        this.cardCornerRadiusPx = context.getResources()
                .getDimension(R.dimen.room_content_card_corner_radius);
        this.cardElevationLevel0Px = context.getResources()
                .getDimension(R.dimen.room_content_card_elevation_level_0);
        this.cardElevationLevel1Px = context.getResources()
                .getDimension(R.dimen.room_content_card_elevation_level_1);
        this.cardElevationLevel2Px = context.getResources()
                .getDimension(R.dimen.room_content_card_elevation_level_2);
        this.hierarchyStyles = createHierarchyStyles(context);
        this.containerTypeBoxLabel = context.getString(R.string.dialog_container_type_box);
        this.containerTypeBagLabel = context.getString(R.string.dialog_container_type_bag);
        this.containerBannerDefaultColor = ContextCompat.getColor(context,
                R.color.room_container_banner_default);
        this.containerBannerBoxColor = ContextCompat.getColor(context,
                R.color.room_container_banner_box);
        this.containerBannerBagColor = ContextCompat.getColor(context,
                R.color.room_container_banner_bag);
        this.containerBannerPalette = new int[] {
                ContextCompat.getColor(context, R.color.room_container_banner_palette_0),
                ContextCompat.getColor(context, R.color.room_container_banner_palette_1),
                ContextCompat.getColor(context, R.color.room_container_banner_palette_2),
                ContextCompat.getColor(context, R.color.room_container_banner_palette_3),
                ContextCompat.getColor(context, R.color.room_container_banner_palette_4),
                ContextCompat.getColor(context, R.color.room_container_banner_palette_5)
        };
        this.itemBannerColors = new int[] {
                ContextCompat.getColor(context, R.color.room_content_banner_default),
                ContextCompat.getColor(context, R.color.room_content_banner_other),
                ContextCompat.getColor(context, R.color.room_content_banner_book),
                ContextCompat.getColor(context, R.color.room_content_banner_cd),
                ContextCompat.getColor(context, R.color.room_content_banner_disc),
                ContextCompat.getColor(context, R.color.room_content_banner_comic)
        };
        registerAdapterDataObserver(hierarchyInvalidatingObserver);
        groupDecoration = new GroupDecoration(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_room_content, parent, false);
        return new ViewHolder(view, interactionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomContentItem item = items.get(position);
        holder.bind(item, position);

        boolean isFurniture = item.isFurniture();
        CharSequence commentText = isFurniture ? null : formatComment(item.getComment());
        boolean hasComment = commentText != null;
        
        List<CharSequence> metadataLines = new ArrayList<>();
        if (!isFurniture) {
            addMetadataLine(metadataLines, R.string.room_content_metadata_category, item.getCategory());
            addMetadataLine(metadataLines, R.string.room_content_metadata_series, item.getSeries());
            addMetadataLine(metadataLines, R.string.room_content_metadata_number, item.getNumber());
            addMetadataLine(metadataLines, R.string.room_content_metadata_author, item.getAuthor());
            addMetadataLine(metadataLines, R.string.room_content_metadata_publisher, item.getPublisher());
            addMetadataLine(metadataLines, R.string.room_content_metadata_edition, item.getEdition());
            addMetadataLine(metadataLines, R.string.room_content_metadata_publication_date,
                    item.getPublicationDate());
            addMetadataLine(metadataLines, R.string.room_content_metadata_summary, item.getSummary());
            List<String> tracks = item.getTracks();
            if (!tracks.isEmpty()) {
                addMetadataLine(metadataLines, R.string.room_content_metadata_tracks,
                        TextUtils.join("\n", tracks));
            }
            addMetadataLine(metadataLines, R.string.room_content_metadata_barcode, item.getBarcode());
        }
        CharSequence metadataText = formatMetadataLines(metadataLines);
        boolean hasMetadata = metadataText != null;

        boolean isContainer = item.isContainer();
        boolean canToggleContainer = !isFurniture && isContainer && item.hasAttachedItems();
        if (canToggleContainer) {
            expandedStates.delete(position);
        }
        if (!hasComment && !hasMetadata) {
            expandedStates.delete(position);
        }
        boolean isContainerExpanded = isContainerExpanded(position);
        boolean shouldDisplayDetails;
        if (canToggleContainer) {
            shouldDisplayDetails = isContainerExpanded && (hasComment || hasMetadata);
        } else {
            boolean isExpanded = (hasComment || hasMetadata)
                    && expandedStates.get(position, false);
            shouldDisplayDetails = isExpanded;
        }
        holder.detailsContainer.setVisibility(shouldDisplayDetails ? View.VISIBLE : View.GONE);
        CharSequence displayedName = holder.nameView.getText();
        String toggleLabel = displayedName != null ? displayedName.toString() : resolveItemName(item);
        boolean hasToggle = hasComment || hasMetadata || canToggleContainer;
        boolean isToggleExpanded = canToggleContainer
                ? isContainerExpanded
                : ((hasComment || hasMetadata) && expandedStates.get(position, false));
        holder.updateToggle(hasToggle, isToggleExpanded, toggleLabel);

        if (shouldDisplayDetails) {
            if (hasComment) {
                holder.commentView.setVisibility(View.VISIBLE);
                holder.commentView.setText(commentText);
            } else {
                holder.commentView.setVisibility(View.GONE);
                holder.commentView.setText(null);
            }

            CharSequence combinedMetadata = metadataText;

            if (combinedMetadata != null) {
                holder.metadataView.setVisibility(View.VISIBLE);
                holder.metadataView.setText(combinedMetadata);
            } else {
                holder.metadataView.setVisibility(View.GONE);
                holder.metadataView.setText(null);
            }
        } else {
            holder.commentView.setVisibility(View.GONE);
            holder.commentView.setText(null);
            holder.metadataView.setVisibility(View.GONE);
            holder.metadataView.setText(null);
        }

        holder.releaseChildrenAdapter();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        attachedRecyclerView = recyclerView;
        if (groupDecoration != null && !groupDecorationAttached) {
            recyclerView.addItemDecoration(groupDecoration);
            groupDecorationAttached = true;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (attachedRecyclerView == recyclerView) {
            attachedRecyclerView = null;
        }
        if (groupDecoration != null && groupDecorationAttached) {
            recyclerView.removeItemDecoration(groupDecoration);
            groupDecorationAttached = false;
        }
    }

    @Nullable
    RoomContentItem getItemAt(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    private void invalidateDecorations() {
        if (attachedRecyclerView != null) {
            attachedRecyclerView.invalidateItemDecorations();
        }
    }

    private void invalidateHierarchyCache() {
        hierarchyDirty = true;
        invalidateDecorations();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.dismissFurniturePopup();
        holder.dismissOptionsMenu();
        holder.releaseChildrenAdapter();
    }

    private void ensureHierarchyComputed() {
        if (!hierarchyDirty && hierarchyParentPositions != null
                && hierarchyParentPositions.length == items.size()) {
            return;
        }
        rebuildHierarchyCache();
    }

    private void rebuildHierarchyCache() {
        int size = items.size();
        hierarchyParentPositions = new int[size];
        hierarchyDepths = new int[size];
        if (size == 0) {
            hierarchyDirty = false;
            return;
        }
        Arrays.fill(hierarchyParentPositions, -1);
        Map<Long, Integer> positionByRank = new HashMap<>();
        for (int i = 0; i < size; i++) {
            positionByRank.put(items.get(i).getRank(), i);
        }
        for (int i = 0; i < size; i++) {
            Long parentRank = items.get(i).getParentRank();
            if (parentRank == null) {
                continue;
            }
            Integer parentPosition = positionByRank.get(parentRank);
            if (parentPosition != null && parentPosition >= 0 && parentPosition != i) {
                hierarchyParentPositions[i] = parentPosition;
            }
        }
        Deque<ContainerState> stack = new ArrayDeque<>();
        for (int i = 0; i < size; i++) {
            RoomContentItem current = items.get(i);
            boolean hasDirectChildren = current.isContainer()
                    && current.getAttachedItemCount() > 0;
            while (!stack.isEmpty() && stack.peek().remainingDirectChildren <= 0) {
                stack.pop();
            }
            if (hierarchyParentPositions[i] < 0) {
                int parentPosition = stack.isEmpty() ? -1 : stack.peek().position;
                hierarchyParentPositions[i] = parentPosition;
            }
            int parentPosition = hierarchyParentPositions[i];
            if (parentPosition >= 0) {
                for (ContainerState state : stack) {
                    if (state.position == parentPosition) {
                        state.consumeChild();
                        break;
                    }
                }
            }
            if (hasDirectChildren) {
                int directChildren = current.getAttachedItemCount();
                stack.push(new ContainerState(i, directChildren));
            }
        }
        Arrays.fill(hierarchyDepths, Integer.MIN_VALUE);
        boolean[] visiting = new boolean[size];
        for (int i = 0; i < size; i++) {
            hierarchyDepths[i] = computeDepthForPosition(i, hierarchyParentPositions,
                    hierarchyDepths, visiting);
        }
        hierarchyDirty = false;
    }

    private int computeDepthForPosition(int position, @NonNull int[] parents,
            @NonNull int[] cache, @NonNull boolean[] visiting) {
        if (position < 0 || position >= parents.length) {
            return 0;
        }
        if (cache[position] != Integer.MIN_VALUE) {
            return cache[position];
        }
        if (visiting[position]) {
            cache[position] = 0;
            return 0;
        }
        int parent = parents[position];
        if (parent < 0 || parent == position) {
            cache[position] = 0;
            return 0;
        }
        visiting[position] = true;
        int depth = computeDepthForPosition(parent, parents, cache, visiting) + 1;
        visiting[position] = false;
        cache[position] = Math.max(0, depth);
        return cache[position];
    }

    private static final class ContainerState {
        final int position;
        int remainingDirectChildren;

        ContainerState(int position, int remainingDirectChildren) {
            this.position = position;
            this.remainingDirectChildren = Math.max(0, remainingDirectChildren);
        }

        void consumeChild() {
            if (remainingDirectChildren > 0) {
                remainingDirectChildren--;
            }
        }
    }

    private void addMetadataLine(@NonNull List<CharSequence> metadataLines, int templateRes,
            @Nullable String value) {
        if (value == null) {
            return;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        metadataLines.add(createMetadataLine(templateRes, trimmed));
    }

    @Nullable
    private CharSequence formatMetadataLines(@NonNull List<CharSequence> metadataLines) {
        if (metadataLines.isEmpty()) {
            return null;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < metadataLines.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(metadataLines.get(i));
        }
        return builder;
    }

    @NonNull
    private CharSequence createMetadataLine(int templateRes, @NonNull String value) {
        String formatted = context.getString(templateRes, value);
        int colonIndex = formatted.indexOf(':');
        String labelPart;
        String valuePart;
        if (colonIndex >= 0) {
            labelPart = formatted.substring(0, colonIndex).trim();
            valuePart = formatted.substring(colonIndex + 1).trim();
        } else {
            labelPart = formatted.trim();
            valuePart = "";
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append('\u2022').append(' ');
        int labelStart = builder.length();
        builder.append(labelPart);
        builder.append(" : ");
        int labelEnd = builder.length();
        applyLabelStyle(builder, labelStart, labelEnd);
        if (!valuePart.isEmpty()) {
            appendValueWithLineBreaks(builder, valuePart);
        }
        return builder;
    }

    private void appendValueWithLineBreaks(@NonNull SpannableStringBuilder builder,
            @NonNull String valuePart) {
        String[] lines = valuePart.split("\r?\n", -1);
        if (lines.length == 0) {
            return;
        }
        builder.append(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            builder.append('\n');
            builder.append("    ");
            builder.append(lines[i]);
        }
    }

    private CharSequence formatComment(@Nullable String comment) {
        if (comment == null) {
            return null;
        }
        String trimmed = comment.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String label = context.getString(R.string.dialog_label_room_content_comment);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(label);
        builder.append(" : ");
        int labelEnd = builder.length();
        applyLabelStyle(builder, 0, labelEnd);
        builder.append(trimmed);
        return builder;
    }

    @NonNull
    private String resolveItemName(@NonNull RoomContentItem item) {
        String name = item.getName();
        String trimmedName = name != null ? name.trim() : "";
        if (trimmedName.isEmpty()) {
            return context.getString(R.string.dialog_room_content_item_placeholder);
        }
        return trimmedName;
    }

    @NonNull
    private String appendAttachmentCount(@NonNull String displayName,
            @NonNull RoomContentItem item) {
        // Le nombre d’éléments rattachés n’est plus affiché à cet endroit.
        return displayName;
    }

    @Nullable
    private RoomContentItem findAttachedContainer(int position) {
        int containerPosition = findAttachedContainerPosition(position);
        if (containerPosition < 0) {
            return null;
        }
        return items.get(containerPosition);
    }

    private int findAttachedContainerPosition(int position) {
        ensureHierarchyComputed();
        if (position < 0 || position >= items.size()) {
            return -1;
        }
        if (hierarchyParentPositions == null || position >= hierarchyParentPositions.length) {
            return -1;
        }
        return hierarchyParentPositions[position];
    }

    private boolean isItemHiddenByCollapsedContainer(int position) {
        if (position < 0 || position >= items.size()) {
            return false;
        }
        RoomContentItem item = items.get(position);
        int containerPosition = findAttachedContainerPosition(position);
        boolean isDirectParent = true;
        while (containerPosition >= 0) {
            int visibilityMask = getContainerVisibilityMask(containerPosition);
            if (visibilityMask == 0) {
                return true;
            }
            if (item.isContainer()) {
                if ((visibilityMask & VISIBILITY_FLAG_CONTAINERS) == 0) {
                    return true;
                }
            } else {
                // Laisser visibles les éléments directement attachés même si les contenants
                // sont masqués.
                if ((visibilityMask & VISIBILITY_FLAG_CONTAINERS) == 0 && !isDirectParent) {
                    return true;
                }
                if (isDirectParent && (visibilityMask & VISIBILITY_FLAG_ITEMS) == 0) {
                    return true;
                }
            }
            isDirectParent = false;
            containerPosition = findAttachedContainerPosition(containerPosition);
        }
        return false;
    }

    private boolean isDescendantOf(int ancestorPosition, int position) {
        if (ancestorPosition < 0 || position <= ancestorPosition || position >= items.size()) {
            return false;
        }
        int current = findAttachedContainerPosition(position);
        while (current >= 0 && current < position) {
            if (current == ancestorPosition) {
                return true;
            }
            current = findAttachedContainerPosition(current);
        }
        return false;
    }

    /**
     * Vérifie si le conteneur possède au moins un enfant direct actuellement affiché.
     */
    private boolean hasVisibleDirectChildren(int containerPosition) {
        ensureHierarchyComputed();
        if (containerPosition < 0 || containerPosition >= items.size()) {
            return false;
        }
        for (int index = containerPosition + 1; index < items.size(); index++) {
            if (!isDescendantOf(containerPosition, index)) {
                break;
            }
            if (findAttachedContainerPosition(index) == containerPosition) {
                RoomContentItem child = items.get(index);
                if (child != null && child.isDisplayed()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLastDirectChild(int parentPosition, int position) {
        if (parentPosition < 0 || position <= parentPosition || position >= items.size()) {
            return true;
        }
        for (int index = position + 1; index < items.size(); index++) {
            if (!isDescendantOf(parentPosition, index)) {
                break;
            }
            if (findAttachedContainerPosition(index) == parentPosition) {
                return false;
            }
        }
        return true;
    }

    private int computeHierarchyDepth(int position) {
        ensureHierarchyComputed();
        if (position < 0 || position >= items.size()) {
            return 0;
        }
        if (hierarchyDepths == null || position >= hierarchyDepths.length) {
            return 0;
        }
        return Math.max(0, hierarchyDepths[position]);
    }

    private boolean isContainerExpanded(int position) {
        return getContainerVisibilityMask(position) != 0;
    }

    private int getContainerVisibilityMask(int position) {
        if (position < 0) {
            return VISIBILITY_DEFAULT_MASK;
        }
        SparseIntArray visibilityStates = containerVisibilityStates;
        if (visibilityStates == null) {
            return VISIBILITY_DEFAULT_MASK;
        }
        return visibilityStates.get(position, VISIBILITY_DEFAULT_MASK);
    }

    private int resolvePopupVisibilityMask(int position) {
        int visibilityMask = getContainerVisibilityMask(position);
        if (visibilityMask != 0) {
            return visibilityMask;
        }
        if (position < 0 || position >= items.size()) {
            return visibilityMask;
        }
        RoomContentItem item = items.get(position);
        if (item != null && item.isContainer() && item.hasAttachedItems()) {
            return VISIBILITY_DEFAULT_MASK;
        }
        return visibilityMask;
    }

    private void setContainerExpanded(int position, boolean expanded) {
        int visibilityMask = expanded ? VISIBILITY_DEFAULT_MASK : 0;
        setContainerVisibilityMask(position, visibilityMask);
    }

    private void setContainerVisibilityMask(int position, int visibilityMask) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        SparseIntArray visibilityStates = containerVisibilityStates;
        if (visibilityStates == null) {
            return;
        }
        int normalizedMask = visibilityMask & (VISIBILITY_FLAG_CONTAINERS | VISIBILITY_FLAG_ITEMS);
        if (normalizedMask == VISIBILITY_DEFAULT_MASK) {
            visibilityStates.delete(position);
        } else {
            visibilityStates.put(position, normalizedMask);
        }
        updateAttachedItemsDisplayState(position, normalizedMask);
        invalidateDecorations();
    }

    private void updateAttachedItemsDisplayState(int containerPosition, int visibilityMask) {
        if (containerPosition < 0 || containerPosition >= items.size()) {
            return;
        }
        RoomContentItem container = items.get(containerPosition);
        if (!container.isContainer()) {
            return;
        }
        container.setDisplayed(true);
        int attachedItemCount = Math.max(0, container.getAttachedItemCount());
        if (attachedItemCount <= 0) {
            return;
        }
        int start = containerPosition + 1;
        for (int index = start; index < items.size(); index++) {
            if (!isDescendantOf(containerPosition, index)) {
                break;
            }
            RoomContentItem attachedItem = items.get(index);
            int parentPosition = findAttachedContainerPosition(index);
            if (parentPosition == containerPosition) {
                if (attachedItem.isContainer()) {
                    boolean showContainers = (visibilityMask & VISIBILITY_FLAG_CONTAINERS) != 0;
                    attachedItem.setDisplayed(showContainers);
                } else {
                    boolean showItems = (visibilityMask & VISIBILITY_FLAG_ITEMS) != 0;
                    attachedItem.setDisplayed(showItems);
                }
            }
        }
    }

    private void notifyAttachedItemsChanged(int containerPosition) {
        int start = containerPosition + 1;
        if (start >= items.size()) {
            return;
        }
        int end = start;
        while (end < items.size() && isDescendantOf(containerPosition, end)) {
            end++;
        }
        if (start >= end) {
            return;
        }
        notifyItemRangeChanged(start, end - start);
        invalidateDecorations();
    }

    public void collapseAllContainers() {
        containerVisibilityStates.clear();
        for (int index = 0; index < items.size(); index++) {
            RoomContentItem item = items.get(index);
            if (!item.isContainer()) {
                continue;
            }
            setContainerVisibilityMask(index, 0);
        }
    }

    private void applyLabelStyle(@NonNull Spannable spannable, int start, int end) {
        if (start >= end) {
            return;
        }
        spannable.setSpan(new ForegroundColorSpan(labelColor), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new RelativeSizeSpan(LABEL_RELATIVE_SIZE), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void applyBannerColor(@NonNull View bannerView, @Nullable String type) {
        @ColorRes int colorRes = resolveBannerColor(type);
        int color = ContextCompat.getColor(context, colorRes);
        Drawable background = bannerView.getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable drawable = (GradientDrawable) background.mutate();
            drawable.setColor(color);
        } else if (background instanceof ColorDrawable) {
            ColorDrawable drawable = (ColorDrawable) background.mutate();
            drawable.setColor(color);
        } else {
            bannerView.setBackgroundColor(color);
        }
    }

    private void applyContainerBannerColor(@NonNull View bannerView,
            @NonNull HierarchyStyle style, @Nullable String type) {
        int color = resolveContainerBannerColor(type, style.bannerColor);
        Drawable background = bannerView.getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable drawable = (GradientDrawable) background.mutate();
            drawable.setColor(color);
        } else if (background instanceof ColorDrawable) {
            ColorDrawable drawable = (ColorDrawable) background.mutate();
            drawable.setColor(color);
        } else {
            bannerView.setBackgroundColor(color);
        }
    }

    private void applyContainerBackground(@NonNull View target, @NonNull HierarchyStyle style,
            boolean hasAttachedItems, boolean isExpanded, boolean joinsParentFrame,
            boolean isLastChildInParentGroup) {
        float topRadius = joinsParentFrame ? 0f : cardCornerRadiusPx;
        float bottomRadius;
        if (hasAttachedItems && isExpanded) {
            bottomRadius = 0f;
        } else if (joinsParentFrame) {
            bottomRadius = isLastChildInParentGroup ? cardCornerRadiusPx : 0f;
        } else {
            bottomRadius = cardCornerRadiusPx;
        }
        boolean hideTopStroke = false;
        boolean hideBottomStroke = false;
        int internalStrokeWidth = 0;
        Drawable drawable = createFramedBackground(style.backgroundColor, topRadius,
                topRadius, bottomRadius, bottomRadius, internalStrokeWidth,
                style.accentColor, hideTopStroke, hideBottomStroke);
        target.setBackground(drawable);
    }

    private void applyStandaloneContentBackground(@NonNull View target,
            @NonNull HierarchyStyle style) {
        Drawable drawable = createFramedBackground(style.backgroundColor, cardCornerRadiusPx,
                cardCornerRadiusPx, cardCornerRadiusPx, cardCornerRadiusPx, 0,
                style.accentColor, false, false);
        target.setBackground(drawable);
    }

    private void applyAttachmentBackground(@NonNull View target, @NonNull HierarchyStyle style,
            boolean isLastAttachment) {
        float bottomRadius = isLastAttachment ? cardCornerRadiusPx : 0f;
        Drawable drawable = createFramedBackground(style.backgroundColor, 0f, 0f, bottomRadius,
                bottomRadius, 0, style.accentColor, false, false);
        target.setBackground(drawable);
    }

    private void applyFilledIndicatorStyle(@NonNull View indicatorView) {
        Drawable background = indicatorView.getBackground();
        int color = ContextCompat.getColor(context, R.color.room_container_border_dark);
        if (background instanceof GradientDrawable) {
            GradientDrawable drawable = (GradientDrawable) background.mutate();
            drawable.setColor(color);
        } else if (background instanceof ColorDrawable) {
            ColorDrawable drawable = (ColorDrawable) background.mutate();
            drawable.setColor(color);
        } else {
            indicatorView.setBackgroundColor(color);
        }
    }

    @NonNull
    private Drawable createFramedBackground(@ColorInt int backgroundColor, float topLeft,
            float topRight, float bottomRight, float bottomLeft, int strokeWidthPx,
            @ColorInt int strokeColor, boolean hideTopStroke, boolean hideBottomStroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(backgroundColor);
        drawable.setCornerRadii(new float[] {
                topLeft, topLeft,
                topRight, topRight,
                bottomRight, bottomRight,
                bottomLeft, bottomLeft
        });
        if (strokeWidthPx <= 0) {
            return drawable;
        }
        drawable.setStroke(strokeWidthPx, strokeColor);
        if (!hideTopStroke && !hideBottomStroke) {
            return drawable;
        }
        return new MaskedStrokeDrawable(drawable, backgroundColor, strokeWidthPx,
                hideTopStroke, hideBottomStroke);
    }

    // Masque les portions du trait afin de fusionner visuellement les cadres adjacents.
    private static class MaskedStrokeDrawable extends Drawable {

        private final GradientDrawable baseDrawable;
        private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int strokeWidth;
        private final boolean hideTopStroke;
        private final boolean hideBottomStroke;
        private final int backgroundColor;
        private int alpha = 255;

        MaskedStrokeDrawable(@NonNull GradientDrawable baseDrawable,
                @ColorInt int backgroundColor, int strokeWidth, boolean hideTopStroke,
                boolean hideBottomStroke) {
            this.baseDrawable = (GradientDrawable) baseDrawable.mutate();
            this.strokeWidth = strokeWidth;
            this.hideTopStroke = hideTopStroke;
            this.hideBottomStroke = hideBottomStroke;
            this.backgroundColor = backgroundColor;
            maskPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect bounds = getBounds();
            baseDrawable.setBounds(bounds);
            baseDrawable.setAlpha(alpha);
            baseDrawable.draw(canvas);
            int maskedColor = multiplyAlpha(backgroundColor, alpha);
            maskPaint.setColor(maskedColor);
            if (hideTopStroke) {
                canvas.drawRect(bounds.left, bounds.top, bounds.right,
                        bounds.top + strokeWidth, maskPaint);
            }
            if (hideBottomStroke) {
                canvas.drawRect(bounds.left, bounds.bottom - strokeWidth, bounds.right,
                        bounds.bottom, maskPaint);
            }
        }

        @Override
        protected void onBoundsChange(@NonNull Rect bounds) {
            super.onBoundsChange(bounds);
            baseDrawable.setBounds(bounds);
        }

        @Override
        public void setAlpha(int alpha) {
            this.alpha = alpha;
            baseDrawable.setAlpha(alpha);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            baseDrawable.setColorFilter(colorFilter);
            maskPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        private static int multiplyAlpha(@ColorInt int color, int alpha) {
            int originalAlpha = Color.alpha(color);
            int combinedAlpha = originalAlpha * alpha / 255;
            return (color & 0x00FFFFFF) | (combinedAlpha << 24);
        }
    }

    @NonNull
    private HierarchyStyle resolveHierarchyStyle(int depth) {
        if (hierarchyStyles.length == 0) {
            int fallbackBackground = ContextCompat.getColor(context,
                    R.color.room_content_card_container_background);
            int fallbackAccent = ContextCompat.getColor(context,
                    R.color.room_content_card_container_border);
            int fallbackBanner = ContextCompat.getColor(context,
                    R.color.room_content_banner_container);
            return new HierarchyStyle(fallbackBackground, fallbackAccent, fallbackBanner);
        }
        int index = Math.max(0, Math.min(depth, hierarchyStyles.length - 1));
        return hierarchyStyles[index];
    }

    private float resolveCardElevation(int depth) {
        if (depth <= 0) {
            return cardElevationLevel0Px;
        }
        if (depth == 1) {
            return cardElevationLevel1Px;
        }
        return cardElevationLevel2Px;
    }

    @NonNull
    private HierarchyStyle[] createHierarchyStyles(@NonNull Context context) {
        return new HierarchyStyle[] {
                new HierarchyStyle(
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_0_background),
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_0_border),
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_0_banner)),
                new HierarchyStyle(
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_1_background),
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_1_border),
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_1_banner)),
                new HierarchyStyle(
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_2_background),
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_2_border),
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_2_banner)),
                new HierarchyStyle(
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_3_background),
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_3_border),
                        ContextCompat.getColor(context,
                                R.color.room_content_hierarchy_level_3_banner))
        };
    }

    @ColorRes
    private int resolveBannerColor(@Nullable String type) {
        if (type == null) {
            return R.color.room_content_banner_default;
        }
        String trimmedType = type.trim();
        if (trimmedType.isEmpty()) {
            return R.color.room_content_banner_default;
        }
        if (trimmedType.equalsIgnoreCase(context.getString(R.string.dialog_type_book))
                || trimmedType.equalsIgnoreCase(context.getString(R.string.dialog_type_magazine))) {
            return R.color.room_content_banner_book;
        }
        if (trimmedType.equalsIgnoreCase(context.getString(R.string.dialog_type_cd))) {
            return R.color.room_content_banner_cd;
        }
        if (trimmedType.equalsIgnoreCase(context.getString(R.string.dialog_type_disc))) {
            return R.color.room_content_banner_disc;
        }
        if (trimmedType.equalsIgnoreCase(context.getString(R.string.dialog_type_comic))) {
            return R.color.room_content_banner_comic;
        }
        if (trimmedType.equalsIgnoreCase(context.getString(R.string.dialog_type_other))) {
            return R.color.room_content_banner_other;
        }
        if (trimmedType.equalsIgnoreCase(context.getString(R.string.dialog_type_key))) {
            return R.color.room_content_banner_other;
        }
        return R.color.room_content_banner_default;
    }

    @ColorInt
    private int resolveContainerBannerColor(@Nullable String type, @ColorInt int fallbackColor) {
        if (type == null) {
            return fallbackColor;
        }
        String trimmedType = type.trim();
        if (trimmedType.isEmpty()) {
            return fallbackColor;
        }
        String normalizedKey = trimmedType.toLowerCase(Locale.ROOT);
        Integer cachedColor = containerBannerColorCache.get(normalizedKey);
        if (cachedColor != null) {
            return cachedColor;
        }
        int resolvedColor;
        if (trimmedType.equalsIgnoreCase(containerTypeBoxLabel)) {
            resolvedColor = containerBannerBoxColor;
        } else if (trimmedType.equalsIgnoreCase(containerTypeBagLabel)) {
            resolvedColor = containerBannerBagColor;
        } else {
            resolvedColor = generateContainerBannerColor(normalizedKey);
        }
        containerBannerColorCache.put(normalizedKey, resolvedColor);
        return resolvedColor;
    }

    @ColorInt
    private int generateContainerBannerColor(@NonNull String normalizedKey) {
        int hash = normalizedKey.hashCode();
        int positiveHash = hash & 0x7fffffff;
        float hue = positiveHash % 360;
        float saturation = 0.5f;
        float value = 0.88f;
        if (containerBannerPalette.length > 0) {
            int paletteIndex = positiveHash % containerBannerPalette.length;
            float[] hsv = new float[3];
            Color.colorToHSV(containerBannerPalette[paletteIndex], hsv);
            saturation = clamp(hsv[1], 0.35f, 0.7f);
            value = clamp(hsv[2], 0.8f, 0.95f);
        }
        float[] hsvColor = new float[] { hue, saturation, value };
        int generatedColor = Color.HSVToColor(hsvColor);
        int attempts = 0;
        while (isItemBannerColor(generatedColor) && attempts < 12) {
            hue = (hue + 30f) % 360f;
            hsvColor[0] = hue;
            generatedColor = Color.HSVToColor(hsvColor);
            attempts++;
        }
        if (isItemBannerColor(generatedColor)) {
            return containerBannerDefaultColor;
        }
        return generatedColor;
    }

    private boolean isItemBannerColor(@ColorInt int color) {
        for (int candidate : itemBannerColors) {
            if (candidate == color) {
                return true;
            }
        }
        return false;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static class HierarchyStyle {
        @ColorInt
        final int backgroundColor;
        @ColorInt
        final int accentColor;
        @ColorInt
        final int bannerColor;

        HierarchyStyle(@ColorInt int backgroundColor, @ColorInt int accentColor,
                @ColorInt int bannerColor) {
            this.backgroundColor = backgroundColor;
            this.accentColor = accentColor;
            this.bannerColor = bannerColor;
        }
    }

    static class ContainerPopupRestoreState {
        final int containerPosition;
        final int visibilityMask;

        ContainerPopupRestoreState(int containerPosition, int visibilityMask) {
            this.containerPosition = containerPosition;
            this.visibilityMask = visibilityMask;
        }
    }

    private void setActiveContainerPopup(int position, int visibilityMask) {
        activeContainerPopupAdapterPosition = position;
        activeContainerPopupVisibilityMask = visibilityMask;
    }

    private void onContainerPopupDismissed(int position) {
        if (activeContainerPopupAdapterPosition == position) {
            activeContainerPopupAdapterPosition = RecyclerView.NO_POSITION;
            activeContainerPopupVisibilityMask = VISIBILITY_DEFAULT_MASK;
        }
    }

    private void preparePendingContainerPopupRestore(int targetPosition) {
        if (activeContainerPopupAdapterPosition == RecyclerView.NO_POSITION) {
            pendingContainerPopupRestore = null;
            return;
        }
        int containerPosition = activeContainerPopupAdapterPosition;
        if (targetPosition != containerPosition
                && !isDescendantOf(containerPosition, targetPosition)) {
            pendingContainerPopupRestore = null;
            return;
        }
        pendingContainerPopupRestore = new ContainerPopupRestoreState(containerPosition,
                activeContainerPopupVisibilityMask);
    }

    @Nullable
    public ContainerPopupRestoreState consumePendingContainerPopupRestoreState() {
        ContainerPopupRestoreState state = pendingContainerPopupRestore;
        pendingContainerPopupRestore = null;
        return state;
    }

    public void restoreContainerPopup(@NonNull RecyclerView recyclerView,
            @NonNull ContainerPopupRestoreState state) {
        RecyclerView.ViewHolder holder = recyclerView
                .findViewHolderForAdapterPosition(state.containerPosition);
        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).reopenContainerPopup(state.visibilityMask);
        } else {
            recyclerView.scrollToPosition(state.containerPosition);
            recyclerView.post(() -> {
                RecyclerView.ViewHolder postHolder = recyclerView
                        .findViewHolderForAdapterPosition(state.containerPosition);
                if (postHolder instanceof ViewHolder) {
                    ((ViewHolder) postHolder).reopenContainerPopup(state.visibilityMask);
                }
            });
        }
    }

    interface OnRoomContentInteractionListener {
        void onCopyRoomContent(@NonNull RoomContentItem item, int position);

        void onMoveRoomContent(@NonNull RoomContentItem item, int position);

        void onEditRoomContent(@NonNull RoomContentItem item, int position);

        void onDeleteRoomContent(@NonNull RoomContentItem item, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        final ViewGroup groupWrapperView;
        final MaterialCardView cardView;
        final View bannerContainer;
        @Nullable
        final View cardBackground;
        final View detailsContainer;
        @Nullable
        final ImageView photoView;
        final TextView nameView;
        final TextView commentView;
        final TextView metadataView;
        @Nullable
        final RecyclerView childrenRecyclerView;
        @Nullable
        final ImageView toggleView;
        @Nullable
        final ImageView menuView;
        @Nullable
        final ImageView addView;
        @Nullable
        final View filledIndicatorView;
        @Nullable
        final ViewGroup filterChipGroup;
        @Nullable
        final Chip containersFilterChip;
        @Nullable
        final Chip itemsFilterChip;
        @Nullable
        final OnRoomContentInteractionListener interactionListener;
        @Nullable
        private RoomContentItem currentItem;
        private final int defaultPaddingStart;
        private final int defaultPaddingTop;
        private final int defaultPaddingEnd;
        private final int defaultPaddingBottom;
        private final int defaultMarginLeft;
        private final int defaultMarginTop;
        private final int defaultMarginRight;
        private final int defaultMarginBottom;
        @Nullable
        private final ColorStateList defaultCardBackgroundColor;
        @Nullable
        private final ColorStateList defaultStrokeColor;
        private final int defaultStrokeWidth;
        private final int defaultContentPaddingLeft;
        private final int defaultContentPaddingTop;
        private final int defaultContentPaddingRight;
        private final int defaultContentPaddingBottom;
        @Nullable
        private final Drawable.ConstantState groupWrapperBorderState;
        private final int defaultGroupPaddingLeft;
        private final int defaultGroupPaddingTop;
        private final int defaultGroupPaddingRight;
        private final int defaultGroupPaddingBottom;
        private static final int MAX_VISIBLE_FURNITURE_COLUMNS = 5;
        @Nullable
        private PopupWindow optionsPopup;
        @Nullable
        private PopupWindow furniturePopup;
        @Nullable
        private PopupWindow furniturePhotoMenuPopup;
        @Nullable
        private PopupWindow containerPopup;
        private int containerPopupVisibilityMask = VISIBILITY_DEFAULT_MASK;
        private int containerPopupAdapterPosition = RecyclerView.NO_POSITION;
        private boolean suppressFilterCallbacks;
        private final int defaultPhotoVisibility;
        private final int defaultMenuVisibility;
        private final int defaultToggleVisibility;
        private final int defaultAddVisibility;
        private final int defaultFilterVisibility;

        ViewHolder(@NonNull View itemView,
                @Nullable OnRoomContentInteractionListener interactionListener) {
            super(itemView);
            View potentialGroupWrapper = itemView.findViewById(R.id.groupFullWrapper);
            groupWrapperView = potentialGroupWrapper instanceof ViewGroup
                    ? (ViewGroup) potentialGroupWrapper
                    : null;
            cardView = itemView.findViewById(R.id.card_room_content);
            bannerContainer = itemView.findViewById(R.id.container_room_content_banner);
            cardBackground = itemView.findViewById(R.id.container_room_content_root);
            detailsContainer = itemView.findViewById(R.id.container_room_content_details);
            childrenRecyclerView = itemView.findViewById(R.id.recycler_room_content_children);
            photoView = itemView.findViewById(R.id.image_room_content_photo);
            nameView = itemView.findViewById(R.id.text_room_content_name);
            commentView = itemView.findViewById(R.id.text_room_content_comment);
            metadataView = itemView.findViewById(R.id.text_room_content_metadata);
            toggleView = itemView.findViewById(R.id.image_room_content_toggle);
            menuView = itemView.findViewById(R.id.image_room_content_menu);
            addView = itemView.findViewById(R.id.image_room_content_add);
            filledIndicatorView = itemView.findViewById(R.id.view_room_container_filled_indicator);
            filterChipGroup = itemView.findViewById(R.id.chip_group_room_content_filters);
            containersFilterChip = itemView.findViewById(R.id.chip_room_content_filter_containers);
            itemsFilterChip = itemView.findViewById(R.id.chip_room_content_filter_items);
            this.interactionListener = interactionListener;
            bannerContainer.setOnClickListener(view -> handleBannerClick());
            if (photoView != null) {
                photoView.setOnClickListener(view -> notifyEdit());
            }
            if (toggleView != null) {
                toggleView.setOnClickListener(view -> toggleExpansion());
            }
            if (menuView != null) {
                menuView.setOnClickListener(view -> toggleOptionsMenu(menuView));
            }
            if (containersFilterChip != null) {
                containersFilterChip.setOnClickListener(
                        view -> onFilterChipToggled(containersFilterChip, VISIBILITY_FLAG_CONTAINERS));
            }
            if (itemsFilterChip != null) {
                itemsFilterChip.setOnClickListener(
                        view -> onFilterChipToggled(itemsFilterChip, VISIBILITY_FLAG_ITEMS));
            }
            if (addView != null) {
                addView.setOnClickListener(view -> Toast.makeText(itemView.getContext(),
                        R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());
            }
            defaultPaddingStart = photoView != null ? photoView.getPaddingStart() : 0;
            defaultPaddingTop = photoView != null ? photoView.getPaddingTop() : 0;
            defaultPaddingEnd = photoView != null ? photoView.getPaddingEnd() : 0;
            defaultPaddingBottom = photoView != null ? photoView.getPaddingBottom() : 0;
            defaultPhotoVisibility = photoView != null ? photoView.getVisibility() : View.GONE;
            defaultMenuVisibility = menuView != null ? menuView.getVisibility() : View.GONE;
            defaultToggleVisibility = toggleView != null ? toggleView.getVisibility() : View.GONE;
            defaultAddVisibility = addView != null ? addView.getVisibility() : View.GONE;
            defaultFilterVisibility = filterChipGroup != null
                    ? filterChipGroup.getVisibility()
                    : View.GONE;
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            if (params instanceof RecyclerView.LayoutParams) {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) params;
                defaultMarginLeft = layoutParams.leftMargin;
                defaultMarginTop = layoutParams.topMargin;
                defaultMarginRight = layoutParams.rightMargin;
                defaultMarginBottom = layoutParams.bottomMargin;
            } else {
                defaultMarginLeft = 0;
                defaultMarginTop = 0;
                defaultMarginRight = 0;
                defaultMarginBottom = 0;
            }
            defaultCardBackgroundColor = cardView != null ? cardView.getCardBackgroundColor() : null;
            defaultStrokeColor = cardView != null ? cardView.getStrokeColorStateList() : null;
            defaultStrokeWidth = cardView != null ? cardView.getStrokeWidth() : 0;
            defaultContentPaddingLeft = cardView != null ? cardView.getContentPaddingLeft() : 0;
            defaultContentPaddingTop = cardView != null ? cardView.getContentPaddingTop() : 0;
            defaultContentPaddingRight = cardView != null ? cardView.getContentPaddingRight() : 0;
            defaultContentPaddingBottom = cardView != null ? cardView.getContentPaddingBottom() : 0;
            if (groupWrapperView != null) {
                Drawable background = groupWrapperView.getBackground();
                groupWrapperBorderState = background != null ? background.getConstantState() : null;
                defaultGroupPaddingLeft = groupWrapperView.getPaddingLeft();
                defaultGroupPaddingTop = groupWrapperView.getPaddingTop();
                defaultGroupPaddingRight = groupWrapperView.getPaddingRight();
                defaultGroupPaddingBottom = groupWrapperView.getPaddingBottom();
                groupWrapperView.setBackground(null);
            } else {
                groupWrapperBorderState = null;
                defaultGroupPaddingLeft = 0;
                defaultGroupPaddingTop = 0;
                defaultGroupPaddingRight = 0;
                defaultGroupPaddingBottom = 0;
            }
        }

        void bind(@NonNull RoomContentItem item, int position) {
            currentItem = item;
            dismissOptionsMenu();
            dismissContainerPopup();
            dismissFurniturePopup();
            if (photoView != null) {
                photoView.setVisibility(defaultPhotoVisibility);
            }
            if (menuView != null) {
                menuView.setVisibility(defaultMenuVisibility);
            }
            if (toggleView != null) {
                toggleView.setVisibility(defaultToggleVisibility);
            }
            if (addView != null) {
                addView.setVisibility(defaultAddVisibility);
            }
            if (filterChipGroup != null) {
                filterChipGroup.setVisibility(defaultFilterVisibility);
            }
            String baseName = resolveItemName(item);
            if (item.isContainer()) {
                baseName = appendAttachmentCount(baseName, item);
            }
            String displayName = baseName;
            String rankLabel = item.getDisplayRank();
            if (rankLabel != null && !rankLabel.trim().isEmpty()) {
                displayName = rankLabel + " · " + baseName;
            }
            String placementLabel = formatFurniturePlacement(item);
            if (placementLabel != null && !placementLabel.isEmpty()) {
                displayName = displayName + " · " + placementLabel;
            }
            nameView.setText(displayName);
            resetCardStyle();
            int parentPosition = RoomContentAdapter.this
                    .findAttachedContainerPosition(position);
            RoomContentItem parentItem = parentPosition >= 0
                    ? RoomContentAdapter.this.items.get(parentPosition)
                    : null;
            boolean parentExpanded = parentItem != null
                    && RoomContentAdapter.this.isContainerExpanded(parentPosition)
                    && parentItem.hasAttachedItems();
            boolean hasAttachedItems = item.isContainer() && item.hasAttachedItems();
            boolean isContainerExpanded = hasAttachedItems
                    && RoomContentAdapter.this.isContainerExpanded(position);
            boolean showGroupBorder = hasAttachedItems;
            boolean isFirstInGroup = showGroupBorder;
            boolean isLastInGroup = showGroupBorder && !isContainerExpanded;
            if (item.isContainer()) {
                if (!hasAttachedItems && parentExpanded && parentPosition >= 0) {
                    showGroupBorder = true;
                    isFirstInGroup = false;
                    isLastInGroup = RoomContentAdapter.this
                            .isLastDirectChild(parentPosition, position);
                } else if (isContainerExpanded) {
                    isLastInGroup = false;
                }
            } else {
                showGroupBorder = false;
                if (parentExpanded && parentPosition >= 0) {
                    showGroupBorder = true;
                    isFirstInGroup = false;
                    isLastInGroup = RoomContentAdapter.this
                            .isLastDirectChild(parentPosition, position);
                }
            }
            updateGroupWrapperBorder(showGroupBorder, isFirstInGroup, isLastInGroup);
            if (filterChipGroup != null) {
                if (hasAttachedItems) {
                    filterChipGroup.setVisibility(View.VISIBLE);
                    suppressFilterCallbacks = true;
                    int visibilityMask = RoomContentAdapter.this.getContainerVisibilityMask(position);
                    if (containersFilterChip != null) {
                        containersFilterChip.setChecked(
                                (visibilityMask & VISIBILITY_FLAG_CONTAINERS) != 0);
                    }
                    if (itemsFilterChip != null) {
                        itemsFilterChip.setChecked(
                                (visibilityMask & VISIBILITY_FLAG_ITEMS) != 0);
                    }
                    suppressFilterCallbacks = false;
                } else {
                    suppressFilterCallbacks = true;
                    if (containersFilterChip != null) {
                        containersFilterChip.setChecked(true);
                    }
                    if (itemsFilterChip != null) {
                        itemsFilterChip.setChecked(true);
                    }
                    suppressFilterCallbacks = false;
                    filterChipGroup.setVisibility(View.GONE);
                }
            }
            if (item.isContainer() && menuView != null) {
                menuView.setVisibility(View.GONE);
            }
            int depth = RoomContentAdapter.this.computeHierarchyDepth(position);
            HierarchyStyle currentStyle = RoomContentAdapter.this.resolveHierarchyStyle(depth);
            HierarchyStyle parentStyleForFrame = null;
            if (parentExpanded && parentPosition >= 0) {
                parentStyleForFrame = RoomContentAdapter.this.resolveHierarchyStyle(
                        RoomContentAdapter.this.computeHierarchyDepth(parentPosition));
            }
            if (cardView != null) {
                float elevation = RoomContentAdapter.this.resolveCardElevation(depth);
                cardView.setCardElevation(elevation);
                cardView.setMaxCardElevation(elevation);
            }
            View backgroundTarget = cardBackground != null ? cardBackground : itemView;
            if (item.isContainer()) {
                boolean joinsParentFrame = parentExpanded && parentStyleForFrame != null;
                boolean isLastChildInParentGroup = joinsParentFrame
                        && RoomContentAdapter.this.isLastDirectChild(parentPosition, position);
                RoomContentAdapter.this.applyContainerBackground(backgroundTarget, currentStyle,
                        hasAttachedItems, isContainerExpanded, joinsParentFrame,
                        isLastChildInParentGroup);
                RoomContentAdapter.this.applyContainerBannerColor(bannerContainer, currentStyle,
                        item.getType());
                if (filledIndicatorView != null) {
                    RoomContentAdapter.this.applyFilledIndicatorStyle(filledIndicatorView);
                }
                if (addView != null) {
                    addView.setVisibility(View.GONE);
                    addView.setEnabled(false);
                    addView.setAlpha(0f);
                }
            } else {
                RoomContentItem attachedContainer = RoomContentAdapter.this
                        .findAttachedContainer(position);
                boolean isPartOfExpandedContainer = false;
                boolean isLastAttachment = false;
                HierarchyStyle parentStyle = parentExpanded ? parentStyleForFrame : null;
                if (attachedContainer != null && parentStyle != null) {
                    int containerPosition = RoomContentAdapter.this
                            .findAttachedContainerPosition(position);
                    if (containerPosition >= 0) {
                        boolean containerExpanded = RoomContentAdapter.this
                                .isContainerExpanded(containerPosition)
                                && attachedContainer.hasAttachedItems();
                        if (containerExpanded) {
                            isPartOfExpandedContainer = true;
                            isLastAttachment = RoomContentAdapter.this.isLastDirectChild(
                                    containerPosition, position);
                        }
                    }
                }
                if (isPartOfExpandedContainer && parentStyle != null) {
                    RoomContentAdapter.this.applyAttachmentBackground(backgroundTarget,
                            parentStyle, isLastAttachment);
                } else {
                    RoomContentAdapter.this.applyStandaloneContentBackground(backgroundTarget,
                            currentStyle);
                }
                applyBannerColor(bannerContainer, item.getType());
                if (addView != null) {
                    addView.setVisibility(View.GONE);
                }
            }
            boolean hiddenByHierarchy = RoomContentAdapter.this
                    .isItemHiddenByCollapsedContainer(position);
            boolean hiddenByDisplayFlag = !item.isContainer() && !item.isDisplayed();
            boolean shouldHide = hiddenByHierarchy || hiddenByDisplayFlag;
            if (!item.isContainer()) {
                if (!hiddenByHierarchy) {
                    item.setDisplayed(!hiddenByDisplayFlag);
                }
            } else {
                item.setDisplayed(!hiddenByHierarchy);
            }
            itemView.setVisibility(shouldHide ? View.GONE : View.VISIBLE);
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            if (layoutParams instanceof RecyclerView.LayoutParams) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) layoutParams;
                if (shouldHide) {
                    params.height = 0;
                    params.leftMargin = 0;
                    params.topMargin = 0;
                    params.rightMargin = 0;
                    params.bottomMargin = 0;
                } else {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.leftMargin = defaultMarginLeft + (depth * hierarchyIndentPx);
                    params.topMargin = defaultMarginTop;
                    params.rightMargin = defaultMarginRight;
                    params.bottomMargin = resolveBottomMargin(item, position,
                            isContainerExpanded);
                }
                itemView.setLayoutParams(params);
            }
            if (filledIndicatorView != null) {
                boolean isExpanded = RoomContentAdapter.this.isContainerExpanded(position);
                boolean showIndicator = item.isContainer() && item.hasAttachedItems() && !isExpanded;
                filledIndicatorView.setVisibility(showIndicator ? View.VISIBLE : View.GONE);
            }
            if (item.isFurniture()) {
                if (photoView != null) {
                    photoView.setVisibility(View.GONE);
                }
                if (menuView != null) {
                    menuView.setVisibility(View.GONE);
                }
                if (addView != null) {
                    addView.setVisibility(View.GONE);
                }
                if (toggleView != null) {
                    toggleView.setVisibility(View.GONE);
                }
                if (filterChipGroup != null) {
                    filterChipGroup.setVisibility(View.GONE);
                }
            }
            updatePhoto(item);
            if (!item.isFurniture()) {
                if (photoView != null) {
                    photoView.setContentDescription(itemView.getContext()
                            .getString(R.string.content_description_room_content_photos, displayName));
                }
                if (menuView != null) {
                    menuView.setContentDescription(itemView.getContext()
                            .getString(R.string.content_description_room_content_menu, displayName));
                }
            } else {
                if (photoView != null) {
                    photoView.setContentDescription(null);
                }
                if (menuView != null) {
                    menuView.setContentDescription(null);
                }
            }
        }

        void updateToggle(boolean hasDetails, boolean isExpanded, @NonNull String name) {
            if (toggleView == null) {
                return;
            }
            if (currentItem != null && currentItem.isContainer()) {
                toggleView.setVisibility(View.GONE);
                toggleView.setContentDescription(null);
                toggleView.setEnabled(false);
                toggleView.setRotation(0f);
                return;
            }
            if (!hasDetails) {
                toggleView.setVisibility(View.GONE);
                toggleView.setContentDescription(null);
                toggleView.setEnabled(false);
                toggleView.setRotation(0f);
                return;
            }
            toggleView.setVisibility(View.VISIBLE);
            toggleView.setEnabled(true);
            toggleView.setRotation(isExpanded ? 180f : 0f);
            int descriptionRes = isExpanded
                    ? R.string.content_description_room_content_collapse
                    : R.string.content_description_room_content_expand;
            toggleView.setContentDescription(itemView.getContext().getString(descriptionRes, name));
        }

        private void applyGroupWrapperBorder(boolean isFirstInGroup, boolean isLastInGroup) {
            if (groupWrapperView == null) {
                return;
            }
            Drawable drawable = null;
            if (isFirstInGroup && isLastInGroup && groupWrapperBorderState != null) {
                drawable = groupWrapperBorderState.newDrawable();
            }
            if (drawable == null) {
                int backgroundResId;
                if (isFirstInGroup && isLastInGroup) {
                    backgroundResId = R.drawable.group_background;
                } else if (isFirstInGroup) {
                    backgroundResId = R.drawable.bg_room_container_group_header;
                } else if (isLastInGroup) {
                    backgroundResId = R.drawable.bg_room_content_group_footer;
                } else {
                    backgroundResId = R.drawable.bg_room_content_group_middle;
                }
                drawable = ContextCompat.getDrawable(groupWrapperView.getContext(), backgroundResId);
            }
            if (drawable != null) {
                drawable = drawable.mutate();
            }
            groupWrapperView.setBackground(drawable);

            int paddingTop = isFirstInGroup ? defaultGroupPaddingTop : 0;
            int paddingBottom = isLastInGroup ? defaultGroupPaddingBottom : 0;
            groupWrapperView.setPadding(defaultGroupPaddingLeft, paddingTop,
                    defaultGroupPaddingRight, paddingBottom);
        }

        private void resetCardStyle() {
            resetGroupWrapperStyle();
            if (cardView == null) {
                return;
            }
            cardView.setStrokeWidth(defaultStrokeWidth);
            if (defaultStrokeColor != null) {
                cardView.setStrokeColor(defaultStrokeColor);
            } else {
                cardView.setStrokeColor(Color.TRANSPARENT);
            }
            if (defaultCardBackgroundColor != null) {
                cardView.setCardBackgroundColor(defaultCardBackgroundColor);
            } else {
                cardView.setCardBackgroundColor(Color.TRANSPARENT);
            }
            cardView.setContentPadding(defaultContentPaddingLeft, defaultContentPaddingTop,
                    defaultContentPaddingRight, defaultContentPaddingBottom);
        }

        private void resetGroupWrapperStyle() {
            if (groupWrapperView == null) {
                return;
            }
            groupWrapperView.setBackground(null);
            groupWrapperView.setPadding(defaultGroupPaddingLeft, defaultGroupPaddingTop,
                    defaultGroupPaddingRight, defaultGroupPaddingBottom);
        }

        private void updateGroupWrapperBorder(boolean showBorder, boolean isFirstInGroup,
                boolean isLastInGroup) {
            if (groupWrapperView == null) {
                return;
            }
            if (showBorder) {
                applyGroupWrapperBorder(isFirstInGroup, isLastInGroup);
            } else {
                resetGroupWrapperStyle();
            }
        }

        void releaseChildrenAdapter() {
            if (childrenRecyclerView != null) {
                childrenRecyclerView.setAdapter(null);
                childrenRecyclerView.setVisibility(View.GONE);
            }
        }

        private void updatePhoto(@NonNull RoomContentItem item) {
            if (photoView == null) {
                return;
            }
            List<String> photos = item.getPhotos();
            if (photos.isEmpty()) {
                resetPhoto();
                return;
            }
            Bitmap bitmap = decodePhoto(photos.get(0));
            if (bitmap == null) {
                resetPhoto();
                return;
            }
            photoView.setImageBitmap(bitmap);
            photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photoView.setPadding(0, 0, 0, 0);
        }

        private void resetPhoto() {
            if (photoView == null) {
                return;
            }
            photoView.setImageResource(R.drawable.ic_establishment_photos);
            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            photoView.setPadding(defaultPaddingStart, defaultPaddingTop, defaultPaddingEnd,
                    defaultPaddingBottom);
        }

        @Nullable
        private Bitmap decodePhoto(@NonNull String encoded) {
            try {
                byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        private void handleBannerClick() {
            if (currentItem == null) {
                return;
            }
            if (currentItem.isFurniture()) {
                toggleFurniturePopup();
            } else if (currentItem.isContainer()) {
                toggleContainerPopup();
            } else {
                notifyEdit();
            }
        }

        private void toggleContainerPopup() {
            if (currentItem == null || !currentItem.isContainer()) {
                return;
            }
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            if (containerPopup != null && containerPopup.isShowing()) {
                containerPopup.dismiss();
                return;
            }
            openContainerPopup(position, VISIBILITY_DEFAULT_MASK);
        }

        private void openContainerPopup(int position, int initialVisibilityMask) {
            if (containerPopup != null && containerPopup.isShowing()) {
                containerPopup.dismiss();
            }
            dismissFurniturePopup();
            LayoutInflater layoutInflater = RoomContentAdapter.this.inflater;
            View popupView = layoutInflater.inflate(R.layout.popup_container_details, null);
            TextView titleView = popupView.findViewById(R.id.text_container_popup_name);
            if (titleView != null) {
                CharSequence displayedName = nameView.getText();
                titleView.setText(displayedName != null && displayedName.length() > 0
                        ? displayedName
                        : currentItem.getName());
            }
            ViewGroup childrenContainer = popupView.findViewById(R.id.container_container_popup_children);
            if (childrenContainer != null) {
                clearContainerPopupChildren(childrenContainer);
                childrenContainer.setVisibility(View.VISIBLE);
            }
            CharSequence resolvedLabel = nameView.getText();
            if (resolvedLabel == null || resolvedLabel.length() == 0) {
                resolvedLabel = currentItem.getName();
            }
            final CharSequence toggleLabel = resolvedLabel;
            containerPopupVisibilityMask = initialVisibilityMask;
            containerPopupAdapterPosition = position;
            RoomContentAdapter.this.setActiveContainerPopup(position, containerPopupVisibilityMask);
            ImageView toggleIcon = popupView.findViewById(R.id.icon_container_popup_toggle);
            if (toggleIcon != null) {
                if (childrenContainer == null) {
                    toggleIcon.setVisibility(View.GONE);
                    toggleIcon.setOnClickListener(null);
                } else {
                    updateContainerPopupToggleIcon(toggleIcon, containerPopupVisibilityMask,
                            toggleLabel);
                    toggleIcon.setOnClickListener(view -> {
                        if (currentItem == null || !currentItem.hasAttachedItems()
                                || childrenContainer == null) {
                            return;
                        }
                        containerPopupVisibilityMask = cyclePopupVisibilityMask(
                                containerPopupVisibilityMask);
                        RoomContentAdapter.this.setActiveContainerPopup(position,
                                containerPopupVisibilityMask);
                        refreshContainerPopupChildren(childrenContainer, position,
                                containerPopupVisibilityMask);
                        updateContainerPopupToggleIcon(toggleIcon, containerPopupVisibilityMask,
                                toggleLabel);
                    });
                }
            }
            ImageView menuIcon = popupView.findViewById(R.id.icon_container_popup_menu);
            if (menuIcon != null) {
                menuIcon.setOnClickListener(view -> toggleOptionsMenu(menuIcon));
            }
            ImageView groupPreviewView = popupView.findViewById(R.id.image_container_popup_group_preview);
            if (childrenContainer != null) {
                refreshContainerPopupChildren(childrenContainer, position,
                        containerPopupVisibilityMask);
            }
            updateContainerPopupGroupPreview(groupPreviewView);
            PopupWindow popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            float elevation = itemView.getResources().getDisplayMetrics().density * 6f;
            popupWindow.setElevation(elevation);
            ViewGroup finalChildrenContainer = childrenContainer;
            popupWindow.setOnDismissListener(() -> {
                containerPopup = null;
                RoomContentAdapter.this.onContainerPopupDismissed(containerPopupAdapterPosition);
                containerPopupAdapterPosition = RecyclerView.NO_POSITION;
                containerPopupVisibilityMask = VISIBILITY_DEFAULT_MASK;
                if (groupPreviewView != null) {
                    groupPreviewView.setImageDrawable(null);
                    groupPreviewView.setVisibility(View.GONE);
                }
                if (finalChildrenContainer != null) {
                    clearContainerPopupChildren(finalChildrenContainer);
                }
            });
            containerPopup = popupWindow;

            popupView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int popupWidth = popupView.getMeasuredWidth();
            int popupHeight = popupView.getMeasuredHeight();
            Resources resources = itemView.getResources();
            float density = resources.getDisplayMetrics().density;
            int verticalOffset = (int) (density * 8);

            Rect displayFrame = new Rect();
            bannerContainer.getWindowVisibleDisplayFrame(displayFrame);
            int[] location = new int[2];
            bannerContainer.getLocationOnScreen(location);
            int anchorBottom = location[1] + bannerContainer.getHeight();
            int spaceBelow = displayFrame.bottom - anchorBottom;
            int spaceAbove = location[1] - displayFrame.top;

            int totalHorizontalMargin = resources.getDimensionPixelSize(
                    R.dimen.container_popup_screen_margin_horizontal);
            int maxPopupWidth = displayFrame.width() - totalHorizontalMargin;
            if (maxPopupWidth > 0) {
                popupWindow.setWidth(maxPopupWidth);
            }

            int totalVerticalMargin = resources.getDimensionPixelSize(
                    R.dimen.container_popup_screen_margin_vertical);
            int availableHeight = Math.max(0,
                    displayFrame.height() - (2 * totalVerticalMargin));
            int finalHeight = Math.min(popupHeight, availableHeight);
            int maxHeightBelow = Math.max(0, spaceBelow - totalVerticalMargin);
            int maxHeightAbove = Math.max(0, spaceAbove - totalVerticalMargin);
            boolean showAbove;
            if (maxHeightBelow >= maxHeightAbove) {
                showAbove = false;
            } else {
                showAbove = true;
            }
            if (finalHeight < popupHeight) {
                popupWindow.setHeight(finalHeight);
            } else {
                finalHeight = popupHeight;
            }

            int frameTop = displayFrame.top + totalVerticalMargin;
            int frameBottom = displayFrame.bottom - totalVerticalMargin;

            int yOffset;
            if (showAbove) {
                yOffset = -(bannerContainer.getHeight() + finalHeight + verticalOffset);
                int popupTop = anchorBottom + yOffset;
                int overflowAbove = frameTop - popupTop;
                if (overflowAbove > 0) {
                    yOffset += overflowAbove;
                }
                int popupBottom = anchorBottom + yOffset + finalHeight;
                int overflowBelow = popupBottom - frameBottom;
                if (overflowBelow > 0) {
                    yOffset -= overflowBelow;
                    int adjustedTop = anchorBottom + yOffset;
                    int readjustAbove = frameTop - adjustedTop;
                    if (readjustAbove > 0) {
                        yOffset += readjustAbove;
                    }
                }
            } else {
                yOffset = verticalOffset;
                int popupTop = anchorBottom + yOffset;
                int popupBottom = popupTop + finalHeight;
                int overflowBelow = popupBottom - frameBottom;
                if (overflowBelow > 0) {
                    yOffset -= overflowBelow;
                }
                int adjustedTop = anchorBottom + yOffset;
                int overflowAbove = frameTop - adjustedTop;
                if (overflowAbove > 0) {
                    yOffset += overflowAbove;
                    int adjustedBottom = anchorBottom + yOffset + finalHeight;
                    int readjustBelow = adjustedBottom - frameBottom;
                    if (readjustBelow > 0) {
                        yOffset -= readjustBelow;
                    }
                }
            }

            PopupWindowCompat.showAsDropDown(popupWindow, bannerContainer, 0, yOffset, Gravity.START);
        }

        void reopenContainerPopup(int visibilityMask) {
            if (currentItem == null || !currentItem.isContainer()) {
                return;
            }
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            openContainerPopup(position, visibilityMask);
        }

        private void updateContainerPopupGroupPreview(@Nullable ImageView previewView) {
            if (previewView == null) {
                return;
            }
            if (groupWrapperView == null) {
                previewView.setImageDrawable(null);
                previewView.setVisibility(View.GONE);
                return;
            }
            Bitmap previewBitmap = captureViewBitmap(groupWrapperView);
            if (previewBitmap == null) {
                previewView.setImageDrawable(null);
                previewView.setVisibility(View.GONE);
                return;
            }
            previewView.setImageBitmap(previewBitmap);
            previewView.setVisibility(View.VISIBLE);
        }

        @Nullable
        private Bitmap captureViewBitmap(@NonNull View source) {
            int width = source.getWidth();
            int height = source.getHeight();
            if (width <= 0 || height <= 0) {
                source.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                width = source.getMeasuredWidth();
                height = source.getMeasuredHeight();
            }
            if (width <= 0 || height <= 0) {
                return null;
            }
            try {
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                source.draw(canvas);
                return bitmap;
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        private void updateContainerPopupToggleIcon(@NonNull ImageView toggleIcon,
                int visibilityMask, @Nullable CharSequence label) {
            boolean hasDetails = currentItem != null && currentItem.hasAttachedItems();
            if (!hasDetails) {
                toggleIcon.setVisibility(View.GONE);
                toggleIcon.setContentDescription(null);
                toggleIcon.setImageDrawable(null);
                return;
            }
            toggleIcon.setVisibility(View.VISIBLE);
            toggleIcon.setRotation(0f);
            int normalizedMask = normalizePopupVisibilityMask(visibilityMask);
            toggleIcon.setImageResource(resolveContainerPopupToggleIcon(normalizedMask));
            String labelText;
            if (label != null && label.length() > 0) {
                labelText = label.toString();
            } else if (currentItem != null && currentItem.getName() != null) {
                labelText = currentItem.getName();
            } else {
                labelText = "";
            }
            @StringRes int descriptionRes;
            if (normalizedMask == VISIBILITY_FLAG_CONTAINERS) {
                descriptionRes = R.string
                        .content_description_room_content_popup_filter_containers;
            } else if (normalizedMask == VISIBILITY_FLAG_ITEMS) {
                descriptionRes = R.string.content_description_room_content_popup_filter_items;
            } else {
                descriptionRes = R.string.content_description_room_content_popup_filter_all;
            }
            toggleIcon.setContentDescription(
                    toggleIcon.getContext().getString(descriptionRes, labelText));
        }

        @DrawableRes
        private int resolveContainerPopupToggleIcon(int visibilityMask) {
            if (visibilityMask == VISIBILITY_FLAG_CONTAINERS) {
                return R.drawable.ic_square_containers;
            }
            if (visibilityMask == VISIBILITY_FLAG_ITEMS) {
                return R.drawable.ic_square_items;
            }
            return R.drawable.ic_square_empty;
        }

        private int normalizePopupVisibilityMask(int visibilityMask) {
            int normalized = visibilityMask & (VISIBILITY_FLAG_CONTAINERS | VISIBILITY_FLAG_ITEMS);
            if (normalized == VISIBILITY_FLAG_CONTAINERS
                    || normalized == VISIBILITY_FLAG_ITEMS) {
                return normalized;
            }
            if ((normalized & VISIBILITY_FLAG_CONTAINERS) != 0
                    && (normalized & VISIBILITY_FLAG_ITEMS) != 0) {
                return VISIBILITY_DEFAULT_MASK;
            }
            return VISIBILITY_DEFAULT_MASK;
        }

        private int cyclePopupVisibilityMask(int currentMask) {
            int normalized = normalizePopupVisibilityMask(currentMask);
            if (normalized == VISIBILITY_DEFAULT_MASK) {
                return VISIBILITY_FLAG_CONTAINERS;
            }
            if (normalized == VISIBILITY_FLAG_CONTAINERS) {
                return VISIBILITY_FLAG_ITEMS;
            }
            return VISIBILITY_DEFAULT_MASK;
        }

        private void refreshContainerPopupChildren(@Nullable ViewGroup childrenContainer,
                int fallbackPosition, int visibilityMask) {
            if (childrenContainer == null) {
                return;
            }
            int updatedPosition = getBindingAdapterPosition();
            int targetPosition = updatedPosition != RecyclerView.NO_POSITION
                    ? updatedPosition
                    : fallbackPosition;
            populateContainerPopupChildren(childrenContainer, targetPosition, visibilityMask);
        }

        private void populateContainerPopupChildren(@NonNull ViewGroup container,
                int containerPosition, int visibilityMask) {
            container.removeAllViews();
            List<Integer> childPositions = collectContainerPopupChildPositions(containerPosition,
                    visibilityMask);
            LayoutInflater layoutInflater = RoomContentAdapter.this.inflater;
            if (childPositions.isEmpty()) {
                View emptyView = layoutInflater.inflate(R.layout.item_furniture_section_empty,
                        container, false);
                TextView emptyText = emptyView.findViewById(R.id.text_furniture_section_empty);
                if (emptyText != null) {
                    emptyText.setText(R.string.container_popup_children_empty);
                }
                container.addView(emptyView);
                container.setVisibility(View.VISIBLE);
                return;
            }
            int baseDepth = RoomContentAdapter.this.computeHierarchyDepth(containerPosition);
            for (Integer childPosition : childPositions) {
                if (childPosition == null) {
                    continue;
                }
                View entryView = layoutInflater.inflate(R.layout.popup_container_child_entry,
                        container, false);
                bindContainerPopupEntry(entryView, containerPosition, baseDepth, childPosition);
                container.addView(entryView);
            }
            container.setVisibility(View.VISIBLE);
        }

        private void clearContainerPopupChildren(@Nullable ViewGroup container) {
            if (container == null) {
                return;
            }
            container.removeAllViews();
            container.setVisibility(View.GONE);
        }

        @NonNull
        private List<Integer> collectContainerPopupChildPositions(int containerPosition,
                int visibilityMask) {
            List<Integer> result = new ArrayList<>();
            if (containerPosition < 0 || containerPosition >= items.size()) {
                return result;
            }
            RoomContentItem containerItem = items.get(containerPosition);
            if (containerItem == null || !containerItem.isContainer()) {
                return result;
            }
            boolean showContainers = (visibilityMask & VISIBILITY_FLAG_CONTAINERS) != 0;
            boolean showItems = (visibilityMask & VISIBILITY_FLAG_ITEMS) != 0;
            RoomContentAdapter.this.ensureHierarchyComputed();
            for (int index = containerPosition + 1; index < items.size(); index++) {
                if (!RoomContentAdapter.this.isDescendantOf(containerPosition, index)) {
                    break;
                }
                if (RoomContentAdapter.this.findAttachedContainerPosition(index)
                        != containerPosition) {
                    continue;
                }
                RoomContentItem child = items.get(index);
                if (child == null) {
                    continue;
                }
                if (child.isContainer()) {
                    if (!showContainers) {
                        continue;
                    }
                } else if (!showItems) {
                    continue;
                }
                result.add(index);
            }
            return result;
        }

        private void bindContainerPopupEntry(@NonNull View entryView, int containerPosition,
                int baseDepth, int childPosition) {
            RoomContentItem child = RoomContentAdapter.this.getItemAt(childPosition);
            if (child == null) {
                return;
            }
            View cardBackground = entryView.findViewById(R.id.container_popup_child_card);
            View bannerContainerView = entryView.findViewById(R.id.container_popup_child_banner);
            TextView titleView = entryView.findViewById(R.id.text_container_popup_child_title);
            TextView commentView = entryView.findViewById(R.id.text_container_popup_child_comment);
            ImageView photoIcon = entryView.findViewById(R.id.image_container_popup_child_photo);
            ImageView menuIcon = entryView.findViewById(R.id.image_container_popup_child_menu);

            String baseName = RoomContentAdapter.this.resolveItemName(child);
            if (child.isContainer()) {
                baseName = RoomContentAdapter.this.appendAttachmentCount(baseName, child);
            }
            String displayName = baseName;
            String rankLabel = child.getDisplayRank();
            if (rankLabel != null && !rankLabel.trim().isEmpty()) {
                displayName = rankLabel + " · " + baseName;
            }
            String placementLabel = formatFurniturePlacement(child);
            if (placementLabel != null && !placementLabel.isEmpty()) {
                displayName = displayName + " · " + placementLabel;
            }
            final String resolvedDisplayName = displayName;

            if (titleView != null) {
                titleView.setText(displayName);
            }

            if (commentView != null) {
                CharSequence formattedComment = RoomContentAdapter.this
                        .formatComment(child.getComment());
                if (formattedComment != null) {
                    commentView.setVisibility(View.VISIBLE);
                    commentView.setText(formattedComment);
                } else {
                    commentView.setVisibility(View.GONE);
                    commentView.setText(null);
                }
            }

            ViewGroup.LayoutParams layoutParams = entryView.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
                int childDepth = RoomContentAdapter.this.computeHierarchyDepth(childPosition);
                int relativeDepth = Math.max(0, childDepth - baseDepth - 1);
                int indent = relativeDepth * RoomContentAdapter.this.hierarchyIndentPx;
                marginParams.setMarginStart(indent);
                entryView.setLayoutParams(marginParams);
            }

            int depth = RoomContentAdapter.this.computeHierarchyDepth(childPosition);
            HierarchyStyle style = RoomContentAdapter.this.resolveHierarchyStyle(depth);
            if (child.isContainer()) {
                boolean hasAttachments = child.hasAttachedItems();
                boolean isExpanded = hasAttachments
                        && RoomContentAdapter.this.isContainerExpanded(childPosition);
                boolean joinsParentFrame = false;
                boolean isLastChildInParent = true;
                int parentPosition = RoomContentAdapter.this
                        .findAttachedContainerPosition(childPosition);
                if (parentPosition >= 0 && parentPosition != childPosition) {
                    boolean parentExpanded = RoomContentAdapter.this.isContainerExpanded(parentPosition)
                            && RoomContentAdapter.this.items.get(parentPosition) != null
                            && RoomContentAdapter.this.items.get(parentPosition).hasAttachedItems();
                    if (parentExpanded) {
                        joinsParentFrame = parentPosition == containerPosition;
                        isLastChildInParent = RoomContentAdapter.this
                                .isLastDirectChild(parentPosition, childPosition);
                    }
                }
                if (cardBackground != null) {
                    RoomContentAdapter.this.applyContainerBackground(cardBackground, style,
                            hasAttachments, isExpanded, joinsParentFrame, isLastChildInParent);
                }
                if (bannerContainerView != null) {
                    RoomContentAdapter.this.applyContainerBannerColor(bannerContainerView, style,
                            child.getType());
                }
            } else {
                if (cardBackground != null) {
                    RoomContentAdapter.this.applyStandaloneContentBackground(cardBackground, style);
                }
                if (bannerContainerView != null) {
                    RoomContentAdapter.this.applyBannerColor(bannerContainerView, child.getType());
                }
            }
            if (bannerContainerView != null) {
                bannerContainerView.setOnClickListener(view -> notifyEdit(child, childPosition));
            }

            boolean isFurnitureChild = child.isFurniture();
            boolean isContainerChild = child.isContainer();

            if (photoIcon != null) {
                if (isFurnitureChild) {
                    photoIcon.setVisibility(View.GONE);
                    photoIcon.setOnClickListener(null);
                } else {
                    photoIcon.setVisibility(View.VISIBLE);
                    bindPopupChildPhoto(photoIcon, child);
                    CharSequence descriptionName = resolvedDisplayName != null
                            ? resolvedDisplayName
                            : child.getName();
                    if (descriptionName == null || descriptionName.length() == 0) {
                        descriptionName = child.getName();
                    }
                    if (descriptionName != null && descriptionName.length() > 0) {
                        Context context = photoIcon.getContext();
                        photoIcon.setContentDescription(context.getString(
                                R.string.content_description_room_content_photos,
                                descriptionName));
                    } else {
                        photoIcon.setContentDescription(null);
                    }
                    photoIcon.setOnClickListener(view -> notifyEdit(child, childPosition));
                }
            }

            if (menuIcon != null) {
                boolean showMenu = !isFurnitureChild && !isContainerChild;
                if (showMenu) {
                    menuIcon.setVisibility(View.VISIBLE);
                    menuIcon.setOnClickListener(view ->
                            toggleOptionsMenu(menuIcon, child, childPosition));
                } else {
                    menuIcon.setVisibility(View.GONE);
                    menuIcon.setOnClickListener(null);
                }
            }

            entryView.setClickable(false);
            entryView.setFocusable(false);
        }

        private void bindPopupChildPhoto(@NonNull ImageView target,
                @NonNull RoomContentItem item) {
            List<String> photos = item.getPhotos();
            if (!photos.isEmpty()) {
                Bitmap bitmap = decodePhoto(photos.get(0));
                if (bitmap != null) {
                    target.setImageBitmap(bitmap);
                    target.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    target.setPadding(0, 0, 0, 0);
                    return;
                }
            }
            target.setImageResource(R.drawable.ic_establishment_photos);
            target.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            int defaultPadding = (int) (target.getResources().getDisplayMetrics().density * 8f);
            target.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding);
        }

        void dismissContainerPopup() {
            if (containerPopup != null) {
                containerPopup.dismiss();
                containerPopup = null;
            }
        }

        private void toggleFurniturePopup() {
            if (currentItem == null) {
                return;
            }
            dismissFurniturePhotoMenu();
            if (furniturePopup != null && furniturePopup.isShowing()) {
                furniturePopup.dismiss();
                return;
            }
            LayoutInflater layoutInflater = RoomContentAdapter.this.inflater;
            View popupView = layoutInflater.inflate(R.layout.popup_furniture_details, null);
            TextView nameTextView = popupView.findViewById(R.id.text_furniture_popup_name);
            if (nameTextView != null) {
                nameTextView.setText(currentItem.getName());
            }
            ImageView photosIcon = popupView.findViewById(R.id.icon_furniture_popup_photos);
            if (photosIcon != null) {
                photosIcon.setOnClickListener(view -> showFurniturePhotoMenu(photosIcon));
            }
            LinearLayout columnsContainer = popupView.findViewById(R.id.container_furniture_columns);
            if (columnsContainer != null) {
                populateFurniturePopupColumns(columnsContainer, currentItem);
            }
            LinearLayout sectionsContainer = popupView.findViewById(R.id.container_furniture_sections);
            if (sectionsContainer != null) {
                populateFurnitureSections(sectionsContainer, currentItem);
            }
            DisplayMetrics displayMetrics = popupView.getResources().getDisplayMetrics();
            int popupWidth = (int) (displayMetrics.widthPixels * 0.9f);
            int popupHeight = (int) (displayMetrics.heightPixels * 0.9f);
            PopupWindow popupWindow = new PopupWindow(popupView, popupWidth, popupHeight, true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            float elevation = itemView.getResources().getDisplayMetrics().density * 6f;
            popupWindow.setElevation(elevation);
            popupWindow.setOnDismissListener(() -> {
                furniturePopup = null;
                dismissFurniturePhotoMenu();
            });
            furniturePopup = popupWindow;
            dismissOptionsMenu();
            popupWindow.showAtLocation(itemView, Gravity.CENTER, 0, 0);
        }

        private void showFurniturePhotoMenu(@NonNull View anchor) {
            if (currentItem == null) {
                return;
            }
            if (furniturePhotoMenuPopup != null && furniturePhotoMenuPopup.isShowing()) {
                furniturePhotoMenuPopup.dismiss();
                return;
            }
            LayoutInflater layoutInflater = RoomContentAdapter.this.inflater;
            View popupView = layoutInflater.inflate(R.layout.popup_furniture_photos_menu, null);
            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(() -> furniturePhotoMenuPopup = null);
            furniturePhotoMenuPopup = popupWindow;

            View manageButton = popupView.findViewById(R.id.button_furniture_photos_manage);
            if (manageButton != null) {
                manageButton.setOnClickListener(view -> {
                    popupWindow.dismiss();
                    notifyEdit();
                });
            }

            View viewButton = popupView.findViewById(R.id.button_furniture_photos_view);
            boolean hasPhotos = currentItem.getPhotos() != null && !currentItem.getPhotos().isEmpty();
            if (viewButton != null) {
                if (hasPhotos) {
                    viewButton.setOnClickListener(view -> {
                        popupWindow.dismiss();
                        showFurniturePhotoPreview(0);
                    });
                } else {
                    viewButton.setOnClickListener(view -> {
                        popupWindow.dismiss();
                        Toast.makeText(anchor.getContext(),
                                R.string.furniture_popup_photos_empty,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }

            popupView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int popupHeight = popupView.getMeasuredHeight();
            int verticalOffset = (int) (anchor.getResources().getDisplayMetrics().density * 8);

            Rect displayFrame = new Rect();
            anchor.getWindowVisibleDisplayFrame(displayFrame);
            int[] location = new int[2];
            anchor.getLocationOnScreen(location);
            int anchorBottom = location[1] + anchor.getHeight();
            int spaceBelow = displayFrame.bottom - anchorBottom;
            int spaceAbove = location[1] - displayFrame.top;

            int yOffset = verticalOffset;
            if (spaceBelow < popupHeight + verticalOffset && spaceAbove >= popupHeight + verticalOffset) {
                yOffset = -(anchor.getHeight() + popupHeight + verticalOffset);
            }

            PopupWindowCompat.showAsDropDown(popupWindow, anchor, 0, yOffset, Gravity.END);
        }

        private void showFurniturePhotoPreview(int startIndex) {
            if (currentItem == null) {
                return;
            }
            List<String> photos = currentItem.getPhotos();
            if (photos.isEmpty()) {
                Toast.makeText(itemView.getContext(), R.string.furniture_popup_photos_empty,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            LayoutInflater layoutInflater = RoomContentAdapter.this.inflater;
            View previewView = layoutInflater.inflate(R.layout.dialog_photo_preview, null);
            ImageView previewImage = previewView.findViewById(R.id.image_photo_preview);
            ImageButton previousButton = previewView.findViewById(R.id.button_previous_photo);
            ImageButton nextButton = previewView.findViewById(R.id.button_next_photo);
            ImageButton deleteButton = previewView.findViewById(R.id.button_delete_photo);
            if (deleteButton != null) {
                deleteButton.setVisibility(View.GONE);
            }
            AlertDialog previewDialog = new AlertDialog.Builder(itemView.getContext())
                    .setView(previewView)
                    .create();
            final int[] currentIndex = {Math.max(0, Math.min(startIndex, photos.size() - 1))};
            Runnable updateImage = () -> {
                Bitmap bitmap = decodePhoto(photos.get(currentIndex[0]));
                if (bitmap != null) {
                    previewImage.setImageBitmap(bitmap);
                } else {
                    previewImage.setImageDrawable(null);
                }
                boolean hasMultiple = photos.size() > 1;
                if (previousButton != null) {
                    previousButton.setEnabled(hasMultiple);
                }
                if (nextButton != null) {
                    nextButton.setEnabled(hasMultiple);
                }
            };
            if (previousButton != null) {
                previousButton.setOnClickListener(view -> {
                    if (photos.size() <= 1) {
                        return;
                    }
                    currentIndex[0] = (currentIndex[0] - 1 + photos.size()) % photos.size();
                    updateImage.run();
                });
            }
            if (nextButton != null) {
                nextButton.setOnClickListener(view -> {
                    if (photos.size() <= 1) {
                        return;
                    }
                    currentIndex[0] = (currentIndex[0] + 1) % photos.size();
                    updateImage.run();
                });
            }
            updateImage.run();
            previewDialog.show();
        }

        private void populateFurniturePopupColumns(@NonNull LinearLayout container,
                @NonNull RoomContentItem item) {
            container.removeAllViews();
            Context context = container.getContext();
            LayoutInflater layoutInflater = RoomContentAdapter.this.inflater;
            container.setVisibility(View.VISIBLE);
            int requestedColumns = item.getFurnitureColumns() != null
                    ? item.getFurnitureColumns()
                    : 1;
            int columnCount = Math.max(1, requestedColumns);
            String columnPrefix = resolveColumnPrefix(context);
            int spacing = context.getResources()
                    .getDimensionPixelSize(R.dimen.furniture_popup_column_chip_spacing);
            for (int index = 1; index <= Math.min(columnCount, MAX_VISIBLE_FURNITURE_COLUMNS); index++) {
                TextView chip = (TextView) layoutInflater.inflate(
                        R.layout.item_furniture_column_chip, container, false);
                chip.setText(columnPrefix + index);
                chip.setContentDescription(context.getString(
                        R.string.furniture_popup_columns_title) + " " + index);
                boolean isSelected = index == 1;
                boolean hasMoreColumns = columnCount > MAX_VISIBLE_FURNITURE_COLUMNS;
                if (hasMoreColumns && index == MAX_VISIBLE_FURNITURE_COLUMNS) {
                    applyDropdownIndicator(chip);
                }
                updateColumnChipBackground(chip, isSelected);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                if (index > 1) {
                    params.leftMargin = spacing;
                }
                chip.setLayoutParams(params);
                container.addView(chip);
            }
        }

        private void applyDropdownIndicator(@NonNull TextView chip) {
            Context context = chip.getContext();
            Drawable arrow = AppCompatResources.getDrawable(context, R.drawable.ic_chevron_down);
            if (arrow == null) {
                return;
            }
            Drawable wrappedArrow = DrawableCompat.wrap(arrow.mutate());
            DrawableCompat.setTint(wrappedArrow, ContextCompat.getColor(context, R.color.text_dark));
            chip.setCompoundDrawablesWithIntrinsicBounds(null, null, wrappedArrow, null);
            int padding = context.getResources()
                    .getDimensionPixelSize(R.dimen.furniture_popup_column_drawable_padding);
            chip.setCompoundDrawablePadding(padding);
        }

        @NonNull
        private String resolveColumnPrefix(@NonNull Context context) {
            String abbreviation = context.getString(R.string.furniture_popup_column_short);
            String candidate = abbreviation != null ? abbreviation.trim() : "";
            if (candidate.isEmpty()) {
                String fallback = context.getString(R.string.furniture_popup_columns_title);
                candidate = fallback != null ? fallback.trim() : "";
            }
            if (candidate.isEmpty()) {
                return "C";
            }
            int codePoint = candidate.codePointAt(0);
            return new String(Character.toChars(codePoint));
        }

        private void updateColumnChipBackground(@NonNull TextView chip, boolean selected) {
            chip.setBackgroundResource(selected
                    ? R.drawable.bg_furniture_column_chip_selected
                    : R.drawable.bg_furniture_column_chip);
        }

        @Nullable
        private String formatFurniturePlacement(@NonNull RoomContentItem item) {
            Integer level = item.getContainerLevel();
            Integer column = item.getContainerColumn();
            if (level == null && column == null) {
                return null;
            }
            Context context = itemView.getContext();
            List<String> parts = new ArrayList<>();
            if (level != null) {
                parts.add(context.getString(R.string.room_content_furniture_level_short, level));
            }
            if (column != null) {
                parts.add(context.getString(R.string.room_content_furniture_column_short, column));
            }
            if (parts.isEmpty()) {
                return null;
            }
            return TextUtils.join(" · ", parts);
        }

        private void populateFurnitureSections(@NonNull LinearLayout container,
                @NonNull RoomContentItem item) {
            container.removeAllViews();
            Context context = container.getContext();
            LayoutInflater layoutInflater = RoomContentAdapter.this.inflater;
            boolean hasTop = item.hasFurnitureTop();
            Integer furnitureLevels = item.getFurnitureLevels();
            int levelCount = furnitureLevels != null && furnitureLevels > 0 ? furnitureLevels : 0;
            boolean hasBottom = item.hasFurnitureBottom();
            List<RoomContentItem> children = item.getChildren();
            if (!hasTop && levelCount <= 0 && !hasBottom) {
                container.setVisibility(View.GONE);
                return;
            }
            container.setVisibility(View.VISIBLE);
            if (hasTop) {
                View section = createFurnitureSection(layoutInflater, container,
                        context.getString(R.string.furniture_popup_top_title), false,
                        Collections.emptyList());
                container.addView(section);
            }
            for (int index = 1; index <= levelCount; index++) {
                String title = context.getString(R.string.furniture_popup_level_title, index);
                List<RoomContentItem> levelItems = collectFurnitureItemsForLevel(children, index);
                View section = createFurnitureSection(layoutInflater, container, title, true,
                        levelItems);
                container.addView(section);
            }
            if (hasBottom) {
                View section = createFurnitureSection(layoutInflater, container,
                        context.getString(R.string.furniture_popup_bottom_title), false,
                        Collections.emptyList());
                container.addView(section);
            }
        }

        @NonNull
        private View createFurnitureSection(@NonNull LayoutInflater layoutInflater,
                @NonNull LinearLayout parent,
                @NonNull String title,
                boolean isLevel,
                @NonNull List<RoomContentItem> levelItems) {
            View section = layoutInflater.inflate(R.layout.item_furniture_section, parent, false);
            TextView titleView = section.findViewById(R.id.text_section_title);
            if (titleView != null) {
                titleView.setText(title);
            }
            section.setBackgroundResource(isLevel
                    ? R.drawable.bg_furniture_section_level
                    : R.drawable.bg_furniture_section_top);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            if (parent.getChildCount() > 0) {
                int spacing = parent.getResources()
                        .getDimensionPixelSize(R.dimen.furniture_popup_section_spacing);
                params.topMargin = spacing;
            }
            section.setLayoutParams(params);
            View headerView = section.findViewById(R.id.container_section_header);
            ImageView chevronView = section.findViewById(R.id.icon_section_chevron);
            LinearLayout contentContainer = section.findViewById(R.id.container_section_content);
            View indicatorView = section.findViewById(R.id.view_section_indicator);
            if (isLevel) {
                populateFurnitureSectionContent(contentContainer, levelItems);
                boolean hasContent = !levelItems.isEmpty();
                if (indicatorView != null) {
                    indicatorView.setVisibility(hasContent ? View.VISIBLE : View.GONE);
                }
                if (chevronView != null) {
                    chevronView.setVisibility(View.VISIBLE);
                    chevronView.setRotation(0f);
                }
                if (headerView != null && chevronView != null && contentContainer != null) {
                    setupSectionToggle(headerView, contentContainer, chevronView);
                } else if (headerView != null) {
                    headerView.setOnClickListener(null);
                }
            } else {
                if (chevronView != null) {
                    chevronView.setVisibility(View.GONE);
                    chevronView.setRotation(0f);
                }
                if (contentContainer != null) {
                    contentContainer.setVisibility(View.GONE);
                    contentContainer.removeAllViews();
                }
                if (indicatorView != null) {
                    indicatorView.setVisibility(View.GONE);
                }
                if (headerView != null) {
                    headerView.setOnClickListener(null);
                }
            }
            return section;
        }

        @NonNull
        private List<RoomContentItem> collectFurnitureItemsForLevel(
                @Nullable List<RoomContentItem> children,
                int desiredLevel) {
            if (children == null || children.isEmpty()) {
                return Collections.emptyList();
            }
            List<RoomContentItem> matches = new ArrayList<>();
            for (RoomContentItem child : children) {
                if (child == null) {
                    continue;
                }
                Integer childLevel = child.getContainerLevel();
                if (childLevel != null && childLevel == desiredLevel) {
                    matches.add(child);
                }
            }
            if (matches.isEmpty()) {
                return Collections.emptyList();
            }
            Collections.sort(matches, (left, right) -> {
                int columnComparison = compareNullableIntegers(left.getContainerColumn(),
                        right.getContainerColumn());
                if (columnComparison != 0) {
                    return columnComparison;
                }
                String leftName = RoomContentAdapter.this.resolveItemName(left);
                String rightName = RoomContentAdapter.this.resolveItemName(right);
                return leftName.compareToIgnoreCase(rightName);
            });
            return matches;
        }

        private void populateFurnitureSectionContent(@Nullable LinearLayout container,
                @NonNull List<RoomContentItem> items) {
            if (container == null) {
                return;
            }
            container.removeAllViews();
            LayoutInflater layoutInflater = RoomContentAdapter.this.inflater;
            if (items.isEmpty()) {
                View emptyView = layoutInflater.inflate(R.layout.item_furniture_section_empty,
                        container, false);
                container.addView(emptyView);
                return;
            }
            for (RoomContentItem child : items) {
                if (child == null) {
                    continue;
                }
                View entryView = layoutInflater.inflate(R.layout.item_room_content_child,
                        container, false);
                TextView nameView = entryView.findViewById(R.id.text_room_content_child_name);
                TextView commentView = entryView.findViewById(R.id.text_room_content_child_comment);
                if (nameView != null) {
                    String name = RoomContentAdapter.this.resolveItemName(child);
                    Integer column = child.getContainerColumn();
                    if (column != null) {
                        Context context = entryView.getContext();
                        String columnLabel = context.getString(
                                R.string.room_content_furniture_column_short, column);
                        nameView.setText(columnLabel + " · " + name);
                    } else {
                        nameView.setText(name);
                    }
                }
                if (commentView != null) {
                    String comment = child.getComment();
                    String trimmed = comment != null ? comment.trim() : "";
                    if (!trimmed.isEmpty()) {
                        commentView.setVisibility(View.VISIBLE);
                        commentView.setText(trimmed);
                    } else {
                        commentView.setVisibility(View.GONE);
                        commentView.setText(null);
                    }
                }
                container.addView(entryView);
            }
        }

        private void setupSectionToggle(@NonNull View headerView,
                @NonNull LinearLayout contentContainer,
                @NonNull ImageView chevronView) {
            headerView.setClickable(true);
            headerView.setFocusable(true);
            contentContainer.setVisibility(View.GONE);
            chevronView.setRotation(0f);
            headerView.setOnClickListener(view -> {
                boolean expanded = contentContainer.getVisibility() == View.VISIBLE;
                chevronView.animate().cancel();
                if (expanded) {
                    contentContainer.setVisibility(View.GONE);
                    chevronView.animate().rotation(0f).setDuration(150).start();
                } else {
                    contentContainer.setVisibility(View.VISIBLE);
                    chevronView.animate().rotation(-180f).setDuration(150).start();
                }
            });
        }

        private int compareNullableIntegers(@Nullable Integer left, @Nullable Integer right) {
            if (left == null && right == null) {
                return 0;
            }
            if (left == null) {
                return 1;
            }
            if (right == null) {
                return -1;
            }
            return Integer.compare(left, right);
        }

        private void dismissFurniturePhotoMenu() {
            if (furniturePhotoMenuPopup != null) {
                furniturePhotoMenuPopup.dismiss();
                furniturePhotoMenuPopup = null;
            }
        }

        void dismissFurniturePopup() {
            if (furniturePopup != null) {
                furniturePopup.dismiss();
                furniturePopup = null;
            }
            dismissFurniturePhotoMenu();
        }

        private void notifyEdit() {
            notifyEdit(currentItem, getBindingAdapterPosition());
        }

        private void notifyEdit(@Nullable RoomContentItem targetItem, int targetPosition) {
            if (interactionListener == null || targetItem == null) {
                return;
            }
            int resolvedPosition = resolveAdapterPosition(targetItem, targetPosition);
            if (resolvedPosition == RecyclerView.NO_POSITION) {
                dismissOptionsMenu();
                dismissFurniturePopup();
                dismissContainerPopup();
                return;
            }
            RoomContentAdapter.this.preparePendingContainerPopupRestore(resolvedPosition);
            dismissOptionsMenu();
            dismissFurniturePopup();
            dismissContainerPopup();
            interactionListener.onEditRoomContent(targetItem, resolvedPosition);
        }

        private void notifyDelete() {
            notifyDelete(currentItem, getBindingAdapterPosition());
        }

        private void notifyDelete(@Nullable RoomContentItem targetItem, int targetPosition) {
            if (interactionListener == null || targetItem == null) {
                return;
            }
            dismissOptionsMenu();
            dismissFurniturePopup();
            dismissContainerPopup();
            int resolvedPosition = resolveAdapterPosition(targetItem, targetPosition);
            if (resolvedPosition == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onDeleteRoomContent(targetItem, resolvedPosition);
        }

        private void notifyCopy() {
            notifyCopy(currentItem, getBindingAdapterPosition());
        }

        private void notifyCopy(@Nullable RoomContentItem targetItem, int targetPosition) {
            if (interactionListener == null || targetItem == null) {
                return;
            }
            dismissOptionsMenu();
            dismissFurniturePopup();
            dismissContainerPopup();
            int resolvedPosition = resolveAdapterPosition(targetItem, targetPosition);
            if (resolvedPosition == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onCopyRoomContent(targetItem, resolvedPosition);
        }

        private void notifyMove() {
            notifyMove(currentItem, getBindingAdapterPosition());
        }

        private void notifyMove(@Nullable RoomContentItem targetItem, int targetPosition) {
            if (interactionListener == null || targetItem == null) {
                return;
            }
            dismissOptionsMenu();
            dismissFurniturePopup();
            dismissContainerPopup();
            int resolvedPosition = resolveAdapterPosition(targetItem, targetPosition);
            if (resolvedPosition == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onMoveRoomContent(targetItem, resolvedPosition);
        }

        private int resolveAdapterPosition(@Nullable RoomContentItem targetItem, int providedPosition) {
            if (providedPosition != RecyclerView.NO_POSITION) {
                return providedPosition;
            }
            if (targetItem == null) {
                return RecyclerView.NO_POSITION;
            }
            int index = RoomContentAdapter.this.items.indexOf(targetItem);
            return index >= 0 ? index : RecyclerView.NO_POSITION;
        }

        private void toggleExpansion() {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            dismissFurniturePopup();
            if (currentItem != null && currentItem.isContainer()
                    && currentItem.hasAttachedItems()) {
                boolean isExpanded = RoomContentAdapter.this.isContainerExpanded(position);
                RoomContentAdapter.this.setContainerExpanded(position, !isExpanded);
                notifyItemChanged(position);
                RoomContentAdapter.this.notifyAttachedItemsChanged(position);
            } else {
                boolean isExpanded = expandedStates.get(position, false);
                if (isExpanded) {
                    expandedStates.delete(position);
                } else {
                    expandedStates.put(position, true);
                }
                notifyItemChanged(position);
            }
        }

        private void onFilterChipToggled(@NonNull Chip chip, int visibilityFlag) {
            if (suppressFilterCallbacks) {
                return;
            }
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            boolean checked = chip.isChecked();
            int visibilityMask = RoomContentAdapter.this.resolvePopupVisibilityMask(position);
            if (checked) {
                visibilityMask |= visibilityFlag;
            } else {
                visibilityMask &= ~visibilityFlag;
            }
            RoomContentAdapter.this.setContainerVisibilityMask(position, visibilityMask);
            notifyItemChanged(position);
            RoomContentAdapter.this.notifyAttachedItemsChanged(position);
        }

        private int resolveBottomMargin(@NonNull RoomContentItem item, int position,
                boolean isContainerExpanded) {
            int margin = defaultMarginBottom;
            int nextVisiblePosition = findNextVisiblePosition(position);
            if (item.isContainer()) {
                if (isContainerExpanded && item.hasAttachedItems()) {
                    margin = 0;
                }
            } else {
                RoomContentItem container = RoomContentAdapter.this.findAttachedContainer(position);
                if (container != null) {
                    int containerPosition = RoomContentAdapter.this
                            .findAttachedContainerPosition(position);
                    if (containerPosition >= 0) {
                        boolean containerExpanded = RoomContentAdapter.this
                                .isContainerExpanded(containerPosition)
                                && container.hasAttachedItems();
                        if (containerExpanded) {
                            boolean isLastAttachment = RoomContentAdapter.this
                                    .isLastDirectChild(containerPosition, position);
                            margin = isLastAttachment ? defaultMarginBottom : 0;
                        }
                    }
                }
            }
            if (margin != 0 && nextVisiblePosition >= 0
                    && sharesTopLevelGroupWith(position, nextVisiblePosition)) {
                margin = 0;
            }
            return margin;
        }

        private int findNextVisiblePosition(int position) {
            int size = RoomContentAdapter.this.items.size();
            for (int index = position + 1; index < size; index++) {
                if (RoomContentAdapter.this.isItemHiddenByCollapsedContainer(index)) {
                    continue;
                }
                RoomContentItem candidate = RoomContentAdapter.this.items.get(index);
                if (!candidate.isContainer() && !candidate.isDisplayed()) {
                    continue;
                }
                return index;
            }
            return -1;
        }

        private boolean sharesTopLevelGroupWith(int position, int otherPosition) {
            int anchor = findTopLevelAncestorPosition(position);
            int otherAnchor = findTopLevelAncestorPosition(otherPosition);
            return anchor >= 0 && anchor == otherAnchor;
        }

        private int findTopLevelAncestorPosition(int position) {
            if (position < 0 || position >= RoomContentAdapter.this.items.size()) {
                return -1;
            }
            int current = position;
            int parent = RoomContentAdapter.this.findAttachedContainerPosition(current);
            int safety = 0;
            while (parent >= 0 && parent < RoomContentAdapter.this.items.size()) {
                current = parent;
                int nextParent = RoomContentAdapter.this.findAttachedContainerPosition(current);
                if (nextParent == parent) {
                    break;
                }
                parent = nextParent;
                safety++;
                if (safety >= RoomContentAdapter.this.items.size()) {
                    break;
                }
            }
            return current;
        }

        private void toggleOptionsMenu(@NonNull View anchor) {
            int position = getBindingAdapterPosition();
            toggleOptionsMenu(anchor, currentItem, position);
        }

        private void toggleOptionsMenu(@NonNull View anchor,
                @Nullable RoomContentItem targetItem, int targetPosition) {
            if (optionsPopup != null && optionsPopup.isShowing()) {
                optionsPopup.dismiss();
                return;
            }
            dismissFurniturePopup();
            View popupView = RoomContentAdapter.this.inflater
                    .inflate(R.layout.popup_room_content_menu, null);
            PopupWindow popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            View copyButton = popupView.findViewById(R.id.button_popup_room_content_copy);
            if (copyButton != null) {
                if (targetItem != null) {
                    copyButton.setOnClickListener(view -> {
                        popupWindow.dismiss();
                        notifyCopy(targetItem, targetPosition);
                    });
                    copyButton.setEnabled(true);
                } else {
                    copyButton.setOnClickListener(null);
                    copyButton.setEnabled(false);
                }
            }
            View editButton = popupView.findViewById(R.id.button_popup_room_content_edit);
            if (editButton != null) {
                boolean showEdit = targetItem != null && targetItem.isContainer();
                editButton.setVisibility(showEdit ? View.VISIBLE : View.GONE);
                if (showEdit) {
                    editButton.setOnClickListener(view -> {
                        popupWindow.dismiss();
                        notifyEdit(targetItem, targetPosition);
                    });
                } else {
                    editButton.setOnClickListener(null);
                }
            }
            View moveButton = popupView.findViewById(R.id.button_popup_room_content_move);
            if (moveButton != null) {
                if (targetItem != null) {
                    moveButton.setOnClickListener(view -> {
                        popupWindow.dismiss();
                        notifyMove(targetItem, targetPosition);
                    });
                    moveButton.setEnabled(true);
                } else {
                    moveButton.setOnClickListener(null);
                    moveButton.setEnabled(false);
                }
            }
            View deleteButton = popupView.findViewById(R.id.button_popup_room_content_delete);
            if (deleteButton != null) {
                if (targetItem != null) {
                    deleteButton.setOnClickListener(view -> {
                        popupWindow.dismiss();
                        notifyDelete(targetItem, targetPosition);
                    });
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setOnClickListener(null);
                    deleteButton.setEnabled(false);
                }
            }
            popupWindow.setOnDismissListener(() -> optionsPopup = null);
            optionsPopup = popupWindow;

            popupView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int popupHeight = popupView.getMeasuredHeight();
            int verticalOffset = (int) (itemView.getResources().getDisplayMetrics().density * 8);

            Rect displayFrame = new Rect();
            anchor.getWindowVisibleDisplayFrame(displayFrame);
            int[] location = new int[2];
            anchor.getLocationOnScreen(location);
            int anchorBottom = location[1] + anchor.getHeight();
            int spaceBelow = displayFrame.bottom - anchorBottom;
            int spaceAbove = location[1] - displayFrame.top;

            int yOffset = verticalOffset;
            if (spaceBelow < popupHeight + verticalOffset
                    && spaceAbove >= popupHeight + verticalOffset) {
                yOffset = -(anchor.getHeight() + popupHeight + verticalOffset);
            }

            PopupWindowCompat.showAsDropDown(popupWindow, anchor, 0, yOffset, Gravity.END);
        }

        void dismissOptionsMenu() {
            if (optionsPopup != null) {
                optionsPopup.dismiss();
                optionsPopup = null;
            }
        }
    }

    private final class GroupDecoration extends RecyclerView.ItemDecoration {

        private final Rect parentBounds = new Rect();
        private final Rect drawingBounds = new Rect();
        private final int strokeWidthPx;
        private final int halfStrokeWidthPx;
        private final float cornerRadiusPx;

        GroupDecoration(@NonNull Context context) {
            this.strokeWidthPx = context.getResources()
                    .getDimensionPixelSize(R.dimen.room_content_group_stroke_width);
            this.halfStrokeWidthPx = Math.max(1, (int) Math.ceil(strokeWidthPx / 2f));
            this.cornerRadiusPx = cardCornerRadiusPx;
        }

        @Override
        public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent,
                @NonNull RecyclerView.State state) {
            ensureHierarchyComputed();
            int childCount = parent.getChildCount();
            if (childCount <= 0) {
                return;
            }
            parentBounds.set(parent.getPaddingLeft(), parent.getPaddingTop(),
                    parent.getWidth() - parent.getPaddingRight(),
                    parent.getHeight() - parent.getPaddingBottom());
            SparseIntArray groups = new SparseIntArray();
            for (int index = 0; index < childCount; index++) {
                View child = parent.getChildAt(index);
                if (child == null || child.getVisibility() != View.VISIBLE) {
                    continue;
                }
                int position = parent.getChildAdapterPosition(child);
                if (position == RecyclerView.NO_POSITION) {
                    continue;
                }
                collectDecoratedGroupsForPosition(position, groups);
            }
            for (int i = 0; i < groups.size(); i++) {
                int groupStart = groups.keyAt(i);
                int groupEnd = groups.valueAt(i);
                computeGroupBounds(parent, groupStart, groupEnd, drawingBounds);
                if (drawingBounds.isEmpty()) {
                    continue;
                }
                expandForStroke(drawingBounds);
                clipToParent(drawingBounds);
                if (drawingBounds.isEmpty()) {
                    continue;
                }
                HierarchyStyle style = resolveHierarchyStyle(computeHierarchyDepth(groupStart));
                GradientDrawable frame = buildGroupDrawable(style);
                frame.setBounds(drawingBounds);
                frame.draw(canvas);
            }
        }

        private void collectDecoratedGroupsForPosition(int position,
                @NonNull SparseIntArray groups) {
            if (position < 0 || position >= items.size()) {
                return;
            }
            int currentPosition = position;
            int safety = 0;
            while (currentPosition >= 0 && currentPosition < items.size()
                    && safety < items.size()) {
                RoomContentItem currentItem = items.get(currentPosition);
                if (currentItem == null) {
                    break;
                }
                int containerPosition = currentItem.isContainer()
                        ? currentPosition
                        : findAttachedContainerPosition(currentPosition);
                if (containerPosition < 0 || containerPosition >= items.size()) {
                    break;
                }
                if (shouldDecorateContainer(containerPosition)
                        && groups.indexOfKey(containerPosition) < 0) {
                    int groupEnd = findLastDisplayedDirectChildPosition(containerPosition);
                    if (groupEnd > containerPosition) {
                        groups.put(containerPosition, groupEnd);
                    }
                }
                int parentPosition = findAttachedContainerPosition(containerPosition);
                if (parentPosition < 0 || parentPosition == currentPosition) {
                    break;
                }
                currentPosition = parentPosition;
                safety++;
            }
        }

        private boolean shouldDecorateContainer(int containerPosition) {
            RoomContentItem container = getItemAt(containerPosition);
            return container != null
                    && container.isContainer()
                    && container.hasAttachedItems()
                    && isContainerExpanded(containerPosition)
                    && hasVisibleDirectChildren(containerPosition);
        }

        private int findLastDisplayedDirectChildPosition(int containerPosition) {
            if (containerPosition < 0 || containerPosition >= items.size()) {
                return containerPosition;
            }
            int last = containerPosition;
            for (int index = containerPosition + 1; index < items.size(); index++) {
                if (!isDescendantOf(containerPosition, index)) {
                    break;
                }
                if (findAttachedContainerPosition(index) != containerPosition) {
                    continue;
                }
                RoomContentItem child = items.get(index);
                if (child != null && child.isDisplayed()) {
                    last = index;
                }
            }
            return last;
        }

        private void computeGroupBounds(@NonNull RecyclerView parent, int groupStart,
                int groupEnd, @NonNull Rect outRect) {
            int left = Integer.MAX_VALUE;
            int top = Integer.MAX_VALUE;
            int right = Integer.MIN_VALUE;
            int bottom = Integer.MIN_VALUE;
            int childCount = parent.getChildCount();
            for (int index = 0; index < childCount; index++) {
                View child = parent.getChildAt(index);
                if (child == null || child.getVisibility() != View.VISIBLE) {
                    continue;
                }
                int position = parent.getChildAdapterPosition(child);
                if (position == RecyclerView.NO_POSITION) {
                    continue;
                }
                boolean include = position == groupStart;
                if (!include && position > groupStart && position <= groupEnd) {
                    include = findAttachedContainerPosition(position) == groupStart;
                }
                if (!include) {
                    continue;
                }
                View wrapper = child.findViewById(R.id.groupFullWrapper);
                if (wrapper == null || wrapper.getVisibility() != View.VISIBLE) {
                    continue;
                }
                int childLeft = child.getLeft() + Math.round(child.getTranslationX());
                int childTop = child.getTop() + Math.round(child.getTranslationY());
                int wrapperLeft = childLeft + wrapper.getLeft();
                int wrapperTop = childTop + wrapper.getTop();
                int wrapperRight = wrapperLeft + wrapper.getWidth();
                int wrapperBottom = wrapperTop + wrapper.getHeight();
                if (wrapperLeft < left) {
                    left = wrapperLeft;
                }
                if (wrapperTop < top) {
                    top = wrapperTop;
                }
                if (wrapperRight > right) {
                    right = wrapperRight;
                }
                if (wrapperBottom > bottom) {
                    bottom = wrapperBottom;
                }
            }
            if (left >= right || top >= bottom) {
                outRect.setEmpty();
            } else {
                outRect.set(left, top, right, bottom);
            }
        }

        private void expandForStroke(@NonNull Rect rect) {
            rect.left -= halfStrokeWidthPx;
            rect.top -= halfStrokeWidthPx;
            rect.right += halfStrokeWidthPx;
            rect.bottom += halfStrokeWidthPx;
        }

        private void clipToParent(@NonNull Rect rect) {
            if (rect.isEmpty()) {
                return;
            }
            if (rect.left < parentBounds.left) {
                rect.left = parentBounds.left;
            }
            if (rect.top < parentBounds.top) {
                rect.top = parentBounds.top;
            }
            if (rect.right > parentBounds.right) {
                rect.right = parentBounds.right;
            }
            if (rect.bottom > parentBounds.bottom) {
                rect.bottom = parentBounds.bottom;
            }
            if (rect.left >= rect.right || rect.top >= rect.bottom) {
                rect.setEmpty();
            }
        }

        @NonNull
        private GradientDrawable buildGroupDrawable(@NonNull HierarchyStyle style) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(style.backgroundColor);
            drawable.setCornerRadius(cornerRadiusPx);
            drawable.setStroke(strokeWidthPx, style.accentColor);
            return drawable;
        }
    }
}
