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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.DisabledIfEnvironmentVariableIntegrationTests.ENIGMA;
import static org.junit.jupiter.api.condition.DisabledIfEnvironmentVariableIntegrationTests.KEY1;
import static org.junit.jupiter.api.condition.DisabledIfEnvironmentVariableIntegrationTests.KEY2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link DisabledIfEnvironmentVariableCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledIfEnvironmentVariableIntegrationTests}.
 *
 * @since 5.1
 */
class DisabledIfEnvironmentVariableConditionTests extends AbstractExecutionConditionTests {

	/**
	 * Stubbed subclass of {@link DisabledIfEnvironmentVariableCondition}.
	 */
	private ExecutionCondition condition = new DisabledIfEnvironmentVariableCondition() {

		@Override
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
		return DisabledIfEnvironmentVariableIntegrationTests.class;
	}

	/**
	 * @see DisabledIfEnvironmentVariableIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains(
			"No @DisabledIfEnvironmentVariable conditions resulting in 'disabled' execution encountered");
	}

	/**
	 * @see DisabledIfEnvironmentVariableIntegrationTests#blankNamedAttribute()
	 */
	@Test
	void blankNamedAttribute() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("The 'named' attribute must not be blank");
	}

	/**
	 * @see DisabledIfEnvironmentVariableIntegrationTests#blankMatchesAttribute()
	 */
	@Test
	void blankMatchesAttribute() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("The 'matches' attribute must not be blank");
	}

	/**
	 * @see DisabledIfEnvironmentVariableIntegrationTests#disabledBecauseEnvironmentVariableMatchesExactly()
	 */
	@Test
	void disabledBecauseEnvironmentVariableMatchesExactly() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("matches regular expression");
		assertCustomDisabledReasonIs("That's an enigma");
	}

	/**
	 * @see DisabledIfEnvironmentVariableIntegrationTests#disabledBecauseEnvironmentVariableForComposedAnnotationMatchesExactly()
	 */
	@Test
	void disabledBecauseEnvironmentVariableForComposedAnnotationMatchesExactly() {
		this.condition = new DisabledIfEnvironmentVariableCondition() {

			@Override
			protected String getEnvironmentVariable(String name) {
				return KEY1.equals(name) || KEY2.equals(name) ? ENIGMA : null;
			}
		};

		evaluateCondition();
		assertDisabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see DisabledIfEnvironmentVariableIntegrationTests#disabledBecauseEnvironmentVariableMatchesPattern()
	 */
	@Test
	void disabledBecauseEnvironmentVariableMatchesPattern() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see DisabledIfEnvironmentVariableIntegrationTests#enabledBecauseEnvironmentVariableDoesNotMatch()
	 */
	@Test
	void enabledBecauseEnvironmentVariableDoesNotMatch() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains(
			"No @DisabledIfEnvironmentVariable conditions resulting in 'disabled' execution encountered");
	}

	/**
	 * @see DisabledIfEnvironmentVariableIntegrationTests#enabledBecauseEnvironmentVariableDoesNotExist()
	 */
	@Test
	void enabledBecauseEnvironmentVariableDoesNotExist() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains(
			"No @DisabledIfEnvironmentVariable conditions resulting in 'disabled' execution encountered");
	}

}
