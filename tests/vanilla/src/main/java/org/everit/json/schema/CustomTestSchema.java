package org.everit.json.schema;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;


import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.FormatValidator.NONE;

import java.util.Objects;

import org.everit.json.schema.internal.JSONPrinter;
import org.everit.json.schema.regexp.JavaUtilRegexpFactory;
import org.everit.json.schema.regexp.Regexp;

/**
 * @author jmfernandez
 * {@code String} schema validator.
 */
public class CustomTestSchema extends AbstractCustomTypeSchema {

    /**
     * Builder class for {@link CustomTestSchema}.
     */
    public static class Builder extends Schema.Builder<CustomTestSchema> {

        private String rightValue;

        @Override
        public CustomTestSchema build() {
            return new CustomTestSchema(this);
        }

        public Builder rightValue(final String rightValue) {
            this.rightValue = rightValue;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    
    private final String rightValue;

    public CustomTestSchema() {
        this(builder());
    }

    /**
     * Constructor.
     *
     * @param builder
     *         the builder object containing validation criteria
     */
    public CustomTestSchema(final Builder builder) {
        super(builder);
        this.rightValue = builder.rightValue;
    }

    @Override
    public Visitor buildVisitor(Object subject,ValidatingVisitor owner) {
        return new CustomTypeSchemaValidatingVisitor(subject, owner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof CustomTestSchema) {
            CustomTestSchema that = (CustomTestSchema) o;
            return that.canEqual(this) &&
		Objects.equals(rightValue, that.rightValue) &&
                super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rightValue);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof CustomTestSchema;
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) {
        writer.ifPresent("rightValue", rightValue);
    }

    public String rightValue() {
        return rightValue;
    }
}
