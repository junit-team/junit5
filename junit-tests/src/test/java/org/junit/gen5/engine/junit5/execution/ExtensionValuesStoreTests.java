/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

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
			assertNull(store.get(namespace, "unknown key"));
		}

		@Test
		void putAndGetWithSameKey() {

			store.put(namespace, key, value);
			assertEquals(value, store.get(namespace, key));
		}

		@Test
		void valueCanBeReplaced() {
			store.put(namespace, key, value);

			Object newValue = new Object();
			store.put(namespace, key, newValue);

			assertEquals(newValue, store.get(namespace, key));
		}

		@Test
		void valueIsComputedIfAbsent() {
			assertEquals(value, store.getOrComputeIfAbsent(namespace, key, innerKey -> value));
			assertEquals(value, store.get(namespace, key));
		}

		@Test
		void valueIsNotComputedIfPresent() {
			store.put(namespace, key, value);

			assertEquals(value, store.getOrComputeIfAbsent(namespace, key, innerKey -> "a different value"));
			assertEquals(value, store.get(namespace, key));
		}

		@Test
		void nullIsAValidValueToPut() {
			store.put(namespace, key, null);

			assertEquals(null, store.getOrComputeIfAbsent(namespace, key, innerKey -> "a different value"));
			assertEquals(null, store.get(namespace, key));
		}

		@Test
		void keysCanBeRemoved() {
			store.put(namespace, key, value);
			assertEquals(value, store.remove(namespace, key));

			assertNull(store.get(namespace, key));
			assertEquals("a different value",
				store.getOrComputeIfAbsent(namespace, key, innerKey -> "a different value"));
		}

		@Test
		void sameKeyWithDifferentNamespaces() {
			Object value1 = createObject("value1");
			Namespace namespace1 = Namespace.of("ns1");

			Object value2 = createObject("value2");
			Namespace namespace2 = Namespace.of("ns2");

			store.put(namespace1, key, value1);
			store.put(namespace2, key, value2);

			assertEquals(value1, store.get(namespace1, key));
			assertEquals(value2, store.get(namespace2, key));
		}

		@Test
		void valueIsComputedIfAbsentInDifferentNamespace() {
			Namespace namespace1 = Namespace.of("ns1");
			Namespace namespace2 = Namespace.of("ns2");

			assertEquals(value, store.getOrComputeIfAbsent(namespace1, key, innerKey -> value));
			assertEquals(value, store.get(namespace1, key));

			assertNull(store.get(namespace2, key));
		}

		@Test
		void keyIsOnlyRemovedInGivenNamespace() {
			Namespace namespace1 = Namespace.of("ns1");
			Namespace namespace2 = Namespace.of("ns2");

			Object value1 = createObject("value1");
			Object value2 = createObject("value2");

			store.put(namespace1, key, value1);
			store.put(namespace2, key, value2);
			store.remove(namespace1, key);

			assertNull(store.get(namespace1, key));
			assertEquals(value2, store.get(namespace2, key));
		}

	}

	@Nested
	class InheritedValuesTests {

		@Test
		void valueFromParentIsVisible() {
			parentStore.put(namespace, key, value);
			assertEquals(value, store.get(namespace, key));
		}

		@Test
		void valueFromParentCanBeOverriddenInChild() {
			parentStore.put(namespace, key, value);

			Object otherValue = new Object();
			store.put(namespace, key, otherValue);
			assertEquals(otherValue, store.get(namespace, key));

			assertEquals(value, parentStore.get(namespace, key));
		}
	}

	@Nested
	class CompositNamespaceTests {

		@Test
		void additionNamespacePartMakesADifferenc() {

			Namespace ns1 = Namespace.of("part1", "part2");
			Namespace ns2 = Namespace.of("part1");
			Namespace ns3 = Namespace.of("part1", "part2");

			Object value2 = createObject("value2");

			parentStore.put(ns1, key, value);
			parentStore.put(ns2, key, value2);

			assertEquals(value, store.get(ns1, key));
			assertEquals(value, store.get(ns3, key));
			assertEquals(value2, store.get(ns2, key));
		}

		@Test
		void orderOfNamespacePartsDoesNotMatter() {

			Namespace ns1 = Namespace.of("part1", "part2");
			Namespace ns2 = Namespace.of("part2", "part1");

			parentStore.put(ns1, key, value);

			assertEquals(value, store.get(ns1, key));
			assertEquals(value, store.get(ns2, key));
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
