package com.example.new1;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class RoomContentHierarchyHelperTest {

    @Test
    public void emptyAttachedContainerReceivesDisplayRank() {
        RoomContentItem parent = createContainer("Parent");
        parent.setRank(1L);

        RoomContentItem childContainer = createContainer("Child");
        childContainer.setRank(2L);
        childContainer.setParentRank(parent.getRank());

        RoomContentItem attachment = createAttachment("Attachment");
        attachment.setRank(3L);
        attachment.setParentRank(parent.getRank());

        List<RoomContentItem> items = new ArrayList<>();
        items.add(parent);
        items.add(childContainer);
        items.add(attachment);

        RoomContentHierarchyHelper.normalizeHierarchy(items);

        assertEquals("1", parent.getDisplayRank());
        assertEquals("1.1", childContainer.getDisplayRank());
        assertEquals("1.2", attachment.getDisplayRank());
    }

    @Test
    public void displayRanksFollowCurrentOrderingInsteadOfStoredRanks() {
        RoomContentItem parent = createContainer("Parent");
        parent.setRank(1L);

        RoomContentItem firstChild = createContainer("Conteneur C");
        firstChild.setRank(30L);
        firstChild.setParentRank(parent.getRank());

        RoomContentItem secondChild = createContainer("Conteneur B");
        secondChild.setRank(20L);
        secondChild.setParentRank(parent.getRank());

        List<RoomContentItem> items = new ArrayList<>();
        items.add(parent);
        items.add(firstChild);
        items.add(secondChild);

        RoomContentHierarchyHelper.normalizeHierarchy(items);

        assertEquals("1", parent.getDisplayRank());
        assertEquals("1.1", firstChild.getDisplayRank());
        assertEquals("1.2", secondChild.getDisplayRank());
    }

    @Test
    public void normalizeHierarchyPopulatesChildrenRelationships() {
        RoomContentItem furniture = createContainer("Bibliothèque");
        furniture.setRank(10L);

        RoomContentItem topLevelAttachment = createAttachment("Livre A");
        topLevelAttachment.setRank(20L);
        topLevelAttachment.setParentRank(furniture.getRank());

        RoomContentItem shelf = createContainer("Étagère 1");
        shelf.setRank(30L);
        shelf.setParentRank(furniture.getRank());

        RoomContentItem nestedAttachment = createAttachment("Livre B");
        nestedAttachment.setRank(40L);
        nestedAttachment.setParentRank(shelf.getRank());

        List<RoomContentItem> items = new ArrayList<>();
        items.add(furniture);
        items.add(topLevelAttachment);
        items.add(shelf);
        items.add(nestedAttachment);

        RoomContentHierarchyHelper.normalizeHierarchy(items);

        List<RoomContentItem> furnitureChildren = furniture.getChildren();
        assertEquals(2, furnitureChildren.size());
        assertEquals(topLevelAttachment, furnitureChildren.get(0));
        assertEquals(shelf, furnitureChildren.get(1));

        List<RoomContentItem> shelfChildren = shelf.getChildren();
        assertEquals(1, shelfChildren.size());
        assertEquals(nestedAttachment, shelfChildren.get(0));
    }

    private RoomContentItem createContainer(String name) {
        return new RoomContentItem(name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true, 0);
    }

    private RoomContentItem createAttachment(String name) {
        return new RoomContentItem(name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, false, 0);
    }
}
