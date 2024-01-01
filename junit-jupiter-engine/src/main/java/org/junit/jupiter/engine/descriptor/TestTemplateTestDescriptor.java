/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.descriptor.ExtensionUtils.populateNewExtensionRegistryFromExtendWithAnnotation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.DefaultExecutableInvoker;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * {@link TestDescriptor} for {@link org.junit.jupiter.api.TestTemplate @TestTemplate}
 * methods.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class TestTemplateTestDescriptor extends MethodBasedTestDescriptor implements Filterable {

	public static final String SEGMENT_TYPE = "test-template";
	private final DynamicDescendantFilter dynamicDescendantFilter = new DynamicDescendantFilter();

	public TestTemplateTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method templateMethod,
			JupiterConfiguration configuration) {
		super(uniqueId, testClass, templateMethod, configuration);
	}

	// --- Filterable ----------------------------------------------------------

	@Override
	public DynamicDescendantFilter getDynamicDescendantFilter() {
		return dynamicDescendantFilter;
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public boolean mayRegisterTests() {
		return true;
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) throws Exception {
		MutableExtensionRegistry registry = populateNewExtensionRegistryFromExtendWithAnnotation(
			context.getExtensionRegistry(), getTestMethod());

		// The test instance should be properly maintained by the enclosing class's ExtensionContext.
		TestInstances testInstances = context.getExtensionContext().getTestInstances().orElse(null);

		ExecutableInvoker executableInvoker = new DefaultExecutableInvoker(context);
		ExtensionContext extensionContext = new TestTemplateExtensionContext(context.getExtensionContext(),
			context.getExecutionListener(), this, context.getConfiguration(), testInstances, executableInvoker);

		// @formatter:off
		return context.extend()
				.withExtensionRegistry(registry)
				.withExtensionContext(extensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {

		ExtensionContext extensionContext = context.getExtensionContext();
		List<TestTemplateInvocationContextProvider> providers = validateProviders(extensionContext,
			context.getExtensionRegistry());
		AtomicInteger invocationIndex = new AtomicInteger();
		// @formatter:off
		providers.stream()
				.flatMap(provider -> provider.provideTestTemplateInvocationContexts(extensionContext))
				.map(invocationContext -> createInvocationTestDescriptor(invocationContext, invocationIndex.incrementAndGet()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(invocationTestDescriptor -> execute(dynamicTestExecutor, invocationTestDescriptor));
		// @formatter:on
		validateWasAtLeastInvokedOnce(invocationIndex.get(), providers);
		return context;
	}

	private List<TestTemplateInvocationContextProvider> validateProviders(ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry) {

		// @formatter:off
		List<TestTemplateInvocationContextProvider> providers = extensionRegistry.stream(TestTemplateInvocationContextProvider.class)
				.filter(provider -> provider.supportsTestTemplate(extensionContext))
				.collect(toList());
		// @formatter:on

		return Preconditions.notEmpty(providers,
			() -> String.format("You must register at least one %s that supports @TestTemplate method [%s]",
				TestTemplateInvocationContextProvider.class.getSimpleName(), getTestMethod()));
	}

	private Optional<TestDescriptor> createInvocationTestDescriptor(TestTemplateInvocationContext invocationContext,
			int index) {
		UniqueId uniqueId = getUniqueId().append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#" + index);
		if (getDynamicDescendantFilter().test(uniqueId, index - 1)) {
			return Optional.of(new TestTemplateInvocationTestDescriptor(uniqueId, getTestClass(), getTestMethod(),
				invocationContext, index, configuration));
		}
		return Optional.empty();
	}

	private void execute(DynamicTestExecutor dynamicTestExecutor, TestDescriptor testDescriptor) {
		testDescriptor.setParent(this);
		dynamicTestExecutor.execute(testDescriptor);
	}

	private void validateWasAtLeastInvokedOnce(int invocationIndex,
			List<TestTemplateInvocationContextProvider> providers) {

		Preconditions.condition(invocationIndex > 0,
			() -> "None of the supporting " + TestTemplateInvocationContextProvider.class.getSimpleName() + "s "
					+ providers.stream().map(provider -> provider.getClass().getSimpleName()).collect(
						joining(", ", "[", "]"))
					+ " provided a non-empty stream");
	}

}
