/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.gen5.commons.util.Preconditions;

/**
 * An {@code ExtensionValuesStore} is used inside {@link AbstractExtensionContext} to store and retrieve
 * attributes with {@code get}, {@code put} and {@code getOrCreateIfAbsent}.
 */
class ExtensionValuesStore {

	private final ExtensionValuesStore parentStore;
	private final Map<Object, StoredValue> storedValues = new HashMap<>();

	public ExtensionValuesStore() {
		this(null);
	}

	public ExtensionValuesStore(ExtensionValuesStore parentStore) {
		this.parentStore = parentStore;
	}

	public Object get(Object key) {
		return get(key, Namespace.DEFAULT);
	}

	public Object get(Object key, Namespace namespace) {
		StoredValue storedValue = getStoredValue(key, namespace);
		if (storedValue != null)
			return storedValue.value;
		else if (parentStore != null)
			return parentStore.get(key, namespace);
		else
			return null;
	}

	private StoredValue getStoredValue(Object key, Namespace namespace) {
		ComposedKey composedKey = new ComposedKey(key, namespace);
		return storedValues.get(composedKey);
	}

	public void put(Object key, Object value) {
		put(key, value, Namespace.DEFAULT);
	}

	public void put(Object key, Object value, Namespace namespace) {
		Preconditions.notNull(key, "A key must not be null");
		Preconditions.notNull(namespace, "A namespace must not be null");

		putStoredValue(key, namespace, new StoredValue(value));
	}

	private void putStoredValue(Object key, Namespace namespace, StoredValue storedValue) {
		ComposedKey composedKey = new ComposedKey(key, namespace);
		storedValues.put(composedKey, storedValue);
	}

	public Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator) {
		return getOrComputeIfAbsent(key, defaultCreator, Namespace.DEFAULT);
	}

	public Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator, Namespace namespace) {
		StoredValue storedValue = getStoredValue(key, namespace);
		if (storedValue == null) {
			storedValue = new StoredValue(defaultCreator.apply(key));
			putStoredValue(key, namespace, storedValue);
		}
		return storedValue.value;
	}

	public Object remove(Object key) {
		return remove(key, Namespace.DEFAULT);
	}

	public Object remove(Object key, Namespace namespace) {
		ComposedKey composedKey = new ComposedKey(key, namespace);
		StoredValue previous = storedValues.remove(composedKey);
		return (previous != null ? previous.value : null);
	}

	private static class ComposedKey {

		private final Object key;
		private final Namespace namespace;

		private ComposedKey(Object key, Namespace namespace) {
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

	public static class Namespace {

		public static Namespace DEFAULT = Namespace.sharedWith(new Object());

		public static Namespace sharedWith(Object local) {
			Preconditions.notNull(local, "A local must not be null");

			return new Namespace(local);
		}

		private final Object local;

		private Namespace(Object local) {
			this.local = local;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Namespace namespace = (Namespace) o;
			return local != null ? local.equals(namespace.local) : namespace.local == null;
		}

		@Override
		public int hashCode() {
			return local != null ? local.hashCode() : 0;
		}
	}

}
