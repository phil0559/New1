package com.example.new1;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class RoomContentActivityMoveDialogTest {

    @Test
    public void excludedContainerRanksIncludeAncestors() {
        RoomContentItem parent = createContainer("A", 1L, null);
        RoomContentItem childContainer = createContainer("B", 2L, parent.getRank());
        RoomContentItem attachment = createAttachment("C", 3L, childContainer.getRank());

        List<RoomContentItem> items = new ArrayList<>();
        items.add(parent);
        items.add(childContainer);
        items.add(attachment);

        RoomContentHierarchyHelper.normalizeHierarchy(items);

        Set<Long> excluded = RoomContentActivity.collectExcludedContainerRanks(items, attachment, 2);

        assertTrue("Le conteneur parent direct doit être exclu", excluded.contains(childContainer.getRank()));
        assertTrue("Les ancêtres doivent être exclus", excluded.contains(parent.getRank()));
        assertTrue("L'élément lui-même reste exclu pour éviter l'auto-affectation", excluded.contains(attachment.getRank()));
    }

    private RoomContentItem createContainer(String name, long rank, Long parentRank) {
        RoomContentItem item = new RoomContentItem(name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true, 0);
        item.setRank(rank);
        item.setParentRank(parentRank);
        return item;
    }

    private RoomContentItem createAttachment(String name, long rank, Long parentRank) {
        RoomContentItem item = new RoomContentItem(name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, false, 0);
        item.setRank(rank);
        item.setParentRank(parentRank);
        return item;
    }
}
