/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Function;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.support.Resource;

/**
 * Resource-related predicates for use with {@link org.junit.platform.commons.support.ReflectionSupport}.
 *
 * @since 1.11
 */
@API(status = EXPERIMENTAL, since = "1.11")
public class ResourceFilter {

	public static final String DEFAULT_PACKAGE_NAME = "";
	private static final char CLASSPATH_RESOURCE_PATH_SEPARATOR = '/';
	private static final char PACKAGE_SEPARATOR_CHAR = '.';

	/**
	 * TODO:
	 */
	public static Function<Resource, Resource> loadClasspathResource() {
		return loadClasspathResource(ReflectionSupport::tryToLoadResource);
	}
	/**
	 * TODO: Improve or reconsider.
	 *
	 * Include only resources that can be loaded by a class loader.
	 * <p>
	 * Resources discovered by {@link org.junit.platform.commons.support.ReflectionSupport}
	 * may include identically named resources from different class
	 * path roots. To get
	 *
	 * @param loadResource function to load the resource, e.g. {@link org.junit.platform.commons.support.ReflectionSupport#tryToLoadResource(String)}.
	 * @return a function that for a given resource, returns the resource as it would bye loaded by {@link ClassLoader#getResource(String)}
	 *
	 * @see ReflectionUtils#tryToLoadResource(String)
	 * @see ReflectionUtils#tryToLoadResource(String, ClassLoader)
	 */

	public static Function<Resource, Resource> loadClasspathResource(Function<String, Try<Resource>> loadResource) {
		return candidate -> loadResource.apply(candidate.getName()) //
				.toOptional() //
				.map(loaded -> {
					if (!loaded.getUri().equals(candidate.getUri())) {
						return new ClasspathResource(candidate.getName(), loaded.getUri());
					}
					return candidate;

				})
				.orElse(candidate);
	}

	/**
	 * TODO: Doc
	 *
	 * A package filter is written to test {@code .} separated package names.
	 * Resources however have {@code /} separated paths. By rewriting the path
	 * of the resource into a package name, we can make the package filter work.
	 */
	public static Predicate<Resource> packageName(Predicate<String> packageFilter) {

		// TODO: Filter out invalid package names?
		return resource -> packageFilter.test(packageName(resource.getName()));
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

	private ResourceFilter() {

	}

}
