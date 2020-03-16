package org.everit.json.schema;

import static org.everit.json.schema.FormatValidator.NONE;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.everit.json.schema.internal.JSONPrinter;
import org.everit.json.schema.loader.SpecificationVersion;
import org.everit.json.schema.regexp.Regexp;
import org.json.JSONException;

class ToStringVisitor extends Visitor {

    private final JSONPrinter writer;

    private boolean jsonObjectIsOpenForCurrentSchemaInstance = false;

    private boolean skipNextObject = false;

    private SpecificationVersion deducedSpecVersion;

    ToStringVisitor(JSONPrinter writer) {
        this.writer = writer;
    }

    @Override void visitSchema(Schema schema) {
        if (schema == null) {
            return;
        }
        if (!jsonObjectIsOpenForCurrentSchemaInstance) {
            writer.object();
        }
        writer.ifPresent("title", schema.getTitle());
        writer.ifPresent("description", schema.getDescription());
        writer.ifPresent("nullable", schema.isNullable());
        writer.ifPresent("default", schema.getDefaultValue());
        writer.ifPresent("examples", schema.getExamples());
        writer.ifPresent("readOnly", schema.isReadOnly());
        writer.ifPresent("writeOnly", schema.isWriteOnly());
        super.visitSchema(schema);
        Object schemaKeywordValue = schema.getUnprocessedProperties().get("$schema");
        String idKeyword = deduceSpecVersion(schemaKeywordValue).idKeyword();
        writer.ifPresent(idKeyword, schema.getId());
        schema.getUnprocessedProperties().forEach((key, val) -> writer.key(key).value(val));
        schema.describePropertiesTo(writer);
        if (!jsonObjectIsOpenForCurrentSchemaInstance) {
            writer.endObject();
        }
    }

    private SpecificationVersion deduceSpecVersion(Object schemaKeywordValue) {
        if (deducedSpecVersion != null) {
            return deducedSpecVersion;
        }
        if (schemaKeywordValue instanceof String) {
            return deducedSpecVersion = SpecificationVersion.lookupByMetaSchemaUrl((String) schemaKeywordValue)
                    .orElse(SpecificationVersion.DRAFT_4);
        } else {
            return deducedSpecVersion = SpecificationVersion.DRAFT_4;
        }
    }

    private void printInJsonObject(Runnable task) {
        if (skipNextObject) {
            skipNextObject = false;
            jsonObjectIsOpenForCurrentSchemaInstance = true;
            task.run();
            jsonObjectIsOpenForCurrentSchemaInstance = false;
        } else {
            writer.object();
            jsonObjectIsOpenForCurrentSchemaInstance = true;
            task.run();
            writer.endObject();
            jsonObjectIsOpenForCurrentSchemaInstance = false;
        }
    }

    @Override
    void visitBooleanSchema(BooleanSchema schema) {
        printInJsonObject(() -> {
            super.visitBooleanSchema(schema);
            writer.key("type").value("boolean");
        });
    }

    @Override void visitArraySchema(ArraySchema schema) {
        printInJsonObject(() -> {
            if (schema.requiresArray()) {
                writer.key("type").value("array");
            }
            writer.ifTrue("uniqueItems", schema.needsUniqueItems())
                    .ifPresent("minItems", schema.getMinItems())
                    .ifPresent("maxItems", schema.getMaxItems())
                    .ifFalse("additionalItems", schema.permitsAdditionalItems());
            super.visitArraySchema(schema);
        });
    }

    @Override void visit(Schema schema) {
        boolean orig = jsonObjectIsOpenForCurrentSchemaInstance;
        jsonObjectIsOpenForCurrentSchemaInstance = false;
        super.visit(schema);
        jsonObjectIsOpenForCurrentSchemaInstance = orig;
    }

    @Override void visitAllItemSchema(Schema allItemSchema) {
        writer.key("items");
        visit(allItemSchema);
    }

    @Override void visitEmptySchema(EmptySchema emptySchema) {
        if (emptySchema instanceof TrueSchema) {
            writer.value(true);
        } else {
            printInJsonObject(() -> super.visitEmptySchema(emptySchema));
        }
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
        printInJsonObject(() -> super.visitConditionalSchema(conditionalSchema));
    }

    @Override void visitNotSchema(NotSchema notSchema) {
        printInJsonObject(() -> {
            visitSchema(notSchema);
            writer.key("not");
            notSchema.getMustNotMatch().accept(this);
        });
    }

    @Override void visitNumberSchema(NumberSchema schema) {
        printInJsonObject(() -> {
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
        });
    }

    @Override void visitConstSchema(ConstSchema constSchema) {
        printInJsonObject(() -> {
            writer.key("const");
            writer.value(constSchema.getPermittedValue());
            super.visitConstSchema(constSchema);
        });
    }

