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

import static org.junit.Assert.assertEquals;

public class JavaxJsonArrayAdapterTest {

    private final JavaxJsonArrayAdapter adapter = new JavaxJsonArrayAdapter(
            Json.createArrayBuilder().add("value").build());

    @Test
    public void testAdapter() {

        assertEquals(1, adapter.length());
        assertEquals(Json.createValue("value"), adapter.get(0));
        assertEquals(Json.createValue("value"), adapter.toList().get(0));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAdapterPut() {
        adapter.put(0, Json.createValue("value"));
    }

}
