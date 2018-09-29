package org.everit.json.schema.loader.internal;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.everit.json.schema.SchemaException;
import org.json.JSONObject;

/**
 * <strong>This class is deprecated. Currently it isn't used by the library itself, although it wasn't
 * removed, to maintain backward compatiblity.</strong>
 * <p>
 * <p>
 * Used by {@code org.everit.json.schema.loader.SchemaLoader.SchemaLoader} during schema loading for
 * type-based action selections. In other words this utility class is used for avoiding
 * {@code if..instanceof..casting} constructs. Together with the {@link OnTypeConsumer}
 * implementations it forms a fluent API to deal with the parts of the JSON schema where multiple
 * kind of values are valid for a given key.
 * <p>
 * Example usage: <code>
 * Object additProps = schemaJson.get("additionalProperties");
 * typeMultiplexer(additionalProps)
 * .ifIs(JSONArray.class).then(arr -&gt; {...if additProps is a JSONArray then process it... })
 * .ifObject().then(obj -&gt; {...if additProps is a JSONArray then process it... })
 * .requireAny(); // throw a SchemaException if additProps is neither a JSONArray nor a JSONObject
 * </code>
 * <p>
 * This class it NOT thread-safe.
 * </p>
 */
@Deprecated
public class TypeBasedMultiplexer {

    /**
     * An {@link OnTypeConsumer} implementation which wraps the action ({@code obj} consumer} set by
     * {@link #then(Consumer)} into an other consumer which maintains
     * {@link org.everit.json.schema.loader.LoadingState#id}.
     */
    private class IdModifyingTypeConsumerImpl extends OnTypeConsumerImpl<JSONObject> {

        IdModifyingTypeConsumerImpl(final Class<?> key) {
            super(key);
        }

        /**
         * Puts the {@code consumer} action with the {@code key} to the {@link TypeBasedMultiplexer}'s
         * action map, and wraps the consumer to an other consumer which properly maintains the
         * {@link org.everit.json.schema.loader.LoadingState#id} attribute.
         *
         * @see {@link TypeBasedMultiplexer#ifObject()} for more details about the wrapping.
         */
        @Override
        public TypeBasedMultiplexer then(final Consumer<JSONObject> consumer) {
            Consumer<JSONObject> wrapperConsumer = obj -> {
                if (obj.has("id") && obj.get("id") instanceof String) {
                    URI origId = id;
                    String idAttr = obj.getString("id");
                    id = ReferenceResolver.resolve(id, idAttr);
                    triggerResolutionScopeChange();
                    consumer.accept(obj);
                    id = origId;
                    triggerResolutionScopeChange();
                } else {
                    consumer.accept(obj);
                }
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
     *         the type of the input to the operation.
     */
    @FunctionalInterface
    public interface OnTypeConsumer<E> {

        /**
         * Sets the callback (consumer) to be called if the type of {@code obj} is the previously set
         * {@code predicateClass}.
         *
         * @param consumer
         *         the callback to be called if the type of {@code obj} is the previously set class.
         * @return the parent multiplexer instance
         */
        TypeBasedMultiplexer then(Consumer<E> consumer);
    }

    /**
     * Default implementation of {@link OnTypeConsumer}, instantiated by
     * {@link TypeBasedMultiplexer#ifIs(Class)}.
     *
     * @param <E>
     *         the type of the input to the operation.
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

    private URI id;

    private final Collection<ResolutionScopeChangeListener> scopeChangeListeners = new ArrayList<>(1);

    /**
     * Constructor with {@code null} {@code keyOfObj} and {@code null} {@code id}.
     *
     * @param obj
     *         the object which' class is matched against the classes defined by {@link #ifIs(Class)}
     *         (or {@link #ifObject()}) calls.
     */
    public TypeBasedMultiplexer(final Object obj) {
        this(null, obj);
    }

    /**
     * Contstructor with {@code null id}.
     *
     * @param keyOfObj
     *         is an optional (nullable) string used by {@link #requireAny()} to construct the
     *         message of the {@link SchemaException} if no appropriate consumer action is found.
     * @param obj
     *         the object which' class is matched against the classes defined by {@link #ifIs(Class)}
     *         (or {@link #ifObject()}) calls.
     */
    public TypeBasedMultiplexer(final String keyOfObj, final Object obj) {
        this(keyOfObj, obj, null);
    }

    /**
     * Constructor.
     *
     * @param keyOfObj
     *         is an optional (nullable) string used by {@link #requireAny()} to construct the
     *         message of the {@link SchemaException} if no appropriate consumer action is found.
     * @param obj
     *         the object which' class is matched against the classes defined by {@link #ifIs(Class)}
     *         (or {@link #ifObject()}) calls.
     * @param id
     *         the scope id at the point where the multiplexer is initialized.
     */
    public TypeBasedMultiplexer(final String keyOfObj, final Object obj, final URI id) {
        this.keyOfObj = keyOfObj;
        this.obj = requireNonNull(obj, "obj cannot be null");
        this.id = id;
    }

    public void addResolutionScopeChangeListener(
            final ResolutionScopeChangeListener resolutionScopeChangeListener) {
        scopeChangeListeners.add(resolutionScopeChangeListener);
    }

    /**
     * Creates a setter which will be invoked by {@link #orElse(Consumer)} or {@link #requireAny()} if
     * {@code obj} is an instance of {@code predicateClass}.
     *
     * @param predicateClass
     *         the predicate class (the callback set by a subsequent
     *         {@link OnTypeConsumer#then(Consumer)} will be executed if {@code obj} is an instance
     *         of {@code predicateClass}).
     * @param <E>
     *         the type represented by {@code predicateClass}.
     * @return an {@code OnTypeConsumer} implementation to be used to set the action performed if
     * {@code obj} is an instance of {@code predicateClass}.
     * @throws IllegalArgumentException
     *         if {@code predicateClass} is {@link JSONObject}. Use {@link #ifObject()} for matching
     *         {@code obj}'s class against {@link JSONObject}.
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
     * @return an {@code OnTypeConsumer} implementation to be used to set the action performed if
     * {@code obj} is a JSONObject instance.
     */
    public OnTypeConsumer<JSONObject> ifObject() {
        return new IdModifyingTypeConsumerImpl(JSONObject.class);
    }

    /**
     * Checks if the {@code obj} is an instance of any previously set classes (by {@link #ifIs(Class)}
     * or {@link #ifObject()}), performs the mapped action of found or invokes {@code orElseConsumer}
     * with the {@code obj}.
     *
     * @param orElseConsumer
     *         the callback to be called if no types matched.
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
