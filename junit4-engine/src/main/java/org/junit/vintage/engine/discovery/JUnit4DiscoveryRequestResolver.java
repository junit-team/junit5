/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static java.util.Arrays.asList;
import static org.junit.gen5.commons.meta.API.Usage.Internal;
import static org.junit.gen5.engine.Filter.composeFilters;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.Filter;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;
import org.junit.gen5.engine.support.filter.ExclusionReasonConsumingFilter;

/**
 * @since 5.0
 */
@API(Internal)
public class JUnit4DiscoveryRequestResolver {

	private final EngineDescriptor engineDescriptor;
	private final Logger logger;

	public JUnit4DiscoveryRequestResolver(EngineDescriptor engineDescriptor, Logger logger) {
		this.engineDescriptor = engineDescriptor;
		this.logger = logger;
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
			new UniqueIdSelectorResolver(logger)//
		);
	}

	private <T extends DiscoverySelector> void resolveSelectorsOfSingleType(EngineDiscoveryRequest discoveryRequest,
			DiscoverySelectorResolver<T> selectorResolver, TestClassCollector collector) {
		discoveryRequest.getSelectorsByType(selectorResolver.getSelectorClass()).forEach(
			selector -> selectorResolver.resolve(selector, collector));
	}

	private Set<TestClassRequest> filterAndConvertToTestClassRequests(EngineDiscoveryRequest discoveryRequest,
			TestClassCollector collector) {
		List<ClassFilter> allClassFilters = discoveryRequest.getDiscoveryFiltersByType(ClassFilter.class);
		Filter<Class<?>> classFilter = new ExclusionReasonConsumingFilter<>(composeFilters(allClassFilters),
			(testClass, reason) -> logger.info(() -> String.format("Class %s was excluded by a class filter: %s",
				testClass.getName(), reason.orElse("<unknown reason>"))));
		return collector.toRequests(classFilter.toPredicate());
	}

	private void populateEngineDescriptor(Set<TestClassRequest> requests) {
		new TestClassRequestResolver(engineDescriptor, logger).populateEngineDescriptorFrom(requests);
	}
}
