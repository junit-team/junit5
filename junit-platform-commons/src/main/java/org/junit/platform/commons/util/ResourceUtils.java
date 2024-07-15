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

import static java.lang.String.format;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.support.Resource;

/**
 * Resource-related utilities to be used in conjunction with {@link ReflectionSupport}.
 *
 * @since 1.11
 */
@API(status = EXPERIMENTAL, since = "1.11")
public class ResourceUtils {
	private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

	public static final String DEFAULT_PACKAGE_NAME = "";
	private static final char CLASSPATH_RESOURCE_PATH_SEPARATOR = '/';
	private static final char PACKAGE_SEPARATOR_CHAR = '.';

	/**
	 * Maps a resource to its class loader version.
	 *
	 * <p>The class loader version of a resource has the uri that
	 * would be produced by calling {@link ClassLoader#getResource(String)}.
	 *
	 * <p>Resources discovered by {@link ReflectionSupport}
	 * may include identically named resources from different class
	 * path roots. After mapping these to their class loader version
	 * these can be deduplicated.
	 *
	 * @return a function that for a given resource, returns the "canonical" resource.
	 */
	public static Function<Resource, Optional<Resource>> getClassLoaderResource() {
		return getClassLoaderResource(ReflectionSupport::tryToGetResource);
	}

	/**
	 * Maps a resource to its "canonical" version.
	 *
	 * <p>The class loader version of a resource has the uri that
	 * would be produced by calling {@link ClassLoader#getResource(String)}.
	 *
	 * <p>Resources discovered by {@link ReflectionSupport}
	 * may include identically named resources from different class
	 * path roots. After mapping these to their class loader version
	 * these can be deduplicated.
	 *
	 * @param getResource function to get the resource, e.g. {@link ReflectionSupport#tryToGetResource(String)}.
	 * @return a function that for a given resource, returns the "canonical" resource.
	 */
	public static Function<Resource, Optional<Resource>> getClassLoaderResource(
			Function<String, Try<Resource>> getResource) {
		return candidate -> getResource.apply(candidate.getName()) //
				.andThenTry(loaded -> {
					if (loaded.getUri().equals(candidate.getUri())) {
						return candidate;
					}
					return new ClasspathResource(candidate.getName(), loaded.getUri());
				}) //
				.ifFailure(
					throwable -> logger.debug(throwable, () -> format("Failed to load [%s].", candidate.getName()))) //
				.toOptional();
	}

	/**
	 * Match resources against a package filter.
	 *
	 * <p>The {@code /} separated path of a resource is rewritten to a
	 * {@code .} separated package names. The package filter is applied to that
	 * package name.
	 */
	public static Predicate<Resource> packageName(Predicate<String> packageFilter) {
		// TODO: Filter out invalid package names? META-INF and such?
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

	private ResourceUtils() {

	}

}
