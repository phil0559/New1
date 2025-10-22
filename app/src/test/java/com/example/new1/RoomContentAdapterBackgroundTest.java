package com.example.new1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import sun.misc.Unsafe;

/**
 * Vérifie le comportement des arrière-plans générés pour les conteneurs et les pièces jointes.
 */
public class RoomContentAdapterBackgroundTest {

    @Test
    public void createFramedBackground_withoutStrokeReturnsGradientDrawable() throws Exception {
        RoomContentAdapter adapter = instantiateAdapter();
        Drawable drawable = invokeCreateFramedBackground(adapter, 0xFF123456, 0f, 0f,
                8f, 8f, 0, 0xFF654321, false, false);

        assertTrue(drawable instanceof GradientDrawable);
        assertTrue("La variante sans bordure ne doit pas être masquée", !isMaskedStrokeDrawable(drawable));
    }

    @Test
    public void createFramedBackground_withStrokeKeepsMaskedStrokeMetadata() throws Exception {
        RoomContentAdapter adapter = instantiateAdapter();
        int strokeWidth = 6;
        Drawable drawable = invokeCreateFramedBackground(adapter, 0xFF123456, 12f, 12f,
                0f, 0f, strokeWidth, 0xFF654321, true, true);

        Class<?> maskedClass = getMaskedStrokeDrawableClass();
        assertTrue("Une bordure doit utiliser le masque dédié", maskedClass.isInstance(drawable));

        Field strokeField = maskedClass.getDeclaredField("strokeWidth");
        strokeField.setAccessible(true);
        assertEquals("La largeur de bordure doit être conservée", strokeWidth,
                strokeField.getInt(drawable));
    }

    private boolean isMaskedStrokeDrawable(Drawable drawable) throws ClassNotFoundException {
        Class<?> maskedClass = getMaskedStrokeDrawableClass();
        return maskedClass.isInstance(drawable);
    }

    private Class<?> getMaskedStrokeDrawableClass() throws ClassNotFoundException {
        return Class.forName("com.example.new1.RoomContentAdapter$MaskedStrokeDrawable");
    }

    private Drawable invokeCreateFramedBackground(RoomContentAdapter adapter, int backgroundColor,
            float topLeft, float topRight, float bottomRight, float bottomLeft, int strokeWidth,
            int strokeColor, boolean hideTopStroke, boolean hideBottomStroke) throws Exception {
        Method method = RoomContentAdapter.class.getDeclaredMethod("createFramedBackground",
                int.class, float.class, float.class, float.class, float.class, int.class, int.class,
                boolean.class, boolean.class);
        method.setAccessible(true);
        return (Drawable) method.invoke(adapter, backgroundColor, topLeft, topRight, bottomRight,
                bottomLeft, strokeWidth, strokeColor, hideTopStroke, hideBottomStroke);
    }

    private RoomContentAdapter instantiateAdapter() throws Exception {
        Unsafe unsafe = getUnsafe();
        return (RoomContentAdapter) unsafe.allocateInstance(RoomContentAdapter.class);
    }

    private Unsafe getUnsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }
}
