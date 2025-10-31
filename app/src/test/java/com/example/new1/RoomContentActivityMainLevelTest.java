package com.example.new1;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import sun.misc.Unsafe;

public class RoomContentActivityMainLevelTest {

    @Test
    public void mainLevelOptionsIncludeContainersUnderRoot() throws Exception {
        RoomContentItem root = new RoomContentItem("Cuisine", null, null, null, null,
                null, null, null, null, null, null, null, null, null, true, 2);
        root.setRank(1L);

        RoomContentItem box = new RoomContentItem("Boîte A", null, null, null, null,
                null, null, null, null, null, null, null, null, null, true, 0);
        box.setRank(2L);
        box.setParentRank(root.getRank());

        RoomContentItem item = new RoomContentItem("Objet B", null, null, null, null,
                null, null, null, null, null, null, null, null, null, false, 0);
        item.setRank(3L);
        item.setParentRank(root.getRank());

        List<RoomContentItem> items = new ArrayList<>();
        items.add(root);
        items.add(box);
        items.add(item);

        RoomContentHierarchyHelper.normalizeHierarchy(items);

        RoomContentActivity activity = instantiateActivity();
        setField(activity, "establishmentName", "Maison");
        setField(activity, "roomName", "Cuisine");
        setField(activity, "roomContentItems", items);

        RoomContentActivity.ContainerNavigationState navigation =
                new RoomContentActivity.ContainerNavigationState();
        navigation.showMainLevel();

        Method method = RoomContentActivity.class.getDeclaredMethod("buildContainerOptions",
                String.class, String.class, RoomContentItem.class, int.class, java.util.Set.class,
                boolean.class, boolean.class, RoomContentActivity.ContainerNavigationState.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<RoomContentActivity.ContainerOption> options =
                (List<RoomContentActivity.ContainerOption>) method.invoke(activity, "Maison",
                        "Cuisine", item, 2, null, false, false, navigation);

        boolean containsBox = false;
        for (RoomContentActivity.ContainerOption option : options) {
            if (option.type == RoomContentActivity.ContainerOptionType.CONTAINER
                    && option.targetRank != null
                    && option.targetRank == box.getRank()) {
                containsBox = true;
                break;
            }
        }

        assertTrue("Le conteneur de niveau principal doit être proposé dans la liste", containsBox);
    }

    private RoomContentActivity instantiateActivity() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);
        return (RoomContentActivity) unsafe.allocateInstance(RoomContentActivity.class);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = RoomContentActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}

