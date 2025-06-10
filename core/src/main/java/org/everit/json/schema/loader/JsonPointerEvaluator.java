package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.OrgJsonUtil.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.function.Supplier;

import org.everit.json.schema.JSONPointerException;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.SchemaLocation;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author erosb
 */
class JsonPointerEvaluator {

    /**
     * Data-transfer object for holding the result of a JSON pointer query.
     */
    static class QueryResult {

        private final JsonObject containingDocument;

        private final JsonValue queryResult;

        /**
         * Constructor.
         *
         * @param containingDocument
         *         the JSON document which contains the query result.
         * @param queryResult
         *         the JSON object being the result of the query execution.
         */
        QueryResult(JsonObject containingDocument, JsonValue queryResult) {
            this.containingDocument = requireNonNull(containingDocument, "containingDocument cannot be null");
            this.queryResult = requireNonNull(queryResult, "queryResult cannot be null");
        }

        /**
         * Getter for {@link #containingDocument}.
         *
         * @return the JSON document which contains the query result.
         */
        public JsonObject getContainingDocument() {
            return containingDocument;
        }

        /**
         * Getter for {@link #queryResult}.
         *
         * @return the JSON object being the result of the query execution.
         */
        public JsonValue getQueryResult() {
            return queryResult;
        }

    }

    private static JsonObject executeWith(final SchemaClient client, final String url) {
        //String resp = null;
        //Applied the REFACTORING method: "Rename variable" to variables resp & reader
        /*rename variable to a more meaning full name */
        String response = null; 

        BufferedReader buffReader = null;
        
        //InputStreamReader reader = null;
        InputStreamReader inputReader = null; 
        try {
            InputStream responseStream = client.get(url);
            inputReader = new InputStreamReader(responseStream, Charset.defaultCharset());
            buffReader = new BufferedReader(inputReader);
            String line;
            StringBuilder strBuilder = new StringBuilder();
            while ((line = buffReader.readLine()) != null) {
                strBuilder.append(line);
            }
            response = strBuilder.toString();
            return new JsonObject(toMap(new JSONObject(new JSONTokener(response))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (JSONException e) {
            throw new SchemaException(url, e);
        } finally {
            try {
                if (buffReader != null) {
                    buffReader.close();
                }
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    static final JsonPointerEvaluator forDocument(JsonObject document, String fragment) {
        return new JsonPointerEvaluator(() -> document, fragment);
    }

    private static JsonObject configureBasedOnState(JsonObject obj, LoadingState callingState, String id) {
        URI documentURI = validateURI(callingState, id).asJavaURI();
        obj.ls = callingState.createCopyForNewSchemaJson(documentURI, obj, new SchemaLocation(documentURI, emptyList()));
        return obj;
    }

    static final JsonPointerEvaluator forURL(SchemaClient schemaClient, String url, LoadingState callingState) {
        Uri uri = validateURI(callingState, url);
        return new JsonPointerEvaluator(
                () -> configureBasedOnState(executeWith(schemaClient, uri.toBeQueried.toString()), callingState,
                        uri.toBeQueried.toString()),
                uri.fragment);
    }

    private static Uri validateURI(LoadingState callingState, String toBeQueried) {
        try {
            return Uri.parse(toBeQueried);
        } catch (URISyntaxException e) {
            throw callingState.createSchemaException(e);
        }
    }

    private final Supplier<JsonObject> documentProvider;

    private final String fragment;

    JsonPointerEvaluator(Supplier<JsonObject> documentProvider, String fragment) {
        this.documentProvider = documentProvider;
        this.fragment = fragment;
    }

    /**
     * Queries from {@code document} based on this pointer.
     *
     * @return a DTO containing the query result and the root document containing the query result.
     * @throws IllegalArgumentException
     *         if the pointer does not start with {@code '#'}.
     */
    public QueryResult query() {
        JsonObject document = documentProvider.get();
        if (fragment.isEmpty()) {
            return new QueryResult(document, document);
        }
        JsonObject foundById = ReferenceLookup.lookupObjById(document, fragment);
        if (foundById != null) {
            return new QueryResult(document, foundById);
        }
        String[] path = fragment.split("/");
        if ((path[0] == null) || !path[0].startsWith("#")) {
            throw new IllegalArgumentException("JSON pointers must start with a '#'");
        }
        try {
            JsonValue result;
            LinkedList<String> tokens = new LinkedList<>(asList(path));
            tokens.poll();
            if (tokens.isEmpty()) {
                result = document;
            } else {
                result = queryFrom(document, tokens);
            }
            return new QueryResult(document, result);
        } catch (JSONPointerException e) {
            throw new SchemaException(e.getMessage());
        }
    }

    private String unescape(String token) {
        try {
            return URLDecoder.decode(token, "utf-8").replace("~1", "/").replace("~0", "~").replace("\\\"", "\"").replace("\\\\", "\\");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonValue queryFrom(JsonValue document, LinkedList<String> tokens) {
        String key = unescape(tokens.poll());
        JsonValue next = document.canBeMappedTo(JsonObject.class, obj -> obj.childFor(key))
                .orMappedTo(JsonArray.class, arr -> arr.at(Integer.parseInt(key)))
                .requireAny();

        if (tokens.isEmpty()) {
            return next;
        }
        return queryFrom(next, tokens);
    }

}
