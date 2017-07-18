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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * @since 5.0
 */
class MethodArgumentsProviderTests {

	@Test
	void providesArgumentsUsingStream() {
		Stream<Object[]> arguments = provideArguments("stringStreamProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingIterable() {
		Stream<Object[]> arguments = provideArguments("stringIterableProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingIterator() {
		Stream<Object[]> arguments = provideArguments("stringIteratorProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingMultipleMethods() {
		Stream<Object[]> arguments = provideArguments("stringStreamProvider", "stringIterableProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"), array("foo"), array("bar"));
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

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingObjectArrays() {
		Stream<Object[]> arguments = provideArguments("objectArrayProvider");

		assertThat(arguments).containsExactly(array("foo", 42), array("bar", 23));
	}

	@Test
	void throwsExceptionWhenMethodDoesNotExists() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> provideArguments("unknownMethod").toArray());

		assertThat(exception).hasMessageContaining("Could not find method");
	}

	@Test
	void throwsExceptionWhenNoTestClassIsAvailable() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments((Class<?>) null, "someMethod"));

		assertThat(exception).hasMessageContaining("required test class is not present");
	}

	@Nested
	class PrimitiveArrays {

		@Test
		void providesArgumentsUsingBooleanArray() {
			Stream<Object[]> arguments = provideArguments("booleanArrayProvider");

			assertThat(arguments).containsExactly(array(Boolean.TRUE), array(Boolean.FALSE));
		}

		@Test
		void providesArgumentsUsingByteArray() {
			Stream<Object[]> arguments = provideArguments("byteArrayProvider");

			assertThat(arguments).containsExactly(array((byte) 1), array(Byte.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingCharArray() {
			Stream<Object[]> arguments = provideArguments("charArrayProvider");

			assertThat(arguments).containsExactly(array((char) 1), array(Character.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingDoubleArray() {
			Stream<Object[]> arguments = provideArguments("doubleArrayProvider");

			assertThat(arguments).containsExactly(array(1d), array(Double.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingFloatArray() {
			Stream<Object[]> arguments = provideArguments("floatArrayProvider");

			assertThat(arguments).containsExactly(array(1f), array(Float.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingIntArray() {
			Stream<Object[]> arguments = provideArguments("intArrayProvider");

			assertThat(arguments).containsExactly(array(47), array(Integer.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingLongArray() {
			Stream<Object[]> arguments = provideArguments("longArrayProvider");

			assertThat(arguments).containsExactly(array(47L), array(Long.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingShortArray() {
			Stream<Object[]> arguments = provideArguments("shortArrayProvider");

			assertThat(arguments).containsExactly(array((short) 47), array(Short.MIN_VALUE));
		}
	}

	static class TestCase {

		static AtomicBoolean collectionStreamClosed = new AtomicBoolean(false);

		static Stream<String> stringStreamProvider() {
			return Stream.of("foo", "bar");
		}

		static boolean[] booleanArrayProvider() {
			return new boolean[] { true, false };
		}

		static byte[] byteArrayProvider() {
			return new byte[] { (byte) 1, Byte.MIN_VALUE };
		}

		static char[] charArrayProvider() {
			return new char[] { (char) 1, Character.MIN_VALUE };
		}

		static double[] doubleArrayProvider() {
			return new double[] { 1d, Double.MIN_VALUE };
		}

		static float[] floatArrayProvider() {
			return new float[] { 1f, Float.MIN_VALUE };
		}

		static int[] intArrayProvider() {
			return new int[] { 47, Integer.MIN_VALUE };
		}

		static long[] longArrayProvider() {
			return new long[] { 47L, Long.MIN_VALUE };
		}

		static short[] shortArrayProvider() {
			return new short[] { (short) 47, Short.MIN_VALUE };
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

		static Stream<Arguments> argumentsStreamProvider() {
			return Stream.of("foo", "bar").map(Arguments::of);
		}

		static Iterable<Object[]> objectArrayProvider() {
			return Arrays.asList(array("foo", 42), array("bar", 23));
		}
	}

	private static Object[] array(Object... objects) {
		return objects;
	}

	private Stream<Object[]> provideArguments(String... methodNames) {
		return provideArguments(TestCase.class, methodNames);
	}

	private Stream<Object[]> provideArguments(Class<?> testClass, String... methodNames) {
		MethodSource annotation = mock(MethodSource.class);
		when(annotation.value()).thenReturn(methodNames);

		ExtensionContext context = mock(ExtensionContext.class);
		when(context.getTestClass()).thenReturn(Optional.ofNullable(testClass));
		doCallRealMethod().when(context).getRequiredTestClass();

		MethodArgumentsProvider provider = new MethodArgumentsProvider();
		provider.accept(annotation);
		return provider.provideArguments(context).map(Arguments::get);
	}

}
