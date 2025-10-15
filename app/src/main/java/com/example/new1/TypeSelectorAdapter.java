package com.example.new1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TypeSelectorAdapter extends RecyclerView.Adapter<TypeSelectorAdapter.TypeViewHolder> {

    public interface TypeActionListener {
        void onTypeSelected(@NonNull String type);

        void onEditType(@NonNull String type);

        void onDeleteType(@NonNull String type);
    }

    @NonNull
    private final List<String> types = new ArrayList<>();
    private final TypeActionListener actionListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public TypeSelectorAdapter(@NonNull List<String> typeValues,
                               @NonNull TypeActionListener actionListener) {
        this.actionListener = actionListener;
        types.addAll(typeValues);
    }

    public void setSelectedType(@Nullable String selectedType) {
        int previousSelected = selectedPosition;
        if (selectedType == null) {
            selectedPosition = RecyclerView.NO_POSITION;
        } else {
            selectedPosition = types.indexOf(selectedType);
        }
        if (previousSelected != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelected);
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }
    }

    @NonNull
    @Override
    public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_type_selector, parent, false);
        return new TypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeViewHolder holder, int position) {
        String type = types.get(position);
        holder.bind(type, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return types.size();
    }

    private void selectType(int position) {
        if (position < 0 || position >= types.size()) {
            return;
        }
        int previousSelected = selectedPosition;
        selectedPosition = position;
        if (previousSelected != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelected);
        }
        notifyItemChanged(selectedPosition);
        actionListener.onTypeSelected(types.get(position));
    }

    class TypeViewHolder extends RecyclerView.ViewHolder {
        private final RadioButton radioButton;
        private final TextView labelView;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        TypeViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radio_type_option);
            labelView = itemView.findViewById(R.id.text_type_label);
            editButton = itemView.findViewById(R.id.button_edit_type);
            deleteButton = itemView.findViewById(R.id.button_delete_type);
        }

        void bind(@NonNull String typeLabel, boolean isSelected) {
            labelView.setText(typeLabel);
            radioButton.setChecked(isSelected);

            View.OnClickListener selectListener = view -> {
                int adapterPosition = getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    selectType(adapterPosition);
                }
            };

            itemView.setOnClickListener(selectListener);
            radioButton.setOnClickListener(selectListener);
            labelView.setOnClickListener(selectListener);

            editButton.setOnClickListener(view -> actionListener.onEditType(typeLabel));
            deleteButton.setOnClickListener(view -> actionListener.onDeleteType(typeLabel));
        }
    }
}
