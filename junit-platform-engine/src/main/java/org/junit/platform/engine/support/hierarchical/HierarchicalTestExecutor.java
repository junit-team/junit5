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

import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.junit.platform.commons.annotation.ExecutionMode;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService.TestTask;
import org.junit.platform.engine.support.hierarchical.Node.SkipResult;

/**
 * Implementation core of all {@link TestEngine TestEngines} that wish to
 * use the {@link Node} abstraction as the driving principle for structuring
 * and executing test suites.
 *
 * <p>A {@code HierarchicalTestExecutor} is instantiated by a concrete
 * implementation of {@link HierarchicalTestEngine} and takes care of
 * executing nodes in the hierarchy in the appropriate order as well as
 * firing the necessary events in the {@link EngineExecutionListener}.
 *
 * @param <C> the type of {@code EngineExecutionContext} used by the
 * {@code HierarchicalTestEngine}
 * @since 1.0
 */
class HierarchicalTestExecutor<C extends EngineExecutionContext> {

	private static final SingleTestExecutor singleTestExecutor = new SingleTestExecutor();

	private final TestDescriptor rootTestDescriptor;
	private final EngineExecutionListener listener;
	private final C rootContext;
	private HierarchicalTestExecutorService<C> executorService;

	HierarchicalTestExecutor(ExecutionRequest request, C rootContext,
			HierarchicalTestExecutorService<C> executorService) {
		this.rootTestDescriptor = request.getRootTestDescriptor();
		this.listener = request.getEngineExecutionListener();
		this.rootContext = rootContext;
		this.executorService = executorService;
	}

	void execute() {
		waitFor(executorService.submit(toTestTask(this.rootTestDescriptor, this.rootContext)));
	}

	class NodeExecutor {

		private final TestDescriptor testDescriptor;
		private final Node<C> node;
		private final List<Throwable> executionErrors = new ArrayList<>();
		private C context;
		private SkipResult skipResult;
		private TestExecutionResult executionResult;

		NodeExecutor(TestDescriptor testDescriptor) {
			this.testDescriptor = testDescriptor;
			node = asNode(testDescriptor);
		}

		void execute(C parentContext) {
			prepare(parentContext);
			if (executionErrors.isEmpty()) {
				checkWhetherSkipped();
			}
			if (executionErrors.isEmpty() && !skipResult.isSkipped()) {
				executeRecursively();
			}
			if (context != null) {
				cleanUp();
			}
			reportDone();
		}

		private void prepare(C parentContext) {
			try {
				context = node.prepare(parentContext);
			}
			catch (Throwable t) {
				addExecutionError(t);
			}
		}

		private void checkWhetherSkipped() {
			try {
				skipResult = node.shouldBeSkipped(context);
			}
			catch (Throwable t) {
				addExecutionError(t);
			}
		}

		private void executeRecursively() {
			listener.executionStarted(testDescriptor);

			executionResult = singleTestExecutor.executeSafely(() -> {
				try {
					context = node.before(context);

					Map<TestDescriptor, Future<?>> futures = new ConcurrentHashMap<>();

					context = node.execute(context, dynamicTestDescriptor -> {
						listener.dynamicTestRegistered(dynamicTestDescriptor);
						TestTask<C> testTask = toTestTask(dynamicTestDescriptor, context);
						futures.put(dynamicTestDescriptor, executorService.submit(testTask));
					});

					// @formatter:off
					testDescriptor.getChildren()
							.forEach(child -> futures.computeIfAbsent(child, d -> executorService.submit(toTestTask(child, context))));
					testDescriptor.getChildren().stream()
							.map(futures::get)
							.forEach(HierarchicalTestExecutor::waitFor);
					// @formatter:on
				}
				finally {
					node.after(context);
				}
			});
		}

		private void cleanUp() {
			try {
				node.cleanUp(context);
			}
			catch (Throwable t) {
				addExecutionError(t);
			}
		}

		private void reportDone() {
			if (executionResult != null) {
				addExecutionErrorsToTestExecutionResult();
				listener.executionFinished(testDescriptor, executionResult);
			}
			else if (executionErrors.isEmpty() && skipResult.isSkipped()) {
				listener.executionSkipped(testDescriptor, skipResult.getReason().orElse("<unknown>"));
			}
			else {
				// Call executionStarted first to comply with the contract of EngineExecutionListener.
				listener.executionStarted(testDescriptor);
				listener.executionFinished(testDescriptor, createTestExecutionResultFromExecutionErrors());
			}
		}

		private void addExecutionErrorsToTestExecutionResult() {
			if (executionErrors.isEmpty()) {
				return;
			}
			if (executionResult.getStatus() == FAILED && executionResult.getThrowable().isPresent()) {
				Throwable throwable = executionResult.getThrowable().get();
				executionErrors.forEach(throwable::addSuppressed);
			}
			else {
				executionResult = createTestExecutionResultFromExecutionErrors();
			}
		}

		private TestExecutionResult createTestExecutionResultFromExecutionErrors() {
			Throwable throwable = executionErrors.get(0);
			executionErrors.stream().skip(1).forEach(throwable::addSuppressed);
			return TestExecutionResult.failed(throwable);
		}

		private void addExecutionError(Throwable throwable) {
			rethrowIfBlacklisted(throwable);
			executionErrors.add(throwable);
		}
	}

	private static void waitFor(Future<?> future) {
		try {
			future.get();
		}
		catch (Exception e) {
			// TODO do something sensible
			throw ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	private TestTask<C> toTestTask(TestDescriptor testDescriptor, C context) {
		return new DefaultTestTask(testDescriptor, context);
	}

	@SuppressWarnings("unchecked")
	private Node<C> asNode(TestDescriptor testDescriptor) {
		return (testDescriptor instanceof Node ? (Node<C>) testDescriptor : noOpNode);
	}

	@SuppressWarnings("rawtypes")
	private static final Node noOpNode = new Node() {
	};

	private class DefaultTestTask implements TestTask<C> {

		private final TestDescriptor testDescriptor;
		private final C context;

		DefaultTestTask(TestDescriptor testDescriptor, C context) {
			this.testDescriptor = testDescriptor;
			this.context = context;
		}

		@Override
		public TestDescriptor getTestDescriptor() {
			return testDescriptor;
		}

		@Override
		public C getParentExecutionContext() {
			return context;
		}

		@Override
		public void execute() {
			new NodeExecutor(this.testDescriptor).execute(this.context);
		}

		@Override
		public List<ExclusiveResource> getExclusiveResources() {
			return asNode(testDescriptor).getExclusiveResources();
		}

		@Override
		public ExecutionMode getExecutionMode() {
			return asNode(testDescriptor).getExecutionMode();
		}
	}
}
