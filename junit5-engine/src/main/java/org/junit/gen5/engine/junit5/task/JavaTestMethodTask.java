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
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import org.junit.gen5.api.*;

@Deprecated
public class JavaTestMethodTask<T> implements ExecutionTask {

	private final Class<T> target;
	private final T instance;
	private final Method method;


	JavaTestMethodTask(Class<T> target, Method method, T instance) {
		this.target = target;
		this.method = method;
		this.instance = instance;
	}

	public void execute() throws Exception {

		System.out.println("--> TASK: " + this.getClass().getSimpleName());

		executeBeforeMethods(this.target, this.instance);
		invokeMethod(this.method, this.instance);
		executeAfterMethods(this.target, this.instance);
	}

	private void executeBeforeMethods(Class<?> testClass, Object testInstance) throws Exception {
		for (Method method : findAnnotatedMethods(testClass, Before.class)) {
			invokeMethod(method, testInstance);
		}
	}

	private void executeAfterMethods(Class<?> testClass, Object testInstance) throws Exception {
		for (Method method : findAnnotatedMethods(testClass, After.class)) {
			invokeMethod(method, testInstance);
		}
	}

	private List<Method> findAnnotatedMethods(Class<?> testClass, Class<? extends Annotation> annotationType) {
		// @formatter:off
		return Arrays.stream(testClass.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(annotationType))
				.collect(toList());
		// @formatter:on
	}

	@Override
	public List<ExecutionTask> getChildren() {
		return null;
	}

}
