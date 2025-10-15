package com.example.new1;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.Nullable;

/**
 * Utilitaires pour le traitement simple des images capturées dans l'application.
 */
public final class ImageProcessingUtils {
    private ImageProcessingUtils() {
    }

    /**
     * Détecte la zone de l'objet posé sur un fond blanc et en extrait le rectangle englobant.
     * <p>
     * L'algorithme est volontairement simple : il parcourt chaque pixel et cherche la zone où
     * l'intensité est suffisamment différente du blanc pur. Si aucune zone n'est détectée, la
     * bitmap d'origine est renvoyée.
     *
     * @param source bitmap source, telle que renvoyée par l'application appareil photo.
     * @return une nouvelle bitmap recadrée, ou {@code source} si aucun objet n'a été détecté.
     */
    @Nullable
    public static Bitmap cropObjectOnWhiteBackground(@Nullable Bitmap source) {
        if (source == null) {
            return null;
        }

        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= 0 || height <= 0) {
            return source;
        }

        // Seuil d'intensité : plus il est bas, plus la détection est sensible aux variations.
        final int luminanceThreshold = 245;

        int left = width;
        int right = -1;
        int top = height;
        int bottom = -1;

        int[] rowBuffer = new int[width];
        for (int y = 0; y < height; y++) {
            source.getPixels(rowBuffer, 0, width, 0, y, width, 1);
            for (int x = 0; x < width; x++) {
                int color = rowBuffer[x];
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);

                int maxComponent = Math.max(red, Math.max(green, blue));
                if (maxComponent < luminanceThreshold) {
                    if (x < left) {
                        left = x;
                    }
                    if (x > right) {
                        right = x;
                    }
                    if (y < top) {
                        top = y;
                    }
                    if (y > bottom) {
                        bottom = y;
                    }
                }
            }
        }

        if (right < left || bottom < top) {
            return source;
        }

        int cropWidth = right - left + 1;
        int cropHeight = bottom - top + 1;
        try {
            return Bitmap.createBitmap(source, left, top, cropWidth, cropHeight);
        } catch (IllegalArgumentException e) {
            // En cas d'erreur inattendue (indices hors limites), on revient à la bitmap initiale.
            return source;
        }
    }
}
