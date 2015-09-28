/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jsonvalidator.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.everit.jsonvalidator.SchemaException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * JSON pointer implementation.
 */
public class JSONPointer {

  /**
   * Data-transfer object for holding the result of a JSON pointer query.
   */
  public static class QueryResult {

    private final JSONObject containingDocument;

    private final JSONObject queryResult;

    /**
     * Constructor.
     */
    public QueryResult(final JSONObject containingDocument, final JSONObject queryResult) {
      this.containingDocument = Objects.requireNonNull(containingDocument,
          "containingDocument cannot be null");
      this.queryResult = Objects.requireNonNull(queryResult, "queryResult cannot be null");
    }

    /**
     * The JSON document which contains the query result.
     */
    public JSONObject getContainingDocument() {
      return containingDocument;
    }

    /**
     * The JSON object being the result of the query execution.
     */
    public JSONObject getQueryResult() {
      return queryResult;
    }

  }

  private static JSONObject executeWith(final HttpClient client, final String url) {
    String resp = null;
    BufferedReader buffReader = null;
    InputStreamReader reader = null;
    try {
      HttpResponse response = client.execute(new HttpGet(url));
      InputStream content = response.getEntity().getContent();
      int capacity = (int) response.getEntity().getContentLength();
      reader = new InputStreamReader(content, Charset.defaultCharset());
      buffReader = new BufferedReader(reader);
      String line;
      StringBuilder strBuilder = new StringBuilder(capacity);
      while ((line = buffReader.readLine()) != null) {
        strBuilder.append(line);
      }
      resp = strBuilder.toString();
      return new JSONObject(new JSONTokener(resp));
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

  public static final JSONPointer forDocument(final JSONObject document, final String fragment) {
    return new JSONPointer(() -> document, fragment);
  }

  /**
   * Static factory method.
   */
  public static final JSONPointer forURL(final HttpClient httpClient, final String url) {
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
    return new JSONPointer(() -> executeWith(httpClient, toBeQueried), fragment);
  }

  private final Supplier<JSONObject> documentProvider;

  private final String fragment;

  public JSONPointer(final Supplier<JSONObject> documentProvider, final String fragment) {
    this.documentProvider = documentProvider;
    this.fragment = fragment;
  }

  /**
   * Queries from {@code document} based on this pointer.
   *
   * @throws IllegalArgumentException
   *           if the pointer does not start with {@code '#'}.
   */
  public QueryResult query() {
    JSONObject document = documentProvider.get();
    if (fragment.isEmpty()) {
      return new QueryResult(document, document);
    }
    String[] path = fragment.split("/");
    if (!"#".equals(path[0])) {
      throw new IllegalArgumentException("JSON pointers must start with a '#'");
    }
    Object current = document;
    for (int i = 1; i < path.length; ++i) {
      String segment = unescape(path[i]);
      if (current instanceof JSONObject) {
        if (!((JSONObject) current).has(segment)) {
          throw new SchemaException(String.format(
              "failed to resolve JSON pointer [%s]. Segment [%s] not found in %s", fragment,
              segment, document.toString()));
        }
        current = ((JSONObject) current).get(segment);
      } else if (current instanceof JSONArray) {
        current = ((JSONArray) current).get(Integer.parseInt(segment));
      }
    }
    return new QueryResult(document, (JSONObject) current);
  }

  private String unescape(final String segment) {
    return segment.replace("~1", "/").replace("~0", "~").replace("%25", "%");
  }

}
