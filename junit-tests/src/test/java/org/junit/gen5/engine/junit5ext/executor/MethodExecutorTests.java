/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.junit5ext.executor.ExecutionContext.contextForDescriptor;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5ext.TestHandler;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;
import org.junit.gen5.engine.junit5ext.descriptor.MethodDescriptor;
import org.junit.gen5.engine.junit5ext.samples.SinglePassingTestSampleClass;
import org.junit.gen5.engine.junit5ext.testdoubles.TestDescriptorStub;
import org.junit.gen5.engine.junit5ext.testdoubles.TestExecutorRegistrySpy;
import org.junit.gen5.engine.junit5ext.testdoubles.TestHandlerSpy;

public class MethodExecutorTests {
	private MethodExecutor methodExecutor = new MethodExecutor();
	private TestHandlerSpy testHandlerSpy = new TestHandlerSpy();

	@Test
	public void givenAnArbitraryDescriptor_executorDeclinesRequest() throws Exception {
		boolean result = methodExecutor.canExecute(contextForDescriptor(new TestDescriptorStub()).build());
		assertThat(result).isFalse();
	}

	@Test
	public void givenAMethodDescriptor_executorAcceptsRequest() throws Exception {
		MethodDescriptor methodDescriptor = getMethodDescriptor(SinglePassingTestSampleClass.class,
			"singlePassingTest");
		boolean result = methodExecutor.canExecute(getContextForMethodDescriptor(methodDescriptor));
		assertThat(result).isTrue();
	}

	@Test
	public void givenAMethodDescriptor_executionIsTriggeredViaTheTestHandler() throws Exception {
		MethodDescriptor methodDescriptor = getMethodDescriptor(SinglePassingTestSampleClass.class,
			"singlePassingTest");
		methodExecutor.execute(getContextForMethodDescriptor(methodDescriptor));
		assertThat(testHandlerSpy.foundTestDescriptor).isEqualTo(methodDescriptor);
	}

	private ExecutionContext getContextForMethodDescriptor(MethodDescriptor methodDescriptor) {
		ExecutionContext executionContext = contextForDescriptor(methodDescriptor).build();
		executionContext.setTestHandler(testHandlerSpy);
		return executionContext;
	}

	private MethodDescriptor getMethodDescriptor(Class<SinglePassingTestSampleClass> testClass, String testMethodName) {
		Method method = ReflectionUtils.findMethod(testClass, testMethodName).orElseThrow(() -> new AssertionError(
			"Could not find method ''" + testMethodName + "'' in class " + testClass.getCanonicalName()));
		return new MethodDescriptor(method, method.toString(), method.getName());
	}
}
