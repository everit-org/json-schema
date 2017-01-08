package org.everit.json.schema.loader;

import org.json.JSONObject;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author erosb
 */
public class JsonArrayTest {

    private static final LoadingState emptyLs = JsonValueTest.emptyLs;

    @Test
    public void testForEach() {
        JSONObject rawObj = new JSONObject();
        JsonArray subject = new JsonArray(asList(true, rawObj), emptyLs);
        JsonArrayIterator iterator = mock(JsonArrayIterator.class);
        subject.forEach(iterator);
        LoadingState childState0 = emptyLs.childFor(0), childState1 = emptyLs.childFor(1);
        verify(iterator).apply(0, JsonValue.of(true, childState0));
        verify(iterator).apply(1, JsonValue.of(rawObj, childState1));
    }
}
