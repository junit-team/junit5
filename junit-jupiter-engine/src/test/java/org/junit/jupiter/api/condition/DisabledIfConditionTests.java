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
 * Unit tests for {@link DisabledIf}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledIfIntegrationTests}.
 *
 * @since 5.7
 */
public class DisabledIfConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new DisabledIfCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return DisabledIfIntegrationTests.class;
	}

	/**
	 * @see DisabledIfIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@DisabledIf is not present");
	}

	/**
	 * @see DisabledIfIntegrationTests#disabledBecauseStaticConditionMethodReturnsTrue()
	 */
	@Test
	void disabledBecauseStaticConditionMethodReturnsTrue() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("Disabled for some reason");
	}

	/**
	 * @see DisabledIfIntegrationTests#enabledBecauseStaticConditionMethodReturnsFalse()
	 */
	@Test
	void enabledBecauseStaticConditionMethodReturnsFalse() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("Condition provided in @DisabledIf evaluates to false");
	}

	/**
	 * @see DisabledIfIntegrationTests#disabledBecauseConditionMethodReturnsTrue()
	 */
	@Test
	void disabledBecauseConditionMethodReturnsTrue() {
		evaluateCondition();
		assertDisabled();
		assertReasonContains("Condition provided in @DisabledIf evaluates to true");
	}

	/**
	 * @see DisabledIfIntegrationTests#enabledBecauseConditionMethodReturnsFalse()
	 */
	@Test
	void enabledBecauseConditionMethodReturnsFalse() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("Condition provided in @DisabledIf evaluates to false");
	}

	@Nested
	class ExternalConditionMethod {

		/**
		 * @see DisabledIfIntegrationTests.ExternalConditionMethod#disabledBecauseConditionMethodReturnsTrue()
		 */
		@Test
		void disabledBecauseConditionMethodReturnsTrue() {
			evaluateCondition();
			assertDisabled();
			assertReasonContains("Condition provided in @DisabledIf evaluates to true");
		}

		/**
		 * @see DisabledIfIntegrationTests.ExternalConditionMethod#enabledBecauseConditionMethodReturnsFalse()
		 */
		@Test
		void enabledBecauseConditionMethodReturnsFalse() {
			evaluateCondition();
			assertEnabled();
			assertReasonContains("Condition provided in @DisabledIf evaluates to false");
		}

	}

}
