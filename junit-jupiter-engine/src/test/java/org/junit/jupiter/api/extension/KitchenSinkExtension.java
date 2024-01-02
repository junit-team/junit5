/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <em>Kitchen Sink</em> extension that implements every extension API
 * supported by JUnit Jupiter.
 *
 * <p>This extension should never actually be registered for any tests.
 * Rather, its sole purpose is to help ensure (via visual inspection)
 * that a concrete extension is able to implement all extension APIs
 * supported by JUnit Jupiter without any naming conflicts or
 * ambiguities with regard to method names or method signatures.
 * {@link ExtensionComposabilityTests}, on the other hand, serves
 * the same purpose in a dynamic and automated fashion.
 *
 * @since 5.0
 * @see ExtensionComposabilityTests
 */
// @formatter:off
public class KitchenSinkExtension implements

	// Lifecycle Callbacks
	BeforeAllCallback,
		BeforeEachCallback,
			BeforeTestExecutionCallback,
				TestExecutionExceptionHandler,
			AfterTestExecutionCallback,
		AfterEachCallback,
	AfterAllCallback,

	// Lifecycle methods exception handling
	LifecycleMethodExecutionExceptionHandler,

	// Dependency Injection
	TestInstancePreConstructCallback,
	TestInstanceFactory,
	TestInstancePostProcessor,
	TestInstancePreDestroyCallback,
	ParameterResolver,

	// Conditional Test Execution
	ExecutionCondition,

	// @TestTemplate
	TestTemplateInvocationContextProvider,

	// Miscellaneous
	TestWatcher,
	InvocationInterceptor

// @formatter:on
{

	// --- Lifecycle Callbacks -------------------------------------------------

	@Override
	public void beforeAll(ExtensionContext context) {
	}

	@Override
	public void beforeEach(ExtensionContext context) {
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) {
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
	}

	@Override
	public void afterEach(ExtensionContext context) {
	}

	@Override
	public void afterAll(ExtensionContext context) {
	}

	// --- Lifecycle methods exception handling

	@Override
	public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable) {
	}

	@Override
	public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) {
	}

	@Override
	public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) {
	}

	@Override
	public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable) {
	}

	// --- Dependency Injection ------------------------------------------------

	@Override
	public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
	}

	@Override
	public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) {
		return null;
	}

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
	}

	@Override
	public void preDestroyTestInstance(ExtensionContext context) {
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return false;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return null;
	}

	// --- Conditional Test Execution ------------------------------------------

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return null;
	}

	// --- @TestTemplate -------------------------------------------------------

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return false;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		return null;
	}

	// --- TestWatcher ---------------------------------------------------------

	@Override
	public void testDisabled(ExtensionContext context, Optional<String> reason) {
	}

	@Override
	public void testSuccessful(ExtensionContext context) {
	}

	@Override
	public void testAborted(ExtensionContext context, Throwable cause) {
	}

	@Override
	public void testFailed(ExtensionContext context, Throwable cause) {
	}

}
