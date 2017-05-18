package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.json.JSONPointerException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class JsonPointerEvaluator {

    /**
     * Data-transfer object for holding the result of a JSON pointer query.
     */
    static class QueryResult {

        private final JsonObject containingDocument;

        private final JsonObject queryResult;

        /**
         * Constructor.
         *
         * @param containingDocument
         *         the JSON document which contains the query result.
         * @param queryResult
         *         the JSON object being the result of the query execution.
         */
        QueryResult(JsonObject containingDocument, JsonObject queryResult) {
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
        public JsonObject getQueryResult() {
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

    static final JsonPointerEvaluator forURL(SchemaClient schemaClient, String url) {
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
        return new JsonPointerEvaluator(() -> executeWith(schemaClient, toBeQueried), fragment);
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
            JsonObject result = queryFrom(document);
            return new QueryResult(document, result);
        } catch (JSONPointerException e) {
            throw new SchemaException(e.getMessage());
        }
    }

    private JsonObject queryFrom(JsonObject document) {
        JSONObject docAsJSONObj = new JSONObject(document.toMap());
        JSONObject resultAsJSONObj = (JSONObject) new JSONPointer(fragment).queryFrom(docAsJSONObj);
        if (resultAsJSONObj == null) {
            throw new JSONPointerException(format("could not query schema document by pointer [%s]", fragment));
        } else {
            return new JsonObject(resultAsJSONObj.toMap());
        }
    }

}
