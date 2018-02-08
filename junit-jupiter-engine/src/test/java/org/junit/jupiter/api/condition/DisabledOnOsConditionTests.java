/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
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
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * Unit tests for {@link DisabledOnOsCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledOnOsIntegrationTests}.
 *
 * @since 5.1
 */
class DisabledOnOsConditionTests extends AbstractExecutionConditionTests {

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
		assertDisabledOnCurrentOs();
	}

	/**
	 * @see DisabledOnOsIntegrationTests#linux()
	 */
	@Test
	void linux() {
		evaluateCondition();
		if (onLinux()) {
			assertDisabledOnCurrentOs();
		}
		else {
			assertEnabledOnCurrentOs();
		}
	}

	/**
	 * @see DisabledOnOsIntegrationTests#macOs()
	 */
	@Test
	void macOs() {
		evaluateCondition();
		if (onMac()) {
			assertDisabledOnCurrentOs();
		}
		else {
			assertEnabledOnCurrentOs();
		}
	}

	/**
	 * @see DisabledOnOsIntegrationTests#macOsWithComposedAnnotation()
	 */
	@Test
	void macOsWithComposedAnnotation() {
		evaluateCondition();
		if (onMac()) {
			assertDisabledOnCurrentOs();
		}
		else {
			assertEnabledOnCurrentOs();
		}
	}

	/**
	 * @see DisabledOnOsIntegrationTests#windows()
	 */
	@Test
	void windows() {
		evaluateCondition();
		if (onWindows()) {
			assertDisabledOnCurrentOs();
		}
		else {
			assertEnabledOnCurrentOs();
		}
	}

	/**
	 * @see DisabledOnOsIntegrationTests#solaris()
	 */
	@Test
	void solaris() {
		evaluateCondition();
		if (onSolaris()) {
			assertDisabledOnCurrentOs();
		}
		else {
			assertEnabledOnCurrentOs();
		}
	}

	/**
	 * @see DisabledOnOsIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		if (onLinux() || onMac() || onSolaris() || onWindows()) {
			assertEnabledOnCurrentOs();
		}
		else {
			assertDisabledOnCurrentOs();
		}
	}

	private void assertEnabledOnCurrentOs() {
		assertEnabled();
		assertReasonContains("Enabled on operating system: " + System.getProperty("os.name"));
	}

	private void assertDisabledOnCurrentOs() {
		assertDisabled();
		assertReasonContains("Disabled on operating system: " + System.getProperty("os.name"));
	}

}
