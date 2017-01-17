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
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import org.junit.Assert;

import java.util.List;

public class TestSupport {

    public static class Failure {

        private Schema subject;

        private Schema expectedViolatedSchema;

        private String expectedPointer = "#";

        private String expectedKeyword;

        private Object input;

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

        public Failure expectedKeyword(final String keyword) {
            this.expectedKeyword = keyword;
            return this;
        }

        public String expectedKeyword() {
            return expectedKeyword;
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
    }

    public static Failure failureOf(final Schema subject) {
        return new Failure().subject(subject);
    }

    public static long countCauseByJsonPointer(final ValidationException root, final String pointer) {
        return FluentIterable.from(root.getCausingExceptions())
                .transform(new Function<ValidationException, String>() {
                    @Override
                    public String apply(ValidationException input) {
                        return input.getPointerToViolation();
                    }
                })
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String ptr) {
                        return ptr.equals(pointer);
                    }
                })
                .size();
    }

    public static long countMatchingMessage(final List<String> messages, final String expectedSubstring) {
        return FluentIterable.from(messages)
                .filter(Predicates.containsPattern(expectedSubstring))
                .size();
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
            Assert.assertEquals(failure.expectedPointer(), e.getPointerToViolation());
            if (failure.expectedKeyword() != null) {
                Assert.assertEquals(failure.expectedKeyword(), e.getKeyword());
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
                Assert.assertEquals(expectedPointer, e.getPointerToViolation());
            }
            throw e;
        }
    }

}
