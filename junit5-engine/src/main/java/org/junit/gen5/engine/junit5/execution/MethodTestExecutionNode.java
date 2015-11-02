/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.junit.gen5.commons.util.AnnotationUtils.*;
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import org.junit.gen5.api.After;
import org.junit.gen5.api.Before;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

/**
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
class MethodTestExecutionNode extends TestExecutionNode {

	private final MethodTestDescriptor testDescriptor;

	MethodTestExecutionNode(MethodTestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	@Override
	public MethodTestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	@Override
	public void execute(EngineExecutionContext context) {
		context.getTestExecutionListener().testStarted(getTestDescriptor());
		Object testInstance = context.getAttributes().get(ClassTestExecutionNode.TEST_INSTANCE_ATTRIBUTE_NAME);
		Class<?> testClass = testInstance.getClass();
		Throwable exceptionThrown = null;

		try {
			executeBeforeMethods(testClass, testInstance);
			this.invokeTestMethod(context, testInstance);
		}
		catch (Throwable ex) {
			exceptionThrown = ex;
			if (ex instanceof InvocationTargetException) {
				exceptionThrown = ((InvocationTargetException) ex).getTargetException();
			}
		}
		finally {
			exceptionThrown = executeAfterMethods(context, testClass, testInstance, exceptionThrown);
		}

		if (exceptionThrown != null) {
			if (exceptionThrown instanceof TestSkippedException) {
				context.getTestExecutionListener().testSkipped(getTestDescriptor(), exceptionThrown);
			}
			else if (exceptionThrown instanceof TestAbortedException) {
				context.getTestExecutionListener().testAborted(getTestDescriptor(), exceptionThrown);
			}
			else {
				context.getTestExecutionListener().testFailed(getTestDescriptor(), exceptionThrown);
			}
		}
		else {
			context.getTestExecutionListener().testSucceeded(getTestDescriptor());
		}
	}

	private void invokeTestMethod(EngineExecutionContext context, Object testInstance)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {

		// for a 'real' solution see: org.springframework.web.method.support.HandlerMethodArgumentResolver

		MethodTestDescriptor methodTestDescriptor = getTestDescriptor();
		Method testMethod = methodTestDescriptor.getTestMethod();

		List<Object> arguments = new ArrayList<>();

		if (testMethod.getParameterCount() > 0) {

			Parameter[] parameters = testMethod.getParameters();
			for (Parameter parameter : parameters) {
				Class<?> parameterType = parameter.getType();
				Annotation[] parameterAnnotations = parameter.getAnnotations();

				System.out.println("				parameterType = " + parameterType);
				System.out.println("				parameterAnnotations = " + Arrays.asList(parameterAnnotations));

				Object newInstance = ReflectionUtils.newInstance(parameterType);
				arguments.add(newInstance);
			}
		}

		ReflectionUtils.invokeMethod(testMethod, testInstance, arguments.toArray());
	}

	private void executeBeforeMethods(Class<?> testClass, Object testInstance) throws Exception {
		for (Method method : findAnnotatedMethods(testClass, Before.class, MethodSortOrder.HierarchyDown)) {
			invokeMethod(method, testInstance);
		}
	}

	private Throwable executeAfterMethods(EngineExecutionContext context, Class<?> testClass, Object testInstance,
			Throwable exceptionThrown) {

		for (Method method : findAnnotatedMethods(testClass, After.class, MethodSortOrder.HierarchyUp)) {
			try {
				invokeMethod(method, testInstance);
			}
			catch (Throwable ex) {
				Throwable currentException = ex;
				if (currentException instanceof InvocationTargetException) {
					currentException = ((InvocationTargetException) currentException).getTargetException();
				}

				if (exceptionThrown == null) {
					exceptionThrown = currentException;
				}
				else {
					exceptionThrown.addSuppressed(currentException);
				}
			}
		}

		return exceptionThrown;
	}

}
