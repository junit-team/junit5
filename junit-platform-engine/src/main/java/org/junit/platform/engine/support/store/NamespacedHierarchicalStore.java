/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.store;

import static java.util.Comparator.comparing;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.commons.util.ReflectionUtils.getWrapperType;
import static org.junit.platform.commons.util.ReflectionUtils.isAssignableTo;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/**
 * {@code ExtensionValuesStore} is a hierarchical namespaced key-value store.
 * <p>
 * Its behavior when closed can be customized by passing a {@link CloseAction}
 * to {@link #NamespacedHierarchicalStore(NamespacedHierarchicalStore, CloseAction)}.
 * <p>
 * This class is thread-safe.
 *
 * @param <N> Namespace type
 * @since 5.10
 */
@API(status = EXPERIMENTAL, since = "5.10")
public class NamespacedHierarchicalStore<N> implements AutoCloseable {

	private final AtomicInteger insertOrderSequence = new AtomicInteger();
	private final ConcurrentMap<CompositeKey<N>, StoredValue> storedValues = new ConcurrentHashMap<>(4);
	private final NamespacedHierarchicalStore<N> parentStore;
	private final CloseAction<N> closeAction;

	/**
	 * Create a new store with the supplied parent.
	 *
	 * @param parentStore The parent store to use for lookups; may be
	 *                    {@code null}.
	 */
	public NamespacedHierarchicalStore(NamespacedHierarchicalStore<N> parentStore) {
		this(parentStore, null);
	}

	/**
	 * Create a new store with the supplied parent and close action.
	 *
	 * @param parentStore The parent store to use for lookups; may be
	 *                    {@code null}.
	 * @param closeAction The action to be called for each stored value when
	 *                    this store is closed; may be {@code null}.
	 */
	public NamespacedHierarchicalStore(NamespacedHierarchicalStore<N> parentStore, CloseAction<N> closeAction) {
		this.parentStore = parentStore;
		this.closeAction = closeAction;
	}

	public NamespacedHierarchicalStore<N> newChild() {
		return new NamespacedHierarchicalStore<>(this, closeAction);
	}

	/**
	 * If a close action is configured, it will be called with all successfully
	 * stored values in reverse insertion order.
	 *
	 * <p>Closing a store does not close its parent or any of its children.
	 */
	@Override
	public void close() {
		if (closeAction == null) {
			return;
		}
		ThrowableCollector throwableCollector = new ThrowableCollector(__ -> false);
		storedValues.entrySet().stream() //
				.map(e -> e.getValue().evaluateSafely(e.getKey())) //
				.filter(Objects::nonNull) //
				.sorted(EvaluatedValue.REVERSE_INSERT_ORDER) //
				.forEach(it -> throwableCollector.execute(() -> it.close(closeAction)));
		throwableCollector.assertEmpty();
	}

	/**
	 * Get the value stored for the supplied namespace and key in this or the
	 * parent store, if present.
	 *
	 * @param namespace the namespace; never {@code null}
	 * @param key the key; never {@code null}
	 * @return stored value; may be {@code null}
	 */
	public Object get(N namespace, Object key) {
		StoredValue storedValue = getStoredValue(new CompositeKey<>(namespace, key));
		return StoredValue.evaluateIfNotNull(storedValue);
	}

	/**
	 * Get the value stored for the supplied namespace and key in this or the
	 * parent store, if present, and cast it to the supplied required type.
	 *
	 * @param namespace the namespace; never {@code null}
	 * @param key the key; never {@code null}
	 * @param requiredType the required type of the value; never {@code null}
	 * @return stored value; may be {@code null}
	 * @throws NamespacedHierarchicalStoreException if the stored value cannot
	 *                                              be cast to the required type
	 */
	public <T> T get(N namespace, Object key, Class<T> requiredType) {
		Object value = get(namespace, key);
		return castToRequiredType(key, value, requiredType);
	}

