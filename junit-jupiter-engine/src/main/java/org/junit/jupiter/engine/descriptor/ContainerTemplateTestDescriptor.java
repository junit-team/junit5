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

import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
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
public class ContainerTemplateTestDescriptor extends ClassBasedTestDescriptor implements Filterable {

	public static final String STATIC_CLASS_SEGMENT_TYPE = "container-template";
	public static final String NESTED_CLASS_SEGMENT_TYPE = "nested-container-template";

	private final DynamicDescendantFilter dynamicDescendantFilter = new DynamicDescendantFilter();
	private final Map<Integer, List<TestDescriptor>> childrenPrototypesByIndex = new HashMap<>();
	private final List<TestDescriptor> childrenPrototypes = new ArrayList<>();
	private final ClassBasedTestDescriptor delegate;

	public ContainerTemplateTestDescriptor(UniqueId uniqueId, ClassBasedTestDescriptor delegate) {
		super(uniqueId, delegate.getTestClass(), delegate.getDisplayName(), delegate.configuration);
		this.delegate = delegate;
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Set<TestTag> getTags() {
		// return modifiable copy
		return new LinkedHashSet<>(this.tags);
	}

	// --- Filterable ----------------------------------------------------------

	@Override
	public DynamicDescendantFilter getDynamicDescendantFilter() {
		return dynamicDescendantFilter;
	}

	// --- JupiterTestDescriptor -----------------------------------------------

	@Override
	protected JupiterTestDescriptor copyIncludingDescendants(UnaryOperator<UniqueId> uniqueIdTransformer) {
		ContainerTemplateTestDescriptor copy = (ContainerTemplateTestDescriptor) super.copyIncludingDescendants(
			uniqueIdTransformer);
		this.childrenPrototypes.forEach(oldChild -> {
			TestDescriptor newChild = ((JupiterTestDescriptor) oldChild).copyIncludingDescendants(uniqueIdTransformer);
			copy.childrenPrototypes.add(newChild);
		});
		this.childrenPrototypesByIndex.forEach((index, oldChildren) -> {
			List<TestDescriptor> newChildren = oldChildren.stream() //
					.map(oldChild -> ((JupiterTestDescriptor) oldChild).copyIncludingDescendants(uniqueIdTransformer)) //
					.collect(Collectors.toCollection(ArrayList::new));
			copy.childrenPrototypesByIndex.put(index, newChildren);
		});
		return copy;
	}

	@Override
	protected ContainerTemplateTestDescriptor withUniqueId(UniqueId newUniqueId) {
		return new ContainerTemplateTestDescriptor(newUniqueId, this.delegate);
	}

	@Override
	public void prunePriorToFiltering() {
		// do nothing to allow PostDiscoveryFilters to be applied first
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public void prune() {
		super.prune();
		this.children.forEach(child -> child.accept(TestDescriptor::prune));
		// Second iteration to avoid processing children that were pruned in the first iteration
		this.children.forEach(child -> {
			if (child instanceof ContainerTemplateInvocationTestDescriptor) {
				child.accept(it -> this.dynamicDescendantFilter.allowUniqueIdPrefix(it.getUniqueId()));
				this.childrenPrototypesByIndex.put(((ContainerTemplateInvocationTestDescriptor) child).getIndex(),
					new ArrayList<>(child.getChildren()));
			}
			else {
				this.childrenPrototypes.add(child);
			}
		});
		this.children.clear();
	}

	@Override
	public boolean mayRegisterTests() {
		return !childrenPrototypes.isEmpty();
	}

	// --- ClassBasedTestDescriptor ---------------------------------------------

	@Override
	public List<Class<?>> getEnclosingTestClasses() {
		return delegate.getEnclosingTestClasses();
	}

	@Override
	public TestInstances instantiateTestClass(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionContextSupplier extensionContext, ExtensionRegistry registry,
			JupiterEngineExecutionContext context) {
		return delegate.instantiateTestClass(parentExecutionContext, extensionContext, registry, context);
	}

	// --- ResourceLockAware ---------------------------------------------------

	@Override
	public Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> getResourceLocksProviderEvaluator() {
		return delegate.getResourceLocksProviderEvaluator();
	}

	// --- Node ----------------------------------------------------------------

	// TODO copied from TestTemplateTestDescriptor

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

	@Override
	public void cleanUp(JupiterEngineExecutionContext context) {
		this.childrenPrototypes.clear();
		this.childrenPrototypesByIndex.clear();
		this.dynamicDescendantFilter.allowAll();
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
				ContainerTemplateInvocationContextProvider.class.getSimpleName(), getTestClass().getName()));
	}

