/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
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

	HierarchicalTestExecutor(ExecutionRequest request, C rootContext) {
		this.rootTestDescriptor = request.getRootTestDescriptor();
		this.listener = request.getEngineExecutionListener();
		this.rootContext = rootContext;
	}

	void execute() {
		execute(this.rootTestDescriptor, this.rootContext, new ExecutionTracker());
	}

	private void execute(TestDescriptor testDescriptor, C parentContext, ExecutionTracker tracker) {
		Node<C> node = asNode(testDescriptor);
		tracker.markExecuted(testDescriptor);

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

				C contextForDynamicChildren = context;
				context = node.execute(context, dynamicTestDescriptor -> {
					this.listener.dynamicTestRegistered(dynamicTestDescriptor);
					execute(dynamicTestDescriptor, contextForDynamicChildren, tracker);
				});

				C contextForStaticChildren = context;
				// @formatter:off
				testDescriptor.getChildren().stream()
						.filter(child -> !tracker.wasAlreadyExecuted(child))
						.forEach(child -> execute(child, contextForStaticChildren, tracker));
				// @formatter:on
			}
			finally {
				node.after(context);
			}
		});

		this.listener.executionFinished(testDescriptor, result);
	}

	@SuppressWarnings("unchecked")
	private Node<C> asNode(TestDescriptor testDescriptor) {
		return (testDescriptor instanceof Node ? (Node<C>) testDescriptor : noOpNode);
	}

	@SuppressWarnings("rawtypes")
	private static final Node noOpNode = new Node() {
	};

}