	/**
	 * Get the value stored for the supplied namespace and key in this or the
	 * parent store, if present, or call the supplied function to compute it.
	 *
	 * @param namespace the namespace; never {@code null}
	 * @param key the key; never {@code null}
	 * @param defaultCreator the function called with the supplied {@code key}
	 * to create a new value; never {@code null} but may return {@code null}
	 * @return stored value; may be {@code null}
	 */
	public <K, V> Object getOrComputeIfAbsent(N namespace, K key, Function<K, V> defaultCreator) {
		CompositeKey<N> compositeKey = new CompositeKey<>(namespace, key);
		Preconditions.notNull(defaultCreator, "defaultCreator must not be null");
		StoredValue storedValue = getStoredValue(compositeKey);
		if (storedValue == null) {
			storedValue = storedValues.computeIfAbsent(compositeKey,
				__ -> storedValue(new MemoizingSupplier(() -> defaultCreator.apply(key))));
		}
		return storedValue.evaluate();
	}

	/**
	 * Get the value stored for the supplied namespace and key in this or the
	 * parent store, if present, or call the supplied function to compute it
	 * and, finally, cast it to the supplied required type.
	 *
	 * @param namespace the namespace; never {@code null}
	 * @param key the key; never {@code null}
	 * @param defaultCreator the function called with the supplied {@code key}
	 * to create a new value; never {@code null} but may return {@code null}
	 * @param requiredType the required type of the value; never {@code null}
	 * @return stored value; may be {@code null}
	 * @throws NamespacedHierarchicalStoreException if the stored value cannot
	 *                                              be cast to the required type
	 */
	public <K, V> V getOrComputeIfAbsent(N namespace, K key, Function<K, V> defaultCreator, Class<V> requiredType) {
		Object value = getOrComputeIfAbsent(namespace, key, defaultCreator);
		return castToRequiredType(key, value, requiredType);
	}

	/**
	 * Put the supplied value for the supplied namespace and key into this
	 * store and return the previously associated value in this store.
	 *
	 * <p>The {@link CloseAction} will <em>not</em> be called for the previously
	 * stored value, if any.
	 *
	 * @param namespace the namespace; never {@code null}
	 * @param key the key; never {@code null}
	 * @param value the value to store; may be {@code null}
	 * @return previously stored value; may be {@code null}
	 * @throws NamespacedHierarchicalStoreException if the stored value cannot
	 *                                              be cast to the required type
	 */
	public Object put(N namespace, Object key, Object value) {
		StoredValue oldValue = storedValues.put(new CompositeKey<>(namespace, key), storedValue(() -> value));
		return StoredValue.evaluateIfNotNull(oldValue);
	}

	/**
	 * Remove the value stored for the supplied namespace and key from this
	 * store.
	 *
	 * <p>The {@link CloseAction} will <em>not</em> be called for the removed
	 * value.
	 *
	 * @param namespace the namespace; never {@code null}
	 * @param key the key; never {@code null}
	 * @return previously stored value; may be {@code null}
	 */
	public Object remove(N namespace, Object key) {
		StoredValue previous = storedValues.remove(new CompositeKey<>(namespace, key));
		return StoredValue.evaluateIfNotNull(previous);
	}

	/**
	 * Remove the value stored for the supplied namespace and key from this
	 * store and cast it to the supplied required type.
	 *
	 * <p>The {@link CloseAction} will <em>not</em> be called for the removed
	 * value.
	 *
	 * @param namespace the namespace; never {@code null}
	 * @param key the key; never {@code null}
	 * @param requiredType the required type of the value; never {@code null}
	 * @return previously stored value; may be {@code null}
	 * @throws NamespacedHierarchicalStoreException if the stored value cannot
	 *                                              be cast to the required type
	 */
	public <T> T remove(N namespace, Object key, Class<T> requiredType) {
		Object value = remove(namespace, key);
		return castToRequiredType(key, value, requiredType);
	}

	private StoredValue storedValue(Supplier<Object> value) {
		return new StoredValue(insertOrderSequence.getAndIncrement(), value);
	}

