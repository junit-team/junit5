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
		// TODO Check whether TestDescriptor should be skipped and fire executionSkipped
		// event instead.
		listener.executionStarted(testDescriptor);
		TestExecutionResult result = singleTestExecutor.executeSafely(() -> {
			C context = adapter.asContainer(testDescriptor).beforeAll(parentContext);
			context = adapter.asLeaf(testDescriptor).execute(context);
			for (TestDescriptor child : testDescriptor.getChildren()) {
				executeAll(child, context);
			}
			context = adapter.asContainer(testDescriptor).afterAll(context);
		});
		listener.executionFinished(testDescriptor, result);
	}

	private static class MixinAdapter<C extends EngineExecutionContext> {

		private final Leaf<C> nullLeaf = c -> c;

		private final Container<C> nullContainer = new Container<C>() {
		};

		@SuppressWarnings("unchecked")
		Container<C> asContainer(TestDescriptor testDescriptor) {
			return testDescriptor instanceof Container ? (Container<C>) testDescriptor : nullContainer;
		}

		@SuppressWarnings("unchecked")
		Leaf<C> asLeaf(TestDescriptor testDescriptor) {
			return testDescriptor instanceof Leaf ? (Leaf<C>) testDescriptor : nullLeaf;
		}
	}

}
