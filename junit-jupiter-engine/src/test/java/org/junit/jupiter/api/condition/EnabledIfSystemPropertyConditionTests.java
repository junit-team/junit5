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
		assertEnabled();
		assertReasonContains("@EnabledIfSystemProperty is not present");
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#enabledBecauseSystemPropertyMatchesExactly()
	 */
	@Test
	void enabledBecauseSystemPropertyMatchesExactly() {
		assertEnabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#enabledBecauseSystemPropertyMatchesPattern()
	 */
	@Test
	void enabledBecauseSystemPropertyMatchesPattern() {
		assertEnabled();
		assertReasonContains("matches regular expression");
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#disabledBecauseSystemPropertyDoesNotMatch()
	 */
	@Test
	void disabledBecauseSystemPropertyDoesNotMatch() {
		assertDisabled();
		assertReasonContains("does not match regular expression");
	}

	/**
	 * @see EnabledIfSystemPropertyIntegrationTests#disabledBecauseSystemPropertyDoesNotExist()
	 */
	@Test
	void disabledBecauseSystemPropertyDoesNotExist() {
		assertDisabled();
		assertReasonContains("does not exist");
	}

}
