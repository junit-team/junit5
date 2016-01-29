/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;

/**
 * @since 5.0
 */
public class PreconfiguredTestResolverRegistry implements TestResolverRegistry {
	private static final Logger LOG = Logger.getLogger(PreconfiguredTestResolverRegistry.class.getName());

	private Map<Class<? extends TestResolver>, TestResolver> testResolvers;

	public PreconfiguredTestResolverRegistry() {
		this.testResolvers = new LinkedHashMap<>();

		register(new EngineResolver());
		register(new PackageResolver());
		register(new ClassResolver());
		register(new NestedMemberClassResolver());
		register(new MethodResolver());
	}

	@Override
	public TestDescriptor fetchParent(DiscoverySelector selector, TestDescriptor root) {
		Preconditions.notNull(selector, "selector must not be null!");
		Preconditions.notNull(root, "root must not be null!");

		List<TestDescriptor> parents = new LinkedList<>();
		for (TestResolver testResolver : testResolvers.values()) {
			testResolver.fetchBySelector(selector, root).ifPresent(parents::add);
		}

		if (parents.isEmpty()) {
			return root;
		}
		else {
			if (parents.size() > 1) {
				logAmbiguousFetchResult(selector, root, parents);
			}
			return parents.get(0);
		}
	}

	@Override
	public void notifyResolvers(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		for (TestResolver testResolver : testResolvers.values()) {
			testResolver.resolveAllFrom(parent, discoveryRequest);
		}
	}

	@Override
	public void resolveUniqueId(TestDescriptor parent, UniqueId uniqueId, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(uniqueId, "remainingUniqueId must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		// The terminal operation of unique id resolution is controlled by the registry
		if (uniqueId.isEmpty()) {
			notifyResolvers(parent, discoveryRequest);
			return;
		}

		for (TestResolver testResolver : testResolvers.values()) {
			testResolver.resolveUniqueId(parent, uniqueId, discoveryRequest);
		}
	}

	@Override
	public void register(TestResolver testResolver) {
		Preconditions.notNull(testResolver, "testResolver must not be null!");

		logDebugInformationOnRegister(testResolver);

		testResolvers.put(testResolver.getClass(), testResolver);
		testResolver.bindTestResolveryRegistry(this);
	}

	private void logAmbiguousFetchResult(DiscoverySelector selector, TestDescriptor root,
			List<TestDescriptor> parents) {
		LOG.warning(() -> {
			StringBuilder parentsListing = new StringBuilder();
			int index = 1;
			for (TestDescriptor parent : parents) {
				parentsListing.append(String.format("\n\t[%d] %s", index++, parent.getUniqueId()));
			}

			// @formatter:off
			return String.format("Ambiguous fetch result: More than one parent found, using first! "
						+ "\n\tSelector: %s"
						+ "\n\tRoot: %s"
						+ "\n\tParents found: %s",
				selector.toString(), root.getUniqueId(), parentsListing.toString());
			// @formatter:on
		});
	}

	private void logDebugInformationOnRegister(TestResolver testResolver) {
		if (testResolvers.containsKey(testResolver.getClass())) {
			LOG.finer(() -> String.format( //
				"Adding test resolver %s to registry. This replaces the previous resolver %s", testResolver,
				testResolvers.get(testResolver.getClass())));
		}
		else {
			LOG.finer(() -> String.format("Adding test resolver %s to registry.", testResolver));
		}
	}
}
