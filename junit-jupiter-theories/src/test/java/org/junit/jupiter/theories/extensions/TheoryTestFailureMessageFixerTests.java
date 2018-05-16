/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories.extensions;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.theories.exceptions.MessageModifyingWrapperException;

/**
 * Tests for {@link TheoryTestFailureMessageFixer}.
 */
class TheoryTestFailureMessageFixerTests {
	@Test
	public void testHandleTestExecutionException() throws Throwable {
		//Setup
		Method testMethod = TheoryTestFailureMessageFixerTests.class.getMethod("testHandleTestExecutionException");

		String argumentsDescription = "Mock arguments description";
		String displayName = "Test display name";
		String testMethodToString = testMethod.toString();

		Throwable testException = new RuntimeException("Test exception");
		Supplier<String> mockArgumentsDescriptionSupplier = () -> argumentsDescription;

		TheoryTestFailureMessageFixer extensionUnderTest = new TheoryTestFailureMessageFixer(
			mockArgumentsDescriptionSupplier);

		ExtensionContext context = mock(ExtensionContext.class);

		when(context.getRequiredTestMethod()).thenReturn(testMethod);
		when(context.getDisplayName()).thenReturn(displayName);

		//Test/Verify
		// @formatter:off
		assertThatThrownBy(() -> extensionUnderTest.handleTestExecutionException(context, testException))
                .isInstanceOf(MessageModifyingWrapperException.class)
                .hasMessageContaining(argumentsDescription)
                .hasMessageContaining(displayName)
                .hasMessageContaining(testMethodToString)
				.returns(testException.getStackTrace(), Throwable::getStackTrace);
        // @formatter:on
	}
}
