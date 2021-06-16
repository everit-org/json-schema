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
            // intentionally not catching MalformedUriTemplateException, because in that case, if
            // com.damnhandy:handy-uri-templates is not on the classpath, a NoClassDefFoundError is thrown
            // from SpecificationVersion line 159, even if there are no uri-template schemas used. The reason is that (most
            // probably) during loading URITemplateFormatValidator, the exception handlers are verified as per section 4.10.1.6
            // of the Java Virtual Machine Specification (version 11). This means that all caught exceptions are checked if they
            // are subclasses of Throwable, so to do it, the JVM has to look into the MalformedUriTemplateException class file,
            // even if the class doesn't actually get loaded. So to work this around, here we first catch a RuntimeException and
            // then check with instanceof if it is a MalformedUriTemplateException, so that this way the
            // MalformedUriTemplateException does not appear in the exception table of the method.
            if (e instanceof MalformedUriTemplateException) {
                return Optional.of(format("[%s] is not a valid URI template", subject));
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override public String formatName() {
        return "uri-template";
    }
}
