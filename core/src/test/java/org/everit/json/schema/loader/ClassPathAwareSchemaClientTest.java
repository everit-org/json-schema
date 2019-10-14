package org.everit.json.schema.loader;

import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.UncheckedIOException;

import org.everit.json.schema.ResourceLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class ClassPathAwareSchemaClientTest {

    private static final JSONObject EXPECTED = ResourceLoader.DEFAULT.readObj("constobject.json");

    private SchemaClient fallbackClient;

    @Before
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

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Test
    public void throwsErrorOnMissingClasspathResource() {
        exception.expect(UncheckedIOException.class);
        exception.expectMessage("Could not find");

        String url = "classpath:/bogus.json";
        ClassPathAwareSchemaClient subject = new ClassPathAwareSchemaClient(fallbackClient);
        subject.get(url);
    }

    @Test
    @Parameters({
            "classpath:/org/everit/jsonvalidator/constobject.json",
            "classpath://org/everit/jsonvalidator/constobject.json",
            "classpath:org/everit/jsonvalidator/constobject.json"
    })
    public void success(String url) {
        ClassPathAwareSchemaClient subject = new ClassPathAwareSchemaClient(fallbackClient);
        JSONObject actual = new JSONObject(new JSONTokener(subject.get(url)));
        assertThat(actual, sameJsonAs(EXPECTED));
    }

}
