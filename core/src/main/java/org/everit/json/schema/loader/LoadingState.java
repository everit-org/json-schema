package org.everit.json.schema.loader;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.loader.internal.ReferenceResolver;
import org.everit.json.schema.loader.internal.TypeBasedMultiplexer;
import org.json.JSONObject;
import org.json.JSONPointer;

import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections.ListUtils.unmodifiableList;

/**
 * @author erosb
 */
class LoadingState {

    public static final Comparator<Class<?>> CLASS_COMPARATOR = (cl1, cl2) -> cl1.getSimpleName().compareTo(cl2.getSimpleName());
    final SchemaClient httpClient;

    final Map<String, FormatValidator> formatValidators;

    URI id = null;

    final List<String> pointerToCurrentObj;

    final Map<String, ReferenceSchema.Builder> pointerSchemas;

    final JsonObject rootSchemaJson;

    final JsonObject schemaJson;

    LoadingState(SchemaClient httpClient,
            Map<String, FormatValidator> formatValidators,
            Map<String, ReferenceSchema.Builder> pointerSchemas,
            JsonObject rootSchemaJson,
            JsonObject schemaJson,
            URI id,
            List<String> pointerToCurrentObj) {
        this.httpClient = requireNonNull(httpClient, "httpClient cannot be null");
        this.formatValidators = requireNonNull(formatValidators, "formatValidators cannot be null");
        this.pointerSchemas = requireNonNull(pointerSchemas, "pointerSchemas cannot be null");
        this.rootSchemaJson = requireNonNull(rootSchemaJson, "rootSchemaJson cannot be null");
        this.schemaJson = requireNonNull(schemaJson, "schemaJson cannot be null");
        this.id = id;
        this.pointerToCurrentObj = unmodifiableList(new ArrayList<>(
                requireNonNull(pointerToCurrentObj, "pointerToCurrentObj cannot be null")));
    }

    LoadingState(SchemaLoader.SchemaLoaderBuilder builder) {
        this(builder.httpClient,
                builder.formatValidators,
                builder.pointerSchemas,
                builder.rootSchemaJson == null ? builder.schemaJson : builder.rootSchemaJson,
                builder.schemaJson,
                builder.id,
                builder.pointerToCurrentObj);
    }

    SchemaLoader.SchemaLoaderBuilder initChildLoader() {
//        System.out.println("initChildLoader() " + pointerToCurrentObj.stream().collect(joining(", ")));
        return SchemaLoader.builder()
                .resolutionScope(id)
                .schemaJson(schemaJson)
                .rootSchemaJson(rootSchemaJson)
                .pointerSchemas(pointerSchemas)
                .httpClient(httpClient)
                .pointerToCurrentObj(pointerToCurrentObj)
                .formatValidators(formatValidators);
    }

    Optional<FormatValidator> getFormatValidator(final String format) {
        return Optional.ofNullable(formatValidators.get(format));
    }

    public LoadingState childFor(String key) {
        List<String> newPtr = new ArrayList<>(pointerToCurrentObj.size() + 1);
        newPtr.addAll(pointerToCurrentObj);
        newPtr.add(key);
        return new LoadingState(httpClient, formatValidators, pointerSchemas, rootSchemaJson, schemaJson, id, newPtr);
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

    public SchemaException createSchemaException(String message) {
        return new SchemaException(new JSONPointer(pointerToCurrentObj), message);
    }

    public SchemaException createSchemaException(Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        return new SchemaException(new JSONPointer(pointerToCurrentObj), actualType, expectedType, furtherExpectedTypes);
    }

    public SchemaException createSchemaException(Class<?> actualType, Collection<Class<?>> expectedTypes) {
        ArrayList<Class<?>> sortedTypes = new ArrayList<>(expectedTypes);
        Collections.sort(sortedTypes, CLASS_COMPARATOR);
        return new SchemaException(new JSONPointer(pointerToCurrentObj), actualType, sortedTypes);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LoadingState that = (LoadingState) o;

        if (!httpClient.equals(that.httpClient))
            return false;
        if (!formatValidators.equals(that.formatValidators))
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;
        if (!pointerToCurrentObj.equals(that.pointerToCurrentObj))
            return false;
        if (pointerSchemas != null ? !pointerSchemas.equals(that.pointerSchemas) : that.pointerSchemas != null)
            return false;
        if (!rootSchemaJson.equals(that.rootSchemaJson))
            return false;
        return schemaJson.equals(that.schemaJson);

    }

    @Override public int hashCode() {
        int result = httpClient.hashCode();
        result = 31 * result + formatValidators.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + pointerToCurrentObj.hashCode();
        result = 31 * result + (pointerSchemas != null ? pointerSchemas.hashCode() : 0);
        result = 31 * result + rootSchemaJson.hashCode();
        result = 31 * result + schemaJson.hashCode();
        return result;
    }
}
