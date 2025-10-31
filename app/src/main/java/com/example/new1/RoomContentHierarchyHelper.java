package com.example.new1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utilitaire chargé de maintenir la cohérence hiérarchique entre éléments et contenants.
 */
final class RoomContentHierarchyHelper {

    private RoomContentHierarchyHelper() {
    }

    static void normalizeHierarchy(@NonNull List<RoomContentItem> items) {
        ensureRanks(items);
        sanitizeParentLinks(items);
        rebuildAttachedCounts(items);
        rebuildChildrenRelationships(items);
        assignDisplayRanks(items);
    }

    static void ensureRanks(@NonNull List<RoomContentItem> items) {
        Set<Long> usedRanks = new HashSet<>();
        for (RoomContentItem item : items) {
            long rank = item.getRank();
            if (rank >= 0 && !usedRanks.add(rank)) {
                item.setRank(-1L);
            }
        }
        for (RoomContentItem item : items) {
            if (item.getRank() >= 0) {
                continue;
            }
            long generated = generateUniqueRank(usedRanks);
            item.setRank(generated);
            usedRanks.add(generated);
        }
    }

    static void sanitizeParentLinks(@NonNull List<RoomContentItem> items) {
        Map<Long, RoomContentItem> byRank = new HashMap<>();
        for (RoomContentItem item : items) {
            byRank.put(item.getRank(), item);
        }
        for (RoomContentItem item : items) {
            if (item.isStorageTower()) {
                item.setParentRank(null);
                continue;
            }
            Long parentRank = item.getParentRank();
            if (parentRank == null) {
                continue;
            }
            RoomContentItem parent = byRank.get(parentRank);
            if (parent == null || !parent.isContainer() || parent == item) {
                item.setParentRank(null);
            }
        }
    }

    static void rebuildAttachedCounts(@NonNull List<RoomContentItem> items) {
        Map<Long, RoomContentItem> byRank = new HashMap<>();
        for (RoomContentItem item : items) {
            item.setAttachedItemCount(0);
            byRank.put(item.getRank(), item);
        }
        for (RoomContentItem item : items) {
            Long parentRank = item.getParentRank();
            if (parentRank == null) {
                continue;
            }
            RoomContentItem parent = byRank.get(parentRank);
            if (parent == null) {
                continue;
            }
            parent.incrementAttachedItemCount();
        }
    }

    private static void rebuildChildrenRelationships(@NonNull List<RoomContentItem> items) {
        Map<Long, RoomContentItem> byRank = new HashMap<>();
        for (RoomContentItem item : items) {
            item.clearChildren();
            byRank.put(item.getRank(), item);
        }
        for (RoomContentItem item : items) {
            Long parentRank = item.getParentRank();
            if (parentRank == null) {
                continue;
            }
            RoomContentItem parent = byRank.get(parentRank);
            if (parent == null || parent == item || !parent.isContainer()) {
                continue;
            }
            parent.addChild(item);
        }
    }

    static void attachToContainer(@NonNull RoomContentItem item,
            @Nullable RoomContentItem container) {
        if (item.isStorageTower()) {
            // Les tours de rangement restent toujours au niveau principal.
            item.setParentRank(null);
            return;
        }
        if (container == null) {
            item.setParentRank(null);
            return;
        }
        if (!container.isContainer()) {
            item.setParentRank(null);
            return;
        }
        item.setParentRank(container.getRank());
    }

    private static void assignDisplayRanks(@NonNull List<RoomContentItem> items) {
        Map<Long, RoomContentItem> byRank = new HashMap<>();
        Map<Long, List<RoomContentItem>> childrenByParent = new HashMap<>();
        for (RoomContentItem item : items) {
            item.setDisplayRank(null);
            byRank.put(item.getRank(), item);
        }
        for (RoomContentItem item : items) {
            Long parentRank = item.getParentRank();
            if (parentRank == null) {
                continue;
            }
            RoomContentItem parent = byRank.get(parentRank);
            if (parent == null) {
                continue;
            }
            List<RoomContentItem> siblings = childrenByParent.get(parentRank);
            if (siblings == null) {
                siblings = new ArrayList<>();
                childrenByParent.put(parentRank, siblings);
            }
            siblings.add(item);
        }

        List<RoomContentItem> topLevelContainers = new ArrayList<>();
        Set<Long> seenTopLevelRanks = new HashSet<>();
        for (RoomContentItem item : items) {
            if (!item.isContainer()) {
                continue;
            }
            if (item.getParentRank() != null) {
                continue;
            }
            if (!seenTopLevelRanks.add(item.getRank())) {
                continue;
            }
            topLevelContainers.add(item);
        }

        int index = 1;
        for (RoomContentItem container : topLevelContainers) {
            if (!isRankable(container, childrenByParent)) {
                container.setDisplayRank(null);
                continue;
            }
            String label = String.valueOf(index);
            index++;
            container.setDisplayRank(label);
            assignDisplayRanksRecursively(container, label, childrenByParent);
        }
    }

    private static void assignDisplayRanksRecursively(@NonNull RoomContentItem parent,
            @NonNull String parentLabel,
            @NonNull Map<Long, List<RoomContentItem>> childrenByParent) {
        List<RoomContentItem> children = childrenByParent.get(parent.getRank());
        if (children == null || children.isEmpty()) {
            return;
        }
        int index = 1;
        for (RoomContentItem child : children) {
            if (!isRankable(child, childrenByParent)) {
                child.setDisplayRank(null);
                continue;
            }
            String label = parentLabel + "." + index;
            index++;
            child.setDisplayRank(label);
            if (child.isContainer()) {
                assignDisplayRanksRecursively(child, label, childrenByParent);
            }
        }
    }

    private static boolean isRankable(@NonNull RoomContentItem item,
            @NonNull Map<Long, List<RoomContentItem>> childrenByParent) {
        if (!item.isContainer()) {
            return true;
        }
        List<RoomContentItem> children = childrenByParent.get(item.getRank());
        if (children == null || children.isEmpty()) {
            // Conserver le rang des conteneurs même lorsqu'ils sont vides afin
            // d'éviter les "trous" dans la numérotation. Cela garantit que
            // le déplacement d'un élément ne modifie pas rétroactivement le
            // rang de son ancien conteneur encore présent dans la hiérarchie.
            return true;
        }
        return hasRankableDescendants(item, childrenByParent);
    }

    private static boolean hasRankableDescendants(@NonNull RoomContentItem item,
            @NonNull Map<Long, List<RoomContentItem>> childrenByParent) {
        List<RoomContentItem> children = childrenByParent.get(item.getRank());
        if (children == null || children.isEmpty()) {
            return false;
        }
        for (RoomContentItem child : children) {
            if (!child.isContainer()) {
                return true;
            }
            if (hasRankableDescendants(child, childrenByParent)) {
                return true;
            }
        }
        return false;
    }

    private static long generateUniqueRank(@NonNull Set<Long> usedRanks) {
        long candidate;
        do {
            // Utiliser une borne supérieure garantit des valeurs toujours positives
            // et évite le cas particulier de Long.MIN_VALUE qui restait négatif après
            // Math.abs, ce qui cassait la normalisation des rangs.
            candidate = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
        } while (usedRanks.contains(candidate));
        return candidate;
    }

}
