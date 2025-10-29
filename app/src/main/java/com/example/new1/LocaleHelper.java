package com.example.new1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import androidx.annotation.Nullable;

import java.util.IllformedLocaleException;
import java.util.Locale;

public final class LocaleHelper {
    private static final String PREFS = "locale_settings";
    private static final String KEY_LANGUAGE = "language";

    private LocaleHelper() {
    }

    public static Context apply(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String tag = prefs.getString(KEY_LANGUAGE, null);
        Locale locale = resolveLocale(tag);
        if (locale == null) {
            if (tag != null && !tag.trim().isEmpty()) {
                prefs.edit().remove(KEY_LANGUAGE).apply();
            }
            return context;
        }
        String canonicalTag = locale.toLanguageTag();
        if (!canonicalTag.equals(tag)) {
            prefs.edit().putString(KEY_LANGUAGE, canonicalTag).apply();
        }
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            configuration.setLocales(new LocaleList(locale));
            return context.createConfigurationContext(configuration);
        } else {
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }

    @Nullable
    public static String persistLanguage(Context context, String tag) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Locale locale = resolveLocale(tag);
        if (locale == null) {
            prefs.edit().remove(KEY_LANGUAGE).apply();
            return null;
        }
        String canonicalTag = locale.toLanguageTag();
        prefs.edit().putString(KEY_LANGUAGE, canonicalTag).apply();
        return canonicalTag;
    }

    public static String currentLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String storedTag = prefs.getString(KEY_LANGUAGE, null);
        Locale locale = resolveLocale(storedTag);
        if (locale == null) {
            if (storedTag != null && !storedTag.trim().isEmpty()) {
                prefs.edit().remove(KEY_LANGUAGE).apply();
            }
            return null;
        }
        String canonicalTag = locale.toLanguageTag();
        if (!canonicalTag.equals(storedTag)) {
            prefs.edit().putString(KEY_LANGUAGE, canonicalTag).apply();
        }
        return canonicalTag;
    }

    @Nullable
    private static Locale resolveLocale(@Nullable String tag) {
        if (tag == null) {
            return null;
        }
        String trimmed = tag.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = trimmed.replace('_', '-');
        Locale locale;
        try {
            locale = Locale.forLanguageTag(normalized);
        } catch (IllformedLocaleException exception) {
            // Conversion impossible : la valeur sauvegardée n'est pas une balise BCP 47 valide.
            return null;
        }
        if (locale == null || locale.getLanguage() == null || locale.getLanguage().isEmpty()) {
            return null;
        }
        return locale;
    }
}
