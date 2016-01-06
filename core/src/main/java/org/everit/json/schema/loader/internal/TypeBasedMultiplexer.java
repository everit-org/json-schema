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
package org.everit.json.schema.loader.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.everit.json.schema.SchemaException;
import org.json.JSONObject;

/**
 * Used by {@code org.everit.json.schema.loader.SchemaLoader.SchemaLoader} during schema loading for
 * type-based action selections. In other words this utility class is used for avoiding
 * {@code if..instanceof..casting} constructs. Together with the {@link OnTypeConsumer}
 * implementations it forms a fluent API to deal with the parts of the JSON schema where multiple
 * kind of values are valid for a given key.
 *
 * <p>
 * Example usage: <code>
 * Object additProps = schemaJson.get("additionalProperties");
 * typeMultiplexer(additionalProps)
 * .ifIs(JSONArray.class).then(arr -> {...if additProps is a JSONArray then process it... })
 * .ifObject().then(obj -> {...if additProps is a JSONArray then process it... })
 * .requireAny(); // throw a SchemaException if additProps is neither a JSONArray nor a JSONObject
 * </code>
 *
 * This class it NOT thread-safe.
 * </p>
 */
public class TypeBasedMultiplexer {

  /**
   * An {@link OnTypeConsumer} implementation which wraps the action ({@code obj} consumer} set by
   * {@link #then(Consumer)} into an other consumer which maintains
   * {@link org.everit.json.schema.loader.SchemaLoader#id}.
   */
  private class IdModifyingTypeConsumerImpl extends OnTypeConsumerImpl<JSONObject> {

    IdModifyingTypeConsumerImpl(final Class<?> key) {
      super(key);
    }

    /**
     * Puts the {@code consumer} action with the {@code key} to the {@link TypeBasedMultiplexer}'s
     * action map, and wraps the consumer to an other consumer which properly maintains the
     * {@link org.everit.json.schema.loader.SchemaLoader#id} attribute.
     *
     * @see {@link TypeBasedMultiplexer#ifObject()} for more details about the wrapping.
     */
    @Override
    public TypeBasedMultiplexer then(final Consumer<JSONObject> consumer) {
      Consumer<JSONObject> wrapperConsumer = obj -> {
        String origId = id;
        if (obj.has("id")) {
          String idAttr = obj.getString("id");
          if (idAttr.startsWith("#")) {
            id += idAttr;
          } else {
            try {
              URL url = new URL(idAttr);
              id = url.toExternalForm();
            } catch (MalformedURLException e) {
              try {
                URL prevIdURL = new URL(id);
                id = prevIdURL.getProtocol() + "://" + prevIdURL.getHost();
                if (prevIdURL.getPort() > -1) {
                  id += ":" + prevIdURL.getPort();
                }
                id += "/" + idAttr;
              } catch (MalformedURLException e1) {
                id += idAttr;
              }
            }
          }
        }
        triggerResolutionScopeChange();
        consumer.accept(obj);
        id = origId;
        triggerResolutionScopeChange();
      };
      actions.put(key, wrapperConsumer);
      return TypeBasedMultiplexer.this;
    }

  }

  /**
   * Created and used by {@link TypeBasedMultiplexer} to set actions (consumers) for matching
   * classes.
   *
   * @param <E>
   *          the type of the input to the operation.
   * @return the parent multiplexer instance
   */
  @FunctionalInterface
  public interface OnTypeConsumer<E> {
    TypeBasedMultiplexer then(Consumer<E> consumer);
  }

  /**
   * Default implementation of {@link OnTypeConsumer}, instantiated by
   * {@link TypeBasedMultiplexer#ifIs(Class)}.
   *
   * @param <E>
   *          the type of the input to the operation.
   */
  private class OnTypeConsumerImpl<E> implements OnTypeConsumer<E> {

    protected final Class<?> key;

    OnTypeConsumerImpl(final Class<?> key) {
      this.key = key;
    }

    @Override
    public TypeBasedMultiplexer then(final Consumer<E> consumer) {
      actions.put(key, consumer);
      return TypeBasedMultiplexer.this;
    }

  }

