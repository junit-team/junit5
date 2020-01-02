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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContextException;

/**
 * Unit tests for {@link ExtensionValuesStore}.
 *
 * @since 5.0
 * @see org.junit.jupiter.engine.descriptor.ExtensionContextTests
 */
public class ExtensionValuesStoreTests {

	private final Object key = "key";
	private final Object value = "value";

	private final Namespace namespace = Namespace.create("ns");

	private final ExtensionValuesStore grandParentStore = new ExtensionValuesStore(null);
	private final ExtensionValuesStore parentStore = new ExtensionValuesStore(grandParentStore);
	private final ExtensionValuesStore store = new ExtensionValuesStore(parentStore);

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
			assertNull(store.get(namespace, key));
			assertEquals(value, store.getOrComputeIfAbsent(namespace, key, innerKey -> value));
			assertEquals(value, store.get(namespace, key));
		}

		@Test
		void valueIsNotComputedIfPresentLocally() {
			store.put(namespace, key, value);

			assertEquals(value, store.getOrComputeIfAbsent(namespace, key, innerKey -> "a different value"));
			assertEquals(value, store.get(namespace, key));
		}

		@Test
		void valueIsNotComputedIfPresentInParent() {
			parentStore.put(namespace, key, value);

			assertEquals(value, store.getOrComputeIfAbsent(namespace, key, k -> "a different value"));
			assertEquals(value, store.get(namespace, key));
		}

		@Test
		void valueIsNotComputedIfPresentInGrandParent() {
			grandParentStore.put(namespace, key, value);

			assertEquals(value, store.getOrComputeIfAbsent(namespace, key, k -> "a different value"));
			assertEquals(value, store.get(namespace, key));
		}

		@Test
		void nullIsAValidValueToPut() {
			store.put(namespace, key, null);

			assertNull(store.getOrComputeIfAbsent(namespace, key, innerKey -> "a different value"));
			assertNull(store.get(namespace, key));
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
			Namespace namespace1 = Namespace.create("ns1");

			Object value2 = createObject("value2");
			Namespace namespace2 = Namespace.create("ns2");

			store.put(namespace1, key, value1);
			store.put(namespace2, key, value2);

			assertEquals(value1, store.get(namespace1, key));
			assertEquals(value2, store.get(namespace2, key));
		}

		@Test
		void valueIsComputedIfAbsentInDifferentNamespace() {
			Namespace namespace1 = Namespace.create("ns1");
			Namespace namespace2 = Namespace.create("ns2");

			assertEquals(value, store.getOrComputeIfAbsent(namespace1, key, innerKey -> value));
			assertEquals(value, store.get(namespace1, key));

			assertNull(store.get(namespace2, key));
		}

		@Test
		void keyIsOnlyRemovedInGivenNamespace() {
			Namespace namespace1 = Namespace.create("ns1");
			Namespace namespace2 = Namespace.create("ns2");

			Object value1 = createObject("value1");
			Object value2 = createObject("value2");

			store.put(namespace1, key, value1);
			store.put(namespace2, key, value2);
			store.remove(namespace1, key);

			assertNull(store.get(namespace1, key));
			assertEquals(value2, store.get(namespace2, key));
		}

		@Test
		void getWithTypeSafetyAndInvalidRequiredTypeThrowsException() {
			Integer key = 42;
			String value = "enigma";
			store.put(namespace, key, value);

			Exception exception = assertThrows(ExtensionContextException.class,
				() -> store.get(namespace, key, Number.class));
			assertEquals("Object stored under key [42] is not of required type [java.lang.Number]",
				exception.getMessage());
		}

		@Test
		void getWithTypeSafety() {
			Integer key = 42;
			String value = "enigma";
			store.put(namespace, key, value);

			// The fact that we can declare this as a String suffices for testing the required type.
			String requiredTypeValue = store.get(namespace, key, String.class);
			assertEquals(value, requiredTypeValue);
		}

		@Test
		void getWithTypeSafetyAndPrimitiveValueType() {
			String key = "enigma";
			int value = 42;
			store.put(namespace, key, value);

			// The fact that we can declare this as an int/Integer suffices for testing the required type.
			int requiredInt = store.get(namespace, key, int.class);
			Integer requiredInteger = store.get(namespace, key, Integer.class);
			assertEquals(value, requiredInt);
			assertEquals(value, requiredInteger.intValue());
		}

		@Test
		void getNullValueWithTypeSafety() {
			store.put(namespace, key, null);

			// The fact that we can declare this as a String suffices for testing the required type.
			String requiredTypeValue = store.get(namespace, key, String.class);
			assertNull(requiredTypeValue);
		}

		@Test
		void getOrComputeIfAbsentWithTypeSafetyAndInvalidRequiredTypeThrowsException() {
			String key = "pi";
			Float value = 3.14f;

			// Store a Float...
			store.put(namespace, key, value);

			// But declare that our function creates a String...
			Function<String, String> defaultCreator = k -> "enigma";

			Exception exception = assertThrows(ExtensionContextException.class,
				() -> store.getOrComputeIfAbsent(namespace, key, defaultCreator, String.class));
			assertEquals("Object stored under key [pi] is not of required type [java.lang.String]",
				exception.getMessage());
		}

		@Test
		void getOrComputeIfAbsentWithTypeSafety() {
			Integer key = 42;
			String value = "enigma";

			// The fact that we can declare this as a String suffices for testing the required type.
			String computedValue = store.getOrComputeIfAbsent(namespace, key, k -> value, String.class);
			assertEquals(value, computedValue);
		}

		@Test
		void getOrComputeIfAbsentWithTypeSafetyAndPrimitiveValueType() {
			String key = "enigma";
			int value = 42;

			// The fact that we can declare this as an int/Integer suffices for testing the required type.
			int computedInt = store.getOrComputeIfAbsent(namespace, key, k -> value, int.class);
			Integer computedInteger = store.getOrComputeIfAbsent(namespace, key, k -> value, Integer.class);
			assertEquals(value, computedInt);
			assertEquals(value, computedInteger.intValue());
		}

		@Test
		void removeWithTypeSafetyAndInvalidRequiredTypeThrowsException() {
			Integer key = 42;
			String value = "enigma";
			store.put(namespace, key, value);

			Exception exception = assertThrows(ExtensionContextException.class,
				() -> store.remove(namespace, key, Number.class));
			assertEquals("Object stored under key [42] is not of required type [java.lang.Number]",
				exception.getMessage());
		}

		@Test
		void removeWithTypeSafety() {
			Integer key = 42;
			String value = "enigma";
			store.put(namespace, key, value);

			// The fact that we can declare this as a String suffices for testing the required type.
			String removedValue = store.remove(namespace, key, String.class);
			assertEquals(value, removedValue);
			assertNull(store.get(namespace, key));
		}

		@Test
		void removeWithTypeSafetyAndPrimitiveValueType() {
			String key = "enigma";
			int value = 42;
			store.put(namespace, key, value);

			// The fact that we can declare this as an int suffices for testing the required type.
			int requiredInt = store.remove(namespace, key, int.class);
			assertEquals(value, requiredInt);

			store.put(namespace, key, value);
			// The fact that we can declare this as an Integer suffices for testing the required type.
			Integer requiredInteger = store.get(namespace, key, Integer.class);
			assertEquals(value, requiredInteger.intValue());
		}

		@Test
		void removeNullValueWithTypeSafety() {
			Integer key = 42;
			store.put(namespace, key, null);

			// The fact that we can declare this as a String suffices for testing the required type.
			String removedValue = store.remove(namespace, key, String.class);
			assertNull(removedValue);
			assertNull(store.get(namespace, key));
		}

		@Test
		void simulateRaceConditionInGetOrComputeIfAbsent() {
			int threads = 10;
			AtomicInteger counter = new AtomicInteger();
			ExtensionValuesStore localStore = new ExtensionValuesStore(null);

			List<Object> values = executeConcurrently(threads, //
				() -> localStore.getOrComputeIfAbsent(namespace, key, it -> counter.incrementAndGet()));

			assertEquals(1, counter.get());
			assertThat(values).hasSize(threads).containsOnly(1);
		}
	}

	private <T> List<T> executeConcurrently(int threads, Supplier<T> supplier) {
		ExecutorService executorService = Executors.newFixedThreadPool(threads);
		try {
			CountDownLatch latch = new CountDownLatch(threads);
			List<CompletableFuture<T>> futures = new ArrayList<>();
			for (int i = 0; i < threads; i++) {
				futures.add(CompletableFuture.supplyAsync(() -> {
					latch.countDown();
					try {
						latch.await();
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					return supplier.get();
				}, executorService));
			}
			return futures.stream().map(CompletableFuture::join).collect(toList());
		}
		finally {
			executorService.shutdown();
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
	class CompositeNamespaceTests {

		@Test
		void namespacesEqualForSamePartsSequence() {
			Namespace ns1 = Namespace.create("part1", "part2");
			Namespace ns2 = Namespace.create("part1", "part2");

			assertEquals(ns1, ns2);
		}

		@Test
		void orderOfNamespacePartsDoesMatter() {
			Namespace ns1 = Namespace.create("part1", "part2");
			Namespace ns2 = Namespace.create("part2", "part1");

			assertNotEquals(ns1, ns2);
		}

		@Test
		void additionNamespacePartMakesADifferenc() {

			Namespace ns1 = Namespace.create("part1", "part2");
			Namespace ns2 = Namespace.create("part1");
			Namespace ns3 = Namespace.create("part1", "part2");

			Object value2 = createObject("value2");

			parentStore.put(ns1, key, value);
			parentStore.put(ns2, key, value2);

			assertEquals(value, store.get(ns1, key));
			assertEquals(value, store.get(ns3, key));
			assertEquals(value2, store.get(ns2, key));
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
