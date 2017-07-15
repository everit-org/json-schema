package org.everit.json.schema.loader;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.everit.json.schema.loader.internal.ReferenceResolver;
import org.json.JSONObject;

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
        return new JSONObject(extend(additional.toMap(), original.toMap()));
    }

    static Map<String, Object> extend(Map<String, Object> additional, Map<String, Object> original) {
        if (additional.keySet().isEmpty()) {
            return original;
        }
        if (original.keySet().isEmpty()) {
            return additional;
        }
        Map<String, Object> rawObj = new HashMap<>();
        original.keySet().stream().forEach(name -> rawObj.put(name, original.get(name)));
        additional.keySet().stream().forEach(name -> rawObj.put(name, additional.get(name)));
        return rawObj;
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

    private Map<String, Object> doExtend(Map<String, Object> additional, Map<String, Object> original) {
        if (ls.specVersion() == SpecificationVersion.DRAFT_4) {
            return extend(additional, original);
        } else {
            return original;
        }
    }

    /**
     * Returns the absolute URI without its fragment part.
     *
     * @param fullUri
     *         the abslute URI
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

    Map<String, Object> withoutRef(JsonObject original) {
        Map<String, Object> rawObj = new HashMap<>();
        original.keySet().stream()
                .filter(name -> !"$ref".equals(name))
                .forEach(name -> rawObj.put(name, original.get(name)));
        return rawObj;
    }

    private JsonObject lookupObjById(JsonValue val, String idAttrVal) {
        if (val instanceof JsonObject) {
            JsonObject obj = (JsonObject) val;
            if (obj.containsKey("$id") && obj.require("$id").requireString().equals(idAttrVal)) {
                return obj;
            }
            for (String key : obj.keySet()) {
                JsonObject maybeFound = lookupObjById(obj.require(key), idAttrVal);
                if (maybeFound != null) {
                    return maybeFound;
                }
            }
        } else if (val instanceof JsonArray) {
            JsonArray arr = (JsonArray) val;
            for (int i = 0; i < arr.length(); ++i) {
                JsonObject maybeFound = lookupObjById(arr.at(i), idAttrVal);
                if (maybeFound != null) {
                    return maybeFound;
                }
            }
        }

        return null;
    }

    /**
     * Returns a schema builder instance after looking up the JSON pointer.
     */
    Schema.Builder<?> lookup(String relPointerString, JsonObject ctx) {
        if (isSameDocumentRef(relPointerString)) {
            if (ls.pointerSchemas.containsKey(relPointerString)) {
                return ls.pointerSchemas.get(relPointerString);
            }
            JsonValue rawInternalReferenced = JsonPointerEvaluator.forDocument(ls.rootSchemaJson(), relPointerString).query()
                    .getQueryResult();
            System.out.println("rawInternalReferenced.ls.id = " + rawInternalReferenced.ls.id);
            Object resultObject;
            if (rawInternalReferenced instanceof JsonObject) {
                resultObject = doExtend(withoutRef(ctx), ((JsonObject) rawInternalReferenced).toMap());
            } else {
                resultObject = rawInternalReferenced;
            }
            ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                    .refValue(relPointerString);
            ls.pointerSchemas.put(relPointerString, refBuilder);
            System.out.println("rawInternalConfig: " + rawInternalReferenced.ls.specVersion());
            Schema referredSchema = new SchemaLoader(rawInternalReferenced.ls).load().build();
            //            rawInternalReferenced.ls.initChildLoader()
            //                    .pointerToCurrentObj(rawInternalReferenced.ls.pointerToCurrentObj)
            //                    .schemaJson(resultObject)
            //                    .build().load().build();
            refBuilder.build().setReferredSchema(referredSchema);
            return refBuilder;
        }
        System.out.println("to abs: " + ls.id + " " + relPointerString);
        String absPointerString = ReferenceResolver.resolve(ls.id, relPointerString).toString();
        if (ls.pointerSchemas.containsKey(absPointerString)) {
            return ls.pointerSchemas.get(absPointerString);
        }

        System.out.println("---lookup start---");
        JsonValue rawInternalRefereced = lookupObjById(ls.rootSchemaJson, absPointerString);
        System.out.println("---lookup end---");
        if (rawInternalRefereced != null) {
            ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                    .refValue(relPointerString);
            ls.pointerSchemas.put(absPointerString, refBuilder);
            Schema referredSchema = ls.initChildLoader()
                    .pointerToCurrentObj(rawInternalRefereced.ls.pointerToCurrentObj)
                    .schemaJson(rawInternalRefereced)
                    .build().load().build();
            refBuilder.build().setReferredSchema(referredSchema);
            return refBuilder;
        }

        boolean isExternal = !absPointerString.startsWith("#");
        JsonPointerEvaluator pointer = isExternal
                ? JsonPointerEvaluator.forURL(httpClient, absPointerString, ls)
                : JsonPointerEvaluator.forDocument(ls.rootSchemaJson(), absPointerString);
        ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                .refValue(relPointerString);
        ls.pointerSchemas.put(absPointerString, refBuilder);
        JsonPointerEvaluator.QueryResult result = pointer.query();
        Object resultObject;
        if (result.getQueryResult() instanceof JsonObject) {
            resultObject = doExtend(withoutRef(ctx), ((JsonObject) result.getQueryResult()).toMap());
        } else {
            resultObject = result.getQueryResult();
        }
        //        JsonValue resultObject = extend(withoutRef(ctx), result.getQueryResult());
        SchemaLoader childLoader = ls.initChildLoader()
                .resolutionScope(isExternal ? withoutFragment(absPointerString) : ls.id)
                .schemaJson(resultObject)
                .rootSchemaJson(result.getContainingDocument()).build();
        Schema referredSchema = childLoader.load().build();
        refBuilder.build().setReferredSchema(referredSchema);
        return refBuilder;
    }

    private boolean isSameDocumentRef(String ref) {
        return ref.startsWith("#");
    }

}
