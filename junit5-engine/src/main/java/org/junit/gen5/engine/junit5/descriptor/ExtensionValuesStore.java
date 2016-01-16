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
 * attributes with {@code get}, {@code put} and {@code getOrCreateIfAbsent}
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
		StoredValue storedValue = storedValues.get(key);
		if (storedValue != null)
			return storedValue.value;
		else if (parentStore != null)
			return parentStore.get(key);
		else
			return null;
	}

	public void put(Object key, Object value) {
		Preconditions.notNull(key, "A key must not be null");
		storedValues.put(key, new StoredValue(value));
	}

	public Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator) {
		StoredValue storedValue = storedValues.get(key);
		if (storedValue == null) {
			storedValue = new StoredValue(defaultCreator.apply(key));
			storedValues.put(key, storedValue);
		}
		return storedValue.value;
	}

	public void remove(Object key) {
		storedValues.remove(key);
	}

	private static class StoredValue {
		private final Object value;

		private StoredValue(Object value) {
			this.value = value;
		}
	}
}
