package com.example.new1;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.PopupWindowCompat;

import java.util.List;

public class EstablishmentAdapter extends RecyclerView.Adapter<EstablishmentAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Establishment> data;
    private final OnEstablishmentMenuListener menuListener;

    public EstablishmentAdapter(Context context, List<Establishment> data,
            OnEstablishmentMenuListener menuListener) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
        this.menuListener = menuListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_establishment, parent, false);
        return new ViewHolder(view, menuListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    interface OnEstablishmentMenuListener {
        void onEditEstablishment(@NonNull Establishment establishment, int position);
        void onDeleteEstablishment(@NonNull Establishment establishment, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView commentView;
        private final ImageView menuView;
        private final OnEstablishmentMenuListener menuListener;
        private PopupWindow popupWindow;
        private Establishment currentItem;

        ViewHolder(@NonNull View itemView, OnEstablishmentMenuListener menuListener) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_establishment_name);
            commentView = itemView.findViewById(R.id.text_establishment_comment);
            menuView = itemView.findViewById(R.id.image_establishment_menu);
            this.menuListener = menuListener;
            menuView.setOnClickListener(view -> togglePopup());
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
            String comment = item.getComment();
            if (comment == null || comment.isEmpty()) {
                commentView.setVisibility(View.GONE);
            } else {
                commentView.setVisibility(View.VISIBLE);
                commentView.setText(comment);
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
                    if (menuListener == null || currentItem == null) {
                        return;
                    }
                    int position = getBindingAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    menuListener.onEditEstablishment(currentItem, position);
                });
            }

            View deleteButton = popupContent.findViewById(R.id.button_popup_delete);
            if (deleteButton != null) {
                deleteButton.setOnClickListener(view -> {
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                    if (menuListener == null || currentItem == null) {
                        return;
                    }
                    int position = getBindingAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    menuListener.onDeleteEstablishment(currentItem, position);
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
    }
}
