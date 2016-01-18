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
import org.junit.gen5.api.extension.ExtensionContext.*;

/**
 * Microtests for {@link ExtensionValuesStore}
 */
class ExtensionValuesStoreTests {

	private ExtensionValuesStore store;
	private ExtensionValuesStore parentStore;

	private Object key = createObject("key");

	private Object value = createObject("value");

	private Namespace namespace = Namespace.of("ns");

	@BeforeEach
	void initializeStore() {
		parentStore = new ExtensionValuesStore();
		store = new ExtensionValuesStore(parentStore);
	}

	@Nested
	class StoringValuesTests {

		@Test
		void getWithUnknownKeyReturnsNull() {
			assertNull(store.get("unknown key", namespace));
		}

		@Test
		void putAndGetWithSameKey() {

			store.put(key, value, namespace);
			assertEquals(value, store.get(key, namespace));
		}

		@Test
		void valueCanBeReplaced() {
			store.put(key, value, namespace);

			Object newValue = new Object();
			store.put(key, newValue, namespace);

			assertEquals(newValue, store.get(key, namespace));
		}

		@Test
		void valueIsComputedIfAbsent() {
			assertEquals(value, store.getOrComputeIfAbsent(key, innerKey -> value, namespace));
			assertEquals(value, store.get(key, namespace));
		}

		@Test
		void valueIsNotComputedIfPresent() {
			store.put(key, value, namespace);

			assertEquals(value, store.getOrComputeIfAbsent(key, innerKey -> "a different value", namespace));
			assertEquals(value, store.get(key, namespace));
		}

		@Test
		void nullIsAValidValueToPut() {
			store.put(key, null, namespace);

			assertEquals(null, store.getOrComputeIfAbsent(key, innerKey -> "a different value", namespace));
			assertEquals(null, store.get(key, namespace));
		}

		@Test
		void keysCanBeRemoved() {
			store.put(key, value, namespace);
			assertEquals(value, store.remove(key, namespace));

			assertNull(store.get(key, namespace));
			assertEquals("a different value",
				store.getOrComputeIfAbsent(key, innerKey -> "a different value", namespace));
		}

		@Test
		void sameKeyWithDifferentNamespaces() {
			Object value1 = createObject("value1");
			Namespace namespace1 = Namespace.of("ns1");

			Object value2 = createObject("value2");
			Namespace namespace2 = Namespace.of("ns2");

			store.put(key, value1, namespace1);
			store.put(key, value2, namespace2);

			assertEquals(value1, store.get(key, namespace1));
			assertEquals(value2, store.get(key, namespace2));
		}

		@Test
		void valueIsComputedIfAbsentInDifferentNamespace() {
			Namespace namespace1 = Namespace.of("ns1");
			Namespace namespace2 = Namespace.of("ns2");

			assertEquals(value, store.getOrComputeIfAbsent(key, innerKey -> value, namespace1));
			assertEquals(value, store.get(key, namespace1));

			assertNull(store.get(key, namespace2));
		}

		@Test
		void keyIsOnlyRemovedInGivenNamespace() {
			Namespace namespace1 = Namespace.of("ns1");
			Namespace namespace2 = Namespace.of("ns2");

			Object value1 = createObject("value1");
			Object value2 = createObject("value2");

			store.put(key, value1, namespace1);
			store.put(key, value2, namespace2);
			store.remove(key, namespace1);

			assertNull(store.get(key, namespace1));
			assertEquals(value2, store.get(key, namespace2));
		}

	}

	@Nested
	class InheritedValuesTests {

		@Test
		void valueFromParentIsVisible() {
			parentStore.put(key, value, namespace);
			assertEquals(value, store.get(key, namespace));
		}

		@Test
		void valueFromParentCanBeOverriddenInChild() {
			parentStore.put(key, value, namespace);

			Object otherValue = new Object();
			store.put(key, otherValue, namespace);
			assertEquals(otherValue, store.get(key, namespace));

			assertEquals(value, parentStore.get(key, namespace));
		}
	}

	private Object createObject(final String display) {
		return new Object() {
			@Override
			public String toString() {
				return display;
			}
		};
	}

}