	private StoredValue getStoredValue(CompositeKey<N> compositeKey) {
		StoredValue storedValue = storedValues.get(compositeKey);
		if (storedValue != null) {
			return storedValue;
		}
		if (parentStore != null) {
			return parentStore.getStoredValue(compositeKey);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> T castToRequiredType(Object key, Object value, Class<T> requiredType) {
		Preconditions.notNull(requiredType, "requiredType must not be null");
		if (value == null) {
			return null;
		}
		if (isAssignableTo(value, requiredType)) {
			if (requiredType.isPrimitive()) {
				return (T) getWrapperType(requiredType).cast(value);
			}
			return requiredType.cast(value);
		}
		// else
		throw new NamespacedHierarchicalStoreException(
			String.format("Object stored under key [%s] is not of required type [%s]", key, requiredType.getName()));
	}

	private static class CompositeKey<N> {

		private final N namespace;
		private final Object key;

		private CompositeKey(N namespace, Object key) {
			this.namespace = Preconditions.notNull(namespace, "namespace must not be null");
			this.key = Preconditions.notNull(key, "key must not be null");
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			CompositeKey<?> that = (CompositeKey<?>) o;
			return this.namespace.equals(that.namespace) && this.key.equals(that.key);
		}

		@Override
		public int hashCode() {
			return Objects.hash(namespace, key);
		}

	}

	private static class StoredValue {

		private final int order;
		private final Supplier<Object> supplier;

		StoredValue(int order, Supplier<Object> supplier) {
			this.order = order;
			this.supplier = supplier;
		}

		private <N> EvaluatedValue<N> evaluateSafely(CompositeKey<N> compositeKey) {
			try {
				return new EvaluatedValue<>(compositeKey, order, evaluate());
			}
			catch (Throwable t) {
				UnrecoverableExceptions.rethrowIfUnrecoverable(t);
				return null;
			}
		}

		private Object evaluate() {
			return supplier.get();
		}

		static Object evaluateIfNotNull(StoredValue value) {
			return value != null ? value.evaluate() : null;
		}

	}

	private static class EvaluatedValue<N> {

		private static final Comparator<EvaluatedValue<?>> REVERSE_INSERT_ORDER = comparing(
			(EvaluatedValue<?> it) -> it.order).reversed();

		private final CompositeKey<N> compositeKey;
		private final int order;
		private final Object value;

		EvaluatedValue(CompositeKey<N> key, int order, Object value) {
			this.compositeKey = key;
			this.order = order;
			this.value = value;
		}

		void close(CloseAction<N> closeAction) throws Throwable {
			closeAction.close(compositeKey.namespace, compositeKey.key, value);
		}
	}

	/**
	 * Thread-safe {@link Supplier} that memoizes the result of calling its
	 * delegate and ensures it is called at most once.
	 *
	 * <p>If the delegate throws an exception, it is stored and rethrown every
	 * time {@link #get()} is called.
	 *
	 * @see StoredValue
	 */
	private static class MemoizingSupplier implements Supplier<Object> {

		private static final Object NO_VALUE_SET = new Object();

		private final Supplier<Object> delegate;
		private volatile Object value = NO_VALUE_SET;

		private MemoizingSupplier(Supplier<Object> delegate) {
			this.delegate = delegate;
		}

		@Override
		public Object get() {
			if (value == NO_VALUE_SET) {
				computeValue();
			}
			if (value instanceof Failure) {
				ExceptionUtils.throwAsUncheckedException(((Failure) value).throwable);
			}
			return value;
		}

		private synchronized void computeValue() {
			try {
				if (value == NO_VALUE_SET) {
					value = delegate.get();
				}
			}
			catch (Throwable t) {
				value = new Failure(t);
				UnrecoverableExceptions.rethrowIfUnrecoverable(t);
			}
		}

		private static class Failure {

			private final Throwable throwable;

			public Failure(Throwable throwable) {
				this.throwable = throwable;
			}
		}

	}

	/**
	 * Called for each stored value in a {@link NamespacedHierarchicalStore}.
	 */
	@FunctionalInterface
	public interface CloseAction<N> {

		/**
		 * Close the supplied {@code value}.
		 *
		 * @param namespace the namespace; never {@code null}
		 * @param key the key; never {@code null}
		 * @param value the value; never {@code null}
		 */
		void close(N namespace, Object key, Object value) throws Throwable;
	}

}
