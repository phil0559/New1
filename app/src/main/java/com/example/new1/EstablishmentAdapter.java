package com.example.new1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.PopupWindowCompat;

import java.util.List;

public class EstablishmentAdapter extends RecyclerView.Adapter<EstablishmentAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Establishment> data;
    private final OnEstablishmentInteractionListener interactionListener;

    public EstablishmentAdapter(Context context, List<Establishment> data,
            OnEstablishmentInteractionListener interactionListener) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
        this.interactionListener = interactionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_establishment, parent, false);
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

    interface OnEstablishmentInteractionListener {
        void onOpenEstablishment(@NonNull Establishment establishment, int position);
        void onEditEstablishment(@NonNull Establishment establishment, int position);
        void onDeleteEstablishment(@NonNull Establishment establishment, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView summaryLabelView;
        private final TextView summaryView;
        private final ImageView menuView;
        private final ImageView photoView;
        private final int defaultPaddingStart;
        private final int defaultPaddingTop;
        private final int defaultPaddingEnd;
        private final int defaultPaddingBottom;
        private final OnEstablishmentInteractionListener interactionListener;
        private PopupWindow popupWindow;
        private Establishment currentItem;

        ViewHolder(@NonNull View itemView, OnEstablishmentInteractionListener interactionListener) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_establishment_name);
            summaryLabelView = itemView.findViewById(R.id.text_establishment_summary_label);
            summaryView = itemView.findViewById(R.id.text_establishment_summary);
            menuView = itemView.findViewById(R.id.image_establishment_menu);
            photoView = itemView.findViewById(R.id.image_establishment_photo);
            defaultPaddingStart = photoView.getPaddingStart();
            defaultPaddingTop = photoView.getPaddingTop();
            defaultPaddingEnd = photoView.getPaddingEnd();
            defaultPaddingBottom = photoView.getPaddingBottom();
            this.interactionListener = interactionListener;
            menuView.setOnClickListener(view -> togglePopup());
            photoView.setOnClickListener(view -> {
                if (interactionListener == null || currentItem == null) {
                    return;
                }
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                interactionListener.onEditEstablishment(currentItem, position);
            });
            itemView.setOnClickListener(this::openContent);
        }

        void bind(Establishment item) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            currentItem = item;
            nameView.setText(item.getName());
            menuView.setContentDescription(itemView.getContext().getString(
                    R.string.content_description_establishment_menu,
                    item.getName()
            ));
            photoView.setContentDescription(itemView.getContext().getString(
                    R.string.content_description_establishment_photos,
                    item.getName()
            ));
            updatePhotoThumbnail(item);
            String comment = item.getComment();
            if (comment == null || comment.trim().isEmpty()) {
                summaryLabelView.setVisibility(View.GONE);
                summaryView.setVisibility(View.GONE);
                summaryView.setText(null);
            } else {
                summaryLabelView.setVisibility(View.VISIBLE);
                summaryView.setVisibility(View.VISIBLE);
                summaryView.setText(comment.trim());
            }
        }

        private void updatePhotoThumbnail(Establishment item) {
            List<String> photos = item.getPhotos();
            if (photos.isEmpty()) {
                resetToDefaultPhoto();
                return;
            }

            Bitmap bitmap = decodePhoto(photos.get(0));
            if (bitmap == null) {
                resetToDefaultPhoto();
                return;
            }

            photoView.setImageBitmap(bitmap);
            photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photoView.setPadding(0, 0, 0, 0);
        }

        private void resetToDefaultPhoto() {
            photoView.setImageResource(R.drawable.ic_establishment_photos);
            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            photoView.setPadding(defaultPaddingStart, defaultPaddingTop, defaultPaddingEnd, defaultPaddingBottom);
        }

        @Nullable
        private Bitmap decodePhoto(String encodedPhoto) {
            try {
                byte[] decoded = Base64.decode(encodedPhoto, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        private void togglePopup() {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
                return;
            }

            View popupContent = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.popup_establishment_menu, null);

            popupWindow = new PopupWindow(
                    popupContent,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(() -> popupWindow = null);

            View editButton = popupContent.findViewById(R.id.button_popup_edit);
            if (editButton != null) {
                editButton.setOnClickListener(view -> {
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                    if (interactionListener == null || currentItem == null) {
                        return;
                    }
                    int position = getBindingAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    interactionListener.onEditEstablishment(currentItem, position);
                });
            }

            View deleteButton = popupContent.findViewById(R.id.button_popup_delete);
            if (deleteButton != null) {
                deleteButton.setOnClickListener(view -> {
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                    if (interactionListener == null || currentItem == null) {
                        return;
                    }
                    int position = getBindingAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    interactionListener.onDeleteEstablishment(currentItem, position);
                });
            }

            int verticalOffset = (int) (itemView.getResources().getDisplayMetrics().density * 8);
            popupContent.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int popupHeight = popupContent.getMeasuredHeight();

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

        private void openContent(View view) {
            if (currentItem == null || interactionListener == null) {
                return;
            }
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            interactionListener.onOpenEstablishment(currentItem, position);
        }
    }
}
