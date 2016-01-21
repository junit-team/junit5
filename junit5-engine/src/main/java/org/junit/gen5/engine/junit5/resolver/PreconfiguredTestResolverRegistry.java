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

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;

/**
 * @since 5.0
 */
public class PreconfiguredTestResolverRegistry implements TestResolverRegistry {
	private TestEngine testEngine;
	private Map<Class<? extends TestResolver>, TestResolver> testResolvers;

	public PreconfiguredTestResolverRegistry(TestEngine testEngine) {
		this.testEngine = testEngine;
		this.testResolvers = new LinkedHashMap<>();

		register(new PackageResolver());
		register(new ClassResolver());
		register(new NestedMemberClassResolver());
		register(new MethodResolver());
	}

	@Override
	public TestDescriptor fetchParent(DiscoverySelector selector, TestDescriptor root) {
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
		for (TestResolver testResolver : testResolvers.values()) {
			testResolver.resolveAllFrom(parent, discoveryRequest);
		}
	}

	@Override
	public void register(TestResolver testResolver) {
		// TODO Logging information (e.g. override existing, adding new one, etc.)
		testResolvers.put(testResolver.getClass(), testResolver);
		testResolver.initialize(testEngine, this);
	}
}
