package org.everit.json.schema;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class DynamicReferenceSchemaExample {

    public static void main(String[] args) {
        ObjectSchema.Builder rootSchema = ObjectSchema.builder();
        ReferenceSchema referenceSchema = ReferenceSchema.builder()
            .refValue("#/definitions/MySubschema")
            .build();
        rootSchema.addPropertySchema("myProperty", referenceSchema);
        
        Map<String, Object> unprocessed = new HashMap<>();
        JSONObject defs = new JSONObject();
        StringSchema referredSchema = StringSchema.builder()
            .minLength(2).maxLength(5)
            .build();
        referenceSchema.setReferredSchema(referredSchema);
        defs.put("MySubschema", new JSONObject(referredSchema.toString()));
        unprocessed.put("definitions", defs);
        
        
        rootSchema.unprocessedProperties(unprocessed);

        System.out.println(rootSchema.build().toString());
    }
    
}
