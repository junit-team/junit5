/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.MethodArgumentsProviderTests.DefaultFactoryMethodNameTestCase.TEST_METHOD;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class MethodArgumentsProviderTests {

	@Test
	void throwsExceptionForIllegalReturnType() {
		var exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments("providerWithIllegalReturnType").toArray());

		assertThat(exception).hasMessageContaining("Cannot convert instance of java.lang.Integer into a Stream");
	}

	@Test
	void providesArgumentsUsingStream() {
		var arguments = provideArguments("stringStreamProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingDoubleStream() {
		var arguments = provideArguments("doubleStreamProvider");

		assertThat(arguments).containsExactly(array(1.2), array(3.4));
	}

	@Test
	void providesArgumentsUsingLongStream() {
		var arguments = provideArguments("longStreamProvider");

		assertThat(arguments).containsExactly(array(1L), array(2L));
	}

	@Test
	void providesArgumentsUsingIntStream() {
		var arguments = provideArguments("intStreamProvider");

		assertThat(arguments).containsExactly(array(1), array(2));
	}

	/**
	 * @since 5.3.2
	 */
	@Test
	void providesArgumentsUsingStreamOfIntArrays() {
		var arguments = provideArguments("intArrayStreamProvider");

		assertThat(arguments).containsExactly( //
			array(new int[] { 1, 2 }), //
			array(new int[] { 3, 4 }) //
		);
	}

	/**
	 * @since 5.3.2
	 */
	@Test
	void providesArgumentsUsingStreamOfTwoDimensionalIntArrays() {
		var arguments = provideArguments("twoDimensionalIntArrayStreamProvider");

		assertThat(arguments).containsExactly( //
			array((Object) new int[][] { { 1, 2 }, { 2, 3 } }), //
			array((Object) new int[][] { { 4, 5 }, { 5, 6 } }) //
		);
	}

	@Test
	void providesArgumentsUsingStreamOfObjectArrays() {
		var arguments = provideArguments("objectArrayStreamProvider");

		assertThat(arguments).containsExactly(array("foo", 42), array("bar", 23));
	}

	/**
	 * @since 5.3.2
	 */
	@Test
	void providesArgumentsUsingStreamOfTwoDimensionalObjectArrays() {
		var arguments = provideArguments("twoDimensionalObjectArrayStreamProvider");

		assertThat(arguments).containsExactly( //
			array((Object) array(array("a", 1), array("b", 2))), //
			array((Object) array(array("c", 3), array("d", 4))) //
		);
	}

	@Test
	void providesArgumentsUsingStreamOfArguments() {
		var arguments = provideArguments("argumentsStreamProvider");

		assertThat(arguments).containsExactly(array("foo", 42), array("bar", 23));
	}

	@Test
	void providesArgumentsUsingIterable() {
		var arguments = provideArguments("stringIterableProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingIterator() {
		var arguments = provideArguments("stringIteratorProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingMultipleFactoryMethods() {
		var arguments = provideArguments("stringStreamProvider", "stringIterableProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"), array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingIterableOfObjectArrays() {
		var arguments = provideArguments("objectArrayIterableProvider");

		assertThat(arguments).containsExactly(array("foo", 42), array("bar", 23));
	}

	@Test
	void providesArgumentsUsingListOfStrings() {
		var arguments = provideArguments("stringArrayListProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingListOfObjectArrays() {
		var arguments = provideArguments("objectArrayListProvider");

		assertThat(arguments).containsExactly(array("foo", 42), array("bar", 23));
	}

	@Test
	void throwsExceptionWhenNonStaticFactoryMethodIsReferencedAndStaticIsRequired() {
		var exception = assertThrows(JUnitException.class,
			() -> provideArguments(NonStaticTestCase.class, null, false, "nonStaticStringStreamProvider").toArray());

		assertThat(exception).hasMessageContaining("Cannot invoke non-static method");
	}

	@Test
	void providesArgumentsFromNonStaticFactoryMethodWhenStaticIsNotRequired() {
		var arguments = provideArguments(NonStaticTestCase.class, null, true, "nonStaticStringStreamProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void throwsExceptionWhenFactoryMethodDoesNotExist() {
		var exception = assertThrows(JUnitException.class, () -> provideArguments("unknownMethod").toArray());

		assertThat(exception.getMessage()).contains("Could not find method [unknownMethod] in class [",
			TestCase.class.getName());
	}

	@Test
	void providesArgumentsUsingDefaultFactoryMethodName() {
		var method = selectMethod(DefaultFactoryMethodNameTestCase.class, TEST_METHOD,
			String.class.getName()).getJavaMethod();
		var arguments = provideArguments(DefaultFactoryMethodNameTestCase.class, method, false, "");

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingExternalFactoryMethod() {
		var arguments = provideArguments(ExternalFactoryMethods.class.getName() + "#stringsProvider");

		assertThat(arguments).containsExactly(array("string1"), array("string2"));
	}

	@Test
	void providesArgumentsUsingExternalFactoryMethodWithParentheses() {
		var arguments = provideArguments(ExternalFactoryMethods.class.getName() + "#stringsProvider()");

		assertThat(arguments).containsExactly(array("string1"), array("string2"));
	}

	@Test
	void providesArgumentsUsingExternalFactoryMethodFromStaticNestedClass() {
		var arguments = provideArguments(ExternalFactoryMethods.class.getName() + "$Nested#stringsProvider()");

		assertThat(arguments).containsExactly(array("nested string1"), array("nested string2"));
	}

	@Test
	void providesArgumentsUsingExternalAndInternalFactoryMethodsCombined() {
		var arguments = provideArguments("stringStreamProvider",
			ExternalFactoryMethods.class.getName() + "#stringsProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"), array("string1"), array("string2"));
	}

	@Test
	void throwsExceptionWhenExternalFactoryMethodDeclaresParameters() {
		var exception = assertThrows(PreconditionViolationException.class, () -> provideArguments(
			ExternalFactoryMethods.class.getName() + "#methodWithParams(String, String)").toArray());

		assertThat(exception.getMessage()).isEqualTo("factory method [" + ExternalFactoryMethods.class.getName()
				+ "#methodWithParams(String, String)] must not declare formal parameters");
	}

	@Test
	void throwsExceptionWhenClassForExternalFactoryMethodCannotBeLoaded() {
		var exception = assertThrows(JUnitException.class,
			() -> provideArguments("com.example.NonExistentExternalFactoryMethods#stringsProvider").toArray());

		assertThat(exception.getMessage()).isEqualTo(
			"Could not load class [com.example.NonExistentExternalFactoryMethods]");
	}

	@Test
	void throwsExceptionWhenExternalFactoryMethodCannotBeFound() {
		var exception = assertThrows(JUnitException.class,
			() -> provideArguments(ExternalFactoryMethods.class.getName() + "#nonExistentMethod").toArray());

		assertThat(exception.getMessage()).isEqualTo(
			"Could not find method [nonExistentMethod] in class [" + ExternalFactoryMethods.class.getName() + "]");
	}

	@Nested
	class PrimitiveArrays {

		@Test
		void providesArgumentsUsingBooleanArray() {
			var arguments = provideArguments("booleanArrayProvider");

			assertThat(arguments).containsExactly(array(Boolean.TRUE), array(Boolean.FALSE));
		}

		@Test
		void providesArgumentsUsingByteArray() {
			var arguments = provideArguments("byteArrayProvider");

			assertThat(arguments).containsExactly(array((byte) 1), array(Byte.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingCharArray() {
			var arguments = provideArguments("charArrayProvider");

			assertThat(arguments).containsExactly(array((char) 1), array(Character.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingDoubleArray() {
			var arguments = provideArguments("doubleArrayProvider");

			assertThat(arguments).containsExactly(array(1d), array(Double.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingFloatArray() {
			var arguments = provideArguments("floatArrayProvider");

			assertThat(arguments).containsExactly(array(1f), array(Float.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingIntArray() {
			var arguments = provideArguments("intArrayProvider");

			assertThat(arguments).containsExactly(array(47), array(Integer.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingLongArray() {
			var arguments = provideArguments("longArrayProvider");

			assertThat(arguments).containsExactly(array(47L), array(Long.MIN_VALUE));
		}

		@Test
		void providesArgumentsUsingShortArray() {
			var arguments = provideArguments("shortArrayProvider");

			assertThat(arguments).containsExactly(array((short) 47), array(Short.MIN_VALUE));
		}

	}

	@Nested
	class ObjectArrays {

		@Test
		void providesArgumentsUsingObjectArray() {
			var arguments = provideArguments("objectArrayProvider");

			assertThat(arguments).containsExactly(array(42), array("bar"));
		}

		@Test
		void providesArgumentsUsingStringArray() {
			var arguments = provideArguments("stringArrayProvider");

			assertThat(arguments).containsExactly(array("foo"), array("bar"));
		}

		@Test
		void providesArgumentsUsing2dObjectArray() {
			var arguments = provideArguments("twoDimensionalObjectArrayProvider");

			assertThat(arguments).containsExactly(array("foo", 42), array("bar", 23));
		}

	}

	// -------------------------------------------------------------------------

	private static Object[] array(Object... objects) {
		return objects;
	}

	private Stream<Object[]> provideArguments(String... methodNames) {
		return provideArguments(TestCase.class, null, false, methodNames);
	}

	private Stream<Object[]> provideArguments(Class<?> testClass, Method testMethod, boolean allowNonStaticMethod,
			String... methodNames) {

		var methodSource = mock(MethodSource.class);

		when(methodSource.value()).thenReturn(methodNames);

		var extensionContext = mock(ExtensionContext.class);
		when(extensionContext.getTestClass()).thenReturn(Optional.ofNullable(testClass));
		when(extensionContext.getTestMethod()).thenReturn(Optional.ofNullable(testMethod));

		doCallRealMethod().when(extensionContext).getRequiredTestMethod();
		doCallRealMethod().when(extensionContext).getRequiredTestClass();

		var testInstance = allowNonStaticMethod ? ReflectionUtils.newInstance(testClass) : null;
		when(extensionContext.getTestInstance()).thenReturn(Optional.ofNullable(testInstance));

		var provider = new MethodArgumentsProvider();
		provider.accept(methodSource);
		return provider.provideArguments(extensionContext).map(Arguments::get);
	}

	// -------------------------------------------------------------------------

	static class DefaultFactoryMethodNameTestCase {

		static final String TEST_METHOD = "testDefaultFactoryMethodName";

		static Stream<String> testDefaultFactoryMethodName() {
			return Stream.of("foo", "bar");
		}

		void testDefaultFactoryMethodName(String param) {
		}
	}

	static class TestCase {

		// --- Invalid ---------------------------------------------------------

		static Object providerWithIllegalReturnType() {
			return -1;
		}

		// --- Stream ----------------------------------------------------------

		static Stream<String> stringStreamProvider() {
			return Stream.of("foo", "bar");
		}

		static DoubleStream doubleStreamProvider() {
			return DoubleStream.of(1.2, 3.4);
		}

		static LongStream longStreamProvider() {
			return LongStream.of(1L, 2L);
		}

		static IntStream intStreamProvider() {
			return IntStream.of(1, 2);
		}

		static Stream<int[]> intArrayStreamProvider() {
			return Stream.of(new int[] { 1, 2 }, new int[] { 3, 4 });
		}

		static Stream<int[][]> twoDimensionalIntArrayStreamProvider() {
			return Stream.of(new int[][] { { 1, 2 }, { 2, 3 } }, new int[][] { { 4, 5 }, { 5, 6 } });
		}

		static Stream<Object[]> objectArrayStreamProvider() {
			return Stream.of(new Object[] { "foo", 42 }, new Object[] { "bar", 23 });
		}

		static Stream<Object[][]> twoDimensionalObjectArrayStreamProvider() {
			return Stream.of(new Object[][] { { "a", 1 }, { "b", 2 } }, new Object[][] { { "c", 3 }, { "d", 4 } });
		}

		static Stream<Arguments> argumentsStreamProvider() {
			return objectArrayStreamProvider().map(Arguments::of);
		}

		// --- Iterable / Collection -------------------------------------------

		static Iterable<String> stringIterableProvider() {
			return TestCase::stringIteratorProvider;
		}

		static Iterable<Object[]> objectArrayIterableProvider() {
			return objectArrayListProvider();
		}

		static List<String> stringArrayListProvider() {
			return Arrays.asList("foo", "bar");
		}

		static List<Object[]> objectArrayListProvider() {
			return Arrays.asList(array("foo", 42), array("bar", 23));
		}

		// --- Iterator --------------------------------------------------------

		static Iterator<String> stringIteratorProvider() {
			return Arrays.asList("foo", "bar").iterator();
		}

		// --- Array of primitives ---------------------------------------------

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

		// --- Array of objects ------------------------------------------------

		static Object[] objectArrayProvider() {
			return new Object[] { 42, "bar" };
		}

		static String[] stringArrayProvider() {
			return new String[] { "foo", "bar" };
		}

		static Object[][] twoDimensionalObjectArrayProvider() {
			return new Object[][] { { "foo", 42 }, { "bar", 23 } };
		}

	}

	// This test case mimics @TestInstance(Lifecycle.PER_CLASS)
	static class NonStaticTestCase {

		Stream<String> nonStaticStringStreamProvider() {
			return Stream.of("foo", "bar");
		}
	}

	static class ExternalFactoryMethods {

		static Stream<String> stringsProvider() {
			return Stream.of("string1", "string2");
		}

		static Stream<String> methodWithParams(String a, String b) {
			return Stream.of(a, b);
		}

		static class Nested {

			static Stream<String> stringsProvider() {
				return Stream.of("nested string1", "nested string2");
			}
		}
	}

}
