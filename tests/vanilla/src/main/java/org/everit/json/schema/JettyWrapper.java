package org.everit.json.schema;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author erosb
 */
class JettyWrapper {

    private Server server;

    private static File getDocumentRoot(String documentRootPath) throws URISyntaxException {
        try {
            return new File(JettyWrapper.class
                    .getResource(documentRootPath).toURI());
        } catch (IllegalArgumentException e) {
            return new File(JettyWrapper.class.getResource(documentRootPath).toExternalForm());
        }
    }

    JettyWrapper(String documentRootPath) throws Exception {
        this(getDocumentRoot(documentRootPath));
    }

    JettyWrapper(File documentRoot) {
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

    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
