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

/**
 * Boolean schema validator.
 */
public class BooleanSchema extends Schema {

  /**
   * Builder class for {@link BooleanSchema}.
   */
  public static class Builder extends Schema.Builder<BooleanSchema> {

    @Override
    public BooleanSchema build() {
      return new BooleanSchema(this);
    }

  }

  public static final BooleanSchema INSTANCE = new BooleanSchema(builder());

  public static Builder builder() {
    return new Builder();
  }

  public BooleanSchema(final Builder builder) {
    super(builder);
  }

  @Override
  public void validate(final Object subject) {
    if (!(subject instanceof Boolean)) {
      throw new ValidationException(Boolean.class, subject);
    }
  }

}
