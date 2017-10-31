package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;
import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.FormatValidator.NONE;

/**
 * {@code String} schema validator.
 */
public class StringSchema extends Schema {

    /**
     * Builder class for {@link StringSchema}.
     */
    public static class Builder extends Schema.Builder<StringSchema> {

        private Integer minLength;

        private Integer maxLength;

        private String pattern;

        private boolean requiresString = true;

        private JsonObject notSchema;

        private FormatValidator formatValidator = NONE;

        @Override
        public StringSchema build() {
            return new StringSchema(this);
        }

        /**
         * Setter for the format validator. It should be used in conjunction with
         * {@link FormatValidator#forFormat(String)} if a {@code "format"} value is found in a schema
         * json.
         *
         * @param formatValidator the format validator
         * @return {@code this}
         */
        public Builder formatValidator(final FormatValidator formatValidator) {
            this.formatValidator = requireNonNull(formatValidator, "formatValidator cannot be null");
            return this;
        }

        public Builder maxLength(final Integer maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder minLength(final Integer minLength) {
            this.minLength = minLength;
            return this;
        }

        public Builder pattern(final String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder requiresString(final boolean requiresString) {
            this.requiresString = requiresString;
            return this;
        }

        public Builder notSchema(final JsonObject notSet) {
            this.notSchema = notSet;
            return this;
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    private final Integer minLength;

    private final Integer maxLength;

    private final Pattern pattern;

    private final boolean requiresString;

    private final FormatValidator formatValidator;

    private final JsonObject notSchema;

    public StringSchema() {
        this(builder());
    }

    /**
     * Constructor.
     *
     * @param builder the builder object containing validation criteria
     */
    public StringSchema(final Builder builder) {
        super(builder);
        this.minLength = builder.minLength;
        this.maxLength = builder.maxLength;
        this.requiresString = builder.requiresString;
        if (builder.pattern != null) {
            this.pattern = Pattern.compile(builder.pattern);
        } else {
            this.pattern = null;
        }
        if(builder.notSchema != null){
            this.notSchema = builder.notSchema;
        } else {
            this.notSchema = null;
        }

        this.formatValidator = builder.formatValidator;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public Pattern getPattern() {
        return pattern;
    }

    private List<ValidationException> testLength(final String subject) {
        int actualLength = subject.codePointCount(0, subject.length());
        List<ValidationException> rval = new ArrayList<>();
        if (minLength != null && actualLength < minLength.intValue()) {
            rval.add(failure("expected minLength: " + minLength + ", actual: "
                    + actualLength, "minLength"));
        }
        if (maxLength != null && actualLength > maxLength.intValue()) {
            rval.add(failure("expected maxLength: " + maxLength + ", actual: "
                    + actualLength, "maxLength"));
        }
        return rval;
    }

    private List<ValidationException> testPattern(final String subject) {
        if (pattern != null && !pattern.matcher(subject).find()) {
            String message = format("string [%s] does not match pattern %s",
                    subject, pattern.pattern());
            return Arrays.asList(failure(message, "pattern"));
        }
        return Collections.emptyList();
    }

    private List<ValidationException> testNotSchema(final String subject) {
        if (notSchema != null) {
            NotSchema.builder().mustNotMatch(SchemaLoader.load(new JSONObject(notSchema.toMap()))).build()
            .validate(subject);

        }
        return Collections.emptyList();
    }

    @Override
    public void validate(final Object subject) {
        if (!(subject instanceof String)) {
            if (requiresString) {
                throw failure(String.class, subject);
            }
        } else {
            String stringSubject = (String) subject;
            List<ValidationException> rval = new ArrayList<>();
            rval.addAll(testLength(stringSubject));
            rval.addAll(testPattern(stringSubject));
            rval.addAll(testNotSchema(stringSubject));
            formatValidator.validate(stringSubject)
                    .map(failure -> failure(failure, "format"))
                    .ifPresent(rval::add);
            ValidationException.throwFor(this, rval);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof StringSchema) {
            StringSchema that = (StringSchema) o;
            return that.canEqual(this) &&
                    requiresString == that.requiresString &&
                    Objects.equals(minLength, that.minLength) &&
                    Objects.equals(maxLength, that.maxLength) &&
                    Objects.equals(patternIfNotNull(pattern), patternIfNotNull(that.pattern)) &&
                    Objects.equals(formatValidator, that.formatValidator) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    private String patternIfNotNull(Pattern pattern) {
        if (pattern == null) {
            return null;
        } else {
            return pattern.pattern();
        }
    }

    public FormatValidator getFormatValidator() {
        return formatValidator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minLength, maxLength, pattern, requiresString, formatValidator);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof StringSchema;
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) {
        if (requiresString) {
            writer.key("type").value("string");
        }
        writer.ifPresent("minLength", minLength);
        writer.ifPresent("maxLength", maxLength);
        writer.ifPresent("pattern", pattern);
        if (formatValidator != null && !NONE.equals(formatValidator)) {
            writer.key("format").value(formatValidator.formatName());
        }
    }
}
