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

import static org.junit.gen5.engine.DummyTestDescriptor.ENGINE_ID;

import java.util.LinkedList;
import java.util.List;

public final class DummyTestEngine implements TestEngine {

	private final List<DummyTestDescriptor> children = new LinkedList<>();

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	public void addTest(String uniqueName, Runnable runnable) {
		children.add(new DummyTestDescriptor(uniqueName, runnable));
	}

	@Override
	public TestDescriptor discoverTests(TestPlanSpecification specification) {
		DummyTestDescriptor root = new DummyTestDescriptor("root", null);
		children.forEach(root::addChild);
		return root;
	}

	@Override
	public void execute(ExecutionRequest request) {
		new HierarchicalTestExecutor<>(request, new DummyEngineExecutionContext()).execute();
	}
}
