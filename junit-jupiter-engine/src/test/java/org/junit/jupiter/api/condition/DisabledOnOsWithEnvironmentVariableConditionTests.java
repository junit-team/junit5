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
import static org.junit.jupiter.api.condition.DisabledIfEnvironmentVariableIntegrationTests.ENIGMA;
import static org.junit.jupiter.api.condition.DisabledIfEnvironmentVariableIntegrationTests.KEY1;
import static org.junit.jupiter.api.condition.DisabledOnOsWithEnvironmentVariableIntegrationTests.KEY2;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onLinux;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onMac;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onSolaris;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onWindows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link DisabledOnOsWithEnvironmentVariableCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledOnOsWithEnvironmentVariableConditionTests}.
 *
 * @since 5.1
 */
final class DisabledOnOsWithEnvironmentVariableConditionTests extends AbstractExecutionConditionTests {

	/**
	 * Stubbed subclass of {@link DisabledIfEnvironmentVariableCondition}.
	 */
	private ExecutionCondition condition = new DisabledOnOsWithEnvironmentVariableCondition() {

		protected String getEnvironmentVariable(String name) {
			return KEY1.equals(name) ? ENIGMA : null;
		}
	};

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return this.condition;
	}

	@Override
	protected Class<?> getTestClass() {
		return DisabledOnOsWithEnvironmentVariableIntegrationTests.class;
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains(
			"No @DisabledOnOsWithEnvironmentVariable conditions resulting in 'disabled' execution encountered");
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#missingOsDeclaration()
	 */
	@Test
	void missingOsDeclaration() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("You must declare at least one OS");
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#disabledOnEveryOs()
	 */
	@Test
	void disabledOnEveryOs() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#enabledOnLinuxWithoutEnvironmentVariablesMatches()
	 */
	@Test
	void enabledOnLinuxWithoutEnvironmentVariablesMatches() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains(
			"No @DisabledOnOsWithEnvironmentVariable conditions resulting in 'disabled' execution encountered");
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#disableOnOsWithEnvironmentVariableMatches()
	 */
	@Test
	void disableOnOsWithEnvironmentVariableMatches() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onLinux());
	}

	/**
	 * This method is coupled with the usage of the LINUX API annotation on the test level. It is done only in this
	 * test since it is hard to get the information about the type of OS at the level of this test.
	 *
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#blankNamedAttribute()
	 */
	@Test
	void blankNamedAttribute() {
		if (onLinux()) {
			Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
			assertThat(exception).hasMessageContaining("The 'named' attribute must not be blank");
		}
	}

	/**
	 * This method is coupled with the usage of the SOLARIS API annotation on the test level. It is done only in this
	 * test since it is hard to get the information about the type of OS at the level of this test.
	 *
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#blankMatchesAttribute()
	 */
	@Test
	void blankMatchesAttribute() {
		if (onSolaris()) {
			Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
			assertThat(exception).hasMessageContaining("The 'matches' attribute must not be blank");
		}
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#macOs()
	 */
	@Test
	void macOs() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onMac());
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#windows()
	 */
	@Test
	void windows() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onWindows());
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#solaris()
	 */
	@Test
	void solaris() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onSolaris());
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(!(onLinux() || onMac() || onSolaris() || onWindows()));
	}

	private void assertDisabledOnCurrentOsIf(boolean condition) {
		if (condition) {
			assertDisabled();
			assertReasonContains("matches regular expression");
		}
		else {
			assertEnabled();
			assertReasonContains(
				"No @DisabledOnOsWithEnvironmentVariable conditions resulting in 'disabled' execution encountered");
		}
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#disabledBecauseEnvironmentVariableForComposedAnnotationMatchesExactly()
	 */
	@Test
	void disabledBecauseEnvironmentVariableForComposedAnnotationMatchesExactly() {
		this.condition = new DisabledOnOsWithEnvironmentVariableCondition() {
			@Override
			protected String getEnvironmentVariable(String name) {
				return KEY1.equals(name) || KEY2.equals(name) ? ENIGMA : null;
			}
		};
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onLinux());
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#disabledBecauseEnvironmentVariableMatchesExactly()
	 */
	@Test
	void disabledBecauseEnvironmentVariableMatchesExactly() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onLinux());
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#disabledBecauseEnvironmentVariableMatchesPattern()
	 */
	@Test
	void disabledBecauseEnvironmentVariableMatchesPattern() {
		evaluateCondition();
		assertDisabledOnCurrentOsIf(onLinux());
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#enabledBecauseEnvironmentVariableDoesNotMatch()
	 */
	@Test
	void enabledBecauseEnvironmentVariableDoesNotMatch() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains(
			"No @DisabledOnOsWithEnvironmentVariable conditions resulting in 'disabled' execution encountered");
	}

	/**
	 * @see DisabledOnOsWithEnvironmentVariableIntegrationTests#enabledBecauseEnvironmentVariableDoesNotExist()
	 */
	@Test
	void enabledBecauseEnvironmentVariableDoesNotExist() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains(
			"No @DisabledOnOsWithEnvironmentVariable conditions resulting in 'disabled' execution encountered");
	}
}
