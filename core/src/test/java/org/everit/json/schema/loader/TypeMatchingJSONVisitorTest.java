package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.json.JSONArray;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class TypeMatchingJSONVisitorTest {

    private static final LoadingState emptyLs = JSONTraverserTest.emptyLs;
    public static final JSONTraverser FLS = new JSONTraverser(false, emptyLs);
    public static final JSONTraverser TRU = new JSONTraverser(true, emptyLs);

    @Rule
    public ExpectedException exc = ExpectedException.none();

    @Test
    public void requireString() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: String, found: Boolean");
        JSONVisitor.requireString(TRU);
    }

    @Test
    public void requireStringSuccess() {
        assertEquals("string", JSONVisitor.requireString(new JSONTraverser("string", emptyLs)));
    }

    @Test
    public void requireStringWithMapper() {
        Integer actual = JSONVisitor.requireString(new JSONTraverser("42", emptyLs), (e, ls) -> Integer.valueOf(e));
        assertEquals(Integer.valueOf(42), actual);
    }

    @Test
    public void requireArray() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: List, found: Boolean");
        JSONVisitor.requireArray(TRU);
    }

    @Test
    public void requireArraySuccess() {
        assertEquals(asList(TRU, FLS), JSONVisitor.requireArray(
                new JSONTraverser(new JSONArray("[true,false]"), emptyLs)
        ));
    }

    @Test
    public void requireArrayWithMapper() {
        JSONTraverser input = new JSONTraverser(new JSONArray("[\"1\", \"2\"]"), emptyLs);
        assertEquals(asList(2, 3), JSONVisitor.requireArray(input,
                (arr, ls) -> arr.stream().map(tr -> JSONVisitor.requireString(tr))
                        .map(Integer::valueOf)
                        .map(i -> i.intValue() + 1)
                        .collect(toList())));
    }

    @Test
    public void requireArrayFailureInside() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#/1: expected type: String, found: Boolean");
        JSONTraverser input = new JSONTraverser(new JSONArray("[\"1\", true]"), emptyLs);
        JSONVisitor.requireArray(input, (arr, ls) -> arr.stream().map(tr -> JSONVisitor.requireString(tr))
                .map(Integer::valueOf)
                .map(i -> i.intValue() + 1)
                .collect(toList()));
    }

}
