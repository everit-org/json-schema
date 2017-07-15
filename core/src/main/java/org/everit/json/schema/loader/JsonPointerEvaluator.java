package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.function.Supplier;

import org.everit.json.schema.SchemaException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointerException;
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
        String resp = null;
        BufferedReader buffReader = null;
        InputStreamReader reader = null;
        try {
            InputStream responseStream = client.get(url);
            reader = new InputStreamReader(responseStream, Charset.defaultCharset());
            buffReader = new BufferedReader(reader);
            String line;
            StringBuilder strBuilder = new StringBuilder();
            while ((line = buffReader.readLine()) != null) {
                strBuilder.append(line);
            }
            resp = strBuilder.toString();
            return new JsonObject(new JSONObject(new JSONTokener(resp)).toMap());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (JSONException e) {
            throw new SchemaException("failed to parse " + resp, e);
        } finally {
            try {
                if (buffReader != null) {
                    buffReader.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    static final JsonPointerEvaluator forDocument(JsonObject document, String fragment) {
        return new JsonPointerEvaluator(() -> document, fragment);
    }

    private static JsonObject configureBasedOnState(JsonObject obj, LoadingState callingState) {
        obj.ls = new LoadingState(callingState.config, callingState.pointerSchemas, obj, obj, null, emptyList());
        return obj;
    }

    static final JsonPointerEvaluator forURL(SchemaClient schemaClient, String url, LoadingState callingState) {
        int poundIdx = url.indexOf('#');
        String fragment;
        String toBeQueried;
        if (poundIdx == -1) {
            toBeQueried = url;
            fragment = "";
        } else {
            fragment = url.substring(poundIdx);
            toBeQueried = url.substring(0, poundIdx);
        }
        return new JsonPointerEvaluator(() -> configureBasedOnState(executeWith(schemaClient, toBeQueried), callingState), fragment);
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
