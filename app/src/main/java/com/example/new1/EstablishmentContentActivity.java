package com.example.new1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EstablishmentContentActivity extends Activity {
    public static final String EXTRA_ESTABLISHMENT_NAME = "extra_establishment_name";
    private static final String EXTRA_ESTABLISHMENT_COMMENT = "extra_establishment_comment";
    private static final String EXTRA_ESTABLISHMENT_PHOTO_COUNT = "extra_establishment_photo_count";

    public static Intent createIntent(Context context, Establishment establishment) {
        Intent intent = new Intent(context, EstablishmentContentActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (establishment != null) {
            String name = establishment.getName();
            if (name != null) {
                intent.putExtra(EXTRA_ESTABLISHMENT_NAME, name);
            }
            String comment = establishment.getComment();
            if (comment != null) {
                intent.putExtra(EXTRA_ESTABLISHMENT_COMMENT, comment);
            }
            intent.putExtra(EXTRA_ESTABLISHMENT_PHOTO_COUNT, establishment.getPhotos().size());
        }
        return intent;
    }

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

        Intent intent = getIntent();
        String establishmentName = intent != null
                ? intent.getStringExtra(EXTRA_ESTABLISHMENT_NAME)
                : null;
        TextView subtitleView = findViewById(R.id.text_establishment_subtitle);
        if (subtitleView != null) {
            if (establishmentName == null || establishmentName.trim().isEmpty()) {
                subtitleView.setText(R.string.establishment_content_placeholder);
            } else {
                subtitleView.setText(getString(R.string.establishment_content_placeholder_with_name,
                        establishmentName));
            }
        }

        TextView commentLabel = findViewById(R.id.text_establishment_comment_label);
        TextView commentValue = findViewById(R.id.text_establishment_comment);
        if (commentLabel != null && commentValue != null) {
            if (establishmentName != null && !establishmentName.trim().isEmpty()) {
                commentLabel.setText(getString(R.string.establishment_content_comment_label_with_name,
                        establishmentName));
            }
            String comment = intent != null
                    ? intent.getStringExtra(EXTRA_ESTABLISHMENT_COMMENT)
                    : null;
            if (comment == null || comment.trim().isEmpty()) {
                commentValue.setText(R.string.establishment_content_comment_placeholder);
            } else {
                commentValue.setText(comment);
            }
        }

        TextView photoCountView = findViewById(R.id.text_establishment_photo_count);
        if (photoCountView != null) {
            int photoCount = intent != null
                    ? intent.getIntExtra(EXTRA_ESTABLISHMENT_PHOTO_COUNT, 0)
                    : 0;
            photoCount = Math.max(photoCount, 0);
            photoCountView.setText(getResources().getQuantityString(
                    R.plurals.establishment_content_photo_count,
                    photoCount,
                    photoCount
            ));
        }

        View addButton = findViewById(R.id.button_add_content);
        if (addButton != null) {
            addButton.setOnClickListener(view ->
                    Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());
        }
    }
}
