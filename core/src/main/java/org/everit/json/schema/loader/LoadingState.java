package org.everit.json.schema.loader;

import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.loader.internal.ReferenceResolver;
import org.json.JSONPointer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections.ListUtils.unmodifiableList;

/**
 * @author erosb
 */
class LoadingState {

    static final Comparator<Class<?>> CLASS_COMPARATOR = (cl1, cl2) -> cl1.getSimpleName().compareTo(cl2.getSimpleName());

    URI id = null;

    final List<String> pointerToCurrentObj;

    final Map<String, ReferenceSchema.Builder> pointerSchemas;

    final JsonValue rootSchemaJson;

    final JsonValue schemaJson;

    LoadingState(Map<String, ReferenceSchema.Builder> pointerSchemas,
            JsonValue rootSchemaJson,
            JsonValue schemaJson,
            URI id,
            List<String> pointerToCurrentObj) {
        this.pointerSchemas = requireNonNull(pointerSchemas, "pointerSchemas cannot be null");
        this.rootSchemaJson = requireNonNull(rootSchemaJson, "rootSchemaJson cannot be null");
        this.schemaJson = requireNonNull(schemaJson, "schemaJson cannot be null");
        this.id = id;
        this.pointerToCurrentObj = unmodifiableList(new ArrayList<>(
                requireNonNull(pointerToCurrentObj, "pointerToCurrentObj cannot be null")));
    }

    LoadingState(SchemaLoader.SchemaLoaderBuilder builder) {
        this(builder.pointerSchemas,
             builder.rootSchemaJson == null ? builder.schemaJson : builder.rootSchemaJson,
             builder.schemaJson,
             builder.id,
             builder.pointerToCurrentObj);
    }

    SchemaLoader.SchemaLoaderBuilder initChildLoader() {
        return SchemaLoader.builder()
                .resolutionScope(id)
                .schemaJson(schemaJson)
                .rootSchemaJson(rootSchemaJson)
                .pointerSchemas(pointerSchemas)
                .pointerToCurrentObj(pointerToCurrentObj);
    }

    public LoadingState childFor(String key) {
        List<String> newPtr = new ArrayList<>(pointerToCurrentObj.size() + 1);
        newPtr.addAll(pointerToCurrentObj);
        newPtr.add(key);
        return new LoadingState(
                pointerSchemas,
                rootSchemaJson,
                schemaJson,
                id,
                newPtr
        );
    }

    public LoadingState childFor(int arrayIndex) {
        return childFor(String.valueOf(arrayIndex));
    }

    public LoadingState childForId(Object idAttr) {
        URI childId = idAttr == null || !(idAttr instanceof String)
                ? this.id
                : ReferenceResolver.resolve(this.id, (String) idAttr);
        return new LoadingState(initChildLoader().resolutionScope(childId));
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

    public SchemaException createSchemaException(String message) {
        return new SchemaException(locationOfCurrentObj(), message);
    }

    public SchemaException createSchemaException(Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        return new SchemaException(locationOfCurrentObj(), actualType, expectedType, furtherExpectedTypes);
    }

    public SchemaException createSchemaException(Class<?> actualType, Collection<Class<?>> expectedTypes) {
        ArrayList<Class<?>> sortedTypes = new ArrayList<>(expectedTypes);
        Collections.sort(sortedTypes, CLASS_COMPARATOR);
        return new SchemaException(locationOfCurrentObj(), actualType, sortedTypes);
    }
}
