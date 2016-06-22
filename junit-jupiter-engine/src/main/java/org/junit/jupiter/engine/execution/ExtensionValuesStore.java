/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code ExtensionValuesStore} is used inside implementations of
 * {@link ExtensionContext}
 * to store and retrieve attributes with {@link #get}, {@link #put}, or
 * {@link #getOrComputeIfAbsent}.
 *
 * @since 5.0
 */
@API(Internal)
public class ExtensionValuesStore {

	private final ExtensionValuesStore parentStore;
	private final Map<Object, StoredValue> storedValues = new HashMap<>();

	ExtensionValuesStore() {
		this(null);
	}

	public ExtensionValuesStore(ExtensionValuesStore parentStore) {
		this.parentStore = parentStore;
	}

	Object get(Namespace namespace, Object key) {
		StoredValue storedValue = getStoredValue(namespace, key);
		if (storedValue != null)
			return storedValue.value;
		else if (parentStore != null)
			return parentStore.get(namespace, key);
		else
			return null;
	}

	private StoredValue getStoredValue(Namespace namespace, Object key) {
		CompositeKey compositeKey = new CompositeKey(namespace, key);
		return storedValues.get(compositeKey);
	}

	void put(Namespace namespace, Object key, Object value) {
		Preconditions.notNull(namespace, "Namespace must not be null");
		Preconditions.notNull(key, "key must not be null");

		putStoredValue(namespace, key, new StoredValue(value));
	}

	private void putStoredValue(Namespace namespace, Object key, StoredValue storedValue) {
		CompositeKey compositeKey = new CompositeKey(namespace, key);
		storedValues.put(compositeKey, storedValue);
	}

	Object getOrComputeIfAbsent(Namespace namespace, Object key, Function<Object, Object> defaultCreator) {
		StoredValue storedValue = getStoredValue(namespace, key);
		if (storedValue == null) {
			storedValue = new StoredValue(defaultCreator.apply(key));
			putStoredValue(namespace, key, storedValue);
		}
		return storedValue.value;
	}

	Object remove(Namespace namespace, Object key) {
		CompositeKey compositeKey = new CompositeKey(namespace, key);
		StoredValue previous = storedValues.remove(compositeKey);
		return (previous != null ? previous.value : null);
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
