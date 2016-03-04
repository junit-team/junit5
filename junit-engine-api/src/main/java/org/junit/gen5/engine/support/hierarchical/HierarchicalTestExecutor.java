/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.hierarchical;

import static org.junit.gen5.engine.support.hierarchical.BlacklistedExceptions.rethrowIfBlacklisted;

import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.support.hierarchical.Node.SkipResult;

/**
 * Implementation core of all {@link TestEngine TestEngines} that wish to
 * use the {@link Node}, {@link Container}, and {@link Leaf} abstractions
 * as the driving principles for structuring and executing test suites.
 *
 * <p>A {@code HierarchicalTestExecutor} is instantiated by a concrete
 * implementation of {@link HierarchicalTestEngine} and takes care of
 * calling containers and leaves in the appropriate order as well as
 * firing the necessary events in the {@link EngineExecutionListener}.
 *
 * @param <C> the type of {@code EngineExecutionContext} used by the {@code HierarchicalTestEngine}
 * @since 5.0
 */
class HierarchicalTestExecutor<C extends EngineExecutionContext> {

	private final SingleTestExecutor singleTestExecutor = new SingleTestExecutor();

	private final MixinAdapter<C> adapter = new MixinAdapter<>();

	private final TestDescriptor rootTestDescriptor;

	private final EngineExecutionListener listener;

	private final C rootContext;

	HierarchicalTestExecutor(ExecutionRequest request, C rootContext) {
		this.rootTestDescriptor = request.getRootTestDescriptor();
		this.listener = request.getEngineExecutionListener();
		this.rootContext = rootContext;
	}

	void execute() {
		executeAll(rootTestDescriptor, rootContext);
	}

	private void executeAll(TestDescriptor testDescriptor, C parentContext) {

		C preparedContext;
		try {
			preparedContext = adapter.asNode(testDescriptor).prepare(parentContext);
			SkipResult skipResult = adapter.asNode(testDescriptor).shouldBeSkipped(preparedContext);
			if (skipResult.isSkipped()) {
				listener.executionSkipped(testDescriptor, skipResult.getReason().orElse("<unknown>"));
				return;
			}
		}
		catch (Throwable throwable) {
			rethrowIfBlacklisted(throwable);

			// TODO Decide if exceptions thrown during preparation should result in the node being marked as "started".
			listener.executionStarted(testDescriptor);
			listener.executionFinished(testDescriptor, TestExecutionResult.failed(throwable));
			return;
		}

		listener.executionStarted(testDescriptor);

		TestExecutionResult result = singleTestExecutor.executeSafely(() -> {
			C context = adapter.asContainer(testDescriptor).beforeAll(preparedContext);
			context = adapter.asLeaf(testDescriptor).execute(context);

			if (testDescriptor instanceof Container) { // to prevent execution of dynamically added children
				for (TestDescriptor child : testDescriptor.getChildren()) {
					executeAll(child, context);
				}
			}
			context = adapter.asContainer(testDescriptor).afterAll(context);
		});
		listener.executionFinished(testDescriptor, result);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static class MixinAdapter<C extends EngineExecutionContext> {

		private static final Container nullContainer = new Container() {
		};

		private static final Leaf nullLeaf = context -> context;

		private static final Node nullNode = new Node() {
		};

		Container<C> asContainer(TestDescriptor testDescriptor) {
			return (Container<C>) (testDescriptor instanceof Container ? testDescriptor : nullContainer);
		}

		Leaf<C> asLeaf(TestDescriptor testDescriptor) {
			return (Leaf<C>) (testDescriptor instanceof Leaf ? testDescriptor : nullLeaf);
		}

		Node<C> asNode(TestDescriptor testDescriptor) {
			return (Node<C>) (testDescriptor instanceof Node ? testDescriptor : nullNode);
		}
	}

}
