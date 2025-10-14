package com.example.new1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_establishment_name);
            commentView = itemView.findViewById(R.id.text_establishment_comment);
            menuView = itemView.findViewById(R.id.image_establishment_menu);
        }

        void bind(Establishment item) {
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
    }
}
