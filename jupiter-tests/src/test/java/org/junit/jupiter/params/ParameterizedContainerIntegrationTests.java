/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.ArgumentCountValidationMode.NONE;
import static org.junit.jupiter.params.ArgumentCountValidationMode.STRICT;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.INDEX_PLACEHOLDER;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.dynamicTestRegistered;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.EventConditions.uniqueId;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.descriptor.ContainerTemplateInvocationTestDescriptor;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.SimpleArgumentsAggregator;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.TypedArgumentConverter;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ParameterDeclarations;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Event;

@SuppressWarnings("ALL")
public class ParameterizedContainerIntegrationTests extends AbstractJupiterTestEngineTests {

	@ParameterizedTest
	@ValueSource(classes = { ConstructorInjectionTestCase.class, RecordTestCase.class,
			ParameterizedContainerDataClassTestCase.class, FieldInjectionTestCase.class,
			RecordWithBuiltInConverterTestCase.class, RecordWithRegisteredConversionTestCase.class,
			FieldInjectionWithRegisteredConversionTestCase.class, RecordWithBuiltInAggregatorTestCase.class,
			FieldInjectionWithBuiltInAggregatorTestCase.class, RecordWithCustomAggregatorTestCase.class,
			FieldInjectionWithCustomAggregatorTestCase.class })
	void injectsParametersIntoContainerTemplate(Class<?> containerTemplateClass) {

		var results = executeTestsForClass(containerTemplateClass);

		String parameterNamePrefix = containerTemplateClass.getSimpleName().contains("Aggregator") ? "" : "value=";

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(containerTemplateClass), started()), //

			event(dynamicTestRegistered("#1"), displayName("[1] %s-1".formatted(parameterNamePrefix))), //
			event(container("#1"), started()), //
			event(dynamicTestRegistered("test1")), //
			event(dynamicTestRegistered("test2")), //
			event(test("test1"), started()), //
			event(test("test1"), finishedSuccessfully()), //
			event(test("test2"), started()), //
			event(test("test2"), finishedSuccessfully()), //
			event(container("#1"), finishedSuccessfully()), //

			event(dynamicTestRegistered("#2"), displayName("[2] %s1".formatted(parameterNamePrefix))), //
			event(container("#2"), started()), //
			event(dynamicTestRegistered("test1")), //
			event(dynamicTestRegistered("test2")), //
			event(test("test1"), started()), //
			event(test("test1"), finishedWithFailure(message(it -> it.contains("negative")))), //
			event(test("test2"), started()), //
			event(test("test2"), finishedWithFailure(message(it -> it.contains("negative")))), //
			event(container("#2"), finishedSuccessfully()), //

