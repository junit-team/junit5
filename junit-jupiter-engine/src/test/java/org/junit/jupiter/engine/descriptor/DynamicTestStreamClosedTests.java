/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.execution.ThrowableCollector;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.Node;

public class DynamicTestStreamClosedTests {
	@SuppressWarnings("unchecked")
	private static final Stream<DynamicTest> mockStream = mock(Stream.class);
	private JupiterEngineExecutionContext context;
	private Method testMethod;

	@Before
	public void before() throws NoSuchMethodException {
		TestExtensionContext testExtensionContext = mock(TestExtensionContext.class);

		testMethod = DynamicCloseHookedStreamTest.class.getMethod("customStream");
		when(testExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
		when(testExtensionContext.getTestInstance()).thenReturn(new DynamicCloseHookedStreamTest());

		context = new JupiterEngineExecutionContext(null, null).extend().withThrowableCollector(
			new ThrowableCollector()).withExtensionContext(testExtensionContext).build();
	}

	@Test
	public void streamsFromTestFactoriesShouldBeClosed() {
		TestFactoryTestDescriptor descriptor = new TestFactoryTestDescriptor(UniqueId.forEngine("engine"),
			DynamicCloseHookedStreamTest.class, testMethod);
		descriptor.invokeTestMethod(context, mock(Node.DynamicTestExecutor.class));
		verify(mockStream).close();
	}

	public static class DynamicCloseHookedStreamTest {

		@TestFactory
		public Stream<DynamicTest> customStream() {
			return mockStream;
		}
	}
}
