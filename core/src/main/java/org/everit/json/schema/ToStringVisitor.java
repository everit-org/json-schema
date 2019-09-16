package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;
import org.everit.json.schema.loader.SpecificationVersion;

class ToStringVisitor extends Visitor {

    private final JSONPrinter writer;

    ToStringVisitor(JSONPrinter writer) {
        this.writer = writer;
    }

    @Override void visitSchema(Schema schema) {
        if (schema == null) {
            return;
        }
        writer.ifPresent("title", schema.getTitle());
        writer.ifPresent("description", schema.getDescription());
        super.visitSchema(schema);
        Object schemaKeywordValue = schema.getUnprocessedProperties().get("$schema");
        String idKeyword;
        if (schemaKeywordValue instanceof String) {
            idKeyword = SpecificationVersion.lookupByMetaSchemaUrl((String) schemaKeywordValue)
                    .map(SpecificationVersion::idKeyword)
                    .orElse("id");
        } else {
            idKeyword = "id";
        }
        writer.ifPresent(idKeyword, schema.getId());
        schema.getUnprocessedProperties().forEach((key, val) -> {
            writer.key(key).value(val);
        });
        schema.describePropertiesTo(writer);
    }

    @Override
    void visitBooleanSchema(BooleanSchema schema) {
        writer.object();
        super.visitBooleanSchema(schema);
        writer.key("type");
        writer.value("boolean");
        writer.endObject();
    }

    @Override void visitArraySchema(ArraySchema arraySchema) {
        writer.object();
        super.visitArraySchema(arraySchema);
        writer.endObject();
    }

    @Override void visitAllItemSchema(Schema allItemSchema) {
        if (allItemSchema == null) {
            return;
        }
        writer.key("items");
        allItemSchema.accept(this);
    }
}
