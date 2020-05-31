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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onLinux;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onMac;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onSolaris;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onWindows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link DisabledOnOsCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledOnOsIntegrationTests}.
 *
 * @since 5.1
 */
class DisabledOnOsConditionTests extends AbstractExecutionConditionTests {

	private static final String OS_NAME = System.getProperty("os.name");

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new DisabledOnOsCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return DisabledOnOsIntegrationTests.class;
	}

	/**
	 * @see DisabledOnOsIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@DisabledOnOs is not present");
	}

	/**
	 * @see DisabledOnOsIntegrationTests#missingOsDeclaration()
	 */
	@Test
	void missingOsDeclaration() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("You must declare at least one OS");
	}

	/**
	 * @see DisabledOnOsIntegrationTests#disabledOnEveryOs()
	 */
	@Test
	void disabledOnEveryOs() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("Disabled on operating system: " + OS_NAME + " ==> Disabled on every OS");
	}

	/**
	 * @see DisabledOnOsIntegrationTests#linux()
	 */
	@Test
	void linux() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onLinux());
	}

	/**
	 * @see DisabledOnOsIntegrationTests#macOs()
	 */
	@Test
	void macOs() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onMac());
	}

	/**
	 * @see DisabledOnOsIntegrationTests#macOsWithComposedAnnotation()
	 */
	@Test
	void macOsWithComposedAnnotation() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onMac());
	}

	/**
	 * @see DisabledOnOsIntegrationTests#windows()
	 */
	@Test
	void windows() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onWindows());
	}

	/**
	 * @see DisabledOnOsIntegrationTests#solaris()
	 */
	@Test
	void solaris() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onSolaris());
	}

	/**
	 * @see DisabledOnOsIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(!(onLinux() || onMac() || onSolaris() || onWindows()));
	}

	private void assertDisabledOnCurrentOsIf(boolean condition) {
		if (condition) {
			assertDisabled();
			assertReasonContains("Disabled on operating system: " + OS_NAME);
		}
		else {
			assertEnabled();
			assertReasonContains("Enabled on operating system: " + OS_NAME);
		}
	}

}
