package com.example.new1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.PopupWindowCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Room> data;
    @Nullable
    private final OnRoomInteractionListener interactionListener;

    public RoomAdapter(Context context, List<Room> data,
            @Nullable OnRoomInteractionListener interactionListener) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
        this.interactionListener = interactionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_room, parent, false);
        return new ViewHolder(view, interactionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    interface OnRoomInteractionListener {
        void onEditRoom(@NonNull Room room, int position);

        void onDeleteRoom(@NonNull Room room, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final ImageView photoView;
        private final TextView commentView;
        private final ImageView menuView;
        private final int defaultPaddingStart;
        private final int defaultPaddingTop;
        private final int defaultPaddingEnd;
        private final int defaultPaddingBottom;
        @Nullable
        private final OnRoomInteractionListener interactionListener;
        @Nullable
        private PopupWindow popupWindow;
        @Nullable
        private Room currentRoom;

        ViewHolder(@NonNull View itemView, @Nullable OnRoomInteractionListener interactionListener) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_room_name);
            photoView = itemView.findViewById(R.id.image_room_photo);
            commentView = itemView.findViewById(R.id.text_room_comment);
            menuView = itemView.findViewById(R.id.image_room_menu);
            defaultPaddingStart = photoView.getPaddingStart();
            defaultPaddingTop = photoView.getPaddingTop();
            defaultPaddingEnd = photoView.getPaddingEnd();
            defaultPaddingBottom = photoView.getPaddingBottom();
            this.interactionListener = interactionListener;
            if (menuView != null) {
                menuView.setOnClickListener(view -> togglePopup());
            }
        }

        void bind(Room room) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            currentRoom = room;
            nameView.setText(room.getName());
            updateComment(room.getComment());
            updatePhoto(room);
            if (menuView != null) {
                menuView.setContentDescription(itemView.getContext().getString(
                        R.string.content_description_room_menu,
                        room.getName()
                ));
            }
        }

        private void updateComment(String comment) {
            if (commentView == null) {
                return;
            }
            if (comment == null || comment.trim().isEmpty()) {
                commentView.setVisibility(View.GONE);
                commentView.setText("");
            } else {
                commentView.setText(comment);
                commentView.setVisibility(View.VISIBLE);
            }
        }

        private void updatePhoto(Room room) {
            List<String> photos = room.getPhotos();
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
            photoView.setImageResource(R.drawable.ic_establishment_photos);
            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            photoView.setPadding(defaultPaddingStart, defaultPaddingTop, defaultPaddingEnd, defaultPaddingBottom);
        }

        @Nullable
        private Bitmap decodePhoto(String encoded) {
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
                    .inflate(R.layout.popup_room_menu, null);
            TextView titleView = popupContent.findViewById(R.id.text_popup_room_title);
            if (titleView != null) {
                titleView.setText(currentRoom != null ? currentRoom.getName() : "");
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

            View editButton = popupContent.findViewById(R.id.button_popup_room_edit);
            if (editButton != null) {
                editButton.setOnClickListener(view -> {
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                    notifyEdit();
                });
            }

            View deleteButton = popupContent.findViewById(R.id.button_popup_room_delete);
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

            PopupWindowCompat.showAsDropDown(popupWindow, menuView, 0, yOffset, android.view.Gravity.END);
        }

        private void notifyEdit() {
            if (interactionListener == null || currentRoom == null) {
                return;
            }
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onEditRoom(currentRoom, position);
        }

        private void notifyDelete() {
            if (interactionListener == null || currentRoom == null) {
                return;
            }
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onDeleteRoom(currentRoom, position);
        }
    }
}
