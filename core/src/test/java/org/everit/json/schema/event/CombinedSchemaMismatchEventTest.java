package org.everit.json.schema.event;

import org.everit.json.schema.FalseSchema;
import org.everit.json.schema.TrueSchema;
import org.everit.json.schema.ValidationException;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class CombinedSchemaMismatchEventTest {

    @Test
    public void equalsVerifier() {
        ValidationException exc1 = new ValidationException(TrueSchema.INSTANCE, "message", "keyword", "#/location");
        ValidationException exc2 = new ValidationException(FalseSchema.INSTANCE, "message", "keyword", "#/loca/tion");
        EqualsVerifier.forClass(CombinedSchemaMismatchEvent.class)
                .withNonnullFields("subSchema", "schema", "instance", "failure")
                .withRedefinedSuperclass()
                .withPrefabValues(ValidationException.class, exc1, exc2)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

}
