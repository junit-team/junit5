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

public abstract class HierarchicalTestEngine<C extends EngineExecutionContext> implements TestEngine {

	@Override
	public abstract TestDescriptor discoverTests(TestPlanSpecification specification);

	@Override
	public final void execute(ExecutionRequest request) {
		TestDescriptor rootTestDescriptor = request.getRootTestDescriptor();
		EngineExecutionListener engineExecutionListener = request.getEngineExecutionListener();
		executeAll(rootTestDescriptor, engineExecutionListener, createContext());
	}

	protected abstract C createContext();

	private void executeAll(TestDescriptor testDescriptor, EngineExecutionListener listener, C parentContext) {
		// TODO Check whether TestDescriptor should be skipped and fire executionSkipped
		// event instead.
		listener.executionStarted(testDescriptor);
		TestExecutionResult result;
		try {
			C context = executeBeforeAll(testDescriptor, parentContext);
			context = executeLeaf(testDescriptor, context);
			for (TestDescriptor child : testDescriptor.getChildren()) {
				executeAll(child, listener, context);
			}
			context = executeAfterAll(testDescriptor, context);
			result = TestExecutionResult.successful();
		}
		catch (Throwable t) {
			result = TestExecutionResult.failed(t);
		}
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
