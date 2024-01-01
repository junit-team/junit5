/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.appendTestTemplateInvocationSegment;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForTestTemplateMethod;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectIteration;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.testkit.engine.EventConditions.abortedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.PreconditionViolationException;
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

	@ParameterizedTest
	@CsvSource(textBlock = """
			apple,   True
			banana,  true
			lemon,   false
			kumquat, FALSE
			""")
	void sweetFruit(String fruit, Boolean sweet) {
		switch (fruit) {
			case "apple" -> assertThat(sweet).isTrue();
			case "banana" -> assertThat(sweet).isTrue();
			case "lemon" -> assertThat(sweet).isFalse();
			case "kumquat" -> assertThat(sweet).isFalse();
			default -> fail("Unexpected fruit : " + fruit);
		}
	}

	@ParameterizedTest
	@CsvSource(nullValues = "null", textBlock = """
			apple,   True
			banana,  true
			lemon,   false
			kumquat, null
			""")
	void sweetFruitWithNullableBoolean(String fruit, Boolean sweet) {
		switch (fruit) {
			case "apple" -> assertThat(sweet).isTrue();
			case "banana" -> assertThat(sweet).isTrue();
			case "lemon" -> assertThat(sweet).isFalse();
			case "kumquat" -> assertThat(sweet).isNull(); // null --> null
			default -> fail("Unexpected fruit : " + fruit);
		}
	}

	@ParameterizedTest
	@CsvSource(quoteCharacter = '"', textBlock = """


			# This is a comment preceded by multiple opening blank lines.
			apple,         1
			banana,        2
			# This is a comment pointing out that the next line contains multiple explicit newlines in quoted text.
			"lemon  \s


			\s  lime",         0xF1
			# The next line is a blank line in the middle of the CSV rows.

			strawberry,    700_000
			# This is a comment followed by 2 closing blank line.

			""")
	void executesLinesFromTextBlock(String fruit, int rank) {
		switch (fruit) {
			case "apple" -> assertThat(rank).isEqualTo(1);
			case "banana" -> assertThat(rank).isEqualTo(2);
			case "lemon   \n\n\n   lime" -> assertThat(rank).isEqualTo(241);
			case "strawberry" -> assertThat(rank).isEqualTo(700_000);
			default -> fail("Unexpected fruit : " + fruit);
		}
	}

	@ParameterizedTest(name = "[{index}] {arguments}")
	@CsvSource(delimiter = '|', useHeadersInDisplayName = true, nullValues = "NIL", textBlock = """
			#---------------------------------
			  FRUIT  | RANK
			#---------------------------------
			  apple  | 1
			#---------------------------------
			  banana | 2
			#---------------------------------
			  cherry | 3.14159265358979323846
			#---------------------------------
			         | 0
			#---------------------------------
			  NIL    | 0
			#---------------------------------
			""")
	void executesLinesFromTextBlockUsingTableFormatAndHeadersAndNullValues(String fruit, double rank,
			TestInfo testInfo) {
		assertFruitTable(fruit, rank, testInfo);
	}

	@ParameterizedTest(name = "[{index}] {arguments}")
	@CsvFileSource(resources = "two-column-with-headers.csv", delimiter = '|', useHeadersInDisplayName = true, nullValues = "NIL")
	void executesLinesFromClasspathResourceUsingTableFormatAndHeadersAndNullValues(String fruit, double rank,
			TestInfo testInfo) {
		assertFruitTable(fruit, rank, testInfo);
	}

	private void assertFruitTable(String fruit, double rank, TestInfo testInfo) {
		String displayName = testInfo.getDisplayName();

		if (fruit == null) {
			assertThat(rank).isEqualTo(0);
			assertThat(displayName).matches("\\[(4|5)\\] FRUIT = null, RANK = 0");
			return;
		}

		switch (fruit) {
			case "apple" -> {
				assertThat(rank).isEqualTo(1);
				assertThat(displayName).isEqualTo("[1] FRUIT = apple, RANK = 1");
			}
			case "banana" -> {
				assertThat(rank).isEqualTo(2);
				assertThat(displayName).isEqualTo("[2] FRUIT = banana, RANK = 2");
			}
			case "cherry" -> {
				assertThat(rank).isCloseTo(Math.PI, within(0.0));
				assertThat(displayName).isEqualTo("[3] FRUIT = cherry, RANK = 3.14159265358979323846");
			}
			default -> fail("Unexpected fruit : " + fruit);
		}
	}

	@Test
	void executesWithSingleArgumentsProviderWithMultipleInvocations() {
		var results = execute("testWithTwoSingleStringArgumentsProvider", String.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("[1] argument=foo"), finishedWithFailure(message("foo")))) //
				.haveExactly(1, event(test(), displayName("[2] argument=bar"), finishedWithFailure(message("bar"))));
	}

	@Test
	void executesWithCsvSource() {
		var results = execute("testWithCsvSource", String.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("[1] argument=foo"), finishedWithFailure(message("foo")))) //
				.haveExactly(1, event(test(), displayName("[2] argument=bar"), finishedWithFailure(message("bar"))));
	}

	@Test
	void executesWithCustomName() {
		var results = execute("testWithCustomName", String.class, int.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("foo and 23"), finishedWithFailure(message("foo, 23")))) //
				.haveExactly(1, event(test(), displayName("bar and 42"), finishedWithFailure(message("bar, 42"))));
	}

	/**
	 * @since 5.2
	 */
	@Test
	void executesWithPrimitiveWideningConversion() {
		var results = execute("testWithPrimitiveWideningConversion", double.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("[1] num=1"), finishedWithFailure(message("num: 1.0")))) //
				.haveExactly(1, event(test(), displayName("[2] num=2"), finishedWithFailure(message("num: 2.0"))));
	}

	/**
	 * @since 5.1
	 */
	@Test
	void executesWithImplicitGenericConverter() {
		var results = execute("testWithImplicitGenericConverter", Book.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("[1] book=book 1"), finishedWithFailure(message("book 1")))) //
				.haveExactly(1, event(test(), displayName("[2] book=book 2"), finishedWithFailure(message("book 2"))));
	}

	@Test
	void legacyReportingNames() {
		var results = execute("testWithCustomName", String.class, int.class);

		// @formatter:off
		var legacyReportingNames = results.testEvents().dynamicallyRegistered()
				.map(Event::getTestDescriptor)
				.map(TestDescriptor::getLegacyReportingName);
		// @formatter:on
		assertThat(legacyReportingNames).containsExactly("testWithCustomName(String, int)[1]",
			"testWithCustomName(String, int)[2]");
	}

	@Test
	void executesWithExplicitConverter() {
		var results = execute("testWithExplicitConverter", int.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), displayName("[1] length=O"), finishedWithFailure(message("length: 1")))) //
				.haveExactly(1,
					event(test(), displayName("[2] length=XXX"), finishedWithFailure(message("length: 3"))));
	}

	@Test
	void executesWithAggregator() {
		var results = execute("testWithAggregator", String.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1,
					event(test(), displayName("[1] ab, cd"), finishedWithFailure(message("concatenation: abcd")))) //
				.haveExactly(1,
					event(test(), displayName("[2] ef, gh"), finishedWithFailure(message("concatenation: efgh"))));
	}

	@Test
	void executesWithIgnoreLeadingAndTrailingSetToFalseForCsvSource() {
		var results = execute("testWithIgnoreLeadingAndTrailingWhitespaceSetToFalseForCsvSource", String.class,
			String.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), finishedWithFailure(message("arguments: ' ab ', ' cd'")))) //
				.haveExactly(1, event(test(), finishedWithFailure(message("arguments: 'ef ', 'gh'"))));
	}

	@Test
	void executesWithIgnoreLeadingAndTrailingSetToTrueForCsvSource() {
		var results = execute("testWithIgnoreLeadingAndTrailingWhitespaceSetToTrueForCsvSource", String.class,
			String.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), finishedWithFailure(message("arguments: 'ab', 'cd'")))) //
				.haveExactly(1, event(test(), finishedWithFailure(message("arguments: 'ef', 'gh'"))));
	}

	@Test
	void executesWithIgnoreLeadingAndTrailingSetToFalseForCsvFileSource() {
		var results = execute("testWithIgnoreLeadingAndTrailingWhitespaceSetToFalseForCsvFileSource", String.class,
			String.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), finishedWithFailure(message("arguments: ' ab ', ' cd'")))) //
				.haveExactly(1, event(test(), finishedWithFailure(message("arguments: 'ef ', 'gh'"))));
	}

	@Test
	void executesWithIgnoreLeadingAndTrailingSetToTrueForCsvFileSource() {
		var results = execute("testWithIgnoreLeadingAndTrailingWhitespaceSetToTrueForCsvFileSource", String.class,
			String.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), finishedWithFailure(message("arguments: 'ab', 'cd'")))) //
				.haveExactly(1, event(test(), finishedWithFailure(message("arguments: 'ef', 'gh'"))));
	}

	@Test
	void failsContainerOnEmptyName() {
		var results = execute("testWithEmptyName", String.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(container(), displayName("testWithEmptyName(String)"), //
					finishedWithFailure(message(msg -> msg.contains("must be declared with a non-empty name")))));
	}

	@Test
	void reportsExceptionForErroneousConverter() {
		var results = execute("testWithErroneousConverter", Object.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), finishedWithFailure(instanceOf(ParameterResolutionException.class), //
					message("Error converting parameter at index 0: something went horribly wrong"))));
	}

	@Test
	void executesLifecycleMethods() {
		// reset static collections
		LifecycleTestCase.lifecycleEvents.clear();
		LifecycleTestCase.testMethods.clear();

		var results = execute(selectClass(LifecycleTestCase.class));
		results.allEvents().assertThatEvents() //
				.haveExactly(1,
					event(test("test1"), displayName("[1] argument=foo"), finishedWithFailure(message("foo")))) //
				.haveExactly(1,
					event(test("test1"), displayName("[2] argument=bar"), finishedWithFailure(message("bar"))));

		List<String> testMethods = new ArrayList<>(LifecycleTestCase.testMethods);

		// @formatter:off
		assertThat(LifecycleTestCase.lifecycleEvents).containsExactly(
			"beforeAll:ParameterizedTestIntegrationTests$LifecycleTestCase",
				"providerMethod",
					"constructor:ParameterizedTestIntegrationTests$LifecycleTestCase",
					"beforeEach:[1] argument=foo",
						testMethods.get(0) + ":[1] argument=foo",
					"afterEach:[1] argument=foo",
					"constructor:ParameterizedTestIntegrationTests$LifecycleTestCase",
					"beforeEach:[2] argument=bar",
						testMethods.get(0) + ":[2] argument=bar",
					"afterEach:[2] argument=bar",
				"providerMethod",
					"constructor:ParameterizedTestIntegrationTests$LifecycleTestCase",
					"beforeEach:[1] argument=foo",
						testMethods.get(1) + ":[1] argument=foo",
					"afterEach:[1] argument=foo",
					"constructor:ParameterizedTestIntegrationTests$LifecycleTestCase",
					"beforeEach:[2] argument=bar",
						testMethods.get(1) + ":[2] argument=bar",
					"afterEach:[2] argument=bar",
			"afterAll:ParameterizedTestIntegrationTests$LifecycleTestCase");
		// @formatter:on
	}

	@Test
	void truncatesArgumentsThatExceedMaxLength() {
		var results = EngineTestKit.engine(new JupiterTestEngine()) //
				.configurationParameter(ParameterizedTestExtension.ARGUMENT_MAX_LENGTH_KEY, "2") //
				.selectors(selectMethod(TestCase.class, "testWithCsvSource", String.class.getName())) //
				.execute();
		results.testEvents().assertThatEvents() //
				.haveExactly(1, event(displayName("[1] argument=f…"), started())) //
				.haveExactly(1, event(displayName("[2] argument=b…"), started()));
	}

	@Test
	void displayNamePatternFromConfiguration() {
		var results = EngineTestKit.engine(new JupiterTestEngine()) //
				.configurationParameter(ParameterizedTestExtension.DISPLAY_NAME_PATTERN_KEY, "{index}") //
				.selectors(selectMethod(TestCase.class, "testWithCsvSource", String.class.getName())) //
				.execute();
		results.testEvents().assertThatEvents() //
				.haveExactly(1, event(displayName("1"), started())) //
				.haveExactly(1, event(displayName("2"), started()));
	}

	private EngineExecutionResults execute(DiscoverySelector... selectors) {
		return EngineTestKit.engine(new JupiterTestEngine()).selectors(selectors).execute();
	}

	private EngineExecutionResults execute(Class<?> testClass, String methodName, Class<?>... methodParameterTypes) {
		return execute(selectMethod(testClass, methodName, ClassUtils.nullSafeToString(methodParameterTypes)));
	}

	private EngineExecutionResults execute(String methodName, Class<?>... methodParameterTypes) {
		return execute(TestCase.class, methodName, methodParameterTypes);
	}

	/**
	 * @since 5.4
	 */
	@Nested
	class NullSourceIntegrationTests {

		@Test
		void executesWithNullSourceForString() {
			var results = execute("testWithNullSourceForString", String.class);
			results.testEvents().failed().assertEventsMatchExactly(
				event(test(), displayName("[1] argument=null"), finishedWithFailure(message("null"))));
		}

		@Test
		void executesWithNullSourceForStringAndTestInfo() {
			var results = execute("testWithNullSourceForStringAndTestInfo", String.class, TestInfo.class);
			results.testEvents().failed().assertEventsMatchExactly(
				event(test(), displayName("[1] argument=null"), finishedWithFailure(message("null"))));
		}

		@Test
		void executesWithNullSourceForNumber() {
			var results = execute("testWithNullSourceForNumber", Number.class);
			results.testEvents().failed().assertEventsMatchExactly(
				event(test(), displayName("[1] argument=null"), finishedWithFailure(message("null"))));
		}

		@Test
		void failsWithNullSourceWithZeroFormalParameters() {
			var methodName = "testWithNullSourceWithZeroFormalParameters";
			execute(methodName).containerEvents().failed().assertEventsMatchExactly(//
				event(container(methodName), //
					finishedWithFailure(//
						instanceOf(PreconditionViolationException.class), //
						message(msg -> msg.matches(
							"@NullSource cannot provide a null argument to method .+: the method does not declare any formal parameters.")))));
		}

		@Test
		void failsWithNullSourceForPrimitive() {
			var results = execute("testWithNullSourceForPrimitive", int.class);
			results.testEvents().failed().assertEventsMatchExactly(event(test(), displayName("[1] argument=null"),
				finishedWithFailure(instanceOf(ParameterResolutionException.class), message(
					"Error converting parameter at index 0: Cannot convert null to primitive value of type int"))));
		}

		private EngineExecutionResults execute(String methodName, Class<?>... methodParameterTypes) {
			return ParameterizedTestIntegrationTests.this.execute(NullSourceTestCase.class, methodName,
				methodParameterTypes);
		}

	}

	/**
	 * @since 5.4
	 */
	@Nested
	class EmptySourceIntegrationTests {

		@Test
		void executesWithEmptySourceForString() {
			var results = execute("testWithEmptySourceForString", String.class);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=")));
		}

		@Test
		void executesWithEmptySourceForStringAndTestInfo() {
			var results = execute("testWithEmptySourceForStringAndTestInfo", String.class, TestInfo.class);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=")));
		}

		/**
		 * @since 5.10
		 */
		@Test
		void executesWithEmptySourceForCollection() {
			var results = execute("testWithEmptySourceForCollection", Collection.class);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=[]")));
		}

		@Test
		void executesWithEmptySourceForList() {
			var results = execute("testWithEmptySourceForList", List.class);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=[]")));
		}

		/**
		 * @since 5.10
		 */
		@ParameterizedTest(name = "{1}")
		@CsvSource(textBlock = """
				testWithEmptySourceForArrayList,  java.util.ArrayList
				testWithEmptySourceForLinkedList, java.util.LinkedList
				""")
		void executesWithEmptySourceForListSubtype(String methodName, Class<?> parameterType) {
			var results = execute(methodName, parameterType);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=[]")));
		}

		@Test
		void executesWithEmptySourceForSet() {
			var results = execute("testWithEmptySourceForSet", Set.class);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=[]")));
		}

		/**
		 * @since 5.10
		 */
		@ParameterizedTest(name = "{1}")
		@CsvSource(textBlock = """
				testWithEmptySourceForSortedSet,     java.util.SortedSet
				testWithEmptySourceForNavigableSet,  java.util.NavigableSet
				testWithEmptySourceForHashSet,       java.util.HashSet
				testWithEmptySourceForTreeSet,       java.util.TreeSet
				testWithEmptySourceForLinkedHashSet, java.util.LinkedHashSet
				""")
		void executesWithEmptySourceForSetSubtype(String methodName, Class<?> parameterType) {
			var results = execute(methodName, parameterType);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=[]")));
		}

		@Test
		void executesWithEmptySourceForMap() {
			var results = execute("testWithEmptySourceForMap", Map.class);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument={}")));
		}

		/**
		 * @since 5.10
		 */
		@ParameterizedTest(name = "{1}")
		@CsvSource(textBlock = """
				testWithEmptySourceForSortedMap,     java.util.SortedMap
				testWithEmptySourceForNavigableMap,  java.util.NavigableMap
				testWithEmptySourceForHashMap,       java.util.HashMap
				testWithEmptySourceForTreeMap,       java.util.TreeMap
				testWithEmptySourceForLinkedHashMap, java.util.LinkedHashMap
				""")
		void executesWithEmptySourceForMapSubtype(String methodName, Class<?> parameterType) {
			var results = execute(methodName, parameterType);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument={}")));
		}

		@Test
		void executesWithEmptySourceForOneDimensionalPrimitiveArray() {
			var results = execute("testWithEmptySourceForOneDimensionalPrimitiveArray", int[].class);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=[]")));
		}

		@Test
		void executesWithEmptySourceForOneDimensionalStringArray() {
			var results = execute("testWithEmptySourceForOneDimensionalStringArray", String[].class);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=[]")));
		}

		@Test
		void executesWithEmptySourceForTwoDimensionalPrimitiveArray() {
			var results = execute("testWithEmptySourceForTwoDimensionalPrimitiveArray", int[][].class);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=[]")));
		}

		@Test
		void executesWithEmptySourceForTwoDimensionalStringArray() {
			var results = execute("testWithEmptySourceForTwoDimensionalStringArray", String[][].class);
			results.testEvents().succeeded().assertEventsMatchExactly(event(test(), displayName("[1] argument=[]")));
		}

		@Test
		void failsWithEmptySourceWithZeroFormalParameters() {
			var methodName = "testWithEmptySourceWithZeroFormalParameters";
			execute(methodName).containerEvents().failed().assertEventsMatchExactly(//
				event(container(methodName), //
					finishedWithFailure(//
						instanceOf(PreconditionViolationException.class), //
						message(msg -> msg.matches(
							"@EmptySource cannot provide an empty argument to method .+: the method does not declare any formal parameters.")))));
		}

		@ParameterizedTest(name = "{1}")
		@CsvSource(textBlock = """
				testWithEmptySourceForPrimitive,                int
				testWithEmptySourceForUnsupportedReferenceType, java.lang.Integer
				""")
		void failsWithEmptySourceForUnsupportedType(String methodName, Class<?> parameterType) {
			execute(methodName, parameterType).containerEvents().failed().assertEventsMatchExactly(//
				event(container(methodName), //
					finishedWithFailure(//
						instanceOf(PreconditionViolationException.class), //
						message(msg -> msg.matches("@EmptySource cannot provide an empty argument to method .+: \\["
								+ parameterType.getName() + "] is not a supported type."))//
					)));
		}

		private EngineExecutionResults execute(String methodName, Class<?>... methodParameterTypes) {
			return ParameterizedTestIntegrationTests.this.execute(EmptySourceTestCase.class, methodName,
				methodParameterTypes);
		}

	}

	/**
	 * @since 5.4
	 */
	@Nested
	class NullAndEmptySourceIntegrationTests {

		@Test
		void executesWithNullAndEmptySourceForString() {
			var results = execute("testWithNullAndEmptySourceForString", String.class);
			assertNullAndEmptyString(results);
		}

		@Test
		void executesWithNullAndEmptySourceForStringAndTestInfo() {
			var results = execute("testWithNullAndEmptySourceForStringAndTestInfo", String.class, TestInfo.class);
			assertNullAndEmptyString(results);
		}

		@Test
		void executesWithNullAndEmptySourceForList() {
			var results = execute("testWithNullAndEmptySourceForList", List.class);
			assertNullAndEmpty(results);
		}

		@Test
		void executesWithNullAndEmptySourceForArrayList() {
			var results = execute("testWithNullAndEmptySourceForArrayList", ArrayList.class);
			assertNullAndEmpty(results);
		}

		@Test
		void executesWithNullAndEmptySourceForOneDimensionalPrimitiveArray() {
			var results = execute("testWithNullAndEmptySourceForOneDimensionalPrimitiveArray", int[].class);
			assertNullAndEmpty(results);
		}

		@Test
		void executesWithNullAndEmptySourceForTwoDimensionalStringArray() {
			var results = execute("testWithNullAndEmptySourceForTwoDimensionalStringArray", String[][].class);
			assertNullAndEmpty(results);
		}

		private EngineExecutionResults execute(String methodName, Class<?>... methodParameterTypes) {
			return ParameterizedTestIntegrationTests.this.execute(NullAndEmptySourceTestCase.class, methodName,
				methodParameterTypes);
		}

		private void assertNullAndEmptyString(EngineExecutionResults results) {
			results.testEvents().succeeded().assertEventsMatchExactly(//
				event(test(), displayName("[1] argument=null")), //
				event(test(), displayName("[2] argument="))//
			);
		}

		private void assertNullAndEmpty(EngineExecutionResults results) {
			results.testEvents().succeeded().assertEventsMatchExactly(//
				event(test(), displayName("[1] argument=null")), //
				event(test(), displayName("[2] argument=[]"))//
			);
		}

	}

	@Nested
	class MethodSourceIntegrationTests {

		@Test
		void emptyMethodSource() {
			execute("emptyMethodSource", String.class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("empty method source"))));
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void oneDimensionalPrimitiveArray() {
			execute("oneDimensionalPrimitiveArray", int.class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("1"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("2"))));
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void twoDimensionalPrimitiveArray() {
			execute("twoDimensionalPrimitiveArray", int[].class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("[1, 2]"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("[3, 4]"))));
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void oneDimensionalObjectArray() {
			execute("oneDimensionalObjectArray", Object.class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("one"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("2"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("three"))));
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void oneDimensionalStringArray() {
			execute("oneDimensionalStringArray", String.class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("one"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("two"))));
		}

		@Test
		void twoDimensionalObjectArray() {
			execute("twoDimensionalObjectArray", String.class, int.class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("one:2"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("three:4"))));
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void twoDimensionalStringArray() {
			execute("twoDimensionalStringArray", String.class, String.class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("one:two"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("three:four"))));
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void streamOfOneDimensionalPrimitiveArrays() {
			execute("streamOfOneDimensionalPrimitiveArrays", int[].class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("[1, 2]"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("[3, 4]"))));
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void streamOfTwoDimensionalPrimitiveArrays() {
			assertStreamOfTwoDimensionalPrimitiveArrays("streamOfTwoDimensionalPrimitiveArrays");
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void streamOfTwoDimensionalPrimitiveArraysWrappedInObjectArrays() {
			assertStreamOfTwoDimensionalPrimitiveArrays("streamOfTwoDimensionalPrimitiveArraysWrappedInObjectArrays");
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void streamOfTwoDimensionalPrimitiveArraysWrappedInArguments() {
			assertStreamOfTwoDimensionalPrimitiveArrays("streamOfTwoDimensionalPrimitiveArraysWrappedInArguments");
		}

		private void assertStreamOfTwoDimensionalPrimitiveArrays(String methodName) {
			execute(methodName, int[][].class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("[[1, 2], [3, 4]]"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("[[5, 6], [7, 8]]"))));
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void streamOfOneDimensionalObjectArrays() {
			execute("streamOfOneDimensionalObjectArrays", String.class, int.class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("one:2"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("three:4"))));
		}

		/**
		 * @since 5.3.2
		 */
		@Test
		void streamOfTwoDimensionalObjectArrays() {
			execute("streamOfTwoDimensionalObjectArrays", Object[][].class).testEvents().assertThatEvents()//
					.haveExactly(1, event(test(), finishedWithFailure(message("[[one, 2], [three, 4]]"))))//
					.haveExactly(1, event(test(), finishedWithFailure(message("[[five, 6], [seven, 8]]"))));
		}

		@Test
		void reportsContainerWithAssumptionFailureInMethodSourceAsAborted() {
			execute("assumptionFailureInMethodSourceFactoryMethod", String.class).allEvents().assertThatEvents() //
					.haveExactly(1, event(container("test-template:assumptionFailureInMethodSourceFactoryMethod"), //
						abortedWithReason(instanceOf(TestAbortedException.class),
							message("Assumption failed: nothing to test"))));
		}

		@Test
		void namedParameters() {
			execute("namedParameters", String.class).allEvents().assertThatEvents() //
					.haveAtLeast(1,
						event(test(), displayName("cool name"), finishedWithFailure(message("parameter value")))) //
					.haveAtLeast(1,
						event(test(), displayName("default name"), finishedWithFailure(message("default name"))));
		}

		@Test
		void nameParametersAlias() {
			execute("namedParametersAlias", String.class).allEvents().assertThatEvents() //
					.haveAtLeast(1,
						event(test(), displayName("cool name"), finishedWithFailure(message("parameter value")))) //
					.haveAtLeast(1,
						event(test(), displayName("default name"), finishedWithFailure(message("default name"))));
		}

		/**
		 * @since 5.9.1
		 * @see https://github.com/junit-team/junit5/issues/3001
		 */
		@Test
		void duplicateMethodNames() {
			// It is sufficient to assert that 8 tests started and finished, because
			// without the fix for #3001 the 4 parameterized tests would fail. In
			// other words, we're not really testing the support for @RepeatedTest
			// and @TestFactory, but their presence also contributes to the bug
			// reported in #3001.
			ParameterizedTestIntegrationTests.this.execute(selectClass(DuplicateMethodNamesMethodSourceTestCase.class))//
					.testEvents()//
					.assertStatistics(stats -> stats.started(8).failed(0).finished(8));
		}

		private EngineExecutionResults execute(String methodName, Class<?>... methodParameterTypes) {
			return ParameterizedTestIntegrationTests.this.execute(MethodSourceTestCase.class, methodName,
				methodParameterTypes);
		}

	}

	@Nested
	class UnusedArgumentsIntegrationTests {

		@Test
		void executesWithArgumentsSourceProvidingUnusedArguments() {
			var results = execute("testWithTwoUnusedStringArgumentsProvider", String.class);
			results.allEvents().assertThatEvents() //
					.haveExactly(1, event(test(), displayName("[1] argument=foo"), finishedWithFailure(message("foo")))) //
					.haveExactly(1,
						event(test(), displayName("[2] argument=bar"), finishedWithFailure(message("bar"))));
		}

		@Test
		void executesWithCsvSourceContainingUnusedArguments() {
			var results = execute("testWithCsvSourceContainingUnusedArguments", String.class);
			results.allEvents().assertThatEvents() //
					.haveExactly(1, event(test(), displayName("[1] argument=foo"), finishedWithFailure(message("foo")))) //
					.haveExactly(1,
						event(test(), displayName("[2] argument=bar"), finishedWithFailure(message("bar"))));
		}

		@Test
		void executesWithCsvFileSourceContainingUnusedArguments() {
			var results = execute("testWithCsvFileSourceContainingUnusedArguments", String.class);
			results.allEvents().assertThatEvents() //
					.haveExactly(1, event(test(), displayName("[1] argument=foo"), finishedWithFailure(message("foo")))) //
					.haveExactly(1,
						event(test(), displayName("[2] argument=bar"), finishedWithFailure(message("bar"))));
		}

		@Test
		void executesWithMethodSourceProvidingUnusedArguments() {
			var results = execute("testWithMethodSourceProvidingUnusedArguments", String.class);
			results.allEvents().assertThatEvents() //
					.haveExactly(1, event(test(), displayName("[1] argument=foo"), finishedWithFailure(message("foo")))) //
					.haveExactly(1,
						event(test(), displayName("[2] argument=bar"), finishedWithFailure(message("bar"))));
		}

		private EngineExecutionResults execute(String methodName, Class<?>... methodParameterTypes) {
			return ParameterizedTestIntegrationTests.this.execute(UnusedArgumentsTestCase.class, methodName,
				methodParameterTypes);
		}

	}

	@Test
	void closeAutoCloseableArgumentsAfterTest() {
		var results = execute("testWithAutoCloseableArgument", AutoCloseableArgument.class);
		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(test(), finishedSuccessfully()));

		assertTrue(AutoCloseableArgument.isClosed);
	}

	@Test
	void executesTwoIterationsBasedOnIterationAndUniqueIdSelector() {
		var methodId = uniqueIdForTestTemplateMethod(TestCase.class, "testWithThreeIterations(int)");
		var results = execute(selectUniqueId(appendTestTemplateInvocationSegment(methodId, 3)),
			selectIteration(selectMethod(TestCase.class, "testWithThreeIterations", "int"), 1));

		results.allEvents().assertThatEvents() //
				.haveExactly(2, event(test(), finishedWithFailure())) //
				.haveExactly(1, event(test(), displayName("[2] argument=3"), finishedWithFailure())) //
				.haveExactly(1, event(test(), displayName("[3] argument=5"), finishedWithFailure()));
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

		@ParameterizedTest
		@CsvSource({ "ab, cd", "ef, gh" })
		void testWithAggregator(@AggregateWith(StringAggregator.class) String concatenation) {
			fail("concatenation: " + concatenation);
		}

		@ParameterizedTest
		@CsvSource(value = { " ab , cd", "ef ,gh" }, ignoreLeadingAndTrailingWhitespace = false)
		void testWithIgnoreLeadingAndTrailingWhitespaceSetToFalseForCsvSource(String argument1, String argument2) {
			fail("arguments: '" + argument1 + "', '" + argument2 + "'");
		}

		@ParameterizedTest
		@CsvSource(value = { " ab , cd", "ef ,gh" }, ignoreLeadingAndTrailingWhitespace = true)
		void testWithIgnoreLeadingAndTrailingWhitespaceSetToTrueForCsvSource(String argument1, String argument2) {
			fail("arguments: '" + argument1 + "', '" + argument2 + "'");
		}

		@ParameterizedTest
		@CsvFileSource(resources = "/leading-trailing-spaces.csv", ignoreLeadingAndTrailingWhitespace = false)
		void testWithIgnoreLeadingAndTrailingWhitespaceSetToFalseForCsvFileSource(String argument1, String argument2) {
			fail("arguments: '" + argument1 + "', '" + argument2 + "'");
		}

		@ParameterizedTest
		@CsvFileSource(resources = "/leading-trailing-spaces.csv", ignoreLeadingAndTrailingWhitespace = true)
		void testWithIgnoreLeadingAndTrailingWhitespaceSetToTrueForCsvFileSource(String argument1, String argument2) {
			fail("arguments: '" + argument1 + "', '" + argument2 + "'");
		}

		@ParameterizedTest
		@ArgumentsSource(AutoCloseableArgumentProvider.class)
		void testWithAutoCloseableArgument(AutoCloseableArgument autoCloseable) {
			assertFalse(AutoCloseableArgument.isClosed);
		}

		@ParameterizedTest
		@ValueSource(ints = { 2, 3, 5 })
		void testWithThreeIterations(int argument) {
			fail("argument: " + argument);
		}
	}

	static class NullSourceTestCase {

		@ParameterizedTest
		@NullSource
		void testWithNullSourceForString(String argument) {
			fail(String.valueOf(argument));
		}

		@ParameterizedTest
		@NullSource
		void testWithNullSourceForStringAndTestInfo(String argument, TestInfo testInfo) {
			assertThat(testInfo).isNotNull();
			fail(String.valueOf(argument));
		}

		@ParameterizedTest
		@NullSource
		void testWithNullSourceForNumber(Number argument) {
			fail(String.valueOf(argument));
		}

		@ParameterizedTest
		@NullSource
		void testWithNullSourceWithZeroFormalParameters() {
			fail("should not have been executed");
		}

		@ParameterizedTest
		@NullSource
		void testWithNullSourceForPrimitive(int argument) {
			fail("should not have been executed");
		}

	}

	static class EmptySourceTestCase {

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForString(String argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForStringAndTestInfo(String argument, TestInfo testInfo) {
			assertThat(argument).isEmpty();
			assertThat(testInfo).isNotNull();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForCollection(Collection<?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForList(List<?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForArrayList(ArrayList<?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForLinkedList(LinkedList<?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForSet(Set<?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForSortedSet(SortedSet<?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForNavigableSet(NavigableSet<?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForHashSet(HashSet<?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForTreeSet(TreeSet<?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForLinkedHashSet(LinkedHashSet<?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForMap(Map<?, ?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForSortedMap(SortedMap<?, ?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForNavigableMap(NavigableMap<?, ?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForHashMap(HashMap<?, ?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForTreeMap(TreeMap<?, ?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForLinkedHashMap(LinkedHashMap<?, ?> argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForOneDimensionalPrimitiveArray(int[] argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForOneDimensionalStringArray(String[] argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForTwoDimensionalPrimitiveArray(int[][] argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForTwoDimensionalStringArray(String[][] argument) {
			assertThat(argument).isEmpty();
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceWithZeroFormalParameters() {
			fail("should not have been executed");
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForPrimitive(int argument) {
			fail("should not have been executed");
		}

		@ParameterizedTest
		@EmptySource
		void testWithEmptySourceForUnsupportedReferenceType(Integer argument) {
			fail("should not have been executed");
		}

	}

	static class NullAndEmptySourceTestCase {

		@ParameterizedTest
		@NullAndEmptySource
		void testWithNullAndEmptySourceForString(String argument) {
			assertTrue(argument == null || argument.isEmpty());
		}

		@ParameterizedTest
		@NullAndEmptySource
		void testWithNullAndEmptySourceForStringAndTestInfo(String argument, TestInfo testInfo) {
			assertTrue(argument == null || argument.isEmpty());
			assertThat(testInfo).isNotNull();
		}

		@ParameterizedTest
		@NullAndEmptySource
		void testWithNullAndEmptySourceForList(List<?> argument) {
			assertTrue(argument == null || argument.isEmpty());
		}

		@ParameterizedTest
		@NullAndEmptySource
		void testWithNullAndEmptySourceForArrayList(ArrayList<?> argument) {
			assertTrue(argument == null || argument.isEmpty());
		}

		@ParameterizedTest
		@NullAndEmptySource
		void testWithNullAndEmptySourceForOneDimensionalPrimitiveArray(int[] argument) {
			assertTrue(argument == null || argument.length == 0);
		}

		@ParameterizedTest
		@NullAndEmptySource
		void testWithNullAndEmptySourceForTwoDimensionalStringArray(String[][] argument) {
			assertTrue(argument == null || argument.length == 0);
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

		@MethodSourceTest
		@Order(13)
		void namedParameters(String string) {
			fail(string);
		}

		@MethodSourceTest
		@Order(14)
		void namedParametersAlias(String string) {
			fail(string);
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

		static Stream<Arguments> namedParameters() {
			return Stream.of(arguments(Named.of("cool name", "parameter value")), arguments("default name"));
		}

		static Stream<Arguments> namedParametersAlias() {
			return Stream.of(arguments(named("cool name", "parameter value")), arguments("default name"));
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

	/**
	 * @since 5.9.1
	 * @see https://github.com/junit-team/junit5/issues/3001
	 */
	static class DuplicateMethodNamesMethodSourceTestCase {

		@ParameterizedTest
		@MethodSource
		void test(String value) {
			test(1, value);
		}

		@ParameterizedTest
		@MethodSource("test")
		void anotherTest(String value) {
			assertTrue(test(value, 1));
		}

		@RepeatedTest(2)
		void test(TestReporter testReporter) {
			assertNotNull(testReporter);
		}

		@TestFactory
		Stream<DynamicTest> test(TestInfo testInfo) {
			return test().map(value -> dynamicTest(value, () -> test(1, value)));
		}

		// neither a test method nor a factory method.
		// intentionally void.
		private void test(int expectedLength, String value) {
			assertEquals(expectedLength, value.length());
		}

		// neither a test method nor a factory method.
		// intentionally non-void and also not convertible to a Stream.
		private boolean test(String value, int expectedLength) {
			return (value.length() == expectedLength);
		}

		// legitimate factory method.
		private static Stream<String> test() {
			return Stream.of("a", "b");
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
			var testMethod = testInfo.getTestMethod().orElseThrow().getName();
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

	private static class StringAggregator implements ArgumentsAggregator {

		@Override
		public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
				throws ArgumentsAggregationException {
			return accessor.getString(0) + accessor.getString(1);
		}
	}

	private static class ErroneousConverter implements ArgumentConverter {

		@Override
		public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
			throw new ArgumentConversionException("something went horribly wrong");
		}
	}

	private static class AutoCloseableArgumentProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(arguments(new AutoCloseableArgument()));
		}
	}

	static class AutoCloseableArgument implements AutoCloseable {

		static boolean isClosed = false;

		@Override
		public void close() {
			isClosed = true;
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
