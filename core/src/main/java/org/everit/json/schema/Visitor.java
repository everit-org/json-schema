package org.everit.json.schema;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.everit.json.schema.regexp.Regexp;

abstract class Visitor {

    void visitSchema(Schema schema) {

    }

    void visitNumberSchema(NumberSchema numberSchema) {
        visitSchema(numberSchema);
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
        visitSchema(arraySchema);
        visitMinItems(arraySchema.getMinItems());
        visitMaxItems(arraySchema.getMaxItems());
        visitUniqueItems(arraySchema.needsUniqueItems());
        if (arraySchema.getAllItemSchema() != null) {
            visitAllItemSchema(arraySchema.getAllItemSchema());
        }
        visitAdditionalItems(arraySchema.permitsAdditionalItems());
        if (arraySchema.getItemSchemas() != null) {
            visitItemSchemas(arraySchema.getItemSchemas());
        }
        if (arraySchema.getSchemaOfAdditionalItems() != null) {
            visitSchemaOfAdditionalItems(arraySchema.getSchemaOfAdditionalItems());
        }
        if (arraySchema.getContainedItemSchema() != null) {
            visitContainedItemSchema(arraySchema.getContainedItemSchema());
        }
    }

    void visitItemSchemas(List<Schema> itemSchemas) {
        if (itemSchemas != null) {
            for (int i = 0; i < itemSchemas.size(); ++i) {
                visitItemSchema(i, itemSchemas.get(i));
            }
        }
    }

    void visitMinItems(Integer minItems) {
    }

    void visitMaxItems(Integer maxItems) {
    }

    void visitUniqueItems(boolean uniqueItems) {
    }

    void visitAllItemSchema(Schema allItemSchema) {
        visitSchema(allItemSchema);
    }

    void visitAdditionalItems(boolean additionalItems) {
    }

    void visitItemSchema(int index, Schema itemSchema) {
        visitSchema(itemSchema);
    }

    void visitSchemaOfAdditionalItems(Schema schemaOfAdditionalItems) {
        visitSchema(schemaOfAdditionalItems);
    }

    void visitContainedItemSchema(Schema containedItemSchema) {
        visitSchema(containedItemSchema);
    }

    void visitBooleanSchema(BooleanSchema schema) {
        visitSchema(schema);
    }

    void visitNullSchema(NullSchema nullSchema) {
        visitSchema(nullSchema);
    }

    void visitEmptySchema(EmptySchema emptySchema) {
        visitSchema(emptySchema);
    }

    void visitConstSchema(ConstSchema constSchema) {
        visitSchema(constSchema);
    }

    void visitEnumSchema(EnumSchema enumSchema) {
        visitSchema(enumSchema);
    }

    void visitFalseSchema(FalseSchema falseSchema) {
        visitSchema(falseSchema);
    }

    void visitNotSchema(NotSchema notSchema) {
        visitSchema(notSchema);
        notSchema.getMustNotMatch().accept(this);
    }

    void visitReferenceSchema(ReferenceSchema referenceSchema) {
        visitSchema(referenceSchema);
    }

    void visitObjectSchema(ObjectSchema objectSchema) {
        visitSchema(objectSchema);
        visitRequiredProperties(objectSchema.getRequiredProperties());
        if (objectSchema.getPropertyNameSchema() != null) {
            visitPropertyNameSchema(objectSchema.getPropertyNameSchema());
        }
        visitMinProperties(objectSchema.getMinProperties());
        visitMaxProperties(objectSchema.getMaxProperties());
        for (Map.Entry<String, Set<String>> entry : objectSchema.getPropertyDependencies().entrySet()) {
            visitPropertyDependencies(entry.getKey(), entry.getValue());
        }
        visitAdditionalProperties(objectSchema.permitsAdditionalProperties());
        if (objectSchema.getSchemaOfAdditionalProperties() != null) {
            visitSchemaOfAdditionalProperties(objectSchema.getSchemaOfAdditionalProperties());
        }
        Map<Regexp, Schema> patternProperties = objectSchema.getRegexpPatternProperties();
        if (patternProperties != null) {
            visitPatternProperties(patternProperties);
        }
        for (Map.Entry<String, Schema> schemaDep : objectSchema.getSchemaDependencies().entrySet()) {
            visitSchemaDependency(schemaDep.getKey(), schemaDep.getValue());
        }
        Map<String, Schema> definitionSchemas = objectSchema.getDefinitionSchemas();
        if (definitionSchemas != null) {
            visitDefinitionSchemas(definitionSchemas);
        }
        Map<String, Schema> propertySchemas = objectSchema.getPropertySchemas();
        if (propertySchemas != null) {
            visitPropertySchemas(propertySchemas);
        }
    }

    void visitRequiredProperties(List<String> requiredProperties) {
        for (String requiredPropName : requiredProperties) {
            visitRequiredPropertyName(requiredPropName);
        }
    }

    void visitPatternProperties(Map<Regexp, Schema> patternProperties) {
        for (Map.Entry<Regexp, Schema> entry : patternProperties.entrySet()) {
            visitPatternPropertySchema(entry.getKey(), entry.getValue());
        }
    }

    void visitDefinitionSchemas(Map<String, Schema> definitionSchemas) {
        for (Map.Entry<String, Schema> entry : definitionSchemas.entrySet()) {
            visitPropertySchema(entry.getKey(), entry.getValue());
        }
    }

    void visitPropertySchemas(Map<String, Schema> propertySchemas) {
        for (Map.Entry<String, Schema> entry : propertySchemas.entrySet()) {
            visitPropertySchema(entry.getKey(), entry.getValue());
        }
    }

    void visitPropertySchema(String properyName, Schema schema) {
        visitSchema(schema);
    }

    void visitSchemaDependency(String propKey, Schema schema) {
        visitSchema(schema);
    }

    void visitPatternPropertySchema(Regexp propertyNamePattern, Schema schema) {
        visitSchema(schema);
    }

    void visitSchemaOfAdditionalProperties(Schema schemaOfAdditionalProperties) {
        visitSchema(schemaOfAdditionalProperties);
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
        visitSchema(propertyNameSchema);
    }

    void visitRequiredPropertyName(String requiredPropName) {
    }

    void visitStringSchema(StringSchema stringSchema) {
        visitSchema(stringSchema);
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
        visitSchema(combinedSchema);
    }

    void visitConditionalSchema(ConditionalSchema conditionalSchema) {
        visitSchema(conditionalSchema);
        conditionalSchema.getIfSchema().ifPresent(this::visitIfSchema);
        conditionalSchema.getThenSchema().ifPresent(this::visitThenSchema);
        conditionalSchema.getElseSchema().ifPresent(this::visitElseSchema);
    }

    void visitIfSchema(Schema ifSchema) {
        visitSchema(ifSchema);
    }

    void visitThenSchema(Schema thenSchema) {
        visitSchema(thenSchema);
    }

    void visitElseSchema(Schema elseSchema) {
        visitSchema(elseSchema);
    }
}
