package org.everit.json.schema.loader;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.everit.json.schema.SchemaException;
import org.everit.json.schema.SchemaLocation;
import org.everit.json.schema.loader.internal.ReferenceResolver;

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

    static final Comparator<Class<?>> CLASS_COMPARATOR = Comparator.comparing(Class::getSimpleName);

    final LoaderConfig config;

    final URI id;

    final SchemaLocation pointerToCurrentObj;

    final Map<String, ReferenceKnot> pointerSchemas;

    final JsonValue rootSchemaJson;

    final JsonValue schemaJson;

    final Map<JsonValue, SubschemaRegistry> subschemaRegistries;

    LoadingState(LoaderConfig config,
        Map<String, ReferenceKnot> pointerSchemas,
        Object rootSchemaJson,
        Object schemaJson,
        URI parentScopeId,
        SchemaLocation pointerToCurrentObj) {
        this(config, pointerSchemas, rootSchemaJson, schemaJson, parentScopeId, pointerToCurrentObj, new HashMap<>());
    }

    LoadingState(LoaderConfig config,
            Map<String, ReferenceKnot> pointerSchemas,
            Object rootSchemaJson,
            Object schemaJson,
            URI parentScopeId,
            SchemaLocation pointerToCurrentObj,
            Map<JsonValue, SubschemaRegistry> subschemaRegistries) {
        this.config = config;
        this.pointerSchemas = requireNonNull(pointerSchemas, "pointerSchemas cannot be null");
        this.id = extractChildId(parentScopeId, schemaJson, config.specVersion.idKeyword());
        this.pointerToCurrentObj = requireNonNull(pointerToCurrentObj, "pointerToCurrentObj cannot be null");
        this.subschemaRegistries = requireNonNull(subschemaRegistries, "subschemaRegistries cannot be null");
        this.rootSchemaJson = JsonValue.of(rootSchemaJson);
        if (this.rootSchemaJson.ls == null) {
            this.rootSchemaJson.ls = this;
        }
        this.schemaJson = JsonValue.of(schemaJson);
        this.schemaJson.ls = this;
    }

    SchemaLoader.SchemaLoaderBuilder initNewDocumentLoader() {
        return config.initLoader().pointerSchemas(pointerSchemas).subschemaRegistries(subschemaRegistries);
    }

    private Object getRawChildOfObject(JsonObject obj, String key) {
        Map<String, Object> rawMap = (Map<String, Object>) obj.unwrap();
        if (!rawMap.containsKey(key)) {
            throw createSchemaException(format("key [%s] not found", key));
        }
        return rawMap.get(key);
    }

    private Object getRawElemOfArray(JsonArray array, String rawIndex) {
        List<?> raw = (List<?>) array.unwrap();
        try {
            int index = Integer.parseInt(rawIndex);
            if (raw.size() <= index) {
                throw createSchemaException(format("array index [%d] is out of bounds", index));
            }
            return raw.get(index);
        } catch (NumberFormatException e) {
            throw createSchemaException(format("[%s] is not an array index", rawIndex));
        }
    }

    JsonValue childFor(String key) {
        Object rawChild = schemaJson
                .canBeMappedTo(JsonObject.class, obj -> getRawChildOfObject(obj, key))
                .orMappedTo(JsonArray.class, array -> getRawElemOfArray(array, key))
                .requireAny();

        LoadingState childLs = new LoadingState(
                config,
                pointerSchemas,
                rootSchemaJson,
                rawChild,
                id,
                pointerToCurrentObj.addPointerSegment(key),
                subschemaRegistries
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
        return pointerToCurrentObj.toString();
    }

    SchemaException createSchemaException(String message) {
        return new SchemaException(locationOfCurrentObj(), message);
    }

    SchemaException createSchemaException(Exception cause) {
        return new SchemaException(locationOfCurrentObj(), cause);
    }

    SchemaException createSchemaException(Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        return new SchemaException(locationOfCurrentObj(), actualType, expectedType, furtherExpectedTypes);
    }

    LoadingState createCopyForNewSchemaJson(URI parentScopeId, JsonValue newRootJson, SchemaLocation locationOfNewRootJson) {
        return new LoadingState(config, pointerSchemas, newRootJson, newRootJson, parentScopeId, locationOfNewRootJson, subschemaRegistries);
    }

    SchemaException createSchemaException(Class<?> actualType, Collection<Class<?>> expectedTypes) {
        ArrayList<Class<?>> sortedTypes = new ArrayList<>(expectedTypes);
        Collections.sort(sortedTypes, CLASS_COMPARATOR);
        return new SchemaException(locationOfCurrentObj(), actualType, sortedTypes);
    }

    SpecificationVersion specVersion() {
        return config.specVersion;
    }

    SubschemaRegistry getSubschemaRegistry(JsonValue rootJson) {
        return subschemaRegistries.computeIfAbsent(rootJson, SubschemaRegistry::new);
    }
}
