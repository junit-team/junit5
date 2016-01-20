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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
	}

	@Override
	public void notifyResolvers(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		for (TestResolver testResolver : testResolvers.values()) {
			testResolver.resolveFor(parent, discoveryRequest);
		}
	}

	@Override
	public void notifyResolvers(Collection<TestDescriptor> parents, EngineDiscoveryRequest discoveryRequest) {
		for (TestDescriptor parent : parents) {
			notifyResolvers(parent, discoveryRequest);
		}
	}

	@Override
	public void register(TestResolver testResolver) {
		// TODO Logging information (e.g. override existing, adding new one, etc.)
		testResolvers.put(testResolver.getClass(), testResolver);
	}

	@Override
	public void initialize() {
		register(new PackageResolver());
		register(new ClassResolver());
		register(new NestedStaticClassResolver());
		register(new NestedMemberClassResolver());
		register(new MethodResolver());

		for (TestResolver testResolver : testResolvers.values()) {
			testResolver.initialize(testEngine, this);
		}
	}

	@Override
	public <R extends TestResolver> Optional<R> lookupTestResolver(Class<R> resolverType) {
		TestResolver testResolver = testResolvers.get(resolverType);
		return Optional.ofNullable((R) testResolver);
	}
}
