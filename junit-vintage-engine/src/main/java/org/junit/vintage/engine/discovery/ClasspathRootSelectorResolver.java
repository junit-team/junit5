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

import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot;

import java.util.Collection;
import java.util.function.Predicate;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClasspathRootSelector;

/**
 * @since 4.12
 */
class ClasspathRootSelectorResolver implements DiscoverySelectorResolver {

	private final Predicate<String> classNamePredicate;

	ClasspathRootSelectorResolver(Predicate<String> classNamePredicate) {
		this.classNamePredicate = classNamePredicate;
	}

	@Override
	public void resolve(EngineDiscoveryRequest request, Predicate<Class<?>> classFilter, TestClassCollector collector) {
		// @formatter:off
		request.getSelectorsByType(ClasspathRootSelector.class)
			.stream()
			.map(ClasspathRootSelector::getClasspathRoot)
			.map(root -> findAllClassesInClasspathRoot(root, classFilter, classNamePredicate))
			.flatMap(Collection::stream)
			.forEach(collector::addCompletely);
		// @formatter:on
	}

}
