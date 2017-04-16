/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.extensions;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ContainerExecutionCondition;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExecutionCondition;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

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
	TestTemplateInvocationContextProvider,

	// @ParameterizedTest
	ArgumentConverter,
	ArgumentsProvider

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
	public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return false;
	}

	@Override
	public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return null;
	}

	// --- Conditional Test Execution ------------------------------------------

	@Override
	public ConditionEvaluationResult evaluate(ContainerExtensionContext context) {
		return null;
	}

	@Override
	public ConditionEvaluationResult evaluate(TestExtensionContext context) {
		return null;
	}

	// --- @TestTemplate -------------------------------------------------------

	@Override
	public boolean supports(ContainerExtensionContext context) {
		return false;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
		return null;
	}

	// --- @ParameterizedTest --------------------------------------------------

	@Override
	public Object convert(Object input, ParameterContext context) throws ArgumentConversionException {
		return null;
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ContainerExtensionContext context) throws Exception {
		return null;
	}

}