    @Override void visitObjectSchema(ObjectSchema schema) {
        printInJsonObject(() -> {
            if (schema.requiresObject()) {
                writer.key("type").value("object");
            }
            writer.ifPresent("minProperties", schema.getMinProperties());
            writer.ifPresent("maxProperties", schema.getMaxProperties());
            if (!schema.getPropertyDependencies().isEmpty()) {
                describePropertyDependencies(schema.getPropertyDependencies());
            }
            if (!schema.getSchemaDependencies().isEmpty()) {
                writer.key("dependencies");
                printSchemaMap(schema.getSchemaDependencies());
            }
            writer.ifFalse("additionalProperties", schema.permitsAdditionalProperties());
            super.visitObjectSchema(schema);
        });
    }

    @Override void visitRequiredProperties(List<String> requiredProperties) {
        if (!requiredProperties.isEmpty()) {
            writer.key("required").value(requiredProperties);
        }
    }

    @Override void visitSchemaOfAdditionalProperties(Schema schemaOfAdditionalProperties) {
        writer.key("additionalProperties");
        visit(schemaOfAdditionalProperties);
    }

    private void describePropertyDependencies(Map<String, Set<String>> propertyDependencies) {
        writer.key("dependencies");
        writer.object();
        propertyDependencies.forEach((key, value) -> {
            writer.key(key);
            writer.array();
            value.forEach(writer::value);
            writer.endArray();
        });
        writer.endObject();
    }

    @Override void visitPropertyNameSchema(Schema propertyNameSchema) {
        writer.key("propertyNames");
        visit(propertyNameSchema);
    }

    @Override void visitPropertySchemas(Map<String, Schema> propertySchemas) {
        if (!propertySchemas.isEmpty()) {
            writer.key("properties");
            printSchemaMap(propertySchemas);
        }
    }

    private void printSchemaMap(Map<?, Schema> schemas) {
        writer.object();
        schemas.forEach((key, value) -> {
            writer.key(key.toString());
            visit(value);
        });
        writer.endObject();
    }

    @Override void visitPatternProperties(Map<Regexp, Schema> patternProperties) {
        if (!patternProperties.isEmpty()) {
            writer.key("patternProperties");
            printSchemaMap(patternProperties);
        }
    }

    @Override void visitCombinedSchema(CombinedSchema combinedSchema) {
        printInJsonObject(() -> {
            super.visitCombinedSchema(combinedSchema);
            if (combinedSchema.isSynthetic()) {
                combinedSchema.getSubschemas().forEach(subschema -> {
                    this.skipNextObject = true;
                    super.visit(subschema);
                });
            } else {
                writer.key(combinedSchema.getCriterion().toString());
                writer.array();
                combinedSchema.getSubschemas().forEach(subschema -> subschema.accept(this));
                writer.endArray();
            }
        });

    }

    @Override void visitIfSchema(Schema ifSchema) {
        writer.key("if");
        visit(ifSchema);
    }

    @Override void visitThenSchema(Schema thenSchema) {
        writer.key("then");
        visit(thenSchema);
    }

    @Override void visitElseSchema(Schema elseSchema) {
        writer.key("else");
        visit(elseSchema);
    }

    @Override void visitFalseSchema(FalseSchema falseSchema) {
        writer.value(false);
    }

    @Override void visitNullSchema(NullSchema nullSchema) {
        printInJsonObject(() -> {
            writer.key("type");
            writer.value("null");
            super.visitNullSchema(nullSchema);
        });
    }

    @Override void visitStringSchema(StringSchema schema) {
        printInJsonObject(() -> {
            if (schema.requireString()) {
                writer.key("type").value("string");
            }
            writer.ifPresent("minLength", schema.getMinLength());
            writer.ifPresent("maxLength", schema.getMaxLength());
            writer.ifPresent("pattern", schema.getPattern());
            if (schema.getFormatValidator() != null && !NONE.equals(schema.getFormatValidator())) {
                writer.key("format").value(schema.getFormatValidator().formatName());
            }
            super.visitStringSchema(schema);
        });
    }

    @Override void visitEnumSchema(EnumSchema schema) {
        printInJsonObject(() -> {
            writer.key("enum");
            writer.array();
            schema.getPossibleValues().forEach(writer::value);
            writer.endArray();
            super.visitEnumSchema(schema);
        });
    }

    @Override void visitReferenceSchema(ReferenceSchema referenceSchema) {
        printInJsonObject(() -> {
            writer.key("$ref");
            writer.value(referenceSchema.getReferenceValue());
            super.visitReferenceSchema(referenceSchema);
        });
    }
}
