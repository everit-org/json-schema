package org.everit.json.schema;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.everit.json.schema.regexp.Regexp;

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

    void visitReferenceSchema(ReferenceSchema referenceSchema) {
    }

    void visitObjectSchema(ObjectSchema objectSchema) {
        for (String requiredPropName : objectSchema.getRequiredProperties()) {
            visitRequiredPropertyName(requiredPropName);
        }
        visitPropertyNameSchema(objectSchema.getPropertyNameSchema());
        visitMinProperties(objectSchema.getMinProperties());
        visitMaxProperties(objectSchema.getMaxProperties());
        for (Map.Entry<String, Set<String>> entry : objectSchema.getPropertyDependencies().entrySet()) {
            visitPropertyDependencies(entry.getKey(), entry.getValue());
        }
        visitAdditionalProperties(objectSchema.permitsAdditionalProperties());
        visitSchemaOfAdditionalProperties(objectSchema.getSchemaOfAdditionalProperties());
        for (Map.Entry<Regexp, Schema> entry : objectSchema.getRegexpPatternProperties().entrySet()) {
            visitPatternPropertySchema(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Schema> schemaDep : objectSchema.getSchemaDependencies().entrySet()) {
            visitSchemaDependency(schemaDep.getKey(), schemaDep.getValue());
        }
        Map<String, Schema> propertySchemas = objectSchema.getPropertySchemas();
        if (propertySchemas != null) {
            for (Map.Entry<String, Schema> entry : propertySchemas.entrySet()) {
                visitPropertySchema(entry.getKey(), entry.getValue());
            }
        }
    }

    void visitPropertySchema(String properyName, Schema schema) {
    }

    void visitSchemaDependency(String propKey, Schema schema) {
    }

    void visitPatternPropertySchema(Regexp propertyNamePattern, Schema schema) {
    }

    void visitSchemaOfAdditionalProperties(Schema schemaOfAdditionalProperties) {
    }

    void visitAdditionalProperties(boolean additionalProperties) {
    }

    void visitPropertyDependencies(String ifPresent, Set<String> allMustBePresent) {
    }

    void visitMaxProperties(Integer maxProperties) {
    }

    void visitMinProperties(Integer minProperties) {
    }

    void visitPropertyNameSchema(Schema propertyNameSchema) {
    }

    void visitRequiredPropertyName(String requiredPropName) {
    }

    void visitStringSchema(StringSchema stringSchema) {
        visitMinLength(stringSchema.getMinLength());
        visitMaxLength(stringSchema.getMaxLength());
        visitPattern(stringSchema.getRegexpPattern());
        visitFormat(stringSchema.getFormatValidator());
    }

    void visitFormat(FormatValidator formatValidator) {
    }

    void visitPattern(Regexp pattern) {
    }

    void visitMaxLength(Integer maxLength) {
    }

    void visitMinLength(Integer minLength) {
    }

    void visitCombinedSchema(CombinedSchema combinedSchema) {
    }

    void visitConditionalSchema(ConditionalSchema conditionalSchema) {
        conditionalSchema.getIfSchema().ifPresent(this::visitIfSchema);
        conditionalSchema.getThenSchema().ifPresent(this::visitThenSchema);
        conditionalSchema.getElseSchema().ifPresent(this::visitElseSchema);
    }

    void visitIfSchema(Schema ifSchema) {
    }

    void visitThenSchema(Schema thenSchema) {
    }

    void visitElseSchema(Schema elseSchema) {
    }
}
