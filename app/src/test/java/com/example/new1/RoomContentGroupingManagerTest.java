package com.example.new1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

public class RoomContentGroupingManagerTest {

    @Test
    public void computeGroupSizeGereLesConteneursImbriques() {
        List<RoomContentItem> items = new ArrayList<>();
        items.add(conteneur("Boîte 1", 2));
        items.add(conteneur("Boîte 1.1", 1));
        items.add(element("Objet A"));
        items.add(element("Objet B"));
        items.add(conteneur("Boîte 2", 0));

        assertEquals(4, RoomContentGroupingManager.computeGroupSize(items, 0));
        assertEquals(2, RoomContentGroupingManager.computeGroupSize(items, 1));
        assertEquals(1, RoomContentGroupingManager.computeGroupSize(items, 2));
        assertEquals(1, RoomContentGroupingManager.computeGroupSize(items, 4));
    }

    @Test
    public void scenarioTroisContenantsIllustreLesDeplacements() {
        List<RoomContentItem> items = new ArrayList<>();
        items.add(conteneur("Boîte 1", 1));
        items.add(element("Objet A"));
        items.add(conteneur("Boîte 2", 0));
        items.add(conteneur("Boîte 3", 0));

        // Vérification de l'état initial : Boîte 1 contient l'objet, le reste est vide.
        verifierDescription(RoomContentGroupingManager.describeHierarchy(items),
                "Boîte 1 (conteneur, enfants=1)",
                "  Objet A (élément)",
                "Boîte 2 (conteneur, enfants=0)",
                "Boîte 3 (conteneur, enfants=0)");

        // Étape 1 : on déplace Boîte 1 (avec son élément) dans Boîte 2.
        List<RoomContentItem> premierGroupe = RoomContentGroupingManager.extractGroup(items, 0);
        assertEquals(2, premierGroupe.size());
        assertSame(items.get(0), premierGroupe.get(0));
        RoomContentGroupingManager.removeGroup(items, 0);
        assertEquals(2, items.size());

        items.set(0, conteneur("Boîte 2", 1));
        items.addAll(1, premierGroupe);

        verifierDescription(RoomContentGroupingManager.describeHierarchy(items),
                "Boîte 2 (conteneur, enfants=1)",
                "  Boîte 1 (conteneur, enfants=1)",
                "    Objet A (élément)",
                "Boîte 3 (conteneur, enfants=0)");

        // Étape 2 : on extrait Boîte 1 et son élément pour préparer le transfert final.
        List<RoomContentItem> sousGroupe = RoomContentGroupingManager.extractGroup(items, 1);
        assertEquals(2, sousGroupe.size());
        RoomContentGroupingManager.removeGroup(items, 1);
        assertEquals(2, items.size());

        RoomContentItem objetTransfere = sousGroupe.get(1);
        items.set(0, conteneur("Boîte 2", 1));
        items.add(1, conteneur("Boîte 1", 0));
        items.set(2, conteneur("Boîte 3", 1));
        items.add(3, objetTransfere);

        // Étape 3 : Boîte 1 reste dans Boîte 2 mais vide, l'objet est désormais dans Boîte 3.
        verifierDescription(RoomContentGroupingManager.describeHierarchy(items),
                "Boîte 2 (conteneur, enfants=1)",
                "  Boîte 1 (conteneur, enfants=0)",
                "Boîte 3 (conteneur, enfants=1)",
                "  Objet A (élément)");

        // Une passe de tri doit conserver les groupes intacts.
        RoomContentGroupingManager.sortWithComparator(items, creerComparateur());
        verifierDescription(RoomContentGroupingManager.describeHierarchy(items),
                "Boîte 2 (conteneur, enfants=1)",
                "  Boîte 1 (conteneur, enfants=0)",
                "Boîte 3 (conteneur, enfants=1)",
                "  Objet A (élément)");
    }

    private Comparator<RoomContentItem> creerComparateur() {
        return new Comparator<RoomContentItem>() {
            @Override
            public int compare(RoomContentItem first, RoomContentItem second) {
                String firstType = normaliser(first.getType());
                String secondType = normaliser(second.getType());
                boolean firstHasType = !firstType.isEmpty();
                boolean secondHasType = !secondType.isEmpty();
                if (firstHasType && secondHasType) {
                    int typeComparison = firstType.compareToIgnoreCase(secondType);
                    if (typeComparison != 0) {
                        return typeComparison;
                    }
                } else if (firstHasType) {
                    return -1;
                } else if (secondHasType) {
                    return 1;
                }
                String firstName = normaliser(first.getName());
                String secondName = normaliser(second.getName());
                return firstName.compareToIgnoreCase(secondName);
            }
        };
    }

    private String normaliser(String value) {
        return value == null ? "" : value.trim();
    }

    private RoomContentItem conteneur(String nom, int enfants) {
        return new RoomContentItem(nom, null, null, null, null, null, null, null, null, null,
                null, null, null, null, true, enfants);
    }

    private RoomContentItem element(String nom) {
        return new RoomContentItem(nom, null, null, null, null, null, null, null, null, null,
                null, null, null, null, false, 0);
    }

    private void verifierDescription(List<String> description, String... lignesAttendue) {
        assertEquals("Nombre de lignes inattendu", lignesAttendue.length, description.size());
        for (int i = 0; i < lignesAttendue.length; i++) {
            assertEquals("Ligne " + i, lignesAttendue[i], description.get(i));
        }
    }
}
