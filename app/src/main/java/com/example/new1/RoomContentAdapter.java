package com.example.new1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
        holder.bind(item);

        CharSequence commentText = formatComment(item.getComment());
        boolean hasComment = commentText != null;
        if (hasComment) {
            holder.commentView.setVisibility(View.VISIBLE);
            holder.commentView.setText(commentText);
        } else {
            holder.commentView.setVisibility(View.GONE);
            holder.commentView.setText(null);
        }

        List<CharSequence> metadataLines = new ArrayList<>();
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
        if (hasMetadata) {
            holder.metadataView.setVisibility(View.VISIBLE);
            holder.metadataView.setText(metadataText);
        } else {
            holder.metadataView.setVisibility(View.GONE);
            holder.metadataView.setText(null);
        }

        boolean hasDetails = hasComment || hasMetadata;
        if (!hasDetails) {
            expandedStates.delete(position);
        }
        boolean isExpanded = hasDetails && expandedStates.get(position, false);
        holder.detailsContainer.setVisibility(hasDetails && isExpanded ? View.VISIBLE : View.GONE);
        holder.updateToggle(hasDetails, isExpanded, item.getName());
    }

    @Override
    public int getItemCount() {
        return items.size();
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

    @Nullable
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
        return R.color.room_content_banner_default;
    }

    interface OnRoomContentInteractionListener {
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
        @Nullable
        final ImageView deleteView;
        final ImageView toggleView;
        @Nullable
        final OnRoomContentInteractionListener interactionListener;
        @Nullable
        private RoomContentItem currentItem;
        private final int defaultPaddingStart;
        private final int defaultPaddingTop;
        private final int defaultPaddingEnd;
        private final int defaultPaddingBottom;

        ViewHolder(@NonNull View itemView,
                @Nullable OnRoomContentInteractionListener interactionListener) {
            super(itemView);
            bannerContainer = itemView.findViewById(R.id.container_room_content_banner);
            detailsContainer = itemView.findViewById(R.id.container_room_content_details);
            photoView = itemView.findViewById(R.id.image_room_content_photo);
            nameView = itemView.findViewById(R.id.text_room_content_name);
            commentView = itemView.findViewById(R.id.text_room_content_comment);
            metadataView = itemView.findViewById(R.id.text_room_content_metadata);
            deleteView = itemView.findViewById(R.id.image_room_content_delete);
            toggleView = itemView.findViewById(R.id.image_room_content_toggle);
            this.interactionListener = interactionListener;
            bannerContainer.setOnClickListener(view -> notifyEdit());
            if (photoView != null) {
                photoView.setOnClickListener(view -> notifyEdit());
            }
            if (deleteView != null) {
                deleteView.setOnClickListener(view -> notifyDelete());
            }
            toggleView.setOnClickListener(view -> toggleExpansion());
            defaultPaddingStart = photoView != null ? photoView.getPaddingStart() : 0;
            defaultPaddingTop = photoView != null ? photoView.getPaddingTop() : 0;
            defaultPaddingEnd = photoView != null ? photoView.getPaddingEnd() : 0;
            defaultPaddingBottom = photoView != null ? photoView.getPaddingBottom() : 0;
        }

        void bind(@NonNull RoomContentItem item) {
            currentItem = item;
            nameView.setText(item.getName());
            applyBannerColor(bannerContainer, item.getType());
            updatePhoto(item);
            if (deleteView != null) {
                deleteView.setContentDescription(itemView.getContext()
                        .getString(R.string.content_description_room_content_delete));
            }
            if (photoView != null) {
                photoView.setContentDescription(itemView.getContext()
                        .getString(R.string.content_description_room_content_photos, item.getName()));
            }
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
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onDeleteRoomContent(currentItem, position);
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
    }
}
