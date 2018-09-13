package org.everit.json.schema;

import org.json.JSONArray;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JSONArrayAdapterTest {

    @Test
    public void testAdapter() {
        final JSONArrayAdapter adapter = new JSONArrayAdapter(
                new JSONArray().put("value"));

        assertEquals(1, adapter.length());
        assertEquals("value", adapter.get(0));
        assertEquals("value", adapter.toList().get(0));

        adapter.put(0, "otherValue");
        assertEquals("otherValue", adapter.get(0));
    }

}
