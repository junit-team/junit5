/*
 * Copyright 2015-2019 the original author or authors.
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
import static org.junit.jupiter.api.condition.EnabledIfEnvironmentVariableIntegrationTests.ENIGMA;
import static org.junit.jupiter.api.condition.EnabledIfEnvironmentVariableIntegrationTests.KEY;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link EnabledIfEnvironmentVariableCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link EnabledIfEnvironmentVariableIntegrationTests}.
 *
 * @since 5.1
 */
class EnabledIfEnvironmentVariableConditionTests extends AbstractExecutionConditionTests {

	/**
	 * Stubbed subclass of {@link EnabledIfEnvironmentVariableCondition}.
	 */
	private static final ExecutionCondition condition = new EnabledIfEnvironmentVariableCondition() {

		@Override
		protected String getEnvironmentVariable(String name) {
			return KEY.equals(name) ? ENIGMA : null;
		}
	};

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return condition;
	}

	@Override
	protected Class<?> getTestClass() {
		return EnabledIfEnvironmentVariableIntegrationTests.class;
	}

	/**
	 * @see EnabledIfEnvironmentVariableIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@EnabledIfEnvironmentVariable is not present");
	}

	/**
	 * @see EnabledIfEnvironmentVariableIntegrationTests#blankNamedAttribute()
	 */
	@Test
	void blankNamedAttribute() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("The 'named' attribute must not be blank");
	}

	/**
	 * @see EnabledIfEnvironmentVariableIntegrationTests#blankMatchesAttribute()
	 */
	@Test
	void blankMatchesAttribute() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("The 'matches' attribute must not be blank");
	}

	/**
	 * @see EnabledIfEnvironmentVariableIntegrationTests#enabledBecauseEnvironmentVariableMatchesExactly()
	 */
	@Test
	void enabledBecauseEnvironmentVariableMatchesExactly() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see EnabledIfEnvironmentVariableIntegrationTests#enabledBecauseEnvironmentVariableMatchesPattern()
	 */
	@Test
	void enabledBecauseEnvironmentVariableMatchesPattern() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see EnabledIfEnvironmentVariableIntegrationTests#disabledBecauseEnvironmentVariableDoesNotMatch()
	 */
	@Test
	void disabledBecauseEnvironmentVariableDoesNotMatch() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("does not match regular expression");
	}

	/**
	 * @see EnabledIfEnvironmentVariableIntegrationTests#disabledBecauseEnvironmentVariableDoesNotExist()
	 */
	@Test
	void disabledBecauseEnvironmentVariableDoesNotExist() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("does not exist");
	}

}
