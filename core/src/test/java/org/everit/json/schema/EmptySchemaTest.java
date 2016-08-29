package org.everit.json.schema;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class EmptySchemaTest {

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(EmptySchema.class)
                .withRedefinedSuperclass()
                .verify();
    }
}
