/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import java.util.stream.Stream;

/**
 * <em>Kitchen Sink</em> extension that implements every extension API
 * supported by JUnit Jupiter.
 *
 * <p>This extension should never actually be registered for any tests.
 * Rather, its sole purpose is to ensure that a concrete extension is
 * able to implement all extension APIs supported by JUnit Jupiter without
 * any naming conflicts or ambiguities with regard to method names or
 * method signatures.
 *
 * @since 5.0
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
	TestInstancePostProcessor,
	ParameterResolver,

	// Conditional Test Execution
	ContainerExecutionCondition,
	TestExecutionCondition,

	// @TestTemplate
	TestTemplateInvocationContextProvider

// @formatter:on
{

	// --- Lifecycle Callbacks -------------------------------------------------

	@Override
	public void beforeAll(ContainerExtensionContext context) throws Exception {
	}

	@Override
	public void beforeEach(TestExtensionContext context) throws Exception {
	}

	@Override
	public void beforeTestExecution(TestExtensionContext context) throws Exception {
	}

	@Override
	public void handleTestExecutionException(TestExtensionContext context, Throwable throwable) throws Throwable {
	}

	@Override
	public void afterTestExecution(TestExtensionContext context) throws Exception {
	}

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
	}

	@Override
	public void afterAll(ContainerExtensionContext context) throws Exception {
	}

	// --- Dependency Injection ------------------------------------------------

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
	public ConditionEvaluationResult evaluateContainerExecutionCondition(ContainerExtensionContext context) {
		return null;
	}

	@Override
	public ConditionEvaluationResult evaluateTestExecutionCondition(TestExtensionContext context) {
		return null;
	}

	// --- @TestTemplate -------------------------------------------------------

	@Override
	public boolean supportsTestTemplate(ContainerExtensionContext context) {
		return false;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
			ContainerExtensionContext context) {

		return null;
	}

}
