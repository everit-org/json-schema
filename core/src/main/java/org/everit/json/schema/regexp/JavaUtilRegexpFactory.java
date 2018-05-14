package org.everit.json.schema.regexp;

import java.util.Optional;
import java.util.regex.Pattern;

class JavaUtilRegexp extends AbstractRegexp {

    private final Pattern pattern;

    JavaUtilRegexp(String pattern) {
        super(pattern);
        this.pattern = Pattern.compile(pattern);
    }

    @Override public Optional<RegexpMatchingFailure> patternMatchingFailure(String input) {
        if (pattern.matcher(input).find()) {
            return Optional.empty();
        } else {
            return Optional.of(new RegexpMatchingFailure());
        }
    }

}

public class JavaUtilRegexpFactory implements RegexpFactory {
    @Override public Regexp createHandler(String regexp) {
        return new JavaUtilRegexp(regexp);
    }
}
