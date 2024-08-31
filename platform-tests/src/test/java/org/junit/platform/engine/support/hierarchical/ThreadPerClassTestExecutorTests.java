/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Micro-tests that verify behavior of {@link HierarchicalTestExecutor}.
 *
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class ThreadPerClassTestExecutorTests {

	@Spy
	MyContainer root = new MyContainer(UniqueId.root("container", "root"));

	@Mock
	EngineExecutionListener listener;

	MyEngineExecutionContext rootContext = new MyEngineExecutionContext();
	HierarchicalTestExecutor<MyEngineExecutionContext> executor;

	Duration testMaxWaitTime = Duration.ofMinutes(5);

	@BeforeEach
	void init() {
		executor = createExecutor(
			new ThreadPerClassHierarchicalTestExecutorService(new EmptyConfigurationParameters()));
	}

	private HierarchicalTestExecutor<MyEngineExecutionContext> createExecutor(
			HierarchicalTestExecutorService executorService) {
		var request = new ExecutionRequest(root, listener, null);
		return new HierarchicalTestExecutor<>(request, rootContext, executorService,
			OpenTest4JAwareThrowableCollector::new);
	}

	@Test
	void failures() throws Exception {
		var rootId = UniqueId.root("engine", "my engine");
		var container = spy(new MyContainer(rootId));
		root.addChild(container);

		var clazzId1 = rootId.append("class", "my.Class");
		var clazz1 = spy(new MyContainer(clazzId1));
		container.addChild(clazz1);
		var failure1 = new AssertionError("something went wrong");
		var thread1 = new AtomicReference<Thread>();
		when(clazz1.execute(any(), any())).then(inv -> {
			thread1.set(Thread.currentThread());
			throw failure1;
		});

		var clazzId2 = rootId.append("class", "my.OtherClass");
		var clazz2 = spy(new MyContainer(clazzId2));
		container.addChild(clazz2);
		var failure2 = new AssertionError("something went wrong");
		var thread2 = new AtomicReference<Thread>();
		when(clazz2.execute(any(), any())).then(inv -> {
			thread2.set(Thread.currentThread());
			throw failure2;
		});

		executor.execute().get();

		assertThat(thread1.get()).isNotNull().isNotSameAs(Thread.currentThread()).extracting(Thread::getName,
			STRING).startsWith("TEST THREAD ").contains(" FOR my.Class");
		assertThat(thread1.get().join(testMaxWaitTime)).isTrue();
		assertThat(thread1.get().isAlive()).isFalse();
		assertThat(thread1.get().isDaemon()).isTrue();

		assertThat(thread2.get()).isNotNull().isNotSameAs(Thread.currentThread()).isNotSameAs(thread1.get()).extracting(
			Thread::getName, STRING).startsWith("TEST THREAD ").contains(" FOR my.OtherClass");
		assertThat(thread2.get().join(testMaxWaitTime)).isTrue();
		assertThat(thread2.get().isAlive()).isFalse();
		assertThat(thread2.get().isDaemon()).isTrue();

		var rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		verify(listener).executionFinished(eq(clazz1), rootExecutionResult.capture());
		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
		assertThat(rootExecutionResult.getValue().getThrowable()).get().isInstanceOf(AssertionError.class).asInstanceOf(
			type(AssertionError.class)).extracting(AssertionError::getMessage).isEqualTo("something went wrong");
	}

	@Test
	void goodResults() throws Exception {
		var rootId = UniqueId.root("engine", "my engine");
		var container = spy(new MyContainer(rootId));
		root.addChild(container);

		var clazzId1 = rootId.append("class", "my.Class");
		var clazz1 = spy(new MyContainer(clazzId1));
		container.addChild(clazz1);
		var thread1 = new AtomicReference<Thread>();
		when(clazz1.execute(any(), any())).then(inv -> {
			thread1.set(Thread.currentThread());
			return inv.callRealMethod();
		});

		var clazzId2 = rootId.append("class", "my.OtherClass");
		var clazz2 = spy(new MyContainer(clazzId2));
		container.addChild(clazz2);
		var failure2 = new AssertionError("something went wrong");
		var thread2 = new AtomicReference<Thread>();
		when(clazz2.execute(any(), any())).then(inv -> {
			thread2.set(Thread.currentThread());
			throw failure2;
		});

		executor.execute().get();

		assertThat(thread1.get()).isNotNull().isNotSameAs(Thread.currentThread()).extracting(Thread::getName,
			STRING).startsWith("TEST THREAD ").contains(" FOR my.Class");
		assertThat(thread1.get().join(testMaxWaitTime)).isTrue();
		assertThat(thread1.get().isAlive()).isFalse();
		assertThat(thread1.get().isDaemon()).isTrue();

		assertThat(thread2.get()).isNotNull().isNotSameAs(Thread.currentThread()).isNotSameAs(thread1.get()).extracting(
			Thread::getName, STRING).startsWith("TEST THREAD ").contains(" FOR my.OtherClass");
		assertThat(thread2.get().join(testMaxWaitTime)).isTrue();
		assertThat(thread2.get().isAlive()).isFalse();
		assertThat(thread2.get().isDaemon()).isTrue();

		var rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		verify(listener).executionFinished(eq(clazz1), rootExecutionResult.capture());
		assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(SUCCESSFUL);
		assertThat(rootExecutionResult.getValue().getThrowable()).isEmpty();
	}

	/** Check that {@code Thread.interrupt()} is propagated to the test execution. */
	@Test
	void interrupt() throws Exception {
		var rootId = UniqueId.root("engine", "my engine");
		var container = spy(new MyContainer(rootId));
		root.addChild(container);
		var clazzId = rootId.append("class", "my.Class");
		var clazz = spy(new MyContainer(clazzId));
		container.addChild(clazz);
		var thread = new AtomicReference<Thread>();

		// latch to wait for that "our test" is currently running
		var sleeping = new CountDownLatch(1);
		var interruptedHandled = new CountDownLatch(1);

		when(clazz.execute(any(), any())).then(inv -> {
			thread.set(Thread.currentThread());
			try {
				sleeping.countDown();
				Thread.sleep(testMaxWaitTime);
			}
			catch (InterruptedException e) {
				interruptedHandled.countDown();
				throw new RuntimeException(e);
			}
			return fail();
		});

		Future<Void> future;
		try (var executorService = Executors.newSingleThreadExecutor()) {
			future = executorService.submit(() -> executor.execute().get());

			// wait until "our test" is running
			assertThat(sleeping.await(testMaxWaitTime.toMillis(), MILLISECONDS)).isTrue();

			// Interrupt the executor
			future.cancel(true);

			// wait for the "test execution" to finish and being interrupted
			assertThatThrownBy(() -> future.get(testMaxWaitTime.toMillis(), MILLISECONDS)).isInstanceOf(
				CancellationException.class);

			assertThat(interruptedHandled.await(testMaxWaitTime.toMillis(), MILLISECONDS)).isTrue();

			assertThat(thread.get()).isNotNull().isNotSameAs(Thread.currentThread()).extracting(Thread::getName,
				STRING).startsWith("TEST THREAD ").contains(" FOR my.Class");
			assertThat(thread.get().join(testMaxWaitTime)).isTrue();
			assertThat(thread.get().isAlive()).isFalse();
			assertThat(thread.get().isDaemon()).isTrue();

			var rootExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
			verify(listener).executionFinished(eq(clazz), rootExecutionResult.capture());
			assertThat(rootExecutionResult.getValue().getStatus()).isEqualTo(FAILED);
			assertThat(rootExecutionResult.getValue().getThrowable()).get().isInstanceOf(
				RuntimeException.class).extracting(Throwable::getCause).isInstanceOf(InterruptedException.class);
		}
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

	private static class EmptyConfigurationParameters implements ConfigurationParameters {
		@Override
		public Optional<String> get(String key) {
			return Optional.empty();
		}

		@Override
		public Optional<Boolean> getBoolean(String key) {
			return Optional.empty();
		}

		@Override
		@SuppressWarnings("deprecation")
		public int size() {
			return 0;
		}

		@Override
		public Set<String> keySet() {
			return Collections.emptySet();
		}

	}
}
