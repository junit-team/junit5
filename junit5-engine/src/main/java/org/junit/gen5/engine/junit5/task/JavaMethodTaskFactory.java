/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.task;

import static java.util.stream.Collectors.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import org.junit.gen5.api.*;
import org.junit.gen5.engine.junit5.*;

public class JavaMethodTaskFactory {

	public ExecutionTask createJavaTestMethodTask(JavaMethodTestDescriptor testDescriptor, Object instance) {

		List<ExecutionTask> tasks = new ArrayList<>();

		tasks.addAll(this.createBeforeTasks(testDescriptor.getTestClass(), instance));
		tasks.add(this.createTestMethodTask(testDescriptor, instance));
		tasks.addAll(this.createAfterTasks(testDescriptor.getTestClass(), instance));

		return new CompositeTask(tasks);

	}

	private ExecutionTask createTestMethodTask(JavaMethodTestDescriptor testDescriptor, Object instance) {
		return new MethodTask(testDescriptor.getTestClass(), testDescriptor.getTestMethod(), instance);
	}

	private List<ExecutionTask> createBeforeTasks(Class<?> testClass, Object instance) {
		return createExecutionTasksForAnnotationType(testClass, instance, Before.class);
	}

	private List<ExecutionTask> createAfterTasks(Class<?> testClass, Object instance) {
		return createExecutionTasksForAnnotationType(testClass, instance, After.class);
	}

	private List<ExecutionTask> createExecutionTasksForAnnotationType(Class<?> testClass, Object instance,
			Class<? extends Annotation> annotationType) {

		List<Method> annotatedMethods = this.findAnnotatedMethods(testClass, annotationType);

		ArrayList<ExecutionTask> executionTasks = new ArrayList<>();

		for (Method method : annotatedMethods) {
			executionTasks.add(new MethodTask(testClass, method, instance));
		}

		return executionTasks;
	}

	private List<Method> findAnnotatedMethods(Class<?> testClass, Class<? extends Annotation> annotationType) {
		// @formatter:off
		return Arrays.stream(testClass.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(annotationType))
				.collect(toList());
		// @formatter:on
	}

}
