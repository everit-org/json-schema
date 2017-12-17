package org.everit.json.schema;

import java.util.List;

abstract class Visitor {

    void visitNumberSchema(NumberSchema numberSchema) {
        visitExclusiveMinimum(numberSchema.isExclusiveMinimum());
        visitMinimum(numberSchema.getMinimum());
        visitExclusiveMinimumLimit(numberSchema.getExclusiveMinimumLimit());
        visitExclusiveMaximum(numberSchema.isExclusiveMaximum());
        visitMaximum(numberSchema.getMaximum());
        visitExclusiveMaximumLimit(numberSchema.getExclusiveMaximumLimit());
        visitMultipleOf(numberSchema.getMultipleOf());
    }

    void visitMinimum(Number minimum) {
    }

    void visitExclusiveMinimum(boolean exclusiveMinimum) {
    }

    void visitExclusiveMinimumLimit(Number exclusiveMinimumLimit) {
    }

    void visitMaximum(Number maximum) {
    }

    void visitExclusiveMaximum(boolean exclusiveMaximum) {
    }

    void visitExclusiveMaximumLimit(Number exclusiveMaximumLimit) {
    }

    void visitMultipleOf(Number multipleOf) {
    }

    void visit(Schema schema) {
        schema.accept(this);
    }

    void visitArraySchema(ArraySchema arraySchema) {
        visitMinItems(arraySchema.getMinItems());
        visitMaxItems(arraySchema.getMaxItems());
        visitUniqueItems(arraySchema.needsUniqueItems());
        visitAllItemSchema(arraySchema.getAllItemSchema());
        visitAdditionalItems(arraySchema.permitsAdditionalItems());
        List<Schema> itemSchemas = arraySchema.getItemSchemas();
        if (itemSchemas != null) {
            for (int i = 0; i < itemSchemas.size(); ++i) {
                visitItemSchema(i, itemSchemas.get(i));
            }
        }
        visitSchemaOfAdditionalItems(arraySchema.getSchemaOfAdditionalItems());
        visitContainedItemSchema(arraySchema.getContainedItemSchema());
    }

    void visitMinItems(Integer minItems) {
    }

    void visitMaxItems(Integer maxItems) {
    }

    void visitUniqueItems(boolean uniqueItems) {
    }

    void visitAllItemSchema(Schema allItemSchema) {
    }

    void visitAdditionalItems(boolean additionalItems) {
    }

    void visitItemSchema(int index, Schema itemSchema) {
    }

    void visitSchemaOfAdditionalItems(Schema schemaOfAdditionalItems) {
    }

    void visitContainedItemSchema(Schema containedItemSchema) {
    }

    void visitBooleanSchema(BooleanSchema schema) {
    }

    void visitNullSchema(NullSchema nullSchema) {
    }

    void visitEmptySchema() {

    }

    void visitConstSchema(ConstSchema constSchema) {

    }

    void visitEnumSchema(EnumSchema enumSchema) {

    }

    void visitFalseSchema(FalseSchema falseSchema) {
    }

    void visitNotSchema(NotSchema notSchema) {
    }
}
