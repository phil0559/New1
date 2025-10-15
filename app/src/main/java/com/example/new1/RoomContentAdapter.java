package com.example.new1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RoomContentAdapter extends RecyclerView.Adapter<RoomContentAdapter.ViewHolder> {

    private final List<RoomContentItem> items;
    private final LayoutInflater inflater;
    private final Context context;
    @Nullable
    private final OnRoomContentInteractionListener interactionListener;

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

        String comment = item.getComment();
        boolean hasComment = comment != null && !comment.trim().isEmpty();
        if (hasComment) {
            holder.commentView.setVisibility(View.VISIBLE);
            holder.commentView.setText(comment);
        } else {
            holder.commentView.setVisibility(View.GONE);
            holder.commentView.setText(null);
        }

        List<String> metadataLines = new ArrayList<>();
        String category = item.getCategory();
        if (category != null && !category.trim().isEmpty()) {
            metadataLines.add(context.getString(R.string.room_content_metadata_category, category));
        }
        String series = item.getSeries();
        if (series != null && !series.trim().isEmpty()) {
            metadataLines.add(context.getString(R.string.room_content_metadata_series, series));
        }
        String number = item.getNumber();
        if (number != null && !number.trim().isEmpty()) {
            metadataLines.add(context.getString(R.string.room_content_metadata_number, number));
        }
        String author = item.getAuthor();
        if (author != null && !author.trim().isEmpty()) {
            metadataLines.add(context.getString(R.string.room_content_metadata_author, author));
        }
        String publisher = item.getPublisher();
        if (publisher != null && !publisher.trim().isEmpty()) {
            metadataLines.add(context.getString(R.string.room_content_metadata_publisher, publisher));
        }
        String barcode = item.getBarcode();
        if (barcode != null && !barcode.trim().isEmpty()) {
            metadataLines.add(context.getString(R.string.room_content_metadata_barcode, barcode));
        }
        boolean hasMetadata = !metadataLines.isEmpty();
        if (hasMetadata) {
            holder.metadataView.setVisibility(View.VISIBLE);
            holder.metadataView.setText(TextUtils.join("\n", metadataLines));
        } else {
            holder.metadataView.setVisibility(View.GONE);
            holder.metadataView.setText(null);
        }

        holder.detailsContainer.setVisibility(hasComment || hasMetadata ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return items.size();
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
            this.interactionListener = interactionListener;
            bannerContainer.setOnClickListener(view -> notifyEdit());
            if (photoView != null) {
                photoView.setOnClickListener(view -> notifyEdit());
            }
            if (deleteView != null) {
                deleteView.setOnClickListener(view -> notifyDelete());
            }
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
    }
}
