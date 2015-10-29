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

import org.junit.gen5.api.*;
import org.junit.gen5.engine.*;
import org.opentestalliance.*;

import java.lang.reflect.*;
import java.util.*;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.junit.gen5.commons.util.ReflectionUtils.*;

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

		// TODO Build a tree of TestDescriptors.
		//
		// Simply iterating over a collection is insufficient for our purposes. We need a
		// tree (or some form of hierarchical data structure) in order to be able to
		// execute each test within the correct scope.
		//
		// For example, we need to execute all test methods within a given test class as a
		// group in order to:
		//
		// 1) retain the instance across test method invocations (if desired).
		// 2) invoke class-level before & after methods _around_ the set of methods.

		for (TestDescriptor testDescriptor : testDescriptors) {
			this.handleSingleDescriptor(testExecutionListener, (JavaMethodTestDescriptor) testDescriptor);
		}
	}


	private void handleSingleDescriptor(TestExecutionListener testExecutionListener, JavaMethodTestDescriptor testDescriptor) {

		try {
			testExecutionListener.testStarted(testDescriptor);


			Object instance = newInstance(testDescriptor.getTestClass());
			JavaTestMethodTask task = new JavaTestMethodTask(testDescriptor.getTestClass(), testDescriptor.getTestMethod(), instance);
			task.execute();

			testExecutionListener.testSucceeded(testDescriptor);
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			if (targetException instanceof TestSkippedException) {
				testExecutionListener.testSkipped(testDescriptor, targetException);
			}
			else if (targetException instanceof TestAbortedException) {
				testExecutionListener.testAborted(testDescriptor, targetException);
			}
			else {
				testExecutionListener.testFailed(testDescriptor, targetException);
			}
		}
		catch (NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
			throw new IllegalStateException(String.format("Test %s is not well-formed and cannot be executed",
					testDescriptor.getUniqueId()), ex);
		}
		catch (Exception ex) {
			testExecutionListener.testFailed(testDescriptor, ex);
		}
	}

}
