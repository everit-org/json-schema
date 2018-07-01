package org.everit.json.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.everit.json.schema.loader.JsonArray;
import org.everit.json.schema.loader.JsonObject;

public class JsonPointer {

    private static final String ENCODING = "utf-8";

    public static class Builder {

        private final List<String> fragments = new ArrayList<String>();

        public JsonPointer build() {
            return new JsonPointer(this.fragments);
        }

        public Builder append(String token) {
            if (token == null) {
                throw new NullPointerException("fragment cannot be null");
            }
            this.fragments.add(token);
            return this;
        }

        public Builder append(int index) {
            this.fragments.add(String.valueOf(index));
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final List<String> fragments;

    public JsonPointer(final String pointer) {
        if (pointer == null) {
            throw new NullPointerException("pointer cannot be null");
        }
        if (pointer.isEmpty() || pointer.equals("#")) {
            this.fragments = Collections.emptyList();
            return;
        }
        String refs;
        if (pointer.startsWith("#/")) {
            refs = pointer.substring(2);
            try {
                refs = URLDecoder.decode(refs, ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else if (pointer.startsWith("/")) {
            refs = pointer.substring(1);
        } else {
            throw new IllegalArgumentException("a JSON pointer should start with '/' or '#/'");
        }
        this.fragments = new ArrayList<String>();
        int slashIdx = -1;
        int prevSlashIdx = 0;
        do {
            prevSlashIdx = slashIdx + 1;
            slashIdx = refs.indexOf('/', prevSlashIdx);
            if(prevSlashIdx == slashIdx || prevSlashIdx == refs.length()) {
                // found 2 slashes in a row ( obj//next )
                // or single slash at the end of a string ( obj/test/ )
                this.fragments.add("");
            } else if (slashIdx >= 0) {
                final String token = refs.substring(prevSlashIdx, slashIdx);
                this.fragments.add(unescape(token));
            } else {
                // last item after separator, or no separator at all.
                final String token = refs.substring(prevSlashIdx);
                this.fragments.add(unescape(token));
            }
        } while (slashIdx >= 0);
    }

    public JsonPointer(List<String> fragments) {
        this.fragments = new ArrayList<String>(fragments);
    }

    private String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public Object queryFrom(Object document) throws JsonPointerException {
        if (this.fragments.isEmpty()) {
            return document;
        }
        Object current = document;
        for (String token : this.fragments) {
            if (current instanceof JsonObject) {
                current = ((JsonObject) current).get(unescape(token));
            } else if (current instanceof JsonArray) {
                current = readByFragmentIndex(current, token);
            } else {
                throw new JsonPointerException(String.format(
                        "value [%s] is not an array or object therefore its key %s cannot be resolved", current,
                        token));
            }
        }
        return current;
    }

    private Object readByFragmentIndex(Object entry, String fragmentIndex) throws JsonPointerException {
        try {
            int index = Integer.parseInt(fragmentIndex);
            JsonArray arrEntry = (JsonArray) entry;
            if (index >= arrEntry.length()) {
                throw new JsonPointerException(String.format("index %d is out of bounds - the array has %d elements", index,
                		arrEntry.length()));
            }
            try {
				return arrEntry.get(index);
			} catch (Throwable e) {
				throw new JsonPointerException("Error reading value at index position " + index, e);
			}
        } catch (NumberFormatException e) {
            throw new JsonPointerException(String.format("%s is not an array index", fragmentIndex), e);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder rval = new StringBuilder("");
        for (String fragment: this.fragments) {
            rval.append('/').append(escape(fragment));
        }
        return rval.toString();
    }

    private String escape(String fragment) {
        return fragment.replace("~", "~0")
                .replace("/", "~1")
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public String toURIFragment() {
        try {
            StringBuilder rval = new StringBuilder("#");
            for (String token : this.fragments) {
                rval.append('/').append(URLEncoder.encode(token, ENCODING));
            }
            return rval.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
}
