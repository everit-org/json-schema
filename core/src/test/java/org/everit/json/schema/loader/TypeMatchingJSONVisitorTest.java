package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author erosb
 */
public class TypeMatchingJSONVisitorTest {

    private static final LoadingState emptyLs = JSONTraverserTest.emptyLs;

    @Rule
    public ExpectedException exc  = ExpectedException.none();

    @Test
    public void requireString() {
        exc.expect(SchemaException.class);
        JSONVisitor.requireString(new JSONTraverser(true, emptyLs));
    }
}
