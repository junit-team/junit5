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

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

public class JUnit4DiscoveryRequestResolver {

	private final EngineDescriptor engineDescriptor;

	public JUnit4DiscoveryRequestResolver(EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolve(EngineDiscoveryRequest discoveryRequest) {
		TestClassCollector collector = collectTestClasses(discoveryRequest);
		Set<TestClassRequest> requests = filterAndConvertToTestClassRequests(discoveryRequest, collector);
		populateEngineDescriptor(requests);
	}

	private TestClassCollector collectTestClasses(EngineDiscoveryRequest discoveryRequest) {
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

	private <T extends DiscoverySelector> void resolveSelectorsOfSingleType(EngineDiscoveryRequest discoveryRequest,
			DiscoverySelectorResolver<T> selectorResolver, TestClassCollector collector) {
		discoveryRequest.getSelectorsByType(selectorResolver.getSelectorClass()).forEach(
			selector -> selectorResolver.resolve(selector, collector));
	}

	private Set<TestClassRequest> filterAndConvertToTestClassRequests(EngineDiscoveryRequest discoveryRequest,
			TestClassCollector collector) {
		// TODO #40 Log classes that are filtered out

		// @formatter:off
		List<ClassFilter> classFilters = discoveryRequest.getDiscoveryFiltersByType(ClassFilter.class);
		return collector.toRequests(
			testClass -> classFilters.stream()
					.map(classFilter -> classFilter.filter(testClass))
					.allMatch(FilterResult::included));
		// @formatter:on
	}

	private void populateEngineDescriptor(Set<TestClassRequest> requests) {
		new TestClassRequestResolver(engineDescriptor).populateEngineDescriptorFrom(requests);
	}
}
