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

import static java.util.stream.Collectors.toSet;
import static org.junit.gen5.engine.DummyTestDescriptor.ENGINE_ID;

import java.util.LinkedHashMap;
import java.util.Set;

public final class DummyTestEngine implements TestEngine {

	private final LinkedHashMap<String, Runnable> children = new LinkedHashMap<>();

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	public void addTest(String uniqueName, Runnable runnable) {
		children.put(uniqueName, runnable);
	}

	@Override
	public TestDescriptor discoverTests(TestPlanSpecification specification) {
		// @formatter:off
		Set<DummyTestDescriptor> children = this.children.keySet().stream()
				.map(DummyTestDescriptor::new)
				.collect(toSet());
		// @formatter:on
		DummyTestDescriptor root = new DummyTestDescriptor("root");
		children.forEach(root::addChild);
		return root;
	}

	@Override
	public void execute(ExecutionRequest request) {
		EngineExecutionListener listener = request.getEngineExecutionListener();
		for (TestDescriptor childDescriptor : request.getRootTestDescriptor().getChildren()) {
			Runnable runnable = children.get(childDescriptor.getDisplayName());
			listener.testStarted(childDescriptor);
			TestExecutionResult result;
			try {
				runnable.run();
				result = TestExecutionResult.successful();
			}
			catch (Throwable t) {
				result = TestExecutionResult.failed(t);
			}
			listener.testFinished(childDescriptor, result);
		}
	}
}