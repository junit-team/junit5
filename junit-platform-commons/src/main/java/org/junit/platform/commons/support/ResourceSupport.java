/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support;

import static org.apiguardian.api.API.Status.MAINTAINED;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.io.Resource;
import org.junit.platform.commons.io.ResourceFilter;
import org.junit.platform.commons.util.ReflectionUtils;

@API(status = MAINTAINED, since = "6.0")
public class ResourceSupport {

	/**
	 * Try to get the {@linkplain Resource resources} for the supplied classpath
	 * resource name.
	 *
	 * <p>The name of a <em>classpath resource</em> must follow the semantics
	 * for resource paths as defined in {@link ClassLoader#getResource(String)}.
	 *
	 * <p>If the supplied classpath resource name is prefixed with a slash
	 * ({@code /}), the slash will be removed.
	 *
	 * @param classpathResourceName the name of the resource to load; never
	 * {@code null} or blank
	 * @return a successful {@code Try} containing the set of loaded resources
	 * (potentially empty) or a failed {@code Try} containing the exception in
	 * case a failure occurred while trying to list resources; never
	 * {@code null}
	 * @since 1.12
	 * @see #tryToGetResources(String, ClassLoader)
	 */
	@API(status = MAINTAINED, since = "1.13.3")
	public static Try<Set<Resource>> tryToGetResources(String classpathResourceName) {
		return ReflectionUtils.tryToGetResources(classpathResourceName);
	}

	/**
	 * Try to load the {@linkplain Resource resources} for the supplied classpath
	 * resource name, using the supplied {@link ClassLoader}.
	 *
	 * <p>The name of a <em>classpath resource</em> must follow the semantics
	 * for resource paths as defined in {@link ClassLoader#getResource(String)}.
	 *
	 * <p>If the supplied classpath resource name is prefixed with a slash
	 * ({@code /}), the slash will be removed.
	 *
	 * @param classpathResourceName the name of the resource to load; never
	 * {@code null} or blank
	 * @param classLoader the {@code ClassLoader} to use; never {@code null}
	 * @return a successful {@code Try} containing the set of loaded resources
	 * (potentially empty) or a failed {@code Try} containing the exception in
	 * case a failure occurred while trying to list resources; never
	 * {@code null}
	 * @since 1.12
	 * @see #tryToGetResources(String)
	 */
	@API(status = MAINTAINED, since = "1.13.3")
	public static Try<Set<Resource>> tryToGetResources(String classpathResourceName, ClassLoader classLoader) {
		return ReflectionUtils.tryToGetResources(classpathResourceName, classLoader);
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
	 * @return an immutable list of all such resources found; never {@code null}
	 * but potentially empty
	 * @since 1.11
	 * @see #findAllResourcesInPackage(String, Predicate)
	 * @see #findAllResourcesInModule(String, Predicate)
	 */
	public static List<Resource> findAllResourcesInClasspathRoot(URI root, ResourceFilter resourceFilter) {
		return ReflectionUtils.findAllResourcesInClasspathRoot(root, resourceFilter);
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
	 * @return a stream of all such classes found; never {@code null}
	 * but potentially empty
	 * @since 1.11
	 * @see #streamAllResourcesInPackage(String, Predicate)
	 * @see #streamAllResourcesInModule(String, Predicate)
	 */
	public static Stream<Resource> streamAllResourcesInClasspathRoot(URI root, ResourceFilter resourceFilter) {
		return ReflectionUtils.streamAllResourcesInClasspathRoot(root, resourceFilter);
	}

	/**
	 * Find all {@linkplain Resource resources} in the supplied {@code basePackageName}
	 * that match the specified {@code resourceFilter} predicate.
	 *
	 * <p>The classpath scanning algorithm searches recursively in subpackages
	 * beginning within the supplied base package. The resulting list may include
	 * identically named resources from different classpath roots.
	 *
	 * @param basePackageName the name of the base package in which to start
	 * scanning; must not be {@code null} and must be valid in terms of Java
	 * syntax
	 * @param resourceFilter the resource type filter; never {@code null}
	 * @return an immutable list of all such classes found; never {@code null}
	 * but potentially empty
	 * @since 1.11
	 * @see #findAllResourcesInClasspathRoot(URI, Predicate)
	 * @see #findAllResourcesInModule(String, Predicate)
	 */
	public static List<Resource> findAllResourcesInPackage(String basePackageName, ResourceFilter resourceFilter) {
		return ReflectionUtils.findAllResourcesInPackage(basePackageName, resourceFilter);
	}

	/**
	 * Find all {@linkplain Resource resources} in the supplied {@code basePackageName}
	 * that match the specified {@code resourceFilter} predicate.
	 *
	 * <p>The classpath scanning algorithm searches recursively in subpackages
	 * beginning within the supplied base package. The resulting stream may
	 * include identically named resources from different classpath roots.
	 *
	 * @param basePackageName the name of the base package in which to start
	 * scanning; must not be {@code null} and must be valid in terms of Java
	 * syntax
	 * @param resourceFilter the resource type filter; never {@code null}
	 * @return a stream of all such resources found; never {@code null}
	 * but potentially empty
	 * @since 1.11
	 * @see #streamAllResourcesInClasspathRoot(URI, Predicate)
	 * @see #streamAllResourcesInModule(String, Predicate)
	 */
	public static Stream<Resource> streamAllResourcesInPackage(String basePackageName, ResourceFilter resourceFilter) {

		return ReflectionUtils.streamAllResourcesInPackage(basePackageName, resourceFilter);
	}

	/**
	 * Find all {@linkplain Resource resources} in the supplied {@code moduleName}
	 * that match the specified {@code resourceFilter} predicate.
	 *
	 * <p>The module-path scanning algorithm searches recursively in all
	 * packages contained in the module.
	 *
	 * @param moduleName the name of the module to scan; never {@code null} or
	 * <em>empty</em>
	 * @param resourceFilter the resource type filter; never {@code null}
	 * @return an immutable list of all such resources found; never {@code null}
	 * but potentially empty
	 * @since 1.11
	 * @see #findAllResourcesInClasspathRoot(URI, Predicate)
	 * @see #findAllResourcesInPackage(String, Predicate)
	 */
	public static List<Resource> findAllResourcesInModule(String moduleName, ResourceFilter resourceFilter) {
		return ReflectionUtils.findAllResourcesInModule(moduleName, resourceFilter);
	}

	/**
	 * Find all {@linkplain Resource resources} in the supplied {@code moduleName}
	 * that match the specified {@code resourceFilter} predicate.
	 *
	 * <p>The module-path scanning algorithm searches recursively in all
	 * packages contained in the module.
	 *
	 * @param moduleName the name of the module to scan; never {@code null} or
	 * <em>empty</em>
	 * @param resourceFilter the resource type filter; never {@code null}
	 * @return a stream of all such resources found; never {@code null}
	 * but potentially empty
	 * @since 1.11
	 * @see #streamAllResourcesInClasspathRoot(URI, Predicate)
	 * @see #streamAllResourcesInPackage(String, Predicate)
	 */
	@API(status = MAINTAINED, since = "1.13.3")
	public static Stream<Resource> streamAllResourcesInModule(String moduleName, ResourceFilter resourceFilter) {
		return ReflectionUtils.streamAllResourcesInModule(moduleName, resourceFilter);
	}

	private ResourceSupport() {
	}

}
