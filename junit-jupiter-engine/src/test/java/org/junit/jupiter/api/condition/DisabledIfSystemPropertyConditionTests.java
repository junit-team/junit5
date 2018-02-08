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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;

/**
 * Unit tests for {@link DisabledIfSystemPropertyCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledIfSystemPropertyIntegrationTests}.
 *
 * @since 5.1
 */
class DisabledIfSystemPropertyConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new DisabledIfSystemPropertyCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return DisabledIfSystemPropertyIntegrationTests.class;
	}

	@BeforeAll
	static void setSystemProperty() {
		DisabledIfSystemPropertyIntegrationTests.setSystemProperty();
	}

	@AfterAll
	static void clearSystemProperty() {
		DisabledIfSystemPropertyIntegrationTests.clearSystemProperty();
	}

	/**
	 * @see DisabledIfSystemPropertyIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		assertEnabled();
		assertReasonContains("@DisabledIfSystemProperty is not present");
	}

	/**
	 * @see DisabledIfSystemPropertyIntegrationTests#disabledBecauseSystemPropertyMatchesExactly()
	 */
	@Test
	void disabledBecauseSystemPropertyMatchesExactly() {
		assertDisabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see DisabledIfSystemPropertyIntegrationTests#disabledBecauseSystemPropertyMatchesPattern()
	 */
	@Test
	void disabledBecauseSystemPropertyMatchesPattern() {
		assertDisabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see DisabledIfSystemPropertyIntegrationTests#enabledBecauseSystemPropertyDoesNotMatch()
	 */
	@Test
	void enabledBecauseSystemPropertyDoesNotMatch() {
		assertEnabled();
		assertReasonContains("does not match regular expression");
	}

	/**
	 * @see DisabledIfSystemPropertyIntegrationTests#enabledBecauseSystemPropertyDoesNotExist()
	 */
	@Test
	void enabledBecauseSystemPropertyDoesNotExist() {
		assertEnabled();
		assertReasonContains("does not exist");
	}

}
