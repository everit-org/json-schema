package org.everit.json.schema;

import java.util.List;

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
        writer.key("type").value("boolean");
        writer.endObject();
    }

    @Override void visitArraySchema(ArraySchema schema) {
        writer.object();
        if (schema.requiresArray()) {
            writer.key("type").value("array");
        }
        writer.ifTrue("uniqueItems", schema.needsUniqueItems())
            .ifPresent("minItems", schema.getMinItems())
            .ifPresent("maxItems", schema.getMaxItems())
            .ifFalse("additionalItems", schema.permitsAdditionalItems());
        super.visitArraySchema(schema);
        writer.endObject();
    }

    @Override void visitAllItemSchema(Schema allItemSchema) {
        if (allItemSchema == null) {
            return;
        }
        writer.key("items");
        visit(allItemSchema);
    }

    @Override void visitItemSchemas(List<Schema> itemSchemas) {
        if (itemSchemas == null || itemSchemas.isEmpty()) {
            return;
        }
        writer.key("items");
        writer.array();
        super.visitItemSchemas(itemSchemas);
        writer.endArray();
    }

    @Override void visitItemSchema(int index, Schema itemSchema) {
        visit(itemSchema);
    }

    @Override void visitSchemaOfAdditionalItems(Schema schemaOfAdditionalItems) {
        if (schemaOfAdditionalItems == null) {
            return;
        }
        writer.key("additionalItems");
        visit(schemaOfAdditionalItems);
    }

    @Override void visitContainedItemSchema(Schema containedItemSchema) {
        if (containedItemSchema == null){
            return;
        }
        writer.key("contains");
        visit(containedItemSchema);
    }
}
