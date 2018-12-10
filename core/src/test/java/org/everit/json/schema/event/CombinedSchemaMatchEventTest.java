package org.everit.json.schema.event;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class CombinedSchemaMatchEventTest {

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(CombinedSchemaMatchEvent.class)
                .withNonnullFields("subSchema", "schema", "instance")
                .withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

}
