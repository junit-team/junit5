/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.Optional;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.ContainerExecutionCondition;
import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.TestExecutionCondition;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.engine.junit5.AbstractJUnit5TestEngineTests;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * Integration tests that verify support for {@link TestExecutionCondition} and
 * {@link ContainerExecutionCondition} extension points in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class ExecutionConditionTests extends AbstractJUnit5TestEngineTests {

	private static final String FOO = "DisabledTests.foo";
	private static final String BAR = "DisabledTests.bar";
	private static final String BOGUS = "DisabledTests.bogus";

	@BeforeEach
	public void setUp() {
		System.setProperty(FOO, BAR);
	}

	@AfterEach
	public void tearDown() {
		System.clearProperty(FOO);
	}

	@Test
	public void executionConditionWorksOnContainer() {
		TestDiscoveryRequest request = request().select(
			forClass(TestCaseWithContainerExecutionCondition.class)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1L, eventRecorder.getContainerSkippedCount(), "# container skipped");
		assertEquals(0L, eventRecorder.getTestStartedCount(), "# tests started");
	}

	@Test
	public void executionConditionWorksOnTest() {
		TestDiscoveryRequest request = request().select(forClass(TestCaseWithTestExecutionCondition.class)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2L, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2L, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(2L, eventRecorder.getTestSkippedCount(), "# tests skipped");
	}

	// -------------------------------------------------------------------

	@SystemProperty(key = FOO, value = BOGUS)
	private static class TestCaseWithContainerExecutionCondition {

		@Test
		void disabledTest() {
			fail("this should be @Disabled");
		}
	}

	private static class TestCaseWithTestExecutionCondition {

		@Test
		void enabledTest() {
		}

		@Test
		@SystemProperty(key = FOO, value = BAR)
		void systemPropertyEnabledTest() {
		}

		@Test
		@SystemProperty(key = FOO, value = BOGUS)
		void systemPropertyWithIncorrectValueTest() {
			fail("this should be disabled");
		}

		@Test
		@SystemProperty(key = BOGUS, value = "doesn't matter")
		void systemPropertyNotSetTest() {
			fail("this should be disabled");
		}

	}

	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@ExtendWith(SystemPropertyCondition.class)
	@interface SystemProperty {

		String key();

		String value();
	}

	private static class SystemPropertyCondition implements TestExecutionCondition, ContainerExecutionCondition {

		@Override
		public ConditionEvaluationResult evaluate(ContainerExtensionContext context) {
			return evaluate((ExtensionContext) context);
		}

		@Override
		public ConditionEvaluationResult evaluate(TestExtensionContext context) {
			return evaluate((ExtensionContext) context);
		}

		private ConditionEvaluationResult evaluate(ExtensionContext context) {
			Optional<SystemProperty> optional = findAnnotation(context.getElement(), SystemProperty.class);

			if (optional.isPresent()) {
				SystemProperty systemProperty = optional.get();
				String key = systemProperty.key();
				String expected = systemProperty.value();
				String actual = System.getProperty(key);

				if (!Objects.equals(expected, actual)) {
					return ConditionEvaluationResult.disabled(String.format(
						"System property [%s] has a value of [%s] instead of [%s]", key, actual, expected));
				}
			}

			return ConditionEvaluationResult.enabled("@SystemProperty is not present");
		}
	}

}
