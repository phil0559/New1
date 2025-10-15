package com.example.new1;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RoomContentAdapter extends RecyclerView.Adapter<RoomContentAdapter.ViewHolder> {

    private final List<RoomContentItem> items;
    private final LayoutInflater inflater;
    private final Context context;

    public RoomContentAdapter(@NonNull Context context, @NonNull List<RoomContentItem> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_room_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomContentItem item = items.get(position);
        holder.nameView.setText(item.getName());

        applyBannerColor(holder.bannerContainer, item.getType());

        String comment = item.getComment();
        if (comment == null || comment.trim().isEmpty()) {
            holder.commentView.setVisibility(View.GONE);
        } else {
            holder.commentView.setVisibility(View.VISIBLE);
            holder.commentView.setText(comment);
        }

        List<String> metadataLines = new ArrayList<>();
        String type = item.getType();
        if (type != null && !type.trim().isEmpty()) {
            metadataLines.add(context.getString(R.string.room_content_metadata_type, type));
        }
        String category = item.getCategory();
        if (category != null && !category.trim().isEmpty()) {
            metadataLines.add(context.getString(R.string.room_content_metadata_category, category));
        }
        String barcode = item.getBarcode();
        if (barcode != null && !barcode.trim().isEmpty()) {
            metadataLines.add(context.getString(R.string.room_content_metadata_barcode, barcode));
        }
        if (metadataLines.isEmpty()) {
            holder.metadataView.setVisibility(View.GONE);
        } else {
            holder.metadataView.setVisibility(View.VISIBLE);
            holder.metadataView.setText(TextUtils.join("\n", metadataLines));
        }
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View bannerContainer;
        final TextView nameView;
        final TextView commentView;
        final TextView metadataView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerContainer = itemView.findViewById(R.id.container_room_content_banner);
            nameView = itemView.findViewById(R.id.text_room_content_name);
            commentView = itemView.findViewById(R.id.text_room_content_comment);
            metadataView = itemView.findViewById(R.id.text_room_content_metadata);
        }
    }
}
