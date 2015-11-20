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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Condition.Result;
import org.junit.gen5.api.TestInstance;
import org.junit.gen5.api.TestInstance.Lifecycle;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
class ClassExecutionNode extends TestExecutionNode {

	private final ClassTestDescriptor testDescriptor;

	ClassExecutionNode(ClassTestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	@Override
	ClassTestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	@Override
	void execute(ExecutionRequest request, TestExecutionContext context) {
		if (isTestDisabled(request, context)) {
			// Abort execution of the test completely at this point.
			return;
		}

		Class<?> testClass = context.getTestClass().get();
		boolean instancePerClass = isInstancePerClassMode(testClass);
		Object testInstance = (instancePerClass ? createAndPostProcessTestInstance(context) : null);

		try {
			executeBeforeAllMethods(testClass, testInstance);
			for (TestExecutionNode child : getChildren()) {
				if (!instancePerClass) {
					testInstance = createAndPostProcessTestInstance(context);
				}
				executeChild(child, request, context, testInstance);
			}
		}
		catch (Exception e) {
			request.getTestExecutionListener().testFailed(getTestDescriptor(), e);
		}
		finally {
			executeAfterAllMethods(request, testClass, (instancePerClass ? testInstance : null));
		}
	}

	@Override
	protected String buildTestSkippedMessage(Result result, TestExecutionContext context) {
		return String.format("Skipped test class [%s]; reason: %s", context.getTestClass().get().getName(),
			result.getReason().orElse("unknown"));
	}

	private boolean isInstancePerClassMode(Class<?> testClass) {
		// @formatter:off
		return AnnotationUtils.findAnnotation(testClass, TestInstance.class)
				.map(TestInstance::value)
				.map(lifecycle -> (lifecycle == Lifecycle.PER_CLASS))
				.orElse(false);
		// @formatter:on
	}

	private void executeBeforeAllMethods(Class<?> testClass, Object testInstance) throws Exception {
		Class<BeforeAll> annotationType = BeforeAll.class;
		for (Method method : findAnnotatedMethods(testClass, annotationType, MethodSortOrder.HierarchyDown)) {
			validateBeforeAllOrAfterAllMethod(annotationType, method, testInstance);
			invokeMethod(method, testInstance);
		}
	}

	private void executeAfterAllMethods(ExecutionRequest request, Class<?> testClass, Object testInstance) {
		Class<AfterAll> annotationType = AfterAll.class;
		Exception exceptionDuringAfterAll = null;

		for (Method method : findAnnotatedMethods(testClass, annotationType, MethodSortOrder.HierarchyUp)) {
			try {
				validateBeforeAllOrAfterAllMethod(annotationType, method, testInstance);
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
			request.getTestExecutionListener().testFailed(getTestDescriptor(), exceptionDuringAfterAll);
		}
	}

	private void validateBeforeAllOrAfterAllMethod(Class<? extends Annotation> annotationType, Method method,
			Object target) {
		if (target == null && !Modifier.isStatic(method.getModifiers())) {
			throw new IllegalStateException(String.format(
				"Failed to invoke @%s method [%s]. Either declare it as static or annotate the test class with @TestInstance(PER_CLASS).",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

	protected Object createAndPostProcessTestInstance(TestExecutionContext context) {
		final Class<?> testClass = getTestDescriptor().getTestClass();
		Object testInstance;
		try {
			testInstance = newInstance(testClass);
		}
		catch (Exception ex) {
			String message = String.format(
				"Failed to create test instance of type [%s] for test descriptor with unique ID [%s]",
				testClass.getName(), getTestDescriptor().getUniqueId());
			throw new IllegalStateException(message, ex);
		}

		postProcessTestInstance(context, testInstance);
		return testInstance;
	}

	protected void postProcessTestInstance(TestExecutionContext context, Object testInstance) {
		try {
			// @formatter:off
			context.getExtensions(InstancePostProcessor.class)
					.forEach(postProcessor -> postProcessor.postProcessTestInstance(testInstance));
			// @formatter:on
		}
		catch (Exception ex) {
			String message = String.format(
				"Failed to post-process test instance of type [%s] for test descriptor with unique ID [%s]",
				testInstance.getClass().getName(), getTestDescriptor().getUniqueId());
			throw new IllegalStateException(message, ex);
		}
	}

	@Override
	void executeBeforeEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance) {

		for (Method method : getBeforeEachMethods()) {
			invokeMethodInContext(method, methodContext, resolutionContext, testInstance);
		}
	}

	protected List<Method> getBeforeEachMethods() {
		return findAnnotatedMethods(getTestDescriptor().getTestClass(), BeforeEach.class,
			MethodSortOrder.HierarchyDown);
	}

	@Override
	void executeAfterEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance, List<Throwable> exceptionCollector) {

		for (Method method : getAfterEachMethods()) {
			invokeMethodInContextWithAggregatingExceptions(method, methodContext, resolutionContext, testInstance,
				exceptionCollector);
		}
	}

	protected List<Method> getAfterEachMethods() {
		return findAnnotatedMethods(getTestDescriptor().getTestClass(), AfterEach.class, MethodSortOrder.HierarchyUp);
	}

}
