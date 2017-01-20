/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Thrown by {@link Schema} subclasses on validation failure.
 */
public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 6192047123024651924L;

    private static int getViolationCount(final List<ValidationException> causes) {
        int causeCount = 0;
        for (ValidationException exception : causes) {
            causeCount += exception.getViolationCount();
        }
        return Math.max(1, causeCount);
    }

    private static List<String> getAllMessages(final List<ValidationException> causes) {
        List<String> messages = Lists.newArrayList();

        for (ValidationException cause : causes) {
            if (cause.getCausingExceptions().isEmpty()) {
                messages.add(cause.getMessage());
            } else {
                messages.addAll(getAllMessages(cause.getCausingExceptions()));
            }
        }

        return messages;
    }

    /**
     * Sort of static factory method. It is used by {@link ObjectSchema} and {@link ArraySchema} to
     * create {@code ValidationException}s, handling the case of multiple violations occuring during
     * validation.
     * <p>
     * <ul>
     * <li>If {@code failures} is empty, then it doesn't do anything</li>
     * <li>If {@code failures} contains 1 exception instance, then that will be thrown</li>
     * <li>Otherwise a new exception instance will be created, its {@link #getViolatedSchema()
     * violated schema} will be {@code rootFailingSchema}, and its {@link #getCausingExceptions()
     * causing exceptions} will be the {@code failures} list</li>
     * </ul>
     *
     * @param rootFailingSchema the schema which detected the {@code failures}
     * @param failures          list containing validation failures to be thrown by this method
     */
    public static void throwFor(final Schema rootFailingSchema,
            final List<ValidationException> failures) {
        int failureCount = failures.size();
        if (failureCount == 0) {
            return;
        } else if (failureCount == 1) {
            throw failures.get(0);
        } else {
            throw new ValidationException(rootFailingSchema, new ArrayList<>(failures));
        }
    }

    private final StringBuilder pointerToViolation;

    private final transient Schema violatedSchema;

    private final List<ValidationException> causingExceptions;

    private final String keyword;

    /**
     * Deprecated, use {@code ValidationException(Schema, Class<?>, Object)} instead.
     *
     * @param expectedType the expected type
     * @param actualValue  the violating value
     */
    @Deprecated
    public ValidationException(final Class<?> expectedType, final Object actualValue) {
        this(null, expectedType, actualValue);
    }

    /**
     * Constructor, creates an instance with {@code keyword="type"}.
     *
     * @param violatedSchema the schema instance which detected the schema violation
     * @param expectedType   the expected type
     * @param actualValue    the violating value
     */
    public ValidationException(final Schema violatedSchema, final Class<?> expectedType,
            final Object actualValue) {
        this(violatedSchema, expectedType, actualValue, "type");
    }

    /**
     * Constructor for type-mismatch failures. It is usually more convenient to use
     * {@link #ValidationException(Schema, Class, Object)} instead.
     *
     * @param violatedSchema the schema instance which detected the schema violation
     * @param expectedType   the expected type
     * @param actualValue    the violating value
     * @param keyword        the violating keyword
     */
    public ValidationException(final Schema violatedSchema, final Class<?> expectedType,
            final Object actualValue, final String keyword) {
        this(violatedSchema, new StringBuilder("#"),
                "expected type: " + expectedType.getSimpleName() + ", found: "
                        + (actualValue == null ? "null" : actualValue.getClass().getSimpleName()),
                Collections.<ValidationException>emptyList(), keyword);
    }

    private ValidationException(final Schema rootFailingSchema,
            final List<ValidationException> causingExceptions) {
        this(rootFailingSchema, new StringBuilder("#"),
                getViolationCount(causingExceptions) + " schema violations found",
                causingExceptions);
    }

    /**
     * Constructor.
     *
     * @param violatedSchema the schema instance which detected the schema violation
     * @param message        the readable exception message
     * @deprecated use one of the constructors which explicitly specify the violated keyword instead
     */
    @Deprecated
    public ValidationException(final Schema violatedSchema, final String message) {
        this(violatedSchema, new StringBuilder("#"), message, Collections.<ValidationException>emptyList());
    }

    /**
     * Constructor.
     *
     * @param violatedSchema the schama instance which detected the schema violation
     * @param message        the readable exception message
     * @param keyword        the violated keyword
     */
    public ValidationException(final Schema violatedSchema, final String message, final String keyword) {
        this(violatedSchema, new StringBuilder("#"), message, Collections.<ValidationException>emptyList(), keyword);
    }

    /***
     * Constructor.
     *
     * @param violatedSchema
     *          the schema instance which detected the schema violation
     * @param pointerToViolation
     *          a JSON pointer denoting the part of the document which violates the schema
     * @param message
     *          the readable exception message
     * @param causingExceptions
     *          a (possibly empty) list of validation failures. It is used if multiple schema
     *          violations are found by violatedSchema
     * @deprecated please explicitly specify the violated keyword using one of these constructors:
     *             <ul>
     *             <li>{@link #ValidationException(Schema, StringBuilder, String, List, String)}
     *             <li>{@link #ValidationException(Schema, String, String)}
     *             <li>{@link #ValidationException(Schema, Class, Object, String)}
     *             </ul>
     */
    @Deprecated
    ValidationException(final Schema violatedSchema, final StringBuilder pointerToViolation,
            final String message,
            final List<ValidationException> causingExceptions) {
        this(violatedSchema, pointerToViolation, message, causingExceptions, null);
    }

    /***
     * Constructor.
     *
     * @param violatedSchema
     *          the schema instance which detected the schema violation
     * @param pointerToViolation
     *          a JSON pointer denoting the part of the document which violates the schema
     * @param message
     *          the readable exception message
     * @param causingExceptions
     *          a (possibly empty) list of validation failures. It is used if multiple schema
     *          violations are found by violatedSchema
     * @param keyword
     *          the violated keyword
     */
    ValidationException(final Schema violatedSchema, final StringBuilder pointerToViolation, final String message,
            final List<ValidationException> causingExceptions, final String keyword) {
        super(message);
        this.violatedSchema = violatedSchema;
        this.pointerToViolation = pointerToViolation;
        this.causingExceptions = Collections.unmodifiableList(causingExceptions);
        this.keyword = keyword;
    }

    /**
     * Deprecated, use {@code ValidationException(Schema, String)} instead.
     *
     * @param message readable exception message
     */
    @Deprecated
    public ValidationException(final String message) {
        this((Schema) null, new StringBuilder("#"), message, Collections.<ValidationException>emptyList());
    }

    private ValidationException(final StringBuilder pointerToViolation,
            final Schema violatedSchema,
            final String message,
            final List<ValidationException> causingExceptions,
            final String keyword) {
        this(violatedSchema, pointerToViolation, message, causingExceptions, keyword);
    }

    /**
     * Constructor.
     *
     * @param violatedSchema    the schema instance which detected the schema violation
     * @param message           the readable exception message
     * @param causingExceptions a (possibly empty) list of validation failures. It is used if multiple schema
     *                          violations are found by violatedSchema
     * @deprecated use one of the constructors which explicitly specify the keyword instead
     */
    @Deprecated
    public ValidationException(final Schema violatedSchema, final String message,
            final List<ValidationException> causingExceptions) {
        this(violatedSchema, new StringBuilder("#"), message, causingExceptions);
    }

    private String escapeFragment(final String fragment) {
        return fragment.replace("~", "~0").replace("/", "~1");
    }

    public List<ValidationException> getCausingExceptions() {
        return causingExceptions;
    }

    /**
     * Returns all messages collected from all violations, including nested causing exceptions.
     *
     * @return all messages
     */
    public List<String> getAllMessages() {
        if (causingExceptions.isEmpty()) {
            return Collections.singletonList(getMessage());
        } else {
            return getAllMessages(causingExceptions);
        }
    }

    /**
     * Returns a programmer-readable error description prepended by {@link #getPointerToViolation()
     * the pointer to the violating fragment} of the JSON document.
     *
     * @return the error description
     */
    @Override
    public String getMessage() {
        return getPointerToViolation() + ": " + super.getMessage();
    }

    /**
     * Returns a programmer-readable error description. Unlike {@link #getMessage()} this doesn't
     * contain the JSON pointer denoting the violating document fragment.
     *
     * @return the error description
     */
    public String getErrorMessage() {
        return super.getMessage();
    }

    /**
     * A JSON pointer denoting the part of the document which violates the schema. It always points
     * from the root of the document to the violating data fragment, therefore it always starts with
     * <code>#</code>.
     *
     * @return the JSON pointer
     */
    public String getPointerToViolation() {
        if (pointerToViolation == null) {
            return null;
        }
        return pointerToViolation.toString();
    }

    public Schema getViolatedSchema() {
        return violatedSchema;
    }

    /**
     * Creates a new {@code ViolationException} instance based on this one, but with changed
     * {@link #getPointerToViolation() JSON pointer}.
     *
     * @param fragment the fragment of the JSON pointer to be prepended to existing pointers
     * @return the new instance
     */
    public ValidationException prepend(final String fragment) {
        return prepend(fragment, this.violatedSchema);
    }

    /**
     * Creates a new {@code ViolationException} instance based on this one, but with changed
     * {@link #getPointerToViolation() JSON pointer} and {link {@link #getViolatedSchema() violated
     * schema}.
     *
     * @param fragment       the fragment of the JSON pointer to be prepended to existing pointers
     * @param violatedSchema the violated schema, which may not be the same as {@link #getViolatedSchema()}
     * @return the new {@code ViolationException} instance
     */
    public ValidationException prepend(final String fragment, final Schema violatedSchema) {
        final String escapedFragment = escapeFragment(requireNonNull(fragment, "fragment cannot be null"));
        StringBuilder newPointer = this.pointerToViolation.insert(1, '/').insert(2, escapedFragment);
        List<ValidationException> prependedCausingExceptions = FluentIterable.from(causingExceptions)
                .transform(new Function<ValidationException, ValidationException>() {
                    @Override
                    public ValidationException apply(ValidationException exc) {
                        return exc.prepend(escapedFragment);
                    }
                })
                .toList();
        return new ValidationException(newPointer, violatedSchema, super.getMessage(),
                prependedCausingExceptions, this.keyword);
    }

    public int getViolationCount() {
        return getViolationCount(causingExceptions);
    }

    public String getKeyword() {
        return keyword;
    }

    /**
     * Creates a JSON representation of the failure.
     * <p>
     * The returned {@code JSONObject} contains the following keys:
     * <ul>
     * <li>{@code "message"}: a programmer-friendly exception message. This value is a non-nullable
     * string.</li>
     * <li>{@code "keyword"}: a JSON Schema keyword which was used in the schema and violated by the
     * input JSON. This value is a nullable string.</li>
     * <li>{@code "pointerToViolation"}: a JSON Pointer denoting the path from the root of the
     * document to the invalid fragment of it. This value is a non-nullable string. See
     * {@link #getPointerToViolation()}</li>
     * <li>{@code "causingExceptions"}: is a (possibly empty) array of violations which caused this
     * exceptions. See {@link #getCausingExceptions()}</li>
     * </ul>
     *
     * @return a JSON description of the validation error
     */
    public JSONObject toJSON() {
        JSONObject rval = new JSONObject();
        rval.put("keyword", keyword);
        if (pointerToViolation == null) {
            rval.put("pointerToViolation", JSONObject.NULL);
        } else {
            rval.put("pointerToViolation", getPointerToViolation());
        }
        rval.put("message", super.getMessage());
        List<JSONObject> causeJsons = FluentIterable.from(causingExceptions)
                .transform(new Function<ValidationException, JSONObject>() {
                    @Override
                    public JSONObject apply(ValidationException input) {
                        return input.toJSON();
                    }
                })
                .toList();
        rval.put("causingExceptions", new JSONArray(causeJsons));
        return rval;
    }
}
