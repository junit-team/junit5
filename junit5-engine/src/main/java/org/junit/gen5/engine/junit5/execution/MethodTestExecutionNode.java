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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.gen5.api.After;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.extension.MethodArgumentResolver;
import org.junit.gen5.api.extension.TestDecorator;
import org.junit.gen5.api.extension.TestDecorators;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
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

	private final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();

	MethodTestExecutionNode(MethodTestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	@Override
	public MethodTestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	@Override
	public void execute(EngineExecutionContext context) {
		final Method testMethod = getTestDescriptor().getTestMethod();

		if (!this.conditionEvaluator.testEnabled(getTestDescriptor())) {
			// TODO Determine if we really need an explicit TestSkippedException.
			// TODO Provide a way for failed conditions to provide a detailed explanation
			// of why a condition failed (e.g., a text message).
			TestSkippedException testSkippedException = new TestSkippedException(
				String.format("Skipped test method [%s] due to failed condition", testMethod.toGenericString()));
			context.getTestExecutionListener().testSkipped(getTestDescriptor(), testSkippedException);

			// Abort execution of the test completely at this point.
			return;
		}

		context.getTestExecutionListener().testStarted(getTestDescriptor());
		Object testInstance = context.getTestInstances().peek();
		Class<?> testClass = testInstance.getClass();
		Throwable exceptionThrown = null;

		try {
			executeBeforeMethods(context, testClass, testInstance);
			invokeTestMethod(context, testInstance);
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

	private void invokeMethod(EngineExecutionContext context, Method method, Object target) {
		MethodArgumentResolverRegistry resolverRegistry = buildMethodArgumentResolverRegistry();
		populateRegistryForMethod(resolverRegistry, method);

		MethodInvoker methodInvoker = new MethodInvoker(method, target, resolverRegistry.getResolvers());

		// TODO Introduce factory for TestExecutionContext instances.
		// TODO Cache & reuse TestExecutionContext instance so extensions can share state.
		methodInvoker.invoke(new TestExecutionContext() {

			private final Map<String, Object> attributes = new HashMap<>();

			@Override
			public Optional<Class<?>> getTestClass() {
				return Optional.of(getTestDescriptor().getTestMethod().getDeclaringClass());
			}

			@Override
			public Optional<Method> getTestMethod() {
				return Optional.of(getTestDescriptor().getTestMethod());
			}

			@Override
			public Optional<Object> getTestInstance() {
				return Optional.of(target);
			}

			@Override
			public String getDisplayName() {
				return getTestDescriptor().getDisplayName();
			}

			@Override
			public Map<String, Object> getAttributes() {
				return this.attributes;
			}
		});
	}

	private void invokeTestMethod(EngineExecutionContext context, Object testInstance) {
		invokeMethod(context, getTestDescriptor().getTestMethod(), testInstance);
	}

	private void executeBeforeMethods(EngineExecutionContext context, Class<?> testClass, Object testInstance) {
		for (Method method : findAnnotatedMethods(testClass, Before.class, MethodSortOrder.HierarchyDown)) {
			invokeMethod(context, method, testInstance);
		}
	}

	private Throwable executeAfterMethods(EngineExecutionContext context, Class<?> testClass, Object testInstance,
			Throwable exceptionThrown) {

		for (Method method : findAnnotatedMethods(testClass, After.class, MethodSortOrder.HierarchyUp)) {
			try {
				invokeMethod(context, method, testInstance);
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

	private MethodArgumentResolverRegistry buildMethodArgumentResolverRegistry() {
		// TODO Determine where the MethodArgumentResolverRegistry should be created.
		return new MethodArgumentResolverRegistry();
	}

	private void populateRegistryForMethod(MethodArgumentResolverRegistry resolverRegistry, Method method) {
		// TODO Load and instantiate TestDecorators only once per test class/method.
		// In other words, we need to store the resolved/instantiated decorators in a
		// cache and *not* recreate them for every @Before, @Test, and @After method.
		findAnnotation(method.getDeclaringClass(), TestDecorators.class).map(TestDecorators::value).ifPresent(
			clazzes -> {
				for (Class<? extends TestDecorator> clazz : clazzes) {
					TestDecorator testDecorator = ReflectionUtils.newInstance(clazz);
					if (testDecorator instanceof MethodArgumentResolver) {
						resolverRegistry.addResolvers((MethodArgumentResolver) testDecorator);
					}
				}
			});
	}

}
