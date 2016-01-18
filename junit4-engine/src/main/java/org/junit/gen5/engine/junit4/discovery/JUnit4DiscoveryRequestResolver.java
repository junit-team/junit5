/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Set;

import org.junit.gen5.engine.ClassFilter;
import org.junit.gen5.engine.DiscoveryRequest;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.specification.AllClassFilters;

public class JUnit4DiscoveryRequestResolver {

	private final EngineDescriptor engineDescriptor;

	public JUnit4DiscoveryRequestResolver(EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolve(DiscoveryRequest discoveryRequest) {
		TestClassCollector collector = collectTestClasses(discoveryRequest);
		Set<TestClassRequest> requests = filterAndConvertToTestClassRequests(discoveryRequest, collector);
		populateEngineDescriptor(requests);
	}

	private TestClassCollector collectTestClasses(DiscoveryRequest discoveryRequest) {
		TestClassCollector collector = new TestClassCollector();
		for (DiscoverySelectorResolver<?> selectorResolver : getAllDiscoverySelectorResolvers()) {
			resolveSelectorsOfSingleType(discoveryRequest, selectorResolver, collector);
		}
		return collector;
	}

	private List<DiscoverySelectorResolver<?>> getAllDiscoverySelectorResolvers() {
		return asList( //
			new ClasspathSelectorResolver(), //
			new PackageNameSelectorResolver(), //
			new ClassSelectorResolver(), //
			new MethodSelectorResolver(), //
			new UniqueIdSelectorResolver(engineDescriptor.getEngine().getId())//
		);
	}

	private <T extends DiscoverySelector> void resolveSelectorsOfSingleType(DiscoveryRequest discoveryRequest,
			DiscoverySelectorResolver<T> selectorResolver, TestClassCollector collector) {
		discoveryRequest.getSelectoryByType(selectorResolver.getSelectorClass()).forEach(
			selector -> selectorResolver.resolve(selector, collector));
	}

	private Set<TestClassRequest> filterAndConvertToTestClassRequests(DiscoveryRequest request,
			TestClassCollector collector) {
		// TODO #40 Log classes that are filtered out
		ClassFilter classFilter = new AllClassFilters(request.getFilterByType(ClassFilter.class));
		return collector.toRequests(testClass -> classFilter.filter(testClass).included());
	}

	private void populateEngineDescriptor(Set<TestClassRequest> requests) {
		new TestClassRequestResolver(engineDescriptor).populateEngineDescriptorFrom(requests);
	}
}
