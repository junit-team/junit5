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
			C context = executeBeforeAll(testDescriptor, parentContext);
			context = executeLeaf(testDescriptor, context);
			for (TestDescriptor child : testDescriptor.getChildren()) {
				executeAll(child, context);
			}
			context = executeAfterAll(testDescriptor, context);
		});
		listener.executionFinished(testDescriptor, result);
	}

	@SuppressWarnings("unchecked")
	private C executeBeforeAll(TestDescriptor testDescriptor, C context) {
		if (testDescriptor instanceof Container) {
			return ((Container<C>) testDescriptor).beforeAll(context);
		}
		return context;
	}

	@SuppressWarnings("unchecked")
	private C executeLeaf(TestDescriptor testDescriptor, C context) throws Throwable {
		if (testDescriptor instanceof Leaf) {
			return ((Leaf<C>) testDescriptor).execute(context);
		}
		return context;
	}

	@SuppressWarnings("unchecked")
	private C executeAfterAll(TestDescriptor testDescriptor, C context) {
		if (testDescriptor instanceof Container) {
			return ((Container<C>) testDescriptor).afterAll(context);
		}
		return context;
	}

}
