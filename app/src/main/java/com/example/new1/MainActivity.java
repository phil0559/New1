package com.example.new1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private PopupWindow languagePopup;
    private ImageView flagIcon;
    private ImageView displayToggleIcon;
    private View menuBanner;
    private View menuIcon;
    private View menuTitle;
    private View menuSearchIcon;
    private View menuSpacer;
    private View flagContainer;
    private View actionScrollView;
    private int menuPaddingLeft;
    private int menuPaddingTop;
    private int menuPaddingRight;
    private int menuPaddingBottom;
    private float menuBannerElevation;
    private DisplayState displayState = DisplayState.ALL_VISIBLE;

    private enum DisplayState {
        ALL_VISIBLE,
        ELEMENTS_HIDDEN,
        CONTAINERS_HIDDEN
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.apply(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuBanner = findViewById(R.id.menu_banner);
        menuIcon = findViewById(R.id.menu_icon);
        menuTitle = findViewById(R.id.menu_title);
        menuSearchIcon = findViewById(R.id.menu_search_icon);
        menuSpacer = findViewById(R.id.menu_spacer);
        flagContainer = findViewById(R.id.flag_container);
        actionScrollView = findViewById(R.id.action_scroll_view);

        menuPaddingLeft = menuBanner.getPaddingLeft();
        menuPaddingTop = menuBanner.getPaddingTop();
        menuPaddingRight = menuBanner.getPaddingRight();
        menuPaddingBottom = menuBanner.getPaddingBottom();
        menuBannerElevation = menuBanner.getElevation();

        flagContainer.setOnClickListener(this::toggleLanguagePopup);

        flagIcon = findViewById(R.id.selected_flag_icon);
        updateFlagIconForCurrentLocale();

        displayToggleIcon = findViewById(R.id.display_toggle_icon);
        displayToggleIcon.setOnClickListener(view -> cycleDisplayState());
        applyDisplayState();

        View establishmentButton = findViewById(R.id.button_establishment);
        establishmentButton.setOnClickListener(view ->
            startActivity(new Intent(this, EstablishmentActivity.class))
        );

        View quitButton = findViewById(R.id.button_quit);
        quitButton.setOnClickListener(view -> finishAffinity());

        requestCameraPermissionIfFirstLaunch();
    }

    @Override
    protected void onDestroy() {
        if (languagePopup != null) {
            languagePopup.dismiss();
            languagePopup = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFlagIconForCurrentLocale();
    }

    private void toggleLanguagePopup(View anchor) {
        setDisplayState(DisplayState.ALL_VISIBLE);
        if (languagePopup != null && languagePopup.isShowing()) {
            languagePopup.dismiss();
            return;
        }

        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_language_selector, null);
        PopupWindow popupWindow = new PopupWindow(
            contentView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        );

        popupWindow.setElevation(12f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(
            ContextCompat.getDrawable(this, R.drawable.bg_language_popup)
        );
        popupWindow.setOnDismissListener(() -> languagePopup = null);

        contentView.findViewById(R.id.flag_fr).setOnClickListener(view -> applyLanguageSelection("fr"));
        contentView.findViewById(R.id.flag_en).setOnClickListener(view -> applyLanguageSelection("en-GB"));
        contentView.findViewById(R.id.flag_de).setOnClickListener(view -> applyLanguageSelection("de"));
        contentView.findViewById(R.id.flag_es).setOnClickListener(view -> applyLanguageSelection("es"));

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = contentView.getMeasuredWidth();
        int anchorWidth = anchor.getWidth();
        int xOffset = anchorWidth - popupWidth;
        int yOffset = (int) Math.round(8 * getResources().getDisplayMetrics().density);

        popupWindow.showAsDropDown(anchor, xOffset, yOffset);
        languagePopup = popupWindow;
    }

    private void applyLanguageSelection(String languageTag) {
        LocaleHelper.persistLanguage(this, languageTag);
        flagIcon.setImageResource(flagIconForTag(languageTag));
        if (languagePopup != null) {
            languagePopup.dismiss();
        }
        recreate();
    }

    private void updateFlagIconForCurrentLocale() {
        String storedLanguage = LocaleHelper.currentLanguage(this);
        String languageTag;
        if (storedLanguage != null && !storedLanguage.isEmpty()) {
            languageTag = storedLanguage;
        } else {
            Locale locale = getResources().getConfiguration().getLocales().get(0);
            languageTag = locale.toLanguageTag();
        }
        flagIcon.setImageResource(flagIconForTag(languageTag));
    }

    private int flagIconForTag(String languageTag) {
        String normalized = languageTag.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("en")) {
            return R.drawable.ic_flag_united_kingdom;
        }
        if (normalized.startsWith("de")) {
            return R.drawable.ic_flag_germany;
        }
        if (normalized.startsWith("es")) {
            return R.drawable.ic_flag_spain;
        }
        return R.drawable.ic_flag_france;
    }

    private void requestCameraPermissionIfFirstLaunch() {
        SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        boolean alreadyRequested = preferences.getBoolean("camera_permission_requested", false);
        if (!alreadyRequested) {
            preferences.edit().putBoolean("camera_permission_requested", true).apply();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        }
    }

    private void cycleDisplayState() {
        if (displayState == DisplayState.ALL_VISIBLE) {
            setDisplayState(DisplayState.ELEMENTS_HIDDEN);
        } else if (displayState == DisplayState.ELEMENTS_HIDDEN) {
            setDisplayState(DisplayState.CONTAINERS_HIDDEN);
        } else {
            setDisplayState(DisplayState.ALL_VISIBLE);
        }
    }

    private void setDisplayState(DisplayState newState) {
        displayState = newState;
        applyDisplayState();
    }

    private void applyDisplayState() {
        switch (displayState) {
            case ALL_VISIBLE:
                // Afficher tous les composants avec l’icône neutre.
                displayToggleIcon.setImageResource(R.drawable.ic_square_empty);
                menuBanner.setBackgroundResource(R.drawable.bg_menu_banner);
                menuBanner.setElevation(menuBannerElevation);
                menuBanner.setPadding(menuPaddingLeft, menuPaddingTop, menuPaddingRight, menuPaddingBottom);
                setMenuChildrenVisibility(View.VISIBLE);
                actionScrollView.setVisibility(View.VISIBLE);
                break;
            case ELEMENTS_HIDDEN:
                // Masquer les éléments principaux tout en conservant le bandeau.
                displayToggleIcon.setImageResource(R.drawable.ic_square_diagonal);
                menuBanner.setBackgroundResource(R.drawable.bg_menu_banner);
                menuBanner.setElevation(menuBannerElevation);
                menuBanner.setPadding(menuPaddingLeft, menuPaddingTop, menuPaddingRight, menuPaddingBottom);
                setMenuChildrenVisibility(View.VISIBLE);
                actionScrollView.setVisibility(View.GONE);
                break;
            case CONTAINERS_HIDDEN:
                // Camoufler les bandeaux en ne laissant que le bouton de contrôle.
                displayToggleIcon.setImageResource(R.drawable.ic_square_cross);
                menuBanner.setBackground(null);
                menuBanner.setElevation(0f);
                menuBanner.setPadding(0, 0, 0, 0);
                setMenuChildrenVisibility(View.GONE);
                actionScrollView.setVisibility(View.GONE);
                break;
        }
    }

    private void setMenuChildrenVisibility(int visibility) {
        menuIcon.setVisibility(visibility);
        menuTitle.setVisibility(visibility);
        menuSearchIcon.setVisibility(visibility);
        menuSpacer.setVisibility(visibility);
        flagContainer.setVisibility(visibility);
    }
}
