package com.example.new1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EstablishmentContentActivity extends Activity {
    static final String EXTRA_ESTABLISHMENT_NAME = "extra_establishment_name";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.apply(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establishment_content);

        ImageView backButton = findViewById(R.id.button_back);
        if (backButton != null) {
            backButton.setOnClickListener(view -> finish());
        }

        ImageView searchButton = findViewById(R.id.button_search);
        if (searchButton != null) {
            searchButton.setOnClickListener(view ->
                    Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());
        }

        String establishmentName = getIntent().getStringExtra(EXTRA_ESTABLISHMENT_NAME);
        TextView subtitleView = findViewById(R.id.text_establishment_subtitle);
        if (subtitleView != null) {
            if (establishmentName == null || establishmentName.trim().isEmpty()) {
                subtitleView.setText(R.string.establishment_content_placeholder);
            } else {
                subtitleView.setText(getString(R.string.establishment_content_placeholder_with_name,
                        establishmentName));
            }
        }

        ImageView addButton = findViewById(R.id.button_add_content);
        if (addButton != null) {
            addButton.setOnClickListener(view ->
                    Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());
        }
    }
}
