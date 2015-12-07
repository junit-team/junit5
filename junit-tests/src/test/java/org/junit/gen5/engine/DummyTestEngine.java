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
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

public final class DummyTestEngine implements TestEngine {

	private final LinkedHashMap<String, Callable<TestResult>> children = new LinkedHashMap<>();

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	public void addTest(String uniqueName, TestResult result, Runnable runnable) {
		children.put(uniqueName, () -> {
			runnable.run();
			return result;
		});
	}

	@Override
	public TestDescriptor discoverTests(TestPlanSpecification specification) {
		DummyTestDescriptor root = new DummyTestDescriptor("root");
		// @formatter:off
		Set<DummyTestDescriptor> children = this.children.keySet().stream()
				.map(DummyTestDescriptor::new)
				.collect(toSet());
		// @formatter:on
		children.forEach(root::addChild);
		return root;
	}

	@Override
	public void execute(ExecutionRequest request) {
		EngineExecutionListener listener = request.getEngineExecutionListener();
		for (TestDescriptor childDescriptor : request.getRootTestDescriptor().getChildren()) {
			Callable<TestResult> callable = children.get(childDescriptor.getDisplayName());
			listener.testStarted(childDescriptor);
			try {
				TestResult testResult = callable.call();
				testResult.accept(listener, childDescriptor);
			}
			catch (Throwable t) {
				listener.testFailed(childDescriptor, t);
			}
		}
	}

	public enum TestResult implements BiConsumer<EngineExecutionListener, TestDescriptor> {
		SUCCESS {

			@Override
			public void accept(EngineExecutionListener listener, TestDescriptor descriptor) {
				listener.testSucceeded(descriptor);
			}
		},

		FAILURE {

			@Override
			public void accept(EngineExecutionListener listener, TestDescriptor descriptor) {
				listener.testFailed(descriptor, new Exception("failure"));
			}
		};
	}
}