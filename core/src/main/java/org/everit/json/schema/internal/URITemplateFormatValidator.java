package org.everit.json.schema.internal;

import static java.lang.String.format;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;

import com.damnhandy.uri.template.MalformedUriTemplateException;
import com.damnhandy.uri.template.UriTemplate;

public class URITemplateFormatValidator implements FormatValidator {

    @Override public Optional<String> validate(String subject) {
        try {
            UriTemplate.fromTemplate(subject);
            return Optional.empty();
        } catch (RuntimeException e) {
            //if (e.getClass().getCanonicalName().equals("com.damnhandy.uri.template.MalformedUriTemplateException"))
            if (e instanceof MalformedUriTemplateException) {
                return Optional.of(format("[%s] is not a valid URI template", subject));
            } else {
                throw e;
            }
        }
    }

    @Override public String formatName() {
        return "uri-template";
    }
}
