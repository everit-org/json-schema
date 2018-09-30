/*
 * File created on Sep 14, 2018
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
package org.everit.json.schema.javax.json;

import org.junit.Test;

import javax.json.Json;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JavaxJsonObjectAdapterTest {

    private final JavaxJsonObjectAdapter adapter = new JavaxJsonObjectAdapter(
            Json.createObjectBuilder().add("key", "value").build());

    @Test
    public void testAdapter() {
        assertEquals(1, adapter.length());
        assertTrue(adapter.has("key"));
        assertEquals(Json.createValue("value"), adapter.get("key"));
        assertTrue(Arrays.asList(adapter.keys()).contains("key"));
        assertEquals(Json.createValue("value"), adapter.toMap().get("key"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAdapterPut() {
        adapter.put("key", Json.createValue("value"));
    }

}
