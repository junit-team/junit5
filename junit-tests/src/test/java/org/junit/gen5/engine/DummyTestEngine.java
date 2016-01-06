/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.LinkedList;
import java.util.List;

public final class DummyTestEngine extends HierarchicalTestEngine<DummyEngineExecutionContext> {
	public static final String ENGINE_ID = "dummy";

	private final List<DummyTestEngineDescriptor> children = new LinkedList<>();

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	public void addTest(String uniqueName, Runnable runnable) {
		children.add(new DummyTestEngineDescriptor(uniqueName, runnable));
	}

	@Override
	public EngineAwareTestDescriptor discoverTests(TestPlanSpecification specification) {
		DummyTestEngineDescriptor root = new DummyTestEngineDescriptor("root", null);
		children.forEach(root::addChild);
		return root;
	}

	@Override
	protected DummyEngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new DummyEngineExecutionContext();
	}
}