	private Optional<TestDescriptor> toTestDescriptor(ContainerTemplateInvocationContext invocationContext, int index) {
		UniqueId invocationUniqueId = getUniqueId().append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#" + index);
		if (getDynamicDescendantFilter().test(invocationUniqueId, index - 1)) {

			ContainerTemplateInvocationTestDescriptor containerInvocationDescriptor = new ContainerTemplateInvocationTestDescriptor(
				invocationUniqueId, this, invocationContext, index, getSource().orElse(null), this.configuration);

			// TODO #871 filter descendants

			collectChildren(index, invocationUniqueId) //
					.forEach(containerInvocationDescriptor::addChild);

			return Optional.of(containerInvocationDescriptor);
		}
		return Optional.empty();
	}

	private Stream<? extends TestDescriptor> collectChildren(int index, UniqueId invocationUniqueId) {
		if (this.childrenPrototypesByIndex.containsKey(index)) {
			return this.childrenPrototypesByIndex.remove(index).stream();
		}
		UnaryOperator<UniqueId> transformer = new UniqueIdPrefixTransformer(getUniqueId(), invocationUniqueId);
		return this.childrenPrototypes.stream() //
				.map(JupiterTestDescriptor.class::cast) //
				.map(it -> it.copyIncludingDescendants(transformer));
	}

	private static class UniqueIdPrefixTransformer implements UnaryOperator<UniqueId> {

		private final UniqueId oldPrefix;
		private final UniqueId newPrefix;
		private final int oldPrefixLength;

		UniqueIdPrefixTransformer(UniqueId oldPrefix, UniqueId newPrefix) {
			this.oldPrefix = oldPrefix;
			this.newPrefix = newPrefix;
			this.oldPrefixLength = oldPrefix.getSegments().size();
		}

		@Override
		public UniqueId apply(UniqueId uniqueId) {
			Preconditions.condition(uniqueId.hasPrefix(oldPrefix),
				() -> String.format("Unique ID %s does not have the expected prefix %s", uniqueId, oldPrefix));
			List<UniqueId.Segment> oldSegments = uniqueId.getSegments();
			List<UniqueId.Segment> suffix = oldSegments.subList(oldPrefixLength, oldSegments.size());
			UniqueId newValue = newPrefix;
			for (UniqueId.Segment segment : suffix) {
				newValue = newValue.append(segment);
			}
			return newValue;
		}
	}

	private static UniqueId changePrefix(UniqueId oldValue, UniqueId oldPrefix, UniqueId newPrefix) {
		List<UniqueId.Segment> oldSegments = oldValue.getSegments();
		Preconditions.condition(oldValue.hasPrefix(oldPrefix), () -> "Old value does not have the expected prefix");
		List<UniqueId.Segment> suffix = oldSegments.subList(oldPrefix.getSegments().size(), oldSegments.size());
		UniqueId newValue = newPrefix;
		for (UniqueId.Segment newSegment : suffix) {
			newValue = newValue.append(newSegment);
		}
		return newValue;
	}

	private void execute(Node.DynamicTestExecutor dynamicTestExecutor, TestDescriptor testDescriptor) {
		testDescriptor.setParent(this);
		dynamicTestExecutor.execute(testDescriptor);
	}
}
