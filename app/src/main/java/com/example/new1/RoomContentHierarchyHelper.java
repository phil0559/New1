package com.example.new1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    static void attachToContainer(@NonNull RoomContentItem item,
            @Nullable RoomContentItem container) {
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
