package com.example.new1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public final class LocaleHelper {
    private static final String PREFS = "locale_settings";
    private static final String KEY_LANGUAGE = "language";

    private LocaleHelper() {
    }

    public static Context apply(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String tag = prefs.getString(KEY_LANGUAGE, null);
        if (tag == null || tag.isEmpty()) {
            return context;
        }
        Locale locale = Locale.forLanguageTag(tag);
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

    public static void persistLanguage(Context context, String tag) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, tag)
            .apply();
    }

    public static String currentLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, null);
    }
}
