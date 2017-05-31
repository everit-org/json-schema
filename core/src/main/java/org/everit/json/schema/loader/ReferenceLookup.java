package org.everit.json.schema.loader;

import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.everit.json.schema.loader.internal.ReferenceResolver;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    @Deprecated
    static JSONObject extend(final JSONObject additional, final JSONObject original) {
        return new JSONObject(extend(new JsonObject(additional.toMap()), new JsonObject(original.toMap())).toMap());
    }

    static JsonObject extend(JsonObject additional, JsonObject original) {
        if (additional.keySet().isEmpty()) {
            return original;
        }
        if (original.keySet().isEmpty()) {
            return additional;
        }
        Map<String, Object> rawObj = new HashMap<>();
        original.keySet().stream().forEach(name -> rawObj.put(name, original.get(name)));
        additional.keySet().stream().forEach(name -> rawObj.put(name, additional.get(name)));
        return new JsonObject(rawObj);
    }

    private LoadingState ls;

    private SchemaClient httpClient;

    /**
     * Creates an instance which uses a {@link DefaultSchemaClient}.
     *
     * @deprecated use {@link #ReferenceLookup(LoadingState, SchemaClient)} instead
     */
    @Deprecated
    public ReferenceLookup(LoadingState ls) {
        this(ls, new DefaultSchemaClient());
    }

    ReferenceLookup(LoadingState ls, SchemaClient httpClient) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.httpClient = requireNonNull(httpClient, "httpClient cannot be null");
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

    JsonObject withoutRef(JsonObject original) {
        Map<String, Object> rawObj = new HashMap<>();
        original.keySet().stream()
                .filter(name -> !"$ref".equals(name))
                .forEach(name -> rawObj.put(name, original.get(name)));
        return new JsonObject(rawObj);
    }

    /**
     * Returns a schema builder instance after looking up the JSON pointer.
     */
    Schema.Builder<?> lookup(String relPointerString, JsonObject ctx) {
        String absPointerString = ReferenceResolver.resolve(ls.id, relPointerString).toString();
        if (ls.pointerSchemas.containsKey(absPointerString)) {
            return ls.pointerSchemas.get(absPointerString);
        }
        boolean isExternal = !absPointerString.startsWith("#");
        JsonPointerEvaluator pointer = isExternal
                ? JsonPointerEvaluator.forURL(httpClient, absPointerString)
                : JsonPointerEvaluator.forDocument(ls.rootSchemaJson(), absPointerString);
        ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                .refValue(relPointerString);
        ls.pointerSchemas.put(absPointerString, refBuilder);
        JsonPointerEvaluator.QueryResult result = pointer.query();
        JsonObject resultObject = extend(withoutRef(ctx), result.getQueryResult());
        SchemaLoader childLoader = ls.initChildLoader()
                        .resolutionScope(isExternal ? withoutFragment(absPointerString) : ls.id)
                        .schemaJson(resultObject)
                        .rootSchemaJson(result.getContainingDocument()).build();
        Schema referredSchema = childLoader.load().build();
        refBuilder.build().setReferredSchema(referredSchema);
        return refBuilder;
    }

}
