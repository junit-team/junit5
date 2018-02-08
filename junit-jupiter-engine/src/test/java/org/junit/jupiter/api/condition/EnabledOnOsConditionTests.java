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
 * Unit tests for {@link EnabledOnOsCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link EnabledOnOsIntegrationTests}.
 *
 * @since 5.1
 */
class EnabledOnOsConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new EnabledOnOsCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return EnabledOnOsIntegrationTests.class;
	}

	/**
	 * @see EnabledOnOsIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@EnabledOnOs is not present");
	}

	/**
	 * @see EnabledOnOsIntegrationTests#missingOsDeclaration()
	 */
	@Test
	void missingOsDeclaration() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("You must declare at least one OS");
	}

	/**
	 * @see EnabledOnOsIntegrationTests#enabledOnEveryOs()
	 */
	@Test
	void enabledOnEveryOs() {
		evaluateCondition();
		assertEnabledOnCurrentOs();
	}

	/**
	 * @see EnabledOnOsIntegrationTests#linux()
	 */
	@Test
	void linux() {
		evaluateCondition();
		if (onLinux()) {
			assertEnabledOnCurrentOs();
		}
		else {
			assertDisabledOnCurrentOs();
		}
	}

	/**
	 * @see EnabledOnOsIntegrationTests#macOs()
	 */
	@Test
	void macOs() {
		evaluateCondition();
		if (onMac()) {
			assertEnabledOnCurrentOs();
		}
		else {
			assertDisabledOnCurrentOs();
		}
	}

	/**
	 * @see EnabledOnOsIntegrationTests#macOsWithComposedAnnotation()
	 */
	@Test
	void macOsWithComposedAnnotation() {
		evaluateCondition();
		if (onMac()) {
			assertEnabledOnCurrentOs();
		}
		else {
			assertDisabledOnCurrentOs();
		}
	}

	/**
	 * @see EnabledOnOsIntegrationTests#windows()
	 */
	@Test
	void windows() {
		evaluateCondition();
		if (onWindows()) {
			assertEnabledOnCurrentOs();
		}
		else {
			assertDisabledOnCurrentOs();
		}
	}

	/**
	 * @see EnabledOnOsIntegrationTests#solaris()
	 */
	@Test
	void solaris() {
		evaluateCondition();
		if (onSolaris()) {
			assertEnabledOnCurrentOs();
		}
		else {
			assertDisabledOnCurrentOs();
		}
	}

	/**
	 * @see EnabledOnOsIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		if (onLinux() || onMac() || onSolaris() || onWindows()) {
			assertDisabledOnCurrentOs();
		}
		else {
			assertEnabledOnCurrentOs();
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
