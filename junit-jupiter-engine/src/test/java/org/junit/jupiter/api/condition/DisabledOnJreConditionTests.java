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
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava10;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava11;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava12;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava13;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava14;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava15;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava16;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava17;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava18;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava19;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava20;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava21;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava22;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava23;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava8;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava9;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onKnownVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link DisabledOnJreCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledOnJreIntegrationTests}.
 *
 * @since 5.1
 */
class DisabledOnJreConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new DisabledOnJreCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return DisabledOnJreIntegrationTests.class;
	}

	/**
	 * @see DisabledOnJreIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@DisabledOnJre is not present");
	}

	/**
	 * @see DisabledOnJreIntegrationTests#missingJreDeclaration()
	 */
	@Test
	void missingJreDeclaration() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("You must declare at least one JRE");
	}

	/**
	 * @see DisabledOnJreIntegrationTests#disabledOnAllJavaVersions()
	 */
	@Test
	void disabledOnAllJavaVersions() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(true);
		assertCustomDisabledReasonIs("Disabled on every JRE");
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java8()
	 */
	@Test
	void java8() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava8());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java9()
	 */
	@Test
	void java9() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava9());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java10()
	 */
	@Test
	void java10() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava10());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java11()
	 */
	@Test
	void java11() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava11());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java12()
	 */
	@Test
	void java12() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava12());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java13()
	 */
	@Test
	void java13() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava13());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java14()
	 */
	@Test
	void java14() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava14());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java15()
	 */
	@Test
	void java15() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava15());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java16()
	 */
	@Test
	void java16() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava16());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java17()
	 */
	@Test
	void java17() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava17());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java18()
	 */
	@Test
	void java18() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava18());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java19()
	 */
	@Test
	void java19() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava19());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java20()
	 */
	@Test
	void java20() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava20());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java21()
	 */
	@Test
	void java21() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava21());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java22()
	 */
	@Test
	void java22() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava22());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#java23()
	 */
	@Test
	void java23() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava23());
	}

	/**
	 * @see DisabledOnJreIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(!onKnownVersion());
	}

	private void assertDisabledOnCurrentJreIf(boolean condition) {
		if (condition) {
			assertDisabled();
			assertReasonContains("Disabled on JRE version: " + System.getProperty("java.version"));
		}
		else {
			assertEnabled();
			assertReasonContains("Enabled on JRE version: " + System.getProperty("java.version"));
		}
	}

}
