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
package org.everit.json.schema.loader.internal;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ReferenceResolverTest {

    public static List<Arguments> params() {
        return asList(
                Arguments.of("fragment id", "http://x.y.z/root.json#foo", "http://x.y.z/root.json", "#foo"),
                Arguments.of("rel path", "http://example.org/foo", "http://example.org/bar", "foo"),
                Arguments.of("file name change", "http://x.y.z/schema/child.json",
                        "http://x.y.z/schema/parent.json",
                        "child.json"),
                Arguments.of("file name after folder path", "http://x.y.z/schema/child.json",
                        "http://x.y.z/schema/", "child.json"),
                Arguments.of("new root", "http://bserver.com", "http://aserver.com/",
                        "http://bserver.com"),
                Arguments.of("null parent", "http://a.b.c", null, "http://a.b.c"),
                Arguments.of("classpath single-slash",
                        "classpath:/hello/world.json/definitions/A",
                        "classpath:/hello/world.json/", "definitions/A"
                ),
                Arguments.of("classpath double-slash",
                        "classpath://hello/world.json#/definitions/A",
                        "classpath://hello/world.json", "#/definitions/A"
                ));
    }

    @ParameterizedTest
    @MethodSource("params")
    public void test(String testcaseName, String expectedOutput, String parentScope, String encounteredSegment) {
        String actual = ReferenceResolver.resolve(parentScope, encounteredSegment);
        assertEquals(expectedOutput, actual);
    }

    @ParameterizedTest
    @MethodSource("params")
    public void testURI(String testcaseName, String expectedOutput, String parentScope, String encounteredSegment) {
        URI parentScopeURI;
        try {
            parentScopeURI = new URI(parentScope);
        } catch (URISyntaxException | NullPointerException e) {
            parentScopeURI = null;
        }
        URI actual = ReferenceResolver.resolve(parentScopeURI, encounteredSegment);
    }

    @Test
    public void resolveWrapsURISyntaxException() {
        try {
            ReferenceResolver.resolve("\\\\somethin\010g invalid///", "segment");
            fail("did not throw exception for invalid URI");
        } catch (RuntimeException e) {
            assertEquals(URISyntaxException.class, e.getCause().getClass());
        }
    }

    @Test public void resolveURIWrapsURISyntaxException() throws Exception {
        try {
            ReferenceResolver.resolve(new URI("http://example.com"), "\\\\somethin\010g invalid///");
            fail("did not throw exception for invalid URI");
        } catch (RuntimeException e) {
            assertEquals(URISyntaxException.class, e.getCause().getClass());
        }
    }

}
