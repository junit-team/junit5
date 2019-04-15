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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link EnabledIfSystemPropertyCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link EnabledIfSystemPropertyIntegrationTests}.
 *
 * @since 5.1
 */
class EnabledIfSystemPropertyConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new EnabledIfSystemPropertyCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return EnabledIfSystemPropertyIntegrationTests.class;
	}

	@BeforeAll
	static void setSystemProperty() {
		EnabledIfSystemPropertyIntegrationTests.setSystemProperty();
	}

	@AfterAll
	static void clearSystemProperty() {
		EnabledIfSystemPropertyIntegrationTests.clearSystemProperty();
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@EnabledIfSystemProperty is not present");
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#blankNamedAttribute()
	 */
	@Test
	void blankNamedAttribute() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("The 'named' attribute must not be blank");
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#blankMatchesAttribute()
	 */
	@Test
	void blankMatchesAttribute() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("The 'matches' attribute must not be blank");
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#enabledBecauseSystemPropertyMatchesExactly()
	 */
	@Test
	void enabledBecauseSystemPropertyMatchesExactly() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#enabledBecauseSystemPropertyMatchesPattern()
	 */
	@Test
	void enabledBecauseSystemPropertyMatchesPattern() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#disabledBecauseSystemPropertyDoesNotMatch()
	 */
	@Test
	void disabledBecauseSystemPropertyDoesNotMatch() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("does not match regular expression");
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#disabledBecauseSystemPropertyDoesNotExist()
	 */
	@Test
	void disabledBecauseSystemPropertyDoesNotExist() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("does not exist");
	}

}
