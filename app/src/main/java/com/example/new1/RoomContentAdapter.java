package com.example.new1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.PopupWindowCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RoomContentAdapter extends RecyclerView.Adapter<RoomContentAdapter.ViewHolder> {

    private static final float LABEL_RELATIVE_SIZE = 0.85f;

    private final List<RoomContentItem> items;
    private final LayoutInflater inflater;

    private final Context context;
    private final int labelColor;
    @Nullable
    private final OnRoomContentInteractionListener interactionListener;
    private final SparseBooleanArray expandedStates = new SparseBooleanArray();
    private final SparseBooleanArray containerCollapsedStates = new SparseBooleanArray();

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

        CharSequence commentText = formatComment(item.getComment());
        boolean hasComment = commentText != null;

        List<CharSequence> metadataLines = new ArrayList<>();
        if (item.isContainer()) {
            addAttachmentSummaryLine(metadataLines, item.getAttachedItemCount());
        }
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
        CharSequence metadataText = formatMetadataLines(metadataLines);
        boolean hasMetadata = metadataText != null;

        boolean isContainer = item.isContainer();
        boolean canToggleContainer = isContainer && item.hasAttachedItems();
        if (canToggleContainer) {
            expandedStates.delete(position);
        }
        if (!hasComment && !hasMetadata) {
            expandedStates.delete(position);
        }
        boolean isContainerExpanded = !containerCollapsedStates.get(position, false);
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
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.dismissOptionsMenu();
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

    private void addAttachmentSummaryLine(@NonNull List<CharSequence> metadataLines, int count) {
        if (count <= 0) {
            return;
        }
        metadataLines.add(createAttachmentSummaryLine(count));
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

    @NonNull
    private CharSequence createAttachmentSummaryLine(int count) {
        String formatted = context.getString(R.string.room_container_attached_summary, count);
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
            builder.append(valuePart);
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
        // Afficher systématiquement le nombre d’éléments rattachés après le nom du contenant.
        int count = Math.max(0, item.getAttachedItemCount());
        return displayName + " (" + count + ")";
    }

    @NonNull
    private String formatRankedName(@NonNull String baseName, int position) {
        String rankLabel = buildRankLabel(position);
        if (rankLabel.isEmpty()) {
            return baseName;
        }
        return rankLabel + " " + baseName;
    }

    @NonNull
    private String buildRankLabel(int position) {
        if (position < 0 || position >= items.size()) {
            return "";
        }
        RoomContentItem item = items.get(position);
        if (item.isContainer()) {
            return buildContainerRank(position, item);
        }
        return buildAttachmentRank(position);
    }

    @NonNull
    private String buildContainerRank(int position, @NonNull RoomContentItem container) {
        int parentPosition = findAttachedContainerPosition(position);
        if (parentPosition < 0) {
            int index = computeTopLevelContainerIndex(position);
            return index > 0 ? String.valueOf(index) : "";
        }
        String parentRank = buildRankLabel(parentPosition);
        if (parentRank.isEmpty()) {
            return "";
        }
        int indexWithinParent = computeAttachmentIndex(parentPosition, position);
        if (indexWithinParent <= 0) {
            return "";
        }
        return parentRank + "." + indexWithinParent;
    }

    @NonNull
    private String buildAttachmentRank(int position) {
        int parentPosition = findAttachedContainerPosition(position);
        if (parentPosition < 0) {
            return "";
        }
        RoomContentItem parent = items.get(parentPosition);
        if (!parent.hasAttachedItems()) {
            return "";
        }
        String parentRank = buildRankLabel(parentPosition);
        if (parentRank.isEmpty()) {
            return "";
        }
        int indexWithinParent = computeAttachmentIndex(parentPosition, position);
        if (indexWithinParent <= 0) {
            return "";
        }
        return parentRank + "." + indexWithinParent;
    }

    private int computeTopLevelContainerIndex(int position) {
        int rankIndex = 0;
        for (int i = 0; i <= position && i < items.size(); i++) {
            RoomContentItem candidate = items.get(i);
            if (!candidate.isContainer()) {
                continue;
            }
            if (findAttachedContainerPosition(i) >= 0) {
                continue;
            }
            rankIndex++;
            if (i == position) {
                return rankIndex;
            }
        }
        return 0;
    }

    private int computeAttachmentIndex(int containerPosition, int targetPosition) {
        if (containerPosition < 0 || containerPosition >= items.size()) {
            return 0;
        }
        RoomContentItem container = items.get(containerPosition);
        int declaredCount = Math.max(0, container.getAttachedItemCount());
        int limit = Math.min(items.size(), containerPosition + 1 + declaredCount);
        int rankIndex = 0;
        for (int i = containerPosition + 1; i < limit; i++) {
            RoomContentItem candidate = items.get(i);
            if (!shouldAttachmentBeRanked(candidate, i)) {
                continue;
            }
            rankIndex++;
            if (i == targetPosition) {
                return rankIndex;
            }
        }
        return 0;
    }

    private boolean shouldAttachmentBeRanked(@NonNull RoomContentItem item, int position) {
        if (item.isContainer()) {
            if (findAttachedContainerPosition(position) >= 0) {
                return true;
            }
            return item.hasAttachedItems();
        }
        return findAttachedContainerPosition(position) >= 0;
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
        if (position <= 0 || position >= items.size()) {
            return -1;
        }
        for (int i = position - 1; i >= 0; i--) {
            RoomContentItem candidate = items.get(i);
            if (!candidate.isContainer()) {
                continue;
            }
            int attachedCount = Math.max(0, candidate.getAttachedItemCount());
            int distanceFromContainer = position - i;
            if (distanceFromContainer <= attachedCount) {
                return i;
            }
            return -1;
        }
        return -1;
    }

    private boolean isItemHiddenByCollapsedContainer(int position) {
        int containerPosition = findAttachedContainerPosition(position);
        if (containerPosition < 0) {
            return false;
        }
        return containerCollapsedStates.get(containerPosition, false);
    }

    private boolean isContainerExpanded(int position) {
        return !containerCollapsedStates.get(position, false);
    }

    private void setContainerExpanded(int position, boolean expanded) {
        if (expanded) {
            containerCollapsedStates.delete(position);
        } else {
            containerCollapsedStates.put(position, true);
        }
        updateAttachedItemsDisplayState(position, expanded);
    }

    private void updateAttachedItemsDisplayState(int containerPosition, boolean expanded) {
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
        int end = Math.min(items.size(), start + attachedItemCount);
        for (int index = start; index < end; index++) {
            RoomContentItem attachedItem = items.get(index);
            if (attachedItem.isContainer()) {
                break;
            }
            attachedItem.setDisplayed(expanded);
        }
    }

    private boolean isLastAttachedItem(@NonNull RoomContentItem container, int containerPosition,
            int attachedPosition) {
        int attachedCount = Math.max(0, container.getAttachedItemCount());
        if (attachedCount <= 0) {
            return true;
        }
        int relativeIndex = attachedPosition - containerPosition;
        return relativeIndex >= attachedCount;
    }

    private void notifyAttachedItemsChanged(int containerPosition, int attachedItemCount) {
        if (attachedItemCount <= 0) {
            return;
        }
        int start = containerPosition + 1;
        int end = Math.min(items.size(), start + attachedItemCount);
        if (start >= end) {
            return;
        }
        notifyItemRangeChanged(start, end - start);
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
        } else {
            bannerView.setBackgroundColor(color);
        }
    }

    private void applyContainerBannerColor(@NonNull View bannerView) {
        int color = ContextCompat.getColor(context, R.color.room_content_banner_container);
        Drawable background = bannerView.getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable drawable = (GradientDrawable) background.mutate();
            drawable.setColor(color);
        } else {
            bannerView.setBackgroundColor(color);
        }
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

    interface OnRoomContentInteractionListener {
        void onCopyRoomContent(@NonNull RoomContentItem item, int position);

        void onMoveRoomContent(@NonNull RoomContentItem item, int position);

        void onEditRoomContent(@NonNull RoomContentItem item, int position);

        void onDeleteRoomContent(@NonNull RoomContentItem item, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View bannerContainer;
        @Nullable
        final View cardBackground;
        final View detailsContainer;
        @Nullable
        final ImageView photoView;
        final TextView nameView;
        final TextView commentView;
        final TextView metadataView;
        final ImageView toggleView;
        final ImageView menuView;
        @Nullable
        final ImageView addView;
        @Nullable
        final View filledIndicatorView;
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
        private PopupWindow optionsPopup;

        ViewHolder(@NonNull View itemView,
                @Nullable OnRoomContentInteractionListener interactionListener) {
            super(itemView);
            bannerContainer = itemView.findViewById(R.id.container_room_content_banner);
            cardBackground = itemView.findViewById(R.id.container_room_content_root);
            detailsContainer = itemView.findViewById(R.id.container_room_content_details);
            photoView = itemView.findViewById(R.id.image_room_content_photo);
            nameView = itemView.findViewById(R.id.text_room_content_name);
            commentView = itemView.findViewById(R.id.text_room_content_comment);
            metadataView = itemView.findViewById(R.id.text_room_content_metadata);
            toggleView = itemView.findViewById(R.id.image_room_content_toggle);
            menuView = itemView.findViewById(R.id.image_room_content_menu);
            addView = itemView.findViewById(R.id.image_room_content_add);
            filledIndicatorView = itemView.findViewById(R.id.view_room_container_filled_indicator);
            this.interactionListener = interactionListener;
            bannerContainer.setOnClickListener(view -> notifyEdit());
            if (photoView != null) {
                photoView.setOnClickListener(view -> notifyEdit());
            }
            toggleView.setOnClickListener(view -> toggleExpansion());
            menuView.setOnClickListener(view -> toggleOptionsMenu());
            if (addView != null) {
                addView.setOnClickListener(view -> Toast.makeText(itemView.getContext(),
                        R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());
            }
            defaultPaddingStart = photoView != null ? photoView.getPaddingStart() : 0;
            defaultPaddingTop = photoView != null ? photoView.getPaddingTop() : 0;
            defaultPaddingEnd = photoView != null ? photoView.getPaddingEnd() : 0;
            defaultPaddingBottom = photoView != null ? photoView.getPaddingBottom() : 0;
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
        }

        void bind(@NonNull RoomContentItem item, int position) {
            currentItem = item;
            dismissOptionsMenu();
            String baseName = resolveItemName(item);
            String rankedName = formatRankedName(baseName, position);
            if (item.isContainer()) {
                rankedName = appendAttachmentCount(rankedName, item);
            }
            nameView.setText(rankedName);
            boolean hasAttachedItems = item.isContainer() && item.hasAttachedItems();
            boolean isContainerExpanded = hasAttachedItems
                    && RoomContentAdapter.this.isContainerExpanded(position);
            View backgroundTarget = cardBackground != null ? cardBackground : itemView;
            if (item.isContainer()) {
                if (isContainerExpanded) {
                    backgroundTarget.setBackgroundResource(R.drawable.bg_room_container_group_header);
                } else {
                    backgroundTarget.setBackgroundResource(R.drawable.bg_room_container_item);
                }
                applyContainerBannerColor(bannerContainer);
                if (addView != null) {
                    addView.setVisibility(View.VISIBLE);
                    addView.setEnabled(true);
                    addView.setAlpha(1f);
                }
            } else {
                RoomContentItem attachedContainer = RoomContentAdapter.this
                        .findAttachedContainer(position);
                boolean isPartOfExpandedContainer = false;
                boolean isLastAttachment = false;
                if (attachedContainer != null) {
                    int containerPosition = RoomContentAdapter.this
                            .findAttachedContainerPosition(position);
                    if (containerPosition >= 0) {
                        boolean containerExpanded = RoomContentAdapter.this
                                .isContainerExpanded(containerPosition)
                                && attachedContainer.hasAttachedItems();
                        if (containerExpanded) {
                            isPartOfExpandedContainer = true;
                            isLastAttachment = RoomContentAdapter.this.isLastAttachedItem(
                                    attachedContainer, containerPosition, position);
                        }
                    }
                }
                int backgroundRes = isPartOfExpandedContainer
                        ? (isLastAttachment
                                ? R.drawable.bg_room_content_group_footer
                                : R.drawable.bg_room_content_group_middle)
                        : R.drawable.bg_room_content_card_default;
                backgroundTarget.setBackgroundResource(backgroundRes);
                applyBannerColor(bannerContainer, item.getType());
                if (addView != null) {
                    addView.setVisibility(View.GONE);
                }
            }
            boolean containerCollapsed = RoomContentAdapter.this
                    .isItemHiddenByCollapsedContainer(position);
            boolean hiddenByContainer = !item.isContainer()
                    && (containerCollapsed || !item.isDisplayed());
            if (!item.isContainer()) {
                item.setDisplayed(!hiddenByContainer);
            } else {
                item.setDisplayed(true);
            }
            itemView.setVisibility(hiddenByContainer ? View.GONE : View.VISIBLE);
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            if (layoutParams instanceof RecyclerView.LayoutParams) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) layoutParams;
                if (hiddenByContainer) {
                    params.height = 0;
                    params.leftMargin = 0;
                    params.topMargin = 0;
                    params.rightMargin = 0;
                    params.bottomMargin = 0;
                } else {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.leftMargin = defaultMarginLeft;
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
            updatePhoto(item);
            if (photoView != null) {
                photoView.setContentDescription(itemView.getContext()
                        .getString(R.string.content_description_room_content_photos, rankedName));
            }
            menuView.setContentDescription(itemView.getContext()
                    .getString(R.string.content_description_room_content_menu, rankedName));
        }

        void updateToggle(boolean hasDetails, boolean isExpanded, @NonNull String name) {
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

        private void notifyEdit() {
            if (interactionListener == null || currentItem == null) {
                return;
            }
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onEditRoomContent(currentItem, position);
        }

        private void notifyDelete() {
            if (interactionListener == null || currentItem == null) {
                return;
            }
            dismissOptionsMenu();
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onDeleteRoomContent(currentItem, position);
        }

        private void notifyCopy() {
            if (interactionListener == null || currentItem == null) {
                return;
            }
            dismissOptionsMenu();
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onCopyRoomContent(currentItem, position);
        }

        private void notifyMove() {
            if (interactionListener == null || currentItem == null) {
                return;
            }
            dismissOptionsMenu();
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onMoveRoomContent(currentItem, position);
        }

        private void toggleExpansion() {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            if (currentItem != null && currentItem.isContainer()
                    && currentItem.hasAttachedItems()) {
                boolean isExpanded = RoomContentAdapter.this.isContainerExpanded(position);
                RoomContentAdapter.this.setContainerExpanded(position, !isExpanded);
                notifyItemChanged(position);
                RoomContentAdapter.this.notifyAttachedItemsChanged(position,
                        currentItem.getAttachedItemCount());
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

        private int resolveBottomMargin(@NonNull RoomContentItem item, int position,
                boolean isContainerExpanded) {
            if (item.isContainer()) {
                return (isContainerExpanded && item.hasAttachedItems()) ? 0 : defaultMarginBottom;
            }
            RoomContentItem container = RoomContentAdapter.this.findAttachedContainer(position);
            if (container == null) {
                return defaultMarginBottom;
            }
            int containerPosition = RoomContentAdapter.this.findAttachedContainerPosition(position);
            if (containerPosition < 0) {
                return defaultMarginBottom;
            }
            boolean containerExpanded = RoomContentAdapter.this.isContainerExpanded(containerPosition)
                    && container.hasAttachedItems();
            if (!containerExpanded) {
                return defaultMarginBottom;
            }
            boolean isLastAttachment = RoomContentAdapter.this.isLastAttachedItem(container,
                    containerPosition, position);
            return isLastAttachment ? defaultMarginBottom : 0;
        }

        private void toggleOptionsMenu() {
            if (optionsPopup != null && optionsPopup.isShowing()) {
                optionsPopup.dismiss();
                return;
            }
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
                copyButton.setOnClickListener(view -> {
                    popupWindow.dismiss();
                    notifyCopy();
                });
            }
            View moveButton = popupView.findViewById(R.id.button_popup_room_content_move);
            if (moveButton != null) {
                moveButton.setOnClickListener(view -> {
                    popupWindow.dismiss();
                    notifyMove();
                });
            }
            View deleteButton = popupView.findViewById(R.id.button_popup_room_content_delete);
            if (deleteButton != null) {
                deleteButton.setOnClickListener(view -> {
                    popupWindow.dismiss();
                    notifyDelete();
                });
            }
            popupWindow.setOnDismissListener(() -> optionsPopup = null);
            optionsPopup = popupWindow;

            popupView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int popupHeight = popupView.getMeasuredHeight();
            int verticalOffset = (int) (itemView.getResources().getDisplayMetrics().density * 8);

            Rect displayFrame = new Rect();
            menuView.getWindowVisibleDisplayFrame(displayFrame);
            int[] location = new int[2];
            menuView.getLocationOnScreen(location);
            int anchorBottom = location[1] + menuView.getHeight();
            int spaceBelow = displayFrame.bottom - anchorBottom;
            int spaceAbove = location[1] - displayFrame.top;

            int yOffset = verticalOffset;
            if (spaceBelow < popupHeight + verticalOffset
                    && spaceAbove >= popupHeight + verticalOffset) {
                yOffset = -(menuView.getHeight() + popupHeight + verticalOffset);
            }

            PopupWindowCompat.showAsDropDown(popupWindow, menuView, 0, yOffset, Gravity.END);
        }

        void dismissOptionsMenu() {
            if (optionsPopup != null) {
                optionsPopup.dismiss();
                optionsPopup = null;
            }
        }
    }
}
