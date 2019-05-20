/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.testkit.engine.Events;
import org.mockito.Mockito;

/**
 * Unit tests for {@link ScriptExecutionCondition}.
 *
 * @since 5.1
 */
@Deprecated
class ScriptExecutionConditionTests extends AbstractJupiterTestEngineTests {

	@Test
	void executeSimpleTestCases() {
		Events tests = executeTestsForClass(SimpleTestCases.class).tests();

		tests.assertStatistics(stats -> stats.started(3).skipped(1).failed(1));

		tests.failed().assertEventsMatchExactly( //
			event(test("syntaxError"), //
				finishedWithFailure(instanceOf(JUnitException.class),
					message(value -> value.contains("syntax error")))));
	}

	@Test
	@EnabledIf("true")
	@DisabledIf("false")
	void annotationDefaultValues(TestInfo info) {
		EnabledIf e = info.getTestMethod().orElseThrow(Error::new).getDeclaredAnnotation(EnabledIf.class);
		assertEquals("Nashorn", e.engine());
		assertEquals("Script `{source}` evaluated to: {result}", e.reason());
		DisabledIf d = info.getTestMethod().orElseThrow(Error::new).getDeclaredAnnotation(DisabledIf.class);
		assertEquals("Nashorn", d.engine());
		assertEquals("Script `{source}` evaluated to: {result}", d.reason());
	}

	@Test
	void throwingEvaluatorExceptionMessage() {
		String message = "Mock for message";
		ReflectiveOperationException cause = new ReflectiveOperationException("Mock for ReflectiveOperationException");
		ScriptExecutionCondition.Evaluator evaluator = new ScriptExecutionCondition.ThrowingEvaluator(message, cause);
		Exception e = assertThrows(Exception.class, () -> evaluator.evaluate(null, null));
		assertTrue(e instanceof ExtensionConfigurationException);
		assertEquals(message, e.getMessage());
	}

	@Test
	void enabledDueToAnnotatedElementNotPresent() {
		ScriptExecutionCondition condition = new ScriptExecutionCondition();
		ExtensionContext context = Mockito.mock(ExtensionContext.class);
		ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);
		assertFalse(result.isDisabled());
		assertThat(result.getReason()).contains("AnnotatedElement not present");
	}

	@Test
	void enabledDueToAnnotationNotPresent() {
		ScriptExecutionCondition condition = new ScriptExecutionCondition();
		ExtensionContext context = Mockito.mock(ExtensionContext.class);
		Optional<AnnotatedElement> optionalElement = Optional.of(ScriptExecutionConditionTests.class);
		Mockito.when(context.getElement()).thenReturn(optionalElement);
		ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);
		assertFalse(result.isDisabled());
		assertThat(result.getReason()).contains("Annotation not present");
	}

	@Test
	void throwingEvaluatorIsCreatedWhenScriptEngineIsNotAvailable() {
		String nameOfScriptEngine = "javax.script.DoesNotExist";
		String name = "org.junit.jupiter.engine.extension.ScriptExecutionEvaluator";
		ScriptExecutionCondition.Evaluator evaluator = ScriptExecutionCondition.Evaluator.forName(nameOfScriptEngine,
			name);
		Exception e = assertThrows(Exception.class, () -> evaluator.evaluate(null, null));
		assertThat(e) //
				.isInstanceOf(ExtensionConfigurationException.class) //
				.hasMessageStartingWith("Class `" + nameOfScriptEngine + "` is not loadable") //
				.hasMessageEndingWith("`--add-modules ...,java.scripting`");
	}

	@Test
	void throwingEvaluatorIsCreatedWhenDefaultEvaluatorClassNameIsIllegal() throws ReflectiveOperationException {
		String name = "illegal class name";
		ScriptExecutionCondition condition = new ScriptExecutionCondition(name);
		ExtensionContext context = Mockito.mock(ExtensionContext.class);
		AnnotatedElement element = SimpleTestCases.class.getDeclaredMethod("testIsEnabled");
		Mockito.when(context.getElement()).thenReturn(Optional.of(element));
		Exception e = assertThrows(Exception.class, () -> condition.evaluateExecutionCondition(context));
		assertThat(e) //
				.isInstanceOf(ExtensionConfigurationException.class) //
				.hasMessageStartingWith("Creating instance of class `" + name + "` failed");

	}

	static class SimpleTestCases {

		@Test
		@EnabledIf("true")
		void testIsEnabled() {
		}

		@Test
		@DisabledIf("false")
		void testIsNotDisabled() {
		}

		@Test
		@EnabledIf("syntax error")
		void syntaxErrorFails() {
			fail("test must not be executed");
		}

		@Test
		@DisabledIf("junitConfigurationParameter.get('does-not-exist') == null")
		void accessingNonExistentJUnitConfigurationParameter() {
			fail("test must not be executed");
		}
	}

}
