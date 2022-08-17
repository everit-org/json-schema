package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;

/**
 * @author erosb
 */
public class TrueSchema extends EmptySchema {

    public static class Builder extends EmptySchema.Builder {

        @Override
        protected Builder getBuilder()
        {
            return this;
        }

        @Override public TrueSchema build() {
            return new TrueSchema(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public TrueSchema(Builder builder) {
        super(builder);
    }

    @Override
    public void describeTo(JSONPrinter writer) {
        writer.value(true);
    }

    @Override
    public String toString() {
        return "true";
    }

    @Override public boolean equals(Object o) {
        return o instanceof TrueSchema && super.equals(o);
    }
}
