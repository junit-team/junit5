/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.discovery;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.engine.Filter.adaptFilter;
import static org.junit.platform.engine.Filter.composeFilters;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.filter.ExclusionReasonConsumingFilter;

/**
 * @since 4.12
 */
@API(status = INTERNAL, since = "4.12")
public class VintageDiscoverer {

	private static final IsPotentialJUnit4TestClass isPotentialJUnit4TestClass = new IsPotentialJUnit4TestClass();
	private final CompleteTestClassesResolver completeTestClassesResolver;
	private final FilteredTestClassesResolver filteredTestClassesResolver;
	private final TestClassRequestResolver resolver;
	private final Logger logger;

	public VintageDiscoverer(Logger logger) {
		this.completeTestClassesResolver = new CompleteTestClassesResolver();
		this.filteredTestClassesResolver = new FilteredTestClassesResolver(logger);
		this.logger = logger;
		this.resolver = new TestClassRequestResolver(logger);
	}

	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "JUnit Vintage");
		// @formatter:off
		collectTestClasses(discoveryRequest)
				.toRequests()
				.map(request -> resolver.createRunnerTestDescriptor(request, uniqueId))
				.filter(Objects::nonNull)
				.forEach(engineDescriptor::addChild);
		// @formatter:on
		return engineDescriptor;
	}

	private TestClassCollector collectTestClasses(EngineDiscoveryRequest discoveryRequest) {
		Predicate<Class<?>> classFilter = createTestClassPredicate(discoveryRequest);
		Set<Class<?>> completeTestClasses = completeTestClassesResolver.resolve(discoveryRequest, classFilter);
		Map<Class<?>, List<RunnerTestDescriptorAwareFilter>> filteredTestClasses = filteredTestClassesResolver.resolve(
			discoveryRequest,
			//only include filtered classes that are not already included completely
			classFilter.and(notIn(completeTestClasses)));
		return new TestClassCollector(completeTestClasses, filteredTestClasses);
	}

	private Predicate<Class<?>> createTestClassPredicate(EngineDiscoveryRequest discoveryRequest) {
		List<ClassNameFilter> allClassNameFilters = discoveryRequest.getFiltersByType(ClassNameFilter.class);
		Filter<Class<?>> adaptedFilter = adaptFilter(composeFilters(allClassNameFilters), Class::getName);
		Filter<Class<?>> classFilter = new ExclusionReasonConsumingFilter<>(adaptedFilter,
			(testClass, reason) -> logger.debug(() -> String.format("Class %s was excluded by a class filter: %s",
				testClass.getName(), reason.orElse("<unknown reason>"))));
		return classFilter.toPredicate().and(isPotentialJUnit4TestClass);
	}

	private Predicate<? super Class<?>> notIn(Set<Class<?>> testClasses) {
		return testClass -> !testClasses.contains(testClass);
	}
}