			event(container(containerTemplateClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@ParameterizedTest
	@ValueSource(classes = { //NullAndEmptySourceConstructorInjectionTestCase.class,
			NullAndEmptySourceConstructorFieldInjectionTestCase.class })
	void supportsNullAndEmptySource(Class<?> containerTemplateClass) {

		var results = executeTestsForClass(containerTemplateClass);

		results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
		assertThat(invocationDisplayNames(results)) //
				.containsExactly("[1] value=null", "[2] value=");
	}

	@ParameterizedTest
	@ValueSource(classes = { CsvFileSourceConstructorInjectionTestCase.class,
			CsvFileSourceFieldInjectionTestCase.class })
	void supportsCsvFileSource(Class<?> containerTemplateClass) {

		var results = executeTestsForClass(containerTemplateClass);

		results.allEvents().assertStatistics(stats -> stats.started(10).succeeded(10));
		assertThat(invocationDisplayNames(results)) //
				.containsExactly("[1] name=foo, value=1", "[2] name=bar, value=2", "[3] name=baz, value=3",
					"[4] name=qux, value=4");
	}

	@ParameterizedTest
	@ValueSource(classes = { SingleEnumSourceConstructorInjectionTestCase.class,
			SingleEnumSourceFieldInjectionTestCase.class })
	void supportsSingleEnumSource(Class<?> containerTemplateClass) {

		var results = executeTestsForClass(containerTemplateClass);

		results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
		assertThat(invocationDisplayNames(results)) //
				.containsExactly("[1] value=FOO");
	}

	@ParameterizedTest
	@ValueSource(classes = { RepeatedEnumSourceConstructorInjectionTestCase.class,
			RepeatedEnumSourceFieldInjectionTestCase.class })
	void supportsRepeatedEnumSource(Class<?> containerTemplateClass) {

		var results = executeTestsForClass(containerTemplateClass);

		results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
		assertThat(invocationDisplayNames(results)) //
				.containsExactly("[1] value=FOO", "[2] value=BAR");
	}

	@ParameterizedTest
	@ValueSource(classes = { MethodSourceConstructorInjectionTestCase.class, MethodSourceFieldInjectionTestCase.class })
	void supportsMethodSource(Class<?> containerTemplateClass) {

		var results = executeTestsForClass(containerTemplateClass);

		results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
		assertThat(invocationDisplayNames(results)) //
				.containsExactly("[1] value=foo", "[2] value=bar");
	}

	@Test
	void doesNotSupportDerivingMethodName() {

		var results = executeTestsForClass(MethodSourceWithoutMethodNameTestCase.class);

		results.allEvents().failed() //
				.assertEventsMatchExactly(finishedWithFailure(
					message("You must specify a method name when using @MethodSource with @ContainerTemplate")));
	}

	@ParameterizedTest
	@ValueSource(classes = { FieldSourceConstructorInjectionTestCase.class, FieldSourceFieldInjectionTestCase.class })
	void supportsFieldSource(Class<?> containerTemplateClass) {

		var results = executeTestsForClass(containerTemplateClass);

		results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
		assertThat(invocationDisplayNames(results)) //
				.containsExactly("[1] value=foo", "[2] value=bar");
	}

	@Test
	void doesNotSupportDerivingFieldName() {

		var results = executeTestsForClass(FieldSourceWithoutFieldNameTestCase.class);

		results.allEvents().failed() //
				.assertEventsMatchExactly(finishedWithFailure(
					message("You must specify a field name when using @FieldSource with @ContainerTemplate")));
	}

	@ParameterizedTest
	@ValueSource(classes = { ArgumentsSourceConstructorInjectionTestCase.class,
			ArgumentsSourceFieldInjectionTestCase.class })
	void supportsArgumentsSource(Class<?> containerTemplateClass) {

		var results = executeTestsForClass(containerTemplateClass);

		results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
		assertThat(invocationDisplayNames(results)) //
				.containsExactly("[1] value=foo", "[2] value=bar");
	}

	@Test
	void supportsCustomNamePatterns() {

		var results = executeTestsForClass(CustomNamePatternTestCase.class);

		results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
		assertThat(invocationDisplayNames(results)) //
				.containsExactly("1 | TesT | 1, foo | set", "2 | TesT | 2, bar | number=2, name=bar");
	}

	@Test
	void closesAutoCloseableArguments() {
		AutoCloseableArgument.closeCounter = 0;

		var results = executeTestsForClass(AutoCloseableArgumentTestCase.class);

		results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
		assertThat(AutoCloseableArgument.closeCounter).isEqualTo(2);
	}

	@Test
	void doesNotCloseAutoCloseableArgumentsWhenDisabled() {
		AutoCloseableArgument.closeCounter = 0;

		var results = executeTestsForClass(AutoCloseableArgumentWithDisabledCleanupTestCase.class);

		results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
		assertThat(AutoCloseableArgument.closeCounter).isEqualTo(0);
	}

	@Test
	void failsOnStrictArgumentCountValidationMode() {
		var results = executeTestsForClass(StrictArgumentCountValidationModeTestCase.class);

		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(finishedWithFailure(message(String.format(
					"Configuration error: the @ParameterizedContainer has 1 parameter(s) but there were 2 argument(s) provided.%nNote: the provided arguments are [foo, unused]")))));
	}

	@ParameterizedTest
	@ValueSource(classes = { NoneArgumentCountValidationModeTestCase.class,
			DefaultArgumentCountValidationModeTestCase.class })
	void doesNotFailOnNoneOrDefaultArgumentCountValidationMode(Class<?> containerTemplateClass) {

		var results = executeTestsForClass(containerTemplateClass);

		results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
	}

	@Test
	void failsOnStrictArgumentCountValidationModeSetViaConfigurationParameter() {
		var results = executeTests(request -> request //
				.selectors(selectClass(DefaultArgumentCountValidationModeTestCase.class)).configurationParameter(
					ArgumentCountValidator.ARGUMENT_COUNT_VALIDATION_KEY, STRICT.name()));

		results.allEvents().assertThatEvents() //
				.haveExactly(1, event(finishedWithFailure(message(String.format(
					"Configuration error: the @ParameterizedContainer has 1 parameter(s) but there were 2 argument(s) provided.%nNote: the provided arguments are [foo, unused]")))));
	}

	@Test
	void failsWhenInvocationIsRequiredButNoArgumentSetsAreProvided() {
		var results = executeTestsForClass(ForbiddenZeroInvocationsTestCase.class);

		results.containerEvents().assertThatEvents() //
				.haveExactly(1, event(finishedWithFailure(message(
					"Configuration error: You must configure at least one set of arguments for this @ParameterizedContainer"))));
	}

	@Test
	void doesNotFailWhenInvocationIsNotRequiredAndNoArgumentSetsAreProvided() {
		var results = executeTestsForClass(AllowedZeroInvocationsTestCase.class);

		results.allEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
	}

	@Test
	void failsWhenNoArgumentsSourceIsDeclared() {
		var results = executeTestsForClass(NoArgumentSourceTestCase.class);

		results.containerEvents().assertThatEvents() //
				.haveExactly(1, event(finishedWithFailure(message(
					"Configuration error: You must configure at least one arguments source for this @ParameterizedContainer"))));
	}

	@ParameterizedTest
	@ValueSource(classes = { NestedFieldInjectionTestCase.class, NestedConstructorInjectionTestCase.class })
	void supportsNestedParameterizedContainers(Class<?> containerTemplateClass) {

		var results = executeTestsForClass(containerTemplateClass);

		results.containerEvents().assertStatistics(stats -> stats.started(14).succeeded(14));
		results.testEvents().assertStatistics(stats -> stats.started(8).succeeded(8));
		assertThat(invocationDisplayNames(results)) //
				.containsExactly( //
					"[1] number=1", "[1] text=foo", "[2] text=bar", //
					"[2] number=2", "[1] text=foo", "[2] text=bar" //
				);
	}

	@Test
	void supportsMultipleAggregatorFields() {

		var results = executeTestsForClass(MultiAggregatorFieldInjectionTestCase.class);

		results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
	}

	// -------------------------------------------------------------------

	private static Stream<String> invocationDisplayNames(EngineExecutionResults results) {
		return results.containerEvents() //
				.started() //
				.filter(uniqueId(lastSegmentType(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE))::matches) //
				.map(Event::getTestDescriptor) //
				.map(TestDescriptor::getDisplayName);
	}

	private static Condition<UniqueId> lastSegmentType(String segmentType) {
		return new Condition<>(it -> segmentType.equals(it.getLastSegment().getType()), "last segment type is '%s'",
			segmentType);
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@ValueSource(ints = { -1, 1 })
	static class ConstructorInjectionTestCase {

		private int value;
		private final TestInfo testInfo;

		public ConstructorInjectionTestCase(int value, TestInfo testInfo) {
			this.value = value;
			this.testInfo = testInfo;
		}

		@Test
		void test1() {
			assertEquals("test1()", testInfo.getDisplayName());
			assertTrue(value < 0, "negative");
			value *= -1;
		}

		@Test
		void test2() {
			assertEquals("test2()", testInfo.getDisplayName());
			assertTrue(value < 0, "negative");
			value *= -1;
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@ValueSource(ints = { -1, 1 })
	record RecordTestCase(int value, TestInfo testInfo) {

		@Test
		void test1() {
			assertEquals("test1()", testInfo.getDisplayName());
			assertTrue(value < 0, "negative");
		}

		@Test
		void test2() {
			assertEquals("test2()", testInfo.getDisplayName());
			assertTrue(value < 0, "negative");
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@ValueSource(ints = { -1, 1 })
	static class FieldInjectionTestCase {

		@Parameter
		private int value;

		@Test
		void test1() {
			assertTrue(value < 0, "negative");
			value *= -1;
		}

		@Test
		void test2() {
			assertTrue(value < 0, "negative");
			value *= -1;
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@CsvSource({ "-1", "1" })
	record RecordWithBuiltInConverterTestCase(int value) {

		@Test
		void test1() {
			assertTrue(value < 0, "negative");
		}

		@Test
		void test2() {
			assertTrue(value < 0, "negative");
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@ValueSource(ints = { -1, 1 })
	record RecordWithRegisteredConversionTestCase(@ConvertWith(CustomIntegerToStringConverter.class) String value) {

		@Test
		void test1() {
			assertTrue(value.startsWith("minus"), "negative");
		}

		@Test
		void test2() {
			assertTrue(value.startsWith("minus"), "negative");
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@ValueSource(ints = { -1, 1 })
	static class FieldInjectionWithRegisteredConversionTestCase {

		@Parameter
		@ConvertWith(CustomIntegerToStringConverter.class)
		private String value;

		@Test
		void test1() {
			assertTrue(value.startsWith("minus"), "negative");
		}

		@Test
		void test2() {
			assertTrue(value.startsWith("minus"), "negative");
		}
	}

	private static class CustomIntegerToStringConverter extends TypedArgumentConverter<Integer, String> {

		CustomIntegerToStringConverter() {
			super(Integer.class, String.class);
		}

		@Override
		protected String convert(Integer source) throws ArgumentConversionException {
			return switch (source) {
				case -1 -> "minus one";
				case +1 -> "plus one";
				default -> throw new IllegalArgumentException("Unsupported value: " + source);
			};
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@ValueSource(ints = { -1, 1 })
	record RecordWithBuiltInAggregatorTestCase(ArgumentsAccessor accessor) {

		@Test
		void test1() {
			assertTrue(accessor.getInteger(0) < 0, "negative");
		}

		@Test
		void test2() {
			assertTrue(accessor.getInteger(0) < 0, "negative");
		}

	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@ValueSource(ints = { -1, 1 })
	static class FieldInjectionWithBuiltInAggregatorTestCase {

		@Parameter
		private ArgumentsAccessor accessor;

		@Test
		void test1() {
			assertTrue(accessor.getInteger(0) < 0, "negative");
		}

		@Test
		void test2() {
			assertTrue(accessor.getInteger(0) < 0, "negative");
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@ValueSource(ints = { -1, 1 })
	record RecordWithCustomAggregatorTestCase(@AggregateWith(TimesTwo.class) int value) {

		@Test
		void test1() {
			assertTrue(value <= -2, "negative");
		}

		@Test
		void test2() {
			assertTrue(value <= -2, "negative");
		}

	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@ValueSource(ints = { -1, 1 })
	static class FieldInjectionWithCustomAggregatorTestCase {

		@Parameter
		@AggregateWith(TimesTwo.class)
		private int value;

		@Test
		void test1() {
			assertTrue(value <= -2, "negative");
		}

		@Test
		void test2() {
			assertTrue(value <= -2, "negative");
		}

	}

	private static class TimesTwo extends SimpleArgumentsAggregator {

		@Override
		protected Object aggregateArguments(ArgumentsAccessor accessor, Class<?> targetType,
				AnnotatedElementContext context, int parameterIndex) throws ArgumentsAggregationException {

			return accessor.getInteger(0) * 2;
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@NullAndEmptySource
	record NullAndEmptySourceConstructorInjectionTestCase(String value) {
		@Test
		void test() {
			assertTrue(StringUtils.isBlank(value));
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedContainer
	@NullAndEmptySource
	static class NullAndEmptySourceConstructorFieldInjectionTestCase {

		@Parameter
		String value;

		@Test
		void test() {
			assertTrue(StringUtils.isBlank(value));
		}
	}

	@ParameterizedContainer
	@CsvFileSource(resources = "two-column.csv")
	record CsvFileSourceConstructorInjectionTestCase(String name, int value) {
		@Test
		void test() {
			assertNotNull(name);
			assertTrue(value > 0 && value < 5);
		}
	}

	@ParameterizedContainer
	@CsvFileSource(resources = "two-column.csv")
	static class CsvFileSourceFieldInjectionTestCase {

		@Parameter(0)
		String name;

		@Parameter(1)
		int value;

		@Test
		void test() {
			assertNotNull(name);
			assertTrue(value > 0 && value < 5);
		}
	}

	@ParameterizedContainer
	@EnumSource
	record SingleEnumSourceConstructorInjectionTestCase(EnumOne value) {
		@Test
		void test() {
			assertEquals(EnumOne.FOO, value);
		}
	}

	@ParameterizedContainer
	@EnumSource
	static class SingleEnumSourceFieldInjectionTestCase {

		@Parameter
		EnumOne value;

		@Test
		void test() {
			assertEquals(EnumOne.FOO, value);
		}
	}

	@ParameterizedContainer
	@EnumSource(EnumOne.class)
	@EnumSource(EnumTwo.class)
	record RepeatedEnumSourceConstructorInjectionTestCase(Object value) {
		@Test
		void test() {
			assertTrue(value == EnumOne.FOO || value == EnumTwo.BAR);
		}
	}

	@ParameterizedContainer
	@EnumSource(EnumOne.class)
	@EnumSource(EnumTwo.class)
	static class RepeatedEnumSourceFieldInjectionTestCase {

		@Parameter
		Object value;

		@Test
		void test() {
			assertTrue(value == EnumOne.FOO || value == EnumTwo.BAR);
		}
	}

	private enum EnumOne {
		FOO
	}

	private enum EnumTwo {
		BAR
	}

	@ParameterizedContainer
	@MethodSource("parameters")
	record MethodSourceConstructorInjectionTestCase(String value) {

		static Stream<String> parameters() {
			return Stream.of("foo", "bar");
		}

		@Test
		void test() {
			assertTrue(value == "foo" || value == "bar");
		}
	}

	@ParameterizedContainer
	@MethodSource("parameters")
	static class MethodSourceFieldInjectionTestCase {

		static Stream<String> parameters() {
			return Stream.of("foo", "bar");
		}

		@Parameter
		String value;

		@Test
		void test() {
			assertTrue(value == "foo" || value == "bar");
		}
	}

	@ParameterizedContainer
	@MethodSource
	record MethodSourceWithoutMethodNameTestCase(String value) {

		@Test
		void test() {
			fail("should not be executed");
		}
	}

	@ParameterizedContainer
	@FieldSource("parameters")
	record FieldSourceConstructorInjectionTestCase(String value) {

		static final List<String> parameters = List.of("foo", "bar");

		@Test
		void test() {
			assertTrue(value == "foo" || value == "bar");
		}
	}

	@ParameterizedContainer
	@FieldSource("parameters")
	static class FieldSourceFieldInjectionTestCase {

		static final List<String> parameters = List.of("foo", "bar");

		@Parameter
		String value;

		@Test
		void test() {
			assertTrue(value == "foo" || value == "bar");
		}
	}

	@ParameterizedContainer
	@FieldSource
	record FieldSourceWithoutFieldNameTestCase(String value) {

		@Test
		void test() {
			fail("should not be executed");
		}
	}

	@ParameterizedContainer
	@ArgumentsSource(CustomArgumentsProvider.class)
	record ArgumentsSourceConstructorInjectionTestCase(String value) {
		@Test
		void test() {
			assertTrue(value == "foo" || value == "bar");
		}
	}

	@ParameterizedContainer
	@ArgumentsSource(CustomArgumentsProvider.class)
	static class ArgumentsSourceFieldInjectionTestCase {

		@Parameter
		String value;

		@Test
		void test() {
			assertTrue(value == "foo" || value == "bar");
		}
	}

	static class CustomArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context)
				throws Exception {
			return Stream.of("foo", "bar").map(Arguments::of);
		}
	}

	@ParameterizedContainer(name = INDEX_PLACEHOLDER + " | " //
			+ DISPLAY_NAME_PLACEHOLDER + " | " //
			+ ARGUMENTS_PLACEHOLDER + " | " //
			+ ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER)
	@MethodSource("arguments")
	@DisplayName("TesT")
	record CustomNamePatternTestCase(int number, String name) {

		static Stream<Arguments> arguments() {
			return Stream.of(argumentSet("set", 1, "foo"), Arguments.of(2, "bar"));
		}

		@Test
		void test() {
			assertTrue(number > 0);
			assertFalse(name.isBlank());
		}
	}

	@ParameterizedContainer
	@ArgumentsSource(AutoCloseableArgumentProvider.class)
	record AutoCloseableArgumentTestCase(AutoCloseableArgument argument) {
		@Test
		void test() {
			assertNotNull(argument);
			assertEquals(0, AutoCloseableArgument.closeCounter);
		}
	}

	@ParameterizedContainer(autoCloseArguments = false)
	@ArgumentsSource(AutoCloseableArgumentProvider.class)
	record AutoCloseableArgumentWithDisabledCleanupTestCase(AutoCloseableArgument argument) {
		@Test
		void test() {
			assertNotNull(argument);
			assertEquals(0, AutoCloseableArgument.closeCounter);
		}
	}

	private static class AutoCloseableArgumentProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters,
				ExtensionContext context) {
			return Stream.of(arguments(new AutoCloseableArgument(), Named.of("unused", new AutoCloseableArgument())));
		}
	}

	static class AutoCloseableArgument implements AutoCloseable {

		static int closeCounter = 0;

		@Override
		public void close() {
			closeCounter++;
		}
	}

	@ParameterizedContainer(argumentCountValidation = STRICT)
	@CsvSource("foo, unused")
	record StrictArgumentCountValidationModeTestCase(String value) {
		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedContainer(argumentCountValidation = NONE)
	@CsvSource("foo, unused")
	record NoneArgumentCountValidationModeTestCase(String value) {
		@Test
		void test() {
			assertEquals("foo", value);
		}
	}

	@ParameterizedContainer
	@CsvSource("foo, unused")
	record DefaultArgumentCountValidationModeTestCase(String value) {
		@Test
		void test() {
			assertEquals("foo", value);
		}
	}

	@ParameterizedContainer
	@MethodSource("org.junit.jupiter.params.ParameterizedContainerIntegrationTests#zeroArguments")
	record ForbiddenZeroInvocationsTestCase(String value) {
		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedContainer(allowZeroInvocations = true)
	@MethodSource("org.junit.jupiter.params.ParameterizedContainerIntegrationTests#zeroArguments")
	record AllowedZeroInvocationsTestCase(String value) {
		@Test
		void test() {
			fail("should not be called");
		}
	}

	static Stream<Arguments> zeroArguments() {
		return Stream.empty();
	}

	@ParameterizedContainer
	record NoArgumentSourceTestCase(String value) {
		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedContainer
	@ValueSource(ints = { 1, 2 })
	static class NestedFieldInjectionTestCase {

		@Parameter
		int number;

		@Nested
		@ParameterizedContainer
		@ValueSource(strings = { "foo", "bar" })
		class InnerTestCase {

			@Parameter
			String text;

			@ParameterizedTest
			@ValueSource(booleans = { true, false })
			void test(boolean flag) {
				assertTrue(number >= 0);
				assertTrue(List.of("foo", "bar").contains(text));
			}
		}
	}

	@ParameterizedContainer
	@ValueSource(ints = { 1, 2 })
	record NestedConstructorInjectionTestCase(int number) {

		@Nested
		@ParameterizedContainer
		@ValueSource(strings = { "foo", "bar" })
		class InnerTestCase {

			final String text;

			InnerTestCase(String text) {
				this.text = text;
			}

			@ParameterizedTest
			@ValueSource(booleans = { true, false })
			void test(boolean flag) {
				assertTrue(number >= 0);
				assertTrue(List.of("foo", "bar").contains(text));
			}
		}
	}

	@ParameterizedContainer
	@CsvSource({ "foo, 1", "bar, 2" })
	static class MultiAggregatorFieldInjectionTestCase {

		@Parameter
		ArgumentsAccessor accessor1;

		@Parameter
		ArgumentsAccessor accessor2;

		@Parameter(0)
		String text;

		@Parameter(1)
		int number;

		@Test
		void test() {
			assertEquals(2, accessor1.size());
			assertEquals(2, accessor2.size());
			assertEquals(accessor1.getInvocationIndex(), accessor2.getInvocationIndex());
			assertEquals(text, accessor1.getString(0));
			assertEquals(text, accessor2.getString(0));
			assertEquals(number, accessor1.getInteger(1));
			assertEquals(number, accessor2.getInteger(1));
		}
	}

}
