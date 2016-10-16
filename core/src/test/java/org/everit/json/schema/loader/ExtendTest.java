/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema.loader;

import org.everit.json.schema.ObjectComparator;
import org.everit.json.schema.ResourceLoader;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class ExtendTest {

    private static final ResourceLoader loader = ResourceLoader.DEFAULT;

    private static JSONObject OBJECTS;

    static {
        OBJECTS = loader.readObj("merge-testcases.json");
    }

    @Test
    public void additionalHasMoreProps() {
        JSONObject actual = ReferenceLookup.extend(get("propIsTrue"), get("empty"));
        assertEquals(get("propIsTrue"), actual);
    }

    @Test
    public void additionalOverridesOriginal() {
        JSONObject actual = ReferenceLookup.extend(get("propIsTrue"), get("propIsFalse"));
        assertEquals(get("propIsTrue"), actual);
    }

    @Test
    public void additionalPropsAreMerged() {
        JSONObject actual = ReferenceLookup.extend(get("propIsTrue"), get("prop2IsFalse"));
        assertEquals(actual, get("propTrueProp2False"));
    }

    private void assertEquals(JSONObject expected, JSONObject actual) {
        Assert.assertTrue(ObjectComparator.deepEquals(expected, actual));
    }

    @Test
    public void bothEmpty() {
        JSONObject actual = ReferenceLookup.extend(get("empty"), get("empty"));
        assertEquals(new JSONObject(), actual);
    }

    private JSONObject get(String objectName) {
        return OBJECTS.getJSONObject(objectName);
    }

    @Test
    public void multiplePropsAreMerged() {
        JSONObject actual = ReferenceLookup.extend(get("multipleWithPropTrue"), get("multipleWithPropFalse"));
        assertEquals(get("mergedMultiple"), actual);
    }

    @Test
    public void originalPropertyRemainsUnchanged() {
        JSONObject actual = ReferenceLookup.extend(get("empty"), get("propIsTrue"));
        assertEquals(get("propIsTrue"), actual);
    }

}
