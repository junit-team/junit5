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
import java.util.Iterator;
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
	public boolean isTest() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return true;
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
		ExtensionRegistry registry = populateNewExtensionRegistryFromExtendWith(this.getTestMethod(),
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
		providers.forEach(provider -> {
			Iterator<TestTemplateInvocationContext> contextIterator = provider.provide(containerExtensionContext);
			contextIterator.forEachRemaining(invocationContext -> {
				int index = invocationIndex.incrementAndGet();
				TestDescriptor invocationTestDescriptor = createInvocationTestDescriptor(invocationContext, index);
				addChild(invocationTestDescriptor);
				dynamicTestExecutor.execute(invocationTestDescriptor);
			});
		});
		validateWasAtLeastInvokedOnce(invocationIndex);
		return context;
	}

	private List<TestTemplateInvocationContextProvider> validateProviders(
			ContainerExtensionContext containerExtensionContext, ExtensionRegistry extensionRegistry) {
		List<TestTemplateInvocationContextProvider> providers = extensionRegistry.getExtensions(
			TestTemplateInvocationContextProvider.class);
		Preconditions.notEmpty(providers, "You must register at least one "
				+ TestTemplateInvocationContextProvider.class.getSimpleName() + " for this method");
		// @formatter:off
		providers = providers.stream()
				.filter(provider -> provider.supports(containerExtensionContext))
				.collect(toList());
		// @formatter:on
		Preconditions.notEmpty(providers, "You must register at least one "
				+ TestTemplateInvocationContextProvider.class.getSimpleName() + " that supports this method");
		return providers;
	}

	private TestDescriptor createInvocationTestDescriptor(TestTemplateInvocationContext invocationContext, int index) {
		UniqueId uniqueId = getUniqueId().append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#" + index);
		return new TestTemplateInvocationTestDescriptor(uniqueId, this.getTestClass(), this.getTestMethod(),
			invocationContext, index);
	}

	private void validateWasAtLeastInvokedOnce(AtomicInteger invocationIndex) {
		if (invocationIndex.get() == 0) {
			throw new TestAbortedException("No supporting "
					+ TestTemplateInvocationContextProvider.class.getSimpleName() + " provided an invocation context");
		}
	}

}
