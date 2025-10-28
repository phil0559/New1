package com.example.new1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import sun.misc.Unsafe;

public class RoomContentAdapterContainerPopupRestoreTest {

    private Method prepareRestoreMethod;
    private Field pendingRestoreField;
    private Field itemsField;
    private Field hierarchyParentsField;
    private Field hierarchyDirtyField;

    @Before
    public void setUp() throws Exception {
        prepareRestoreMethod = RoomContentAdapter.class.getDeclaredMethod(
                "preparePendingContainerPopupRestore",
                RoomContentAdapter.ContainerPopupRestoreState.class,
                int.class,
                boolean.class);
        prepareRestoreMethod.setAccessible(true);

        pendingRestoreField = RoomContentAdapter.class.getDeclaredField(
                "pendingContainerPopupRestore");
        pendingRestoreField.setAccessible(true);

        itemsField = RoomContentAdapter.class.getDeclaredField("items");
        itemsField.setAccessible(true);

        hierarchyParentsField = RoomContentAdapter.class.getDeclaredField(
                "hierarchyParentPositions");
        hierarchyParentsField.setAccessible(true);

        hierarchyDirtyField = RoomContentAdapter.class.getDeclaredField("hierarchyDirty");
        hierarchyDirtyField.setAccessible(true);
    }

    @Test
    public void restoresPopupWhenTargetIsSameContainer() throws Exception {
        List<RoomContentItem> items = new ArrayList<>();
        items.add(createContainer("Boîte"));

        RoomContentAdapter adapter = instantiateAdapter(items, new int[]{-1});

        RoomContentAdapter.ContainerPopupRestoreState state =
                new RoomContentAdapter.ContainerPopupRestoreState(0, 5);

        prepareRestoreMethod.invoke(adapter, state, 0, true);

        RoomContentAdapter.ContainerPopupRestoreState pending =
                (RoomContentAdapter.ContainerPopupRestoreState) pendingRestoreField.get(adapter);

        assertNotNull(pending);
        assertEquals(0, pending.containerPosition);
        assertEquals(5, pending.visibilityMask);
        assertTrue(pending.autoOpenWhenBound);
    }

    @Test
    public void restoresPopupWhenTargetIsDescendant() throws Exception {
        List<RoomContentItem> items = new ArrayList<>();
        items.add(createContainer("Boîte"));
        items.add(createItem("Livre"));

        RoomContentAdapter adapter = instantiateAdapter(items, new int[]{-1, 0});

        RoomContentAdapter.ContainerPopupRestoreState state =
                new RoomContentAdapter.ContainerPopupRestoreState(0, 3);

        prepareRestoreMethod.invoke(adapter, state, 1, true);

        RoomContentAdapter.ContainerPopupRestoreState pending =
                (RoomContentAdapter.ContainerPopupRestoreState) pendingRestoreField.get(adapter);

        assertNotNull(pending);
        assertEquals(0, pending.containerPosition);
        assertEquals(3, pending.visibilityMask);
        assertTrue(pending.autoOpenWhenBound);
    }

    @Test
    public void clearsPendingStateWhenTargetIsUnrelated() throws Exception {
        List<RoomContentItem> items = new ArrayList<>();
        items.add(createContainer("Boîte"));
        items.add(createItem("Livre"));
        items.add(createItem("Vase"));

        RoomContentAdapter adapter = instantiateAdapter(items, new int[]{-1, 0, -1});

        pendingRestoreField.set(adapter, new RoomContentAdapter.ContainerPopupRestoreState(0, 1));

        RoomContentAdapter.ContainerPopupRestoreState state =
                new RoomContentAdapter.ContainerPopupRestoreState(0, 7);

        prepareRestoreMethod.invoke(adapter, state, 2, true);

        assertNull(pendingRestoreField.get(adapter));
    }

    private RoomContentAdapter instantiateAdapter(List<RoomContentItem> items, int[] parentPositions)
            throws Exception {
        RoomContentAdapter adapter = (RoomContentAdapter) getUnsafe()
                .allocateInstance(RoomContentAdapter.class);
        itemsField.set(adapter, items);
        hierarchyParentsField.set(adapter, parentPositions);
        hierarchyDirtyField.set(adapter, false);
        return adapter;
    }

    private RoomContentItem createContainer(String name) {
        return new RoomContentItem(name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true, 0);
    }

    private RoomContentItem createItem(String name) {
        return new RoomContentItem(name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, false, 0);
    }

    private Unsafe getUnsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }
}
