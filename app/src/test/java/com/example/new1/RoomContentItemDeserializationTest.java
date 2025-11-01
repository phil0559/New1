package com.example.new1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class RoomContentItemDeserializationTest {

    @Test
    public void furnitureAttributesRestoreEvenWithoutFlag() throws Exception {
        JSONObject object = new JSONObject();
        object.put("name", "Bibliothèque oubliée");
        object.put("furnitureType", "shelf");
        object.put("furnitureCustomType", "étagère spéciale");
        object.put("furnitureLevels", 3);
        object.put("furnitureColumns", 2);
        object.put("furnitureHasTop", true);
        object.put("furnitureHasBottom", false);
        object.put("furnitureStorageTower", true);

        RoomContentItem item = RoomContentItem.fromJson(object);

        assertTrue("Le mobilier doit être détecté même sans drapeau explicite", item.isFurniture());
        assertTrue("La tour de rangement doit être restaurée", item.isStorageTower());
        assertEquals("shelf", item.getFurnitureType());
        assertEquals("étagère spéciale", item.getFurnitureCustomType());
        assertEquals(Integer.valueOf(3), item.getFurnitureLevels());
        assertEquals(Integer.valueOf(2), item.getFurnitureColumns());
        assertTrue(item.hasFurnitureTop());
        assertFalse(item.hasFurnitureBottom());
        assertFalse(item.isContainer());
        assertEquals("Bibliothèque oubliée", item.getName());
    }
}
