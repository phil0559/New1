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

public class CategorySelectorAdapter extends RecyclerView.Adapter<CategorySelectorAdapter.CategoryViewHolder> {

    public interface CategoryActionListener {
        void onCategorySelected(@NonNull String category);

        void onEditCategory(@NonNull String category);

        void onDeleteCategory(@NonNull String category);
    }

    @NonNull
    private final List<String> categories = new ArrayList<>();
    private final CategoryActionListener actionListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public CategorySelectorAdapter(@NonNull List<String> categoryValues,
                                   @NonNull CategoryActionListener actionListener) {
        this.actionListener = actionListener;
        categories.addAll(categoryValues);
    }

    public void setSelectedCategory(@Nullable String selectedCategory) {
        int previousSelected = selectedPosition;
        if (selectedCategory == null) {
            selectedPosition = RecyclerView.NO_POSITION;
        } else {
            selectedPosition = categories.indexOf(selectedCategory);
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
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_selector, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.bind(category, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void addCategory(@NonNull String categoryLabel) {
        categories.add(categoryLabel);
        notifyItemInserted(categories.size() - 1);
    }

    private void selectCategory(int position) {
        if (position < 0 || position >= categories.size()) {
            return;
        }
        int previousSelected = selectedPosition;
        selectedPosition = position;
        if (previousSelected != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelected);
        }
        notifyItemChanged(selectedPosition);
        actionListener.onCategorySelected(categories.get(position));
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final RadioButton radioButton;
        private final TextView labelView;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radio_category_option);
            labelView = itemView.findViewById(R.id.text_category_label);
            editButton = itemView.findViewById(R.id.button_edit_category);
            deleteButton = itemView.findViewById(R.id.button_delete_category);
        }

        void bind(@NonNull String categoryLabel, boolean isSelected) {
            labelView.setText(categoryLabel);
            radioButton.setChecked(isSelected);

            View.OnClickListener selectListener = view -> {
                int adapterPosition = getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    selectCategory(adapterPosition);
                }
            };

            itemView.setOnClickListener(selectListener);
            radioButton.setOnClickListener(selectListener);
            labelView.setOnClickListener(selectListener);

            editButton.setOnClickListener(view -> actionListener.onEditCategory(categoryLabel));
            deleteButton.setOnClickListener(view -> actionListener.onDeleteCategory(categoryLabel));
        }
    }
}
