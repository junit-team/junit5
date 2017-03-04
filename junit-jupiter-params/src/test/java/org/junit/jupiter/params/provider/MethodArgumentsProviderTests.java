/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.Arguments;
import org.junit.jupiter.params.support.ObjectArrayArguments;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.PreconditionViolationException;

class MethodArgumentsProviderTests {

	@Test
	void providesArgumentsUsingStream() {
		Stream<Object[]> arguments = provideArguments("stringStreamProvider");

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" });
	}

	@Test
	void providesArgumentsUsingIterable() {
		Stream<Object[]> arguments = provideArguments("stringIterableProvider");

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" });
	}

	@Test
	void providesArgumentsUsingIterator() {
		Stream<Object[]> arguments = provideArguments("stringIterableProvider");

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" });
	}

	@Test
	void providesArgumentsUsingCollection() {
		try (Stream<Object[]> arguments = provideArguments("stringCollectionProvider")) {
			assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" });
		}

		assertThat(TestCase.collectionStreamClosed.get()).describedAs("collectionStreamClosed").isTrue();
	}

	@Test
	void throwsExceptionForIllegalReturnType() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments("providerWithIllegalReturnType"));

		assertThat(exception).hasMessageContaining("Cannot convert instance of java.lang.Integer into a Stream");
	}

	@Test
	void providesArgumentsUsingArgumentsStream() {
		Stream<Object[]> arguments = provideArguments("argumentsStreamProvider");

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" });
	}

	@Test
	void providesArgumentsUsingObjectArrays() {
		Stream<Object[]> arguments = provideArguments("objectArrayProvider");

		assertThat(arguments).containsExactly(new Object[] { "foo", 42 }, new Object[] { "bar", 23 });
	}

	@Test
	void throwsExceptionWhenMethodDoesNotExists() {
		JUnitException exception = assertThrows(JUnitException.class, () -> provideArguments("unknownMethod"));

		assertThat(exception).hasMessageContaining("Could not find method");
	}

	@Test
	void throwsExceptionWhenNoTestClassIsAvailable() {
		JUnitException exception = assertThrows(JUnitException.class, () -> provideArguments(null, "someMethod"));

		assertThat(exception).hasMessageContaining("Cannot invoke method without test class");
	}

	static class TestCase {

		static AtomicBoolean collectionStreamClosed = new AtomicBoolean(false);

		static Stream<String> stringStreamProvider() {
			return Stream.of("foo", "bar");
		}

		static Iterable<String> stringIterableProvider() {
			return TestCase::stringIteratorProvider;
		}

		static Iterator<String> stringIteratorProvider() {
			return Arrays.asList("foo", "bar").iterator();
		}

		@SuppressWarnings("serial")
		static Collection<String> stringCollectionProvider() {
			return new ArrayList<String>() {
				{
					add("foo");
					add("bar");
				}

				@Override
				public Stream<String> stream() {
					return super.stream().onClose(() -> collectionStreamClosed.set(true));
				}
			};
		}

		static Object providerWithIllegalReturnType() {
			return -1;
		}

		static Stream<ObjectArrayArguments> argumentsStreamProvider() {
			return Stream.of("foo", "bar").map(ObjectArrayArguments::create);
		}

		static Iterable<Object[]> objectArrayProvider() {
			return Arrays.asList(new Object[] { "foo", 42 }, new Object[] { "bar", 23 });
		}
	}

	private Stream<Object[]> provideArguments(String methodName) {
		return provideArguments(TestCase.class, methodName);
	}

	private Stream<Object[]> provideArguments(Class<?> testClass, String methodName) {
		MethodSource annotation = mock(MethodSource.class);
		when(annotation.value()).thenReturn(methodName);

		ContainerExtensionContext context = mock(ContainerExtensionContext.class);
		when(context.getTestClass()).thenReturn(Optional.ofNullable(testClass));

		MethodArgumentsProvider provider = new MethodArgumentsProvider();
		provider.initialize(annotation);
		return provider.arguments(context).map(Arguments::get);
	}
}
