package org.everit.json.schema;

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IssueServlet extends HttpServlet {
    private static final long serialVersionUID = -951266179406031349L;

    private final String documentRoot;

    private InputStream openStream(String pathInfo) {
        try {
            InputStream stream = getClass().getResourceAsStream(documentRoot + pathInfo);
            if (stream == null) {
                File file = new File(documentRoot + pathInfo);
                if (file.exists()) {
                    return new FileInputStream(file);
                }
            }
            return stream;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public IssueServlet(final String documentRoot) {
        this.documentRoot = requireNonNull(documentRoot, "documentRoot cannot be null");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("GET " + req.getPathInfo());
        try {
            resp.setContentType("application/json");
            try (
                    BufferedReader bis = new BufferedReader(
                            new InputStreamReader(openStream(req.getPathInfo())));) {
                String line;
                while ((line = bis.readLine()) != null) {
                    resp.getWriter().write(line);
                }
            }
        } catch (FileNotFoundException e) {
            resp.setStatus(404);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
