/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.testdoubles;

import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.resolver.TestResolver;
import org.junit.gen5.engine.junit5ext.resolver.TestResolverRegistry;

public class TestResolverRegistrySpy implements TestResolverRegistry {
	public List<TestResolverRequest> notifications = new LinkedList<>();
	public List<TestResolver> registeredTestResolvers = new LinkedList<>();

	@Override
	public void notifyResolvers(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		notifications.add(new TestResolverRequest(parent, testPlanSpecification));
	}

	@Override
	public void register(TestResolver testResolver) {
		registeredTestResolvers.add(testResolver);
	}
}