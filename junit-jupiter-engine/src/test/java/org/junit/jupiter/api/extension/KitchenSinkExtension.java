/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

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

	// Dependency Injection
	TestInstanceFactory,
	TestInstancePostProcessor,
	ParameterResolver,

	// Conditional Test Execution
	ExecutionCondition,

	// @TestTemplate
	TestTemplateInvocationContextProvider

// @formatter:on
{

	// --- Lifecycle Callbacks -------------------------------------------------

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
	}

	// --- Dependency Injection ------------------------------------------------

	@Override
	public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
			throws TestInstantiationException {

		return null;
	}

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		return false;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

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

}
