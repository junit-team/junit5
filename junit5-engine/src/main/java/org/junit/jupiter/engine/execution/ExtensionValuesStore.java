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

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

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
		ComposedKey composedKey = new ComposedKey(namespace, key);
		return storedValues.get(composedKey);
	}

	void put(Namespace namespace, Object key, Object value) {
		Preconditions.notNull(namespace, "Namespace must not be null");
		Preconditions.notNull(key, "key must not be null");

		putStoredValue(namespace, key, new StoredValue(value));
	}

	private void putStoredValue(Namespace namespace, Object key, StoredValue storedValue) {
		ComposedKey composedKey = new ComposedKey(namespace, key);
		storedValues.put(composedKey, storedValue);
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
		ComposedKey composedKey = new ComposedKey(namespace, key);
		StoredValue previous = storedValues.remove(composedKey);
		return (previous != null ? previous.value : null);
	}

	private static class ComposedKey {

		private final Object key;
		private final Namespace namespace;

		private ComposedKey(Namespace namespace, Object key) {
			this.key = key;
			this.namespace = namespace;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			ComposedKey composedKey = (ComposedKey) o;
			return namespace.equals(composedKey.namespace) && key.equals(composedKey.key);
		}

		@Override
		public int hashCode() {
			return 31 * key.hashCode() + namespace.hashCode();
		}
	}

	private static class StoredValue {

		private final Object value;

		private StoredValue(Object value) {
			this.value = value;
		}
	}

}
