package org.everit.json.schema.event;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ConditionalSchemaMatchEventTest {

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(ConditionalSchemaMatchEvent.class)
                .withNonnullFields("keyword", "schema", "instance")
                .withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

}
