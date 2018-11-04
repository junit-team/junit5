/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.testkit.engine.EventConditions.abortedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.isA;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.opentest4j.TestAbortedException;

/**
 * @since 5.0
 */
class ParameterizedTestIntegrationTests {

	@Test
	void executesWithSingleArgumentsProviderWithMultipleInvocations() {
		var results = execute(
			selectMethod(TestCase.class, "testWithTwoSingleStringArgumentsProvider", String.class.getName()));
		results.all().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("[1] foo"), finishedWithFailure(message("foo")))) //
				.haveExactly(1, event(test(), displayName("[2] bar"), finishedWithFailure(message("bar"))));
	}

	@Test
	void executesWithCsvSource() {
		var results = execute(selectMethod(TestCase.class, "testWithCsvSource", String.class.getName()));
		results.all().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("[1] foo"), finishedWithFailure(message("foo")))) //
				.haveExactly(1, event(test(), displayName("[2] bar"), finishedWithFailure(message("bar"))));
	}

	@Test
	void executesWithCustomName() {
		var results = execute(
			selectMethod(TestCase.class, "testWithCustomName", String.class.getName() + "," + Integer.TYPE.getName()));
		results.all().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("foo and 23"), finishedWithFailure(message("foo, 23")))) //
				.haveExactly(1, event(test(), displayName("bar and 42"), finishedWithFailure(message("bar, 42"))));
	}

	/**
	 * @since 5.2
	 */
	@Test
	void executesWithPrimitiveWideningConversion() {
		var results = execute(
			selectMethod(TestCase.class, "testWithPrimitiveWideningConversion", double.class.getName()));
		results.all().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("[1] 1"), finishedWithFailure(message("num: 1.0")))) //
				.haveExactly(1, event(test(), displayName("[2] 2"), finishedWithFailure(message("num: 2.0"))));
	}

	/**
	 * @since 5.1
	 */
	@Test
	void executesWithImplicitGenericConverter() {
		var results = execute(selectMethod(TestCase.class, "testWithImplicitGenericConverter", Book.class.getName()));
		results.all().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("[1] book 1"), finishedWithFailure(message("book 1")))) //
				.haveExactly(1, event(test(), displayName("[2] book 2"), finishedWithFailure(message("book 2"))));
	}

	@Test
	void legacyReportingNames() {
		var results = execute(
			selectMethod(TestCase.class, "testWithCustomName", String.class.getName() + "," + Integer.TYPE.getName()));

		// @formatter:off
		Stream<String> legacyReportingNames = results.tests().dynamicallyRegistered()
				.map(Event::getTestDescriptor)
				.map(TestDescriptor::getLegacyReportingName);
		// @formatter:on
		assertThat(legacyReportingNames).containsExactly("testWithCustomName(String, int)[1]",
			"testWithCustomName(String, int)[2]");
	}

	@Test
	void executesWithExplicitConverter() {
		var results = execute(selectMethod(TestCase.class, "testWithExplicitConverter", Integer.TYPE.getName()));
		results.all().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("[1] O"), finishedWithFailure(message("length: 1")))) //
				.haveExactly(1, event(test(), displayName("[2] XXX"), finishedWithFailure(message("length: 3"))));
	}

	@Test
	void failsContainerOnEmptyName() {
		var results = execute(selectMethod(TestCase.class, "testWithEmptyName", String.class.getName()));
		results.all().assertThatEvents() //
				.haveExactly(1, event(container(), displayName("testWithEmptyName(String)"), //
					finishedWithFailure(message(value -> value.contains("must be declared with a non-empty name")))));
	}

	@Test
	void reportsExceptionForErroneousConverter() {
		var results = execute(selectMethod(TestCase.class, "testWithErroneousConverter", Object.class.getName()));
		results.all().assertThatEvents() //
				.haveExactly(1, event(test(), finishedWithFailure(allOf(isA(ParameterResolutionException.class), //
					message("Error converting parameter at index 0: something went horribly wrong")))));
	}

	@Nested
	class MethodSourceIntegrationTests {

		@Test
		void emptyMethodSource() {
			execute("emptyMethodSource", String.class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("empty method source"))));
		}

		/**
		 * @since 5.4
		 */
		@Test
		void oneDimensionalPrimitiveArray() {
			execute("oneDimensionalPrimitiveArray", int.class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("1"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("2"))));
		}

		/**
		 * @since 5.4
		 */
		@Test
		void twoDimensionalPrimitiveArray() {
			execute("twoDimensionalPrimitiveArray", int[].class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("[1, 2]"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("[3, 4]"))));
		}

		/**
		 * @since 5.4
		 */
		@Test
		void oneDimensionalObjectArray() {
			execute("oneDimensionalObjectArray", Object.class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("one"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("2"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("three"))));
		}

		/**
		 * @since 5.4
		 */
		@Test
		void oneDimensionalStringArray() {
			execute("oneDimensionalStringArray", String.class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("one"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("two"))));
		}

		@Test
		void twoDimensionalObjectArray() {
			execute("twoDimensionalObjectArray", String.class, int.class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("one:2"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("three:4"))));
		}

		/**
		 * @since 5.4
		 */
		@Test
		void twoDimensionalStringArray() {
			execute("twoDimensionalStringArray", String.class, String.class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("one:two"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("three:four"))));
		}

		/**
		 * @since 5.4
		 */
		@Test
		void streamOfOneDimensionalPrimitiveArrays() {
			execute("streamOfOneDimensionalPrimitiveArrays", int[].class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("[1, 2]"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("[3, 4]"))));
		}

		/**
		 * @since 5.4
		 */
		@Test
		void streamOfTwoDimensionalPrimitiveArrays() {
			assertStreamOfTwoDimensionalPrimitiveArrays("streamOfTwoDimensionalPrimitiveArrays");
		}

		/**
		 * @since 5.4
		 */
		@Test
		void streamOfTwoDimensionalPrimitiveArraysWrappedInObjectArrays() {
			assertStreamOfTwoDimensionalPrimitiveArrays("streamOfTwoDimensionalPrimitiveArraysWrappedInObjectArrays");
		}

		/**
		 * @since 5.4
		 */
		@Test
		void streamOfTwoDimensionalPrimitiveArraysWrappedInArguments() {
			assertStreamOfTwoDimensionalPrimitiveArrays("streamOfTwoDimensionalPrimitiveArraysWrappedInArguments");
		}

		private void assertStreamOfTwoDimensionalPrimitiveArrays(String methodName) {
			execute(methodName, int[][].class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("[[1, 2], [3, 4]]"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("[[5, 6], [7, 8]]"))));
		}

		/**
		 * @since 5.4
		 */
		@Test
		void streamOfOneDimensionalObjectArrays() {
			execute("streamOfOneDimensionalObjectArrays", String.class, int.class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("one:2"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("three:4"))));
		}

		/**
		 * @since 5.4
		 */
		@Test
		void streamOfTwoDimensionalObjectArrays() {
			execute("streamOfTwoDimensionalObjectArrays", Object[][].class).tests().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("[[one, 2], [three, 4]]"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("[[five, 6], [seven, 8]]"))));
		}

		@Test
		void reportsContainerWithAssumptionFailureInMethodSourceAsAborted() {
			execute("assumptionFailureInMethodSourceFactoryMethod", String.class).all().assertThatEvents() //
					.haveExactly(1, event(container("test-template:assumptionFailureInMethodSourceFactoryMethod"), //
						abortedWithReason(
							allOf(isA(TestAbortedException.class), message("Assumption failed: nothing to test")))));
		}

		private EngineExecutionResults execute(String methodName, Class<?>... methodParameterTypes) {
			return EngineTestKit.engine(new JupiterTestEngine())//
					.selectors(selectMethod(MethodSourceTestCase.class, methodName,
						ClassUtils.nullSafeToString(methodParameterTypes)))//
					.execute();
		}

	}

	@Nested
	class UnusedArgumentsIntegrationTests {

		@Test
		void executesWithArgumentsSourceProvidingUnusedArguments() {
			var results = execute(selectMethod(UnusedArgumentsTestCase.class,
				"testWithTwoUnusedStringArgumentsProvider", String.class.getName()));
			results.all().assertThatEvents() //
					.haveExactly(1, event(test(), displayName("[1] foo"), finishedWithFailure(message("foo")))) //
					.haveExactly(1, event(test(), displayName("[2] bar"), finishedWithFailure(message("bar"))));
		}

		@Test
		void executesWithCsvSourceContainingUnusedArguments() {
			var results = execute(selectMethod(UnusedArgumentsTestCase.class,
				"testWithCsvSourceContainingUnusedArguments", String.class.getName()));
			results.all().assertThatEvents() //
					.haveExactly(1, event(test(), displayName("[1] foo"), finishedWithFailure(message("foo")))) //
					.haveExactly(1, event(test(), displayName("[2] bar"), finishedWithFailure(message("bar"))));
		}

		@Test
		void executesWithCsvFileSourceContainingUnusedArguments() {
			var results = execute(selectMethod(UnusedArgumentsTestCase.class,
				"testWithCsvFileSourceContainingUnusedArguments", String.class.getName()));
			results.all().assertThatEvents() //
					.haveExactly(1, event(test(), displayName("[1] foo"), finishedWithFailure(message("foo")))) //
					.haveExactly(1, event(test(), displayName("[2] bar"), finishedWithFailure(message("bar"))));
		}

		@Test
		void executesWithMethodSourceProvidingUnusedArguments() {
			var results = execute(selectMethod(UnusedArgumentsTestCase.class,
				"testWithMethodSourceProvidingUnusedArguments", String.class.getName()));
			results.all().assertThatEvents() //
					.haveExactly(1, event(test(), displayName("[1] foo"), finishedWithFailure(message("foo")))) //
					.haveExactly(1, event(test(), displayName("[2] bar"), finishedWithFailure(message("bar"))));
		}

	}

	@Test
	void executesLifecycleMethods() {
		// reset static collections
		LifecycleTestCase.lifecycleEvents.clear();
		LifecycleTestCase.testMethods.clear();

		var results = execute(selectClass(LifecycleTestCase.class));
		results.all().assertThatEvents() //
				.haveExactly(1, event(test("test1"), displayName("[1] foo"), finishedWithFailure(message("foo")))) //
				.haveExactly(1, event(test("test1"), displayName("[2] bar"), finishedWithFailure(message("bar"))));

		List<String> testMethods = new ArrayList<>(LifecycleTestCase.testMethods);

		// @formatter:off
		assertThat(LifecycleTestCase.lifecycleEvents).containsExactly(
			"beforeAll:ParameterizedTestIntegrationTests$LifecycleTestCase",
				"providerMethod",
					"constructor:ParameterizedTestIntegrationTests$LifecycleTestCase",
					"beforeEach:[1] foo",
						testMethods.get(0) + ":[1] foo",
					"afterEach:[1] foo",
					"constructor:ParameterizedTestIntegrationTests$LifecycleTestCase",
					"beforeEach:[2] bar",
						testMethods.get(0) + ":[2] bar",
					"afterEach:[2] bar",
				"providerMethod",
					"constructor:ParameterizedTestIntegrationTests$LifecycleTestCase",
					"beforeEach:[1] foo",
						testMethods.get(1) + ":[1] foo",
					"afterEach:[1] foo",
					"constructor:ParameterizedTestIntegrationTests$LifecycleTestCase",
					"beforeEach:[2] bar",
						testMethods.get(1) + ":[2] bar",
					"afterEach:[2] bar",
			"afterAll:ParameterizedTestIntegrationTests$LifecycleTestCase");
		// @formatter:on
	}

	private EngineExecutionResults execute(DiscoverySelector... selectors) {
		return EngineTestKit.engine(new JupiterTestEngine()).selectors(selectors).execute();
	}

	// -------------------------------------------------------------------------

	static class TestCase {

		@ParameterizedTest
		@ArgumentsSource(TwoSingleStringArgumentsProvider.class)
		void testWithTwoSingleStringArgumentsProvider(String argument) {
			fail(argument);
		}

		@ParameterizedTest
		@CsvSource({ "foo", "bar" })
		void testWithCsvSource(String argument) {
			fail(argument);
		}

		@ParameterizedTest(name = "{0} and {1}")
		@CsvSource({ "foo, 23", "bar, 42" })
		void testWithCustomName(String argument, int i) {
			fail(argument + ", " + i);
		}

		@ParameterizedTest
		@ValueSource(shorts = { 1, 2 })
		void testWithPrimitiveWideningConversion(double num) {
			fail("num: " + num);
		}

		@ParameterizedTest
		@ValueSource(strings = { "book 1", "book 2" })
		void testWithImplicitGenericConverter(Book book) {
			fail(book.title);
		}

		@ParameterizedTest
		@ValueSource(strings = { "O", "XXX" })
		void testWithExplicitConverter(@ConvertWith(StringLengthConverter.class) int length) {
			fail("length: " + length);
		}

		@ParameterizedTest(name = "  \t   ")
		@ValueSource(strings = "not important")
		void testWithEmptyName(String argument) {
			fail(argument);
		}

		@ParameterizedTest
		@ValueSource(ints = 42)
		void testWithErroneousConverter(@ConvertWith(ErroneousConverter.class) Object ignored) {
			fail("this should never be called");
		}

	}

	@TestMethodOrder(OrderAnnotation.class)
	static class MethodSourceTestCase {

		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		@ParameterizedTest(name = "{arguments}")
		@MethodSource
		@interface MethodSourceTest {
		}

		@MethodSourceTest
		void emptyMethodSource(String argument) {
			fail(argument);
		}

		@MethodSourceTest
		@Order(1)
		void oneDimensionalPrimitiveArray(int x) {
			fail("" + x);
		}

		@MethodSourceTest
		@Order(2)
		void twoDimensionalPrimitiveArray(int[] array) {
			fail(Arrays.toString(array));
		}

		@MethodSourceTest
		@Order(3)
		void oneDimensionalObjectArray(Object o) {
			fail("" + o);
		}

		@MethodSourceTest
		@Order(4)
		void oneDimensionalStringArray(String s) {
			fail(s);
		}

		@MethodSourceTest
		@Order(5)
		void twoDimensionalObjectArray(String s, int x) {
			fail(s + ":" + x);
		}

		@MethodSourceTest
		@Order(6)
		void twoDimensionalStringArray(String s1, String s2) {
			fail(s1 + ":" + s2);
		}

		@MethodSourceTest
		@Order(7)
		void streamOfOneDimensionalPrimitiveArrays(int[] array) {
			fail(Arrays.toString(array));
		}

		@MethodSourceTest
		@Order(8)
		void streamOfTwoDimensionalPrimitiveArrays(int[][] array) {
			fail(Arrays.deepToString(array));
		}

		@MethodSourceTest
		@Order(9)
		void streamOfTwoDimensionalPrimitiveArraysWrappedInObjectArrays(int[][] array) {
			fail(Arrays.deepToString(array));
		}

		@MethodSourceTest
		@Order(10)
		void streamOfTwoDimensionalPrimitiveArraysWrappedInArguments(int[][] array) {
			fail(Arrays.deepToString(array));
		}

		@MethodSourceTest
		@Order(11)
		void streamOfOneDimensionalObjectArrays(String s, int x) {
			fail(s + ":" + x);
		}

		@MethodSourceTest
		@Order(12)
		void streamOfTwoDimensionalObjectArrays(Object[][] array) {
			fail(Arrays.deepToString(array));
		}

		// ---------------------------------------------------------------------

		static Stream<Arguments> emptyMethodSource() {
			return Stream.of(arguments("empty method source"));
		}

		static int[] oneDimensionalPrimitiveArray() {
			return new int[] { 1, 2 };
		}

		static int[][] twoDimensionalPrimitiveArray() {
			return new int[][] { { 1, 2 }, { 3, 4 } };
		}

		static Object[] oneDimensionalObjectArray() {
			return new Object[] { "one", 2, "three" };
		}

		static Object[] oneDimensionalStringArray() {
			return new Object[] { "one", "two" };
		}

		static Object[][] twoDimensionalObjectArray() {
			return new Object[][] { { "one", 2 }, { "three", 4 } };
		}

		static String[][] twoDimensionalStringArray() {
			return new String[][] { { "one", "two" }, { "three", "four" } };
		}

		static Stream<int[]> streamOfOneDimensionalPrimitiveArrays() {
			return Stream.of(new int[] { 1, 2 }, new int[] { 3, 4 });
		}

		static Stream<int[][]> streamOfTwoDimensionalPrimitiveArrays() {
			return Stream.of(new int[][] { { 1, 2 }, { 3, 4 } }, new int[][] { { 5, 6 }, { 7, 8 } });
		}

		static Stream<Object[]> streamOfTwoDimensionalPrimitiveArraysWrappedInObjectArrays() {
			return Stream.of(new Object[] { new int[][] { { 1, 2 }, { 3, 4 } } },
				new Object[] { new int[][] { { 5, 6 }, { 7, 8 } } });
		}

		static Stream<Arguments> streamOfTwoDimensionalPrimitiveArraysWrappedInArguments() {
			return Stream.of(arguments((Object) new int[][] { { 1, 2 }, { 3, 4 } }),
				arguments((Object) new int[][] { { 5, 6 }, { 7, 8 } }));
		}

		static Stream<Object[]> streamOfOneDimensionalObjectArrays() {
			return Stream.of(new Object[] { "one", 2 }, new Object[] { "three", 4 });
		}

		static Stream<Object[][]> streamOfTwoDimensionalObjectArrays() {
			return Stream.of(new Object[][] { { "one", 2 }, { "three", 4 } },
				new Object[][] { { "five", 6 }, { "seven", 8 } });
		}

		// ---------------------------------------------------------------------

		@MethodSourceTest
		void assumptionFailureInMethodSourceFactoryMethod(String test) {
		}

		static List<String> assumptionFailureInMethodSourceFactoryMethod() {
			Assumptions.assumeFalse(true, "nothing to test");
			return null;
		}

	}

	static class UnusedArgumentsTestCase {

		@ParameterizedTest
		@ArgumentsSource(TwoUnusedStringArgumentsProvider.class)
		void testWithTwoUnusedStringArgumentsProvider(String argument) {
			fail(argument);
		}

		@ParameterizedTest
		@CsvSource({ "foo, unused1", "bar, unused2" })
		void testWithCsvSourceContainingUnusedArguments(String argument) {
			fail(argument);
		}

		@ParameterizedTest
		@CsvFileSource(resources = "two-column.csv")
		void testWithCsvFileSourceContainingUnusedArguments(String argument) {
			fail(argument);
		}

		@ParameterizedTest
		@MethodSource("unusedArgumentsProviderMethod")
		void testWithMethodSourceProvidingUnusedArguments(String argument) {
			fail(argument);
		}

		static Stream<Arguments> unusedArgumentsProviderMethod() {
			return Stream.of(arguments("foo", "unused1"), arguments("bar", "unused2"));
		}

	}

	static class LifecycleTestCase {

		private static final List<String> lifecycleEvents = new ArrayList<>();
		private static final Set<String> testMethods = new LinkedHashSet<>();

		public LifecycleTestCase(TestInfo testInfo) {
			lifecycleEvents.add("constructor:" + testInfo.getDisplayName());
		}

		@BeforeAll
		static void beforeAll(TestInfo testInfo) {
			lifecycleEvents.add("beforeAll:" + testInfo.getDisplayName());
		}

		@AfterAll
		static void afterAll(TestInfo testInfo) {
			lifecycleEvents.add("afterAll:" + testInfo.getDisplayName());
		}

		@BeforeEach
		void beforeEach(TestInfo testInfo) {
			lifecycleEvents.add("beforeEach:" + testInfo.getDisplayName());
		}

		@AfterEach
		void afterEach(TestInfo testInfo) {
			lifecycleEvents.add("afterEach:" + testInfo.getDisplayName());
		}

		@ParameterizedTest
		@MethodSource("providerMethod")
		void test1(String argument, TestInfo testInfo) {
			performTest(argument, testInfo);
		}

		@ParameterizedTest
		@MethodSource("providerMethod")
		void test2(String argument, TestInfo testInfo) {
			performTest(argument, testInfo);
		}

		private void performTest(String argument, TestInfo testInfo) {
			String testMethod = testInfo.getTestMethod().get().getName();
			testMethods.add(testMethod);
			lifecycleEvents.add(testMethod + ":" + testInfo.getDisplayName());
			fail(argument);
		}

		static Stream<String> providerMethod() {
			lifecycleEvents.add("providerMethod");
			return Stream.of("foo", "bar");
		}

	}

	private static class TwoSingleStringArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(arguments("foo"), arguments("bar"));
		}
	}

	private static class TwoUnusedStringArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(arguments("foo", "unused1"), arguments("bar", "unused2"));
		}
	}

	private static class StringLengthConverter implements ArgumentConverter {

		@Override
		public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
			return String.valueOf(source).length();
		}
	}

	private static class ErroneousConverter implements ArgumentConverter {

		@Override
		public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
			throw new ArgumentConversionException("something went horribly wrong");
		}
	}

	static class Book {

		private final String title;

		private Book(String title) {
			this.title = title;
		}

		static Book factory(String title) {
			return new Book(title);
		}
	}

}
