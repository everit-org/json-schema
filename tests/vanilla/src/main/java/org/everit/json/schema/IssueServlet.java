package org.everit.json.schema;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public class IssueServlet extends HttpServlet {
    private static final long serialVersionUID = -951266179406031349L;

    private final File documentRoot;

    public IssueServlet(final File documentRoot) {
        this.documentRoot = requireNonNull(documentRoot, "documentRoot cannot be null");
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("GET " + req.getPathInfo());
        try {
            File content = fileByPath(req.getPathInfo());
            resp.setContentType("application/json");
            try (
                    BufferedReader bis = new BufferedReader(
                            new InputStreamReader(new FileInputStream(content)));) {
                String line;
                while ((line = bis.readLine()) != null) {
                    resp.getWriter().write(line);
                }
            }
        } catch (FileNotFoundException e) {
            resp.setStatus(404);
        }
    }

    private File fileByPath(final String pathInfo) throws FileNotFoundException {
        File rval = documentRoot;
        if (pathInfo != null && !pathInfo.equals("/") && !pathInfo.isEmpty()) {
            String[] segments = pathInfo.trim().split("/");
            for (String fileName : segments) {
                if (fileName.isEmpty()) {
                    continue;
                }
                rval = Arrays.stream(rval.listFiles())
                        .filter(file -> file.getName().equals(fileName))
                        .findFirst()
                        .orElseThrow(() -> new FileNotFoundException("file [" + pathInfo + "] not found"));
            }
        }
        return rval;
    }

}
