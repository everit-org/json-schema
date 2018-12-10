package org.everit.json.schema.event;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class SchemaReferencedEventTest {

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(SchemaReferencedEvent.class)
                .withNonnullFields("referredSchema", "schema", "instance")
                .withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
