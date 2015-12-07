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

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

public final class DummyTestEngine implements TestEngine {

	public static final String ENGINE_ID = "dummy";

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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public TestDescriptor discoverTests(TestPlanSpecification specification) {
		// @formatter:off
		Set<TestDescriptor> childrenDescriptors = children.keySet().stream()
				.map(name -> stubDescriptor(name))
				.collect(toSet());
		// @formatter:on
		TestDescriptor root = stubDescriptor("root", childrenDescriptors);
		for (TestDescriptor child : childrenDescriptors) {
			when(child.getParent()).thenReturn((Optional) Optional.of(root));
		}
		return root;
	}

	private TestDescriptor stubDescriptor(String name) {
		return stubDescriptor(name, emptySet());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private TestDescriptor stubDescriptor(String name, Set<TestDescriptor> children) {
		TestDescriptor descriptor = mock(TestDescriptor.class);
		when(descriptor.getDisplayName()).thenReturn(name);
		when(descriptor.getUniqueId()).thenReturn(ENGINE_ID + ":" + name);
		when(descriptor.getParent()).thenReturn((Optional) Optional.empty());
		when(descriptor.getSource()).thenReturn((Optional) Optional.empty());
		when(descriptor.getTags()).thenReturn(emptySet());
		when(descriptor.getChildren()).thenAnswer(invocation -> children);
		when(descriptor.countStaticTests()).thenAnswer(invocation -> children.size());
		return descriptor;
	}

	@Override
	public void execute(ExecutionRequest request) {
		TestExecutionListener listener = request.getTestExecutionListener();
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

	public enum TestResult implements BiConsumer<TestExecutionListener, TestDescriptor> {
		SUCCESS {

			@Override
			public void accept(TestExecutionListener listener, TestDescriptor descriptor) {
				listener.testSucceeded(descriptor);
			}
		},

		FAILURE {

			@Override
			public void accept(TestExecutionListener listener, TestDescriptor descriptor) {
				listener.testFailed(descriptor, new Exception("failure"));
			}
		};
	}
}