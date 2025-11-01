package com.example.new1;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.util.LongSparseArray;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import sun.misc.Unsafe;

public class RoomContentAdapterVisibilityTest {

    private Field itemsField;
    private Field hierarchyParentsField;
    private Field hierarchyDepthsField;
    private Field hierarchyDirtyField;
    private Field visibilityStatesField;

    @Before
    public void setUp() throws Exception {
        itemsField = RoomContentAdapter.class.getDeclaredField("items");
        itemsField.setAccessible(true);

        hierarchyParentsField = RoomContentAdapter.class.getDeclaredField("hierarchyParentPositions");
        hierarchyParentsField.setAccessible(true);

        hierarchyDepthsField = RoomContentAdapter.class.getDeclaredField("hierarchyDepths");
        hierarchyDepthsField.setAccessible(true);

        hierarchyDirtyField = RoomContentAdapter.class.getDeclaredField("hierarchyDirty");
        hierarchyDirtyField.setAccessible(true);

        visibilityStatesField = RoomContentAdapter.class.getDeclaredField("containerVisibilityStates");
        visibilityStatesField.setAccessible(true);
    }

    @Test
    public void ensureItemsVisibleRestoresDefaultMaskAndDisplaysChildren() throws Exception {
        RoomContentItem container = createContainer("Boîte");
        container.setRank(42L);
        container.setAttachedItemCount(1);

        RoomContentItem child = createItem("Livre");
        child.setRank(84L);
        child.setParentRank(container.getRank());
        child.setDisplayed(false);

        List<RoomContentItem> items = new ArrayList<>();
        items.add(container);
        items.add(child);

        RoomContentAdapter adapter = instantiateAdapter(items);

        LongSparseArray<Integer> visibilityStates = (LongSparseArray<Integer>) visibilityStatesField.get(adapter);
        visibilityStates.put(container.getRank(), 1);

        adapter.ensureItemsVisibleForContainer(container);

        assertNull(visibilityStates.get(container.getRank()));
        assertTrue(child.isDisplayed());
    }

    private RoomContentAdapter instantiateAdapter(List<RoomContentItem> items) throws Exception {
        RoomContentAdapter adapter = (RoomContentAdapter) getUnsafe().allocateInstance(RoomContentAdapter.class);
        itemsField.set(adapter, items);
        hierarchyParentsField.set(adapter, new int[] { -1, 0 });
        hierarchyDepthsField.set(adapter, new int[] { 0, 1 });
        hierarchyDirtyField.set(adapter, false);
        visibilityStatesField.set(adapter, new LongSparseArray<>());
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
