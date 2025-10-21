package com.example.new1;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import sun.misc.Unsafe;

public class RoomContentActivitySortingTest {

    @Test
    public void triReordonneLesEnfantsEtRecalculeLesRangsVisuels() throws Exception {
        List<RoomContentItem> items = new ArrayList<>();

        RoomContentItem racine = creerConteneur("Racine");
        racine.setRank(1L);
        items.add(racine);

        RoomContentItem conteneurB = creerConteneur("Boîte B");
        conteneurB.setRank(2L);
        conteneurB.setParentRank(racine.getRank());
        items.add(conteneurB);

        RoomContentItem objetB = creerElement("Objet B");
        objetB.setRank(3L);
        objetB.setParentRank(conteneurB.getRank());
        items.add(objetB);

        RoomContentItem conteneurA = creerConteneur("Boîte A");
        conteneurA.setRank(4L);
        conteneurA.setParentRank(racine.getRank());
        items.add(conteneurA);

        RoomContentItem objetC = creerElement("Objet C");
        objetC.setRank(5L);
        objetC.setParentRank(racine.getRank());
        items.add(objetC);

        Method method = RoomContentActivity.class.getDeclaredMethod("sortRoomContentItems", List.class);
        method.setAccessible(true);
        method.invoke(instancierActivite(), items);

        assertEquals("Racine", items.get(0).getName());
        assertEquals("Boîte A", items.get(1).getName());
        assertEquals("Boîte B", items.get(2).getName());
        assertEquals("Objet B", items.get(3).getName());
        assertEquals("Objet C", items.get(4).getName());

        assertEquals("1", items.get(0).getDisplayRank());
        assertEquals("1.1", items.get(1).getDisplayRank());
        assertEquals("1.2", items.get(2).getDisplayRank());
        assertEquals("1.2.1", items.get(3).getDisplayRank());
        assertEquals("1.3", items.get(4).getDisplayRank());
    }

    private RoomContentItem creerConteneur(String nom) {
        return new RoomContentItem(nom, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true, 0);
    }

    private RoomContentItem creerElement(String nom) {
        return new RoomContentItem(nom, null, null, null, null, null, null, null, null,
                null, null, null, null, null, false, 0);
    }

    private RoomContentActivity instancierActivite() throws Exception {
        Unsafe unsafe = obtenirUnsafe();
        return (RoomContentActivity) unsafe.allocateInstance(RoomContentActivity.class);
    }

    private Unsafe obtenirUnsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }
}
