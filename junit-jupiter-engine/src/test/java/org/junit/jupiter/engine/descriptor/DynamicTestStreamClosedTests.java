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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.execution.ThrowableCollector;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.Node;

public class DynamicTestStreamClosedTests {
	private JupiterEngineExecutionContext context;
	private Method testMethod;
	private TestExtensionContext testExtensionContext;
	private boolean isClosed;

	@BeforeEach
	public void before() {
		testExtensionContext = mock(TestExtensionContext.class);
		isClosed = false;

		context = new JupiterEngineExecutionContext(null, null).extend().withThrowableCollector(
			new ThrowableCollector()).withExtensionContext(testExtensionContext).build();
	}

	@Test
	public void streamsFromTestFactoriesShouldBeClosed() throws NoSuchMethodException {
		testMethod = DynamicCloseHookedStreamTest.class.getMethod("customStream");
		when(testExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

		Stream<DynamicTest> stream = Stream.<DynamicTest> of().onClose(() -> isClosed = true);
		when(testExtensionContext.getTestInstance()).thenReturn(new DynamicCloseHookedStreamTest(stream));

		TestFactoryTestDescriptor descriptor = new TestFactoryTestDescriptor(UniqueId.forEngine("engine"),
			DynamicCloseHookedStreamTest.class, testMethod);

		descriptor.invokeTestMethod(context, mock(Node.DynamicTestExecutor.class));
		assertTrue(isClosed);
	}

	@Test
	public void streamsFromTestFactoriesShouldBeClosedWhenTheyThrow() throws NoSuchMethodException {
		testMethod = StreamOfIntClosedTest.class.getMethod("customStream");
		when(testExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

		Stream<Integer> mockStream = Stream.of(1, 2).onClose(() -> isClosed = true);
		when(testExtensionContext.getTestInstance()).thenReturn(new StreamOfIntClosedTest(mockStream));

		TestFactoryTestDescriptor descriptor = new TestFactoryTestDescriptor(UniqueId.forEngine("engine"),
			StreamOfIntClosedTest.class, testMethod);
		descriptor.invokeTestMethod(context, mock(Node.DynamicTestExecutor.class));

		assertTrue(isClosed);
	}

	@Disabled
	public static class DynamicCloseHookedStreamTest {
		private Stream<DynamicTest> mockStream;

		public DynamicCloseHookedStreamTest(Stream<DynamicTest> mockStream) {
			this.mockStream = mockStream;
		}

		@TestFactory
		public Stream<DynamicTest> customStream() {
			return mockStream;
		}
	}

	@Disabled
	public static class StreamOfIntClosedTest {
		private Stream<Integer> mockStream;

		public StreamOfIntClosedTest(Stream<Integer> mockStream) {
			this.mockStream = mockStream;
		}

		@TestFactory
		public Stream<Integer> customStream() {
			return mockStream;
		}
	}
}
