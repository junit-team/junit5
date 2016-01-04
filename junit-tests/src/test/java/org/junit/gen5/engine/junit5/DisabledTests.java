/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.Optional;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Disabled;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.TestExecutionCondition;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TrackingEngineExecutionListener;

/**
 * Integration tests that verify support for {@link Disabled @Disabled} and
 * custom {@link TestExecutionCondition Conditions} in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class DisabledTests extends AbstractJUnit5TestEngineTests {

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
	public void executeTestsWithDisabledTestClass() {
		TestPlanSpecification spec = build(forClass(DisabledTestClassTestCase.class));
		TrackingEngineExecutionListener listener = executeTests(spec, 2);

		assertEquals(1, listener.containerSkippedCount.get(), "# container skipped");
		assertEquals(0, listener.testStartedCount.get(), "# tests started");
	}

	@Test
	public void executeTestsWithDisabledTestMethods() {
		TestPlanSpecification spec = build(forClass(DisabledTestMethodsTestCase.class));
		TrackingEngineExecutionListener listener = executeTests(spec, 6);

		assertEquals(2, listener.testStartedCount.get(), "# tests started");
		assertEquals(2, listener.testSucceededCount.get(), "# tests succeeded");
		assertEquals(3, listener.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, listener.testAbortedCount.get(), "# tests aborted");
		assertEquals(0, listener.testFailedCount.get(), "# tests failed");
	}

	// -------------------------------------------------------------------

	@Disabled
	private static class DisabledTestClassTestCase {

		@Test
		void disabledTest() {
			fail("this should be @Disabled");
		}
	}

	private static class DisabledTestMethodsTestCase {

		@Test
		void enabledTest() {
		}

		@Test
		@Disabled
		void disabledTest() {
			fail("this should be @Disabled");
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

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ExtendWith(SystemPropertyCondition.class)
	@interface SystemProperty {

		String key();

		String value();
	}

	private static class SystemPropertyCondition implements TestExecutionCondition {

		@Override
		public ConditionEvaluationResult evaluate(TestExtensionContext context) {
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
