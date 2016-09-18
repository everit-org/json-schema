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

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONObject;

/**
 * {@code Null} schema validator.
 */
public class NullSchema extends Schema {

    /**
     * Builder class for {@link NullSchema}.
     */
    public static class Builder extends Schema.Builder<NullSchema> {

        @Override
        public NullSchema build() {
            return new NullSchema(this);
        }
    }

    public static final NullSchema INSTANCE = new NullSchema(builder());

    public static Builder builder() {
        return new Builder();
    }

    public NullSchema(final Builder builder) {
        super(builder);
    }

    @Override
    public void validate(final Object subject) {
        if (!(subject == null || subject == JSONObject.NULL)) {
            throw new ValidationException(this, "expected: null, found: "
                    + subject.getClass().getSimpleName(), "type");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof NullSchema) {
            NullSchema that = (NullSchema) o;
            return that.canEqual(this) && super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NullSchema;
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) {
        writer.key("type");
        writer.value("null");
    }
}
