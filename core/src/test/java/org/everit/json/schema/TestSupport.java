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

import org.everit.json.schema.loader.JsonValueTest;
import org.everit.json.schema.loader.SchemaLoader;
import org.junit.Assert;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestSupport {

    public static class Failure {

        private Schema subject;

        private Schema expectedViolatedSchema;

        private String expectedPointer = "#";

        private String expectedSchemaLocation = "#";

        private String expectedKeyword;

        private Object input;

        private String expectedMessageFragment;

        public Failure subject(final Schema subject) {
            this.subject = subject;
            return this;
        }

        public Schema subject() {
            return subject;
        }

        public Failure expectedViolatedSchema(final Schema expectedViolatedSchema) {
            this.expectedViolatedSchema = expectedViolatedSchema;
            return this;
        }

        public Schema expectedViolatedSchema() {
            if (expectedViolatedSchema != null) {
                return expectedViolatedSchema;
            }
            return subject;
        }

        public Failure expectedPointer(final String expectedPointer) {
            this.expectedPointer = expectedPointer;
            return this;
        }

        public String expectedPointer() {
            return expectedPointer;
        }

        public Failure expectedSchemaLocation(String expectedSchemaLocation) {
            this.expectedSchemaLocation = expectedSchemaLocation;
            return this;
        }
        public String expectedSchemaLocation() {
            return expectedSchemaLocation;
        }

        public Failure expectedKeyword(final String keyword) {
            this.expectedKeyword = keyword;
            return this;
        }

        public String expectedKeyword() {
            return expectedKeyword;
        }

        public String expectedMessageFragment() {
            return expectedMessageFragment;
        }

        public Failure input(final Object input) {
            this.input = input;
            return this;
        }

        public Object input() {
            return input;
        }

        public void expect() {
            expectFailure(this);
        }

        public Failure expectedMessageFragment(String expectedFragment) {
            this.expectedMessageFragment = expectedFragment;
            return this;
        }
    }

    public static Failure failureOf(Schema subject) {
        return new Failure().subject(subject);
    }

    public static Failure failureOf(Schema.Builder<?> subjectBuilder) {
        return failureOf(buildWithLocation(subjectBuilder));
    }

    public static <S extends Schema> S buildWithLocation(Schema.Builder<S> builder) {
        return builder.schemaLocation("#").build();
    }

    public static SchemaLoader.SchemaLoaderBuilder v6Loader() {
        return SchemaLoader.builder().draftV6Support();
    }

    public static Schema loadAsV6(Object schema) {
        SchemaLoader loader = v6Loader().schemaJson(schema).draftV6Support().build();
        return loader.load().build();
    }

    public static long countCauseByJsonPointer(final ValidationException root, final String pointer) {
        return root.getCausingExceptions().stream()
                .map(ValidationException::getPointerToViolation)
                .filter(ptr -> ptr.equals(pointer))
                .count();
    }

    public static long countMatchingMessage(final List<String> messages, final String expectedSubstring) {
        return messages.stream()
                .filter(message -> message.contains(expectedSubstring))
                .count();
    }

    public static void expectFailure(final Schema failingSchema,
            final Class<? extends Schema> expectedViolatedSchemaClass,
            final String expectedPointer, final Object input) {
        try {
            test(failingSchema, expectedPointer, input);
        } catch (ValidationException e) {
            Assert.assertSame(expectedViolatedSchemaClass, e.getViolatedSchema().getClass());
        }
    }

    public static void expectFailure(final Schema failingSchema, final Object input) {
        expectFailure(failingSchema, null, input);
    }

    public static void expectFailure(final Schema failingSchema,
            final Schema expectedViolatedSchema,
            final String expectedPointer, final Object input) {
        try {
            test(failingSchema, expectedPointer, input);
        } catch (ValidationException e) {
            Assert.assertSame(expectedViolatedSchema, e.getViolatedSchema());
        }
    }

    public static void expectFailure(final Schema failingSchema, final String expectedPointer,
            final Object input) {
        expectFailure(failingSchema, failingSchema, expectedPointer, input);
    }

    public static void expectFailure(final Failure failure) {
        try {
            failure.subject().validate(failure.input());
            Assert.fail(failure.subject() + " did not fail for " + failure.input());
        } catch (ValidationException e) {
            Assert.assertSame(failure.expectedViolatedSchema(), e.getViolatedSchema());
            assertEquals(failure.expectedPointer(), e.getPointerToViolation());
            assertEquals(failure.expectedSchemaLocation(), e.getSchemaLocation());
            if (failure.expectedKeyword() != null) {
                assertEquals(failure.expectedKeyword(), e.getKeyword());
            }
            if (failure.expectedMessageFragment() != null) {
                assertThat(e.getMessage(), containsString(failure.expectedMessageFragment()));
            }
        }
    }

    private static void test(final Schema failingSchema, final String expectedPointer,
            final Object input) {
        try {
            failingSchema.validate(input);
            Assert.fail(failingSchema + " did not fail for " + input);
        } catch (ValidationException e) {
            if (expectedPointer != null) {
                assertEquals(expectedPointer, e.getPointerToViolation());
            }
            throw e;
        }
    }

}
