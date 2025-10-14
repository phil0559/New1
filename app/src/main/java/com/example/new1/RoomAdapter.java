package com.example.new1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Room> data;

    public RoomAdapter(Context context, List<Room> data) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_room, parent, false);
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
        private final ImageView photoView;
        private final int defaultPaddingStart;
        private final int defaultPaddingTop;
        private final int defaultPaddingEnd;
        private final int defaultPaddingBottom;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_room_name);
            photoView = itemView.findViewById(R.id.image_room_photo);
            defaultPaddingStart = photoView.getPaddingStart();
            defaultPaddingTop = photoView.getPaddingTop();
            defaultPaddingEnd = photoView.getPaddingEnd();
            defaultPaddingBottom = photoView.getPaddingBottom();
        }

        void bind(Room room) {
            nameView.setText(room.getName());
            updatePhoto(room);
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
    }
}
