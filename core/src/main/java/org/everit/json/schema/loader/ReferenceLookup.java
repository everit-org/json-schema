package org.everit.json.schema.loader;

import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.internal.JSONPointer;
import org.everit.json.schema.loader.internal.ReferenceResolver;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class ReferenceLookup {

    /**
     * Underscore-like extend function. Merges the properties of {@code additional} and
     * {@code original}. Neither {@code additional} nor {@code original} will be modified, but the
     * returned object may be referentially the same as one of the parameters (in case the other
     * parameter is an empty object).
     */
    static JSONObject extend(final JSONObject additional, final JSONObject original) {
        String[] additionalNames = JSONObject.getNames(additional);
        if (additionalNames == null) {
            return original;
        }
        String[] originalNames = JSONObject.getNames(original);
        if (originalNames == null) {
            return additional;
        }
        JSONObject rval = new JSONObject();
        for (String name : originalNames) {
            rval.put(name, original.get(name));
        }
        for (String name : additionalNames) {
            rval.put(name, additional.get(name));
        }
        return rval;
    }

    private LoadingState ls;

    public ReferenceLookup(LoadingState ls) {
        this.ls = requireNonNull(ls, "ls cannot eb null");
    }

    /**
     * Returns the absolute URI without its fragment part.
     *
     * @param fullUri the abslute URI
     * @return the URI without the fragment part
     */
    static URI withoutFragment(final String fullUri) {
        int hashmarkIdx = fullUri.indexOf('#');
        String rval;
        if (hashmarkIdx == -1) {
            rval = fullUri;
        } else {
            rval = fullUri.substring(0, hashmarkIdx);
        }
        try {
            return new URI(rval);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Rerurns a shallow copy of the {@code original} object, but it does not copy the {@code $ref}
     * key, in case it is present in {@code original}.
     */
    JSONObject withoutRef(JSONObject original) {
        String[] names = JSONObject.getNames(original);
        if (names == null) {
            return original;
        }
        JSONObject rval = new JSONObject();
        for (String name : names) {
            if (!"$ref".equals(name)) {
                rval.put(name, original.get(name));
            }
        }
        return rval;
    }

    /**
     * Returns a schema builder instance after looking up the JSON pointer.
     */
    Schema.Builder<?> lookup(String relPointerString, JSONObject ctx) {
        String absPointerString = ReferenceResolver.resolve(ls.id, relPointerString).toString();
        if (ls.pointerSchemas.containsKey(absPointerString)) {
            return ls.pointerSchemas.get(absPointerString);
        }
        boolean isExternal = !absPointerString.startsWith("#");
        JSONPointer pointer = isExternal
                ? JSONPointer.forURL(ls.httpClient, absPointerString)
                : JSONPointer.forDocument(ls.rootSchemaJson, absPointerString);
        ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                .refValue(relPointerString);
        ls.pointerSchemas.put(absPointerString, refBuilder);
        JSONPointer.QueryResult result = pointer.query();
        JSONObject resultObject = extend(withoutRef(ctx), result.getQueryResult());
        SchemaLoader childLoader = ls.initChildLoader()
                .resolutionScope(isExternal ? withoutFragment(absPointerString) : ls.id)
                .schemaJson(resultObject)
                .rootSchemaJson(result.getContainingDocument()).build();
        Schema referredSchema = childLoader.load().build();
        refBuilder.build().setReferredSchema(referredSchema);
        return refBuilder;
    }

}
