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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.Condition.Result;
import org.junit.gen5.api.extension.MethodArgumentResolver;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.opentestalliance.TestSkippedException;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
class ClassTestExecutionNode extends TestExecutionNode {

	private final ClassTestDescriptor testDescriptor;

	private final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();

	ClassTestExecutionNode(ClassTestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	@Override
	public ClassTestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	@Override
	public void execute(ExecutionRequest request, TestExecutionContext context) {
		Class<?> testClass = context.getTestClass().get();

		Result result = this.conditionEvaluator.evaluate(context);
		if (!result.isSuccess()) {
			// TODO Determine if we really need an explicit TestSkippedException.
			TestSkippedException testSkippedException = new TestSkippedException(String.format(
				"Skipping test class [%s]; reason: %s", testClass.getName(), result.getReason().orElse("unknown")));
			request.getTestExecutionListener().testSkipped(getTestDescriptor(), testSkippedException);

			// Abort execution of the test completely at this point.
			return;
		}

		Object testInstance = createTestInstance();

		try {
			executeBeforeAllMethods(testClass, testInstance);
			for (TestExecutionNode child : getChildren()) {
				executeChild(child, request, context, testInstance);
			}
		}
		catch (Exception e) {
			request.getTestExecutionListener().testFailed(getTestDescriptor(), e);
		}
		finally {
			executeAfterAllMethods(request, testClass, testInstance);
		}
	}

	private void executeBeforeAllMethods(Class<?> testClass, Object testInstance) throws Exception {
		for (Method method : findAnnotatedMethods(testClass, BeforeAll.class, MethodSortOrder.HierarchyDown)) {
			invokeMethod(method, testInstance);
		}
	}

	private void executeAfterAllMethods(ExecutionRequest context, Class<?> testClass, Object testInstance) {
		Exception exceptionDuringAfterAll = null;

		for (Method method : findAnnotatedMethods(testClass, AfterAll.class, MethodSortOrder.HierarchyUp)) {
			try {
				invokeMethod(method, testInstance);
			}
			catch (Exception e) {
				if (exceptionDuringAfterAll == null) {
					exceptionDuringAfterAll = e;
				}
				else {
					exceptionDuringAfterAll.addSuppressed(e);
				}
			}
		}

		if (exceptionDuringAfterAll != null) {
			context.getTestExecutionListener().testFailed(getTestDescriptor(), exceptionDuringAfterAll);
		}
	}

	/**
	 * Must be public to be executable by child contexts
	 * @return
	 */
	public Object createTestInstance() {
		try {
			return newInstance(getTestDescriptor().getTestClass());
		}
		catch (Exception ex) {
			throw new IllegalStateException(
				String.format("Test %s is not well-formed and cannot be executed", getTestDescriptor().getUniqueId()),
				ex);
		}
	}

	@Override
	public void executeBeforeEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance) {
		List<Method> beforeEachMethods = getBeforeEachMethods();

		Set<MethodArgumentResolver> parentResolvers = resolutionContext.getArgumentResolvers();
		for (Method method : beforeEachMethods) {
			invokeMethodInContext(method, methodContext, parentResolvers, testInstance);
		}

	}

	protected List<Method> getBeforeEachMethods() {
		return findAnnotatedMethods(getTestDescriptor().getTestClass(), Before.class, MethodSortOrder.HierarchyDown);
	}

	@Override
	public Throwable executeAfterEachTest(TestExecutionContext context, Object testInstance,
			Throwable previousException) {
		List<Method> afterEachMethods = getAfterEachMethods();

		for (Method method : afterEachMethods) {
			previousException = invokeMethodInContextWithAggregatingExceptions(method, context, testInstance,
				previousException);
		}

		return previousException;
	}

	protected List<Method> getAfterEachMethods() {
		return findAnnotatedMethods(getTestDescriptor().getTestClass(), Before.class, MethodSortOrder.HierarchyUp);
	}

}
