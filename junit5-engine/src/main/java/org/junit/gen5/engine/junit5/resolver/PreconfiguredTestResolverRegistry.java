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

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;

/**
 * @since 5.0
 */
public class PreconfiguredTestResolverRegistry implements TestResolverRegistry {
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
			// TODO LOG warning, if (parents.size() > 1)!
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
	public void resolveUniqueId(TestDescriptor parent, UniqueId remainingUniqueId,
			EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(remainingUniqueId, "remainingUniqueId must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		// The terminal operation of unique id resolution is controlled by the registry
		if (remainingUniqueId.isEmpty()) {
			notifyResolvers(parent, discoveryRequest);
			return;
		}

		for (TestResolver testResolver : testResolvers.values()) {
			testResolver.resolveUniqueId(parent, remainingUniqueId, discoveryRequest);
		}
	}

	@Override
	public void register(TestResolver testResolver) {
		Preconditions.notNull(testResolver, "testResolver must not be null!");

		// TODO Logging information (e.g. override existing, adding new one, etc.)
		testResolvers.put(testResolver.getClass(), testResolver);
		testResolver.bindTestResolveryRegistry(this);
	}
}
