package com.example.new1;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import sun.misc.Unsafe;

public class RoomContentAdapterRankingTest {

    @Test
    public void topLevelContainerWithoutChildrenKeepsRank() throws Exception {
        List<RoomContentItem> items = new ArrayList<>();
        items.add(createContainer("Parent", 0));

        RoomContentAdapter adapter = instantiateAdapter(items);

        assertEquals("1", invokeBuildRankLabel(adapter, 0));
    }

    @Test
    public void attachedContainerWithoutChildrenInheritsParentRank() throws Exception {
        List<RoomContentItem> items = new ArrayList<>();
        items.add(createContainer("Parent", 1));
        items.add(createContainer("Child", 0));

        RoomContentAdapter adapter = instantiateAdapter(items);

        assertEquals("1", invokeBuildRankLabel(adapter, 0));
        assertEquals("1.1", invokeBuildRankLabel(adapter, 1));
    }

    @Test
    public void attachmentsRemainSequentialAfterContainer() throws Exception {
        List<RoomContentItem> items = new ArrayList<>();
        items.add(createContainer("Parent", 2));
        items.add(createContainer("Child", 0));
        items.add(createItem("Attachment"));

        RoomContentAdapter adapter = instantiateAdapter(items);

        assertEquals("1", invokeBuildRankLabel(adapter, 0));
        assertEquals("1.1", invokeBuildRankLabel(adapter, 1));
        assertEquals("1.2", invokeBuildRankLabel(adapter, 2));
    }

    private RoomContentItem createContainer(String name, int attachedCount) {
        return new RoomContentItem(name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true, attachedCount);
    }

    private RoomContentItem createItem(String name) {
        return new RoomContentItem(name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, false, 0);
    }

    private RoomContentAdapter instantiateAdapter(List<RoomContentItem> items) throws Exception {
        Unsafe unsafe = getUnsafe();
        RoomContentAdapter adapter = (RoomContentAdapter) unsafe.allocateInstance(
                RoomContentAdapter.class);
        Field itemsField = RoomContentAdapter.class.getDeclaredField("items");
        itemsField.setAccessible(true);
        itemsField.set(adapter, items);
        return adapter;
    }

    private Unsafe getUnsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private String invokeBuildRankLabel(RoomContentAdapter adapter, int position) throws Exception {
        Method method = RoomContentAdapter.class.getDeclaredMethod("buildRankLabel", int.class);
        method.setAccessible(true);
        return (String) method.invoke(adapter, position);
    }
}
