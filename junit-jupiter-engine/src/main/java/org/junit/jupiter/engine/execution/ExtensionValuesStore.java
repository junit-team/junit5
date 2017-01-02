/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.commons.util.ReflectionUtils.getWrapperType;
import static org.junit.platform.commons.util.ReflectionUtils.isAssignableTo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContextException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code ExtensionValuesStore} is used inside implementations of
 * {@link ExtensionContext} to store and retrieve attributes.
 *
 * @since 5.0
 */
@API(Internal)
public class ExtensionValuesStore {

	private final ExtensionValuesStore parentStore;
	private final Map<Object, StoredValue> storedValues = new HashMap<>(4);
	private final Object monitor = new Object();

	ExtensionValuesStore() {
		this(null);
	}

	public ExtensionValuesStore(ExtensionValuesStore parentStore) {
		this.parentStore = parentStore;
	}

	Object get(Namespace namespace, Object key) {
		synchronized (this.monitor) {
			StoredValue storedValue = getStoredValue(namespace, key);
			if (storedValue != null) {
				return storedValue.value;
			}
			else if (this.parentStore != null) {
				return this.parentStore.get(namespace, key);
			}
			else {
				return null;
			}
		}
	}

	<T> T get(Namespace namespace, Object key, Class<T> requiredType) {
		Object value = get(namespace, key);
		return castToRequiredType(key, value, requiredType);
	}

	<K, V> Object getOrComputeIfAbsent(Namespace namespace, K key, Function<K, V> defaultCreator) {
		synchronized (this.monitor) {
			StoredValue storedValue = getStoredValue(namespace, key);
			if (storedValue == null) {
				if (this.parentStore != null) {
					storedValue = this.parentStore.getStoredValue(namespace, key);
				}
				if (storedValue == null) {
					storedValue = new StoredValue(defaultCreator.apply(key));
					putStoredValue(namespace, key, storedValue);
				}
			}
			return storedValue.value;
		}
	}

	<K, V> V getOrComputeIfAbsent(Namespace namespace, K key, Function<K, V> defaultCreator, Class<V> requiredType) {
		Object value = getOrComputeIfAbsent(namespace, key, defaultCreator);
		return castToRequiredType(key, value, requiredType);
	}

	void put(Namespace namespace, Object key, Object value) {
		Preconditions.notNull(namespace, "Namespace must not be null");
		Preconditions.notNull(key, "key must not be null");

		synchronized (this.monitor) {
			putStoredValue(namespace, key, new StoredValue(value));
		}
	}

	Object remove(Namespace namespace, Object key) {
		synchronized (this.monitor) {
			StoredValue previous = this.storedValues.remove(new CompositeKey(namespace, key));
			return (previous != null ? previous.value : null);
		}
	}

	<T> T remove(Namespace namespace, Object key, Class<T> requiredType) {
		Object value = remove(namespace, key);
		return castToRequiredType(key, value, requiredType);
	}

	private StoredValue getStoredValue(Namespace namespace, Object key) {
		CompositeKey compositeKey = new CompositeKey(namespace, key);
		return this.storedValues.get(compositeKey);
	}

	private void putStoredValue(Namespace namespace, Object key, StoredValue storedValue) {
		CompositeKey compositeKey = new CompositeKey(namespace, key);
		this.storedValues.put(compositeKey, storedValue);
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
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			CompositeKey that = (CompositeKey) o;
			return this.namespace.equals(that.namespace) && this.key.equals(that.key);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.namespace, this.key);
		}
	}

	private static class StoredValue {

		private final Object value;

		private StoredValue(Object value) {
			this.value = value;
		}
	}

}
