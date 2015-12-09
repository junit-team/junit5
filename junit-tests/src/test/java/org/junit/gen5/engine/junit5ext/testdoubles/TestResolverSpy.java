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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.resolver.TestResolver;

public class TestResolverSpy implements TestResolver {
	public List<TestResolverRequest> resolvedFor = new LinkedList<>();

	@Override
	public List<MutableTestDescriptor> resolveFor(MutableTestDescriptor parent,
			TestPlanSpecification testPlanSpecification) {
		resolvedFor.add(new TestResolverRequest(parent, testPlanSpecification));
		return Collections.emptyList();
	}
}
