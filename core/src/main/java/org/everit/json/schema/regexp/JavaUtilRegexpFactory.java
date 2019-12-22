package org.everit.json.schema.regexp;

import java.util.Objects;
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

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof JavaUtilRegexp))
            return false;
        JavaUtilRegexp that = (JavaUtilRegexp) o;
        return Objects.equals(pattern.pattern(), that.pattern.pattern());
    }

    @Override public int hashCode() {
        return Objects.hash(pattern);
    }
}

public class JavaUtilRegexpFactory implements RegexpFactory {
    @Override public Regexp createHandler(String regexp) {
        return new JavaUtilRegexp(regexp);
    }
}
