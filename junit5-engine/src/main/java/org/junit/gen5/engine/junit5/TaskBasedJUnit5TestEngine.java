/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;

import java.util.*;

import org.junit.gen5.api.*;
import org.junit.gen5.engine.*;
import org.junit.gen5.engine.junit5.task.*;

public class TaskBasedJUnit5TestEngine implements TestEngine {

	// TODO Consider using class names for engine IDs.
	private static final String ENGINE_ID = "tb_junit5";


	@Override
	public String getId() {
		return ENGINE_ID;
	}

	@Override
	public List<TestDescriptor> discoverTests(TestPlanSpecification specification) {
		List<TestDescriptor> testDescriptors = new ArrayList<>();

		for (TestPlanSpecificationElement element : specification) {
			if (element instanceof ClassNameSpecification) {
				ClassNameSpecification classNameSpecification = (ClassNameSpecification) element;
				Class<?> testClass = discoverTestClass(classNameSpecification.getClassName());
				// @formatter:off
				testDescriptors.addAll(Arrays.stream(testClass.getDeclaredMethods())
					.filter(method -> method.isAnnotationPresent(Test.class))
					.map(method -> new JavaMethodTestDescriptor(getId(), testClass, method))
					.collect(toList()));
				// @formatter:on
			}
			else if (element instanceof UniqueIdSpecification) {
				UniqueIdSpecification uniqueIdSpecification = (UniqueIdSpecification) element;
				testDescriptors.add(JavaMethodTestDescriptor.from(uniqueIdSpecification.getUniqueId()));
			}
		}

		return testDescriptors;
	}

	private Class<?> discoverTestClass(String className) {
		// TODO Use correct ClassLoader
		try {
			return Class.forName(className);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(format("Failed to load test class '%s'", className));
		}
	}

	@Override
	public boolean supports(TestDescriptor testDescriptor) {
		return testDescriptor instanceof JavaMethodTestDescriptor;
	}

	@Override
	public void execute(Collection<TestDescriptor> testDescriptors, TestExecutionListener testExecutionListener) {

		List<ExecutionTask> executionTasks = this.buildTaskTrees(testDescriptors, testExecutionListener);

		CompositeTask root = new CompositeTask(executionTasks, "ENGINE: " + this.getId());
		try {
			root.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		this.logCompleteTree(root);

		// this.executeAllTaskTrees(executionTasks);

	}

	private void executeAllTaskTrees(List<ExecutionTask> executionTasks) {
		try {
			for (ExecutionTask executionTask : executionTasks) {
				executionTask.execute();

				this.logCompleteTree(executionTask);

			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void logCompleteTree(ExecutionTask executionTask) {
		System.out.println();
		TaskPrinter printer = new TaskPrinter();
		printer.print(executionTask);
		System.out.println();
	}

	private List<ExecutionTask> buildTaskTrees(Collection<TestDescriptor> testDescriptors,
			TestExecutionListener testExecutionListener) {
		List<ExecutionTask> executionTasks = new ArrayList<>();

		for (TestDescriptor testDescriptor : testDescriptors) {
			RootTask task = new RootTask(testExecutionListener, (JavaMethodTestDescriptor) testDescriptor);
			executionTasks.add(task);

		}
		return executionTasks;
	}

}
