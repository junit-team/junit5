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

import static org.junit.gen5.api.Assertions.*;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;

/**
 * Microtests for {@link ExtensionValuesStore}
 */
class ExtensionValuesStoreTests {

	private ExtensionValuesStore store;
	private ExtensionValuesStore parentStore;

	private Object key = new Object();
	private Object value = new Object();

	@BeforeEach
	void initializeStore() {
		parentStore = new ExtensionValuesStore();
		store = new ExtensionValuesStore(parentStore);
	}

	@Nested
	class UsingDefaultNamespaceTest {

		@Test
		void getWithUnknownKeyReturnsNull() {
			assertNull(store.get("unknown key"));
		}

		@Test
		void putAndGetWithSameKey() {

			store.put(key, value);
			assertEquals(value, store.get(key));
		}

		@Test
		void valueCanBeReplaced() {
			store.put(key, value);

			Object newValue = new Object();
			store.put(key, newValue);

			assertEquals(newValue, store.get(key));
		}

		@Test
		void valueIsComputedIfAbsent() {
			assertEquals(value, store.getOrComputeIfAbsent(key, innerKey -> value));
			assertEquals(value, store.get(key));
		}

		@Test
		void valueIsNotComputedIfPresent() {
			store.put(key, value);

			assertEquals(value, store.getOrComputeIfAbsent(key, innerKey -> "a different value"));
			assertEquals(value, store.get(key));
		}

		@Test
		void nullIsAValidValueToPut() {
			store.put(key, null);

			assertEquals(null, store.getOrComputeIfAbsent(key, innerKey -> "a different value"));
			assertEquals(null, store.get(key));
		}

		@Test
		void keysCanBeRemoved() {
			store.put(key, value);
			store.remove(key);

			assertNull(store.get(key));
			assertEquals("a different value", store.getOrComputeIfAbsent(key, innerKey -> "a different value"));
		}

	}

	@Nested
	class InheritedValuesTest {

		@Test
		void valueFromParentIsVisible() {
			parentStore.put(key, value);
			assertEquals(value, store.get(key));
		}

		@Test
		void valueFromParentCanBeOverriddenInChild() {
			parentStore.put(key, value);

			Object otherValue = new Object();
			store.put(key, otherValue);
			assertEquals(otherValue, store.get(key));

			assertEquals(value, parentStore.get(key));
		}
	}

}
