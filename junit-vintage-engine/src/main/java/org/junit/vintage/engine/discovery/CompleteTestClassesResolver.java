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
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage;
import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassNamePredicate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.PackageSelector;

class CompleteTestClassesResolver {

	Set<Class<?>> resolve(EngineDiscoveryRequest request, Predicate<Class<?>> classFilter) {
		Set<Class<?>> testClasses = new LinkedHashSet<>();
		Predicate<String> classNamePredicate = buildClassNamePredicate(request);
		resolveClasspathRootSelector(request, classFilter, classNamePredicate).forEach(testClasses::addAll);
		resolvePackageSelector(request, classFilter, classNamePredicate).forEach(testClasses::addAll);
		resolveClassSelector(request, classFilter).forEach(testClasses::add);
		return testClasses;
	}

	private Stream<List<Class<?>>> resolveClasspathRootSelector(EngineDiscoveryRequest request,
			Predicate<Class<?>> classFilter, Predicate<String> classNamePredicate) {
		// @formatter:off
		return request.getSelectorsByType(ClasspathRootSelector.class)
				.stream()
				.map(ClasspathRootSelector::getClasspathRoot)
				.map(root -> findAllClassesInClasspathRoot(root, classFilter, classNamePredicate));
		// @formatter:on
	}

	private Stream<List<Class<?>>> resolvePackageSelector(EngineDiscoveryRequest request,
			Predicate<Class<?>> classFilter, Predicate<String> classNamePredicate) {
		// @formatter:off
		return request.getSelectorsByType(PackageSelector.class)
				.stream()
				.map(PackageSelector::getPackageName)
				.map(packageName -> findAllClassesInPackage(packageName, classFilter, classNamePredicate));
		// @formatter:on
	}

	private Stream<? extends Class<?>> resolveClassSelector(EngineDiscoveryRequest request,
			Predicate<Class<?>> classFilter) {
		// @formatter:off
		return request.getSelectorsByType(ClassSelector.class)
				.stream()
				.map(ClassSelector::getJavaClass)
				.filter(classFilter);
		// @formatter:on
	}
}
