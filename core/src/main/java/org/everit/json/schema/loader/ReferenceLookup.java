package org.everit.json.schema.loader;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.internal.ReferenceResolver;

/**
 * @author erosb
 */
class ReferenceLookup {

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

    public ReferenceLookup(LoadingState ls) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.httpClient = ls.config.httpClient;
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
            if (obj.containsKey("$id") && obj.require("$id").requireString()
                    .equals(idAttrVal)) {
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

    private Schema.Builder<?> performQueryEvaluation(String mapKey, JsonPointerEvaluator pointerEvaluator) {
        if (ls.pointerSchemas.containsKey(mapKey)) {
            return ls.pointerSchemas.get(mapKey);
        }
        JsonValue rawInternalReferenced = pointerEvaluator.query().getQueryResult();
        ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                .refValue(mapKey);
        ls.pointerSchemas.put(mapKey, refBuilder);
        Schema referredSchema = new SchemaLoader(rawInternalReferenced.ls).load().build();
        refBuilder.build().setReferredSchema(referredSchema);
        return refBuilder;
    }

    /**
     * Returns a schema builder instance after looking up the JSON pointer.
     */
    Schema.Builder<?> lookup(String relPointerString, JsonObject ctx) {
        if (isSameDocumentRef(relPointerString)) {
            return performQueryEvaluation(relPointerString, JsonPointerEvaluator.forDocument(ls.rootSchemaJson(), relPointerString));
        }
        String absPointerString = ReferenceResolver.resolve(ls.id, relPointerString).toString();
        if (ls.pointerSchemas.containsKey(absPointerString)) {
            return ls.pointerSchemas.get(absPointerString);
        }
        JsonValue rawInternalRefereced = lookupObjById(ls.rootSchemaJson, absPointerString);
        if (rawInternalRefereced != null) {
            ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                    .refValue(relPointerString);
            ls.pointerSchemas.put(absPointerString, refBuilder);
            Schema referredSchema = new SchemaLoader(rawInternalRefereced.ls).load().build();
            refBuilder.build().setReferredSchema(referredSchema);
            return refBuilder;
        }

        boolean isInternal = isSameDocumentRef(absPointerString);
        JsonPointerEvaluator pointer = isInternal
                ? JsonPointerEvaluator.forDocument(ls.rootSchemaJson(), absPointerString)
                : JsonPointerEvaluator.forURL(httpClient, absPointerString, ls);
        ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                .refValue(relPointerString);
        ls.pointerSchemas.put(absPointerString, refBuilder);
        JsonPointerEvaluator.QueryResult result = pointer.query();

        SchemaLoader childLoader = ls.initChildLoader()
                .resolutionScope(!isInternal ? withoutFragment(absPointerString) : ls.id)
                .schemaJson(result.getQueryResult())
                .rootSchemaJson(result.getContainingDocument()).build();
        Schema referredSchema = childLoader.load().build();
        refBuilder.build().setReferredSchema(referredSchema);
        return refBuilder;
    }

    private boolean isSameDocumentRef(String ref) {
        return ref.startsWith("#");
    }

}
