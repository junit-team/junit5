/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.engine.junit5.descriptor.MethodInvocationContextFactory.methodInvocationContext;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.Leaf;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.execution.ConditionEvaluator;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;
import org.junit.gen5.engine.junit5.execution.RegisteredExtensionPoint;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;

/**
 * {@link TestDescriptor} for tests based on Java methods.
 *
 * @since 5.0
 */
public class MethodTestDescriptor extends JUnit5TestDescriptor implements Leaf<JUnit5EngineExecutionContext> {

	private final String displayName;

	private final Class<?> testClass;

	private final Method testMethod;

	MethodTestDescriptor(String uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId);

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.testMethod = Preconditions.notNull(testMethod, "Method must not be null");
		this.displayName = determineDisplayName(testMethod, testMethod.getName());

		setSource(new JavaSource(testMethod));
	}

	@Override
	public final Set<TestTag> getTags() {
		Set<TestTag> methodTags = getTags(getTestMethod());
		getParent().ifPresent(parentDescriptor -> methodTags.addAll(parentDescriptor.getTags()));
		return methodTags;
	}

	@Override
	public String getName() {
		// Intentionally get the class name via getTestClass() instead of
		// testMethod.getDeclaringClass() in order to ensure that inherited
		// test methods in different test subclasses get different names
		// via this method (e.g., for reporting purposes). The caller is,
		// however, still able to determine the declaring class via
		// reflection is necessary.

		// TODO Consider extracting JUnit 5's "method representation" into a common utility.
		return String.format("%s#%s(%s)", getTestClass().getName(), testMethod.getName(),
			StringUtils.nullSafeToString(testMethod.getParameterTypes()));
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	public final Method getTestMethod() {
		return this.testMethod;
	}

	@Override
	public final boolean isTest() {
		return true;
	}

	@Override
	public final boolean isContainer() {
		return false;
	}

	@Override
	public JUnit5EngineExecutionContext prepare(JUnit5EngineExecutionContext context) throws Exception {
		TestExtensionRegistry testExtensionRegistry = populateNewTestExtensionRegistryFromExtendWith(testMethod,
			context.getTestExtensionRegistry());
		Object testInstance = context.getTestInstanceProvider().getTestInstance();
		TestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(context.getExtensionContext(),
			this, testInstance);

		// @formatter:off
		return context.extend()
				.withTestExtensionRegistry(testExtensionRegistry)
				.withExtensionContext(testExtensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JUnit5EngineExecutionContext context) throws Exception {
		ConditionEvaluationResult evaluationResult = new ConditionEvaluator().evaluateForTest(
			context.getTestExtensionRegistry(), (TestExtensionContext) context.getExtensionContext());
		if (evaluationResult.isDisabled()) {
			return SkipResult.skip(evaluationResult.getReason().orElse(""));
		}
		return SkipResult.dontSkip();
	}

	@Override
	public JUnit5EngineExecutionContext execute(JUnit5EngineExecutionContext context) throws Exception {
		TestExtensionContext testExtensionContext = (TestExtensionContext) context.getExtensionContext();
		ThrowableCollector throwableCollector = new ThrowableCollector();

		invokeInstancePostProcessorExtensionPoints(context.getTestExtensionRegistry(), testExtensionContext);
		invokeBeforeEachExtensionPoints(context.getTestExtensionRegistry(), testExtensionContext);
		invokeTestMethod(testExtensionContext, context.getTestExtensionRegistry(), throwableCollector);
		invokeAfterEachExtensionPoints(context.getTestExtensionRegistry(), testExtensionContext, throwableCollector);

		throwableCollector.assertEmpty();

		return context;
	}

	private void invokeInstancePostProcessorExtensionPoints(TestExtensionRegistry testExtensionRegistry,
			TestExtensionContext testExtensionContext) throws Exception {

		Consumer<RegisteredExtensionPoint<InstancePostProcessor>> applyInstancePostProcessor = registeredExtensionPoint -> {
			executeAndMaskThrowable(
				() -> registeredExtensionPoint.getExtensionPoint().postProcessTestInstance(testExtensionContext));
		};

		testExtensionRegistry.stream(InstancePostProcessor.class,
			TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(applyInstancePostProcessor);
	}

	private void invokeBeforeEachExtensionPoints(TestExtensionRegistry testExtensionRegistry,
			TestExtensionContext testExtensionContext) throws Exception {

		Consumer<RegisteredExtensionPoint<BeforeEachExtensionPoint>> applyBeforeEach = registeredExtensionPoint -> {
			executeAndMaskThrowable(
				() -> registeredExtensionPoint.getExtensionPoint().beforeEach(testExtensionContext));
		};

		testExtensionRegistry.stream(BeforeEachExtensionPoint.class,
			TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(applyBeforeEach);
	}

	private void invokeTestMethod(TestExtensionContext testExtensionContext,
			TestExtensionRegistry testExtensionRegistry, ThrowableCollector throwableCollector) {

		throwableCollector.execute(() -> {
			MethodInvocationContext methodInvocationContext = methodInvocationContext(
				testExtensionContext.getTestInstance(), testExtensionContext.getTestMethod());
			new MethodInvoker(testExtensionContext, testExtensionRegistry).invoke(methodInvocationContext);
		});
	}

	private void invokeAfterEachExtensionPoints(TestExtensionRegistry testExtensionRegistry,
			TestExtensionContext testExtensionContext, ThrowableCollector throwableCollector) throws Exception {

		testExtensionRegistry.stream(AfterEachExtensionPoint.class,
			TestExtensionRegistry.ApplicationOrder.BACKWARD).forEach(registeredExtensionPoint -> {
				throwableCollector.execute(
					() -> registeredExtensionPoint.getExtensionPoint().afterEach(testExtensionContext));
			});
	}

}
