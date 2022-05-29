/*
 * Copyright 2015-2022 the original author or authors.
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
import static org.junit.jupiter.engine.extension.MutableExtensionRegistry.createRegistryWithDefaultExtensions;
import static org.junit.jupiter.params.provider.MethodArgumentsProviderTests.DefaultFactoryMethodNameTestCase.TEST_METHOD;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.DefaultExecutableInvoker;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class MethodArgumentsProviderTests {

	private MutableExtensionRegistry extensionRegistry;

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
			new Object[] { new int[] { 1, 2 } }, //
			new Object[] { new int[] { 3, 4 } } //
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

		assertThat(exception.getMessage()).contains("Could not find factory method [unknownMethod] in class [",
			TestCase.class.getName());
	}

	@Test
	void providesArgumentsUsingDefaultFactoryMethodName() throws Exception {
		Class<?> testClass = DefaultFactoryMethodNameTestCase.class;
		var testMethod = testClass.getDeclaredMethod(TEST_METHOD, String.class);
		var arguments = provideArguments(testClass, testMethod, false, "");

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

		assertThat(exception.getMessage()).isEqualTo("Could not find factory method [nonExistentMethod()] in class ["
				+ ExternalFactoryMethods.class.getName() + "]");
	}

	@Test
	void throwsExceptionWhenFullyQualifiedMethodNameIsInvalid() {
		var exception = assertThrows(JUnitException.class,
			() -> provideArguments(ExternalFactoryMethods.class.getName() + ".wrongSyntax").toArray());

		assertThat(exception.getMessage()).isEqualTo(
			"[" + ExternalFactoryMethods.class.getName() + ".wrongSyntax] is not a valid fully qualified method name: "
					+ "it must start with a fully qualified class name followed by a '#' and then the method name, "
					+ "optionally followed by a parameter list enclosed in parentheses.");
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

	@Nested
	class ParameterResolution {

		@BeforeEach
		void registerParameterResolver() {
			JupiterConfiguration configuration = mock(JupiterConfiguration.class);
			extensionRegistry = createRegistryWithDefaultExtensions(configuration);
			extensionRegistry.registerExtension(StringResolver.class);
		}

		@Test
		void providesArgumentsUsingDefaultFactoryMethodWithParameter() throws Exception {
			Class<?> testClass = TestCase.class;
			var testMethod = testClass.getDeclaredMethod("overloadedStringStreamProvider", Object.class);
			var arguments = provideArguments(testClass, testMethod, false, "");

			assertThat(arguments).containsExactly(array("foo!"), array("bar!"));
		}

		@Test
		void providesArgumentsUsingFactoryMethodWithParameter() {
			var arguments = provideArguments("stringStreamProviderWithParameter");

			assertThat(arguments).containsExactly(array("foo!"), array("bar!"));
		}

		@Test
		void providesArgumentsUsingFullyQualifiedNameWithParameter() {
			var arguments = provideArguments(
				TestCase.class.getName() + "#stringStreamProviderWithParameter(java.lang.String)");

			assertThat(arguments).containsExactly(array("foo!"), array("bar!"));
		}

		@Test
		void throwsExceptionWhenSeveralFactoryMethodsWithSameNameAreAvailable() {
			var exception = assertThrows(PreconditionViolationException.class,
				() -> provideArguments("stringStreamProviderWithOrWithoutParameter").toArray());

			assertThat(exception.getMessage()).isEqualTo(
				"Several factory methods named [stringStreamProviderWithOrWithoutParameter] were found in class [org.junit.jupiter.params.provider.MethodArgumentsProviderTests$TestCase]");
		}

		@Test
		void providesArgumentsUsingFactoryMethodSelectedViaFullyQualifiedNameWithParameter() {
			var arguments = provideArguments(
				TestCase.class.getName() + "#stringStreamProviderWithOrWithoutParameter(java.lang.String)");

			assertThat(arguments).containsExactly(array("foo!"), array("bar!"));
		}

		@Test
		void providesArgumentsUsingFactoryMethodSelectedViaFullyQualifiedNameWithoutParameter() {
			var arguments = provideArguments(
				TestCase.class.getName() + "#stringStreamProviderWithOrWithoutParameter()");

			assertThat(arguments).containsExactly(array("foo"), array("bar"));
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

		if (testMethod == null) {
			try {
				// ensure we have a non-null method, even if it's not a real test method.
				testMethod = getClass().getMethod("toString");
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		var methodSource = mock(MethodSource.class);

		when(methodSource.value()).thenReturn(methodNames);

		var extensionContext = mock(ExtensionContext.class);
		when(extensionContext.getTestClass()).thenReturn(Optional.of(testClass));
		when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
		when(extensionContext.getExecutableInvoker()).thenReturn(
			new DefaultExecutableInvoker(extensionContext, extensionRegistry));

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

		static Stream<String> stringStreamProviderWithParameter(String parameter) {
			return Stream.of("foo" + parameter, "bar" + parameter);
		}

		static Stream<String> stringStreamProviderWithOrWithoutParameter() {
			return stringStreamProvider();
		}

		static Stream<String> stringStreamProviderWithOrWithoutParameter(String parameter) {
			return stringStreamProviderWithParameter(parameter);
		}

		// @ParameterizedTest
		// @MethodSource // use default, inferred factory method
		void overloadedStringStreamProvider(Object parameter) {
			// test implementation
		}

		// Default factory method for overloadedStringStreamProvider(Object)
		static Stream<String> overloadedStringStreamProvider(String parameter) {
			return stringStreamProviderWithParameter(parameter);
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

		static class Nested {

			static Stream<String> stringsProvider() {
				return Stream.of("nested string1", "nested string2");
			}
		}
	}

	static class StringResolver implements ParameterResolver {

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return parameterContext.getParameter().getType() == String.class;
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return "!";
		}
	}

}
