/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.MethodArgumentsProviderTests.TestCaseDefaultValue.TEST_METHOD;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
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
import org.junit.platform.commons.util.ReflectionUtils;

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
	void providesArgumentsUsingMultipleFactoryMethods() {
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
	void throwsExceptionWhenNonStaticFactoryMethodIsReferencedAndStaticIsRequired() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> provideArguments(NonStaticTestCase.class, null, false, "nonStaticStringStreamProvider").toArray());

		assertThat(exception).hasMessageContaining("Cannot invoke non-static method");
	}

	@Test
	void providesArgumentsFromNonStaticFactoryMethodWhenStaticIsNotRequired() {
		Stream<Object[]> arguments = provideArguments(NonStaticTestCase.class, null, true,
			"nonStaticStringStreamProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void throwsExceptionWhenFactoryMethodDoesNotExist() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> provideArguments("unknownMethod").toArray());

		assertThat(exception.getMessage()).contains("Could not find factory method [unknownMethod] in class [",
			TestCase.class.getName());
	}

	@Test
	void providesArgumentsUsingDefaultValue() {
		Stream<Object[]> arguments = provideArguments(TestCaseDefaultValue.class,
			selectMethod(TestCaseDefaultValue.class, TEST_METHOD, String.class.getName()).getJavaMethod(), false, "");
		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void providesArgumentsUsingExternalFactoryMethod() {
		Stream<Object[]> arguments = provideArguments(ExternalFactoryMethods.class.getName() + "#stringsProvider");

		assertThat(arguments).containsExactly(array("string1"), array("string2"));
	}

	@Test
	void providesArgumentsUsingExternalFactoryMethodWithParentheses() {
		Stream<Object[]> arguments = provideArguments(ExternalFactoryMethods.class.getName() + "#stringsProvider()");

		assertThat(arguments).containsExactly(array("string1"), array("string2"));
	}

	@Test
	void providesArgumentsUsingExternalFactoryMethodFromStaticNestedClass() {
		Stream<Object[]> arguments = provideArguments(
			ExternalFactoryMethods.class.getName() + "$Nested#stringsProvider()");

		assertThat(arguments).containsExactly(array("nested string1"), array("nested string2"));
	}

	@Test
	void providesArgumentsUsingExternalAndInternalFactoryMethodsCombined() {
		Stream<Object[]> arguments = provideArguments("stringStreamProvider",
			ExternalFactoryMethods.class.getName() + "#stringsProvider");

		assertThat(arguments).containsExactly(array("foo"), array("bar"), array("string1"), array("string2"));
	}

	@Test
	void throwsExceptionWhenExternalFactoryMethodDeclaresParameters() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(
				ExternalFactoryMethods.class.getName() + "#methodWithParams(String, String)").toArray());

		assertThat(exception.getMessage()).isEqualTo("factory method [" + ExternalFactoryMethods.class.getName()
				+ "#methodWithParams(String, String)] must not declare formal parameters");
	}

	@Test
	void throwsExceptionWhenClassForExternalFactoryMethodCannotBeLoaded() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> provideArguments("com.example.NonExistentExternalFactoryMethods#stringsProvider").toArray());

		assertThat(exception.getMessage()).isEqualTo(
			"Could not load class [com.example.NonExistentExternalFactoryMethods]");
	}

	@Test
	void throwsExceptionWhenExternalFactoryMethodCannotBeFound() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> provideArguments(ExternalFactoryMethods.class.getName() + "#nonExistentMethod").toArray());

		assertThat(exception.getMessage()).isEqualTo("Could not find factory method [nonExistentMethod] in class ["
				+ ExternalFactoryMethods.class.getName() + "]");
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

	private static Object[] array(Object... objects) {
		return objects;
	}

	private Stream<Object[]> provideArguments(String... methodNames) {
		return provideArguments(TestCase.class, null, false, methodNames);
	}

	private Stream<Object[]> provideArguments(Class<?> testClass, Method testMethod, boolean allowNonStaticMethod,
			String... methodNames) {
		MethodSource annotation = mock(MethodSource.class);

		when(annotation.value()).thenReturn(methodNames);

		ExtensionContext context = mock(ExtensionContext.class);
		when(context.getTestClass()).thenReturn(Optional.ofNullable(testClass));
		when(context.getTestMethod()).thenReturn(Optional.ofNullable(testMethod));

		doCallRealMethod().when(context).getRequiredTestMethod();
		doCallRealMethod().when(context).getRequiredTestClass();

		Object testInstance = allowNonStaticMethod ? ReflectionUtils.newInstance(testClass) : null;
		when(context.getTestInstance()).thenReturn(Optional.ofNullable(testInstance));

		MethodArgumentsProvider provider = new MethodArgumentsProvider();
		provider.accept(annotation);
		return provider.provideArguments(context).map(Arguments::get);
	}

	// -------------------------------------------------------------------------

	static class TestCaseDefaultValue {

		static final String TEST_METHOD = "testDefaultValue";

		static Stream<String> testDefaultValue() {
			return Stream.of("foo", "bar");
		}

		public void testDefaultValue(String param) {
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
