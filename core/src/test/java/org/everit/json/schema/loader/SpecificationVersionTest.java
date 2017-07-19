package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_6;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class SpecificationVersionTest {

    @Test
    public void v6ContainsAdditionalFormats() {
        Set<String> actual = DRAFT_6.defaultFormatValidators().keySet();
        assertTrue(actual.containsAll(asList("json-pointer", "uri-reference", "uri-template")));
    }
}
