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

import java.util.Iterator;
import java.util.List;

/**
 * Thrown by {@link org.everit.json.schema.loader.SchemaLoader#load()} when it encounters
 * un-parseable schema JSON definition.
 */
public class SchemaException extends RuntimeException {
  private static final long serialVersionUID = 5987489689035036987L;

  public SchemaException(final String message) {
    super(message);
  }

  public SchemaException(final String key, final Class<?> expectedType, final Object actualValue) {
    super(String.format("key %s : expected type: %s , found : %s", key, expectedType
        .getSimpleName(), (actualValue == null ? "null" : actualValue.getClass().getSimpleName())));
  }

  public SchemaException(final String key, final List<Class<?>> expectedTypes,
      final Object actualValue) {
    super(String.format("key %s: expected type is one of %s, found: %s",
        key, joinClassNames(expectedTypes), typeOfValue(actualValue)));
  }

  private static Object typeOfValue(final Object actualValue) {
    return actualValue == null ? "null" : actualValue.getClass().getSimpleName();
  }

  private static String joinClassNames(final List<Class<?>> expectedTypes)
  {
	  StringBuilder builder = new StringBuilder();
	  Iterator<Class<?>> iterator = expectedTypes.iterator();
	  int index = 0;
	  while(iterator.hasNext())
	  {
		  if(index > 0)
		  {
			  builder.append(", ");
		  }
		  builder.append(iterator.next().getSimpleName());
		  index++;
	  }
	  return builder.toString();
  }

  public SchemaException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
