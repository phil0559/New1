package com.example.new1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    @Test
    public void filterContainersForLevelHonorsLevels() {
        RoomContentItem furniture = RoomContentItem.createFurniture("Bibliothèque", null, null, null,
                null, 2, null, true, true);
        furniture.setRank(10L);

        RoomContentItem topBox = createContainer("Boîte haut", 11L, furniture.getRank());
        topBox.setContainerLevel(null);
        RoomContentItem levelOneBox = createContainer("Boîte niveau 1", 12L, furniture.getRank());
        levelOneBox.setContainerLevel(1);
        RoomContentItem bottomBox = createContainer("Boîte bas", 13L, furniture.getRank());
        bottomBox.setContainerLevel(RoomContentItem.FURNITURE_BOTTOM_LEVEL);
        RoomContentItem roomContainer = createContainer("Boîte pièce", 14L, null);

        List<RoomContentItem> containers = new ArrayList<>();
        containers.add(topBox);
        containers.add(levelOneBox);
        containers.add(bottomBox);
        containers.add(roomContainer);

        List<RoomContentItem> topMatches = RoomContentActivity.filterContainersForLevel(containers,
                furniture, null, null);
        assertEquals(1, topMatches.size());
        assertTrue(topMatches.contains(topBox));

        List<RoomContentItem> levelMatches = RoomContentActivity.filterContainersForLevel(containers,
                furniture, 1, null);
        assertEquals(1, levelMatches.size());
        assertTrue(levelMatches.contains(levelOneBox));

        List<RoomContentItem> bottomMatches = RoomContentActivity.filterContainersForLevel(
                containers, furniture, RoomContentItem.FURNITURE_BOTTOM_LEVEL, null);
        assertEquals(1, bottomMatches.size());
        assertTrue(bottomMatches.contains(bottomBox));

        List<RoomContentItem> roomMatches = RoomContentActivity.filterContainersForLevel(containers,
                null, null, null);
        assertEquals(1, roomMatches.size());
        assertTrue(roomMatches.contains(roomContainer));
        assertFalse(roomMatches.contains(topBox));
    }

    @Test
    public void navigationStateMaintainsStack() {
        RoomContentActivity.ContainerNavigationState state = new RoomContentActivity.ContainerNavigationState();
        RoomContentItem furniture = RoomContentItem.createFurniture("Armoire", null, null, null,
                null, 3, null, true, true);
        furniture.setRank(20L);

        assertEquals(RoomContentActivity.NavigationStage.ROOT, state.getCurrentStep().stage);
        state.enterFurniture(furniture);
        assertEquals(RoomContentActivity.NavigationStage.FURNITURE, state.getCurrentStep().stage);
        state.enterLevel(furniture, 1);
        assertEquals(RoomContentActivity.NavigationStage.LEVEL, state.getCurrentStep().stage);
        state.navigateBack();
        assertEquals(RoomContentActivity.NavigationStage.FURNITURE, state.getCurrentStep().stage);
        state.showMainLevel();
        assertEquals(RoomContentActivity.NavigationStage.LEVEL, state.getCurrentStep().stage);
        state.navigateBack();
        assertEquals(RoomContentActivity.NavigationStage.ROOT, state.getCurrentStep().stage);
        assertTrue(state.applyContext("Autre établissement", "Salle"));
        assertEquals(RoomContentActivity.NavigationStage.ROOT, state.getCurrentStep().stage);
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
