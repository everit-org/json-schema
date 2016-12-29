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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.File;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

public class ServletSupport {

    public static ServletSupport withDocumentRoot(final String path) {
        try {
            return withDocumentRoot(new File(ServletSupport.class.getResource(path).toURI()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static ServletSupport withDocumentRoot(final File documentRoot) {
        return new ServletSupport(documentRoot);
    }

    private final File documentRoot;

    public ServletSupport(final File documentRoot) {
        this.documentRoot = requireNonNull(documentRoot, "documentRoot cannot be null");
    }

    public void run(final Runnable runnable) {
        initJetty();
        runnable.run();
        stopJetty();
    }

    private Server server;

    public void initJetty() {
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

    public void stopJetty() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        server = null;
    }

}
