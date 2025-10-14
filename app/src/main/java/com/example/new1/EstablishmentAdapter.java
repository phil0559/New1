package com.example.new1;

import android.content.Context;
import android.graphics.Color;
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

    public EstablishmentAdapter(Context context, List<Establishment> data) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_establishment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView commentView;
        private final ImageView menuView;
        private PopupWindow popupWindow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_establishment_name);
            commentView = itemView.findViewById(R.id.text_establishment_comment);
            menuView = itemView.findViewById(R.id.image_establishment_menu);
            menuView.setOnClickListener(view -> togglePopup());
        }

        void bind(Establishment item) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
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

            int verticalOffset = (int) (itemView.getResources().getDisplayMetrics().density * 8);
            PopupWindowCompat.showAsDropDown(popupWindow, menuView, 0, verticalOffset, Gravity.END);
        }
    }
}
