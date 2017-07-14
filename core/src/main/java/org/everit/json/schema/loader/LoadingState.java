package org.everit.json.schema.loader;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections.ListUtils.unmodifiableList;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_6;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.loader.internal.ReferenceResolver;
import org.json.JSONPointer;

/**
 * @author erosb
 */
class LoadingState {

    static URI extractChildId(URI parentScopeId, Object childJson, String idKeyword) {
        if (childJson instanceof JsonObject) {
            childJson = ((JsonObject) childJson).toMap();
        }
        if (childJson instanceof Map) {
            Map<String, Object> child = (Map<String, Object>) childJson;
            Object childId = child.get(idKeyword);
            if (childId instanceof String) {
                return ReferenceResolver.resolve(parentScopeId, (String) childId);
            }
        }
        return parentScopeId;
    }

    static final Comparator<Class<?>> CLASS_COMPARATOR = (cl1, cl2) -> cl1.getSimpleName().compareTo(cl2.getSimpleName());

    final LoaderConfig config;

    final URI id;

    final List<String> pointerToCurrentObj;

    final Map<String, ReferenceSchema.Builder> pointerSchemas;

    final JsonValue rootSchemaJson;

    final JsonValue schemaJson;

    LoadingState(LoaderConfig config,
            Map<String, ReferenceSchema.Builder> pointerSchemas,
            Object rootSchemaJson,
            Object schemaJson,
            URI parentScopeId,
            List<String> pointerToCurrentObj) {
        this.config = config;
        this.pointerSchemas = requireNonNull(pointerSchemas, "pointerSchemas cannot be null");
        this.id = extractChildId(parentScopeId, schemaJson, config.specVersion.idKeyword());
        this.pointerToCurrentObj = unmodifiableList(new ArrayList<>(
                requireNonNull(pointerToCurrentObj, "pointerToCurrentObj cannot be null")));
        this.rootSchemaJson = JsonValue.of(rootSchemaJson);
        if (this.rootSchemaJson.ls == null) {
            this.rootSchemaJson.ls = this;
        }
        this.schemaJson = JsonValue.of(schemaJson);
        this.schemaJson.ls = this;
    }

    SchemaLoader.SchemaLoaderBuilder initChildLoader() {
        SchemaLoader.SchemaLoaderBuilder rval = SchemaLoader.builder()
                .httpClient(this.config.httpClient)
                .formatValidators(this.config.formatValidators)
                .resolutionScope(id)
                .schemaJson(schemaJson)
                .rootSchemaJson(rootSchemaJson)
                .pointerSchemas(pointerSchemas)
                .pointerToCurrentObj(pointerToCurrentObj);
        if (DRAFT_6.equals(specVersion())) {
            rval.draftV6Support();
        }
        return rval;
    }

    JsonValue childFor(String key) {
        List<String> newPtr = new ArrayList<>(pointerToCurrentObj.size() + 1);
        newPtr.addAll(pointerToCurrentObj);
        newPtr.add(key);

        Object rawChild = schemaJson
                .canBeMappedTo(JsonObject.class, schemaJsonObj -> ((Map<String, Object>) schemaJsonObj.unwrap()).get(key))
                .orMappedTo(JsonArray.class, schemaJsonArray -> ((List<?>) schemaJsonArray.unwrap()).get(Integer.parseInt(key)))
                .requireAny();

        LoadingState childLs = new LoadingState(
                config,
                pointerSchemas,
                rootSchemaJson,
                rawChild,
                id,
                newPtr
        );
        return childLs.schemaJson;
    }

    JsonValue childFor(int arrayIndex) {
        return childFor(String.valueOf(arrayIndex));
    }

    JsonObject schemaJson() {
        return schemaJson.requireObject();
    }

    JsonObject rootSchemaJson() {
        return rootSchemaJson.requireObject();
    }

    String locationOfCurrentObj() {
        return new JSONPointer(pointerToCurrentObj).toURIFragment();
    }

    SchemaException createSchemaException(String message) {
        return new SchemaException(locationOfCurrentObj(), message);
    }

    SchemaException createSchemaException(Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        return new SchemaException(locationOfCurrentObj(), actualType, expectedType, furtherExpectedTypes);
    }

    SchemaException createSchemaException(Class<?> actualType, Collection<Class<?>> expectedTypes) {
        ArrayList<Class<?>> sortedTypes = new ArrayList<>(expectedTypes);
        Collections.sort(sortedTypes, CLASS_COMPARATOR);
        return new SchemaException(locationOfCurrentObj(), actualType, sortedTypes);
    }

    SpecificationVersion specVersion() {
        return config.specVersion;
    }
}
