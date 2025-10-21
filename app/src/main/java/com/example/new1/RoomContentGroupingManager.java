package com.example.new1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Gestionnaire de hiérarchie pour les éléments de pièces.
 * <p>
 * La représentation persistée du contenu d'une pièce est une liste aplatie :
 * chaque conteneur annonce combien d'éléments lui sont directement rattachés.
 * Cette classe reconstruit une structure arborescente temporaire à partir de
 * cette liste afin de pouvoir manipuler des groupes complets (conteneur +
 * descendants) sans jamais perdre la hiérarchie initiale.
 */
final class RoomContentGroupingManager {

    private RoomContentGroupingManager() {
    }

    /**
     * Extrait le groupe situé à la position demandée. Le groupe retourné
     * contient le conteneur initial ainsi que tous ses descendants directs ou
     * indirects. Pour un élément simple, la liste ne contient qu'une entrée.
     */
    @NonNull
    static List<RoomContentItem> extractGroup(@NonNull List<RoomContentItem> items, int position) {
        HierarchyGroup group = parseGroup(items, position);
        if (group == null) {
            return new ArrayList<>();
        }
        return group.flatten();
    }

    /**
     * Calcule la taille (en nombre d'entrées dans la liste aplatie) du groupe
     * présent à la position fournie. Retourne {@code 0} si la position est
     * invalide.
     */
    static int computeGroupSize(@NonNull List<RoomContentItem> items, int position) {
        HierarchyGroup group = parseGroup(items, position);
        return group == null ? 0 : group.size();
    }

    /**
     * Supprime de la liste toutes les entrées associées au groupe situé à la
     * position fournie.
     */
    static void removeGroup(@NonNull List<RoomContentItem> items, int position) {
        HierarchyGroup group = parseGroup(items, position);
        if (group == null) {
            return;
        }
        int size = group.size();
        for (int i = 0; i < size && position < items.size(); i++) {
            items.remove(position);
        }
    }

    /**
     * Trie la liste aplatie sans dissocier les groupes hiérarchiques : chaque
     * conteneur conserve ses éléments pendant le tri.
     */
    static void sortWithComparator(@NonNull List<RoomContentItem> items,
            @NonNull Comparator<RoomContentItem> comparator) {
        if (items.size() <= 1) {
            return;
        }
        List<HierarchyGroup> groups = parseAllGroups(items);
        groups.sort((first, second) -> comparator.compare(first.root, second.root));
        items.clear();
        for (HierarchyGroup group : groups) {
            items.addAll(group.flatten());
        }
    }

    /**
     * Produit une description textuelle de la hiérarchie, pratique pour
     * afficher rapidement l'état des conteneurs lors d'un scénario de
     * déplacements.
     */
    @NonNull
    static List<String> describeHierarchy(@NonNull List<RoomContentItem> items) {
        List<String> lines = new ArrayList<>();
        List<HierarchyGroup> groups = parseAllGroups(items);
        for (HierarchyGroup group : groups) {
            group.describe(lines, 0);
        }
        return lines;
    }

    /**
     * Construit la liste complète des groupes (racines successives dans la
     * liste aplatie).
     */
    @NonNull
    private static List<HierarchyGroup> parseAllGroups(@NonNull List<RoomContentItem> items) {
        List<HierarchyGroup> groups = new ArrayList<>();
        int cursor = 0;
        while (cursor < items.size()) {
            HierarchyGroup group = consumeGroup(items, cursor);
            if (group == null) {
                // Données incohérentes : considérer l'entrée actuelle comme un
                // élément isolé afin de ne pas bloquer la manipulation.
                RoomContentItem fallback = items.get(cursor);
                group = HierarchyGroup.single(fallback);
                cursor++;
            } else {
                cursor = group.endIndex;
            }
            groups.add(group);
        }
        return groups;
    }

    @Nullable
    private static HierarchyGroup parseGroup(@NonNull List<RoomContentItem> items, int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }
        HierarchyGroup group = consumeGroup(items, position);
        if (group == null || group.startIndex != position) {
            return null;
        }
        return group;
    }

    @Nullable
    private static HierarchyGroup consumeGroup(@NonNull List<RoomContentItem> items, int start) {
        if (start < 0 || start >= items.size()) {
            return null;
        }
        int[] cursor = new int[] { start };
        HierarchyGroup group = consumeGroupRecursive(items, cursor);
        if (group == null) {
            return null;
        }
        group.startIndex = start;
        group.endIndex = cursor[0];
        return group;
    }

    @Nullable
    private static HierarchyGroup consumeGroupRecursive(@NonNull List<RoomContentItem> items,
            @NonNull int[] cursor) {
        if (cursor[0] < 0 || cursor[0] >= items.size()) {
            return null;
        }
        RoomContentItem current = items.get(cursor[0]);
        cursor[0]++;
        HierarchyGroup group = new HierarchyGroup(current);
        if (!current.isContainer()) {
            return group;
        }
        int announcedChildren = Math.max(0, current.getAttachedItemCount());
        for (int i = 0; i < announcedChildren; i++) {
            if (cursor[0] >= items.size()) {
                break;
            }
            HierarchyGroup child = consumeGroupRecursive(items, cursor);
            if (child == null) {
                break;
            }
            group.children.add(child);
        }
        return group;
    }

    private static final class HierarchyGroup {
        final RoomContentItem root;
        final List<HierarchyGroup> children = new ArrayList<>();
        int startIndex;
        int endIndex;

        HierarchyGroup(@NonNull RoomContentItem root) {
            this.root = root;
        }

        static HierarchyGroup single(@NonNull RoomContentItem item) {
            HierarchyGroup group = new HierarchyGroup(item);
            group.startIndex = -1;
            group.endIndex = -1;
            return group;
        }

        int size() {
            int result = 1;
            for (HierarchyGroup child : children) {
                result += child.size();
            }
            return result;
        }

        @NonNull
        List<RoomContentItem> flatten() {
            List<RoomContentItem> result = new ArrayList<>(size());
            collect(result);
            return result;
        }

        void collect(@NonNull List<RoomContentItem> destination) {
            destination.add(root);
            for (HierarchyGroup child : children) {
                child.collect(destination);
            }
        }

        void describe(@NonNull List<String> destination, int depth) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                line.append("  ");
            }
            if (root.isContainer()) {
                line.append(root.getName())
                        .append(" (conteneur, enfants=")
                        .append(Math.max(0, root.getAttachedItemCount()))
                        .append(")");
            } else {
                line.append(root.getName()).append(" (élément)");
            }
            destination.add(line.toString());
            for (HierarchyGroup child : children) {
                child.describe(destination, depth + 1);
            }
        }
    }
}
