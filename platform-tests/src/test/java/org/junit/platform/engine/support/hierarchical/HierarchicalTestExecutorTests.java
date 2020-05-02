/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;
import static org.junit.platform.engine.TestExecutionResult.successful;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;
import org.junit.platform.engine.support.hierarchical.Node.DynamicTestExecutor;
import org.junit.platform.launcher.core.ConfigurationParametersFactoryForTests;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.opentest4j.TestAbortedException;

/**
 * Micro-tests that verify behavior of {@link HierarchicalTestExecutor}.
 *
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class HierarchicalTestExecutorTests {

	@Spy
	MyContainer root = new MyContainer(UniqueId.root("container", "root"));

	@Mock
	EngineExecutionListener listener;

	MyEngineExecutionContext rootContext = new MyEngineExecutionContext();
	HierarchicalTestExecutor<MyEngineExecutionContext> executor;

	@BeforeEach
	void init() {
		executor = createExecutor(new SameThreadHierarchicalTestExecutorService());
	}

	private HierarchicalTestExecutor<MyEngineExecutionContext> createExecutor(
			HierarchicalTestExecutorService executorService) {
		ExecutionRequest request = new ExecutionRequest(root, listener, null);
		return new HierarchicalTestExecutor<>(request, rootContext, executorService,
			OpenTest4JAwareThrowableCollector::new);
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

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(SUCCESSFUL);
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

		assertThat(childExecutionResult.getValue().getStatus()).isEqualTo(SUCCESSFUL);
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

		assertThat(aTestExecutionResult.getValue().getStatus()).isEqualTo(SUCCESSFUL);
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
		verify(child, never()).execute(any(), any());
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
		verify(child, never()).execute(any(), any());
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

		verify(child, never()).execute(any(), any());

		assertThat(childExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
		assertThat(childExecutionResult.getValue().getThrowable()).containsSame(anException);
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

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
		assertThat(rootExecutionResult.getValue().getThrowable()).containsSame(anException);

		verify(child, never()).execute(any(), any());
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

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
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

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
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

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
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

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
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

		assertThat(childExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
		assertThat(childExecutionResult.getValue().getThrowable()).containsSame(anException);
	}

	@Test
	void abortInRootBeforeAll() throws Exception {

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

		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(ABORTED);
		assertThat(rootExecutionResult.getValue().getThrowable()).containsSame(anAbortedException);

		verify(child, never()).execute(any(), any());
	}

	@Test
	void abortInChildContainerBeforeAll() throws Exception {

		MyContainer child = spy(new MyContainer(UniqueId.root("container", "child container")));
		root.addChild(child);
		TestAbortedException anAbortedException = new TestAbortedException("in BeforeAll");
		when(child.before(rootContext)).thenThrow(anAbortedException);

		InOrder inOrder = inOrder(listener, root, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);

		inOrder.verify(root).prepare(rootContext);
		inOrder.verify(root).shouldBeSkipped(rootContext);
		inOrder.verify(listener).executionStarted(root);
		inOrder.verify(root).before(rootContext);
		inOrder.verify(child).shouldBeSkipped(rootContext);
		inOrder.verify(child).before(rootContext);
		inOrder.verify(child).after(rootContext);
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());
		inOrder.verify(root).after(rootContext);

		assertThat(childExecutionResult.getValue().getStatus()).isEqualTo(ABORTED);
		assertThat(childExecutionResult.getValue().getThrowable()).containsSame(anAbortedException);

		verify(child, never()).execute(any(), any());
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

		assertThat(childExecutionResult.getValue().getStatus()).isEqualTo(ABORTED);
		assertThat(childExecutionResult.getValue().getThrowable()).containsSame(anAbortedException);
	}

	@Test
	void executesDynamicTestDescriptors() throws Exception {

		UniqueId leafUniqueId = UniqueId.root("leaf", "child leaf");
		MyLeaf child = spy(new MyLeaf(leafUniqueId));
		MyLeaf dynamicTestDescriptor = spy(new MyLeaf(leafUniqueId.append("dynamic", "child")));

		when(child.execute(any(), any())).thenAnswer(execute(dynamicTestDescriptor));
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
			SUCCESSFUL, SUCCESSFUL);
	}

	@Test
	void executesDynamicTestDescriptorsUsingContainerAndTestType() throws Exception {

		MyContainerAndTestTestCase child = spy(
			new MyContainerAndTestTestCase(root.getUniqueId().append("c&t", "child")));
		MyContainerAndTestTestCase dynamicContainerAndTest = spy(
			new MyContainerAndTestTestCase(child.getUniqueId().append("c&t", "dynamicContainerAndTest")));
		MyLeaf dynamicLeaf = spy(new MyLeaf(dynamicContainerAndTest.getUniqueId().append("test", "dynamicLeaf")));

		root.addChild(child);
		when(child.execute(any(), any())).thenAnswer(execute(dynamicContainerAndTest));
		when(dynamicContainerAndTest.execute(any(), any())).thenAnswer(execute(dynamicLeaf));
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
			FAILED, SUCCESSFUL, SUCCESSFUL);
	}

	@Test
	void executesDynamicTestDescriptorsWithCustomListener() {

		UniqueId leafUniqueId = UniqueId.root("leaf", "child leaf");
		MyLeaf child = spy(new MyLeaf(leafUniqueId));
		MyLeaf dynamicTestDescriptor = spy(new MyLeaf(leafUniqueId.append("dynamic", "child")));
		root.addChild(child);

		EngineExecutionListener anotherListener = mock(EngineExecutionListener.class);
		when(child.execute(any(), any())).thenAnswer(
			useDynamicTestExecutor(executor -> executor.execute(dynamicTestDescriptor, anotherListener)));

		executor.execute();

		InOrder inOrder = inOrder(listener, anotherListener, root, child, dynamicTestDescriptor);
		inOrder.verify(anotherListener).dynamicTestRegistered(dynamicTestDescriptor);
		inOrder.verify(anotherListener).executionStarted(dynamicTestDescriptor);
		inOrder.verify(dynamicTestDescriptor).execute(eq(rootContext), any());
		inOrder.verify(dynamicTestDescriptor).nodeFinished(rootContext, dynamicTestDescriptor, successful());
		inOrder.verify(anotherListener).executionFinished(dynamicTestDescriptor, successful());
	}

	@Test
	void canAbortExecutionOfDynamicChild() throws Exception {

		UniqueId leafUniqueId = UniqueId.root("leaf", "child leaf");
		MyLeaf child = spy(new MyLeaf(leafUniqueId));
		MyLeaf dynamicTestDescriptor = spy(new MyLeaf(leafUniqueId.append("dynamic", "child")));
		root.addChild(child);

		var startedLatch = new CountDownLatch(1);
		var interrupted = new CompletableFuture<Boolean>();

		when(child.execute(any(), any())).thenAnswer(useDynamicTestExecutor(executor -> {
			Future<?> future = executor.execute(dynamicTestDescriptor, EngineExecutionListener.NOOP);
			startedLatch.await();
			future.cancel(true);
			executor.awaitFinished();
		}));
		when(dynamicTestDescriptor.execute(any(), any())).thenAnswer(invocation -> {
			startedLatch.countDown();
			try {
				new CountDownLatch(1).await(); // block until interrupted
				interrupted.complete(false);
				return null;
			}
			catch (InterruptedException e) {
				interrupted.complete(true);
				throw e;
			}
		});

		ConfigurationParameters parameters = ConfigurationParametersFactoryForTests.create(Map.of(//
			DefaultParallelExecutionConfigurationStrategy.CONFIG_STRATEGY_PROPERTY_NAME, "fixed", //
			DefaultParallelExecutionConfigurationStrategy.CONFIG_FIXED_PARALLELISM_PROPERTY_NAME, "2"));

		try (var executorService = new ForkJoinPoolHierarchicalTestExecutorService(parameters)) {
			createExecutor(executorService).execute().get();
		}

		verify(listener).executionFinished(child, successful());
		assertTrue(interrupted.get(), "dynamic node was interrupted");
	}

	private Answer<Object> execute(TestDescriptor dynamicChild) {
		return useDynamicTestExecutor(executor -> executor.execute(dynamicChild));
	}

	private Answer<Object> useDynamicTestExecutor(ThrowingConsumer<DynamicTestExecutor> action) {
		return invocation -> {
			DynamicTestExecutor dynamicTestExecutor = invocation.getArgument(1);
			action.accept(dynamicTestExecutor);
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
	void outOfMemoryErrorInLeafExecution() {
		MyLeaf child = spy(new MyLeaf(UniqueId.root("leaf", "leaf")));
		OutOfMemoryError outOfMemoryError = new OutOfMemoryError("in test");
		when(child.execute(eq(rootContext), any())).thenThrow(outOfMemoryError);
		root.addChild(child);

		Throwable actualException = assertThrows(OutOfMemoryError.class, () -> executor.execute());
		assertSame(outOfMemoryError, actualException);
	}

	@Test
	void exceptionInAfterDoesNotHideEarlierException() throws Exception {

		MyLeaf child = spy(new MyLeaf(UniqueId.root("leaf", "leaf")));
		Exception exceptionInExecute = new RuntimeException("execute");
		Exception exceptionInAfter = new RuntimeException("after");
		doThrow(exceptionInExecute).when(child).execute(eq(rootContext), any());
		doThrow(exceptionInAfter).when(child).after(eq(rootContext));
		root.addChild(child);

		InOrder inOrder = inOrder(listener, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(child).execute(eq(rootContext), any());
		inOrder.verify(child).after(eq(rootContext));
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());

		assertThat(childExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
		assertThat(childExecutionResult.getValue().getThrowable().get()).isSameAs(
			exceptionInExecute).hasSuppressedException(exceptionInAfter);
	}

	@Test
	void dynamicTestDescriptorsMustNotDeclareExclusiveResources() {

		UniqueId leafUniqueId = UniqueId.root("leaf", "child leaf");
		MyLeaf child = spy(new MyLeaf(leafUniqueId));
		MyLeaf dynamicTestDescriptor = spy(new MyLeaf(leafUniqueId.append("dynamic", "child")));
		when(dynamicTestDescriptor.getExclusiveResources()).thenReturn(
			singleton(new ExclusiveResource("foo", LockMode.READ)));

		when(child.execute(any(), any())).thenAnswer(execute(dynamicTestDescriptor));
		root.addChild(child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> aTestExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		verify(listener).executionStarted(dynamicTestDescriptor);
		verify(listener).executionFinished(eq(dynamicTestDescriptor), aTestExecutionResult.capture());

		TestExecutionResult executionResult = aTestExecutionResult.getValue();
		assertThat(executionResult.getStatus()).isEqualTo(FAILED);
		assertThat(executionResult.getThrowable()).isPresent();
		assertThat(executionResult.getThrowable().get()).hasMessageContaining(
			"Dynamic test descriptors must not declare exclusive resources");
	}

	@Test
	void exceptionInAfterIsReportedInsteadOfEarlierTestAbortedException() throws Exception {

		MyLeaf child = spy(new MyLeaf(UniqueId.root("leaf", "leaf")));
		Exception exceptionInExecute = new TestAbortedException("execute");
		Exception exceptionInAfter = new RuntimeException("after");
		doThrow(exceptionInExecute).when(child).execute(eq(rootContext), any());
		doThrow(exceptionInAfter).when(child).after(eq(rootContext));
		root.addChild(child);

		InOrder inOrder = inOrder(listener, child);

		executor.execute();

		ArgumentCaptor<TestExecutionResult> childExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		inOrder.verify(child).execute(eq(rootContext), any());
		inOrder.verify(child).after(eq(rootContext));
		inOrder.verify(listener).executionFinished(eq(child), childExecutionResult.capture());

		assertThat(childExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
		assertThat(childExecutionResult.getValue().getThrowable().get()).isSameAs(
			exceptionInAfter).hasSuppressedException(exceptionInExecute);
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
				DynamicTestExecutor dynamicTestExecutor) {
			return context;
		}

		@Override
		public Type getType() {
			return Type.TEST;
		}
	}

	private static class MyContainerAndTestTestCase extends AbstractTestDescriptor
			implements Node<MyEngineExecutionContext> {

		MyContainerAndTestTestCase(UniqueId uniqueId) {
			super(uniqueId, uniqueId.toString());
		}

		@Override
		public Type getType() {
			return Type.CONTAINER_AND_TEST;
		}
	}

}
