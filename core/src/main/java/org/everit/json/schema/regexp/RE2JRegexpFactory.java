package org.everit.json.schema.regexp;

import java.util.Objects;
import java.util.Optional;

import com.google.re2j.Pattern;

class RE2JRegexp extends AbstractRegexp {

    private final Pattern pattern;

    RE2JRegexp(String pattern) {
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
        if (!(o instanceof RE2JRegexp))
            return false;
        RE2JRegexp that = (RE2JRegexp) o;
        return Objects.equals(pattern, that.pattern);
    }

    @Override public int hashCode() {
        return Objects.hash(pattern);
    }
}

public class RE2JRegexpFactory implements RegexpFactory {
    @Override public Regexp createHandler(String input) {
        return new RE2JRegexp(input);
    }
}
