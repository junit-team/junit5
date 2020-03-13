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

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link DisabledIf}.
 *
 * @since 5.7
 */
public class DisabledIfIntegrationTests {

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@DisabledIf("methodThatReturnsTrue")
	void disabledBecauseConditionMethodReturnsTrue() {
		fail("Should be disabled");
	}

	@Test
	@DisabledIf("methodThatReturnsFalse")
	void enabledBecauseConditionMethodReturnsFalse() {
	}

	private static boolean methodThatReturnsTrue() {
		return true;
	}

	private static boolean methodThatReturnsFalse() {
		return false;
	}

	@Nested
	class ExternalConditionMethod {

		@Test
		@DisabledIf("org.junit.jupiter.api.condition.StaticConditionMethods#returnsTrue")
		void disabledBecauseConditionMethodReturnsTrue() {
			fail("Should be disabled");
		}

		@Test
		@DisabledIf("org.junit.jupiter.api.condition.StaticConditionMethods#returnsFalse")
		void enabledBecauseConditionMethodReturnsFalse() {
		}

	}

}
