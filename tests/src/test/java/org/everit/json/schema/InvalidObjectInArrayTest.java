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
package org.everit.json.schema;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Test;

public class InvalidObjectInArrayTest {

    private JSONObject readObject(final String fileName) {
        return new JSONObject(new JSONTokener(getClass()
                .getResourceAsStream("/org/everit/json/schema/invalidobjectinarray/" + fileName)));
    }

    @Test
    public void test() {
        Schema schema = SchemaLoader.load(readObject("schema.json"));
        Object subject = readObject("subject.json");
        try {
            schema.validate(subject);
            Assert.fail("did not throw exception");
        } catch (ValidationException e) {
            Assert.assertEquals("#/notification/target/apps/0/id", e.getPointerToViolation());
        }
    }

}
