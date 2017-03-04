/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.displayName;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.support.ObjectArrayArguments;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

class ParameterizedTestIntegrationTests {

	@Test
	void executesWithSingleArgumentsProviderWithMultipleInvocations() {
		List<ExecutionEvent> executionEvents = execute(
			selectMethod(TestCase.class, "testWithTwoSingleStringArgumentsProvider", String.class.getName()));
		assertThat(executionEvents) //
				.haveExactly(1, event(test(), displayName("[1] foo"), finishedWithFailure(message("foo")))) //
				.haveExactly(1, event(test(), displayName("[2] bar"), finishedWithFailure(message("bar"))));
	}

	@Test
	void executesWithStringSource() {
		List<ExecutionEvent> executionEvents = execute(
			selectMethod(TestCase.class, "testWithStringSource", String.class.getName()));
		assertThat(executionEvents) //
				.haveExactly(1, event(test(), displayName("[1] foo"), finishedWithFailure(message("foo")))) //
				.haveExactly(1, event(test(), displayName("[2] bar"), finishedWithFailure(message("bar"))));
	}

	@Test
	void executesWithCustomName() {
		List<ExecutionEvent> executionEvents = execute(
			selectMethod(TestCase.class, "testWithCustomName", String.class.getName() + "," + Integer.TYPE.getName()));
		assertThat(executionEvents) //
				.haveExactly(1, event(test(), displayName("foo and 23"), finishedWithFailure(message("foo, 23")))) //
				.haveExactly(1, event(test(), displayName("bar and 42"), finishedWithFailure(message("bar, 42"))));
	}

	@Test
	void executesWithExplicitConverter() {
		List<ExecutionEvent> executionEvents = execute(
			selectMethod(TestCase.class, "testWithExplicitConverter", Integer.TYPE.getName()));
		assertThat(executionEvents) //
				.haveExactly(1, event(test(), displayName("[1] O"), finishedWithFailure(message("length: 1")))) //
				.haveExactly(1, event(test(), displayName("[2] XXX"), finishedWithFailure(message("length: 3"))));
	}

	private List<ExecutionEvent> execute(DiscoverySelector... selectors) {
		return ExecutionEventRecorder.execute(new JupiterTestEngine(), request().selectors(selectors).build());
	}

	static class TestCase {
		@ParameterizedTest
		@ArgumentsSource(TwoSingleStringArgumentsProvider.class)
		void testWithTwoSingleStringArgumentsProvider(String argument) {
			fail(argument);
		}

		@ParameterizedTest
		@CsvSource({ "foo", "bar" })
		void testWithStringSource(String argument) {
			fail(argument);
		}

		@ParameterizedTest(name = "{0} and {1}")
		@CsvSource({ "foo, 23", "bar, 42" })
		void testWithCustomName(String argument, int i) {
			fail(argument + ", " + i);
		}

		@ParameterizedTest
		@ValueSource(strings = { "O", "XXX" })
		void testWithExplicitConverter(@ConvertWith(StringLengthConverter.class) int length) {
			fail("length: " + length);
		}
	}

	private static class TwoSingleStringArgumentsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> arguments(ContainerExtensionContext context) throws Exception {
			return Stream.of(ObjectArrayArguments.create("foo"), ObjectArrayArguments.create("bar"));
		}
	}

	private static class StringLengthConverter implements ArgumentConverter {
		@Override
		public Object convert(Object input, ParameterContext context) throws ArgumentConversionException {
			return String.valueOf(input).length();
		}
	}
}
