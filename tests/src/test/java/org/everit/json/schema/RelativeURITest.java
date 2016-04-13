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
package org.everit.json.schema;

import java.net.URISyntaxException;

import org.eclipse.jetty.server.Server;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

public class RelativeURITest {

  private Server server;

  @Test
  public void test() throws URISyntaxException {
    ServletSupport.withDocumentRoot("/org/everit/json/schema/relative-uri/")
        .run(this::run);
  }

  private void run() {
    SchemaLoader
        .builder()
        .resolutionScope("http://localhost:1234/schema/")
        .schemaJson(
            new JSONObject(new JSONTokener(getClass().getResourceAsStream(
                "/org/everit/json/schema/relative-uri/schema/main.json"))))
            .build().load().build();
  }
}
