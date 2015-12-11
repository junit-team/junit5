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
		try {
			TestDescriptor rootTestDescriptor = request.getRootTestDescriptor();
			executeAll(rootTestDescriptor, request.getEngineExecutionListener(), createContext());
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected abstract C createContext();

	@SuppressWarnings({ "unused", "unchecked" })
	private void executeAll(TestDescriptor parentDescriptor, EngineExecutionListener listener, C parentContext)
			throws Exception {
		C context = parentContext;
		if (parentDescriptor instanceof Container) {
			context = ((Container<C>) parentDescriptor).beforeAll(context);
		}
		for (TestDescriptor childDescriptor : parentDescriptor.getChildren()) {
			if (childDescriptor instanceof Leaf) {
				Leaf<C> child = (Leaf<C>) childDescriptor;
				try {
					listener.testStarted(childDescriptor);
					C childContext = child.execute(context);
					listener.testSucceeded(childDescriptor);
				}
				catch (Throwable t) {
					listener.testFailed(childDescriptor, t);
				}
			}
			executeAll(childDescriptor, listener, context);
		}
		if (parentDescriptor instanceof Container) {
			context = ((Container<C>) parentDescriptor).afterAll(context);
		}
	}

}
