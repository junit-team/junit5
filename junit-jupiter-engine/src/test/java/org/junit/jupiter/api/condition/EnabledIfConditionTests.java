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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;

/**
 * Unit tests for {@link EnabledIf}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link EnabledIfIntegrationTests}.
 *
 * @since 5.7
 */
public class EnabledIfConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new EnabledIfCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return EnabledIfIntegrationTests.class;
	}

	/**
	 * @see EnabledIfIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@EnabledIf is not present");
	}

	/**
	 * @see EnabledIfIntegrationTests#enabledBecauseStaticConditionMethodReturnsTrue()
	 */
	@Test
	void enabledBecauseStaticConditionMethodReturnsTrue() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("Condition provided in @EnabledIf evaluates to true");
	}

	/**
	 * @see EnabledIfIntegrationTests#disabledBecauseStaticConditionMethodReturnsFalse()
	 */
	@Test
	void disabledBecauseStaticConditionMethodReturnsFalse() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("Disabled for some reason");
	}

	/**
	 * @see EnabledIfIntegrationTests#enabledBecauseConditionMethodReturnsTrue()
	 */
	@Test
	void enabledBecauseConditionMethodReturnsTrue() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("Condition provided in @EnabledIf evaluates to true");
	}

	/**
	 * @see EnabledIfIntegrationTests#disabledBecauseConditionMethodReturnsFalse()
	 */
	@Test
	void disabledBecauseConditionMethodReturnsFalse() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("Condition provided in @EnabledIf evaluates to false");
	}

	@Nested
	class ExternalConditionMethod {

		/**
		 * @see EnabledIfIntegrationTests.ExternalConditionMethod#enabledBecauseConditionMethodReturnsTrue()
		 */
		@Test
		void enabledBecauseConditionMethodReturnsTrue() {
			evaluateCondition();
			assertEnabled();
			assertReasonContains("Condition provided in @EnabledIf evaluates to true");
		}

		/**
		 * @see EnabledIfIntegrationTests.ExternalConditionMethod#disabledBecauseConditionMethodReturnsFalse()
		 */
		@Test
		void disabledBecauseConditionMethodReturnsFalse() {
			evaluateCondition();
			assertDisabled();
			assertReasonContains("Condition provided in @EnabledIf evaluates to false");
		}

	}

}
