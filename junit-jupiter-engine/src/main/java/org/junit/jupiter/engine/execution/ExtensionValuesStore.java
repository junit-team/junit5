/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.support.JupiterThrowableCollectorFactory.createThrowableCollector;
import static org.junit.platform.commons.util.ReflectionUtils.getWrapperType;
import static org.junit.platform.commons.util.ReflectionUtils.isAssignableTo;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContextException;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/**
 * {@code ExtensionValuesStore} is used inside implementations of
 * {@link ExtensionContext} to store and retrieve values.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class ExtensionValuesStore {

	private final ExtensionValuesStore parentStore;
	private final ConcurrentMap<CompositeKey, Supplier<Object>> storedValues = new ConcurrentHashMap<>(4);

	public ExtensionValuesStore(ExtensionValuesStore parentStore) {
		this.parentStore = parentStore;
	}

	/**
	 * Close all values that implement {@link ExtensionContext.Store.CloseableResource}.
	 *
	 * @implNote Only close values stored in this instance. This implementation
	 * does not close values in parent stores.
	 */
	public void closeAllStoredCloseableValues() {
		ThrowableCollector throwableCollector = createThrowableCollector();
		for (Supplier<Object> supplier : storedValues.values()) {
			Object value = supplier.get();
			if (value instanceof ExtensionContext.Store.CloseableResource) {
				ExtensionContext.Store.CloseableResource resource = (ExtensionContext.Store.CloseableResource) value;
				throwableCollector.execute(resource::close);
			}
		}
		throwableCollector.assertEmpty();
	}

	Object get(Namespace namespace, Object key) {
		Supplier<Object> storedValue = getStoredValue(new CompositeKey(namespace, key));
		return (storedValue != null ? storedValue.get() : null);
	}

	<T> T get(Namespace namespace, Object key, Class<T> requiredType) {
		Object value = get(namespace, key);
		return castToRequiredType(key, value, requiredType);
	}

	<K, V> Object getOrComputeIfAbsent(Namespace namespace, K key, Function<K, V> defaultCreator) {
		CompositeKey compositeKey = new CompositeKey(namespace, key);
		Supplier<Object> storedValue = getStoredValue(compositeKey);
		if (storedValue == null) {
			Supplier<Object> newValue = new MemoizingSupplier(() -> defaultCreator.apply(key));
			storedValue = Optional.ofNullable(storedValues.putIfAbsent(compositeKey, newValue)).orElse(newValue);
		}
		return storedValue.get();
	}

	<K, V> V getOrComputeIfAbsent(Namespace namespace, K key, Function<K, V> defaultCreator, Class<V> requiredType) {
		Object value = getOrComputeIfAbsent(namespace, key, defaultCreator);
		return castToRequiredType(key, value, requiredType);
	}

	void put(Namespace namespace, Object key, Object value) {
		storedValues.put(new CompositeKey(namespace, key), () -> value);
	}

	Object remove(Namespace namespace, Object key) {
		Supplier<Object> previous = storedValues.remove(new CompositeKey(namespace, key));
		return (previous != null ? previous.get() : null);
	}

	<T> T remove(Namespace namespace, Object key, Class<T> requiredType) {
		Object value = remove(namespace, key);
		return castToRequiredType(key, value, requiredType);
	}

	private Supplier<Object> getStoredValue(CompositeKey compositeKey) {
		Supplier<Object> storedValue = storedValues.get(compositeKey);
		if (storedValue != null) {
			return storedValue;
		}
		else if (parentStore != null) {
			return parentStore.getStoredValue(compositeKey);
		}
		else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T castToRequiredType(Object key, Object value, Class<T> requiredType) {
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
		throw new ExtensionContextException(
			String.format("Object stored under key [%s] is not of required type [%s]", key, requiredType.getName()));
	}

	private static class CompositeKey {

		private final Namespace namespace;
		private final Object key;

		private CompositeKey(Namespace namespace, Object key) {
			this.namespace = namespace;
			this.key = key;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			CompositeKey that = (CompositeKey) o;
			return this.namespace.equals(that.namespace) && this.key.equals(that.key);
		}

		@Override
		public int hashCode() {
			return Objects.hash(namespace, key);
		}

	}

	private static class MemoizingSupplier implements Supplier<Object> {

		private static final Object NO_VALUE_SET = new Object();

		private final Lock lock = new ReentrantLock();
		private final Supplier<Object> delegate;
		private volatile Object value = NO_VALUE_SET;

		private MemoizingSupplier(Supplier<Object> delegate) {
			this.delegate = delegate;
		}

		@Override
		public Object get() {
			if (value == NO_VALUE_SET) {
				lock.lock();
				try {
					if (value == NO_VALUE_SET) {
						value = delegate.get();
					}
				}
				finally {
					lock.unlock();
				}
			}
			return value;
		}

	}

}
