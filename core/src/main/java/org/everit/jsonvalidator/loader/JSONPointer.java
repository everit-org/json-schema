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
package org.everit.jsonvalidator.loader;

import java.util.Objects;

import org.everit.jsonvalidator.SchemaException;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONPointer {

  private final String pointer;

  public JSONPointer(final String pointer) {
    this.pointer = Objects.requireNonNull(pointer, "pointer cannot be null");
  }

  public <E> E queryFrom(final JSONObject document) {
    String[] path = pointer.split("/");
    if (!"#".equals(path[0])) {
      throw new IllegalArgumentException("JSON pointers must start with a '#'");
    }
    Object current = document;
    for (int i = 1; i < path.length; ++i) {
      String segment = path[i];
      if (current instanceof JSONObject) {
        if (!((JSONObject) current).has(segment)) {
          throw new SchemaException(String.format(
              "failed to resolve JSON pointer [%s]. Segment [%s] not found", pointer, segment));
        }
        current = ((JSONObject) current).get(segment);
      } else if (current instanceof JSONArray) {
        current = ((JSONArray) current).get(Integer.valueOf(segment));
      }
    }
    return (E) current;
  }

}
