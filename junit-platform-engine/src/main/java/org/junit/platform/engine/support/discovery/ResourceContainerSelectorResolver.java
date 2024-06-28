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
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.selectors;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;

import java.util.List;
import java.util.function.Predicate;

import org.junit.platform.commons.support.Resource;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;

/**
 * @since 1.11
 */
class ResourceContainerSelectorResolver implements SelectorResolver {
	private static final char CLASSPATH_RESOURCE_PATH_SEPARATOR = '/';
	private static final char PACKAGE_SEPARATOR_CHAR = '.';
	public static final String DEFAULT_PACKAGE_NAME = "";
	private final Predicate<Resource> resourceFilter;
	private final Predicate<String> resourceNameFilter;

	ResourceContainerSelectorResolver(Predicate<Resource> resourceFilter, Predicate<String> resourcePackageFilter) {
		this.resourceFilter = resourceFilter;
		this.resourceNameFilter = adaptPackageFilter(resourcePackageFilter);
	}

	/**
	 * A package filter is written to test {@code .} separated package names.
	 * Resources however have {@code /} separated paths. By rewriting the path
	 * of the resource into a package name, we can make the package filter work.
	 */
	private static Predicate<String> adaptPackageFilter(Predicate<String> packageFilter) {
		return resource -> packageFilter.test(packageName(resource));
	}

	private static String packageName(String classpathResourceName) {
		int lastIndexOf = classpathResourceName.lastIndexOf(CLASSPATH_RESOURCE_PATH_SEPARATOR);
		if (lastIndexOf < 0) {
			return DEFAULT_PACKAGE_NAME;
		}
		// classpath resource names do not start with /
		String resourcePackagePath = classpathResourceName.substring(0, lastIndexOf);
		return resourcePackagePath.replace(CLASSPATH_RESOURCE_PATH_SEPARATOR, PACKAGE_SEPARATOR_CHAR);
	}

	@Override
	public Resolution resolve(ClasspathRootSelector selector, Context context) {
		return resourceSelectors(
			findAllResourcesInClasspathRoot(selector.getClasspathRoot(), resourceFilter, resourceNameFilter));
	}

	@Override
	public Resolution resolve(ModuleSelector selector, Context context) {
		return resourceSelectors(
			findAllResourcesInModule(selector.getModuleName(), resourceFilter, resourceNameFilter));
	}

	@Override
	public Resolution resolve(PackageSelector selector, Context context) {
		return resourceSelectors(
			findAllResourcesInPackage(selector.getPackageName(), resourceFilter, resourceNameFilter));
	}

	private Resolution resourceSelectors(List<Resource> resources) {
		if (resources.isEmpty()) {
			return unresolved();
		}
		return selectors(resources.stream().map(DiscoverySelectors::selectClasspathResource).collect(toSet()));
	}

}
