package org.everit.json.schema;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.File;

/**
 * @author erosb
 */
class JettyWrapper {

    private Server server;

    JettyWrapper(String documentRootPath) throws Exception {
        server = new Server(1234);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(new ServletHolder(new IssueServlet(new File(ServletSupport.class
                .getResource(documentRootPath).toURI()))), "/*");
        server.start();
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
