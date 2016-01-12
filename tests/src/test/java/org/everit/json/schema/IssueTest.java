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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class IssueTest {

  @Parameters
  public static List<Object[]> params() {
    List<Object[]> rval = new ArrayList<>();
    try {
      File issuesDir = new File(
          IssueTest.class.getResource("/org/everit/json/schema/issues").toURI());
      for (File issue : issuesDir.listFiles()) {
        rval.add(new Object[] { issue });
      }
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return rval;
  }

  private final File issueDir;

  private Server server;

  public IssueTest(final File issueDir) {
    this.issueDir = Objects.requireNonNull(issueDir, "issueDir cannot be null");
  }

  private Optional<File> fileByName(final String fileName) {
    return Arrays.stream(issueDir.listFiles())
        .filter(file -> file.getName().equals(fileName))
        .findFirst();
  }

  private void initJetty(final File documentRoot) {
    server = new Server(1234);
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);
    handler.addServletWithMapping(new ServletHolder(new IssueServlet(documentRoot)), "/*");
    try {
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Schema loadSchema() {
    Optional<File> schemaFile = fileByName("schema.json");
    try {
      if (schemaFile.isPresent()) {
        JSONObject schemaObj = new JSONObject(
            new JSONTokener(new FileInputStream(schemaFile.get())));
        return SchemaLoader.load(schemaObj);
      }
      throw new RuntimeException(issueDir.getCanonicalPath() + "/schema.json is not found");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void stopJetty() {
    if (server != null) {
      try {
        server.stop();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    server = null;
  }

  @Test
  public void test() {
    fileByName("remotes").ifPresent(this::initJetty);
    Schema schema = loadSchema();
    fileByName("subject-valid.json").ifPresent(file -> validate(file, schema, true));
    fileByName("subject-invalid.json").ifPresent(file -> validate(file, schema, false));
    stopJetty();
  }

  private void validate(final File file, final Schema schema, final boolean shouldBeValid) {
    ValidationException thrown = null;
    try {
      JSONObject subject = new JSONObject(new JSONTokener(new FileInputStream(file)));
      try {
        schema.validate(subject);
      } catch (ValidationException e) {
        thrown = e;
      }
    } catch (JSONException e) {
      throw new RuntimeException("failed to parse subject json file", e);
    } catch (FileNotFoundException e) {
      throw new UncheckedIOException(e);
    }
    if (shouldBeValid && thrown != null) {
      Assert.fail("validation failed with: " + thrown);
    }
    if (!shouldBeValid && thrown == null) {
      Assert.fail("did not throw ValidationException for invalid subject");
    }
  }
}
