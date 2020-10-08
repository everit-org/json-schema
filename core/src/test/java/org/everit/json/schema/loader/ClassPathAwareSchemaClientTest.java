package org.everit.json.schema.loader;

import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.UncheckedIOException;

import org.everit.json.schema.ResourceLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ClassPathAwareSchemaClientTest {

    private static final JSONObject EXPECTED = ResourceLoader.DEFAULT.readObj("constobject.json");

    private SchemaClient fallbackClient;

    @BeforeEach
    public void before() {
        fallbackClient = mock(SchemaClient.class);
    }

    @Test
    public void delegatesUnhandledProtocolsToFallback() {
        InputStream expected = ResourceLoader.DEFAULT.getStream("arraytestcases.json");
        when(fallbackClient.get("http://example.org")).thenReturn(expected);
        ClassPathAwareSchemaClient subject = new ClassPathAwareSchemaClient(fallbackClient);
        InputStream actual = subject.get("http://example.org");
        assertSame(expected, actual);
    }

    @Test
    public void throwsErrorOnMissingClasspathResource() {
        UncheckedIOException thrown = assertThrows(UncheckedIOException.class, () -> {
            String url = "classpath:/bogus.json";
            ClassPathAwareSchemaClient subject = new ClassPathAwareSchemaClient(fallbackClient);
            subject.get(url);
        });
        assertEquals("java.io.IOException: Could not find classpath:/bogus.json", thrown.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"classpath:/org/everit/jsonvalidator/constobject.json",
            "classpath://org/everit/jsonvalidator/constobject.json",
            "classpath:org/everit/jsonvalidator/constobject.json"})
    public void success(String url) {
        ClassPathAwareSchemaClient subject = new ClassPathAwareSchemaClient(fallbackClient);
        JSONObject actual = new JSONObject(new JSONTokener(subject.get(url)));
        assertThat(actual, sameJsonAs(EXPECTED));
    }

}
