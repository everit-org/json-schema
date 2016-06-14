/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import java.io.StringWriter;

import javax.annotation.Generated;

import org.json.JSONWriter;

/**
 * Superclass of all other schema validator classes of this package.
 */
public abstract class Schema {

  /**
   * Abstract builder class for the builder classes of {@code Schema} subclasses. This builder is
   * used to load the generic properties of all types of schemas like {@code title} or
   * {@code description}.
   *
   * @param <S>
   *          the type of the schema being built by the builder subclass.
   */
  public abstract static class Builder<S extends Schema> {

    private String title;

    private String description;

    private String id;

    public Builder<S> title(final String title) {
      this.title = title;
      return this;
    }

    public Builder<S> description(final String description) {
      this.description = description;
      return this;
    }

    public Builder<S> id(final String id) {
      this.id = id;
      return this;
    }

    public abstract S build();

  }

  private final String title;

  private final String description;

  private final String id;

  /**
   * Constructor.
   *
   * @param builder
   *          the builder containing the optional title, description and id attributes of the schema
   */
  protected Schema(final Builder<?> builder) {
    this.title = builder.title;
    this.description = builder.description;
    this.id = builder.id;
  }

  /**
   * Performs the schema validation.
   *
   * @param subject
   *          the object to be validated
   * @throws ValidationException
   *           if the {@code subject} is invalid against this schema.
   */
  public abstract void validate(final Object subject);

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    return result;
  }

  @Override
  @Generated(value = "eclipse")
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Schema other = (Schema) obj;
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (title == null) {
      if (other.title != null) {
        return false;
      }
    } else if (!title.equals(other.title)) {
      return false;
    }
    return true;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getId() {
    return id;
  }

  /**
   * Describes the instance as a JSONObject to {@code writer}.
   *
   * @param writer
   *          it will receive the schema description
   */
  final void describeTo(final JSONWriter writer) {
    writer.object();
    if (title != null) {
      writer.key("title");
      writer.value(title);
    }
    if (description != null) {
      writer.key("description");
      writer.value(description);
    }
    if (id != null) {
      writer.key("id");
      writer.value(id);
    }
    describePropertiesTo(writer);
    writer.endObject();
  }

  /**
   * Subclasses are supposed to override this method to describe the subclass-specific attributes.
   * This method is called by {@link #describeTo(JSONWriter)} after adding the generic properties if
   * they are present ({@code id}, {@code title} and {@code description}). As a side effect,
   * overriding subclasses don't have to open and close the object with {@link JSONWriter#object()}
   * and {@link JSONWriter#endObject()}.
   *
   * @param writer
   *          it will receive the schema description
   */
  void describePropertiesTo(final JSONWriter writer) {

  }

  @Override
  public String toString() {
    StringWriter w = new StringWriter();
    describeTo(new JSONWriter(w));
    return w.getBuffer().toString();
  }

}
