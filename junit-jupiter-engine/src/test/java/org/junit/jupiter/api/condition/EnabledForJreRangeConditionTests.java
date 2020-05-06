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
 * Unit tests for {@link EnabledForJreRange}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link EnabledForJreRangeIntegrationTests}.
 *
 * @since 5.6
 */
class EnabledForJreRangeConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new EnabledForJreRangeCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return EnabledForJreRangeIntegrationTests.class;
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@EnabledForJreRange is not present");
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#defaultValues()
	 */
	@Test
	void defaultValues() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessageContaining("You must declare a non-default value for min or max in @EnabledForJreRange");
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#java8()
	 */
	@Test
	void java8() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava8());
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#java8to11()
	 */
	@Test
	void java8to11() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava8() || onJava9() || onJava10() || onJava11());
		assertCustomDisabledReasonIs("Disabled on some JRE");
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#java9to12()
	 */
	@Test
	void java9to12() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava9() || onJava10() || onJava11() || onJava12());
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#javaMax12()
	 */
	@Test
	void javaMax12() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava8() || onJava9() || onJava10() || onJava11() || onJava12());
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#javaMin10()
	 */
	@Test
	void javaMin10() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(!(onJava8() || onJava9()));
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(!(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13()
				|| onJava14() || onJava15()));
	}

	private void assertEnabledOnCurrentJreIf(boolean condition) {
		if (condition) {
			assertEnabled();
			assertReasonContains("Enabled on JRE version: " + System.getProperty("java.version"));
		}
		else {
			assertDisabled();
			assertReasonContains("Disabled on JRE version: " + System.getProperty("java.version"));
		}
	}

}
