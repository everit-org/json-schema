package org.everit.json.schema;

import org.junit.jupiter.api.Test;

/**
 * These tests are actually compile-time validation that the superclass `Schema.Builder` returns the subclassed builder
 * so that additional method chaining is possible. Tests are executed via `ObjectSchema.Builder` for no specific reason.
 */
public class SchemaBuilderFluencyTest
{
    @Test
    public void title__when_called_from_subclass__returns_subclass_instance()
    {
        final ObjectSchema.Builder builder = ObjectSchema.builder().title(null);
    }

    @Test
    public void description__when_called_from_subclass__returns_subclass_instance()
    {
        final ObjectSchema.Builder builder = ObjectSchema.builder().description(null);
    }

    @Test
    public void id__when_called_from_subclass__returns_subclass_instance()
    {
        final ObjectSchema.Builder builder = ObjectSchema.builder().id(null);
    }

    @Test
    public void schemaLocation__when_called_from_subclass_with_String__returns_subclass_instance()
    {
        final ObjectSchema.Builder builder = ObjectSchema.builder().schemaLocation("https://example.com/schema");
    }

    @Test
    public void schemaLocation__when_called_from_subclass_with_SchemaLocation__returns_subclass_instance()
    {
        final ObjectSchema.Builder builder = ObjectSchema.builder().schemaLocation((SchemaLocation.empty()));
    }

    @Test
    public void defaultValue__when_called_from_subclass__returns_subclass_instance()
    {
        final ObjectSchema.Builder builder = ObjectSchema.builder().defaultValue(null);
    }

    @Test
    public void nullable__when_called_from_subclass__returns_subclass_instance()
    {
        final ObjectSchema.Builder builder = ObjectSchema.builder().nullable(null);
    }

    @Test
    public void readOnly__when_called_from_subclass__returns_subclass_instance()
    {
        final ObjectSchema.Builder builder = ObjectSchema.builder().readOnly(null);
    }

    @Test
    public void writeOnly__when_called_from_subclass__returns_subclass_instance()
    {
        final ObjectSchema.Builder builder = ObjectSchema.builder().writeOnly(null);
    }

    @Test
    public void unprocessedProperties__when_called_from_subclass__returns_subclass_instance()
    {
        final ObjectSchema.Builder builder = ObjectSchema.builder().unprocessedProperties(null);
    }
}
