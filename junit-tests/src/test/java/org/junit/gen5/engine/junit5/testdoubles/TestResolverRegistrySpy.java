/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testdoubles;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5.resolver.TestResolver;
import org.junit.gen5.engine.junit5.resolver.TestResolverRegistry;

public class TestResolverRegistrySpy implements TestResolverRegistry {
	public List<TestResolverRequest> notifications = new LinkedList<>();
	public List<TestResolver> registeredTestResolvers = new LinkedList<>();

	@Override
	public void notifyResolvers(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		notifications.add(new TestResolverRequest(parent, testPlanSpecification));
	}

	@Override
	public void notifyResolvers(List<TestDescriptor> parents, TestPlanSpecification testPlanSpecification) {
		parents.forEach(parent -> notifyResolvers(parent, testPlanSpecification));
	}

	@Override
	public void register(TestResolver testResolver) {
		registeredTestResolvers.add(testResolver);
	}

	@Override
	public void initialize() {
	}

	@Override
	public <R extends TestResolver> Optional<R> lookupTestResolver(Class<R> resolverType) {
		return Optional.empty();
	}
}