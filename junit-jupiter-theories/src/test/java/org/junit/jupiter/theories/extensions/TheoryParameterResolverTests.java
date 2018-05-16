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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.theories.domain.DataPointDetails;

/**
 * Tests for {@link TheoryParameterResolver}.
 */
class TheoryParameterResolverTests {

	private static final int SUPPORTED_PARAMETER_INDEX = 0;
	private static final int UNSUPPORTED_PARAMETER_INDEX = 42;

	private Map<Integer, DataPointDetails> testTheoryArguments;

	private TheoryParameterResolver resolverUnderTest;

	@BeforeEach
	public void beforeEach() {
		Map<Integer, DataPointDetails> arguments = new HashMap<>();
		arguments.put(SUPPORTED_PARAMETER_INDEX, new DataPointDetails("Foo", Collections.emptyList(), "Test source 0"));
		arguments.put(1, new DataPointDetails(UUID.randomUUID(), Collections.emptyList(), "Test source 1"));
		arguments.put(6, new DataPointDetails(42, Collections.emptyList(), "Test source 6"));
		this.testTheoryArguments = Collections.unmodifiableMap(arguments);

		this.resolverUnderTest = new TheoryParameterResolver(testTheoryArguments);
	}

	@Test
	public void testSupportsParameter_Supported() {
		//Setup
		ParameterContext parameterContext = mock(ParameterContext.class);
		when(parameterContext.getIndex()).thenReturn(SUPPORTED_PARAMETER_INDEX);

		ExtensionContext ignored = mock(ExtensionContext.class);

		//Test
		boolean result = resolverUnderTest.supportsParameter(parameterContext, ignored);

		//Verify
		assertTrue(result);
	}

	@Test
	public void testSupportsParameter_NotSupported() {
		//Setup
		ParameterContext parameterContext = mock(ParameterContext.class);
		when(parameterContext.getIndex()).thenReturn(UNSUPPORTED_PARAMETER_INDEX);

		ExtensionContext ignored = mock(ExtensionContext.class);

		//Test
		boolean result = resolverUnderTest.supportsParameter(parameterContext, ignored);

		//Verify
		assertFalse(result);
	}

	@Test
	public void testResolveParameter_Success() {
		//Setup
		ParameterContext parameterContext = mock(ParameterContext.class);
		when(parameterContext.getIndex()).thenReturn(SUPPORTED_PARAMETER_INDEX);

		ExtensionContext ignored = mock(ExtensionContext.class);

		Object expectedResult = testTheoryArguments.get(SUPPORTED_PARAMETER_INDEX).getValue();

		//Test
		Object actualResult = resolverUnderTest.resolveParameter(parameterContext, ignored);

		//Verify
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void testResolveParameter_InvalidParameter() throws Exception {
		//Setup
		Parameter testParameter = String.class.getMethod("charAt", int.class).getParameters()[0];
		String testParameterName = testParameter.getName();

		ParameterContext parameterContext = mock(ParameterContext.class);
		when(parameterContext.getIndex()).thenReturn(UNSUPPORTED_PARAMETER_INDEX);
		when(parameterContext.getParameter()).thenReturn(testParameter);

		ExtensionContext ignored = mock(ExtensionContext.class);

		//Test/Verify
		// @formatter:off
		assertThatThrownBy(() -> resolverUnderTest.resolveParameter(parameterContext, ignored))
				.isInstanceOf(ParameterResolutionException.class)
				.hasMessageContaining("Unable to resolve argument")
				.hasMessageContaining("index " + UNSUPPORTED_PARAMETER_INDEX)
				.hasMessageContaining(testParameterName);
		// @formatter:on
	}
}
