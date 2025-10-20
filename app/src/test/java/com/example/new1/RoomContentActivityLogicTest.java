package com.example.new1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.Test;

public class RoomContentActivityLogicTest {

    private static RoomContentItem createContainer(String name, int attachedCount) {
        return new RoomContentItem(name,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                attachedCount);
    }

    private static RoomContentItem createItem(String name) {
        return new RoomContentItem(name,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                0);
    }

    @Test
    public void insertIntoContainerSkipsNestedSubtrees() {
        List<RoomContentItem> items = new ArrayList<>();
        RoomContentItem containerOne = createContainer("Conteneur 1", 0);
        RoomContentItem containerTwo = createContainer("Conteneur 2", 0);
        RoomContentItem itemA = createItem("Élément A");
        RoomContentItem itemB = createItem("Élément B");
        RoomContentItem itemC = createItem("Élément C");

        items.add(containerOne);
        items.add(containerTwo);
        items.add(itemA);
        items.add(itemB);
        items.add(itemC);

        insertGroupIntoContainer(items, itemA, containerOne);
        insertGroupIntoContainer(items, itemB, containerTwo);

        moveGroup(items, findIndex(items, containerTwo), containerOne);

        int containerOneIndex = findIndex(items, containerOne);
        int containerTwoIndex = findIndex(items, containerTwo);
        int itemBIndex = findIndex(items, itemB);

        moveGroup(items, findIndex(items, itemC), containerOne);

        int itemCIndex = findIndex(items, itemC);
        assertEquals(containerOneIndex + 1, findIndex(items, itemA));
        assertEquals(containerOneIndex + 2, containerTwoIndex);
        assertEquals(containerTwoIndex + 1, itemBIndex);
        assertTrue(itemCIndex > itemBIndex);
    }

    private static int findIndex(List<RoomContentItem> items, RoomContentItem target) {
        for (int i = 0; i < items.size(); i++) {
            RoomContentItem candidate = items.get(i);
            if (candidate.getName().equals(target.getName())) {
                return i;
            }
        }
        return -1;
    }

    private static void moveGroup(List<RoomContentItem> items, int position,
            RoomContentItem targetContainer) {
        MovementGroup group = extractMovementGroup(items, position);
        adjustContainerCountForRemoval(items, position);
        removeGroupAtPosition(items, position, group.size);
        if (targetContainer != null) {
            insertGroupIntoContainer(items, group.items, targetContainer);
        }
    }

    private static MovementGroup extractMovementGroup(List<RoomContentItem> items, int position) {
        RoomContentItem current = items.get(position);
        if (current.isContainer()) {
            ContainerExtraction extraction = extractContainerGroup(items, position);
            List<RoomContentItem> groupItems = new ArrayList<>();
            groupItems.add(extraction.container);
            groupItems.addAll(extraction.attachments);
            return new MovementGroup(groupItems);
        }
        List<RoomContentItem> singleton = new ArrayList<>();
        singleton.add(current);
        return new MovementGroup(singleton);
    }

    private static void adjustContainerCountForRemoval(List<RoomContentItem> items, int position) {
        int containerIndex = findContainerIndexForItem(items, position);
        if (containerIndex < 0 || containerIndex >= items.size()) {
            return;
        }
        RoomContentItem container = items.get(containerIndex);
        int currentCount = Math.max(0, container.getAttachedItemCount());
        if (currentCount <= 0) {
            return;
        }
        RoomContentItem updatedContainer = recreateContainerWithNewCount(container, currentCount - 1);
        items.set(containerIndex, updatedContainer);
    }

    private static void insertGroupIntoContainer(List<RoomContentItem> items,
            RoomContentItem item,
            RoomContentItem containerTemplate) {
        List<RoomContentItem> single = new ArrayList<>();
        single.add(item);
        insertGroupIntoContainer(items, single, containerTemplate);
    }

    private static void insertGroupIntoContainer(List<RoomContentItem> items,
            List<RoomContentItem> group,
            RoomContentItem containerTemplate) {
        int containerIndex = findContainerIndex(items, containerTemplate);
        RoomContentItem container = items.get(containerIndex);
        int attachedCount = Math.max(0, container.getAttachedItemCount());
        int insertionIndex = findContainerInsertionIndex(items, containerIndex);
        items.addAll(insertionIndex, group);
        RoomContentItem updatedContainer = recreateContainerWithNewCount(container,
                attachedCount + (group.isEmpty() ? 0 : 1));
        items.set(containerIndex, updatedContainer);
    }

