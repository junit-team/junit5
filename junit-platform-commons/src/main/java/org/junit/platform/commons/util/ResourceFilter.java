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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.support.Resource;

/**
 * Resource-related predicate used by reflection utilities.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.11
 */
@API(status = INTERNAL, since = "1.11")
public class ResourceFilter implements Predicate<Resource> {

	/**
	 * Create a {@link ResourceFilter} instance that accepts all names but filters resources.
	 */
	public static ResourceFilter of(Predicate<Resource> resourcePredicate) {
		return of(name -> true, resourcePredicate);
	}

	/**
	 * Create a {@link ResourceFilter} instance that filters by resource names and resources.
	 */
	public static ResourceFilter of(Predicate<String> namePredicate, Predicate<Resource> resourcePredicate) {
		return new ResourceFilter(namePredicate, resourcePredicate);
	}

	private final Predicate<String> namePredicate;
	private final Predicate<Resource> resourcePredicate;

	private ResourceFilter(Predicate<String> namePredicate, Predicate<Resource> resourcePredicate) {
		this.namePredicate = Preconditions.notNull(namePredicate, "name predicate must not be null");
		this.resourcePredicate = Preconditions.notNull(resourcePredicate, "resource predicate must not be null");
	}

	public boolean match(String name) {
		return namePredicate.test(name);
	}

	public boolean match(Resource resource) {
		return resourcePredicate.test(resource);
	}

	/**
	 * @implNote This implementation combines all tests stored in the predicates
	 * of this instance. Any new predicate must be added to this test method as
	 * well.
	 */
	@Override
	public boolean test(Resource resource) {
		return match(resource.getName()) && match(resource);
	}
}
