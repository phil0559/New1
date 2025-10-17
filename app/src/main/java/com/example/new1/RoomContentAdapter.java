package com.example.new1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

        boolean hasExpandableContent = hasComment || hasMetadata;
        if (!hasExpandableContent) {
            expandedStates.delete(position);
        }
        boolean isExpanded = hasExpandableContent && expandedStates.get(position, false);
        holder.detailsContainer
                .setVisibility(hasExpandableContent && isExpanded ? View.VISIBLE : View.GONE);
        CharSequence displayedName = holder.nameView.getText();
        String toggleLabel = displayedName != null ? displayedName.toString() : resolveItemName(item);
        holder.updateToggle(hasExpandableContent, isExpanded, toggleLabel);

        if (isExpanded) {
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
        // La numérotation ne concerne que les contenants et les éléments qui leur sont rattachés.
        int containerIndex = 0;
        int remainingAttachedItems = 0;
        int currentContentIndex = 0;
        for (int i = 0; i <= position && i < items.size(); i++) {
            RoomContentItem item = items.get(i);
            if (item.isContainer()) {
                containerIndex++;
                remainingAttachedItems = Math.max(0, item.getAttachedItemCount());
                currentContentIndex = 0;
                if (i == position) {
                    return String.valueOf(containerIndex);
                }
                continue;
            }
            if (remainingAttachedItems > 0) {
                currentContentIndex++;
                remainingAttachedItems--;
                if (i == position) {
                    return containerIndex > 0
                            ? containerIndex + "." + currentContentIndex
                            : "";
                }
                continue;
            }
            if (i == position) {
                return "";
            }
        }
        return "";
    }

    @Nullable
    private RoomContentItem findAttachedContainer(int position) {
        if (position <= 0 || position >= items.size()) {
            return null;
        }
        for (int i = position - 1; i >= 0; i--) {
            RoomContentItem candidate = items.get(i);
            if (!candidate.isContainer()) {
                continue;
            }
            int attachedCount = Math.max(0, candidate.getAttachedItemCount());
            int distanceFromContainer = position - i;
            if (distanceFromContainer <= attachedCount) {
                return candidate;
            }
            return null;
        }
        return null;
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
        if (trimmedType.equalsIgnoreCase(context.getString(R.string.dialog_type_book))) {
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
        @Nullable
        private PopupWindow optionsPopup;

        ViewHolder(@NonNull View itemView,
                @Nullable OnRoomContentInteractionListener interactionListener) {
            super(itemView);
            bannerContainer = itemView.findViewById(R.id.container_room_content_banner);
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
            if (item.isContainer()) {
                itemView.setBackgroundResource(R.drawable.bg_room_container_item);
                applyContainerBannerColor(bannerContainer);
                if (addView != null) {
                    addView.setVisibility(View.VISIBLE);
                    addView.setEnabled(true);
                    addView.setAlpha(1f);
                }
            } else {
                RoomContentItem attachedContainer = RoomContentAdapter.this
                        .findAttachedContainer(position);
                boolean isAttachedToContainer = attachedContainer != null;
                int backgroundRes = isAttachedToContainer
                        ? R.drawable.bg_room_content_attached_item
                        : R.drawable.bg_input_field_white;
                itemView.setBackgroundResource(backgroundRes);
                applyBannerColor(bannerContainer, item.getType());
                if (addView != null) {
                    addView.setVisibility(View.GONE);
                }
            }
            if (filledIndicatorView != null) {
                boolean isExpanded = expandedStates.get(position, false);
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
            boolean isExpanded = expandedStates.get(position, false);
            if (isExpanded) {
                expandedStates.delete(position);
            } else {
                expandedStates.put(position, true);
            }
            notifyItemChanged(position);
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
            popupWindow.showAsDropDown(menuView, 0, 0, Gravity.END);
        }

        void dismissOptionsMenu() {
            if (optionsPopup != null) {
                optionsPopup.dismiss();
                optionsPopup = null;
            }
        }
    }
}
