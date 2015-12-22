/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.engine.junit5.descriptor.MethodContextImpl.methodContext;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.api.extension.MethodContext;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.util.Preconditions;
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
		this.testClass = testClass;

		Preconditions.notNull(testMethod, "testMethod must not be null");

		this.testMethod = testMethod;
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
	public final String getDisplayName() {
		return this.displayName;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public final Method getTestMethod() {
		return this.testMethod;
	}

	@Override
	public final boolean isTest() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public JUnit5EngineExecutionContext prepare(JUnit5EngineExecutionContext context) throws Throwable {
		TestExtensionRegistry newTestExtensionRegistry = populateNewTestExtensionRegistryFromExtendWith(testMethod,
			context.getTestExtensionRegistry());
		Object testInstance = context.getTestInstanceProvider().getTestInstance();
		TestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(context.getExtensionContext(),
			this, testInstance);

		// @formatter:off
		return context.extend()
				.withTestExtensionRegistry(newTestExtensionRegistry)
				.withExtensionContext(testExtensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JUnit5EngineExecutionContext context) throws Throwable {
		ConditionEvaluationResult evaluationResult = new ConditionEvaluator().evaluateForTest(
			context.getTestExtensionRegistry(), (TestExtensionContext) context.getExtensionContext());
		if (evaluationResult.isDisabled())
			return SkipResult.skip(evaluationResult.getReason().orElse(""));
		return SkipResult.dontSkip();
	}

	@Override
	public JUnit5EngineExecutionContext execute(JUnit5EngineExecutionContext context) throws Throwable {

		TestExtensionContext testExtensionContext = (TestExtensionContext) context.getExtensionContext();

		invokeInstancePostProcessorExtensionPoints(context.getTestExtensionRegistry(), testExtensionContext);
		invokeBeforeEachExtensionPoints(context.getTestExtensionRegistry(), testExtensionContext);

		List<Throwable> throwablesCollector = new LinkedList<>();
		invokeTestMethod(testExtensionContext, context.getTestExtensionRegistry(), throwablesCollector);

		invokeAfterEachExtensionPoints(context.getTestExtensionRegistry(), testExtensionContext, throwablesCollector);
		throwIfAnyThrowablePresent(throwablesCollector);

		return context;
	}

	private void invokeInstancePostProcessorExtensionPoints(TestExtensionRegistry newTestExtensionRegistry,
			TestExtensionContext testExtensionContext) throws Throwable {
		Consumer<RegisteredExtensionPoint<InstancePostProcessor>> applyInstancePostProcessor = registeredExtensionPoint -> {
			executeAndWrapThrowables(
				() -> registeredExtensionPoint.getExtensionPoint().postProcessTestInstance(testExtensionContext));
		};
		executeAndUnwrapTargetExceptionWrapper(() -> newTestExtensionRegistry.stream(InstancePostProcessor.class,
			TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(applyInstancePostProcessor));
	}

	private void invokeBeforeEachExtensionPoints(TestExtensionRegistry newTestExtensionRegistry,
			TestExtensionContext testExtensionContext) throws Throwable {
		Consumer<RegisteredExtensionPoint<BeforeEachExtensionPoint>> applyBeforeEach = registeredExtensionPoint -> {
			executeAndWrapThrowables(
				() -> registeredExtensionPoint.getExtensionPoint().beforeEach(testExtensionContext));
		};
		executeAndUnwrapTargetExceptionWrapper(() -> newTestExtensionRegistry.stream(BeforeEachExtensionPoint.class,
			TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(applyBeforeEach));
	}

	private void invokeTestMethod(TestExtensionContext testExtensionContext,
			TestExtensionRegistry testExtensionRegistry, List<Throwable> throwablesCollector) {
		executeAndCollectThrowables(() -> {
			MethodContext methodContext = methodContext(testExtensionContext.getTestInstance(),
				testExtensionContext.getTestMethod());
			new MethodInvoker(testExtensionContext, testExtensionRegistry).invoke(methodContext);
		}, throwablesCollector);
	}

	private void invokeAfterEachExtensionPoints(TestExtensionRegistry newTestExtensionRegistry,
			TestExtensionContext testExtensionContext, List<Throwable> throwablesCollector) throws Throwable {
		newTestExtensionRegistry.stream(AfterEachExtensionPoint.class,
			TestExtensionRegistry.ApplicationOrder.BACKWARD).forEach(registeredExtensionPoint -> {
				executeAndCollectThrowables(
					() -> registeredExtensionPoint.getExtensionPoint().afterEach(testExtensionContext),
					throwablesCollector);
			});
	}

}
