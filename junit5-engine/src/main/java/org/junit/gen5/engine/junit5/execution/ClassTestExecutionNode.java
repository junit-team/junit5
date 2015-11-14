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
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.lang.reflect.Method;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.ReflectionUtils.*;
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

	private final ConditionEvaluator conditionalEvaluator = new ConditionEvaluator();

	ClassTestExecutionNode(ClassTestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	@Override
	public ClassTestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	@Override
	public void execute(ExecutionRequest request, TestExecutionContext context) {
		Class<?> testClass = getTestDescriptor().getTestClass();

		if (!this.conditionalEvaluator.testEnabled(getTestDescriptor())) {
			// TODO Determine if we really need an explicit TestSkippedException.
			// TODO Provide a way for failed conditions to provide a detailed explanation
			// of why a condition failed (e.g., a text message).
			TestSkippedException testSkippedException = new TestSkippedException(
				String.format("Skipped test class [%s] due to failed condition", testClass.getName()));
			request.getTestExecutionListener().testSkipped(getTestDescriptor(), testSkippedException);

			// Abort execution of the test completely at this point.
			return;
		}

		Object testInstance = createTestInstance();
		request.getTestInstances().push(testInstance);

		try {
			executeBeforeAllMethods(testClass, testInstance);
			for (TestExecutionNode child : getChildren()) {
				executeChild(child, request, context);
			}
		}
		catch (Exception e) {
			request.getTestExecutionListener().testFailed(getTestDescriptor(), e);
		}
		finally {
			try {
				executeAfterAllMethods(request, testClass, testInstance);
			}
			finally {
				request.getTestInstances().pop();
			}
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

}
