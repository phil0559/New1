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

    @Test
    public void triMultiFonctionRespecteLOrdreDesTiroirsDeToursDeRangement() throws Exception {
        List<RoomContentItem> items = new ArrayList<>();

        RoomContentItem tour = creerConteneur("Tour");
        tour.setRank(100L);
        items.add(tour);

        RoomContentItem objetHaut = creerElement("Objet Haut", "TypeB");
        objetHaut.setRank(101L);
        objetHaut.setParentRank(tour.getRank());
        items.add(objetHaut);

        RoomContentItem tiroirColonneUnTypeB = creerElement("Objet Colonne 1B", "TypeB");
        tiroirColonneUnTypeB.setRank(102L);
        tiroirColonneUnTypeB.setParentRank(tour.getRank());
        tiroirColonneUnTypeB.setContainerColumn(1);
        tiroirColonneUnTypeB.setContainerLevel(1);
        items.add(tiroirColonneUnTypeB);

        RoomContentItem tiroirColonneDeuxTypeA = creerElement("Objet Colonne 2A", "TypeA");
        tiroirColonneDeuxTypeA.setRank(103L);
        tiroirColonneDeuxTypeA.setParentRank(tour.getRank());
        tiroirColonneDeuxTypeA.setContainerColumn(2);
        tiroirColonneDeuxTypeA.setContainerLevel(1);
        items.add(tiroirColonneDeuxTypeA);

        RoomContentItem tiroirColonneUnTypeA = creerElement("Objet Colonne 1A", "TypeA");
        tiroirColonneUnTypeA.setRank(104L);
        tiroirColonneUnTypeA.setParentRank(tour.getRank());
        tiroirColonneUnTypeA.setContainerColumn(1);
        tiroirColonneUnTypeA.setContainerLevel(1);
        items.add(tiroirColonneUnTypeA);

        Method method = RoomContentActivity.class.getDeclaredMethod("sortRoomContentItems", List.class);
        method.setAccessible(true);
        method.invoke(instancierActivite(), items);

        assertEquals("Tour", items.get(0).getName());
        assertEquals("Objet Haut", items.get(1).getName());
        assertEquals("Objet Colonne 1A", items.get(2).getName());
        assertEquals("Objet Colonne 1B", items.get(3).getName());
        assertEquals("Objet Colonne 2A", items.get(4).getName());
    }

    @Test
    public void triMultiFonctionRespecteLesNiveauxDeMobiliers() throws Exception {
        List<RoomContentItem> items = new ArrayList<>();

        RoomContentItem meuble = creerConteneur("Meuble");
        meuble.setRank(200L);
        items.add(meuble);

        RoomContentItem niveauDeux = creerElement("Niveau 2B", "TypeB");
        niveauDeux.setRank(201L);
        niveauDeux.setParentRank(meuble.getRank());
        niveauDeux.setContainerLevel(2);
        items.add(niveauDeux);

        RoomContentItem niveauUnB = creerElement("Niveau 1B", "TypeB");
        niveauUnB.setRank(202L);
        niveauUnB.setParentRank(meuble.getRank());
        niveauUnB.setContainerLevel(1);
        items.add(niveauUnB);

        RoomContentItem niveauUnA = creerElement("Niveau 1A", "TypeA");
        niveauUnA.setRank(203L);
        niveauUnA.setParentRank(meuble.getRank());
        niveauUnA.setContainerLevel(1);
        items.add(niveauUnA);

        Method method = RoomContentActivity.class.getDeclaredMethod("sortRoomContentItems", List.class);
        method.setAccessible(true);
        method.invoke(instancierActivite(), items);

        assertEquals("Meuble", items.get(0).getName());
        assertEquals("Niveau 1A", items.get(1).getName());
        assertEquals("Niveau 1B", items.get(2).getName());
        assertEquals("Niveau 2B", items.get(3).getName());
    }

    @Test
    public void triMultiFonctionTrieLesElementsDansLesContenantsSimples() throws Exception {
        List<RoomContentItem> items = new ArrayList<>();

        RoomContentItem boite = creerConteneur("Boîte");
        boite.setRank(300L);
        items.add(boite);

        RoomContentItem elementTypeB = creerElement("Element B", "TypeB");
        elementTypeB.setRank(301L);
        elementTypeB.setParentRank(boite.getRank());
        items.add(elementTypeB);

        RoomContentItem elementTypeA = creerElement("Element A", "TypeA");
        elementTypeA.setRank(302L);
        elementTypeA.setParentRank(boite.getRank());
        items.add(elementTypeA);

        Method method = RoomContentActivity.class.getDeclaredMethod("sortRoomContentItems", List.class);
        method.setAccessible(true);
        method.invoke(instancierActivite(), items);

        assertEquals("Boîte", items.get(0).getName());
        assertEquals("Element A", items.get(1).getName());
        assertEquals("Element B", items.get(2).getName());
    }

    private RoomContentItem creerConteneur(String nom) {
        return new RoomContentItem(nom, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true, 0);
    }

    private RoomContentItem creerElement(String nom) {
        return new RoomContentItem(nom, null, null, null, null, null, null, null, null,
                null, null, null, null, null, false, 0);
    }

    private RoomContentItem creerElement(String nom, String type) {
        return new RoomContentItem(nom, null, type, null, null, null, null, null, null,
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
