/*
 * Copyright 2015-2020 the original author or authors.
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
	 * @see DisabledForJreRangeIntegrationTests#java8()
	 */
	@Test
	void java8() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava8());
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#java8to11()
	 */
	@Test
	void java8to11() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava8() || onJava9() || onJava10() || onJava11());
		assertCustomDisabledReasonIs("Disabled on some JRE");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#java9to12()
	 */
	@Test
	void java9to12() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava9() || onJava10() || onJava11() || onJava12());
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#javaMax12()
	 */
	@Test
	void javaMax12() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava8() || onJava9() || onJava10() || onJava11() || onJava12());
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#javaMin10()
	 */
	@Test
	void javaMin10() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(!(onJava8() || onJava9()));
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(!(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13()
				|| onJava14() || onJava15()));
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
