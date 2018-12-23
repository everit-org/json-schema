package org.everit.json.schema.event;

import java.util.Objects;

import org.everit.json.schema.ConditionalSchema;

public abstract class ConditionalSchemaValidationEvent extends ValidationEvent<ConditionalSchema> {

    public enum Keyword {
        IF {
            @Override public String toString() {
                return "if";
            }
        },
        THEN {
            @Override public String toString() {
                return "then";
            }
        },
        ELSE {
            @Override public String toString() {
                return "else";
            }
        };
    }

    final ConditionalSchemaMatchEvent.Keyword keyword;

    public ConditionalSchemaValidationEvent(ConditionalSchema schema, Object instance, Keyword keyword) {
        super(schema, instance);
        this.keyword = keyword;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ConditionalSchemaValidationEvent))
            return false;
        if (!super.equals(o))
            return false;
        ConditionalSchemaValidationEvent that = (ConditionalSchemaValidationEvent) o;
        return keyword == that.keyword;
    }

    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), keyword);
    }
}
