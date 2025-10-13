package com.example.new1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EstablishmentAdapter extends ArrayAdapter<Establishment> {
    private final LayoutInflater inflater;

    public EstablishmentAdapter(Context context, List<Establishment> data) {
        super(context, R.layout.item_establishment, data);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_establishment, parent, false);
        }

        TextView nameView = view.findViewById(R.id.text_establishment_name);
        TextView commentView = view.findViewById(R.id.text_establishment_comment);

        Establishment item = getItem(position);
        if (item != null) {
            nameView.setText(item.getName());
            String comment = item.getComment();
            if (comment == null || comment.isEmpty()) {
                commentView.setVisibility(View.GONE);
            } else {
                commentView.setVisibility(View.VISIBLE);
                commentView.setText(comment);
            }
        }

        return view;
    }
}
