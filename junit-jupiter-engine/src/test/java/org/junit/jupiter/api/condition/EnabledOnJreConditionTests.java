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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava10;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava11;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava8;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava9;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.util.PreconditionViolationException;

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
		assertEnabledOnCurrentJre();
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java8()
	 */
	@Test
	void java8() {
		evaluateCondition();
		if (onJava8()) {
			assertEnabledOnCurrentJre();
		}
		else {
			assertDisabledOnCurrentJre();
		}
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java9()
	 */
	@Test
	void java9() {
		evaluateCondition();
		if (onJava9()) {
			assertEnabledOnCurrentJre();
		}
		else {
			assertDisabledOnCurrentJre();
		}
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java10()
	 */
	@Test
	void java10() {
		evaluateCondition();
		if (onJava10()) {
			assertEnabledOnCurrentJre();
		}
		else {
			assertDisabledOnCurrentJre();
		}
	}

	/**
	 * @see EnabledOnJreIntegrationTests#java11()
	 */
	@Test
	void java11() {
		evaluateCondition();
		if (onJava11()) {
			assertEnabledOnCurrentJre();
		}
		else {
			assertDisabledOnCurrentJre();
		}
	}

	/**
	 * @see EnabledOnJreIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		if (onJava8() || onJava9() || onJava10() || onJava11()) {
			assertDisabledOnCurrentJre();
		}
		else {
			assertEnabledOnCurrentJre();
		}
	}

	private void assertEnabledOnCurrentJre() {
		assertEnabled();
		assertReasonContains("Enabled on JRE version: " + System.getProperty("java.version"));
	}

	private void assertDisabledOnCurrentJre() {
		assertDisabled();
		assertReasonContains("Disabled on JRE version: " + System.getProperty("java.version"));
	}

}
