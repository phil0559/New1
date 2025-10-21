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

    private RoomContentItem createContainer(String name) {
        return new RoomContentItem(name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true, 0);
    }

    private RoomContentItem createAttachment(String name) {
        return new RoomContentItem(name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, false, 0);
    }
}
