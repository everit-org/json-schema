package org.everit.json.schema.loader;

import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.OrgJsonUtil.toMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaLocation;
import org.everit.json.schema.loader.internal.ReferenceResolver;
import org.json.JSONObject;

class ReferenceKnot {

    private Schema referredSchema;

    private final List<ReferenceSchema.Builder> refs = new ArrayList<>(1);

    ReferenceSchema.Builder initReference(String refValue) {
        ReferenceSchema.Builder builder = new ReferenceSchema.Builder().refValue(refValue);
        if (referredSchema != null) {
            builder.build().setReferredSchema(referredSchema);
        }
        refs.add(builder);
        return builder;
    }

    void resolveWith(Schema referredSchema) {
        refs.forEach(ref -> ref.build().setReferredSchema(referredSchema));
        this.referredSchema = referredSchema;
    }

}

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
    static JSONObject extend(JSONObject additional, JSONObject original) {
        return new JSONObject(extend(toMap(additional), toMap(original)));
    }

    static Map<String, Object> extend(Map<String, Object> additional, Map<String, Object> original) {
        if (additional.keySet().isEmpty()) {
            return original;
        }
        if (original.keySet().isEmpty()) {
            return additional;
        }
        Map<String, Object> rawObj = new HashMap<>();
        original.forEach(rawObj::put);
        additional.forEach(rawObj::put);
        return rawObj;
    }

    static JsonObject lookupObjById(JsonValue val, String idAttrVal) {
        return val.ls.getSubschemaRegistry(val).getById(idAttrVal);
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

    private final LoadingState ls;

    private final SchemaClient schemaClient;

    public ReferenceLookup(LoadingState ls) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.schemaClient = ls.config.schemaClient;
    }

    private Map<String, Object> doExtend(Map<String, Object> additional, Map<String, Object> original) {
        if (ls.specVersion() == SpecificationVersion.DRAFT_4) {
            return extend(additional, original);
        } else {
            return original;
        }
    }

    Map<String, Object> withoutRef(JsonObject original) {
        Map<String, Object> rawObj = new HashMap<>();
        original.keySet().stream()
                .filter(name -> !"$ref".equals(name))
                .forEach(name -> rawObj.put(name, original.get(name)));
        return rawObj;
    }

    private Schema.Builder<?, ?> performQueryEvaluation(String mapKey, JsonPointerEvaluator pointerEvaluator) {
        String absolutePointer = ReferenceResolver.resolve(ls.id, mapKey).toString();
        if (ls.pointerSchemas.containsKey(absolutePointer)) {
            return ls.pointerSchemas.get(absolutePointer).initReference(absolutePointer);
        }
        JsonValue referencedRawSchema = pointerEvaluator.query().getQueryResult();
        return createReferenceSchema(mapKey, absolutePointer, referencedRawSchema);
    }

    /**
     * Returns a schema builder instance after looking up the JSON pointer.
     */
    Schema.Builder<?, ?> lookup(String relPointerString, JsonObject ctx) {
        String absPointerString = ReferenceResolver.resolve(ls.id, relPointerString).toString();
        if (ls.pointerSchemas.containsKey(absPointerString)) {
            return ls.pointerSchemas.get(absPointerString).initReference(absPointerString);
        }
        JsonValue rawInternalReferenced = lookupObjById(ls.rootSchemaJson, absPointerString);
        if (rawInternalReferenced != null) {
            return createReferenceSchema(relPointerString, absPointerString, rawInternalReferenced);
        }
        if (isSameDocumentRef(relPointerString)) {
            return performQueryEvaluation(relPointerString, JsonPointerEvaluator.forDocument(ls.rootSchemaJson(), relPointerString));
        }
        JsonPointerEvaluator pointer = createPointerEvaluator(absPointerString);
        ReferenceKnot knot = new ReferenceKnot();
        ReferenceSchema.Builder refBuilder = knot.initReference(relPointerString);
        ls.pointerSchemas.put(absPointerString, knot);
        JsonPointerEvaluator.QueryResult result = pointer.query();

        URI resolutionScope = !isSameDocumentRef(absPointerString) ? withoutFragment(absPointerString) : ls.id;
        JsonObject containingDocument = result.getContainingDocument();
        SchemaLocation resultLocation = result.getQueryResult().ls.pointerToCurrentObj;
        SchemaLoader childLoader = ls.initNewDocumentLoader()
                .resolutionScope(resolutionScope)
                .pointerToCurrentObj(resultLocation)
                .schemaJson(result.getQueryResult())
                .rootSchemaJson(containingDocument).build();
        Schema referredSchema = childLoader.load().build();
        refBuilder.schemaLocation(resultLocation);
        knot.resolveWith(referredSchema);
        return refBuilder;
    }

    private Schema.Builder<?, ?> createReferenceSchema(String relPointerString, String absPointerString, JsonValue rawReferenced) {
        ReferenceKnot knot = new ReferenceKnot();
        ReferenceSchema.Builder refBuilder = knot.initReference(relPointerString);
        ls.pointerSchemas.put(absPointerString, knot);
        Schema referredSchema = new SchemaLoader(rawReferenced.ls).load().build();
        knot.resolveWith(referredSchema);
        return refBuilder;
    }

    private JsonObject initJsonObjectById(URI id) {
        JsonObject o = JsonValue.of(ls.config.schemasByURI.get(id)).requireObject();
        ls.createCopyForNewSchemaJson(id, o, SchemaLocation.parseURI(id.toString()));
        return o;
    }

    private JsonPointerEvaluator createPointerEvaluator(String absPointerString) {
        if (isSameDocumentRef(absPointerString)) {
            return JsonPointerEvaluator.forDocument(ls.rootSchemaJson(), absPointerString);
        }
        try {
            Uri uri = Uri.parse(absPointerString);
            if (ls.config.schemasByURI.containsKey(uri.asJavaURI())) {
                JsonObject o = initJsonObjectById(uri.asJavaURI());
                return JsonPointerEvaluator.forDocument(o, "#");
            } else if (ls.config.schemasByURI.containsKey(uri.toBeQueried)) {
                JsonObject o = initJsonObjectById(uri.toBeQueried);
                return JsonPointerEvaluator.forDocument(o, uri.fragment);
            }
        } catch (URISyntaxException e) {
            throw ls.createSchemaException(e);
        }
        return JsonPointerEvaluator.forURL(schemaClient, absPointerString, ls);
    }

    private boolean isSameDocumentRef(String ref) {
        return ref.startsWith("#");
    }

}
