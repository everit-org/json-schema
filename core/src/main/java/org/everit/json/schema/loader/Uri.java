package org.everit.json.schema.loader;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;

class Uri {

    static Uri parse(String str) throws URISyntaxException {
        URI rawUri = new URI(str);
        int poundIdx = str.indexOf('#');
        String fragment;
        URI toBeQueried;
        if (poundIdx == -1) {
            toBeQueried = rawUri;
            fragment = "";
        } else {
            fragment = str.substring(poundIdx);
            toBeQueried = new URI(str.substring(0, poundIdx));
        }
        return new Uri(toBeQueried, fragment);
    }

    URI toBeQueried;

    String fragment;

    private Uri(URI toBeQueried, String fragment) {
        this.toBeQueried = requireNonNull(toBeQueried, "toBeQueried cannot be null");
        this.fragment = requireNonNull(fragment, "fragment cannot be null");
    }

    URI asJavaURI() {
        try {
            return new URI(toBeQueried + fragment);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
