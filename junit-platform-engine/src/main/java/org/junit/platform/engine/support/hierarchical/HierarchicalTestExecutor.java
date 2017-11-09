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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.hierarchical.Node.SkipResult;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService.TestTask;

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

	HierarchicalTestExecutor(ExecutionRequest request, C rootContext, HierarchicalTestExecutorService<C> executorService) {
		this.rootTestDescriptor = request.getRootTestDescriptor();
		this.listener = request.getEngineExecutionListener();
		this.rootContext = rootContext;
		this.executorService = executorService;
	}

	void execute() {
		waitFor(executorService.submit(toTestTask(this.rootTestDescriptor, this.rootContext)));
	}

	private void execute(TestDescriptor testDescriptor, C parentContext) {
		Node<C> node = asNode(testDescriptor);

		C preparedContext;
		try {
			preparedContext = node.prepare(parentContext);
			SkipResult skipResult = node.shouldBeSkipped(preparedContext);
			if (skipResult.isSkipped()) {
				this.listener.executionSkipped(testDescriptor, skipResult.getReason().orElse("<unknown>"));
				return;
			}
		}
		catch (Throwable throwable) {
			rethrowIfBlacklisted(throwable);
			// We call executionStarted first to comply with the contract of EngineExecutionListener
			this.listener.executionStarted(testDescriptor);
			this.listener.executionFinished(testDescriptor, TestExecutionResult.failed(throwable));
			return;
		}

		this.listener.executionStarted(testDescriptor);

		TestExecutionResult result = singleTestExecutor.executeSafely(() -> {
			C context = preparedContext;
			try {
				context = node.before(context);

				Map<TestDescriptor, Future<?>> futures = new ConcurrentHashMap<>();

				C contextForDynamicChildren = context;
				context = node.execute(context, dynamicTestDescriptor -> {
					this.listener.dynamicTestRegistered(dynamicTestDescriptor);
					futures.put(dynamicTestDescriptor, executorService.submit(toTestTask(dynamicTestDescriptor, contextForDynamicChildren)));
				});

				C contextForStaticChildren = context;
				// @formatter:off
				testDescriptor.getChildren()
						.forEach(child -> futures.computeIfAbsent(child, d -> executorService.submit(toTestTask(child, contextForStaticChildren))));
				testDescriptor.getChildren().stream()
						.map(futures::get)
						.forEach(this::waitFor);
				// @formatter:on
			}
			finally {
				node.after(context);
			}
		});

		this.listener.executionFinished(testDescriptor, result);
	}

	private void waitFor(Future<?> future) {
		try {
            future.get();
        } catch (Exception e) {
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
            HierarchicalTestExecutor.this.execute(testDescriptor, context);
        }
	}
}
