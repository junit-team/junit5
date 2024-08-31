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

import static java.lang.String.format;
import static java.time.Duration.ofMinutes;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apiguardian.api.API.Status.STABLE;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * A {@linkplain HierarchicalTestExecutorService executor service} that creates a new thread for
 * each test class, all {@linkplain TestTask test tasks}.
 *
 * <p>This execution model is useful to prevent some kinds of class / class-loader leaks. For
 * example, if a test creates {@link ClassLoader}s and the tests or any of the code and libraries
 * create {@link ThreadLocal}s, those thread locals would accumulate in the single {@link
 * SameThreadHierarchicalTestExecutorService} causing a class-(loader)-leak.
 *
 * @since 5.12
 */
@API(status = STABLE, since = "5.12")
public class ThreadPerClassHierarchicalTestExecutorService implements HierarchicalTestExecutorService {

	private final AtomicInteger threadCount = new AtomicInteger();
	private final Duration interruptWaitDuration;

	static final Duration DEFAULT_INTERRUPT_WAIT_DURATION = ofMinutes(5);
	static final String THREAD_PER_CLASS_INTERRUPTED_WAIT_TIME_SECONDS = "junit.jupiter.execution.threadperclass.interrupted.waittime.seconds";

	public ThreadPerClassHierarchicalTestExecutorService(ConfigurationParameters config) {
		interruptWaitDuration = config.get(THREAD_PER_CLASS_INTERRUPTED_WAIT_TIME_SECONDS).map(Integer::parseInt).map(
			Duration::ofSeconds).orElse(DEFAULT_INTERRUPT_WAIT_DURATION);
	}

	@Override
	public Future<Void> submit(TestTask testTask) {
		executeTask(testTask);
		return completedFuture(null);
	}

	@Override
	public void invokeAll(List<? extends TestTask> tasks) {
		tasks.forEach(this::executeTask);
	}

	protected void executeTask(TestTask testTask) {
		NodeTestTask<?> nodeTestTask = (NodeTestTask<?>) testTask;
		TestDescriptor testDescriptor = nodeTestTask.getTestDescriptor();

		UniqueId.Segment lastSegment = testDescriptor.getUniqueId().getLastSegment();

		if ("class".equals(lastSegment.getType())) {
			executeOnDifferentThread(testTask, lastSegment);
		}
		else {
			testTask.execute();
		}
	}

	private void executeOnDifferentThread(TestTask testTask, UniqueId.Segment lastSegment) {
		CompletableFuture<Object> future = new CompletableFuture<>();
		Thread threadPerClass = new Thread(() -> {
			try {
				testTask.execute();
				future.complete(null);
			}
			catch (Exception e) {
				future.completeExceptionally(e);
			}
		}, threadName(lastSegment));
		threadPerClass.setDaemon(true);
		threadPerClass.start();

		try {
			try {
				future.get();
			}
			catch (InterruptedException e) {
				// propagate a thread-interrupt to the executing class
				threadPerClass.interrupt();
				try {
					future.get(interruptWaitDuration.toMillis(), MILLISECONDS);
				}
				catch (InterruptedException ie) {
					threadPerClass.interrupt();
				}
				catch (TimeoutException to) {
					throw new JUnitException(format("Test class %s was interrupted but did not terminate within %s",
						lastSegment.getValue(), interruptWaitDuration), to);
				}
			}
		}
		catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw new JUnitException("TestTask execution failure", cause);
		}
	}

	private String threadName(UniqueId.Segment lastSegment) {
		return format("TEST THREAD #%d FOR %s", threadCount.incrementAndGet(), lastSegment.getValue());
	}

	@Override
	public void close() {
		// nothing to do
	}
}
