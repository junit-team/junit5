/*
 * Copyright 2015-2021 the original author or authors.
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.CombineTestTemplates;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.commons.support.AnnotationSupport;
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

		ExtensionContext extensionContext = new TestTemplateExtensionContext(context.getExtensionContext(),
			context.getExecutionListener(), this, context.getConfiguration(), testInstances);

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

		boolean productTestTemplates = AnnotationSupport.isAnnotated(extensionContext.getElement(),
			CombineTestTemplates.class);
		Stream<TestTemplateInvocationContext> invocationContexts;
		if (productTestTemplates) {
			invocationContexts = productOfTestTemplateContexts(providers, extensionContext).map(
				AdaptingTestTemplateExecutionContext::new);
		}
		else {
			invocationContexts = providers.stream().flatMap(
				provider -> provider.provideTestTemplateInvocationContexts(extensionContext));
		}

		// @formatter:off
		invocationContexts
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

	/**
	 * Allows the creation of a product of {@link TestTemplateInvocationContext TestTemplateInvocationContext} from a
	 * list of providers. Each provider will be asked for execution contexts and those will be combined.
	 * <p>
	 * 		If there are two Executors (for example the {@code RepeatedTestExtension} and the
	 * 		{@code ParameterizedTestExtension} and they provide templates [1, 2, 3] and [A, B] respectively; then this
	 * 		method will return a stream of the following items:
	 * </p>
	 * <ul>
	 *     <li>[1, A]</li>
	 *     <li>[1, B]</li>
	 *     <li>[2, A]</li>
	 *     <li>[2, B]</li>
	 *     <li>[3, A<]</li>
	 *     <li>[3, B]</li>
	 * </ul>
	 * <p>
	 *     The intention here is that this can then be passed to {@link AdaptingTestTemplateExecutionContext} to be executed.
	 * </p>
	 *
	 * @param providers the providers to use to generate the template contexts
	 * @param extensionContext the extension context to use when generating the test contexts
	 * @return a stream of test invocation context combinations
	 */
	private static Stream<List<TestTemplateInvocationContext>> productOfTestTemplateContexts(
			List<TestTemplateInvocationContextProvider> providers, ExtensionContext extensionContext) {
		if (providers.isEmpty()) {
			return Stream.of(Collections.emptyList());
		}

		TestTemplateInvocationContextProvider firstProvider = providers.get(0);
		List<TestTemplateInvocationContextProvider> tail = providers.subList(1, providers.size());

		return firstProvider.provideTestTemplateInvocationContexts(extensionContext).flatMap(
			context -> productOfTestTemplateContexts(tail, extensionContext).map(
				contexts -> Stream.concat(Stream.of(context), contexts.stream()).collect(toList())));
	}

	/**
	 * Adapt a List of {@link TestTemplateInvocationContext TestTemplateInvocationContexts} into a single one for
	 * execution. The methods on {@link TestTemplateInvocationContext} delegate to each of the wrapped elements in turn.
	 */
	private static class AdaptingTestTemplateExecutionContext implements TestTemplateInvocationContext {
		private final List<TestTemplateInvocationContext> delegates;

		private AdaptingTestTemplateExecutionContext(List<TestTemplateInvocationContext> delegates) {
			this.delegates = delegates;
		}

		@Override
		public List<Extension> getAdditionalExtensions() {
			return delegates.stream().flatMap(context -> context.getAdditionalExtensions().stream()).collect(
				Collectors.toList());
		}

		@Override
		public String getDisplayName(int invocationIndex) {
			return delegates.stream().map(context -> context.getDisplayName(invocationIndex)).collect(
				Collectors.joining(", ")) + " [Combined " + this.delegates.size() + " contexts. Execution "
					+ invocationIndex + "]";
		}
	}

}
