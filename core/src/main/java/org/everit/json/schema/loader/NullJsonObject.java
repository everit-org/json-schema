package org.everit.json.schema.loader;

import java.util.HashMap;

public final class NullJsonObject extends JsonObject {

    @Override
    protected final Object clone() {
        return this;
    }

    @Override public Object unwrap() {
        return null;
    }
    
    @Override
    public boolean equals(Object object) {
        return object == null || object == this;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "null";
    }
}