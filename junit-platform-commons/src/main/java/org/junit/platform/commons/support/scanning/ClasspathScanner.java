/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support.scanning;

import static org.apiguardian.api.API.Status;
import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.MAINTAINED;

import java.net.URI;
import java.util.List;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.io.Resource;
import org.junit.platform.commons.io.ResourceFilter;

/**
 * {@code ClasspathScanner} allows to scan the classpath for classes and
 * resources.
 *
 * <p>An implementation of this interface can be registered via the
 * {@link java.util.ServiceLoader ServiceLoader} mechanism.
 *
 * @since 1.12
 */
@API(status = Status.MAINTAINED, since = "1.13.3")
public interface ClasspathScanner {

	/**
	 * Find all {@linkplain Class classes} in the supplied classpath {@code root}
	 * that match the specified {@code classFilter} filter.
	 *
	 * <p>The classpath scanning algorithm searches recursively in subpackages
	 * beginning with the root of the classpath.
	 *
	 * @param basePackageName the name of the base package in which to start
	 * scanning; must not be {@code null} and must be valid in terms of Java
	 * syntax
	 * @param classFilter the class type filter; never {@code null}
	 * @return a list of all such classes found; never {@code null}
	 * but potentially empty
	 */
	List<Class<?>> scanForClassesInPackage(String basePackageName, ClassFilter classFilter);

	/**
	 * Find all {@linkplain Class classes} in the supplied classpath {@code root}
	 * that match the specified {@code classFilter} filter.
	 *
	 * <p>The classpath scanning algorithm searches recursively in subpackages
	 * beginning with the root of the classpath.
	 *
	 * @param root the URI for the classpath root in which to scan; never
	 * {@code null}
	 * @param classFilter the class type filter; never {@code null}
	 * @return a list of all such classes found; never {@code null}
	 * but potentially empty
	 */
	List<Class<?>> scanForClassesInClasspathRoot(URI root, ClassFilter classFilter);

	/**
	 * Find all {@linkplain Resource resources} in the supplied classpath {@code root}
	 * that match the specified {@code resourceFilter} predicate.
	 *
	 * <p>The classpath scanning algorithm searches recursively in subpackages
	 * beginning with the root of the classpath.
	 *
	 * @param basePackageName the name of the base package in which to start
	 * scanning; must not be {@code null} and must be valid in terms of Java
	 * syntax
	 * @param resourceFilter the resource type filter; never {@code null}
	 * @return a list of all such resources found; never {@code null}
	 * but potentially empty
	 * @deprecated Please use {@link #scanForResourcesInPackage(String, ResourceFilter)} instead.
	 */
	@API(status = DEPRECATED, since = "6.0")
	@Deprecated(since = "6.0", forRemoval = true)
	@SuppressWarnings("removal")
	default List<org.junit.platform.commons.support.Resource> scanForResourcesInPackage(String basePackageName,
			Predicate<org.junit.platform.commons.support.Resource> resourceFilter) {
		return toSupportResources(scanForResourcesInPackage(basePackageName, toResourceFilter(resourceFilter)));
	}

	/**
	 * Find all {@linkplain Resource resources} in the supplied classpath {@code root}
	 * that match the specified {@code resourceFilter} predicate.
	 *
	 * <p>The classpath scanning algorithm searches recursively in subpackages
	 * beginning with the root of the classpath.
	 *
	 * @param basePackageName the name of the base package in which to start
	 * scanning; must not be {@code null} and must be valid in terms of Java
	 * syntax
	 * @param resourceFilter the resource type filter; never {@code null}
	 * @return a list of all such resources found; never {@code null}
	 * but potentially empty
	 */
	@API(status = MAINTAINED, since = "6.0")
	List<Resource> scanForResourcesInPackage(String basePackageName, ResourceFilter resourceFilter);

	/**
	 * Find all {@linkplain Resource resources} in the supplied classpath {@code root}
	 * that match the specified {@code resourceFilter} predicate.
	 *
	 * <p>The classpath scanning algorithm searches recursively in subpackages
	 * beginning with the root of the classpath.
	 *
	 * @param root the URI for the classpath root in which to scan; never
	 * {@code null}
	 * @param resourceFilter the resource type filter; never {@code null}
	 * @return a list of all such resources found; never {@code null}
	 * but potentially empty
	 * @deprecated Please use {@link #scanForResourcesInClasspathRoot(URI, ResourceFilter)} instead.
	 */
	@API(status = DEPRECATED, since = "6.0")
	@Deprecated(since = "6.0", forRemoval = true)
	@SuppressWarnings("removal")
	default List<org.junit.platform.commons.support.Resource> scanForResourcesInClasspathRoot(URI root,
			Predicate<org.junit.platform.commons.support.Resource> resourceFilter) {
		return toSupportResources(scanForResourcesInClasspathRoot(root, toResourceFilter(resourceFilter)));
	}

	/**
	 * Find all {@linkplain Resource resources} in the supplied classpath {@code root}
	 * that match the specified {@code resourceFilter} predicate.
	 *
	 * <p>The classpath scanning algorithm searches recursively in subpackages
	 * beginning with the root of the classpath.
	 *
	 * @param root the URI for the classpath root in which to scan; never
	 * {@code null}
	 * @param resourceFilter the resource type filter; never {@code null}
	 * @return a list of all such resources found; never {@code null}
	 * but potentially empty
	 */
	@API(status = MAINTAINED, since = "6.0")
	List<Resource> scanForResourcesInClasspathRoot(URI root, ResourceFilter resourceFilter);

	@SuppressWarnings("removal")
	private static ResourceFilter toResourceFilter(
			Predicate<org.junit.platform.commons.support.Resource> resourceFilter) {
		return ResourceFilter.of(r -> resourceFilter.test(org.junit.platform.commons.support.Resource.from(r)));
	}

	@SuppressWarnings("removal")
	private static List<org.junit.platform.commons.support.Resource> toSupportResources(List<Resource> resources) {
		return resources.stream() //
				.map(org.junit.platform.commons.support.Resource::from) //
				.toList();
	}

}
