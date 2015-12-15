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

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5.resolver.TestResolver;
import org.junit.gen5.engine.junit5.resolver.TestResolverRegistry;

public class TestResolverSpy implements TestResolver {
	public List<TestResolverRequest> resolvedFor = new LinkedList<>();

	@Override
	public void initialize(TestEngine testEngine, TestResolverRegistry testResolverRegistry) {
	}

	@Override
	public void resolveFor(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		resolvedFor.add(new TestResolverRequest(parent, testPlanSpecification));
	}
}
