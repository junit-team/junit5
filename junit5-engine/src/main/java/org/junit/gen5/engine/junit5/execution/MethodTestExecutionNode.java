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

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotatedMethods;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.junit.gen5.api.After;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.extension.MethodArgumentResolver;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.ExecutionRequest;
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

	private final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();

	MethodTestExecutionNode(MethodTestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	@Override
	public MethodTestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	@Override
	public void execute(ExecutionRequest request, TestExecutionContext context) {
		final Method testMethod = getTestDescriptor().getTestMethod();

		if (!this.conditionEvaluator.testEnabled(getTestDescriptor())) {
			// TODO Determine if we really need an explicit TestSkippedException.
			// TODO Provide a way for failed conditions to provide a detailed explanation
			// of why a condition failed (e.g., a text message).
			TestSkippedException testSkippedException = new TestSkippedException(
				String.format("Skipped test method [%s] due to failed condition", testMethod.toGenericString()));
			request.getTestExecutionListener().testSkipped(getTestDescriptor(), testSkippedException);

			// Abort execution of the test completely at this point.
			return;
		}

		request.getTestExecutionListener().testStarted(getTestDescriptor());
		Throwable exceptionThrown = null;

		try {
			executeBeforeMethods(context);
			invokeTestMethod(context.getTestMethod().get(), context);
		}
		catch (Throwable ex) {
			exceptionThrown = ex;
			if (ex instanceof InvocationTargetException) {
				exceptionThrown = ((InvocationTargetException) ex).getTargetException();
			}
		}
		finally {
			exceptionThrown = executeAfterMethods(context, exceptionThrown);
		}

		if (exceptionThrown != null) {
			if (exceptionThrown instanceof TestSkippedException) {
				request.getTestExecutionListener().testSkipped(getTestDescriptor(), exceptionThrown);
			}
			else if (exceptionThrown instanceof TestAbortedException) {
				request.getTestExecutionListener().testAborted(getTestDescriptor(), exceptionThrown);
			}
			else {
				request.getTestExecutionListener().testFailed(getTestDescriptor(), exceptionThrown);
			}
		}
		else {
			request.getTestExecutionListener().testSucceeded(getTestDescriptor());
		}
	}

	private void invokeMethod(Method method, TestExecutionContext context) {
		Set<MethodArgumentResolver> resolvers = context.getArgumentResolvers();
		MethodInvoker methodInvoker = new MethodInvoker(method, context.getTestInstance().get(), resolvers);
		methodInvoker.invoke(context);
	}

	private void invokeTestMethod(Method method, TestExecutionContext context) {
		invokeMethod(method, context);
	}

	private void executeBeforeMethods(TestExecutionContext context) {
		for (Method method : findAnnotatedMethods(context.getTestClass().get(), Before.class,
			MethodSortOrder.HierarchyDown)) {
			invokeMethod(method, context);
		}
	}

	private Throwable executeAfterMethods(TestExecutionContext context, Throwable exceptionThrown) {

		for (Method method : findAnnotatedMethods(context.getTestClass().get(), After.class,
			MethodSortOrder.HierarchyUp)) {
			try {
				invokeMethod(method, context);
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
