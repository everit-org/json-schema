package org.everit.json.schema;

public interface Validator {

    void performValidation(Schema schema, Object input);

}


