package com.example.new1;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * Application qui propage systématiquement la langue choisie aux ressources globales.
 */
public class New1Application extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.apply(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.apply(this);
    }
}
