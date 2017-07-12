package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static org.everit.json.schema.loader.JsonValueTest.withLs;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.json.JSONObject;
import org.junit.Test;

/**
 * @author erosb
 */
public class JsonArrayTest {

    @Test
    public void testForEach() {
        JSONObject rawObj = new JSONObject();
        JsonArray subject = withLs(new JsonArray(asList(true, rawObj))).requireArray();
        JsonArrayIterator iterator = mock(JsonArrayIterator.class);
        subject.forEach(iterator);
        verify(iterator).apply(0, JsonValue.of(true));
        verify(iterator).apply(1, JsonValue.of(rawObj));
    }
}
