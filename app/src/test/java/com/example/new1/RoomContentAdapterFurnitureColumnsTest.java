package com.example.new1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class RoomContentAdapterFurnitureColumnsTest {

    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
    }

    @Test
    public void chipsVisibleWhenColumnCountIsAtMostFive() throws Exception {
        RoomContentItem item = createFurnitureItem(4);
        HolderBundle bundle = createBoundHolder(item);
        RoomContentAdapter adapter = bundle.adapter;
        RoomContentAdapter.ViewHolder holder = bundle.holder;

        LayoutInflater inflater = LayoutInflater.from(context);
        FrameLayout parent = new FrameLayout(context);
        View popupView = inflater.inflate(R.layout.popup_furniture_details, parent, false);
        LinearLayout columnsContainer = popupView.findViewById(R.id.container_furniture_columns);
        LinearLayout sectionsContainer = popupView.findViewById(R.id.container_furniture_sections);
        HorizontalScrollView scrollContainer = popupView.findViewById(R.id.scroll_furniture_columns);
        Spinner dropdownView = popupView.findViewById(R.id.spinner_furniture_columns);

        invokePopulateColumns(holder, columnsContainer, sectionsContainer, item,
                scrollContainer, dropdownView);

        assertEquals(View.VISIBLE, scrollContainer.getVisibility());
        assertEquals(View.GONE, dropdownView.getVisibility());
        assertEquals(4, columnsContainer.getChildCount());

        Field selectedField = holder.getClass().getDeclaredField("selectedFurnitureColumn");
        selectedField.setAccessible(true);
        assertEquals(Integer.valueOf(1), (Integer) selectedField.get(holder));

        TextView thirdChip = (TextView) columnsContainer.getChildAt(2);
        thirdChip.performClick();

        assertEquals(Integer.valueOf(3), (Integer) selectedField.get(holder));
        Field adapterSelectedField = RoomContentAdapter.class
                .getDeclaredField("activeFurniturePopupSelectedColumn");
        adapterSelectedField.setAccessible(true);
        assertEquals(Integer.valueOf(3), (Integer) adapterSelectedField.get(adapter));
        Field adapterPositionField = RoomContentAdapter.class
                .getDeclaredField("activeFurniturePopupAdapterPosition");
        adapterPositionField.setAccessible(true);
        assertEquals(0, adapterPositionField.getInt(adapter));
    }

    @Test
    public void dropdownVisibleWhenColumnCountExceedsFive() throws Exception {
        RoomContentItem item = createFurnitureItem(7);
        HolderBundle bundle = createBoundHolder(item);
        RoomContentAdapter adapter = bundle.adapter;
        RoomContentAdapter.ViewHolder holder = bundle.holder;

        LayoutInflater inflater = LayoutInflater.from(context);
        FrameLayout parent = new FrameLayout(context);
        View popupView = inflater.inflate(R.layout.popup_furniture_details, parent, false);
        LinearLayout columnsContainer = popupView.findViewById(R.id.container_furniture_columns);
        LinearLayout sectionsContainer = popupView.findViewById(R.id.container_furniture_sections);
        HorizontalScrollView scrollContainer = popupView.findViewById(R.id.scroll_furniture_columns);
        Spinner dropdownView = popupView.findViewById(R.id.spinner_furniture_columns);

        Field selectedField = holder.getClass().getDeclaredField("selectedFurnitureColumn");
        selectedField.setAccessible(true);
        selectedField.set(holder, 4);

        invokePopulateColumns(holder, columnsContainer, sectionsContainer, item,
                scrollContainer, dropdownView);

        assertEquals(View.GONE, scrollContainer.getVisibility());
        assertEquals(View.VISIBLE, dropdownView.getVisibility());
        assertEquals(0, columnsContainer.getChildCount());
        assertNotNull(dropdownView.getAdapter());
        assertEquals(7, dropdownView.getAdapter().getCount());
        assertEquals(3, dropdownView.getSelectedItemPosition());

        dropdownView.setSelection(5);
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        assertEquals(Integer.valueOf(6), (Integer) selectedField.get(holder));
        Field adapterSelectedField = RoomContentAdapter.class
                .getDeclaredField("activeFurniturePopupSelectedColumn");
        adapterSelectedField.setAccessible(true);
        assertEquals(Integer.valueOf(6), (Integer) adapterSelectedField.get(adapter));
        Field adapterPositionField = RoomContentAdapter.class
                .getDeclaredField("activeFurniturePopupAdapterPosition");
        adapterPositionField.setAccessible(true);
        assertEquals(0, adapterPositionField.getInt(adapter));
    }

    private void invokePopulateColumns(RoomContentAdapter.ViewHolder holder,
            LinearLayout columnsContainer,
            LinearLayout sectionsContainer,
            RoomContentItem item,
            HorizontalScrollView scrollContainer,
            Spinner dropdownView) throws Exception {
        Method method = holder.getClass().getDeclaredMethod("populateFurniturePopupColumns",
                LinearLayout.class,
                LinearLayout.class,
                RoomContentItem.class,
                HorizontalScrollView.class,
                Spinner.class);
        method.setAccessible(true);
        method.invoke(holder, columnsContainer, sectionsContainer, item, scrollContainer,
                dropdownView);
    }

    private HolderBundle createBoundHolder(RoomContentItem item) {
        List<RoomContentItem> items = new ArrayList<>();
        items.add(item);
        RoomContentAdapter adapter = new RoomContentAdapter(context, items);
        FrameLayout parent = new FrameLayout(context);
        RoomContentAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.bindViewHolder(holder, 0);
        return new HolderBundle(adapter, holder);
    }

    private RoomContentItem createFurnitureItem(int columns) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", "Bibliothèque");
        object.put("furniture", true);
        object.put("furnitureColumns", columns);
        object.put("furnitureLevels", 1);
        object.put("container", false);
        return RoomContentItem.fromJson(object);
    }

    private static final class HolderBundle {
        final RoomContentAdapter adapter;
        final RoomContentAdapter.ViewHolder holder;

        HolderBundle(RoomContentAdapter adapter, RoomContentAdapter.ViewHolder holder) {
            this.adapter = adapter;
            this.holder = holder;
        }
    }
}
