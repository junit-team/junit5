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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * @since 5.0
 */
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
		Stream<Object[]> arguments = provideArguments("stringIteratorProvider");

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" });
	}

	@Test
	void providesArgumentsUsingMultipleMethods() {
		Stream<Object[]> arguments = provideArguments("stringStreamProvider", "stringIterableProvider");

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" }, new Object[] { "foo" },
			new Object[] { "bar" });
	}

	@Test
	void throwsExceptionForIllegalReturnType() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments("providerWithIllegalReturnType").toArray());

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
		JUnitException exception = assertThrows(JUnitException.class,
			() -> provideArguments("unknownMethod").toArray());

		assertThat(exception).hasMessageContaining("Could not find method");
	}

	@Test
	void throwsExceptionWhenNoTestClassIsAvailable() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> provideArguments((Class<?>) null, "someMethod"));

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

	private Stream<Object[]> provideArguments(String... methodNames) {
		return provideArguments(TestCase.class, methodNames);
	}

	private Stream<Object[]> provideArguments(Class<?> testClass, String... methodNames) {
		MethodSource annotation = mock(MethodSource.class);
		when(annotation.names()).thenReturn(methodNames);

		ContainerExtensionContext context = mock(ContainerExtensionContext.class);
		when(context.getTestClass()).thenReturn(Optional.ofNullable(testClass));

		MethodArgumentsProvider provider = new MethodArgumentsProvider();
		provider.accept(annotation);
		return provider.arguments(context).map(Arguments::get);
	}
}
