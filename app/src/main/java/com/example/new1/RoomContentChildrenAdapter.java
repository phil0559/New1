package com.example.new1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class RoomContentChildrenAdapter extends RecyclerView.Adapter<RoomContentChildrenAdapter.ChildViewHolder> {

    private final LayoutInflater inflater;
    private final List<RoomContentItem> children = new ArrayList<>();

    RoomContentChildrenAdapter(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_room_content_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        RoomContentItem child = children.get(position);
        holder.bind(child);
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    void submitChildren(@Nullable List<RoomContentItem> newChildren) {
        children.clear();
        if (newChildren == null) {
            notifyDataSetChanged();
            return;
        }
        for (RoomContentItem child : newChildren) {
            if (child != null) {
                children.add(child);
            }
        }
        notifyDataSetChanged();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView commentView;

        ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_room_content_child_name);
            commentView = itemView.findViewById(R.id.text_room_content_child_comment);
        }

        void bind(@NonNull RoomContentItem item) {
            nameView.setText(item.getName());
            String comment = item.getComment();
            String trimmed = comment != null ? comment.trim() : "";
            if (!trimmed.isEmpty()) {
                commentView.setVisibility(View.VISIBLE);
                commentView.setText(trimmed);
            } else {
                commentView.setVisibility(View.GONE);
                commentView.setText(null);
            }
        }
    }
}
