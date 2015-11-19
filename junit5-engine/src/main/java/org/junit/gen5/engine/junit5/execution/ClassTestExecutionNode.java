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
import static org.junit.gen5.commons.util.ReflectionUtils.invokeMethod;
import static org.junit.gen5.commons.util.ReflectionUtils.newInstance;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Condition.Result;
import org.junit.gen5.api.TestInstance;
import org.junit.gen5.api.TestInstance.Lifecycle;
import org.junit.gen5.api.extension.MethodParameterResolver;
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
class ClassTestExecutionNode extends TestExecutionNode {

	private final ClassTestDescriptor testDescriptor;

	ClassTestExecutionNode(ClassTestDescriptor testDescriptor) {
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
		Object testInstance = (instancePerClass ? createTestInstance() : null);

		try {
			executeBeforeAllMethods(testClass, testInstance);
			for (TestExecutionNode child : getChildren()) {
				if (!instancePerClass) {
					testInstance = createTestInstance();
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

	private void executeAfterAllMethods(ExecutionRequest context, Class<?> testClass, Object testInstance) {
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
			context.getTestExecutionListener().testFailed(getTestDescriptor(), exceptionDuringAfterAll);
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

	Object createTestInstance() {
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
	void executeBeforeEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance) {
		List<Method> beforeEachMethods = getBeforeEachMethods();

		Set<MethodParameterResolver> parentResolvers = resolutionContext.getParameterResolvers();
		for (Method method : beforeEachMethods) {
			invokeMethodInContext(method, methodContext, parentResolvers, testInstance);
		}

	}

	protected List<Method> getBeforeEachMethods() {
		return findAnnotatedMethods(getTestDescriptor().getTestClass(), BeforeEach.class,
			MethodSortOrder.HierarchyDown);
	}

	@Override
	void executeAfterEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance, List<Throwable> exceptionCollector) {

		List<Method> afterEachMethods = getAfterEachMethods();

		Set<MethodParameterResolver> parentResolvers = resolutionContext.getParameterResolvers();
		for (Method method : afterEachMethods) {
			invokeMethodInContextWithAggregatingExceptions(method, methodContext, parentResolvers, testInstance,
				exceptionCollector);
		}
	}

	protected List<Method> getAfterEachMethods() {
		return findAnnotatedMethods(getTestDescriptor().getTestClass(), AfterEach.class, MethodSortOrder.HierarchyUp);
	}

}
