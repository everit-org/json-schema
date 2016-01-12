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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Enum schema validator.
 *
 */
public class EnumSchema extends Schema {

  /**
   * Builder class for {@link EnumSchema}.
   */
  public static class Builder extends Schema.Builder<EnumSchema> {

    private Set<Object> possibleValues = new HashSet<Object>();

    public Builder possibleValue(final Object possibleValue) {
      possibleValues.add(possibleValue);
      return this;
    }

    public Builder possibleValues(final Set<Object> possibleValues) {
      this.possibleValues = possibleValues;
      return this;
    }

    @Override
    public EnumSchema build() {
      return new EnumSchema(this);
    }
  }

  private final Set<Object> possibleValues;

  public EnumSchema(final Builder builder) {
    super(builder);
    this.possibleValues = Collections.unmodifiableSet(new HashSet<Object>(builder.possibleValues));
  }

  @Override
  public void validate(final Object subject) 
  {
	  Iterator<Object> iterator = possibleValues.iterator();
	  while(iterator.hasNext())
	  {
		 if(ObjectComparator.deepEquals(iterator.next(), subject))
		 {
			 return;
		 }
	  }
	  throw new ValidationException(String.format(
			  "%s is not a valid enum value", subject));
  }

  public Set<Object> getPossibleValues() {
    return possibleValues;
  }

  public static Builder builder() {
    return new Builder();
  }

}
