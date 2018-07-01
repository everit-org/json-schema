package org.everit.json.schema;

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.everit.json.schema.internal.JsonPrinter;
import org.everit.json.schema.loader.JsonArray;
import org.everit.json.schema.loader.JsonObject;

/**
 * Enum schema validator.
 */
public class EnumSchema extends Schema {

    static Object toJavaValue(Object orig) {
    	if (orig instanceof JsonArray) {
    		return ((JsonArray)orig).toList();
        } else if (orig instanceof JsonObject && ((JsonObject)orig).equals(JsonObject.NULL)) {
        	return null;
        } else if (orig instanceof JsonObject) {
        	return ((JsonObject)orig).toMap();
        } else {
            return orig;
        }
    }

    static Set<Object> toJavaValues(Set<Object> orgJsons) {
        return orgJsons.stream().map(EnumSchema::toJavaValue).collect(toSet());
    }

    /**
     * Builder class for {@link EnumSchema}.
     */
    public static class Builder extends Schema.Builder<EnumSchema> {

        private Set<Object> possibleValues = new HashSet<>();

        @Override
        public EnumSchema build() {
            return new EnumSchema(this);
        }

        public Builder possibleValue(Object possibleValue) {
            possibleValues.add(possibleValue);
            return this;
        }

        public Builder possibleValues(Set<Object> possibleValues) {
            this.possibleValues = possibleValues;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Set<Object> possibleValues;

    public EnumSchema(Builder builder) {
        super(builder);
        possibleValues = Collections.unmodifiableSet(toJavaValues(builder.possibleValues));
    }

    public Set<Object> getPossibleValues() {
        return possibleValues;
    }

    @Override
    void describePropertiesTo(JsonPrinter writer) {
        writer.key("enum");
        writer.array();
        possibleValues.forEach(writer::value);
        writer.endArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EnumSchema) {
            EnumSchema that = (EnumSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(possibleValues, that.possibleValues) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), possibleValues);
    }

    @Override public void accept(Visitor visitor) {
        visitor.visitEnumSchema(this);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof EnumSchema;
    }

}
