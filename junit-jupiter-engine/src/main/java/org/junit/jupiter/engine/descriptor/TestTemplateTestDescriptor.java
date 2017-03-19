/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.opentest4j.TestAbortedException;

/**
 * {@link TestDescriptor} for {@link org.junit.jupiter.api.TestTemplate @TestTemplate}
 * methods.
 *
 * @since 5.0
 */
@API(Internal)
public class TestTemplateTestDescriptor extends MethodBasedTestDescriptor {

	public TestTemplateTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method templateMethod) {
		super(uniqueId, testClass, templateMethod);
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public boolean hasTests() {
		return true;
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) throws Exception {
		ExtensionRegistry registry = populateNewExtensionRegistryFromExtendWith(getTestMethod(),
			context.getExtensionRegistry());
		ContainerExtensionContext testExtensionContext = new TestTemplateContainerExtensionContext(
			context.getExtensionContext(), context.getExecutionListener(), this);

		// @formatter:off
		return context.extend()
				.withExtensionRegistry(registry)
				.withExtensionContext(testExtensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) throws Exception {
		return shouldContainerBeSkipped(context);
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {

		ContainerExtensionContext containerExtensionContext = (ContainerExtensionContext) context.getExtensionContext();
		List<TestTemplateInvocationContextProvider> providers = validateProviders(containerExtensionContext,
			context.getExtensionRegistry());
		AtomicInteger invocationIndex = new AtomicInteger();
		// @formatter:off
		providers.stream()
				.flatMap(provider -> provider.provide(containerExtensionContext))
				.map(invocationContext -> createInvocationTestDescriptor(invocationContext, invocationIndex.incrementAndGet()))
				.forEach(invocationTestDescriptor -> execute(dynamicTestExecutor, invocationTestDescriptor));
		// @formatter:on
		validateWasAtLeastInvokedOnce(invocationIndex.get());
		return context;
	}

	private List<TestTemplateInvocationContextProvider> validateProviders(
			ContainerExtensionContext containerExtensionContext, ExtensionRegistry extensionRegistry) {

		List<TestTemplateInvocationContextProvider> providers = extensionRegistry.getExtensions(
			TestTemplateInvocationContextProvider.class);
		Preconditions.notEmpty(providers,
			() -> String.format("You must register at least one %s for @TestTemplate method [%s]",
				TestTemplateInvocationContextProvider.class.getSimpleName(), getTestMethod()));
		// @formatter:off
		providers = providers.stream()
				.filter(provider -> provider.supports(containerExtensionContext))
				.collect(toList());
		// @formatter:on
		Preconditions.notEmpty(providers,
			() -> String.format("You must register at least one %s that supports @TestTemplate method [%s]",
				TestTemplateInvocationContextProvider.class.getSimpleName(), getTestMethod()));
		return providers;
	}

	private TestDescriptor createInvocationTestDescriptor(TestTemplateInvocationContext invocationContext, int index) {
		UniqueId uniqueId = getUniqueId().append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#" + index);
		return new TestTemplateInvocationTestDescriptor(uniqueId, getTestClass(), getTestMethod(), invocationContext,
			index);
	}

	private void execute(DynamicTestExecutor dynamicTestExecutor, TestDescriptor testDescriptor) {
		addChild(testDescriptor);
		dynamicTestExecutor.execute(testDescriptor);
	}

	private void validateWasAtLeastInvokedOnce(int invocationIndex) {
		if (invocationIndex == 0) {
			throw new TestAbortedException("No supporting "
					+ TestTemplateInvocationContextProvider.class.getSimpleName() + " provided an invocation context");
		}
	}

}
