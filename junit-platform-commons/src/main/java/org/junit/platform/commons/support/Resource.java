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

import static org.apiguardian.api.API.Status.DEPRECATED;

import java.net.URI;
import java.util.function.Predicate;

import org.apiguardian.api.API;

/**
 * {@code Resource} represents a resource on the classpath.
 *
 * <p><strong>WARNING</strong>: a {@code Resource} must provide correct
 * {@link Object#equals(Object) equals} and {@link Object#hashCode() hashCode}
 * implementations since a {@code Resource} may potentially be stored in a
 * collection or map.
 *
 * @since 1.11
 * @see ReflectionSupport#findAllResourcesInClasspathRoot(URI, Predicate)
 * @see ReflectionSupport#findAllResourcesInPackage(String, Predicate)
 * @see ReflectionSupport#findAllResourcesInModule(String, Predicate)
 * @see ReflectionSupport#streamAllResourcesInClasspathRoot(URI, Predicate)
 * @see ReflectionSupport#streamAllResourcesInPackage(String, Predicate)
 * @see ReflectionSupport#streamAllResourcesInModule(String, Predicate)
 * @deprecated Please use {@link org.junit.platform.commons.io.Resource} instead.
 */
@SuppressWarnings("removal")
@API(status = DEPRECATED, since = "6.0")
@Deprecated(since = "6.0", forRemoval = true)
public interface Resource extends org.junit.platform.commons.io.Resource {
	/**
	 * Create a new {@link Resource} with the given name and URI.
	 *
	 * @param name the name of the resource; never {@code null}
	 * @param uri the URI of the resource; never {@code null}
	 * @return a new {@code Resource}
	 * @since 6.0
	 */
	static Resource from(String name, URI uri) {
		return new DefaultResource(name, uri);
	}

	static Resource from(org.junit.platform.commons.io.Resource resource) {
		return new DefaultResource(resource.getName(), resource.getUri());
	}

}
