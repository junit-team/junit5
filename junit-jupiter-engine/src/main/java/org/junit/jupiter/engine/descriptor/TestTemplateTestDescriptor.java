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

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * {@link TestDescriptor} for {@link org.junit.jupiter.api.TestTemplate @TestTemplate}
 * methods.
 *
 * @since 5.0
 */
@API(Internal)
public class TestTemplateTestDescriptor extends JupiterTestDescriptor {

	private final Class<?> testClass;
	private final Method templateMethod;

	public TestTemplateTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method templateMethod) {
		super(uniqueId, determineDisplayName(Preconditions.notNull(templateMethod, "Method must not be null"),
			MethodTestDescriptor::generateDefaultDisplayName));

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.templateMethod = templateMethod;

		setSource(new MethodSource(templateMethod));
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public Method getTemplateMethod() {
		return templateMethod;
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public final Set<TestTag> getTags() {
		Set<TestTag> methodTags = getTags(templateMethod);
		getParent().ifPresent(parentDescriptor -> methodTags.addAll(parentDescriptor.getTags()));
		return methodTags;
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
		ExtensionRegistry registry = populateNewExtensionRegistryFromExtendWith(this.templateMethod,
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
			Consumer<TestDescriptor> dynamicTestExecutor) throws Exception {
		List<TestTemplateInvocationContextProvider> providers = context.getExtensionRegistry().getExtensions(
			TestTemplateInvocationContextProvider.class);
		ContainerExtensionContext containerExtensionContext = (ContainerExtensionContext) context.getExtensionContext();
		Preconditions.notEmpty(providers,
			"You need to register at least one TestTemplateInvocationContextProvider for this method");
		AtomicInteger invocationIndex = new AtomicInteger();
		providers.forEach(provider -> {
			Iterator<TestTemplateInvocationContext> contextIterator = provider.provide(containerExtensionContext);
			contextIterator.forEachRemaining(invocationContext -> {
				UniqueId uniqueId = getUniqueId().append("template-invocation",
					"#" + invocationIndex.incrementAndGet());
				TestDescriptor invocationTestDescriptor = new MethodTestDescriptor(uniqueId, this.testClass,
					this.templateMethod);
				addChild(invocationTestDescriptor);
				dynamicTestExecutor.accept(invocationTestDescriptor);
			});
		});
		return context;
	}

}
