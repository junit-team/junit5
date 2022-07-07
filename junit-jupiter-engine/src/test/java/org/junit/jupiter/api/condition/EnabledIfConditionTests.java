/*
 * Copyright 2015-2022 the original author or authors.
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
		assertReasonMatches("(?:interface org\\.junit\\.jupiter\\.api\\.condition\\.|@)EnabledIf is not present");
	}

	/**
	 * @see EnabledIfIntegrationTests#enabledBecauseStaticConditionMethodReturnsTrue()
	 */
	@Test
	void enabledBecauseStaticConditionMethodReturnsTrue() {
		evaluateCondition();
		assertEnabled();
		assertReasonMatches("Condition provided in (?:interface org\\.junit\\.jupiter\\.api\\.condition\\.|@)EnabledIf evaluate(?:s|d) to true");
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
		assertReasonMatches("Condition provided in (?:interface org\\.junit\\.jupiter\\.api\\.condition\\.|@)EnabledIf evaluate(?:s|d) to true");
	}

	/**
	 * @see EnabledIfIntegrationTests#disabledBecauseConditionMethodReturnsFalse()
	 */
	@Test
	void disabledBecauseConditionMethodReturnsFalse() {
		evaluateCondition();
		assertDisabled();
		assertReasonMatches("Condition provided in (?:interface org\\.junit\\.jupiter\\.api\\.condition\\.|@)EnabledIf evaluate(?:s|d) to false");
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
			assertReasonMatches("Condition provided in (?:interface org\\.junit\\.jupiter\\.api\\.condition\\.|@)EnabledIf evaluate(?:s|d) to true");
		}

		/**
		 * @see EnabledIfIntegrationTests.ExternalConditionMethod#disabledBecauseConditionMethodReturnsFalse()
		 */
		@Test
		void disabledBecauseConditionMethodReturnsFalse() {
			evaluateCondition();
			assertDisabled();
			assertReasonMatches("Condition provided in (?:interface org\\.junit\\.jupiter\\.api\\.condition\\.|@)EnabledIf evaluate(?:s|d) to false");
		}

	}

}
