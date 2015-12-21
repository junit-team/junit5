/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Microtests that verify behaviour of {@link HierarchicalTestExecutor}
 */
public class HierarchicalTestExecutorTests {

	MyContainer root;
	EngineExecutionListener listener;
	MyEngineExecutionContext rootContext;
	HierarchicalTestExecutor<MyEngineExecutionContext> executor;

	@Before
	public void init() {
		root = Mockito.spy(new MyContainer("root"));
		listener = Mockito.mock(EngineExecutionListener.class);
		rootContext = new MyEngineExecutionContext();
		ExecutionRequest request = new ExecutionRequest(root, listener);
		executor = new MyExecutor(request, rootContext);
	}

	@Test
	public void emptyRootDescriptor() throws Throwable {

		InOrder inOrder = inOrder(listener, root);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> aTestExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).beforeAll(rootContext);
		inOrder.verify(root).afterAll(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), aTestExecutionResult.capture());

		assertTrue(aTestExecutionResult.getValue().getStatus() == TestExecutionResult.Status.SUCCESSFUL,
			"Execution should be successful.");
	}

	private static class MyEngineExecutionContext implements EngineExecutionContext {
	}

	private static class MyContainer extends AbstractTestDescriptor implements Container<MyEngineExecutionContext> {

		protected MyContainer(String uniqueId) {
			super(uniqueId);
		}

		@Override
		public String getDisplayName() {
			return getUniqueId();
		}

		@Override
		public boolean isTest() {
			return false;
		}

		@Override
		public boolean isContainer() {
			return true;
		}

		@Override
		public MyEngineExecutionContext beforeAll(MyEngineExecutionContext context) throws Throwable {
			return context;
		}

		@Override
		public MyEngineExecutionContext afterAll(MyEngineExecutionContext context) throws Throwable {
			return context;
		}
	}

	private static class MyExecutor extends HierarchicalTestExecutor<MyEngineExecutionContext> {

		MyExecutor(ExecutionRequest request, MyEngineExecutionContext rootContext) {
			super(request, rootContext);
		}
	}
}
