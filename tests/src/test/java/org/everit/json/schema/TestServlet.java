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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet {
  private static final long serialVersionUID = -1900657382935266378L;

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    InputStream in = getClass().getResourceAsStream(
        "/org/everit/json/schema/draft4/remotes" + req.getPathInfo());
    if (in == null) {
      resp.setStatus(404);
      resp.getWriter().println("resource " + req.getPathInfo() + " not found");
      return;
    }
    resp.setContentType("application/json");
    int b;
    ServletOutputStream out = resp.getOutputStream();
    while ((b = in.read()) != -1) { // cannot believe i wrote this crap
      out.write(b);
    }
  }
}