    private static int findContainerInsertionIndex(List<RoomContentItem> items, int containerIndex) {
        if (containerIndex < 0 || containerIndex >= items.size()) {
            return items.size();
        }
        int insertionIndex = containerIndex + 1;
        int remainingDirectChildren = Math.max(0, items.get(containerIndex).getAttachedItemCount());
        while (insertionIndex < items.size() && remainingDirectChildren > 0) {
            RoomContentItem current = items.get(insertionIndex);
            remainingDirectChildren--;
            if (current.isContainer()) {
                remainingDirectChildren += Math.max(0, current.getAttachedItemCount());
            }
            insertionIndex++;
        }
        return insertionIndex;
    }

    private static void removeGroupAtPosition(List<RoomContentItem> items, int startPosition, int count) {
        for (int i = 0; i < count && startPosition < items.size(); i++) {
            items.remove(startPosition);
        }
    }

    private static int findContainerIndexForItem(List<RoomContentItem> items, int position) {
        if (position < 0 || position >= items.size()) {
            return -1;
        }
        Deque<ContainerFrame> stack = new ArrayDeque<>();
        for (int i = 0; i < items.size(); i++) {
            while (!stack.isEmpty() && stack.peek().remainingDirectChildren <= 0) {
                stack.pop();
            }
            if (i == position) {
                return stack.isEmpty() ? -1 : stack.peek().index;
            }
            RoomContentItem current = items.get(i);
            if (!stack.isEmpty()) {
                ContainerFrame parent = stack.peek();
                parent.remainingDirectChildren = Math.max(0, parent.remainingDirectChildren - 1);
            }
            if (current.isContainer()) {
                int directChildren = Math.max(0, current.getAttachedItemCount());
                stack.push(new ContainerFrame(i, directChildren));
            }
        }
        return -1;
    }

    private static RoomContentItem recreateContainerWithNewCount(RoomContentItem container, int newCount) {
        return new RoomContentItem(container.getName(),
                container.getComment(),
                container.getType(),
                container.getCategory(),
                container.getBarcode(),
                container.getSeries(),
                container.getNumber(),
                container.getAuthor(),
                container.getPublisher(),
                container.getEdition(),
                container.getPublicationDate(),
                container.getSummary(),
                new ArrayList<>(container.getTracks()),
                new ArrayList<>(container.getPhotos()),
                true,
                Math.max(0, newCount));
    }

    private static int findContainerIndex(List<RoomContentItem> items, RoomContentItem template) {
        for (int i = 0; i < items.size(); i++) {
            RoomContentItem candidate = items.get(i);
            if (!candidate.isContainer()) {
                continue;
            }
            if (candidate.getName().equals(template.getName())) {
                return i;
            }
        }
        return -1;
    }

    private static ContainerExtraction extractContainerGroup(List<RoomContentItem> items,
            int containerIndex) {
        RoomContentItem container = items.get(containerIndex);
        int declaredCount = Math.max(0, container.getAttachedItemCount());
        List<RoomContentItem> attachments = new ArrayList<>();
        int nextIndex = containerIndex + 1;
        int processedChildren = 0;
        while (nextIndex < items.size() && processedChildren < declaredCount) {
            RoomContentItem candidate = items.get(nextIndex);
            if (candidate.isContainer()) {
                ContainerExtraction childExtraction = extractContainerGroup(items, nextIndex);
                attachments.add(childExtraction.container);
                attachments.addAll(childExtraction.attachments);
                nextIndex = childExtraction.nextIndex;
            } else {
                attachments.add(candidate);
                nextIndex++;
            }
            processedChildren++;
        }
        if (processedChildren != declaredCount) {
            container = recreateContainerWithNewCount(container, processedChildren);
        }
        return new ContainerExtraction(container, attachments, processedChildren, nextIndex);
    }

    private static final class MovementGroup {
        final List<RoomContentItem> items;
        final int size;

        MovementGroup(List<RoomContentItem> items) {
            this.items = items;
            this.size = items.size();
        }
    }

    private static final class ContainerExtraction {
        final RoomContentItem container;
        final List<RoomContentItem> attachments;
        final int nextIndex;

        ContainerExtraction(RoomContentItem container, List<RoomContentItem> attachments, int directCount,
                int nextIndex) {
            this.container = container;
            this.attachments = attachments;
            this.nextIndex = nextIndex;
        }
    }

    private static final class ContainerFrame {
        final int index;
        int remainingDirectChildren;

        ContainerFrame(int index, int remainingDirectChildren) {
            this.index = index;
            this.remainingDirectChildren = remainingDirectChildren;
        }
    }
}

