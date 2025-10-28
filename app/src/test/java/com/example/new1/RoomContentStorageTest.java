package com.example.new1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class RoomContentStorageTest {

    private SharedPreferences preferences;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        preferences = context.getSharedPreferences(RoomContentStorage.PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }

    @Test
    public void buildKeyGeneratesDistinctValuesForSimilarNames() {
        String keyWithSymbol = RoomContentStorage.buildKey("Salle #1", "Salon");
        String keyWithoutSymbol = RoomContentStorage.buildKey("Salle 1", "Salon");

        assertNotEquals("Les clés doivent différer pour éviter les collisions.",
                keyWithoutSymbol,
                keyWithSymbol);
    }

    @Test
    public void ensureCanonicalKeyMigratesLegacyEntry() {
        String legacyKey = "room_content_Salle__1_Salon";
        preferences.edit().putString(legacyKey, "legacy-value").commit();

        String resolvedKey = RoomContentStorage.resolveKey(preferences, "Salle #1", "Salon");
        assertEquals("La résolution doit d'abord retourner la clé héritée.", legacyKey, resolvedKey);

        RoomContentStorage.ensureCanonicalKey(preferences, "Salle #1", "Salon", resolvedKey);

        String canonicalKey = RoomContentStorage.buildKey("Salle #1", "Salon");
        assertEquals("La valeur doit être migrée vers la clé canonique.",
                "legacy-value",
                preferences.getString(canonicalKey, null));
        assertFalse("La clé héritée doit être supprimée après migration.",
                preferences.contains(legacyKey));
    }
}
