/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.descriptor.DisplayNameUtils.createDisplayNameSupplierForClass;
import static org.junit.jupiter.engine.discovery.DiscoverySelectorResolver.createTestDescriptor;
import static org.junit.jupiter.engine.discovery.predicates.IsTestClassWithTests.isTestOrTestFactoryOrTestTemplateMethod;
import static org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;
import static org.junit.platform.commons.support.ReflectionSupport.streamMethods;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.ExtensionContextSupplier;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.Node;

/**
 * @since 5.13
 */
@API(status = INTERNAL, since = "5.13")
public class ContainerTemplateTestDescriptor extends ClassBasedTestDescriptor {

	public static final String SEGMENT_TYPE = "container-template";

	public ContainerTemplateTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration) {
		super(uniqueId, testClass, createDisplayNameSupplierForClass(testClass, configuration), configuration);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Set<TestTag> getTags() {
		// return modifiable copy
		return new LinkedHashSet<>(this.tags);
	}

	@Override
	public boolean mayRegisterTests() {
		return true;
	}

	// --- ClassBasedTestDescriptor ---------------------------------------------

	@Override
	public List<Class<?>> getEnclosingTestClasses() {
		return emptyList();
	}

	@Override
	protected TestInstances instantiateTestClass(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionContextSupplier extensionContext, ExtensionRegistry registry,
			JupiterEngineExecutionContext context) {
		return instantiateTestClass(Optional.empty(), registry, extensionContext);
	}

	// --- ResourceLockAware ---------------------------------------------------

	@Override
	public Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> getResourceLocksProviderEvaluator() {
		return provider -> provider.provideForClass(getTestClass());
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		return super.prepare(context);
	}

	// TODO copied from ContainerTemplateTestDescriptor

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {
		ExtensionContext extensionContext = context.getExtensionContext();
		List<ContainerTemplateInvocationContextProvider> providers = validateProviders(extensionContext,
			context.getExtensionRegistry());
		AtomicInteger invocationIndex = new AtomicInteger();
		for (ContainerTemplateInvocationContextProvider provider : providers) {
			executeForProvider(provider, invocationIndex, dynamicTestExecutor, extensionContext);
		}
		return context;
	}

	private void executeForProvider(ContainerTemplateInvocationContextProvider provider, AtomicInteger invocationIndex,
			DynamicTestExecutor dynamicTestExecutor, ExtensionContext extensionContext) {

		int initialValue = invocationIndex.get();

		try (Stream<ContainerTemplateInvocationContext> stream = invocationContexts(provider, extensionContext)) {
			stream.forEach(invocationContext -> toTestDescriptor(invocationContext, invocationIndex.incrementAndGet()) //
					.ifPresent(testDescriptor -> execute(dynamicTestExecutor, testDescriptor)));
		}

		Preconditions.condition(
			invocationIndex.get() != initialValue
					|| provider.mayReturnZeroContainerTemplateInvocationContexts(extensionContext),
			String.format(
				"Provider [%s] did not provide any invocation contexts, but was expected to do so. "
						+ "You may override mayReturnZeroContainerTemplateInvocationContexts() to allow this.",
				provider.getClass().getSimpleName()));
	}

	private static Stream<ContainerTemplateInvocationContext> invocationContexts(
			ContainerTemplateInvocationContextProvider provider, ExtensionContext extensionContext) {
		return provider.provideContainerTemplateInvocationContexts(extensionContext);
	}

	private List<ContainerTemplateInvocationContextProvider> validateProviders(ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry) {

		// @formatter:off
		List<ContainerTemplateInvocationContextProvider> providers = extensionRegistry.stream(ContainerTemplateInvocationContextProvider.class)
				.filter(provider -> provider.supportsContainerTemplate(extensionContext))
				.collect(toList());
		// @formatter:on

		return Preconditions.notEmpty(providers,
			() -> String.format("You must register at least one %s that supports @ContainerTemplate class [%s]",
				ContainerTemplateInvocationContextProvider.class.getSimpleName(), getTestClass()));
	}

	private Optional<TestDescriptor> toTestDescriptor(ContainerTemplateInvocationContext invocationContext, int index) {
		UniqueId uniqueId = getUniqueId().append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#" + index);
		// TODO #871 filter descendants
		// TODO #871 support @Nested classes
		ClassTestDescriptor classDescriptor = new ClassTestDescriptor(uniqueId, getTestClass(), this.configuration);
		ContainerTemplateInvocationTestDescriptor containerDescriptor = new ContainerTemplateInvocationTestDescriptor(
			classDescriptor, invocationContext, index);
		streamMethods(getTestClass(), isTestOrTestFactoryOrTestTemplateMethod, TOP_DOWN) //
				.map(method -> createTestDescriptor(classDescriptor, getTestClass(), method, this.configuration)) //
				.forEach(methodDescriptor -> methodDescriptor.ifPresent(containerDescriptor::addChild));
		return Optional.of(containerDescriptor);
	}

	private void execute(Node.DynamicTestExecutor dynamicTestExecutor, TestDescriptor testDescriptor) {
		testDescriptor.setParent(this);
		dynamicTestExecutor.execute(testDescriptor);
	}
}
