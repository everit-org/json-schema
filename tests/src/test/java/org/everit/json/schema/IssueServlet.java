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

import com.google.common.base.Preconditions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class IssueServlet extends HttpServlet {
    private static final long serialVersionUID = -951266179406031349L;

    private final File documentRoot;

    public IssueServlet(final File documentRoot) {
        this.documentRoot = Preconditions.checkNotNull(documentRoot, "documentRoot cannot be null");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("GET " + req.getPathInfo());
        File content = fileByPath(req.getPathInfo());
        resp.setContentType("application/json");
        BufferedReader bis = new BufferedReader(new InputStreamReader(new FileInputStream(content)));
        try {
            String line;
            while ((line = bis.readLine()) != null) {
                resp.getWriter().write(line);
            }
        } finally {
            bis.close();
        }
    }

    private File fileByPath(final String pathInfo) {
        File rval = documentRoot;
        if (pathInfo != null && !pathInfo.equals("/") && !pathInfo.isEmpty()) {
            String[] segments = pathInfo.trim().split("/");
            for (final String fileName : segments) {
                if (fileName.isEmpty()) {
                    continue;
                }
                boolean found = false;
                for (File file : rval.listFiles()) {
                    if (file.getName().equals(fileName)) {
                        rval = file;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new RuntimeException("file [" + pathInfo + "] not found");
                }
            }
        }
        return rval;
    }

}
