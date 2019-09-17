package org.everit.json.schema;

import java.util.List;
import java.util.Map;

import org.everit.json.schema.internal.JSONPrinter;
import org.everit.json.schema.loader.SpecificationVersion;
import org.everit.json.schema.regexp.Regexp;
import org.json.JSONException;

class ToStringVisitor extends Visitor {

    private final JSONPrinter writer;

    private boolean jsonObjectIsOpenForCurrentSchemaInstance = false;

    private boolean skipNextObject = false;

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
        writer.ifPresent("readOnly", schema.isReadOnly());
        writer.ifPresent("writeOnly", schema.isWriteOnly());
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
        if (!jsonObjectIsOpenForCurrentSchemaInstance) {
            writer.endObject();
        }
    }

    void printInJsonObject(Runnable task) {
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
            writer.key("not");
            super.visitNotSchema(notSchema);
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

    @Override void visitObjectSchema(ObjectSchema objectSchema) {
        printInJsonObject(() -> super.visitObjectSchema(objectSchema));
    }

    @Override void visitPropertyNameSchema(Schema propertyNameSchema) {
        writer.key("propertyNames");
        printInJsonObject(() -> super.visitPropertyNameSchema(propertyNameSchema));
    }

    @Override void visitPropertySchemas(Map<String, Schema> propertySchemas) {
        if (!propertySchemas.isEmpty()) {
            writer.key("properties");
            writer.printSchemaMap(propertySchemas);
        }
    }

    @Override void visitPatternProperties(Map<Regexp, Schema> patternProperties) {
        if (!patternProperties.isEmpty()) {
            writer.key("patternProperties");
            writer.printSchemaMap(patternProperties);
        }
    }

    @Override void visitCombinedSchema(CombinedSchema combinedSchema) {
        printInJsonObject(() -> {
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

}
