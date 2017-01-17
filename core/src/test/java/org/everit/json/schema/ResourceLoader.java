package org.everit.json.schema;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class ResourceLoader {

    public static final ResourceLoader DEFAULT = new ResourceLoader("/org/everit/jsonvalidator/");

    private final String rootPath;

    public ResourceLoader(String rootPath) {
        this.rootPath = requireNonNull(rootPath, "rootPath cannot be null");
    }

    public JSONObject readObj(String relPath) {
        InputStream stream = getStream(relPath);
        return new JSONObject(new JSONTokener(stream));
    }

    public InputStream getStream(String relPath) {
        String absPath = rootPath + relPath;
        InputStream rval = getClass().getResourceAsStream(absPath);
        if (rval == null) {
            throw new IllegalArgumentException(
                    format("failed to load resource by relPath [%s].\n"
                            + "InputStream by path [%s] is null", relPath, absPath));
        }
        return rval;
    }

}
