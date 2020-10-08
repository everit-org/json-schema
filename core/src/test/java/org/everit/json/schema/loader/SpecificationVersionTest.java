package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_6;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_7;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.everit.json.schema.FormatValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SpecificationVersionTest {

    @Test
    public void v6ContainsAdditionalFormats() {
        Set<String> actual = DRAFT_6.defaultFormatValidators().keySet();
        assertTrue(actual.containsAll(asList("json-pointer", "uri-reference", "uri-template")));
    }

    @Test
    public void v4MapMatchesFormatNames() {
        for (Map.Entry<String, FormatValidator> entry : DRAFT_4.defaultFormatValidators().entrySet()) {
            assertEquals(entry.getKey(), entry.getValue().formatName());
        }
    }

    @Test
    public void v6MapMatchesFormatNames() {
        for (Map.Entry<String, FormatValidator> entry : DRAFT_6.defaultFormatValidators().entrySet()) {
            assertEquals(entry.getKey(), entry.getValue().formatName());
        }
    }

    @Test
    public void isAtLeastTrue() {
        Assertions.assertTrue(DRAFT_7.isAtLeast(DRAFT_6));
    }

    @Test
    public void isAtLeast_False() {
        Assertions.assertFalse(DRAFT_6.isAtLeast(DRAFT_7));
    }

    @Test
    public void isAtLeast_equal() {
        Assertions.assertTrue(DRAFT_6.isAtLeast(DRAFT_6));
    }

}
