/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava10;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava11;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava12;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava13;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava14;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava15;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava16;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava17;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava18;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava19;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava20;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava21;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava22;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava23;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava8;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava9;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link DisabledForJreRange}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledForJreRangeIntegrationTests}.
 *
 * @since 5.6
 */
class DisabledForJreRangeConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new DisabledForJreRangeCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return DisabledForJreRangeIntegrationTests.class;
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@DisabledForJreRange is not present");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#defaultValues()
	 */
	@Test
	void defaultValues() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessageContaining("You must declare a non-default value for min or max in @DisabledForJreRange");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#java17()
	 */
	@Test
	void java17() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava17());
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#java18to19()
	 */
	@Test
	void java18to19() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava18() || onJava19());
		assertCustomDisabledReasonIs("Disabled on some JRE");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#javaMax18()
	 */
	@Test
	void javaMax18() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13()
				|| onJava14() || onJava15() || onJava16() || onJava17() || onJava18());
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#javaMin18()
	 */
	@Test
	void javaMin18() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(!(onJava17()));
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(!(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13()
				|| onJava14() || onJava15() || onJava16() || onJava17() || onJava18() || onJava19() || onJava20()
				|| onJava21() || onJava22() || onJava23()));
	}

	private void assertDisabledOnCurrentJreIf(boolean condition) {
		if (condition) {
			assertDisabled();
			assertReasonContains("Disabled on JRE version: " + System.getProperty("java.version"));
		}
		else {
			assertEnabled();
			assertReasonContains("Enabled on JRE version: " + System.getProperty("java.version"));
		}
	}

}
