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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.dynamicTestRegistered;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.SimpleArgumentsAggregator;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.TypedArgumentConverter;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;

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

	@Test
	void canInjectNullAndEmptyValues() {
		var results = executeTestsForClass(NullAndEmptySourceTestCase.class);

		results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
	}

	// -------------------------------------------------------------------

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
	record NullAndEmptySourceTestCase(String value) {
		@Test
		void test() {
			assertTrue(StringUtils.isBlank(value));
		}
	}

}
