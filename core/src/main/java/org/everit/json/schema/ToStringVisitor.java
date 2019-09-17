package org.everit.json.schema;

import java.util.List;

import org.everit.json.schema.internal.JSONPrinter;
import org.everit.json.schema.loader.SpecificationVersion;
import org.json.JSONException;

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
        schema.getUnprocessedProperties().forEach((key, val) -> writer.key(key).value(val));
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
        writer.key("items");
        visit(allItemSchema);
    }

    @Override void visitEmptySchema(EmptySchema emptySchema) {
        writer.object();
        super.visitEmptySchema(emptySchema);
        writer.endObject();
    }

    @Override void visitItemSchemas(List<Schema> itemSchemas) {
        writer.key("items");
        writer.array();
        super.visitItemSchemas(itemSchemas);
        writer.endArray();
    }

    @Override void visitItemSchema(int index, Schema itemSchema) {
        visit(itemSchema);
    }

    @Override void visitSchemaOfAdditionalItems(Schema schemaOfAdditionalItems) {
        writer.key("additionalItems");
        visit(schemaOfAdditionalItems);
    }

    @Override void visitContainedItemSchema(Schema containedItemSchema) {
        writer.key("contains");
        visit(containedItemSchema);
    }

    @Override void visitConditionalSchema(ConditionalSchema conditionalSchema) {
        writer.object();
        super.visitConditionalSchema(conditionalSchema);
        writer.endObject();
    }

    @Override void visitNotSchema(NotSchema notSchema) {
        writer.object();
        writer.key("not");
        super.visitNotSchema(notSchema);
        writer.endObject();
    }

    @Override void visitNumberSchema(NumberSchema schema) {
        writer.object();
        if (schema.requiresInteger()) {
            writer.key("type").value("integer");
        } else if (schema.isRequiresNumber()) {
            writer.key("type").value("number");
        }
        writer.ifPresent("minimum", schema.getMinimum());
        writer.ifPresent("maximum", schema.getMaximum());
        writer.ifPresent("multipleOf", schema.getMultipleOf());
        writer.ifTrue("exclusiveMinimum", schema.isExclusiveMinimum());
        writer.ifTrue("exclusiveMaximum", schema.isExclusiveMaximum());
        try {
            writer.ifPresent("exclusiveMinimum", schema.getExclusiveMinimumLimit());
            writer.ifPresent("exclusiveMaximum", schema.getExclusiveMaximumLimit());
        } catch (JSONException e) {
            throw new IllegalStateException("overloaded use of exclusiveMinimum or exclusiveMaximum keyword");
        }
        super.visitNumberSchema(schema);
        writer.endObject();
    }

    @Override void visitConstSchema(ConstSchema constSchema) {
        writer.object();
        writer.key("const");
        writer.value(constSchema.getPermittedValue());
        super.visitConstSchema(constSchema);
        writer.endObject();
    }

    @Override void visitObjectSchema(ObjectSchema objectSchema) {
        writer.object();
        super.visitObjectSchema(objectSchema);
        writer.endObject();
    }

}
