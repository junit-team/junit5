/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.api.Assertions.assertSame;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.opentest4j.TestAbortedException;

/**
 * Microtests that verify behaviour of {@link HierarchicalTestExecutor}
 */
public class HierarchicalTestExecutorTests {

	MyContainer root;
	EngineExecutionListener listener;
	MyEngineExecutionContext rootContext;
	HierarchicalTestExecutor<MyEngineExecutionContext> executor;

	@BeforeEach
	public void init() {
		root = spy(new MyContainer("root"));
		listener = Mockito.mock(EngineExecutionListener.class);
		rootContext = new MyEngineExecutionContext();
		ExecutionRequest request = new ExecutionRequest(root, listener);
		executor = new MyExecutor(request, rootContext);
	}

	@Test
	public void emptyRootDescriptor() throws Throwable {

		InOrder inOrder = inOrder(listener, root);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).beforeAll(rootContext);
		inOrder.verify(root).afterAll(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());

		assertTrue(rootExecutionResult.getValue().getStatus() == TestExecutionResult.Status.SUCCESSFUL,
			"Execution of root should be successful.");
	}

	@Test
	public void rootDescriptorWithOneChildContainer() throws Throwable {

		MyContainer child = spy(new MyContainer("child container"));
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).beforeAll(rootContext);
		inOrder.verify(child).afterAll(rootContext);
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		assertTrue(childExecutionResult.getValue().getStatus() == TestExecutionResult.Status.SUCCESSFUL,
			"Execution of child container should be successful.");
	}

	@Test
	public void rootDescriptorWithOneChildLeaf() throws Throwable {

		MyLeaf child = spy(new MyLeaf("child leaf"));
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> aTestExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).execute(rootContext);
		inOrder.verify(listener).executionFinished(eq(child), aTestExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		assertTrue(aTestExecutionResult.getValue().getStatus() == TestExecutionResult.Status.SUCCESSFUL,
			"Execution of child leaf be successful.");
	}

	@Test
	public void skippingAContainer() throws Throwable {

		MyContainer child = spy(new MyContainer("child container"));
		stub(child.shouldBeSkipped(rootContext)).toReturn(Node.SkipResult.skip("in test"));
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		verify(listener, never()).executionStarted(child);
		verifyNoMoreInteractions(child);
		verify(listener, never()).executionFinished(eq(child), any(TestExecutionResult.class));
	}

	@Test
	public void skippingALeaf() throws Throwable {

		MyLeaf child = spy(new MyLeaf("child leaf"));
		stub(child.shouldBeSkipped(rootContext)).toReturn(Node.SkipResult.skip("in test"));
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		verify(listener, never()).executionStarted(child);
		verifyNoMoreInteractions(child);
		verify(listener, never()).executionFinished(eq(child), any(TestExecutionResult.class));
	}

	@Test
	public void exceptionInShouldBeSkipped() throws Throwable {

		MyContainer child = spy(new MyContainer("child container"));
		RuntimeException anException = new RuntimeException("in test");
		stub(child.shouldBeSkipped(rootContext)).toThrow(anException);
		root.addChild(child);

		InOrder inOrder = inOrder(listener, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		verifyNoMoreInteractions(child);

		assertTrue(childExecutionResult.getValue().getStatus() == TestExecutionResult.Status.FAILED,
			"Execution of child should fail.");
		assertSame(childExecutionResult.getValue().getThrowable().get(), anException);
	}

	@Test
	public void exceptionInContainerBeforeAll() throws Throwable {

		MyContainer child = spy(new MyContainer("child container"));
		root.addChild(child);
		RuntimeException anException = new RuntimeException("in test");
		stub(root.beforeAll(rootContext)).toThrow(anException);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).beforeAll(rootContext);
		inOrder.verify(root, never()).afterAll(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());

		assertTrue(rootExecutionResult.getValue().getStatus() == TestExecutionResult.Status.FAILED,
			"Execution of root should fail.");
		assertSame(rootExecutionResult.getValue().getThrowable().get(), anException);

		verifyNoMoreInteractions(child);
	}

	@Test
	public void exceptionInContainerAfterAll() throws Throwable {

		MyLeaf child = spy(new MyLeaf("child container"));
		root.addChild(child);
		RuntimeException anException = new RuntimeException("in test");
		stub(root.afterAll(rootContext)).toThrow(anException);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).beforeAll(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).execute(rootContext);
		inOrder.verify(listener).executionFinished(eq(child), any(TestExecutionResult.class));
		inOrder.verify(root).afterAll(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());

		assertTrue(rootExecutionResult.getValue().getStatus() == TestExecutionResult.Status.FAILED,
			"Execution of root should fail.");
		assertSame(rootExecutionResult.getValue().getThrowable().get(), anException);
	}

	@Test
	public void exceptionInLeafExecute() throws Throwable {

		MyLeaf child = spy(new MyLeaf("child container"));
		RuntimeException anException = new RuntimeException("in test");
		stub(child.execute(rootContext)).toThrow(anException);
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).beforeAll(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).execute(rootContext);
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());
		inOrder.verify(root).afterAll(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		assertTrue(childExecutionResult.getValue().getStatus() == TestExecutionResult.Status.FAILED,
			"Execution of child should fail.");
		assertSame(childExecutionResult.getValue().getThrowable().get(), anException);
	}

	@Test
	public void abortInContainerBeforeAll() throws Throwable {

		MyContainer child = spy(new MyContainer("child container"));
		root.addChild(child);
		TestAbortedException anAbortedException = new TestAbortedException("in test");
		stub(root.beforeAll(rootContext)).toThrow(anAbortedException);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).beforeAll(rootContext);
		inOrder.verify(root, never()).afterAll(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());

		assertTrue(rootExecutionResult.getValue().getStatus() == TestExecutionResult.Status.ABORTED,
			"Execution of root should abort.");
		assertSame(rootExecutionResult.getValue().getThrowable().get(), anAbortedException);

		verifyNoMoreInteractions(child);
	}

	@Test
	public void abortInLeafExecute() throws Throwable {

		MyLeaf child = spy(new MyLeaf("child container"));
		TestAbortedException anAbortedException = new TestAbortedException("in test");
		stub(child.execute(rootContext)).toThrow(anAbortedException);
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).beforeAll(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).execute(rootContext);
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());
		inOrder.verify(root).afterAll(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		assertTrue(childExecutionResult.getValue().getStatus() == TestExecutionResult.Status.ABORTED,
			"Execution of child should abort.");
		assertSame(childExecutionResult.getValue().getThrowable().get(), anAbortedException);
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

	}

	private static class MyLeaf extends AbstractTestDescriptor implements Leaf<MyEngineExecutionContext> {

		protected MyLeaf(String uniqueId) {
			super(uniqueId);
		}

		@Override
		public MyEngineExecutionContext execute(MyEngineExecutionContext context) throws Throwable {
			return context;
		}

		@Override
		public String getDisplayName() {
			return getUniqueId();
		}

		@Override
		public boolean isTest() {
			return true;
		}

		@Override
		public boolean isContainer() {
			return false;
		}
	}

	private static class MyExecutor extends HierarchicalTestExecutor<MyEngineExecutionContext> {

		MyExecutor(ExecutionRequest request, MyEngineExecutionContext rootContext) {
			super(request, rootContext);
		}
	}
}
