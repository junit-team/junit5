/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static java.util.Arrays.asList;
import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.engine.Filter.adaptFilter;
import static org.junit.platform.engine.Filter.composeFilters;
import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassNamePredicate;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.filter.ExclusionReasonConsumingFilter;

/**
 * @since 4.12
 */
@API(Internal)
public class JUnit4DiscoveryRequestResolver {

	private static final IsPotentialJUnit4TestClass isPotentialJUnit4TestClass = new IsPotentialJUnit4TestClass();
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
		for (DiscoverySelectorResolver selectorResolver : getAllDiscoverySelectorResolvers(discoveryRequest)) {
			selectorResolver.resolve(discoveryRequest, collector);
		}
		return collector;
	}

	private List<DiscoverySelectorResolver> getAllDiscoverySelectorResolvers(EngineDiscoveryRequest request) {
		Predicate<String> classNamePredicate = buildClassNamePredicate(request);
		return asList( //
			new ClasspathRootSelectorResolver(classNamePredicate), //
			new PackageNameSelectorResolver(classNamePredicate), //
			new ClassSelectorResolver(), //
			new MethodSelectorResolver(), //
			new UniqueIdSelectorResolver(logger)//
		);
	}

	private Set<TestClassRequest> filterAndConvertToTestClassRequests(EngineDiscoveryRequest discoveryRequest,
			TestClassCollector collector) {
		List<ClassNameFilter> allClassNameFilters = discoveryRequest.getDiscoveryFiltersByType(ClassNameFilter.class);
		Filter<Class<?>> adaptedFilter = adaptFilter(composeFilters(allClassNameFilters), Class::getName);
		Filter<Class<?>> classFilter = new ExclusionReasonConsumingFilter<>(adaptedFilter,
			(testClass, reason) -> logger.fine(() -> String.format("Class %s was excluded by a class filter: %s",
				testClass.getName(), reason.orElse("<unknown reason>"))));
		return collector.toRequests(classFilter.toPredicate().and(loggingPotentialJUnit4TestClassPredicate()));
	}

	private Predicate<Class<?>> loggingPotentialJUnit4TestClassPredicate() {
		return testClass -> {
			boolean isPotentialTestClass = isPotentialJUnit4TestClass.test(testClass);

			if (!isPotentialTestClass) {
				logger.info(() -> String.format("Class %s could not be resolved", testClass.getName()));
			}

			return isPotentialTestClass;
		};
	}

	private void populateEngineDescriptor(Set<TestClassRequest> requests) {
		new TestClassRequestResolver(engineDescriptor, logger).populateEngineDescriptorFrom(requests);
	}
}
