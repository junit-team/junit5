/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.stubs;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.resolver.TestResolver;
import org.junit.gen5.engine.junit5.resolver.TestResolverRegistry;
import org.junit.gen5.engine.junit5.resolver.UniqueId;

public class TestResolverRegistryMock implements TestResolverRegistry {
	public BiFunction<DiscoverySelector, TestDescriptor, TestDescriptor> fetchParentFunction = (selector, root) -> root;

	public EngineDiscoveryRequest discoveryRequest;
	public List<TestDescriptor> testDescriptors = new LinkedList<>();

	@Override
	public TestDescriptor fetchParent(DiscoverySelector selector, TestDescriptor root) {
		return fetchParentFunction.apply(selector, root);
	}

	@Override
	public void notifyResolvers(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		this.discoveryRequest = discoveryRequest;
		this.testDescriptors.add(parent);
	}

	@Override
	public void resolveUniqueId(TestDescriptor parent, UniqueId remainingUniqueId,
			EngineDiscoveryRequest discoveryRequest) {
	}

	@Override
	public void register(TestResolver testResolver) {
	}
}
