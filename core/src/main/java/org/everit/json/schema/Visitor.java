package org.everit.json.schema;

import java.util.*;
import java.util.List;
import org.everit.json.schema.regexp.Regexp;

abstract class Visitor {

    List<String> appendPath(List<String> path, int index) {
        return appendPath(path, String.format("[%d]", index));
    }

    List<String> appendPath(List<String> path, String field) {
        List<String> newList = new ArrayList<>(path);
        newList.add(field);
        return newList;
    }

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

    void visit(Schema schema, List<String> path) {
        schema.accept(this, path);
    }

    void visitArraySchema(ArraySchema arraySchema, List<String> path) {
        visitMinItems(arraySchema.getMinItems());
        visitMaxItems(arraySchema.getMaxItems());
        visitUniqueItems(arraySchema.needsUniqueItems());
        visitAllItemSchema(arraySchema.getAllItemSchema(), path);
        visitAdditionalItems(arraySchema.permitsAdditionalItems());
        List<Schema> itemSchemas = arraySchema.getItemSchemas();
        if (itemSchemas != null) {
            for (int i = 0; i < itemSchemas.size(); ++i) {
                visitItemSchema(i, itemSchemas.get(i), appendPath(path, i));
            }
        }
        visitSchemaOfAdditionalItems(arraySchema.getSchemaOfAdditionalItems(), path);
        visitContainedItemSchema(arraySchema.getContainedItemSchema(), path);
    }

    void visitMinItems(Integer minItems) {
    }

    void visitMaxItems(Integer maxItems) {
    }

    void visitUniqueItems(boolean uniqueItems) {
    }

    void visitAllItemSchema(Schema allItemSchema, List<String> path) {
    }

    void visitAdditionalItems(boolean additionalItems) {
    }

    void visitItemSchema(int index, Schema itemSchema, List<String> path) {
    }

    void visitSchemaOfAdditionalItems(Schema schemaOfAdditionalItems, List<String> path) {
    }

    void visitContainedItemSchema(Schema containedItemSchema, List<String> path) {
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

    void visitNotSchema(NotSchema notSchema, List<String> path) {
    }

    void visitReferenceSchema(ReferenceSchema referenceSchema, List<String> path) {
    }

    void visitObjectSchema(ObjectSchema objectSchema, List<String> path) {
        for (String requiredPropName : objectSchema.getRequiredProperties()) {
            visitRequiredPropertyName(requiredPropName);
        }
        visitPropertyNameSchema(objectSchema.getPropertyNameSchema(), path);
        visitMinProperties(objectSchema.getMinProperties());
        visitMaxProperties(objectSchema.getMaxProperties());
        for (Map.Entry<String, Set<String>> entry : objectSchema.getPropertyDependencies().entrySet()) {
            visitPropertyDependencies(entry.getKey(), entry.getValue(), appendPath(path, entry.getKey()));
        }
        visitAdditionalProperties(objectSchema.permitsAdditionalProperties());
        visitSchemaOfAdditionalProperties(objectSchema.getSchemaOfAdditionalProperties(), path);
        for (Map.Entry<Regexp, Schema> entry : objectSchema.getRegexpPatternProperties().entrySet()) {
            visitPatternPropertySchema(entry.getKey(), entry.getValue(), path);
        }
        for (Map.Entry<String, Schema> schemaDep : objectSchema.getSchemaDependencies().entrySet()) {
            visitSchemaDependency(schemaDep.getKey(), schemaDep.getValue(), appendPath(path, schemaDep.getKey()));
        }
        Map<String, Schema> propertySchemas = objectSchema.getPropertySchemas();
        if (propertySchemas != null) {
            for (Map.Entry<String, Schema> entry : propertySchemas.entrySet()) {
                visitPropertySchema(entry.getKey(), entry.getValue(), appendPath(path, entry.getKey()));
            }
        }
    }

    void visitPropertySchema(String properyName, Schema schema, List<String> path) {
    }

    void visitSchemaDependency(String propKey, Schema schema, List<String> path) {
    }

    void visitPatternPropertySchema(Regexp propertyNamePattern, Schema schema, List<String> path) {
    }

    void visitSchemaOfAdditionalProperties(Schema schemaOfAdditionalProperties, List<String> path) {
    }

    void visitAdditionalProperties(boolean additionalProperties) {
    }

    void visitPropertyDependencies(String ifPresent, Set<String> allMustBePresent, List<String> path) {
    }

    void visitMaxProperties(Integer maxProperties) {
    }

    void visitMinProperties(Integer minProperties) {
    }

    void visitPropertyNameSchema(Schema propertyNameSchema, List<String> path) {
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

    void visitCombinedSchema(CombinedSchema combinedSchema, List<String> path) {
    }

    void visitConditionalSchema(ConditionalSchema conditionalSchema, List<String> path) {
        conditionalSchema.getIfSchema().ifPresent(schema -> visitIfSchema(schema, path));
        conditionalSchema.getThenSchema().ifPresent(schema -> visitThenSchema(schema, path));
        conditionalSchema.getElseSchema().ifPresent(schema -> visitElseSchema(schema, path));
    }

    void visitIfSchema(Schema ifSchema, List<String> path) {
    }

    void visitThenSchema(Schema thenSchema, List<String> path) {
    }

    void visitElseSchema(Schema elseSchema, List<String> path) {
    }
}
