/*
 * File created on Sep 13, 2018
 *
 * Copyright (c) 2018 Carl Harris, Jr
 * and others as noted
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JSONObjectAdapterTest {

    @Test
    public void testAdapter() {
        final JSONObjectAdapter adapter = new JSONObjectAdapter(
                new JSONObject().put("key", "value"));

        assertEquals(1, adapter.length());
        assertTrue(adapter.has("key"));
        assertEquals("value", adapter.get("key"));
        assertTrue(Arrays.asList(adapter.keys()).contains("key"));
        assertTrue(adapter.toMap().containsKey("key"));
        assertEquals("value", adapter.toMap().get("key"));

        adapter.put("key", "otherValue");
        assertEquals("otherValue", adapter.get("key"));
    }

}
