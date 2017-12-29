/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node.DynamicTestExecutor;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;
import org.opentest4j.TestAbortedException;

/**
 * Micro-tests that verify behavior of {@link HierarchicalTestExecutor}.
 *
 * @since 1.0
 */
class HierarchicalTestExecutorTests {

	MyContainer root;
	EngineExecutionListener listener;
	MyEngineExecutionContext rootContext;
	HierarchicalTestExecutor<MyEngineExecutionContext> executor;

	@BeforeEach
	void init() {
		root = spy(new MyContainer(UniqueId.root("container", "root")));
		listener = mock(EngineExecutionListener.class);
		rootContext = new MyEngineExecutionContext();
		ExecutionRequest request = new ExecutionRequest(root, listener, null);
		executor = new MyExecutor(request, rootContext);
	}

	@Test
	void emptyRootDescriptor() throws Exception {

		InOrder inOrder = inOrder(listener, root);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).before(rootContext);
		inOrder.verify(root).after(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());

		assertTrue(rootExecutionResult.getValue().getStatus() == TestExecutionResult.Status.SUCCESSFUL,
			"Execution of root should be successful.");
	}

	@Test
	void rootDescriptorWithOneChildContainer() throws Exception {

		MyContainer child = spy(new MyContainer(UniqueId.root("container", "child container")));
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).before(rootContext);
		inOrder.verify(child).after(rootContext);
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		assertTrue(childExecutionResult.getValue().getStatus() == TestExecutionResult.Status.SUCCESSFUL,
			"Execution of child container should be successful.");
	}

	@Test
	void rootDescriptorWithOneChildLeaf() throws Exception {

		MyLeaf child = spy(new MyLeaf(UniqueId.root("leaf", "child leaf")));
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> aTestExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).execute(eq(rootContext), any());
		inOrder.verify(listener).executionFinished(eq(child), aTestExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		assertTrue(aTestExecutionResult.getValue().getStatus() == TestExecutionResult.Status.SUCCESSFUL,
			"Execution of child leaf be successful.");
	}

	@Test
	void skippingAContainer() throws Exception {

		MyContainer child = spy(new MyContainer(UniqueId.root("container", "child container")));
		when(child.shouldBeSkipped(rootContext)).thenReturn(Node.SkipResult.skip("in test"));
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(child).cleanUp(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		verify(listener, never()).executionStarted(child);
		verifyNoMoreInteractions(child);
		verify(listener, never()).executionFinished(eq(child), any(TestExecutionResult.class));
	}

	@Test
	void skippingALeaf() throws Exception {

		MyLeaf child = spy(new MyLeaf(UniqueId.root("leaf", "child leaf")));
		when(child.shouldBeSkipped(rootContext)).thenReturn(Node.SkipResult.skip("in test"));
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(child).cleanUp(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		verify(listener, never()).executionStarted(child);
		verifyNoMoreInteractions(child);
		verify(listener, never()).executionFinished(eq(child), any(TestExecutionResult.class));
	}

	@Test
	void exceptionInShouldBeSkipped() throws Exception {

		MyContainer child = spy(new MyContainer(UniqueId.root("container", "child container")));
		RuntimeException anException = new RuntimeException("in skip");
		when(child.shouldBeSkipped(rootContext)).thenThrow(anException);
		root.addChild(child);

		InOrder inOrder = inOrder(listener, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(child).cleanUp(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		verifyNoMoreInteractions(child);

		assertTrue(childExecutionResult.getValue().getStatus() == TestExecutionResult.Status.FAILED,
			"Execution of child should fail.");
		assertSame(childExecutionResult.getValue().getThrowable().get(), anException);
	}

	@Test
	void exceptionInContainerBeforeAll() throws Exception {

		MyContainer child = spy(new MyContainer(UniqueId.root("container", "child container")));
		root.addChild(child);
		RuntimeException anException = new RuntimeException("in test");
		when(root.before(rootContext)).thenThrow(anException);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).before(rootContext);
		inOrder.verify(root).after(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());

		assertTrue(rootExecutionResult.getValue().getStatus() == TestExecutionResult.Status.FAILED,
			"Execution of root should fail.");
		assertSame(rootExecutionResult.getValue().getThrowable().get(), anException);

		verifyNoMoreInteractions(child);
	}

	@Test
	void exceptionInContainerAfterAllAndCleanUp() throws Exception {

		MyLeaf child = spy(new MyLeaf(UniqueId.root("leaf", "child container")));
		root.addChild(child);
		RuntimeException afterException = new RuntimeException("in after()");
		doThrow(afterException).when(root).after(rootContext);
		RuntimeException cleanUpException = new RuntimeException("in cleanUp()");
		doThrow(cleanUpException).when(root).cleanUp(rootContext);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		InOrder inOrder = inOrder(listener, root, child);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).before(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).execute(eq(rootContext), any());
		inOrder.verify(listener).executionFinished(eq(child), any(TestExecutionResult.class));
		inOrder.verify(root).after(rootContext);
		inOrder.verify(root).cleanUp(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());
		inOrder.verifyNoMoreInteractions();

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(TestExecutionResult.Status.FAILED);
		assertThat(rootExecutionResult.getValue().getThrowable()).containsSame(afterException);
		assertThat(afterException.getSuppressed()).containsExactly(cleanUpException);
	}

	@Test
	void exceptionInPrepare() throws Exception {
		RuntimeException prepareException = new RuntimeException("in prepare()");
		doThrow(prepareException).when(root).prepare(rootContext);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		InOrder inOrder = inOrder(listener, root);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());
		inOrder.verifyNoMoreInteractions();

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(TestExecutionResult.Status.FAILED);
		assertThat(rootExecutionResult.getValue().getThrowable()).containsSame(prepareException);
		assertThat(prepareException.getSuppressed()).isEmpty();
	}

	@Test
	void exceptionInCleanUp() throws Exception {
		RuntimeException cleanUpException = new RuntimeException("in cleanUp()");
		doThrow(cleanUpException).when(root).cleanUp(rootContext);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		InOrder inOrder = inOrder(listener, root);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).execute(eq(rootContext), any());
		inOrder.verify(root).after(rootContext);
		inOrder.verify(root).cleanUp(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());
		inOrder.verifyNoMoreInteractions();

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(TestExecutionResult.Status.FAILED);
		assertThat(rootExecutionResult.getValue().getThrowable()).containsSame(cleanUpException);
		assertThat(cleanUpException.getSuppressed()).isEmpty();
	}

	@Test
	void exceptionInShouldBeSkippedAndCleanUp() throws Exception {
		RuntimeException shouldBeSkippedException = new RuntimeException("in prepare()");
		doThrow(shouldBeSkippedException).when(root).shouldBeSkipped(rootContext);
		RuntimeException cleanUpException = new RuntimeException("in cleanUp()");
		doThrow(cleanUpException).when(root).cleanUp(rootContext);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		InOrder inOrder = inOrder(listener, root);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(root).cleanUp(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());
		inOrder.verifyNoMoreInteractions();

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(TestExecutionResult.Status.FAILED);
		assertThat(rootExecutionResult.getValue().getThrowable()).containsSame(shouldBeSkippedException);
		assertThat(shouldBeSkippedException.getSuppressed()).containsExactly(cleanUpException);
	}

	@Test
	void exceptionInLeafExecute() throws Exception {

		MyLeaf child = spy(new MyLeaf(UniqueId.root("leaf", "leaf")));
		RuntimeException anException = new RuntimeException("in test");
		when(child.execute(eq(rootContext), any())).thenThrow(anException);
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).before(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).execute(eq(rootContext), any());
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());
		inOrder.verify(root).after(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		assertTrue(childExecutionResult.getValue().getStatus() == TestExecutionResult.Status.FAILED,
			"Execution of child should fail.");
		assertSame(childExecutionResult.getValue().getThrowable().get(), anException);
	}

	@Test
	void abortInContainerBeforeAll() throws Exception {

		MyContainer child = spy(new MyContainer(UniqueId.root("container", "child container")));
		root.addChild(child);
		TestAbortedException anAbortedException = new TestAbortedException("in BeforeAll");
		when(root.before(rootContext)).thenThrow(anAbortedException);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).before(rootContext);
		inOrder.verify(root).after(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), rootExecutionResult.capture());

		assertTrue(rootExecutionResult.getValue().getStatus() == TestExecutionResult.Status.ABORTED,
			"Execution of root should abort.");
		assertSame(rootExecutionResult.getValue().getThrowable().get(), anAbortedException);

		verifyNoMoreInteractions(child);
	}

	@Test
	void abortInLeafExecute() throws Exception {

		MyLeaf child = spy(new MyLeaf(UniqueId.root("leaf", "leaf")));
		TestAbortedException anAbortedException = new TestAbortedException("in test");
		when(child.execute(eq(rootContext), any())).thenThrow(anAbortedException);
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).before(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).execute(eq(rootContext), any());
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());
		inOrder.verify(root).after(rootContext);
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		assertTrue(childExecutionResult.getValue().getStatus() == TestExecutionResult.Status.ABORTED,
			"Execution of child should abort.");
		assertSame(childExecutionResult.getValue().getThrowable().get(), anAbortedException);
	}

	@Test
	void executesDynamicTestDescriptors() throws Exception {

		UniqueId leafUniqueId = UniqueId.root("leaf", "child leaf");
		MyLeaf child = spy(new MyLeaf(leafUniqueId));
		MyLeaf dynamicTestDescriptor = spy(new MyLeaf(leafUniqueId.append("dynamic", "child")));

		when(child.execute(any(), any())).thenAnswer(invocation -> {
			DynamicTestExecutor dynamicTestExecutor = invocation.getArgument(1);
			dynamicTestExecutor.execute(dynamicTestDescriptor);
			return invocation.getArgument(0);
		});
		root.addChild(child);

		InOrder inOrder = inOrder(listener, root, child, dynamicTestDescriptor);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> aTestExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).execute(eq(rootContext), any());
		inOrder.verify(listener).dynamicTestRegistered(dynamicTestDescriptor);
		inOrder.verify(dynamicTestDescriptor).prepare(rootContext);
		inOrder.verify(dynamicTestDescriptor).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(dynamicTestDescriptor);
		inOrder.verify(dynamicTestDescriptor).execute(eq(rootContext), any());
		inOrder.verify(listener).executionFinished(eq(dynamicTestDescriptor), aTestExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(child), aTestExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		assertThat(aTestExecutionResult.getAllValues()).extracting(TestExecutionResult::getStatus).containsExactly(
			TestExecutionResult.Status.SUCCESSFUL, TestExecutionResult.Status.SUCCESSFUL);
	}

	@Test
	void executesDynamicTestDescriptorsUsingContainerAndTestType() throws Exception {

		MyContainerAndTest child = spy(new MyContainerAndTest(root.getUniqueId().append("c&t", "child")));
		MyContainerAndTest dynamicContainerAndTest = spy(
			new MyContainerAndTest(child.getUniqueId().append("c&t", "dynamicContainerAndTest")));
		MyLeaf dynamicLeaf = spy(new MyLeaf(dynamicContainerAndTest.getUniqueId().append("test", "dynamicLeaf")));

		root.addChild(child);
		when(child.execute(any(), any())).thenAnswer(registerAndExecute(dynamicContainerAndTest));
		when(dynamicContainerAndTest.execute(any(), any())).thenAnswer(registerAndExecute(dynamicLeaf));
		when(dynamicLeaf.execute(any(), any())).thenAnswer(invocation -> {
			throw new AssertionError("test fails");
		});

		InOrder inOrder = inOrder(listener, root, child, dynamicContainerAndTest, dynamicLeaf);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> aTestExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(listener).executionStarted(root);

		inOrder.verify(child).prepare(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(child);
		inOrder.verify(child).execute(eq(rootContext), any());

		inOrder.verify(listener).dynamicTestRegistered(dynamicContainerAndTest);
		inOrder.verify(dynamicContainerAndTest).prepare(rootContext);
		inOrder.verify(dynamicContainerAndTest).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(dynamicContainerAndTest);
		inOrder.verify(dynamicContainerAndTest).execute(eq(rootContext), any());

		inOrder.verify(listener).dynamicTestRegistered(dynamicLeaf);
		inOrder.verify(dynamicLeaf).prepare(rootContext);
		inOrder.verify(dynamicLeaf).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(dynamicLeaf);
		inOrder.verify(dynamicLeaf).execute(eq(rootContext), any());

		inOrder.verify(listener).executionFinished(eq(dynamicLeaf), aTestExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(dynamicContainerAndTest), aTestExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(child), aTestExecutionResult.capture());
		inOrder.verify(listener).executionFinished(eq(root), any(TestExecutionResult.class));

		assertThat(aTestExecutionResult.getAllValues()).extracting(TestExecutionResult::getStatus).containsExactly(
			TestExecutionResult.Status.FAILED, TestExecutionResult.Status.SUCCESSFUL,
			TestExecutionResult.Status.SUCCESSFUL);
	}

	private Answer<Object> registerAndExecute(TestDescriptor dynamicChild) {
		return invocation -> {
			DynamicTestExecutor dynamicTestExecutor = invocation.getArgument(1);
			dynamicTestExecutor.execute(dynamicChild);
			return invocation.getArgument(0);
		};
	}

	/**
	 * Verifies support for blacklisted exceptions.
	 */
	@Test
	void outOfMemoryErrorInShouldBeSkipped() throws Exception {
		MyContainer child = spy(new MyContainer(UniqueId.root("container", "child container")));
		OutOfMemoryError outOfMemoryError = new OutOfMemoryError("in skip");
		when(child.shouldBeSkipped(rootContext)).thenThrow(outOfMemoryError);
		root.addChild(child);

		Throwable actualException = assertThrows(OutOfMemoryError.class, () -> executor.execute());
		assertSame(outOfMemoryError, actualException);
	}

	/**
	 * Verifies support for blacklisted exceptions.
	 */
	@Test
	void outOfMemoryErrorInLeafExecution() throws Exception {
		MyLeaf child = spy(new MyLeaf(UniqueId.root("leaf", "leaf")));
		OutOfMemoryError outOfMemoryError = new OutOfMemoryError("in test");
		when(child.execute(eq(rootContext), any())).thenThrow(outOfMemoryError);
		root.addChild(child);

		Throwable actualException = assertThrows(OutOfMemoryError.class, () -> executor.execute());
		assertSame(outOfMemoryError, actualException);
	}

	// -------------------------------------------------------------------

	private static class MyEngineExecutionContext implements EngineExecutionContext {
	}

	private static class MyContainer extends AbstractTestDescriptor implements Node<MyEngineExecutionContext> {

		MyContainer(UniqueId uniqueId) {
			super(uniqueId, uniqueId.toString());
		}

		@Override
		public Type getType() {
			return Type.CONTAINER;
		}
	}

	private static class MyLeaf extends AbstractTestDescriptor implements Node<MyEngineExecutionContext> {

		MyLeaf(UniqueId uniqueId) {
			super(uniqueId, uniqueId.toString());
		}

		@Override
		public MyEngineExecutionContext execute(MyEngineExecutionContext context,
				DynamicTestExecutor dynamicTestExecutor) throws Exception {
			return context;
		}

		@Override
		public Type getType() {
			return Type.TEST;
		}
	}

	private static class MyContainerAndTest extends AbstractTestDescriptor implements Node<MyEngineExecutionContext> {

		MyContainerAndTest(UniqueId uniqueId) {
			super(uniqueId, uniqueId.toString());
		}

		@Override
		public Type getType() {
			return Type.CONTAINER_AND_TEST;
		}
	}

	private static class MyExecutor extends HierarchicalTestExecutor<MyEngineExecutionContext> {

		MyExecutor(ExecutionRequest request, MyEngineExecutionContext rootContext) {
			super(request, rootContext);
		}
	}

}
