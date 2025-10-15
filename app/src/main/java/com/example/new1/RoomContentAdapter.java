package com.example.new1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.PopupWindowCompat;
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
        final ImageView menuView;
        @Nullable
        final OnRoomContentInteractionListener interactionListener;
        @Nullable
        private PopupWindow popupWindow;
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
            menuView = itemView.findViewById(R.id.image_room_content_menu);
            this.interactionListener = interactionListener;
            if (photoView != null) {
                photoView.setOnClickListener(view -> notifyEdit());
            }
            if (menuView != null) {
                menuView.setOnClickListener(view -> togglePopup());
            }
            defaultPaddingStart = photoView != null ? photoView.getPaddingStart() : 0;
            defaultPaddingTop = photoView != null ? photoView.getPaddingTop() : 0;
            defaultPaddingEnd = photoView != null ? photoView.getPaddingEnd() : 0;
            defaultPaddingBottom = photoView != null ? photoView.getPaddingBottom() : 0;
        }

        void bind(@NonNull RoomContentItem item) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            currentItem = item;
            nameView.setText(item.getName());
            applyBannerColor(bannerContainer, item.getType());
            updatePhoto(item);
            if (menuView != null) {
                menuView.setContentDescription(itemView.getContext()
                        .getString(R.string.content_description_room_content_menu));
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

        private void togglePopup() {
            if (menuView == null) {
                return;
            }
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
                return;
            }

            View popupContent = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.popup_room_content_menu, null);
            TextView titleView = popupContent.findViewById(R.id.text_popup_room_content_title);
            if (titleView != null) {
                String name = currentItem != null ? currentItem.getName() : "";
                if (name == null || name.trim().isEmpty()) {
                    name = itemView.getContext()
                            .getString(R.string.dialog_room_content_item_placeholder);
                }
                titleView.setText(name);
            }

            popupWindow = new PopupWindow(
                    popupContent,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(() -> popupWindow = null);

            View editButton = popupContent.findViewById(R.id.button_popup_room_content_edit);
            if (editButton != null) {
                editButton.setOnClickListener(view -> {
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                    notifyEdit();
                });
            }

            View deleteButton = popupContent.findViewById(R.id.button_popup_room_content_delete);
            if (deleteButton != null) {
                deleteButton.setOnClickListener(view -> {
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                    notifyDelete();
                });
            }

            popupContent.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int popupHeight = popupContent.getMeasuredHeight();
            int verticalOffset = (int) (itemView.getResources().getDisplayMetrics().density * 8);

            Rect displayFrame = new Rect();
            menuView.getWindowVisibleDisplayFrame(displayFrame);
            int[] location = new int[2];
            menuView.getLocationOnScreen(location);
            int anchorBottom = location[1] + menuView.getHeight();
            int spaceBelow = displayFrame.bottom - anchorBottom;
            int spaceAbove = location[1] - displayFrame.top;

            int yOffset = verticalOffset;
            if (spaceBelow < popupHeight + verticalOffset && spaceAbove >= popupHeight + verticalOffset) {
                yOffset = -(menuView.getHeight() + popupHeight + verticalOffset);
            }

            PopupWindowCompat.showAsDropDown(popupWindow, menuView, 0, yOffset, Gravity.END);
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
