/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.discovery;

import static java.util.stream.Collectors.toSet;
import static org.junit.platform.commons.support.ReflectionSupport.findAllResourcesInClasspathRoot;
import static org.junit.platform.commons.support.ReflectionSupport.findAllResourcesInPackage;
import static org.junit.platform.commons.util.ReflectionUtils.findAllResourcesInModule;
import static org.junit.platform.commons.util.ResourceFilter.packageName;
import static org.junit.platform.commons.util.ResourceFilter.loadClasspathResource;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.selectors;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.platform.commons.support.Resource;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;

/**
 * @since 1.11
 */
class ResourceContainerSelectorResolver implements SelectorResolver {
	private static final Function<Resource, Resource> classpathResource = loadClasspathResource();
	private final Predicate<Resource> packageFilter;
	private final Predicate<Resource> resourceFilter;

	ResourceContainerSelectorResolver(Predicate<Resource> resourceFilter, Predicate<String> resourcePackageFilter) {
		this.packageFilter = packageName(resourcePackageFilter);
		this.resourceFilter = resourceFilter;
	}

	@Override
	public Resolution resolve(ClasspathRootSelector selector, Context context) {
		return resourceSelectors(
			findAllResourcesInClasspathRoot(selector.getClasspathRoot(), packageFilter));
	}

	@Override
	public Resolution resolve(ModuleSelector selector, Context context) {
		return resourceSelectors(
			findAllResourcesInModule(selector.getModuleName(), packageFilter));
	}

	@Override
	public Resolution resolve(PackageSelector selector, Context context) {
		return resourceSelectors(
			findAllResourcesInPackage(selector.getPackageName(), packageFilter));
	}

	private Resolution resourceSelectors(List<Resource> resources) {
		Set<ClasspathResourceSelector> classpathResources = resources.stream() //
				.map(classpathResource) //
				.filter(resourceFilter) //
				.map(DiscoverySelectors::selectClasspathResource) //
				.collect(toSet());

		if (classpathResources.isEmpty()) {
			return unresolved();
		}
		return selectors(classpathResources);
	}

}
