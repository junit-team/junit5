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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.engine.EngineDiscoveryRequest;

class FilteredTestClassesResolver {
	private final MethodSelectorResolver methodSelectorResolver;
	private final UniqueIdSelectorResolver uniqueIdSelectorResolver;

	FilteredTestClassesResolver(Logger logger) {
		methodSelectorResolver = new MethodSelectorResolver();
		uniqueIdSelectorResolver = new UniqueIdSelectorResolver(logger);
	}

	Map<Class<?>, List<RunnerTestDescriptorAwareFilter>> resolve(EngineDiscoveryRequest request,
			Predicate<Class<?>> classFilter) {
		Map<Class<?>, List<RunnerTestDescriptorAwareFilter>> filteredTestClasses = new LinkedHashMap<>();
		BiConsumer<Class<?>, RunnerTestDescriptorAwareFilter> addFilter = (testClass,
				filter) -> filteredTestClasses.computeIfAbsent(testClass, key -> new ArrayList<>()).add(filter);
		methodSelectorResolver.resolve(request, classFilter, addFilter);
		uniqueIdSelectorResolver.resolve(request, classFilter, addFilter);
		return filteredTestClasses;
	}
}
