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
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava10;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava11;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava12;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava13;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava14;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava15;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava16;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava17;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava18;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava19;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava20;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava21;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava22;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava23;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava8;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava9;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link EnabledOnJreCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link EnabledOnJreIntegrationTests}.
 *
 * @since 5.1
 */
class EnabledOnJreConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new EnabledOnJreCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return EnabledOnJreIntegrationTests.class;
	}

	/**
	 * @see EnabledOnJreIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@EnabledOnJre is not present");
	}

	/**
	 * @see EnabledOnJreIntegrationTests#missingJreDeclaration()
	 */
	@Test
	void missingJreDeclaration() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("You must declare at least one JRE");
	}

	/**
	 * @see EnabledOnJreIntegrationTests#enabledOnAllJavaVersions()
	 */
	@Test
	void enabledOnAllJavaVersions() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(true);
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java8()
	 */
	@Test
	void java8() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava8());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java9()
	 */
	@Test
	void java9() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava9());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java10()
	 */
	@Test
	void java10() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava10());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java11()
	 */
	@Test
	void java11() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava11());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java12()
	 */
	@Test
	void java12() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava12());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java13()
	 */
	@Test
	void java13() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava13());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java14()
	 */
	@Test
	void java14() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava14());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java15()
	 */
	@Test
	void java15() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava15());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java16()
	 */
	@Test
	void java16() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava16());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java17()
	 */
	@Test
	void java17() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava17());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java18()
	 */
	@Test
	void java18() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava18());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java19()
	 */
	@Test
	void java19() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava19());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java20()
	 */
	@Test
	void java20() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava20());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java21()
	 */
	@Test
	void java21() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava21());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java22()
	 */
	@Test
	void java22() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava22());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java23()
	 */
	@Test
	void java23() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava23());
	}

	/**
	 * @see EnabledOnJreIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(!(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13()
				|| onJava14() || onJava15() || onJava16() || onJava17() || onJava18() || onJava19() || onJava20()
				|| onJava21() || onJava22() || onJava23()));
		assertCustomDisabledReasonIs("Disabled on almost every JRE");
	}

	private void assertEnabledOnCurrentJreIf(boolean condition) {
		if (condition) {
			assertEnabled();
			assertReasonContains("Enabled on JRE version: " + System.getProperty("java.version"));
		}
		else {
			assertDisabled();
			assertReasonContains("Disabled on JRE version: " + System.getProperty("java.version"));
		}
	}

}