  private final Map<Class<?>, Consumer<?>> actions = new HashMap<>();

  private final String keyOfObj;

  private final Object obj;

  private String id = "";

  private final Collection<ResolutionScopeChangeListener> scopeChangeListeners = new ArrayList<>(1);

  /**
   * Constructor with {@code null} {@code keyOfObj}.
   */
  public TypeBasedMultiplexer(final Object obj) {
    this(null, obj);
  }

  /**
   * Contstructor with {@code null id}.
   */
  public TypeBasedMultiplexer(final String keyOfObj, final Object obj) {
    this(keyOfObj, obj, null);
  }

  /**
   * Constructor.
   *
   * @param keyOfObj
   *          is an optional (nullable) string used by {@link #requireAny()} to construct the
   *          message of the {@link SchemaException} if no appropriate consumer action is found.
   * @param obj
   *          the object which' class is matched against the classes defined by {@link #ifIs(Class)}
   *          (or {@link #ifObject()}) calls.
   * @param id
   *          the scope id at the point where the multiplexer is initialized.
   */
  public TypeBasedMultiplexer(final String keyOfObj, final Object obj, final String id) {
    this.keyOfObj = keyOfObj;
    this.obj = Objects.requireNonNull(obj, "obj cannot be null");
    this.id = id == null ? "" : id;
  }

  public void addResolutionScopeChangeListener(
      final ResolutionScopeChangeListener resolutionScopeChangeListener) {
    scopeChangeListeners.add(resolutionScopeChangeListener);
  }

  /**
   * Creates a setter which will be invoked by {@link #orElse(Consumer)} or {@link #requireAny()} if
   * {@code obj} is an instance of {@code predicateClass}.
   *
   * @throws IllegalArgumentException
   *           if {@code predicateClass} is {@link JSONObject}. Use {@link #ifObject()} for matching
   *           {@code obj}'s class against {@link JSONObject}.
   */
  public <E> OnTypeConsumer<E> ifIs(final Class<E> predicateClass) {
    if (predicateClass == JSONObject.class) {
      throw new IllegalArgumentException("use ifObject() instead");
    }
    return new OnTypeConsumerImpl<E>(predicateClass);
  }

  /**
   * Creates a {@link JSONObject} consumer setter.
   *
   * <p>
   * The returned {@link OnTypeConsumer} implementation will wrap the
   * {@link OnTypeConsumer#then(Consumer) passed consumer action} with an other consumer which
   * properly maintains the {@link org.everit.json.schema.loader.SchemaLoader#id} attribute, ie. if
   * {@code obj} is a {@link JSONObject} instance and it has an {@code id} property then it will
   * append this id value to {@link org.everit.json.schema.loader.SchemaLoader#id} for the duration
   * of the action execution, then it will restore the original id.
   * </p>
   */
  public OnTypeConsumer<JSONObject> ifObject() {
    return new IdModifyingTypeConsumerImpl(JSONObject.class);
  }

  /**
   * Checks if the {@code obj} is an instance of any previously set classes (by {@link #ifIs(Class)}
   * or {@link #ifObject()}), performs the mapped action of found or invokes {@code orElseConsumer}
   * with the {@code obj}.
   */
  public void orElse(final Consumer<Object> orElseConsumer) {
    @SuppressWarnings("unchecked")
    Consumer<Object> consumer = (Consumer<Object>) actions.keySet().stream()
        .filter(clazz -> clazz.isAssignableFrom(obj.getClass()))
        .findFirst()
        .map(actions::get)
        .orElse(orElseConsumer::accept);
    consumer.accept(obj);

  }

  /**
   * Checks if the {@code obj} is an instance of any previously set classes (by {@link #ifIs(Class)}
   * or {@link #ifObject()}), performs the mapped action of found or throws with a
   * {@link SchemaException}.
   */
  public void requireAny() {
    orElse(obj -> {
      throw new SchemaException(keyOfObj, new ArrayList<Class<?>>(actions.keySet()), obj);
    });
  }

  private void triggerResolutionScopeChange() {
    for (ResolutionScopeChangeListener listener : scopeChangeListeners) {
      listener.resolutionScopeChanged(id);
    }
  }

}
